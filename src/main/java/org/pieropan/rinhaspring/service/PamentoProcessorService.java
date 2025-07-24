package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.MelhorOpcao;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.pieropan.rinhaspring.job.PagamentoHelthCheckJob.melhorOpcao;

@Service
public class PamentoProcessorService {

    private final PagamentoComRedisService pagamentoComRedisService;

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    private final PagamentoProcessorManualClient pagamentoProcessorFallback;

    public static BlockingQueue<PagamentoProcessorCompleto> pagamentosPendentes = new LinkedBlockingQueue<>();

    public PamentoProcessorService(PagamentoComRedisService pagamentoComRedisService,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                   @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {
        this.pagamentoComRedisService = pagamentoComRedisService;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.pagamentoProcessorFallback = pagamentoProcessorFallback;
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
        MelhorOpcao melhorOpcaoAtual = melhorOpcao;

        if (melhorOpcaoAtual == null) {
            pagamentosPendentes.offer(completo);
            return;
        }

        try {
            boolean sucesso = enviarRequisicao(completo.pagamentoEmJson(), melhorOpcaoAtual.processadorDefault());
            if (sucesso) {
                pagamentoComRedisService.salvarPagamento(completo, melhorOpcaoAtual.processadorDefault());
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