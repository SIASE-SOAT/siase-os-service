package br.com.fiap.siase.infrastructure.adapters.input.rest;

import br.com.fiap.siase.application.usecase.AprovarOrcamentoUseCase;
import br.com.fiap.siase.application.usecase.CancelarOsUseCase;
import br.com.fiap.siase.application.usecase.ConsultarOsUseCase;
import br.com.fiap.siase.application.usecase.CriarOsUseCase;
import br.com.fiap.siase.infrastructure.adapters.input.rest.dto.CriarOsRequest;
import br.com.fiap.siase.infrastructure.adapters.input.rest.dto.OsResponse;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.model.EventoOs;
import br.com.fiap.siase.domain.model.OrdemDeServico;
import br.com.fiap.siase.infrastructure.adapters.input.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ordens")
@Tag(name = "Ordens de Serviço")
public class OsController {

  private final CriarOsUseCase criar;
  private final AprovarOrcamentoUseCase aprovarOrcamento;
  private final CancelarOsUseCase cancelar;
  private final ConsultarOsUseCase consultar;

  public OsController(CriarOsUseCase criar, AprovarOrcamentoUseCase aprovarOrcamento,
                      CancelarOsUseCase cancelar, ConsultarOsUseCase consultar) {
    this.criar = criar;
    this.aprovarOrcamento = aprovarOrcamento;
    this.cancelar = cancelar;
    this.consultar = consultar;
  }

  @PostMapping
  @Operation(summary = "Criar OS (inicia o Saga: publica OS_CRIADA)")
  public ResponseEntity<OsResponse> criar(@Valid @RequestBody CriarOsRequest request) {
    OrdemDeServico os = criar.executar(request.clienteId(), request.veiculo(),
            request.descricaoProblema());
    return ResponseEntity.status(HttpStatus.CREATED).body(OsResponse.de(os));
  }

  @GetMapping
  public List<OsResponse> listar() {
    return consultar.listar().stream().map(OsResponse::de).toList();
  }

  @GetMapping("/{id}")
  public OsResponse porId(@PathVariable UUID id) {
    return OsResponse.de(consultar.porId(id));
  }

  @GetMapping("/cliente/{clienteId}")
  public List<OsResponse> porCliente(@PathVariable UUID clienteId) {
    return consultar.porCliente(clienteId).stream().map(OsResponse::de).toList();
  }

  @GetMapping("/{id}/historico")
  @Operation(summary = "Histórico de eventos da OS (DynamoDB)")
  public List<EventoOs> historico(@PathVariable UUID id) {
    return consultar.historico(id);
  }

  // --- endpoints públicos do cliente (por número da OS) ---

  @GetMapping("/acompanhar/{numero}")
  @Operation(summary = "Acompanhamento público da OS pelo número")
  public OsResponse acompanhar(@PathVariable String numero) {
    return OsResponse.de(consultar.porNumero(numero));
  }

  @PatchMapping("/acompanhar/{numero}/aprovar-orcamento")
  @Operation(summary = "Cliente aprova orçamento → publica ORCAMENTO_APROVADO")
  public OsResponse aprovar(@PathVariable String numero) {
    return OsResponse.de(aprovarOrcamento.aprovar(numero));
  }

  @PatchMapping("/acompanhar/{numero}/recusar-orcamento")
  @Operation(summary = "Cliente recusa orçamento → compensação no billing")
  public OsResponse recusar(@PathVariable String numero) {
    return OsResponse.de(aprovarOrcamento.recusar(numero));
  }

  @PatchMapping("/{id}/cancelar")
  @Operation(summary = "Cancela a OS → publica OS_CANCELADA (compensação do Saga)")
  public OsResponse cancelar(@PathVariable UUID id) {
    return OsResponse.de(cancelar.executar(id));
  }
}
