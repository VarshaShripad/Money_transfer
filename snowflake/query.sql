CREATE OR REPLACE WAREHOUSE COMPUTE_WH
WITH WAREHOUSE_SIZE = 'X-SMALL'
AUTO_SUSPEND = 60
AUTO_RESUME = TRUE;

CREATE OR REPLACE DATABASE MONEY_TRANSFER_DW;

CREATE OR REPLACE SCHEMA MONEY_TRANSFER_DW.ANALYTICS;

USE WAREHOUSE COMPUTE_WH;
USE DATABASE MONEY_TRANSFER_DW;
USE SCHEMA ANALYTICS;

CREATE OR REPLACE TABLE DIM_ACCOUNT (
    account_key NUMBER PRIMARY KEY,
    account_id VARCHAR(50) NOT NULL,
    holder_name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    effective_date DATE NOT NULL
);

CREATE OR REPLACE TABLE ANALYTICS.DIM_DATE (
    date_key NUMBER PRIMARY KEY,
    full_date DATE NOT NULL,
    day INT,
    month INT,
    year INT,
    quarter INT
);

CREATE OR REPLACE TABLE ANALYTICS.FACT_TRANSACTIONS (
    transaction_key NUMBER PRIMARY KEY,
    account_from_key NUMBER REFERENCES ANALYTICS.DIM_ACCOUNT(account_key),
    account_to_key NUMBER REFERENCES ANALYTICS.DIM_ACCOUNT(account_key),
    date_key NUMBER REFERENCES ANALYTICS.DIM_DATE(date_key),
    amount DECIMAL(18,2),
    status VARCHAR(50)
);




CREATE OR REPLACE FILE FORMAT CSV_FMT
TYPE = CSV
FIELD_OPTIONALLY_ENCLOSED_BY = '"'
SKIP_HEADER = 0;

CREATE OR REPLACE TABLE DIM_ACCOUNT_STAGE (
  account_key NUMBER,
  holder_name STRING,
  status STRING
);


INSERT INTO DIM_ACCOUNT
SELECT
  account_key,
  'ACC' || account_key,
  holder_name,
  status,
  CURRENT_DATE()
FROM DIM_ACCOUNT_STAGE;


CREATE OR REPLACE TABLE TX_RAW (
  from_key STRING,
  to_key STRING,
  amount STRING,
  status STRING,
  created_ts TIMESTAMP
);


INSERT INTO FACT_TRANSACTIONS
(transaction_key,
 account_from_key,
 account_to_key,
 date_key,
 amount,
 status)
SELECT
  ROW_NUMBER() OVER (ORDER BY created_ts),
  TO_NUMBER(from_key),
  TO_NUMBER(to_key),
  TO_NUMBER(TO_CHAR(TO_DATE(created_ts), 'YYYYMMDD')),
  TO_NUMBER(amount),
  status
FROM TX_RAW;

SELECT * FROM FACT_TRANSACTIONS;


CREATE OR REPLACE TABLE DIM_DATE (
    date_key NUMBER PRIMARY KEY,
    full_date DATE,
    day NUMBER,
    month NUMBER,
    year NUMBER,
    quarter NUMBER
);


INSERT INTO DIM_DATE
SELECT DISTINCT
    date_key,
    TO_DATE(TO_VARCHAR(date_key), 'YYYYMMDD') AS full_date,
    DAY(TO_DATE(TO_VARCHAR(date_key), 'YYYYMMDD')) AS day,
    MONTH(TO_DATE(TO_VARCHAR(date_key), 'YYYYMMDD')) AS month,
    YEAR(TO_DATE(TO_VARCHAR(date_key), 'YYYYMMDD')) AS year,
    QUARTER(TO_DATE(TO_VARCHAR(date_key), 'YYYYMMDD')) AS quarter
FROM FACT_TRANSACTIONS;


SELECT * FROM TX_RAW;
SELECT * FROM DIM_DATE;
SELECT * FROM FACT_TRANSACTIONS;

//Daily Transaction volume
SELECT
    d.full_date,
    COUNT(*) AS total_transactions,
    SUM(f.amount) AS total_amount
FROM FACT_TRANSACTIONS f
JOIN DIM_DATE d ON f.date_key = d.date_key
GROUP BY d.full_date
ORDER BY d.full_date;


//Most active accounts
SELECT
    a.holder_name,
    COUNT(*) AS tx_count
FROM FACT_TRANSACTIONS f
JOIN DIM_ACCOUNT a
  ON a.account_id = 'ACC' || TO_VARCHAR(f.account_from_key)
GROUP BY a.holder_name
ORDER BY tx_count DESC;


//Success rate of transactions
SELECT
  ROUND(
    100 * SUM(CASE WHEN status='SUCCESS' THEN 1 ELSE 0 END)
    / COUNT(*),
    2
  ) AS success_rate_percent
FROM FACT_TRANSACTIONS;


//Peak hours
SELECT
    d.full_date,
    COUNT(*) AS tx_count
FROM FACT_TRANSACTIONS f
JOIN DIM_DATE d ON f.date_key = d.date_key
GROUP BY d.full_date
ORDER BY tx_count DESC;


//Average transfer amount
SELECT AVG(amount) AS avg_amount
FROM FACT_TRANSACTIONS;

MONEY_TRANSFER_DW.ANALYTICS.FACT_TRANSACTIONS
CREATE OR REPLACE VIEW VW_ACCOUNT_ACTIVITY AS
SELECT
  a.holder_name,
  COUNT(*) tx_count,
  SUM(amount) total_amount
FROM FACT_TRANSACTIONS f
JOIN DIM_ACCOUNT a
ON f.account_from_key = a.account_key
GROUP BY a.holder_name;


-- Check new transactions
SELECT *
FROM FACT_TRANSACTIONS
ORDER BY transaction_key DESC
LIMIT 10;

SELECT *
FROM DIM_DATE
ORDER BY full_date DESC
LIMIT 10;

SELECT *
FROM TX_RAW
ORDER BY created_ts DESC
LIMIT 10;


--works
INSERT INTO FACT_TRANSACTIONS (
    transaction_key,
    account_from_key,
    account_to_key,
    date_key,
    amount,
    status
)
SELECT
    COALESCE((SELECT MAX(transaction_key) FROM FACT_TRANSACTIONS), 0)
        + ROW_NUMBER() OVER (ORDER BY t.created_ts) AS transaction_key,
    TO_NUMBER(t.from_key) AS account_from_key,
    TO_NUMBER(t.to_key) AS account_to_key,
    TO_NUMBER(TO_CHAR(CONVERT_TIMEZONE('UTC', 'Asia/Kolkata', t.created_ts), 'YYYYMMDD')) AS date_key,
    TO_NUMBER(t.amount) AS amount,
    t.status
FROM TX_RAW t
WHERE NOT EXISTS (
    SELECT 1
    FROM FACT_TRANSACTIONS f
    WHERE f.account_from_key = TO_NUMBER(t.from_key)
      AND f.account_to_key = TO_NUMBER(t.to_key)
      AND f.amount = TO_NUMBER(t.amount)
      AND f.date_key = TO_NUMBER(TO_CHAR(CONVERT_TIMEZONE('UTC', 'Asia/Kolkata', t.created_ts), 'YYYYMMDD'))
      -- AND f.transaction_timestamp = t.created_ts  -- newly added
);



SELECT * FROM TX_RAW;
SELECT * FROM FACT_TRANSACTIONS;


SELECT CONVERT_TIMEZONE('UTC', 'Asia/Kolkata', created_ts) AS created_ts_in_local_time
FROM ANALYTICS.TX_RAW
ORDER BY created_ts DESC
LIMIT 10;


SELECT 
    from_key,
    to_key,
    amount,
    status,
    CONVERT_TIMEZONE('UTC', 'Asia/Kolkata', created_ts) AS created_ts_in_local_time
FROM ANALYTICS.TX_RAW
ORDER BY created_ts DESC
LIMIT 10;



INSERT INTO DIM_DATE (
    date_key,
    full_date,
    day,
    month,
    year,
    quarter
)
SELECT DISTINCT
    f.date_key,
    TO_DATE(TO_VARCHAR(f.date_key), 'YYYYMMDD'),
    DAY(TO_DATE(TO_VARCHAR(f.date_key), 'YYYYMMDD')),
    MONTH(TO_DATE(TO_VARCHAR(f.date_key), 'YYYYMMDD')),
    YEAR(TO_DATE(TO_VARCHAR(f.date_key), 'YYYYMMDD')),
    QUARTER(TO_DATE(TO_VARCHAR(f.date_key), 'YYYYMMDD'))
FROM FACT_TRANSACTIONS f
LEFT JOIN DIM_DATE d
    ON f.date_key = d.date_key
WHERE d.date_key IS NULL;



