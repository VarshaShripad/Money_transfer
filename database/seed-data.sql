--Replace passwords with actual hashed passwords
INSERT INTO users (username, password, role) VALUES
('alice', '$2a$10$5g6BMeer4NJzH8Nq32/VSOVjUD7Prow75rvNiyyJ1isHcEDFvlfXO', 'ROLE_USER'),
('bob', '$2a$10$jJgv82PghSKY4F76GNz8POxSokP3KQf2c2UbtlumwGkTgosO6suyO', 'ROLE_USER'),
('charlie', '$2a$10$/LJPqmnvi3m9f4VM8.cXh.iAGVsvs2sRVo6gEoq.hu2U6FRRWfkpq', 'ROLE_USER'),
('dave', '$2a$10$0B3T4XpGFGHD9ZbQEozEEOq283D37yjYZqbFSwil4cOSCZqml8r5S', 'ROLE_USER'),
('eve', '$2a$10$hc04/C2eGpvpnXuH95JMAe3dc49oFZSbrx93SpTb52etIHrNnsP1u', 'ROLE_USER'),
('frank', '$2a$10$WIVHdp8ucrkWFXu3Yu.hOuCHeEV7IcLR.46vSsnIODdaCrsB0Q48u', 'ROLE_USER'),
('grace', '$2a$10$XKtGOvQB1CTcoXAFTas6sezce6ZdMTqrp/b8a6iqhEvAs27MOYJ9W', 'ROLE_USER'),
('heidi', '$2a$10$c9yTwPjsQ9kaGNeqP15yHefIoHPQh3fbS5HM0jnXrHca7TYL5KhBa', 'ROLE_USER'),
('ivan', '$2a$10$9Y2WdxftPe/AnkzFUFKEmeMd5J9S4qjqxGFODX2QKsPr9O.Wv5Gb.', 'ROLE_USER'),
('judy', '$2a$10$5XfZMwFkHrsK8922KRBNh..MwRF.h5AsQ/mamye1KsCyCMYdUbdFG', 'ROLE_USER'),
('admin', '$2a$10$21aFKm0qn2wWgaCOn8ss8u.PhdGLONsXpgKAvW098cP5w9vucIRN.', 'ROLE_ADMIN');



INSERT INTO accounts (balance, holder_name, last_updated, status, version, username) VALUES
(1000.00, 'Alice', NOW(), 'ACTIVE', 1, 'alice'),
(1500.00, 'Bob', NOW(), 'LOCKED', 1, 'bob'),
(500.00, 'Charlie', NOW(), 'ACTIVE', 1, 'charlie'),
(2000.00, 'Dave', NOW(), 'CLOSED', 1, 'dave'),
(1200.00, 'Eve', NOW(), 'ACTIVE', 1, 'eve'),
(800.00, 'Frank', NOW(), 'LOCKED', 1, 'frank'),
(1700.00, 'Grace', NOW(), 'ACTIVE', 1, 'grace'),
(900.00, 'Heidi', NOW(), 'ACTIVE', 1, 'heidi'),
(600.00, 'Ivan', NOW(), 'CLOSED', 1, 'ivan'),
(1300.00, 'Judy', NOW(), 'ACTIVE', 1, 'judy'),
(5000.00, 'Admin', NOW(), 'ACTIVE', 1, 'admin');


-- Sample transaction logs for testing rewards
INSERT INTO transaction_logs (id, from_account_id, to_account_id, amount, status, failure_reason, idempotency_key, created_on) VALUES
('tx-001', 1, 2, 250.00, 'SUCCESS', NULL, 'idem-001', NOW()),
('tx-002', 1, 3, 150.00, 'SUCCESS', NULL, 'idem-002', NOW()),
('tx-003', 5, 6, 200.00, 'SUCCESS', NULL, 'idem-003', NOW()),
('tx-004', 7, 8, 350.00, 'SUCCESS', NULL, 'idem-004', NOW()),
('tx-005', 10, 1, 500.00, 'SUCCESS', NULL, 'idem-005', NOW()),
('tx-006', 1, 1, 100.00, 'FAILED', 'Same account transfer', 'idem-006', NOW());


-- Sample rewards corresponding to successful eligible transactions
-- Alice (user_id=1) earned 2 points from ₹250 transfer to Bob
INSERT INTO rewards (id, user_id, transaction_id, reward_points, earned_on) VALUES
('reward-001', 1, 'tx-001', 2, NOW());

-- Alice (user_id=1) earned 1 point from ₹150 transfer to Charlie
INSERT INTO rewards (id, user_id, transaction_id, reward_points, earned_on) VALUES
('reward-002', 1, 'tx-002', 1, NOW());

-- Eve (user_id=5) earned 2 points from ₹200 transfer to Frank
INSERT INTO rewards (id, user_id, transaction_id, reward_points, earned_on) VALUES
('reward-003', 5, 'tx-003', 2, NOW());

-- Grace (user_id=7) earned 3 points from ₹350 transfer to Heidi
INSERT INTO rewards (id, user_id, transaction_id, reward_points, earned_on) VALUES
('reward-004', 7, 'tx-004', 3, NOW());

-- Judy (user_id=10) earned 5 points from ₹500 transfer to Alice
INSERT INTO rewards (id, user_id, transaction_id, reward_points, earned_on) VALUES
('reward-005', 10, 'tx-005', 5, NOW());
