package br.com.fiap.siase.infrastructure.web;

import br.com.fiap.siase.application.dto.input.CriarOsRequest;
import br.com.fiap.siase.application.dto.output.OsResponse;
import br.com.fiap.siase.application.usecase.*;
import br.com.fiap.siase.domain.enums.StatusOS;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ordens")
@Tag(name = "Ordens de Serviço")
@RequiredArgsConstructor
public class OrdemServicoController {

    private final CriarOsUC criarOsUC;
    private final ConsultarOsUC consultarOsUC;
    private final AvancarStatusOsUC avancarStatusUC;
    private final CancelarOsUC cancelarOsUC;
    private final AprovarOrcamentoUC aprovarOrcamentoUC;

    @PostMapping
    @Operation(summary = "Criar ordem de serviço")
    public ResponseEntity<OsResponse> criar(@Valid @RequestBody CriarOsRequest request) {
        var response = criarOsUC.executar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar ordens de serviço")
    public ResponseEntity<List<OsResponse>> listar(@RequestParam(required = false) StatusOS status) {
        return ResponseEntity.ok(consultarOsUC.listar(status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar OS por ID")
    public ResponseEntity<OsResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(consultarOsUC.porId(id));
    }

    @GetMapping("/acompanhar/{numero}")
    @Operation(summary = "Acompanhar OS por número (público)")
    public ResponseEntity<OsResponse> acompanhar(@PathVariable String numero) {
        return ResponseEntity.ok(consultarOsUC.porNumero(numero));
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar OS por cliente")
    public ResponseEntity<List<OsResponse>> porCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(consultarOsUC.porCliente(clienteId));
    }

    @PatchMapping("/{id}/avancar")
    @Operation(summary = "Avançar status da OS")
    public ResponseEntity<OsResponse> avancar(@PathVariable UUID id) {
        return ResponseEntity.ok(avancarStatusUC.executar(id));
    }

    @PatchMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar OS")
    public ResponseEntity<OsResponse> cancelar(@PathVariable UUID id,
                                                @RequestParam(required = false) String motivo) {
        return ResponseEntity.ok(cancelarOsUC.executar(id, motivo));
    }

    @PatchMapping("/acompanhar/{numero}/aprovar-orcamento")
    @Operation(summary = "Aprovar orçamento (público)")
    public ResponseEntity<OsResponse> aprovar(@PathVariable String numero) {
        return ResponseEntity.ok(aprovarOrcamentoUC.aprovar(numero));
    }

    @PatchMapping("/acompanhar/{numero}/recusar-orcamento")
    @Operation(summary = "Recusar orçamento (público)")
    public ResponseEntity<OsResponse> recusar(@PathVariable String numero) {
        return ResponseEntity.ok(aprovarOrcamentoUC.recusar(numero));
    }
}
