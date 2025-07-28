package org.pieropan.rinhaspring.job;

import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.pieropan.rinhaspring.service.PamentoProcessorService.pagamentosPendentes;

@Configuration
public class ProcessaPendenteJob {

    private final PamentoProcessorService pamentoProcessorService;

    public ProcessaPendenteJob(PamentoProcessorService pamentoProcessorService) {
        this.pamentoProcessorService = pamentoProcessorService;
    }

    private final AtomicBoolean rodando = new AtomicBoolean(false);

    @Scheduled(fixedDelay = 15)
    public void processa() {
        if (!rodando.compareAndSet(false, true)) return;

        int size = Math.min(pagamentosPendentes.size(), 20);

        Flux.range(0, size)
                .map(i -> pagamentosPendentes.poll())
                .filter(Objects::nonNull)
                .flatMap(pagamento -> pamentoProcessorService.pagar(pagamento)
                                .timeout(Duration.ofMillis(800))
                                .retry(1)
                                .onErrorResume(e -> {
                                    pagamentosPendentes.offer(pagamento);
                                    return Mono.empty();
                                })
                        , 20)
                .doFinally(signal -> rodando.set(false))
                .subscribe();
    }
}