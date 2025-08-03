package com.nighttrip.core.domain.avatar.repository;

import com.nighttrip.core.domain.avatar.entity.Avatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {

    Optional<Avatar> findByLevel(int level);
}
