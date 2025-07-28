package com.nighttrip.core.global.repository;

import com.nighttrip.core.global.dto.SearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SearchDocumentRepository extends ElasticsearchRepository<SearchDocument, String> {
    List<SearchDocument> findByName(String name);
}