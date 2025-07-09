package org.pieropan.rinhaspring;

import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RinhaSpringApplication implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public RinhaSpringApplication(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(RinhaSpringApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Document index = new Document()
                .append("createdAt", 1)
                .append("paymentProcessorDefault", 1);

        IndexDefinition indexDefinition = new CompoundIndexDefinition(index);

        mongoTemplate.indexOps("payments").createIndex(indexDefinition);
    }
}