# Users schema

# --- !Ups

CREATE TABLE "users" (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    salt VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    date_joined TIMESTAMP NOT NULL
);

# --- !Downs

DROP TABLE "users";
