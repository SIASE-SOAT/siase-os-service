package br.com.fiap.siase.domain.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EventoOS {
    private String id;
    private String osId;
    private String osNumero;
    private String statusAnterior;
    private String statusNovo;
    private String origem;
    private String descricao;
    private LocalDateTime ocorridoEm;

    public static EventoOS criar(OrdemDeServico os, String statusAnterior, String origem, String descricao) {
        return EventoOS.builder()
                .id(UUID.randomUUID().toString())
                .osId(os.getId() != null ? os.getId().toString() : "")
                .osNumero(os.getNumero())
                .statusAnterior(statusAnterior)
                .statusNovo(os.getStatus().name())
                .origem(origem)
                .descricao(descricao)
                .ocorridoEm(LocalDateTime.now())
                .build();
    }
}
