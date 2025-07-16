package org.pieropan.rinhabackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.pieropan.rinhabackend.dto.PaymentProcessorRequest;
import org.pieropan.rinhabackend.dto.PaymentRequest;
import org.pieropan.rinhabackend.mongo.MongoProvider;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.pieropan.rinhabackend.util.ObjectMapperUtil.getObjectMapper;

public class ProcessorPaymentService {

    private final MongoClient mongoProvider = MongoProvider.getInstance().getMongoClient();

    private final HttpClient httpClient = createHttpClient();

    private final ObjectMapper objectMapper = getObjectMapper();

    public static Queue<PaymentRequest> paymentsPending = new ConcurrentLinkedQueue<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ProcessorPaymentService() {
        startReprocessJob();
    }

    public void startReprocessJob() {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                processPendingPayments();
            } catch (Exception ignored) {
            }
        }, 10, 1, TimeUnit.SECONDS);
    }

    private void processPendingPayments() {
        if (paymentsPending.isEmpty()) {
            return;
        }

        System.out.printf("Iniciando reprocessamento. Total pendentes: %d", paymentsPending.size());

        for (PaymentRequest payment : paymentsPending) {
            try {
                Instant createdAt = Instant.now();

                HttpRequest request = buildRequest(payment, true, createdAt);
                boolean success = trySend(request, payment, true, createdAt);

                if (success) {
                    paymentsPending.remove(payment);
                } else {
                    break;
                }
            } catch (Exception ignored) {
                break;
            }
        }
    }

    public void processPayment(PaymentRequest paymentRequest) {
        Instant createdAt = Instant.now();

        try {
            HttpRequest defaultRequest = buildRequest(paymentRequest, true, createdAt);

            boolean success = trySend(defaultRequest, paymentRequest, true, createdAt);

            if (!success) {
                HttpRequest fallbackRequest = buildRequest(paymentRequest, false, createdAt);
                boolean fallbackSuccess = trySend(fallbackRequest, paymentRequest, false, createdAt);

                if (!fallbackSuccess) {
                    paymentsPending.add(paymentRequest);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro processPayment(PaymentRequest paymentRequest)");
        }
    }

    private boolean trySend(HttpRequest request, PaymentRequest paymentRequest, boolean processorDefault, Instant createdAt) {
        try {
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            saveMongoDB(paymentRequest, processorDefault, createdAt);
                            return true;
                        }
                        return false;
                    })
                    .exceptionally(ex -> false).join();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void saveMongoDB(PaymentRequest paymentRequest, boolean paymentProcessorDefault, Instant createdAt) {
        MongoCollection<Document> collection = mongoProvider.getDatabase("rinhabackend").getCollection("payments");

        Document paymentDoc = new Document()
                .append("correlationId", paymentRequest.correlationId())
                .append("amount", paymentRequest.amount())
                .append("paymentProcessorDefault", paymentProcessorDefault)
                .append("createdAt", createdAt);

        collection.insertOne(paymentDoc);
    }

    private HttpRequest buildRequest(PaymentRequest paymentRequest, boolean paymentProcessorDefault, Instant createdAt)
            throws URISyntaxException, JsonProcessingException {

          String endpoint = paymentProcessorDefault
                    ? "http://payment-processor-default:8080/payments"
                    : "http://payment-processor-fallback:8080/payments";

//        String endpoint = paymentProcessorDefault
//                ? "http://localhost:8001/payments"
//                : "http://localhost:8002/payments";

        PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest(
                paymentRequest.correlationId(),
                paymentRequest.amount(),
                createdAt);

        return HttpRequest.newBuilder()
                .uri(new URI(endpoint))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(paymentProcessorRequest)))
                .build();
    }

    private static HttpClient createHttpClient() {
        return HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    }
}