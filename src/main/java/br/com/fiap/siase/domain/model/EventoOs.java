package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de domínio trafegado via SQS no Saga coreografado (RFC-001).
 */
public record EventoOs(
        String tipoEvento,
        UUID osId,
        String numeroOs,
        String payload,
        Instant ocorridoEm
) {
    // Tipos produzidos pelo os-service
    public static final String OS_CRIADA = "OS_CRIADA";
    public static final String ORCAMENTO_APROVADO = "ORCAMENTO_APROVADO";
    public static final String ORCAMENTO_RECUSADO = "ORCAMENTO_RECUSADO";
    public static final String OS_CANCELADA = "OS_CANCELADA";

    // Tipos consumidos (publicados por billing/execution na fila os-updates)
    public static final String ORCAMENTO_GERADO = "ORCAMENTO_GERADO";
    public static final String EXECUCAO_INICIADA = "EXECUCAO_INICIADA";
    public static final String EXECUCAO_FINALIZADA = "EXECUCAO_FINALIZADA";
    public static final String PAGAMENTO_CONFIRMADO = "PAGAMENTO_CONFIRMADO";
    public static final String PAGAMENTO_CANCELADO = "PAGAMENTO_CANCELADO";
    public static final String EXECUCAO_CANCELADA = "EXECUCAO_CANCELADA";

    public static EventoOs de(String tipo, OrdemDeServico os) {
        return new EventoOs(tipo, os.getId(), os.getNumero(), null, Instant.now());
    }
}
