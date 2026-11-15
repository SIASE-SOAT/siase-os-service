package br.com.fiap.siase.infrastructure.adapters.input.rest;

import br.com.fiap.siase.application.usecase.OsNaoEncontradaException;
import br.com.fiap.siase.domain.model.TransicaoStatusInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(OsNaoEncontradaException.class)
  public ProblemDetail notFound(OsNaoEncontradaException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(TransicaoStatusInvalidaException.class)
  public ProblemDetail conflict(TransicaoStatusInvalidaException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
  }
}
