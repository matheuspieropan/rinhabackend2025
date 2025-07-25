package org.pieropan.rinhaspring.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
public class PagamentoHttpClientConfig {

    @Bean(name = "pagamentoProcessorDefaultClient")
    public PagamentoProcessorManualClient defaultClient(@Value("${pagamento.processor.default.url}") String url,
                                                        HttpClient httpClient) {
        return new PagamentoProcessorHttpClientImpl(url, httpClient);
    }
}