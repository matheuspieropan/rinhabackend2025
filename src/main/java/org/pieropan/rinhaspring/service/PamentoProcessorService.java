package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PamentoProcessorService {

    private final PagamentoComRedisService pagamentoComRedisService;

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    public static Queue<PagamentoProcessorCompleto> pagamentosPendentes = new ConcurrentLinkedQueue<>();

    public PamentoProcessorService(PagamentoComRedisService pagamentoComRedisService,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault) {
        this.pagamentoComRedisService = pagamentoComRedisService;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
    }

    public Mono<Void> adicionaNaFila(PagamentoProcessorCompleto completo) {
        boolean adicionado = pagamentosPendentes.offer(completo);
        return adicionado ? Mono.empty() : Mono.error(new IllegalStateException());
    }

    public String convertObjetoParaJson(PagamentoProcessorRequest request) {
        return """
                {
                  "correlationId": "%s",
                  "amount": %s,
                  "requestedAt": "%s"
                }
                """.formatted(escape(request.correlationId()), request.amount().toPlainString(), request.requestedAt().toString()).replace("\n", "").replace("  ", "");
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    public Mono<Void> pagar(PagamentoProcessorCompleto completo) {
        return Mono.fromCallable(() -> enviarRequisicao(completo.pagamentoEmJson()))
                .flatMap(sucesso -> {
                    if (sucesso) {
                        return pagamentoComRedisService.salvarPagamento(completo)
                                .then();
                    } else {
                        pagamentosPendentes.offer(completo);
                        return Mono.empty();
                    }
                })
                .onErrorResume(e -> {
                    pagamentosPendentes.offer(completo);
                    return Mono.empty();
                });
    }

    public boolean enviarRequisicao(String pagamento) throws IOException, InterruptedException {
        return pagamentoProcessorDefault.processaPagamento(pagamento);
    }
}