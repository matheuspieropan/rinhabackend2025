package org.pieropan.rinhaspring.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
public class HttpClientConfig {
    @Value("${rinha.payment.processor.timeout-default}")
    public int timeoutDefault;

    @Value("${rinha.payment.processor.timeout-fallback}")
    public int timeoutFallback;

    @Value("${rinha.payment.processor.url.default}")
    public String paymentProcessorUrlDefault;

    @Value("${rinha.payment.processor.url.fallback}")
    public String paymentProcessorUrlFallback;

    @Bean
    public URI uriDefault() {
        return URI.create(paymentProcessorUrlDefault);
    }

    @Bean
    public URI uriFallback() {
        return URI.create(paymentProcessorUrlFallback);
    }

    @Bean
    public Duration timeoutApiFallback() {
        return Duration.ofMillis(timeoutFallback);
    }

    @Bean
    public Duration timeoutApiDefault() {
        return Duration.ofMillis(timeoutDefault);
    }

    @Bean
    public java.net.http.HttpClient paymentClient() {
        return java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .executor(Runnable::run)
                .build();
    }
}
