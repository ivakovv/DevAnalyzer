--liquibase formatted sql

--changeset devanalyzer:002
CREATE TABLE commit_heatmap (
    id         BIGSERIAL PRIMARY KEY,
    github_id  BIGINT NOT NULL,
    week_start DATE NOT NULL,
    days       INTEGER[] NOT NULL,
    total      INTEGER NOT NULL DEFAULT 0,
    UNIQUE (github_id, week_start),
    FOREIGN KEY (github_id) REFERENCES github_stats(github_id)
);