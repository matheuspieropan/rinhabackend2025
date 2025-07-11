package com.pieropan.app.rabbit;

import com.pieropan.app.dto.PaymentRequest;
import com.pieropan.app.service.ProcessorPaymentService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;

@ApplicationScoped
public class RabbitProvider {

    private Channel channel;

    private Connection connection = null;

    private final Jsonb jsonb = JsonbBuilder.create();

    @Inject
    private ProcessorPaymentService processorPaymentService;

    @PostConstruct
    public void init() {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setPort(5672);

            connection = factory.newConnection();
            channel = connection.createChannel();

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String body = new String(delivery.getBody());
                try {
                    PaymentRequest request = jsonb.fromJson(body, PaymentRequest.class);
                    processorPaymentService.processPayment(request);
                } catch (Exception e) {
                    System.out.println("Erro ao processar mensagem: " + e.getMessage());
                }
            };

            channel.basicConsume("processor-payment.queue", true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar no RabbitMQ: " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void close() {
        try {
            if (channel != null && channel.isOpen()) channel.close();
            if (connection != null && connection.isOpen()) connection.close();
            System.out.println("RabbitMQ listener encerrado!");
        } catch (Exception e) {
            System.out.println("Erro ao fechar RabbitMQ: " + e.getMessage());
        }
    }

    public void send(PaymentRequest paymentRequest) {
        try {
            channel.basicPublish("processor-payment.ex", "", null, jsonb.toJson(paymentRequest).getBytes());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao enviar mensagem: " + e.getMessage(), e);
        }
    }
}