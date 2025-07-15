package com.pieropan.app.service;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.pieropan.app.dto.PaymentProcessor;
import com.pieropan.app.dto.PaymentSummaryResponse;
import com.pieropan.app.mongo.MongoProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Accumulators.sum;
import static com.mongodb.client.model.Aggregates.group;
import static com.mongodb.client.model.Aggregates.match;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.gte;
import static com.mongodb.client.model.Filters.lte;

@ApplicationScoped
public class PaymentSummaryService {

    @Inject
    private MongoProvider mongoProvider;

    public PaymentSummaryResponse paymentSummary(String from,
                                                 String to) {

        Instant fromDate = parseFlexibleDate(from);
        Instant toDate = parseFlexibleDate(to);

        Bson dateFilter = and(
                gte("createdAt", Date.from(fromDate)),
                lte("createdAt", Date.from(toDate))
        );

        MongoCollection<Document> collection = mongoProvider.getDatabase().getCollection("payments");

        AggregateIterable<Document> results = collection.aggregate(List.of(
                match(dateFilter),
                group("$paymentProcessorDefault",
                        sum("totalAmount", "$amount"),
                        sum("totalRequests", 1))
        ));

        BigDecimal defaultAmount = BigDecimal.ZERO;
        long defaultRequests = 0;
        BigDecimal fallbackAmount = BigDecimal.ZERO;
        long fallbackRequests = 0;

        for (Document doc : results) {
            Boolean isDefault = doc.getBoolean("_id");

            Number amountNumber = doc.get("totalAmount", Number.class);
            BigDecimal amount;

            if (amountNumber instanceof Double) {
                amount = BigDecimal.valueOf(amountNumber.doubleValue());
            } else if (amountNumber instanceof Integer) {
                amount = BigDecimal.valueOf(amountNumber.intValue());
            } else {
                amount = new BigDecimal(amountNumber.toString());
            }

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

    public Instant parseFlexibleDate(String dateStr) {
        return OffsetDateTime.parse(dateStr).toInstant();
    }
}