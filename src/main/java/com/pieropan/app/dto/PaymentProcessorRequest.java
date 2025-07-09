package com.pieropan.app.dto;

import java.time.Instant;

public record PaymentProcessorRequest(String correlationId, Double amount, Instant requestedAt) {
}