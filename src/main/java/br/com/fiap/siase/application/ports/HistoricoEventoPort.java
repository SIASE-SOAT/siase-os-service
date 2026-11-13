package br.com.fiap.siase.application.ports;

import br.com.fiap.siase.domain.model.EventoOs;

import java.util.List;
import java.util.UUID;

/**
 * Histórico de eventos da OS (DynamoDB — NoSQL do serviço).
 */
public interface HistoricoEventoPort {

  void registrar(EventoOs evento);

  List<EventoOs> historicoDaOs(UUID osId);
}
