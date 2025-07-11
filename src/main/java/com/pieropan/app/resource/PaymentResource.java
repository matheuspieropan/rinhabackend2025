package com.pieropan.app.resource;


import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.rabbit.RabbitProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Path("/payments")
public class PaymentResource {

    @Inject
    RabbitProvider rabbitProvider;

    private final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @POST
    public void save(PaymentRequest paymentRequest) {
        virtualExecutor.submit(() -> rabbitProvider.send(paymentRequest));
    }
}