package com.nighttrip.core.domain.touristspot.entity;

import com.nighttrip.core.domain.city.entity.City;
import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.global.converter.SpotCategoryConverter;
import com.nighttrip.core.global.enums.SpotCategory;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_order_id")
    private TripOrder tripOrder;

    @OneToMany(mappedBy = "touristSpot")
    private List<BookMark> bookMarks = new ArrayList<>();

    @OneToMany(mappedBy = "touristSpot")
    private List<TouristSpotReview> touristSpotReviews = new ArrayList<>();
    @OneToMany(mappedBy = "touristSpot")
    private List<TourLike> tourLikes = new ArrayList<>();
    @OneToMany(mappedBy = "touristSpot")
    private List<TouristSpotImageUri> touristSpotImageUris = new ArrayList<>();
}