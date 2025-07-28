package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class PagamentoComRedisService {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public PagamentoComRedisService(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<Boolean> salvarPagamento(PagamentoProcessorCompleto pagamentoProcessorCompleto) {
        Instant createdAt = pagamentoProcessorCompleto.pagamentoProcessorRequest().requestedAt();
        long timestamp = createdAt.toEpochMilli();
        String json = pagamentoProcessorCompleto.pagamentoEmJson();

        return redisTemplate.opsForZSet()
                .add("payments:default", json, timestamp)
                .doOnError(error -> {
                    System.err.println("Erro ao salvar no Redis: " + error.getMessage());
                });
    }
}