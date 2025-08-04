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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @OneToMany(mappedBy = "touristSpot")
    private List<BookMark> bookMarks = new ArrayList<>();

    @OneToMany(mappedBy = "touristSpot")
    private List<TouristSpotReview> touristSpotReviews = new ArrayList<>();
    @OneToMany(mappedBy = "touristSpot")
    private List<TourLike> tourLikes = new ArrayList<>();

    @Convert(converter = SpotDetailsConverter.class)
    @Column(name = "tourist_spot_details", columnDefinition = "TEXT")
    private EnumSet<SpotDetails> touristSpotDetails = EnumSet.noneOf(SpotDetails.class);

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
}