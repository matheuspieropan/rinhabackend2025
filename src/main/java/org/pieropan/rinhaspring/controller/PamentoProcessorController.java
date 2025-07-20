package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.pieropan.rinhaspring.utils.JsonUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    private final JsonUtils jsonUtils;

    public PamentoProcessorController(PamentoProcessorService pamentoProcessorService, JsonUtils jsonUtils) {
        this.pamentoProcessorService = pamentoProcessorService;
        this.jsonUtils = jsonUtils;
    }

    @PostMapping
    public void pagar(@RequestBody String pagamentoRequest) {

        PagamentoProcessorRequest pagamentoProcessorRequest = new PagamentoProcessorRequest(
                jsonUtils.extractUUIDFromRequest(pagamentoRequest));

        pagamentoProcessorRequest.setJson(jsonUtils.buildPaymentDTO(pagamentoProcessorRequest));

        pamentoProcessorService.adicionaNaFila(pagamentoProcessorRequest);
    }
}