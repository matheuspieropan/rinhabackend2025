package com.pieropan.app.service;

import com.pieropan.app.dto.PaymentProcessorHealthResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@ApplicationScoped
public class PaymentProcessorHealthService {

    private final Jsonb jsonb = JsonbBuilder.create();

    public static boolean PAYMENT_PROCESSOR_DEFAULT_OK = true;

    public static int TIMEOUT = 5000;

    public void processPaymentDefault() {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {

            HttpRequest request = buildRequest("http://payment-processor-default:8080/payments/service-health");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PaymentProcessorHealthResponse healthResponse = jsonb.fromJson(response.body(), PaymentProcessorHealthResponse.class);

            System.out.println("default" + healthResponse.toString());
            PAYMENT_PROCESSOR_DEFAULT_OK = !healthResponse.failing();
            TIMEOUT = healthResponse.minResponseTime();

        } catch (Exception e) {
            System.out.println(e.getMessage());
            PAYMENT_PROCESSOR_DEFAULT_OK = false;
            processPaymentFallback();
        }
    }

    public void processPaymentFallback() {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {

            HttpRequest request = buildRequest("http://payment-processor-fallback:8080/payments/service-health");
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PaymentProcessorHealthResponse healthResponse = jsonb.fromJson(response.body(), PaymentProcessorHealthResponse.class);

            System.out.println("fallback" + healthResponse.toString());
            if (!PAYMENT_PROCESSOR_DEFAULT_OK) {
                TIMEOUT = healthResponse.minResponseTime();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private HttpRequest buildRequest(String endpoint) throws URISyntaxException {

        return HttpRequest.newBuilder()
                .uri(new URI(endpoint))
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }
}