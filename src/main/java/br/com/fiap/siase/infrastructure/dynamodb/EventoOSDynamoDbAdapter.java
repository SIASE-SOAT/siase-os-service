package br.com.fiap.siase.infrastructure.dynamodb;

import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventoOSDynamoDbAdapter implements EventoOSRepositoryPort {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-eventos:siase-os-eventos}")
    private String tableName;

    @Override
    public void salvar(EventoOS evento) {
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("id", AttributeValue.fromS(evento.getId()));
            item.put("osId", AttributeValue.fromS(evento.getOsId()));
            item.put("osNumero", AttributeValue.fromS(evento.getOsNumero()));
            item.put("statusNovo", AttributeValue.fromS(evento.getStatusNovo()));
            item.put("origem", AttributeValue.fromS(evento.getOrigem()));
            item.put("descricao", AttributeValue.fromS(evento.getDescricao()));
            item.put("ocorridoEm", AttributeValue.fromS(evento.getOcorridoEm().toString()));
            if (evento.getStatusAnterior() != null)
                item.put("statusAnterior", AttributeValue.fromS(evento.getStatusAnterior()));

            dynamoDbClient.putItem(PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build());
        } catch (Exception e) {
            log.warn("Falha ao salvar evento no DynamoDB (não crítico): {}", e.getMessage());
        }
    }

    @Override
    public List<EventoOS> findByOsId(String osId) {
        try {
            var response = dynamoDbClient.query(QueryRequest.builder()
                    .tableName(tableName)
                    .indexName("osId-index")
                    .keyConditionExpression("osId = :osId")
                    .expressionAttributeValues(Map.of(":osId", AttributeValue.fromS(osId)))
                    .build());

            return response.items().stream().map(this::toEvento).toList();
        } catch (Exception e) {
            log.warn("Falha ao consultar eventos no DynamoDB: {}", e.getMessage());
            return List.of();
        }
    }

    private EventoOS toEvento(Map<String, AttributeValue> item) {
        return EventoOS.builder()
                .id(item.get("id").s())
                .osId(item.get("osId").s())
                .osNumero(item.get("osNumero").s())
                .statusAnterior(item.containsKey("statusAnterior") ? item.get("statusAnterior").s() : null)
                .statusNovo(item.get("statusNovo").s())
                .origem(item.get("origem").s())
                .descricao(item.get("descricao").s())
                .ocorridoEm(java.time.LocalDateTime.parse(item.get("ocorridoEm").s()))
                .build();
    }
}
