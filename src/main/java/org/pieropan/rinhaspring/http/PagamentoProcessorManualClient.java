package org.pieropan.rinhaspring.http;

import java.io.IOException;

public interface PagamentoProcessorManualClient {

    boolean processaPagamento(String request) throws IOException, InterruptedException;
}