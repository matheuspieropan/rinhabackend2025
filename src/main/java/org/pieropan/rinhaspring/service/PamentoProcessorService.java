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

    public static Queue<PagamentoProcessorCompleto> pagamentosPendentes = new ConcurrentLinkedQueue<>();

    public PamentoProcessorService(PagamentoComRedisService pagamentoComRedisService,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault) {
        this.pagamentoComRedisService = pagamentoComRedisService;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
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
            boolean sucesso = enviarRequisicao(completo.pagamentoEmJson());
            if (sucesso) {
                pagamentoComRedisService.salvarPagamento(completo);
                return;
            }
        } catch (Exception ignored) {
        }
        pagamentosPendentes.offer(completo);
    }

    public boolean enviarRequisicao(String pagamento) throws IOException, InterruptedException {
        return pagamentoProcessorDefault.processaPagamento(pagamento);
    }
}