package org.pieropan.rinhaspring.job;

import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static org.pieropan.rinhaspring.job.PagamentoHelthCheckJob.melhorOpcao;
import static org.pieropan.rinhaspring.service.PamentoProcessorService.pagamentosPendentes;

@Configuration
public class ProcessaPendenteJob {

    private final PamentoProcessorService pamentoProcessorService;
    private final ExecutorService executorService;

    public ProcessaPendenteJob(PamentoProcessorService pamentoProcessorService,
                               ExecutorService executorService) {
        this.pamentoProcessorService = pamentoProcessorService;
        this.executorService = executorService;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 100)
    public void processa() {
        if (pagamentosPendentes.isEmpty() || melhorOpcao == null) {
            return;
        }

        Instant inicioJob = Instant.now();
        int size = pagamentosPendentes.size();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            futures.add(pagarAsync());
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Duration duracaoJob = Duration.between(inicioJob, Instant.now());
        System.out.println("✅ Processados " + size + " pagamentos em " + duracaoJob.toMillis() + " ms");
    }

    private CompletableFuture<Void> pagarAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                var pagamento = pagamentosPendentes.take();
                pamentoProcessorService.pagar(pagamento);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, executorService);
    }
}