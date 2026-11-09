package br.com.fiap.siase.bdd;

import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestAwsConfig {

    @Bean
    @Primary
    public SqsClient sqsClientMock() {
        return mock(SqsClient.class);
    }

    @Bean
    @Primary
    public DynamoDbClient dynamoDbClientMock() {
        return mock(DynamoDbClient.class);
    }

    @Bean
    @Primary
    public EventoOSRepositoryPort eventoOSRepositoryPortMock() {
        return mock(EventoOSRepositoryPort.class);
    }
}
