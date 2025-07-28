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

    @GetMapping(produces = "application/json")
    public String summary(@RequestParam(value = "from", required = false) Instant from,
                          @RequestParam(value = "to", required = false) Instant to) {
        PagamentoSummaryResponse summary = pagamentoSummaryService.summary(from, to);
        return """
                {
                    "default": {
                        "totalRequests": %d,
                        "totalAmount": %s
                    },
                    "fallback": {
                        "totalRequests": %d,
                        "totalAmount": %s
                    }
                }
                """.formatted(
                summary.defaultValue().totalRequests(),
                summary.defaultValue().totalAmount(),
                summary.fallback().totalRequests(),
                summary.fallback().totalAmount()
        );
    }
}