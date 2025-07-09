package com.pieropan.app.resource;


import com.mongodb.client.MongoCollection;
import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.mongo.MongoProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.bson.Document;

import java.time.Instant;

@Path("/payments")
public class PaymentResource {

    @Inject
    MongoProvider mongoProvider;

    @POST
    public void save(PaymentRequest paymentRequest) {

        MongoCollection<Document> collection = mongoProvider.getDatabase().getCollection("payments");

        Document paymentDoc = new Document()
                .append("correlationId", paymentRequest.correlationId())
                .append("amount", paymentRequest.amount())
                .append("default", false)
                .append("createdAt", Instant.now());

        collection.insertOne(paymentDoc);

        System.out.println(paymentRequest);
    }
}