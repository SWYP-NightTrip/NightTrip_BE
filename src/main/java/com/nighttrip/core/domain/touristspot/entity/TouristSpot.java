package com.nighttrip.core.domain.touristspot.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.global.converter.SpotCategoryConverter;
import com.nighttrip.core.global.converter.SpotDetailsConverter;
import com.nighttrip.core.global.enums.SpotCategory;
import com.nighttrip.core.global.enums.SpotDetails;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.*;

@Entity
@Table(name = "tourist_spot",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"city_id", "spot_name"})
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TouristSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tourist_spot_id")
    private Long id;


    @Column(name = "spot_name", nullable = false, length = 100)
    private String spotName;

    private Double longitude;
    private Double latitude;
    private Integer checkCount;

    private String address;
    private String link;

    @Convert(converter = SpotCategoryConverter.class)
    private SpotCategory category;

    private String spotDescription;
    private String telephone;
    private Integer mainWeight;
    private Integer subWeight;

    @Column(columnDefinition = "jsonb")
    private String computedMeta;

    private Integer metaVersion;
    private Timestamp metaUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "touristSpot")
    private List<BookMark> bookMarks = new ArrayList<>();

    @OneToMany(mappedBy = "touristSpot")
    private List<TouristSpotReview> touristSpotReviews = new ArrayList<>();
    @OneToMany(mappedBy = "touristSpot")
    private List<TourLike> tourLikes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tourist_spot_hashtags",
            joinColumns = @JoinColumn(name = "tourist_spot_id")
    )
    @Column(name = "hashtag", length = 100, nullable = false)
    private Set<String> hashTags = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "tourist_spot_details",
            joinColumns = @JoinColumn(name = "tourist_spot_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "detail", nullable = false, length = 64)
    private Set<SpotDetails> touristSpotDetails = EnumSet.noneOf(SpotDetails.class);

    @Builder
    public TouristSpot(String spotName, Double longitude, Double latitude,
                       Integer checkCount, String address, String link,
                       SpotCategory category, String spotDescription, String telephone,
                       Integer mainWeight, Integer subWeight, City city, EnumSet<SpotDetails> touristSpotDetails) {
        this.spotName = spotName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.checkCount = checkCount;
        this.address = address;
        this.link = link;
        this.category = category;
        this.spotDescription = spotDescription;
        this.telephone = telephone;
        this.mainWeight = mainWeight;
        this.subWeight = subWeight;
        this.city = city;
        this.touristSpotDetails = touristSpotDetails != null ? touristSpotDetails : EnumSet.noneOf(SpotDetails.class);
    }


    public void changeHashTagsFrom(Collection<String> tags) {
        this.hashTags.clear();
        if (tags == null) return;

        for (String s : tags) {
            if (s == null) continue;
            String norm = s.trim().toLowerCase();
            if (!norm.isEmpty()) {
                this.hashTags.add(norm);
            }
        }
    }

    public List<String> getHashTagsAsList() {
        return List.copyOf(this.hashTags);
    }

    public void changeComputedMeta(String metaJson) {
        this.computedMeta = metaJson;
    }

    public void changeMetaVersion(int metaVersion) {
        this.metaVersion = metaVersion;
    }

    public void changeMetaUpdatedAt(Timestamp now) {
        this.metaUpdatedAt = now;
    }
}