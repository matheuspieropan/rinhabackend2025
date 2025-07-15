package com.pieropan.app.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoProvider {

    private final MongoClient client;

    public MongoDatabase getDatabase() {
        return client.getDatabase("rinhabackend");
    }

    public MongoProvider() {
        ConnectionString connectionString = new ConnectionString("mongodb://admin:123456@mongodb:27017/?authSource=admin");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(250)
                        .minSize(20)
                )
                .build();

        this.client = MongoClients.create(settings);
    }
}