package org.pieropan.rinhaspring.http;

import org.pieropan.rinhaspring.model.HealthResponse;

import java.io.IOException;

public interface PagamentoProcessorManualClient {

    boolean processaPagamento(String request) throws IOException, InterruptedException;

    boolean checaPagamentoRepetido(String request) throws IOException, InterruptedException;

    HealthResponse healthCheck() throws IOException, InterruptedException;
}