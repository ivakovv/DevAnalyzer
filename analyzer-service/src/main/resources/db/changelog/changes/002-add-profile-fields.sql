--liquibase formatted sql

--changeset devanalyzer:002
ALTER TABLE github_stats
    ADD COLUMN login VARCHAR(255) NOT NULL DEFAULT '',
    ADD COLUMN name VARCHAR(255),
    ADD COLUMN location VARCHAR(255),
    ADD COLUMN company VARCHAR(255);
