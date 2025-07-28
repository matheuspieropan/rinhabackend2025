package org.pieropan.rinhaspring.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PagamentoComRedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public PagamentoComRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void salvarPagamento(String json) {
        Instant createdAt = extrairCreatedAt(json);
        long timestamp = createdAt.toEpochMilli();

        redisTemplate.opsForZSet().add("payments:default", json, timestamp);
    }

    private Instant extrairCreatedAt(String json) {
        String chave = "\"requestedAt\":\"";
        int inicio = json.indexOf(chave);
        if (inicio == -1) {
            throw new IllegalArgumentException("Campo createdAt não encontrado no JSON: " + json);
        }

        inicio += chave.length();
        int fim = json.indexOf("\"", inicio);
        if (fim == -1) {
            throw new IllegalArgumentException("Formato inválido de createdAt no JSON: " + json);
        }

        String valor = json.substring(inicio, fim);
        return Instant.parse(valor);
    }
}