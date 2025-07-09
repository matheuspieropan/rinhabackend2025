package org.pieropan.rinhaspring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PagamentoProcessorHttpClientImpl implements PagamentoProcessorManualClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PagamentoProcessorHttpClientImpl(String baseUrl, ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean processaPagamento(PagamentoProcessorRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/payments"))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception ignored) {
            return false;
        }
    }
}