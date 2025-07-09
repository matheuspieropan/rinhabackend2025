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
        PagamentoProcessor defaultProcessor = processarResumo("payments:default", from, to);
        PagamentoProcessor fallbackProcessor = processarResumo("payments:fallback", from, to);

        return new PagamentoSummaryResponse(defaultProcessor, fallbackProcessor);
    }

    private PagamentoProcessor processarResumo(String key, Instant from, Instant to) {
        Set<String> registros = redisTemplate.opsForZSet()
                .rangeByScore(key, from.toEpochMilli(), to.toEpochMilli());

        if (registros == null || registros.isEmpty()) {
            return new PagamentoProcessor(0, BigDecimal.ZERO);
        }

        int totalRequests = registros.size();
        BigDecimal valorUnitario = BigDecimal.valueOf(19.9);
        BigDecimal totalAmount = valorUnitario.multiply(BigDecimal.valueOf(totalRequests));

        return new PagamentoProcessor(totalRequests, totalAmount);
    }
}