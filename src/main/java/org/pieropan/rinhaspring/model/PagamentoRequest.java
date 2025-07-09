package org.pieropan.rinhaspring.model;

import java.math.BigDecimal;

public record PagamentoRequest(String correlationId, BigDecimal amount) {
}