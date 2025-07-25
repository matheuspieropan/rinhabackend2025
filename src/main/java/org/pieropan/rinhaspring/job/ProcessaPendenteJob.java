package org.pieropan.rinhaspring.job;

import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

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

    @Scheduled(initialDelay = 100, fixedDelay = 15)
    public void processa() {
        if (pagamentosPendentes.isEmpty() || melhorOpcao == null) {
            return;
        }

        int size = Math.min(pagamentosPendentes.size(), 70);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            futures.add(pagarAsync());
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Void> pagarAsync() {
        return CompletableFuture.runAsync(() -> {
            var pagamento = pagamentosPendentes.poll();
            if (pagamento == null) return;
            pamentoProcessorService.pagar(pagamento);
        }, executorService);
    }
}