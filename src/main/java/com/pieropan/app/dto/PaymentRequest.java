package com.pieropan.app.dto;

import java.math.BigDecimal;

public record PaymentRequest(String correlationId, BigDecimal amount) {
}