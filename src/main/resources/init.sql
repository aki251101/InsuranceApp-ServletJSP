-- データベースとテーブルの作成
CREATE DATABASE IF NOT EXISTS insurance_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE insurance_app;

-- 既存データをクリア
DROP TABLE IF EXISTS accidents;
DROP TABLE IF EXISTS policies;

-- policiesテーブル
CREATE TABLE policies (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_number VARCHAR(30) UNIQUE NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    renewal_due_end_date DATE,
    renewed_at DATETIME,
    cancelled_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_end_date (end_date),
    INDEX idx_customer_name (customer_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- accidentsテーブル
CREATE TABLE accidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id BIGINT NOT NULL,
    occurred_at DATE NOT NULL,
    place VARCHAR(200),
    description TEXT,
    status VARCHAR(20) NOT NULL,
    last_contacted_at DATETIME,
    memo TEXT NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    FOREIGN KEY (policy_id) REFERENCES policies(id),
    INDEX idx_policy_id (policy_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初期データ投入（契約）
-- 明日満期（要注意）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, created_at, updated_at)
VALUES ('P-2024-0001', '山田太郎', DATE_SUB(CURDATE(), INTERVAL 364 DAY), DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'ACTIVE', NOW(), NOW());

-- 10日後満期（要注意）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, created_at, updated_at)
VALUES ('P-2024-0002', '佐藤花子', DATE_SUB(CURDATE(), INTERVAL 355 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'ACTIVE', NOW(), NOW());

-- 30日後満期（更新可能）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, created_at, updated_at)
VALUES ('P-2024-0003', '鈴木一郎', DATE_SUB(CURDATE(), INTERVAL 335 DAY), DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW());

-- 70日後満期（失効）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, created_at, updated_at)
VALUES ('P-2024-0004', '田中美咲', DATE_SUB(CURDATE(), INTERVAL 295 DAY), DATE_ADD(CURDATE(), INTERVAL 70 DAY), 'ACTIVE', NOW(), NOW());

-- 過去満期（失効）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, created_at, updated_at)
VALUES ('P-2023-0001', '高橋健一', DATE_SUB(CURDATE(), INTERVAL 395 DAY), DATE_SUB(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', NOW(), NOW());

-- 解約済み
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, cancelled_at, created_at, updated_at)
VALUES ('P-2024-0005', '伊藤優子', DATE_SUB(CURDATE(), INTERVAL 200 DAY), DATE_ADD(CURDATE(), INTERVAL 165 DAY), 'CANCELLED', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), NOW());

-- 早期更改済み（当年度）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, renewal_due_end_date, renewed_at, created_at, updated_at)
VALUES ('P-2023-0002', '渡辺次郎', '2023-06-01', DATE_ADD(CURDATE(), INTERVAL 200 DAY), 'ACTIVE', '2024-06-01', DATE_SUB(NOW(), INTERVAL 30 DAY), NOW(), NOW());

-- 通常更改済み（当年度）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, renewal_due_end_date, renewed_at, created_at, updated_at)
VALUES ('P-2023-0003', '中村和子', '2023-07-15', DATE_ADD(CURDATE(), INTERVAL 180 DAY), 'ACTIVE', '2024-07-15', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW(), NOW());

-- デモ用: 当月満期・早期更改達成済み（初期状態で分子/分母に寄与）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, renewal_due_end_date, renewed_at, created_at, updated_at)
VALUES (
    'P-2024-0101',
    '小林健太',
    DATE_ADD(
        CASE
            WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
            ELSE LAST_DAY(CURDATE())
        END,
        INTERVAL -1 YEAR
    ),
    DATE_ADD(
        CASE
            WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
            ELSE LAST_DAY(CURDATE())
        END,
        INTERVAL 1 YEAR
    ),
    'ACTIVE',
    CASE
        WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
        ELSE LAST_DAY(CURDATE())
    END,
    DATE_SUB(NOW(), INTERVAL 25 DAY),
    NOW(),
    NOW()
);

-- デモ用: 当月満期・早期更改未達成（更新済み、分母のみ増える）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, renewal_due_end_date, renewed_at, created_at, updated_at)
VALUES (
    'P-2024-0102',
    '松本理沙',
    DATE_ADD(
        CASE
            WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
            ELSE LAST_DAY(CURDATE())
        END,
        INTERVAL -1 YEAR
    ),
    DATE_ADD(
        CASE
            WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
            ELSE LAST_DAY(CURDATE())
        END,
        INTERVAL 1 YEAR
    ),
    'ACTIVE',
    CASE
        WHEN DAY(CURDATE()) <= 20 THEN DATE_ADD(CURDATE(), INTERVAL 10 DAY)
        ELSE LAST_DAY(CURDATE())
    END,
    DATE_SUB(NOW(), INTERVAL 1 DAY),
    NOW(),
    NOW()
);

-- デモ用: 前月満期・早期更改達成済み（当年度には入るが当月には入らない）
INSERT INTO policies (policy_number, customer_name, start_date, end_date, status, renewal_due_end_date, renewed_at, created_at, updated_at)
VALUES (
    'P-2024-0103',
    '井上大輔',
    DATE_SUB(CURDATE(), INTERVAL 385 DAY),
    DATE_ADD(CURDATE(), INTERVAL 345 DAY),
    'ACTIVE',
    DATE_SUB(CURDATE(), INTERVAL 20 DAY),
    DATE_SUB(CURDATE(), INTERVAL 40 DAY),
    NOW(),
    NOW()
);

-- 初期データ投入（事故）
-- 事故1: 滞留中（8日前に最終対応）
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
VALUES (1, DATE_SUB(CURDATE(), INTERVAL 15 DAY), '大阪市北区梅田1丁目', '追突事故', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 8 DAY), '初回連絡完了。保険会社へ報告済み。', NOW(), NOW());

-- 事故2: 対応中（昨日対応）
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
VALUES (2, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '京都市中京区', '自損事故', 'IN_PROGRESS', DATE_SUB(NOW(), INTERVAL 1 DAY), '修理工場と連絡調整中。', NOW(), NOW());

-- 事故3: 未対応（last_contacted_atがnull）
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
VALUES (3, CURDATE(), '神戸市中央区三宮町', '接触事故', 'OPEN', NULL, '', NOW(), NOW());

-- 事故4: 完了
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
VALUES (4, DATE_SUB(CURDATE(), INTERVAL 30 DAY), '大阪市天王寺区', '車両破損', 'RESOLVED', DATE_SUB(NOW(), INTERVAL 25 DAY), '示談成立。保険金支払い完了。', NOW(), NOW());

-- 事故5: 完了
INSERT INTO accidents (policy_id, occurred_at, place, description, status, last_contacted_at, memo, created_at, updated_at)
VALUES (1, DATE_SUB(CURDATE(), INTERVAL 60 DAY), '奈良市', '物損事故', 'RESOLVED', DATE_SUB(NOW(), INTERVAL 58 DAY), '相手方と示談成立。クローズ。', NOW(), NOW());
