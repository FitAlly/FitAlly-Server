package com.fitally.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exercises")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exerciseId;

    /** Wger 원본 ID (중복 저장 방지용) */
    @Column(unique = true)
    private Long wgerId;

    @Column(nullable = false)
    private String name;

    private String category;

    private String difficulty;

    private String emoji;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** 주요 사용 근육 목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> muscles = new ArrayList<>();

    /** 보조 근육 목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> musclesSecondary = new ArrayList<>();

    /** 필요 기구 목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> equipment = new ArrayList<>();

    /** 운동 이미지 URL 목록 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> images = new ArrayList<>();

    /** 운동 별칭 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> aliases = new ArrayList<>();

    /** 주의사항 / 팁 */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> notes = new ArrayList<>();

    /** 변형 동작 Wger ID */
    private Long variations;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Exercise(Long wgerId, String name, String category, String difficulty, String emoji,
                    String description, List<String> muscles, List<String> musclesSecondary,
                    List<String> equipment, List<String> images, List<String> aliases,
                    List<String> notes, Long variations) {
        this.wgerId = wgerId;
        this.name = name;
        this.category = category;
        this.difficulty = difficulty;
        this.emoji = emoji;
        this.description = description;
        this.muscles = muscles != null ? muscles : new ArrayList<>();
        this.musclesSecondary = musclesSecondary != null ? musclesSecondary : new ArrayList<>();
        this.equipment = equipment != null ? equipment : new ArrayList<>();
        this.images = images != null ? images : new ArrayList<>();
        this.aliases = aliases != null ? aliases : new ArrayList<>();
        this.notes = notes != null ? notes : new ArrayList<>();
        this.variations = variations;
    }
}
