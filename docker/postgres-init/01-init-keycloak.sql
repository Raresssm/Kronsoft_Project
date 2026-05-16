-- Keycloak database (runs once when the Postgres volume is first created).
CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak OWNER keycloak;
