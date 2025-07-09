package com.pieropan.app.dto;

import jakarta.json.bind.annotation.JsonbProperty;

public record PaymentSummaryResponse(@JsonbProperty("default") PaymentProcessor defaultValue,
                                     PaymentProcessor fallback) {
}