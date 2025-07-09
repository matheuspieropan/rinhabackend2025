package org.pieropan.rinhaspring.model;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.Instant;

public record PagamentoProcessorRequest(String correlationId, BigDecimal amount,
                                        @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
                                        Instant requestedAt) {
}