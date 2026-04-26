package com.plog.plogbackend.domain.Member.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "terms")
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name; // 약관명 (예: 서비스 이용약관 동의)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 약관 내용

    @Column(nullable = false)
    private boolean required; // 필수 여부

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 생성일

    @Column(nullable = false, length = 20)
    private String version; // 버전 (예: v1.0)

    @Builder
    public Terms(String name, String content, boolean required, String version) {
        this.name = name;
        this.content = content;
        this.required = required;
        this.version = version;
        this.createdAt = LocalDateTime.now();
    }
}
