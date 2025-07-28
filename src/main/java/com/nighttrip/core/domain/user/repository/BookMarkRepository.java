package com.nighttrip.core.domain.user.repository;

import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

    List<BookMark> findByUser(User user);
}
