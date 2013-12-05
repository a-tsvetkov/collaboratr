# Documents to users reference table

# --- !Ups

CREATE TABLE "document_user" (
       user_id INT,
       document_id INT,
       PRIMARY KEY (user_id, document_id),
       FOREIGN KEY (user_id) REFERENCES "users",
       FOREIGN KEY (document_id) REFERENCES "document"
)

# --- !Downs

DROP TABLE "document_user"
