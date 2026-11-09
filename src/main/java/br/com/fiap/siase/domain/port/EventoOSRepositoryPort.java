package br.com.fiap.siase.domain.port;

import br.com.fiap.siase.domain.model.EventoOS;
import java.util.List;

public interface EventoOSRepositoryPort {
    void salvar(EventoOS evento);
    List<EventoOS> findByOsId(String osId);
}
