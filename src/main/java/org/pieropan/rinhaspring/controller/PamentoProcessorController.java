package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.config.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.model.PagamentoRequest;
import org.pieropan.rinhaspring.repository.PagamentoRepository;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    public PamentoProcessorController(PagamentoRepository pagamentoRepository,
                                      @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault,
                                      @Qualifier("pagamentoProcessorFallbackClient") PagamentoProcessorManualClient pagamentoProcessorFallback) {
        this.pamentoProcessorService = new PamentoProcessorService(pagamentoRepository, pagamentoProcessorDefault, pagamentoProcessorFallback);
    }

    @PostMapping
    public void pagar(@RequestBody PagamentoRequest pagamentoRequest) {
        PagamentoProcessorRequest pagamentoProcessorRequest = new PagamentoProcessorRequest(
                pagamentoRequest.correlationId(), pagamentoRequest.amount(), Instant.now().truncatedTo(ChronoUnit.SECONDS));

        String pagamentoEmJson = pamentoProcessorService.convertObjetoParaJson(pagamentoProcessorRequest);
        PagamentoProcessorCompleto pagamentoProcessorCompleto = new PagamentoProcessorCompleto(pagamentoEmJson, pagamentoProcessorRequest);

        pamentoProcessorService.adicionaNaFila(pagamentoProcessorCompleto);
    }
}