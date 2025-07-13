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
import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public class ProcessorPaymentService {

    @Inject
    MongoProvider mongoProvider;

    private final Jsonb jsonb = JsonbBuilder.create();

    public void processPayment(PaymentRequest paymentRequest) {
        boolean paymentProcessorDefault = PaymentProcessorHealthService.PAYMENT_PROCESSOR_DEFAULT_OK;
        try (HttpClient httpClient = HttpClient.newHttpClient()) {

            Instant createdAt = Instant.now();
            HttpRequest request = buildRequest(paymentRequest, paymentProcessorDefault, createdAt);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                saveMongoDB(paymentRequest, paymentProcessorDefault, createdAt);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void saveMongoDB(PaymentRequest paymentRequest,
                             boolean paymentProcessorDefault,
                             Instant createdAt) {
        MongoCollection<Document> collection = mongoProvider.getDatabase().getCollection("payments");

        Document paymentDoc = new Document()
                .append("correlationId", paymentRequest.correlationId())
                .append("amount", paymentRequest.amount())
                .append("paymentProcessorDefault", paymentProcessorDefault)
                .append("createdAt", createdAt);

        collection.insertOne(paymentDoc);
    }

    private HttpRequest buildRequest(PaymentRequest paymentRequest,
                                     boolean paymentProcessorDefault,
                                     Instant createdAt) throws URISyntaxException {

        String endpoint = paymentProcessorDefault
                ? "http://payment-processor-default:8080/payments"
                : "http://payment-processor-fallback:8080/payments";

        PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest(
                paymentRequest.correlationId(),
                paymentRequest.amount(),
                createdAt);

        return HttpRequest.newBuilder()
                .uri(new URI(endpoint))
                .timeout(Duration.ofMillis(PaymentProcessorHealthService.TIMEOUT + 200))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonb.toJson(paymentProcessorRequest)))
                .build();
    }
}