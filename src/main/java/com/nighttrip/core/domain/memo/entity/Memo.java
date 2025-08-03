package com.nighttrip.core.domain.memo.entity;

import com.nighttrip.core.domain.triporder.entity.TripOrder;
import com.nighttrip.core.global.enums.MemoType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "memo")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Memo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "memo_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_order_id", nullable = false, unique = true)
    private TripOrder tripOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "memo_type", length = 20)
    private MemoType memoType;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createdAt;

}