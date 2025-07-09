package org.pieropan.rinhaspring.model;

public record HealthResponse(boolean failing, int minResponseTime) {
}