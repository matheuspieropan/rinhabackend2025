package com.pieropan.app.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentProcessorRequest(String correlationId, BigDecimal amount, Instant requestedAt) {
}