CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              token TEXT NOT NULL UNIQUE,
                                              expiry_date INTEGER NOT NULL,
                                              user_id INTEGER NOT NULL,
                                              FOREIGN KEY (user_id) REFERENCES users(id)
);