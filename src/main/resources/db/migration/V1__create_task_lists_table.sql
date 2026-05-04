CREATE TABLE task_lists (
                            id          UUID        PRIMARY KEY,
                            title       VARCHAR(255) NOT NULL,
                            description TEXT,
                            created_at  TIMESTAMP   NOT NULL,
                            updated_at  TIMESTAMP   NOT NULL
);