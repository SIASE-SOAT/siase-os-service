package br.com.fiap.siase.application.usecase;

import br.com.fiap.siase.application.dto.input.CriarOsRequest;
import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.domain.enums.StatusOS;
import br.com.fiap.siase.domain.exception.BusinessException;
import br.com.fiap.siase.domain.model.EventoOS;
import br.com.fiap.siase.domain.model.ItemPeca;
import br.com.fiap.siase.domain.model.ItemServico;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.domain.port.EventPublisherPort;
import br.com.fiap.siase.domain.port.EventoOSRepositoryPort;
import br.com.fiap.siase.domain.port.OrdemServicoRepositoryPort;

import java.util.ArrayList;
import java.util.List;

public class CriarOsUC {

    private final OrdemServicoRepositoryPort repository;
    private final EventPublisherPort eventPublisher;
    private final EventoOSRepositoryPort eventoRepository;

    public CriarOsUC(OrdemServicoRepositoryPort repository,
                     EventPublisherPort eventPublisher,
                     EventoOSRepositoryPort eventoRepository) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.eventoRepository = eventoRepository;
    }

    public OsResponse executar(CriarOsRequest request) {
        if (repository.existsByVeiculoIdAndStatusNotIn(
                request.veiculoId(), List.of(StatusOS.ENTREGUE, StatusOS.CANCELADA))) {
            throw new BusinessException("Veículo já possui OS em andamento.");
        }

        var os = OrdemDeServico.builder()
                .numero(OrdemDeServico.gerarNumero())
                .clienteId(request.clienteId())
                .clienteNome(request.clienteNome())
                .clienteEmail(request.clienteEmail())
                .veiculoId(request.veiculoId())
                .veiculoPlaca(request.veiculoPlaca())
                .veiculoModelo(request.veiculoModelo())
                .observacoes(request.observacoes())
                .itensServico(new ArrayList<>())
                .itensPeca(new ArrayList<>())
                .build();

        if (request.itensServico() != null) {
            for (var req : request.itensServico()) {
                os.getItensServico().add(ItemServico.builder()
                        .servicoId(req.servicoId())
                        .servicoNome(req.servicoNome())
                        .precoUnitario(req.preco())
                        .tempoEstimadoMinutos(req.tempoEstimadoMinutos())
                        .observacoes(req.observacoes())
                        .build());
            }
        }

        if (request.itensPeca() != null) {
            for (var req : request.itensPeca()) {
                os.getItensPeca().add(ItemPeca.builder()
                        .pecaId(req.pecaId())
                        .pecaCodigo(req.pecaCodigo())
                        .pecaNome(req.pecaNome())
                        .quantidade(req.quantidade())
                        .precoUnitario(req.preco())
                        .build());
            }
        }

        os.recalcularTotais();
        var salvo = repository.save(os);

        eventoRepository.salvar(EventoOS.criar(salvo, null, "OS-SERVICE", "OS criada"));
        eventPublisher.publicarOsCriada(salvo);

        return OsResponse.from(salvo);
    }
}
