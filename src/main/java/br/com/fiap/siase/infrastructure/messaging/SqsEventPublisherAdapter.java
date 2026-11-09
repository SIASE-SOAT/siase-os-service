package br.com.fiap.siase.infrastructure.messaging;

import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SqsEventPublisherAdapter implements EventPublisherPort {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-url-os-eventos}")
    private String osEventosQueueUrl;

    @Value("${aws.sqs.queue-url-billing}")
    private String billingQueueUrl;

    @Value("${aws.sqs.queue-url-execution}")
    private String executionQueueUrl;

    @Override
    public void publicarOsCriada(OrdemDeServico os) {
        publicar(billingQueueUrl, "OS_CRIADA", os);
        log.info("Evento OS_CRIADA publicado para OS {}", os.getNumero());
    }

    @Override
    public void publicarStatusAlterado(OrdemDeServico os, String statusAnterior) {
        var payload = Map.of(
                "tipo", "STATUS_ALTERADO",
                "osNumero", os.getNumero(),
                "osId", os.getId().toString(),
                "statusAnterior", statusAnterior,
                "statusNovo", os.getStatus().name()
        );
        publicarRaw(osEventosQueueUrl, payload);
    }

    @Override
    public void publicarOsCancelada(OrdemDeServico os, String motivo) {
        var payload = Map.of(
                "tipo", "OS_CANCELADA",
                "osNumero", os.getNumero(),
                "osId", os.getId().toString(),
                "motivo", motivo != null ? motivo : ""
        );
        publicarRaw(billingQueueUrl, payload);
        publicarRaw(executionQueueUrl, payload);
        log.info("Evento OS_CANCELADA publicado para OS {}", os.getNumero());
    }

    @Override
    public void publicarOrcamentoAprovado(OrdemDeServico os) {
        publicar(executionQueueUrl, "ORCAMENTO_APROVADO", os);
        log.info("Evento ORCAMENTO_APROVADO publicado para OS {}", os.getNumero());
    }

    @Override
    public void publicarOrcamentoRecusado(OrdemDeServico os) {
        publicar(billingQueueUrl, "ORCAMENTO_RECUSADO", os);
        log.info("Evento ORCAMENTO_RECUSADO publicado para OS {}", os.getNumero());
    }

    private void publicar(String queueUrl, String tipo, OrdemDeServico os) {
        var payload = new java.util.HashMap<String, String>();
        payload.put("tipo", tipo);
        payload.put("osNumero", os.getNumero());
        payload.put("osId", os.getId().toString());
        payload.put("clienteId", os.getClienteId().toString());
        payload.put("clienteNome", os.getClienteNome() != null ? os.getClienteNome() : "");
        payload.put("clienteEmail", os.getClienteEmail() != null ? os.getClienteEmail() : "");
        payload.put("veiculoId", os.getVeiculoId().toString());
        payload.put("veiculoPlaca", os.getVeiculoPlaca() != null ? os.getVeiculoPlaca() : "");
        payload.put("veiculoModelo", os.getVeiculoModelo() != null ? os.getVeiculoModelo() : "");
        payload.put("total", os.getTotal().toPlainString());
        payload.put("status", os.getStatus().name());
        publicarRaw(queueUrl, payload);
    }

    private void publicarRaw(String queueUrl, Object payload) {
        try {
            String body = objectMapper.writeValueAsString(payload);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build());
        } catch (Exception e) {
            log.error("Falha ao publicar evento SQS: {}", e.getMessage(), e);
        }
    }
}
