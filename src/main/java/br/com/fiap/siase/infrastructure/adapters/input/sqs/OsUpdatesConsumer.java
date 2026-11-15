package br.com.fiap.siase.infrastructure.adapters.input.sqs;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.fiap.siase.application.usecase.ProcessarEventoUpdateUseCase;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Polling consumer da fila os-updates (eventos de billing/execution).
 * Mensagem que falha volta à fila e após maxReceiveCount vai para a DLQ.
 */
@Component
public class OsUpdatesConsumer {

  private static final Logger log = LoggerFactory.getLogger(OsUpdatesConsumer.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final ProcessarEventoUpdateUseCase useCase;
  private final String queueUrl;

  public OsUpdatesConsumer(SqsClient sqs, ObjectMapper mapper,
                           ProcessarEventoUpdateUseCase useCase,
                           @Value("${app.sqs.os-updates-url}") String queueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.useCase = useCase;
    this.queueUrl = queueUrl;
  }

  @Scheduled(fixedDelayString = "${app.sqs.poll-delay-ms:5000}")
  public void poll() {
    var response = sqs.receiveMessage(ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)
            .messageAttributeNames("All")
            .build());

    for (Message message : response.messages()) {
      try {
        SqsCorrelationId.mdcFrom(message);
        EventoOs evento = mapper.readValue(message.body(), EventoOs.class);
        useCase.executar(evento);
        sqs.deleteMessage(DeleteMessageRequest.builder()
                .queueUrl(queueUrl)
                .receiptHandle(message.receiptHandle())
                .build());
      } catch (Exception e) {
        // não deleta: a mensagem volta à fila e, após 3 tentativas, vai para a DLQ
        log.error("Falha ao processar mensagem {}: {}", message.messageId(), e.getMessage(), e);
      } finally {
        MDC.clear();
      }
    }
  }
}
