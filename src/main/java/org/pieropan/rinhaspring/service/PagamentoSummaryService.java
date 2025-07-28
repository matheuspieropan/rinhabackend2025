package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.model.PagamentoProcessor;
import org.pieropan.rinhaspring.model.PagamentoSummaryResponse;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class PagamentoSummaryService {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private static final BigDecimal VALOR_UNITARIO = BigDecimal.valueOf(19.9);

    public PagamentoSummaryService(ReactiveRedisTemplate<String, String> reactiveRedisTemplate) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
    }

    public Mono<PagamentoSummaryResponse> summary(Instant from, Instant to) {
        return processarResumo("payments:default", from, to)
                .zipWith(processarResumo("payments:fallback", from, to))
                .map(tuple -> new PagamentoSummaryResponse(tuple.getT1(), tuple.getT2()));
    }

    private Mono<PagamentoProcessor> processarResumo(String key, Instant from, Instant to) {
        double minScore = (from != null) ? from.toEpochMilli() : Double.NEGATIVE_INFINITY;
        double maxScore = (to != null) ? to.toEpochMilli() : Double.POSITIVE_INFINITY;
        Range<Double> range = Range.open(minScore, maxScore);

        Flux<String> registros = reactiveRedisTemplate.opsForZSet()
                .rangeByScore(key, range);

        return registros.count()
                .map(totalRequests -> {
                    BigDecimal totalAmount = VALOR_UNITARIO.multiply(BigDecimal.valueOf(totalRequests));
                    return new PagamentoProcessor(totalRequests.intValue(), totalAmount);
                })
                .defaultIfEmpty(new PagamentoProcessor(0, BigDecimal.ZERO));
    }
}