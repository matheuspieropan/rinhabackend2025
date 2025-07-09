package org.pieropan.rinhaspring.service;

import org.pieropan.rinhaspring.config.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.document.PagamentoDocument;
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

    private final LinkedBlockingQueue<PagamentoProcessorRequest> pagamentosPendentes = new LinkedBlockingQueue<>();

    public PamentoProcessorService(PagamentoRepository pagamentoRepository,
                                   @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                   @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {

        this.pagamentoRepository = pagamentoRepository;
        this.pagamentoProcessorDefault = pagamentoProcessorDefault;
        this.pagamentoProcessorFallback = pagamentoProcessorFallback;

        for (int i = 0; i < 15; i++) {
            Thread.startVirtualThread(this::runWorker);
        }
    }

    private void runWorker() {
        while (true) {
            var request = buscarPagamento();
            processPayment(request);
        }
    }

    private void processPayment(PagamentoProcessorRequest pagamentoRequest) {
        boolean sucesso = pagar(pagamentoRequest);
        if (sucesso) {
            return;
        }
        adicionaNaFila(pagamentoRequest);
    }

    public PagamentoProcessorRequest buscarPagamento() {
        try {
            return pagamentosPendentes.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void adicionaNaFila(PagamentoProcessorRequest pagamentoRequest) {
        pagamentosPendentes.offer(pagamentoRequest);
    }

    public boolean pagar(PagamentoProcessorRequest pagamentoProcessorRequest) {
        try {
            for (int tentativa = 0; tentativa < 3; tentativa++) {

                boolean sucesso = enviarRequisicao(pagamentoProcessorRequest, true);
                if (sucesso) {
                    salvarDocument(pagamentoProcessorRequest, true);
                    return true;
                }

                if (enviarRequisicao(pagamentoProcessorRequest, false)) {
                    salvarDocument(pagamentoProcessorRequest, false);
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean enviarRequisicao(PagamentoProcessorRequest pagamentoProcessorRequest, boolean processadorDefault) {

        try {
            if (processadorDefault) {
                return pagamentoProcessorDefault.processaPagamento(pagamentoProcessorRequest);
            } else {
                return pagamentoProcessorFallback.processaPagamento(pagamentoProcessorRequest);
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