package com.pieropan.app.dto;

public record PaymentRequest(String correlationId, Double amount) {
}