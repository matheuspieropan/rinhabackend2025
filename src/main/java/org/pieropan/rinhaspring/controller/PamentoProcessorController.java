package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.service.PagamentoComRedisService;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    public PamentoProcessorController(PagamentoComRedisService pagamentoComRedisService,
                                      @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault) {
        this.pamentoProcessorService = new PamentoProcessorService(pagamentoComRedisService, pagamentoProcessorDefault);
    }

    @PostMapping(consumes = "application/json", produces = "text/plain")
    public void pagar(@RequestBody String pagamentoRequest) {
        String pagamentoEmJson = pamentoProcessorService.adicionaTimestamp(pagamentoRequest);
        pamentoProcessorService.adicionaNaFila(pagamentoEmJson);
    }
}