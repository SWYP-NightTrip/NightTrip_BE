package com.nighttrip.core.global.dto;

import com.nighttrip.core.global.util.LocationFormatter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

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

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "korean_search_analyzer_v2"),
            otherFields = {
                    @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "korean_autocomplete_analyzer_v2")
            }
    )
    private String name;

    @Field(type = FieldType.Text, analyzer = "korean_search_analyzer_v2")
    private String description;

    @Field(type = FieldType.Text, analyzer = "korean_search_analyzer_v2")
    private String cityName;

    @Field(type = FieldType.Text, analyzer = "korean_search_analyzer_v2")
    private String address;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword, index = false)
    private String imageUrl;

    public SearchDocument withFormattedCityName() {
        String formattedCityName = LocationFormatter.formatForSearch(this.cityName);
        return new SearchDocument(
                this.id,
                this.type,
                this.name,
                this.description,
                formattedCityName,
                this.address,
                this.category,
                this.imageUrl
        );
    }
}