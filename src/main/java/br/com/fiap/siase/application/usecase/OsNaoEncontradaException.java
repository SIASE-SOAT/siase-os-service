package br.com.fiap.siase.application.usecase;

public class OsNaoEncontradaException extends RuntimeException {
  public OsNaoEncontradaException(String referencia) {
    super("Ordem de serviço não encontrada: " + referencia);
  }
}
