package org.pieropan.rinhaspring.utils;

import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.springframework.stereotype.Component;

@Component
public class JsonUtils {
    private final static String key = "\"correlationId\":";

    public String extractUUIDFromRequest(String json) {
        var idx = json.indexOf(key);
        if (idx == -1) throw new IllegalArgumentException("correlationId not found");

        var start = json.indexOf('"', idx + key.length()) + 1;
        var end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    public String buildPaymentDTO(PagamentoProcessorRequest payment) {
        return new StringBuilder("{")
                .append("\"correlationId\":\"").append(payment.getCorrelationId()).append("\",")
                .append("\"amount\":").append(payment.getAmount().toPlainString()).append(",")
                .append("\"requestedAt\":\"").append(payment.getRequestedAt().toString()).append("\"")
                .append("}")
                .toString();
    }
}
