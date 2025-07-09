package org.pieropan.rinhaspring.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PagamentoSummaryResponse(@JsonProperty("default") PagamentoProcessor defaultValue,
                                       PagamentoProcessor fallback) {
}