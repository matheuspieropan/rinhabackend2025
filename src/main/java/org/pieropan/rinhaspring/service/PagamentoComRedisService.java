package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PagamentoComRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public PagamentoComRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void salvarPagamento(PagamentoProcessorCompleto pagamentoProcessorCompleto) {
        Instant createdAt = pagamentoProcessorCompleto.pagamentoProcessorRequest().requestedAt();

        long timestamp = createdAt.toEpochMilli();
        String json = pagamentoProcessorCompleto.pagamentoEmJson();

        redisTemplate.opsForZSet().add("payments:default", json, timestamp);
    }
}