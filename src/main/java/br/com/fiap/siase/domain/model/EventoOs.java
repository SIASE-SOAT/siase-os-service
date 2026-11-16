package br.com.fiap.siase.domain.model;

import java.time.Instant;
import java.util.List;
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
        String payload = OS_CRIADA.equals(tipo) || ORCAMENTO_APROVADO.equals(tipo)
                ? payloadContexto(os)
                : null;
        return new EventoOs(tipo, os.getId(), os.getNumero(), payload, Instant.now());
    }

    private static String payloadContexto(OrdemDeServico os) {
        StringBuilder json = new StringBuilder("{");
        campoTexto(json, "clienteNome", os.getClienteNome());
        campoTexto(json, "veiculo", os.getVeiculo());
        if (os.getValorOrcamento() != null) {
            separador(json).append("\"valorOrcamento\":")
                    .append(os.getValorOrcamento().toPlainString());
        }
        servicos(json, os.getItensServico());
        pecas(json, os.getItensPeca());
        return json.append("}").toString();
    }

    private static void campoTexto(StringBuilder json, String campo, String valor) {
        if (valor == null) return;
        separador(json).append("\"").append(campo).append("\":\"")
                .append(escape(valor)).append("\"");
    }

    private static void servicos(StringBuilder json, List<ItemServico> itens) {
        if (itens == null || itens.isEmpty()) return;
        separador(json).append("\"servicos\":[");
        for (int i = 0; i < itens.size(); i++) {
            ItemServico item = itens.get(i);
            if (i > 0) json.append(',');
            json.append("{\"descricao\":\"").append(escape(item.descricao())).append("\"");
            if (item.preco() != null) {
                json.append(",\"preco\":").append(item.preco().toPlainString());
            }
            json.append('}');
        }
        json.append(']');
    }

    private static void pecas(StringBuilder json, List<ItemPeca> itens) {
        if (itens == null || itens.isEmpty()) return;
        separador(json).append("\"pecas\":[");
        for (int i = 0; i < itens.size(); i++) {
            ItemPeca item = itens.get(i);
            if (i > 0) json.append(',');
            json.append("{\"nome\":\"").append(escape(item.nome()))
                    .append("\",\"quantidade\":").append(item.quantidade());
            if (item.precoUnitario() != null) {
                json.append(",\"precoUnitario\":").append(item.precoUnitario().toPlainString());
            }
            json.append('}');
        }
        json.append(']');
    }

    private static StringBuilder separador(StringBuilder json) {
        if (json.charAt(json.length() - 1) != '{') json.append(',');
        return json;
    }

    private static String escape(String valor) {
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
