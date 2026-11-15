package br.com.fiap.siase.application.ports;

import br.com.fiap.siase.domain.model.EventoOs;

/**
 * Publica eventos de domínio nas filas do Saga (destino depende do tipo).
 */
public interface EventoPublicadorPort {

  void publicarParaBilling(EventoOs evento);

  void publicarParaExecution(EventoOs evento);

  void publicarEventoDominio(EventoOs evento);
}
