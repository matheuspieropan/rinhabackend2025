package org.pieropan.rinhaspring.model;

import java.math.BigDecimal;
import java.time.Instant;

public class PagamentoProcessorRequest {

    private String correlationId;
    private BigDecimal amount;
    private Instant requestedAt;

    private String json;
    private boolean isDefault;

    public PagamentoProcessorRequest(String correlationId) {
        this.correlationId = correlationId;
        this.amount = new BigDecimal("19.90");
        this.requestedAt = Instant.now();
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