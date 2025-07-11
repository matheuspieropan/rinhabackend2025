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

    public void processPayment() {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {

            HttpRequest request = buildRequest();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            PaymentProcessorHealthResponse healthResponse = jsonb.fromJson(response.body(), PaymentProcessorHealthResponse.class);

            Global.payment_default_ok = !healthResponse.failing();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private HttpRequest buildRequest() throws URISyntaxException {

        return HttpRequest.newBuilder()
                .uri(new URI("http://payment-processor-default:8080/payments/service-health"))
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }
}