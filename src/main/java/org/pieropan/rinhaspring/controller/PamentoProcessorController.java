package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.http.PagamentoProcessorManualClient;
import org.pieropan.rinhaspring.model.PagamentoProcessorCompleto;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.pieropan.rinhaspring.model.PagamentoRequest;
import org.pieropan.rinhaspring.service.PagamentoComRedisService;
import org.pieropan.rinhaspring.service.PamentoProcessorService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/payments")
public class PamentoProcessorController {

    private final PamentoProcessorService pamentoProcessorService;

    public PamentoProcessorController(PagamentoComRedisService pagamentoComRedisService,
                                      @Qualifier("pagamentoProcessorDefaultClient") PagamentoProcessorManualClient pagamentoProcessorDefault) {
        this.pamentoProcessorService = new PamentoProcessorService(pagamentoComRedisService, pagamentoProcessorDefault);
    }

    @PostMapping
    public Mono<Void> pagar(@RequestBody Mono<PagamentoRequest> pagamentoRequestMono) {
        return pagamentoRequestMono
                .map(request -> {
                    var pagamentoProcessorRequest = new PagamentoProcessorRequest(
                            request.correlationId(),
                            request.amount(),
                            Instant.now().truncatedTo(ChronoUnit.SECONDS)
                    );

                    return new PagamentoProcessorCompleto(pamentoProcessorService.convertObjetoParaJson(pagamentoProcessorRequest), pagamentoProcessorRequest
                    );
                })
                .flatMap(pamentoProcessorService::adicionaNaFila);
    }
}