package org.pieropan.rinhaspring.repository;

import org.pieropan.rinhaspring.document.PagamentoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PagamentoRepository extends MongoRepository<PagamentoDocument, String> {
}