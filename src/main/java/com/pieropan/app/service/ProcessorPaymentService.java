package com.pieropan.app.service;

import com.mongodb.client.MongoCollection;
import com.pieropan.app.dto.PaymentProcessorRequest;
import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.mongo.MongoProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.bson.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@ApplicationScoped
public class ProcessorPaymentService {

    @Inject
    MongoProvider mongoProvider;

    private final Jsonb jsonb = JsonbBuilder.create();

    public void processPayment(PaymentRequest paymentRequest) {
        try (HttpClient httpClient = HttpClient.newHttpClient()) {

            HttpRequest request = buildRequest(paymentRequest);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                saveMongoDB(paymentRequest);
            }

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void saveMongoDB(PaymentRequest paymentRequest) {
        MongoCollection<Document> collection = mongoProvider.getDatabase().getCollection("payments");

        Document paymentDoc = new Document()
                .append("correlationId", paymentRequest.correlationId())
                .append("amount", paymentRequest.amount())
                .append("default", false)
                .append("createdAt", Instant.now());

        collection.insertOne(paymentDoc);
    }

    private HttpRequest buildRequest(PaymentRequest paymentRequest) throws URISyntaxException {
        String endpoint = Global.payment_default_ok ? "http://payment-processor-default:8080/payments"
                : "http://payment-processor-fallback:8080/payments";

        PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest(
                paymentRequest.correlationId(),
                paymentRequest.amount(),
                Instant.now());

        return HttpRequest.newBuilder()
                .uri(new URI(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonb.toJson(paymentProcessorRequest)))
                .build();
    }
}