package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PamentoProcessorService {

    private final PagamentoComRedisService pagamentoComRedisService;

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    private final PagamentoProcessorManualClient pagamentoProcessorFallback;

    public static Queue<PagamentoProcessorCompleto> pagamentosPendentes = new ConcurrentLinkedQueue<>();

    public PamentoProcessorService(PagamentoComRedisService pagamentoComRedisService,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                   @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {
        this.pagamentoComRedisService = pagamentoComRedisService;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.pagamentoProcessorFallback = pagamentoProcessorFallback;
    }

    public void adicionaNaFila(PagamentoProcessorCompleto completo) {
        pagamentosPendentes.offer(completo);
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

    public void pagar(PagamentoProcessorCompleto completo) {
        try {
            boolean sucesso = enviarRequisicao(completo.pagamentoEmJson(), true);
            if (sucesso) {
                pagamentoComRedisService.salvarPagamento(completo, true);
                return;
            }
        } catch (Exception ignored) {
        }
        pagamentosPendentes.offer(completo);
    }

    public boolean enviarRequisicao(String pagamento, boolean processadorDefault) throws IOException, InterruptedException {
        if (processadorDefault) {
            return pagamentoProcessorDefault.processaPagamento(pagamento);
        } else {
            return pagamentoProcessorFallback.processaPagamento(pagamento);
        }
    }
}