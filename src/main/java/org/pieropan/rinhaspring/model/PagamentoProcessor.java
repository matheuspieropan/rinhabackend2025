package org.pieropan.rinhaspring.model;

import java.math.BigDecimal;

public record PagamentoProcessor(int totalRequests, BigDecimal totalAmount) {
}