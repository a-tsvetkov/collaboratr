# Documents schema

# --- !Ups

CREATE TABLE "document" (
       id SERIAL PRIMARY KEY,
       title VARCHAR(1000),
       body TEXT,
       owner_id INT NOT NULL,
       created_at TIMESTAMP NOT NULL,
       updated_at TIMESTAMP NOT NULL,
       FOREIGN KEY (owner_id) REFERENCES "users"
)

# --- !Downs

DROP TABLE "document"
