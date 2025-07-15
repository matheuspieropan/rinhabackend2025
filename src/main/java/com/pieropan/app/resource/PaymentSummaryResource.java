package com.pieropan.app.resource;

import com.pieropan.app.dto.PaymentSummaryResponse;
import com.pieropan.app.service.PaymentSummaryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import java.time.Instant;
import java.time.OffsetDateTime;

@Path("/payments-summary")
public class PaymentSummaryResource {

    @Inject
    PaymentSummaryService paymentSummaryService;

    @GET
    @Produces("application/json")
    public PaymentSummaryResponse paymentSummary(@QueryParam("from") String from,
                                                 @QueryParam("to") String to) {

        return paymentSummaryService.paymentSummary(from, to);
    }

    public Instant parseFlexibleDate(String dateStr) {
        return OffsetDateTime.parse(dateStr).toInstant();
    }
}