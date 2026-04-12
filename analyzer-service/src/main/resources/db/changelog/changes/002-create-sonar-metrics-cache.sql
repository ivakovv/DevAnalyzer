CREATE TABLE sonar_metrics_cache (
    id BIGSERIAL PRIMARY KEY,
    
    repository_full_name VARCHAR(255) NOT NULL,
    branch VARCHAR(100) NOT NULL DEFAULT 'main',
    last_pushed_at TIMESTAMP NOT NULL,
    
    quality_gate_status VARCHAR(20),
    bugs INTEGER,
    vulnerabilities INTEGER,
    code_smells INTEGER,
    coverage DOUBLE PRECISION,
    duplications DOUBLE PRECISION,
    lines_of_code INTEGER,
    security_rating VARCHAR(1),
    reliability_rating VARCHAR(1),
    maintainability_rating VARCHAR(1),
    tech_stack TEXT[],
    
    analyzed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT uk_repo_pushed UNIQUE (repository_full_name, last_pushed_at)
);

CREATE INDEX idx_repo_name ON sonar_metrics_cache(repository_full_name);
CREATE INDEX idx_analyzed_at ON sonar_metrics_cache(analyzed_at DESC);

COMMENT ON TABLE sonar_metrics_cache IS 'Кеш результатов SonarQube анализа для избежания повторных сканирований';
COMMENT ON COLUMN sonar_metrics_cache.last_pushed_at IS 'Дата последнего коммита в репозитории - ключ для инвалидации кеша';
