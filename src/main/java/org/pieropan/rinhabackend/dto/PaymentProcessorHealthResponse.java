package org.pieropan.rinhabackend.dto;

public record PaymentProcessorHealthResponse(boolean failing, int minResponseTime) {
}