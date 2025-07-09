package org.pieropan.rinhaspring.config;

import org.pieropan.rinhaspring.model.PagamentoProcessorRequest;

public interface PagamentoProcessorManualClient {

    boolean processaPagamento(PagamentoProcessorRequest request);
}