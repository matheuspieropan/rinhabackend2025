package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class PamentoProcessorService {

    private final PagamentoComRedisService pagamentoComRedisService;

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    public static Queue<String> pagamentosPendentes = new ConcurrentLinkedQueue<>();

    public PamentoProcessorService(PagamentoComRedisService pagamentoComRedisService,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault) {
        this.pagamentoComRedisService = pagamentoComRedisService;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
    }

    public void adicionaNaFila(String completo) {
        pagamentosPendentes.offer(completo);
    }

    public String adicionaTimestamp(String jsonOriginal) {
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        if (jsonOriginal.endsWith("}")) {
            return jsonOriginal.substring(0, jsonOriginal.length() - 1)
                    + ",\"requestedAt\":\"" + agora.toString() + "\"}";
        } else {
            throw new IllegalArgumentException("JSON inválido: " + jsonOriginal);
        }
    }

    public void pagar(String json) {
        try {
            boolean sucesso = enviarRequisicao(json);
            if (sucesso) {
                pagamentoComRedisService.salvarPagamento(json);
                return;
            }
        } catch (Exception ignored) {
        }
        pagamentosPendentes.offer(json);
    }

    public boolean enviarRequisicao(String pagamento) throws IOException, InterruptedException {
        return pagamentoProcessorDefault.processaPagamento(pagamento);
    }
}