package com.pieropan.app.resource;


import com.mongodb.client.MongoCollection;
import com.pieropan.app.dto.PaymentProcessorRequest;
import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.mongo.MongoProvider;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.bson.Document;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;

@Path("/payments")
public class PaymentResource {

    @Inject
    MongoProvider mongoProvider;

    private final Jsonb jsonb = JsonbBuilder.create();

    @POST
    public void save(PaymentRequest paymentRequest) {

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

    private HttpRequest buildRequest(PaymentRequest paymentRequest) throws URISyntaxException {
        PaymentProcessorRequest paymentProcessorRequest = new PaymentProcessorRequest(
                paymentRequest.correlationId(),
                paymentRequest.amount(),
                Instant.now());

        return HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8001/payments"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonb.toJson(paymentProcessorRequest)))
                .build();
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
}