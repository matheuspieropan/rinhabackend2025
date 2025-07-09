package com.pieropan.app.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MongoProvider {

    private final MongoClient client = MongoClients.create("mongodb://admin:123456@localhost:27017/?authSource=admin");

    public MongoDatabase getDatabase() {
        return client.getDatabase("rinhabackend");
    }
}