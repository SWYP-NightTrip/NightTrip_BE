package com.nighttrip.core.global.dto;

import com.nighttrip.core.global.util.LocationFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
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

    @Field(type = FieldType.Text)
    private List<String> suggestName;

    public SearchDocument withFormattedCityName() {
        String formattedCityName = LocationFormatter.formatForSearch(this.cityName);
        return new SearchDocument(
                this.id,
                this.type,
                this.name,
                this.description,
                formattedCityName, // <-- 이 부분만 변경됩니다.
                this.category,
                this.imageUrl,
                this.suggestName
        );
    }
}