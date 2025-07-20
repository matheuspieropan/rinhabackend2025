package org.pieropan.rinhaspring.service;

import jakarta.annotation.PostConstruct;
import org.pieropan.rinhaspring.client.PaymentProcessorClient;
import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;

@Service
public class PamentoProcessorService {

    private final LinkedBlockingQueue<PagamentoProcessorRequest> pagamentosPendentes = new LinkedBlockingQueue<>(13_1000);

    private final PagamentoRepository pagamentoRepository;

    private final PaymentProcessorClient paymentProcessorClient;

    @Value("${rinha.worker.pool-size}")
    private int workerPoolSize;

    public PamentoProcessorService(PagamentoRepository pagamentoRepository, PaymentProcessorClient paymentProcessorClient) {

        this.pagamentoRepository = pagamentoRepository;
        this.paymentProcessorClient = paymentProcessorClient;
    }

    @PostConstruct
    public void start() {
        for (int i = 0; i < workerPoolSize; i++) {
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
        boolean sucesso = paymentProcessorClient.sendPayment(pagamentoRequest);
        if (sucesso) {
            salvarDocument(pagamentoRequest, pagamentoRequest.isDefault());
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

    public void salvarDocument(PagamentoProcessorRequest pagamentoProcessorRequest, boolean isDefault) {
        PagamentoDocument doc = new PagamentoDocument();

        doc.setCorrelationId(pagamentoProcessorRequest.getCorrelationId());
        doc.setAmount(pagamentoProcessorRequest.getAmount());
        doc.setPaymentProcessorDefault(isDefault);
        doc.setCreatedAt(pagamentoProcessorRequest.getRequestedAt());

        pagamentoRepository.save(doc);
    }
}