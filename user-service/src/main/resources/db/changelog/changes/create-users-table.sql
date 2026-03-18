--liquibase formatted sql
--changeset max:001-create-users-table
CREATE TABLE table_users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(80),
    first_name  VARCHAR(50),
    patronymic  VARCHAR(50),
    last_name   VARCHAR(50),
    role        VARCHAR(30),
    company     VARCHAR(255),
    password    VARCHAR(255),
    position    VARCHAR(255),
    created_at  TIMESTAMP WITH TIME ZONE,
    updated_at  TIMESTAMP WITH TIME ZONE
);
