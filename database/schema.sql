CREATE DATABASE IF NOT EXISTS money_transfer_db;
USE money_transfer_db;


CREATE TABLE users (
    username VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);


CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holder_name VARCHAR(255),
    balance DECIMAL(38,2) NOT NULL,
    status ENUM('ACTIVE','CLOSED','LOCKED'),
    version INT,
    last_updated DATETIME(6),
    username VARCHAR(255) NOT NULL UNIQUE,

    CONSTRAINT fk_accounts_user
        FOREIGN KEY (username)
        REFERENCES users(username)
        ON DELETE CASCADE
);


CREATE TABLE transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(38,2) NOT NULL,
    status ENUM('SUCCESS','FAILED'),
    failure_reason VARCHAR(255),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    created_on DATETIME(6),

    CONSTRAINT fk_tx_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_tx_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id)
);

CREATE TABLE rewards (
    id VARCHAR(36) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    transaction_id VARCHAR(36) NOT NULL,
    reward_points BIGINT NOT NULL,
    earned_on DATETIME(6),

    CONSTRAINT fk_reward_user
        FOREIGN KEY (user_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_reward_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transaction_logs(id)
);

CREATE INDEX idx_accounts_username ON accounts(username);
CREATE INDEX idx_tx_from_account ON transaction_logs(from_account_id);
CREATE INDEX idx_tx_to_account ON transaction_logs(to_account_id);
CREATE INDEX idx_tx_created_on ON transaction_logs(created_on);
CREATE INDEX idx_reward_user ON rewards(user_id);
CREATE INDEX idx_reward_transaction ON rewards(transaction_id);
CREATE INDEX idx_reward_earned_on ON rewards(earned_on);
