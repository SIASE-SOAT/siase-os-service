package br.com.fiap.siase.infrastructure.adapters.output.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.fiap.siase.application.ports.EventoPublicadorPort;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.infrastructure.observability.SqsCorrelationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Publica eventos do Saga nas filas SQS (RFC-001).
 * O correlationId vai como message attribute — rastreio ponta a ponta no Grafana.
 */
@Component
public class SqsEventoPublicadorAdapter implements EventoPublicadorPort {

  private static final Logger log = LoggerFactory.getLogger(SqsEventoPublicadorAdapter.class);

  private final SqsClient sqs;
  private final ObjectMapper mapper;
  private final String billingQueueUrl;
  private final String executionQueueUrl;
  private final String osEventosQueueUrl;

  public SqsEventoPublicadorAdapter(SqsClient sqs, ObjectMapper mapper,
                                    @Value("${app.sqs.billing-url}") String billingQueueUrl,
                                    @Value("${app.sqs.execution-url}") String executionQueueUrl,
                                    @Value("${app.sqs.os-eventos-url}") String osEventosQueueUrl) {
    this.sqs = sqs;
    this.mapper = mapper;
    this.billingQueueUrl = billingQueueUrl;
    this.executionQueueUrl = executionQueueUrl;
    this.osEventosQueueUrl = osEventosQueueUrl;
  }

  @Override
  public void publicarParaBilling(EventoOs evento) {
    enviar(billingQueueUrl, evento);
  }

  @Override
  public void publicarParaExecution(EventoOs evento) {
    enviar(executionQueueUrl, evento);
  }

  @Override
  public void publicarEventoDominio(EventoOs evento) {
    enviar(osEventosQueueUrl, evento);
  }

  private void enviar(String queueUrl, EventoOs evento) {
    SendMessageRequest request = SqsCorrelationId.withCorrelationId(
                    SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(serializar(evento)))
            .build();
    sqs.sendMessage(request);
    log.info("Evento {} da OS {} publicado em {}", evento.tipoEvento(), evento.numeroOs(), queueUrl);
  }

  private String serializar(EventoOs evento) {
    try {
      return mapper.writeValueAsString(evento);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar evento " + evento.tipoEvento(), e);
    }
  }
}
