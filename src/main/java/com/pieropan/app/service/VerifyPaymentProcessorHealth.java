package com.pieropan.app.service;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class VerifyPaymentProcessorHealth {

    @Inject
    private PaymentProcessorHealthService paymentProcessorHealthService;

    @Schedule(hour = "*", minute = "*", second = "*/5", persistent = false)
    public void executar() {
        paymentProcessorHealthService.processPayment();
    }
}