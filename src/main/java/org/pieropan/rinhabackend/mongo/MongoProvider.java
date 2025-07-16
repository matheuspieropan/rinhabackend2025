package org.pieropan.rinhabackend.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public class MongoProvider {

    private final MongoClient client;

    private static MongoProvider instance;

    private MongoProvider() {
        ConnectionString connectionString = new ConnectionString("mongodb://admin:123456@mongodb:27017/?authSource=admin");

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .applyToConnectionPoolSettings(builder -> builder
                        .maxSize(250)
                        .minSize(20)
                ).build();

        client = MongoClients.create(settings);
    }

    public static MongoProvider getInstance() {
        if (instance == null) {
            instance = new MongoProvider();
        }
        return instance;
    }

    public MongoClient getMongoClient() {
        return client;
    }
}