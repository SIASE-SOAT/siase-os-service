package br.com.fiap.siase.infrastructure.adapters.output.dynamo;

import br.com.fiap.siase.application.ports.HistoricoEventoPort;
import br.com.fiap.siase.domain.model.EventoOs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.*;

/**
 * Histórico de eventos da OS no DynamoDB (NoSQL do serviço).
 * Tabela: hash key osId (S), range key timestampEvento (N).
 */
@Component
public class HistoricoEventoAdapter implements HistoricoEventoPort {

  private final DynamoDbClient dynamo;
  private final String tableName;

  public HistoricoEventoAdapter(DynamoDbClient dynamo,
                                @Value("${app.dynamodb.historico-table}") String tableName) {
    this.dynamo = dynamo;
    this.tableName = tableName;
  }

  @Override
  public void registrar(EventoOs evento) {
    Map<String, AttributeValue> item = new HashMap<>();
    item.put("osId", AttributeValue.fromS(evento.osId().toString()));
    item.put("timestampEvento", AttributeValue.fromN(
            String.valueOf(evento.ocorridoEm().toEpochMilli())));
    item.put("tipoEvento", AttributeValue.fromS(evento.tipoEvento()));
    item.put("numeroOs", AttributeValue.fromS(evento.numeroOs()));
    item.put("payload", AttributeValue.fromS(
            evento.payload() != null ? evento.payload() : ""));
    item.put("registradoEm", AttributeValue.fromS(Instant.now().toString()));

    dynamo.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
  }

  @Override
  public List<EventoOs> historicoDaOs(UUID osId) {
    QueryResponse response = dynamo.query(QueryRequest.builder()
            .tableName(tableName)
            .keyConditionExpression("osId = :os")
            .expressionAttributeValues(Map.of(
                    ":os", AttributeValue.fromS(osId.toString())))
            .scanIndexForward(true)
            .build());

    return response.items().stream().map(item -> new EventoOs(
            item.get("tipoEvento").s(),
            UUID.fromString(item.get("osId").s()),
            item.get("numeroOs").s(),
            item.get("payload").s(),
            Instant.ofEpochMilli(Long.parseLong(item.get("timestampEvento").n()))
    )).toList();
  }
}
