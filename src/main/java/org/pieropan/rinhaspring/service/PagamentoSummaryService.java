package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.model.PagamentoProcessor;
import org.pieropan.rinhaspring.model.PagamentoSummaryResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

@Service
public class PagamentoSummaryService {

    private final RedisTemplate<String, String> redisTemplate;

    public PagamentoSummaryService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public PagamentoSummaryResponse summary(Instant from, Instant to) {
        PagamentoProcessor defaultProcessor = processarResumo(from, to);
        PagamentoProcessor fallbackProcessor = new PagamentoProcessor(0, BigDecimal.ZERO);

        return new PagamentoSummaryResponse(defaultProcessor, fallbackProcessor);
    }

    private PagamentoProcessor processarResumo(Instant from, Instant to) {
        double minScore = (from != null) ? from.toEpochMilli() : Double.NEGATIVE_INFINITY;
        double maxScore = (to != null) ? to.toEpochMilli() : Double.POSITIVE_INFINITY;

        Set<String> registros = redisTemplate.opsForZSet()
                .rangeByScore("payments:default", minScore, maxScore);

        if (registros == null || registros.isEmpty()) {
            return new PagamentoProcessor(0, BigDecimal.ZERO);
        }

        int totalRequests = registros.size();
        BigDecimal valorUnitario = BigDecimal.valueOf(19.9);
        BigDecimal totalAmount = valorUnitario.multiply(BigDecimal.valueOf(totalRequests));

        return new PagamentoProcessor(totalRequests, totalAmount);
    }
}