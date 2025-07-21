package com.nighttrip.core.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "bookmark_folder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookMarkFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_folder_id")
    private Long id;

    @Column(name="folder_name", nullable=false)
    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "bookMarkFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookMark> bookMarks;
}
