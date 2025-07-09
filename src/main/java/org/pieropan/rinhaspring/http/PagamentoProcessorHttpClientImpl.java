package org.pieropan.rinhaspring.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpRequest.BodyPublishers.ofString;

public class PagamentoProcessorHttpClientImpl implements PagamentoProcessorManualClient {

    private final String baseUrl;
    private final HttpClient httpClient;

    public PagamentoProcessorHttpClientImpl(String baseUrl, HttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
    }

    @Override
    public boolean processaPagamento(String pagamento) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().
                uri(URI.create(baseUrl + "/payments")).POST(ofString(pagamento)).
                header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() == 200;
    }
}