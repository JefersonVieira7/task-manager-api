CREATE TABLE tasks (
                       id           UUID         PRIMARY KEY,
                       title        VARCHAR(255) NOT NULL,
                       description  TEXT,
                       due_date     TIMESTAMP,
                       status       VARCHAR(20)  NOT NULL,
                       priority     VARCHAR(20)  NOT NULL,
                       task_list_id UUID         NOT NULL REFERENCES task_lists(id) ON DELETE CASCADE,
                       created_at   TIMESTAMP    NOT NULL,
                       updated_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_tasks_task_list_id ON tasks(task_list_id);
CREATE INDEX idx_tasks_status       ON tasks(status);