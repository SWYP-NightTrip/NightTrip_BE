package com.nighttrip.core.domain.triporder.service;

import com.nighttrip.core.domain.triporder.entity.TripOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripOrderRepository extends JpaRepository<TripOrder, Long> {
}
