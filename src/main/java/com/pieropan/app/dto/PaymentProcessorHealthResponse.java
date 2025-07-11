package com.pieropan.app.dto;

public record PaymentProcessorHealthResponse(boolean failing, int minResponseTime) {
}