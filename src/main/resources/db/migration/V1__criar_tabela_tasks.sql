CREATE TABLE tasks (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    title          TEXT    NOT NULL CHECK (length(title) BETWEEN 1 AND 120),
    description    TEXT             CHECK (description IS NULL OR length(description) <= 1000),
    scheduled_date TEXT    NOT NULL,
    completed      INTEGER NOT NULL DEFAULT 0 CHECK (completed IN (0, 1)),
    created_at     TEXT    NOT NULL,
    updated_at     TEXT    NOT NULL
);

CREATE INDEX idx_tasks_scheduled_date ON tasks (scheduled_date);
