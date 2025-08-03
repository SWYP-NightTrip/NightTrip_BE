package com.nighttrip.core.domain.touristspot.repository;

import com.nighttrip.core.domain.touristspot.entity.TourLike;
import com.nighttrip.core.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TourLikeRepository extends JpaRepository<TourLike, Long> {

    long countByUser(User user);

    Page<TourLike> findByUserOrderByLikedAtDesc(User user, Pageable pageable);
}
