package org.pieropan.rinhabackend.dto;

import java.math.BigDecimal;

public record PaymentRequest(String correlationId, BigDecimal amount) {
}