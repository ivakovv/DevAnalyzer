package ru.devanalyzer.analyzer_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "github_stats")
public class GitHubStatsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", unique = true, nullable = false)
    private Long githubId;

    @Column(nullable = false)
    private int repositories;

    @Column(nullable = false)
    private int stars;

    @Column(nullable = false)
    private int forks;

    @Column(nullable = false)
    private int followers;

    @Column(nullable = false)
    private int commits;

    @Column(name = "age_in_days", nullable = false)
    private long ageInDays;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
}
