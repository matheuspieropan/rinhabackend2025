package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.config.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;

@Service
public class PamentoProcessorService {

    private final PagamentoRepository pagamentoRepository;

    private final PagamentoProcessorManualClient pagamentoProcessorDefault;

    private final PagamentoProcessorManualClient pagamentoProcessorFallback;

    private final LinkedBlockingQueue<PagamentoProcessorCompleto> pagamentosPendentes = new LinkedBlockingQueue<>();

    public PamentoProcessorService(PagamentoRepository pagamentoRepository,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                   @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {

        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.pagamentoProcessorFallback = pagamentoProcessorFallback;

        for (int i = 0; i < 20; i++) {
            Thread.startVirtualThread(this::runWorker);
        }
    }

    private void runWorker() {
        while (true) {
            var request = buscarPagamento();
            processaPagamento(request);
        }
    }

    private void processaPagamento(PagamentoProcessorCompleto pagamentoProcessorCompleto) {
        boolean sucesso = pagar(pagamentoProcessorCompleto);
        if (sucesso) {
            return;
        }
        adicionaNaFila(pagamentoProcessorCompleto);
    }

    public PagamentoProcessorCompleto buscarPagamento() {
        try {
            return pagamentosPendentes.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void adicionaNaFila(PagamentoProcessorCompleto pagamentoProcessorCompleto) {
        pagamentosPendentes.offer(pagamentoProcessorCompleto);
    }

    public String convertObjetoParaJson(PagamentoProcessorRequest request) {
        return """
                {
                  "correlationId": "%s",
                  "amount": %s,
                  "requestedAt": "%s"
                }
                """.formatted(
                escape(request.correlationId()),
                request.amount().toPlainString(),
                request.requestedAt().toString()
        ).replace("\n", "").replace("  ", "");
    }

    private String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    public boolean pagar(PagamentoProcessorCompleto pagamentoProcessorCompleto) {
        try {

            boolean sucesso;
            for (int tentativa = 0; tentativa < 15; tentativa++) {

                sucesso = enviarRequisicao(pagamentoProcessorCompleto.pagamentoEmJson(), true);
                if (sucesso) {
                    salvarDocument(pagamentoProcessorCompleto.pagamentoProcessorRequest(), true);
                    return true;
                }
            }

            sucesso = enviarRequisicao(pagamentoProcessorCompleto.pagamentoEmJson(), false);
            if (sucesso) {
                salvarDocument(pagamentoProcessorCompleto.pagamentoProcessorRequest(), false);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enviarRequisicao(String pagamento, boolean processadorDefault) {

        try {
            if (processadorDefault) {
                return pagamentoProcessorDefault.processaPagamento(pagamento);
            } else {
                return pagamentoProcessorFallback.processaPagamento(pagamento);
            }
        } catch (Exception ex) {
            return false;
        }
    }

    public void salvarDocument(PagamentoProcessorRequest pagamentoProcessorRequest, boolean isDefault) {
        PagamentoDocument doc = new PagamentoDocument();

        doc.setCorrelationId(pagamentoProcessorRequest.correlationId());
        doc.setAmount(pagamentoProcessorRequest.amount());
        doc.setPaymentProcessorDefault(isDefault);
        doc.setCreatedAt(pagamentoProcessorRequest.requestedAt());

        pagamentoRepository.save(doc);
    }
}