package com.pieropan.app.service;

import com.mongodb.client.MongoCollection;
import com.pieropan.app.dto.PaymentProcessorRequest;
import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.mongo.MongoProvider;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
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
import java.util.function.Supplier;

@ApplicationScoped
public class ProcessorPaymentService {

    @Inject
    private MongoProvider mongoProvider;

    @Inject
    private CircuitBreaker circuitBreaker;

    private final Jsonb jsonb = JsonbBuilder.create();

    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();

    public void processPayment(PaymentRequest paymentRequest) {
        Instant createdAt = Instant.now();

        Supplier<Boolean> meuSupplier = () -> {
            try {
                HttpRequest request = buildRequest(paymentRequest, true, createdAt);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    saveMongoDB(paymentRequest, true, createdAt);
                    return true;
                }
                return false;
            } catch (Exception e) {
                System.out.println("Erro no processador default: " + e.getMessage());
                throw new RuntimeException("Erro no processador default: " + e.getMessage(), e);
            }
        };

        Supplier<Boolean> supplier = circuitBreaker.decorateSupplier(meuSupplier);

        try {
            supplier.get();
        } catch (CallNotPermittedException ex) {

            try {
                HttpRequest fallbackRequest = buildRequest(paymentRequest, false, createdAt);
                HttpResponse<String> fallbackResponse = httpClient.send(fallbackRequest, HttpResponse.BodyHandlers.ofString());

                if (fallbackResponse.statusCode() == 200) {
                    saveMongoDB(paymentRequest, false, createdAt);
                } else {
                    processPayment(paymentRequest);
                }
            } catch (Exception fallbackEx) {
                System.out.println("fallbackEx: " + fallbackEx.getMessage());
                processPayment(paymentRequest);
            }
        } catch (Exception ex) {
            System.out.println("ex do supplier" + ex.getMessage());
            processPayment(paymentRequest);
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
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonb.toJson(paymentProcessorRequest)))
                .build();
    }
}