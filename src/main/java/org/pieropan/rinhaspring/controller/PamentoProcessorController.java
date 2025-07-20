package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.model.PagamentoRequest;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.pieropan.rinhaspring.utils.JsonMapper;
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

    private final JsonMapper jsonMapper;

    public PamentoProcessorController(PamentoProcessorService pamentoProcessorService, JsonMapper jsonMapper) {
        this.pamentoProcessorService = pamentoProcessorService;
        this.jsonMapper = jsonMapper;
    }

    @PostMapping
    public void pagar(@RequestBody PagamentoRequest pagamentoRequest) {

        PagamentoProcessorRequest pagamentoProcessorRequest = new PagamentoProcessorRequest(
                pagamentoRequest.correlationId(), pagamentoRequest.amount(), Instant.now().truncatedTo(ChronoUnit.SECONDS));

        pagamentoProcessorRequest.setJson(jsonMapper.toJson(pagamentoProcessorRequest));

        pamentoProcessorService.adicionaNaFila(pagamentoProcessorRequest);
    }
}