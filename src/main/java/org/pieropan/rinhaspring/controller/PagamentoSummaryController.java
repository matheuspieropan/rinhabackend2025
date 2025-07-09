package org.pieropan.rinhaspring.controller;

import org.pieropan.rinhaspring.model.PagamentoSummaryResponse;
import org.pieropan.rinhaspring.service.PagamentoSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/payments-summary")
public class PagamentoSummaryController {

    private final PagamentoSummaryService pagamentoSummaryService;

    public PagamentoSummaryController(PagamentoSummaryService pagamentoSummaryService) {
        this.pagamentoSummaryService = pagamentoSummaryService;
    }

    @GetMapping
    public PagamentoSummaryResponse summary(@RequestParam(value = "from", required = false) Instant from,
                                            @RequestParam(value = "to", required = false) Instant to) {
        return pagamentoSummaryService.summary(from, to);
    }
}