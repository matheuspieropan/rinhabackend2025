package org.pieropan.rinhaspring.job;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PagamentoHelthCheckJob {

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    public static boolean procesadorDisponivel = true;

    private final RedisTemplate<String, String> redisTemplate;

    public PagamentoHelthCheckJob(@Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                  RedisTemplate<String, String> redisTemplate) {
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 15)
    public void checaProcessadorDefault() {
        String json = obtemRegistradoJaSalvo();
        if (json == null) return;

        try {
            boolean processadorDefaultDisponivel = pagamentoProcessorDefault.checaPagamentoRepetido(json);
            if (processadorDefaultDisponivel) {
                procesadorDisponivel = true;
                return;
            }
        } catch (Exception ignored) {
        }
        procesadorDisponivel = false;
    }

    private String obtemRegistradoJaSalvo() {
        String redisKey = "payments:default";
        Set<String> result = redisTemplate.opsForZSet().range(redisKey, 0, 0);
        if (result == null || result.isEmpty()) {
            return null;
        }
        return result.iterator().next();
    }
}