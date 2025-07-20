package org.pieropan.rinhaspring.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.Instant;

public class PagamentoProcessorRequest {

    private String correlationId;
    private BigDecimal amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC")
    private Instant requestedAt;
    @JsonIgnore
    private String json;
    @JsonIgnore
    private boolean isDefault;

    public PagamentoProcessorRequest(String correlationId, BigDecimal amount, Instant requestedAt) {
        this.correlationId = correlationId;
        this.amount = amount;
        this.requestedAt = requestedAt;
        this.isDefault = false;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }
}