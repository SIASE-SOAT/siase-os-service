package br.com.fiap.siase.application;

import br.com.fiap.siase.application.dto.input.CriarOsRequest;
import br.com.fiap.siase.application.usecase.CriarOsUC;
import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CriarOsUCTest {

    @Mock OrdemServicoRepositoryPort repository;
    @Mock EventPublisherPort eventPublisher;
    @Mock EventoOSRepositoryPort eventoRepository;

    CriarOsUC useCase;

    @BeforeEach
    void setUp() {
        useCase = new CriarOsUC(repository, eventPublisher, eventoRepository);
    }

    @Test
    void deveCriarOsComSucesso() {
        var clienteId = UUID.randomUUID();
        var veiculoId = UUID.randomUUID();
        var request = new CriarOsRequest(clienteId, "João Silva", "joao@test.com",
                veiculoId, "ABC-1234", "Civic", "Revisão geral", List.of(), List.of());

        var osSalva = OrdemDeServico.builder()
                .id(UUID.randomUUID())
                .numero("OS-20250101-ABC123")
                .clienteId(clienteId)
                .clienteNome("João Silva")
                .clienteEmail("joao@test.com")
                .veiculoId(veiculoId)
                .veiculoPlaca("ABC-1234")
                .status(StatusOS.RECEBIDA)
                .build();

        when(repository.existsByVeiculoIdAndStatusNotIn(any(), any())).thenReturn(false);
        when(repository.save(any())).thenReturn(osSalva);

        var response = useCase.executar(request);

        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("RECEBIDA");
        verify(eventPublisher).publicarOsCriada(any());
        verify(eventoRepository).salvar(any());
    }

    @Test
    void deveRejeitarCriacaoQuandoVeiculoJaPossuiOsAtiva() {
        var request = new CriarOsRequest(UUID.randomUUID(), "João", "joao@test.com",
                UUID.randomUUID(), "ABC-1234", null, null, null, null);
        when(repository.existsByVeiculoIdAndStatusNotIn(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> useCase.executar(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("OS em andamento");
    }

    @Test
    void naoDevePublicarEventoQuandoFalhaNoPersistir() {
        var request = new CriarOsRequest(UUID.randomUUID(), "João", "joao@test.com",
                UUID.randomUUID(), "ABC-1234", null, null, null, null);
        when(repository.existsByVeiculoIdAndStatusNotIn(any(), any())).thenReturn(false);
        when(repository.save(any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> useCase.executar(request))
                .isInstanceOf(RuntimeException.class);
        verify(eventPublisher, never()).publicarOsCriada(any());
    }
}
