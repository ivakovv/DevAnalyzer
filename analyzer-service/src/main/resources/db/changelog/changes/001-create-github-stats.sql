--liquibase formatted sql

--changeset devanalyzer:001
CREATE TABLE github_stats
(
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT NOT NULL UNIQUE,
    repositories INTEGER NOT NULL,
    stars INTEGER NOT NULL,
    forks INTEGER NOT NULL,
    followers INTEGER NOT NULL,
    commits INTEGER NOT NULL,
    age_in_days BIGINT NOT NULL,
    fetched_at TIMESTAMP NOT NULL
);
