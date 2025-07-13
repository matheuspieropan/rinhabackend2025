package com.pieropan.app.dto;

import java.math.BigDecimal;

public record PaymentProcessor(int totalRequests, BigDecimal totalAmount) {
}