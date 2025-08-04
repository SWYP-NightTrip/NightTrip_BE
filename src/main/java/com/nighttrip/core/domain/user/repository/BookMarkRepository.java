package com.nighttrip.core.domain.user.repository;

import com.nighttrip.core.domain.user.entity.BookMark;
import com.nighttrip.core.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Long> {

    List<BookMark> findByBookMarkFolder_User(User user);

    @Query("SELECT COUNT(bm) FROM BookMark bm WHERE bm.bookMarkFolder.user = :user")
    long countByUser(@Param("user") User user);
}
