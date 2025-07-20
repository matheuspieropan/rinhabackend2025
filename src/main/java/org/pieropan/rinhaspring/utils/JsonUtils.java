package org.pieropan.rinhaspring.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;
import org.springframework.stereotype.Component;

@Component
public class JsonMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String toJson(Object pagamentoProcessorRequest) {
        try {
            return objectMapper.writeValueAsString(pagamentoProcessorRequest);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing PagamentoProcessorRequest");
        }
    }
}
