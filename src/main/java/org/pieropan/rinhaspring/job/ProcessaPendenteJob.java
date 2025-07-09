//package org.pieropan.rinhaspring.job;
//
//import org.pieropan.rinhaspring.service.PamentoProcessorService;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//import static org.pieropan.rinhaspring.job.PagamentoHelthCheckJob.melhorOpcao;
//import static org.pieropan.rinhaspring.service.PamentoProcessorService.pagamentosPendentes;
//
//@Configuration
//public class ProcessaPendenteJob {
//
//    private final PamentoProcessorService pamentoProcessorService;
//
//    public ProcessaPendenteJob(PamentoProcessorService pamentoProcessorService) {
//        this.pamentoProcessorService = pamentoProcessorService;
//    }
//
//    @Scheduled(initialDelay = 5000, fixedDelay = 100)
//    public void processa() {
//        if (pagamentosPendentes.isEmpty() || melhorOpcao == null) {
//            return;
//        }
//
//        Instant inicio = Instant.now();
//
//        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
//        for (int i = 0; i < pagamentosPendentes.size(); i++) {
//            futures.add(pagar());
//        }
//
//        CompletableFuture
//                .allOf(futures.toArray(new CompletableFuture[0]))
//                .thenRun(() -> {
//                    long sucessoCount = futures.stream()
//                            .map(CompletableFuture::join)
//                            .filter(Boolean::booleanValue)
//                            .count();
//
//                    Duration duracao = Duration.between(inicio, Instant.now());
//                    System.out.println("✅ Processados " + sucessoCount + " pagamentos com sucesso em " + duracao.toMillis() + " ms");
//                });
//    }
//
//    @Async
//    public CompletableFuture<Boolean> pagar() {
//        try {
//            boolean sucesso = pamentoProcessorService.pagar(pagamentosPendentes.take());
//            return CompletableFuture.completedFuture(sucesso);
//        } catch (InterruptedException ignored) {
//            Thread.currentThread().interrupt();
//        }
//        return CompletableFuture.completedFuture(false);
//    }
//}