package com.nighttrip.core.global.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Builder
@Document(indexName = "search_documents")
public class SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String type;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Text)
    private String cityName;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    @Field(type = FieldType.Text) // 이제 text 타입
    private List<String> suggestName;
}