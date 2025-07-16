package org.pieropan.rinhabackend.dto;

import java.math.BigDecimal;

public record PaymentProcessor(int totalRequests, BigDecimal totalAmount) {
}