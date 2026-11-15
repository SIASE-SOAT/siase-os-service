package br.com.fiap.siase.domain.model;

public class TransicaoStatusInvalidaException extends RuntimeException {
  public TransicaoStatusInvalidaException(String message) {
    super(message);
  }
}
