package com.pieropan.app.resource;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.pieropan.app.dto.PaymentProcessor;
import com.pieropan.app.dto.PaymentSummaryResponse;
import com.pieropan.app.mongo.MongoProvider;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Path("/payments-summary")
public class PaymentSummaryResource {

    @Inject
    MongoProvider mongoProvider;

    @GET
    @Produces("application/json")
    public PaymentSummaryResponse paymentSummary(@QueryParam("from") String from,
                                                 @QueryParam("to") String to) {

        Instant fromDate = Instant.parse(from);
        Instant toDate = Instant.parse(to);


        Bson dateFilter = Filters.and(
                Filters.gte("createdAt", Date.from(fromDate)),
                Filters.lte("createdAt", Date.from(toDate))
        );

        MongoCollection<Document> collection = mongoProvider.getDatabase().getCollection("payments");

        AggregateIterable<Document> results = collection.aggregate(List.of(
                Aggregates.match(dateFilter),
                Aggregates.group("$default",
                        Accumulators.sum("totalAmount", "$amount"),
                        Accumulators.sum("totalRequests", 1)))
        );

        double defaultAmount = 0.0;
        long defaultRequests = 0;
        double fallbackAmount = 0.0;
        long fallbackRequests = 0;

        for (Document doc : results) {
            Boolean isDefault = doc.getBoolean("_id");
            double amount = doc.getDouble("totalAmount");
            int count = doc.getInteger("totalRequests");

            if (Boolean.TRUE.equals(isDefault)) {
                defaultAmount = amount;
                defaultRequests = count;
            } else {
                fallbackAmount = amount;
                fallbackRequests = count;
            }
        }

        PaymentProcessor defaultSummary = new PaymentProcessor((int) defaultRequests, defaultAmount);
        PaymentProcessor fallbackSummary = new PaymentProcessor((int) fallbackRequests, fallbackAmount);

        return new PaymentSummaryResponse(defaultSummary, fallbackSummary);
    }
}