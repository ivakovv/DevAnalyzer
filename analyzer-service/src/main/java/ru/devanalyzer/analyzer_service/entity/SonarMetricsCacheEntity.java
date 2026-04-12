package ru.devanalyzer.analyzer_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sonar_metrics_cache")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SonarMetricsCacheEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "repository_full_name", nullable = false)
    private String repositoryFullName;
    
    @Column(name = "branch", nullable = false)
    private String branch;
    
    @Column(name = "last_pushed_at", nullable = false)
    private Instant lastPushedAt;
    
    @Column(name = "quality_gate_status")
    private String qualityGateStatus;
    
    @Column(name = "bugs")
    private Integer bugs;
    
    @Column(name = "vulnerabilities")
    private Integer vulnerabilities;
    
    @Column(name = "code_smells")
    private Integer codeSmells;
    
    @Column(name = "coverage")
    private Double coverage;
    
    @Column(name = "duplications")
    private Double duplications;
    
    @Column(name = "lines_of_code")
    private Integer linesOfCode;
    
    @Column(name = "security_rating")
    private String securityRating;
    
    @Column(name = "reliability_rating")
    private String reliabilityRating;
    
    @Column(name = "maintainability_rating")
    private String maintainabilityRating;
    
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tech_stack", columnDefinition = "text[]")
    private List<String> techStack;
    
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        analyzedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
