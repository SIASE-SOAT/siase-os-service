package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Aggregate root: Ordem de Serviço.
 */
public class OrdemDeServico {

    private final UUID id;
    private final String numero;
    private final UUID clienteId;
    private final String veiculo;
    private final String descricaoProblema;
    private StatusOs status;
    private final List<ItemServico> itensServico = new ArrayList<>();
    private final List<ItemPeca> itensPeca = new ArrayList<>();
    private final Instant criadoEm;
    private Instant atualizadoEm;

    public OrdemDeServico(UUID id, String numero, UUID clienteId, String veiculo,
                          String descricaoProblema, StatusOs status,
                          Instant criadoEm, Instant atualizadoEm) {
        this.id = id;
        this.numero = numero;
        this.clienteId = clienteId;
        this.veiculo = veiculo;
        this.descricaoProblema = descricaoProblema;
        this.status = status;
        this.criadoEm = criadoEm;
        this.atualizadoEm = atualizadoEm;
    }

    public static OrdemDeServico criar(UUID clienteId, String veiculo, String descricaoProblema) {
        Instant agora = Instant.now();
        String numero = "OS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new OrdemDeServico(UUID.randomUUID(), numero, clienteId, veiculo,
                descricaoProblema, StatusOs.CRIADA, agora, agora);
    }

    public void transicionarPara(StatusOs destino) {
        if (!status.podeTransicionarPara(destino)) {
            throw new TransicaoStatusInvalidaException(
                    "Transição inválida: " + status + " -> " + destino);
        }
        this.status = destino;
        this.atualizadoEm = Instant.now();
    }

    public void cancelar() {
        transicionarPara(StatusOs.CANCELADA);
    }

    public void adicionarItemServico(ItemServico item) {
        itensServico.add(item);
    }

    public void adicionarItemPeca(ItemPeca item) {
        itensPeca.add(item);
    }

    public UUID getId() { return id; }
    public String getNumero() { return numero; }
    public UUID getClienteId() { return clienteId; }
    public String getVeiculo() { return veiculo; }
    public String getDescricaoProblema() { return descricaoProblema; }
    public StatusOs getStatus() { return status; }
    public List<ItemServico> getItensServico() { return List.copyOf(itensServico); }
    public List<ItemPeca> getItensPeca() { return List.copyOf(itensPeca); }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }

    public void restaurarItens(List<ItemServico> servicos, List<ItemPeca> pecas) {
        itensServico.addAll(servicos);
        itensPeca.addAll(pecas);
    }
}
