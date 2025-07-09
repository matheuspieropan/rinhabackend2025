package org.pieropan.rinhaspring.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PagamentoHttpClientConfig {

    @Bean(name = "pagamentoProcessorDefaultClient")
    public PagamentoProcessorManualClient defaultClient(@Value("${pagamento.processor.default.url}") String url,
                                                        ObjectMapper objectMapper) {
        return new PagamentoProcessorHttpClientImpl(url, objectMapper);
    }

    @Bean(name = "pagamentoProcessorFallbackClient")
    public PagamentoProcessorManualClient fallbackClient(@Value("${pagamento.processor.fallback.url}") String url,
                                                         ObjectMapper objectMapper) {
        return new PagamentoProcessorHttpClientImpl(url, objectMapper);
    }
}