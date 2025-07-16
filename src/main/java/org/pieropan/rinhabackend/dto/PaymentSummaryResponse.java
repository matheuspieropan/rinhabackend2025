package org.pieropan.rinhabackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PaymentSummaryResponse(@JsonProperty("default") PaymentProcessor defaultValue,
                                     PaymentProcessor fallback) {
}