package br.com.fiap.siase.domain.model;

import java.math.BigDecimal;

public record ItemPeca(String nome, int quantidade, BigDecimal precoUnitario) {
}
