package org.pieropan.rinhaspring.http;

import org.pieropan.rinhaspring.model.HealthResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

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
        HttpResponse<Void> response = criaHttpResponse(pagamento);
        return response.statusCode() == 200 || response.statusCode() == 422;
    }

    @Override
    public boolean checaPagamentoRepetido(String request) throws IOException, InterruptedException {
        HttpResponse<Void> httpResponse = criaHttpResponse(request);
        return httpResponse.statusCode() == 422;
    }

    @Override
    public HealthResponse healthCheck() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().
                uri(URI.create(baseUrl + "/payments/service-health")).
                GET().
                header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        String body = response.body();

        return getHealthResponse(body);
    }

    private HttpResponse<Void> criaHttpResponse(String pagamento) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder().
                timeout(Duration.ofMillis(500)).
                uri(URI.create(baseUrl + "/payments")).POST(ofString(pagamento)).
                header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).
                build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.discarding());
    }

    private HealthResponse getHealthResponse(String body) {
        String json = body.replaceAll("[{}\\s\"]", "");

        String[] pairs = json.split(",");

        boolean failing = false;
        int minResponseTime = 0;

        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv[0].equals("failing")) {
                failing = Boolean.parseBoolean(kv[1]);
            } else if (kv[0].equals("minResponseTime")) {
                minResponseTime = Integer.parseInt(kv[1]);
            }
        }

        return new HealthResponse(failing, minResponseTime);
    }
}