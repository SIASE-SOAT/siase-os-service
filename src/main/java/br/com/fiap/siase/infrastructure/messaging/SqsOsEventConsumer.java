package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.application.usecase.AtualizarStatusPorEventoUC;
import br.com.fiap.siase.domain.enums.StatusOS;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsOsEventConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AtualizarStatusPorEventoUC atualizarStatusUC;

    @Value("${aws.sqs.queue-url-os-updates}")
    private String osUpdatesQueueUrl;

    @Scheduled(fixedDelayString = "${aws.sqs.poll-interval-ms:5000}")
    public void consumir() {
        try {
            var messages = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                    .queueUrl(osUpdatesQueueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5)
                    .build()).messages();

            for (var msg : messages) {
                try {
                    processar(msg.body());
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(osUpdatesQueueUrl)
                            .receiptHandle(msg.receiptHandle())
                            .build());
                } catch (Exception e) {
                    log.error("Erro ao processar mensagem SQS: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.warn("Erro ao consumir fila SQS: {}", e.getMessage());
        }
    }

    private void processar(String body) throws Exception {
        JsonNode node = objectMapper.readTree(body);
        String tipo = node.get("tipo").asText();
        String osNumero = node.get("osNumero").asText();

        switch (tipo) {
            case "ORCAMENTO_GERADO" ->
                atualizarStatusUC.executar(osNumero, StatusOS.AGUARDANDO_APROVACAO, "BILLING-SERVICE");
            case "EXECUCAO_INICIADA" ->
                atualizarStatusUC.executar(osNumero, StatusOS.EM_EXECUCAO, "EXECUTION-SERVICE");
            case "EXECUCAO_FINALIZADA" ->
                atualizarStatusUC.executar(osNumero, StatusOS.FINALIZADA, "EXECUTION-SERVICE");
            case "PAGAMENTO_CONFIRMADO" ->
                atualizarStatusUC.executar(osNumero, StatusOS.ENTREGUE, "BILLING-SERVICE");
            default -> log.debug("Tipo de evento ignorado: {}", tipo);
        }
    }
}
