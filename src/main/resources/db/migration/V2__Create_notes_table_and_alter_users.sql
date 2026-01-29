
ALTER TABLE users ADD COLUMN role TEXT DEFAULT 'ROLE_USER';

CREATE TABLE IF NOT EXISTS notes (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     title TEXT NOT NULL,
                                     content TEXT,
                                     user_id INTEGER NOT NULL,
                                     FOREIGN KEY (user_id) REFERENCES users(id)
);