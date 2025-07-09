package org.pieropan.rinhaspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class HttpClientConfiguration {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NEVER)
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .executor(Runnable::run)
                .build();
    }
}