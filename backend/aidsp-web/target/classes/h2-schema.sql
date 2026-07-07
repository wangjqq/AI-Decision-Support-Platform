-- =========================================================
-- H2 dev schema (MySQL 兼容模式)
-- 仅 Company 模块所需的最小表结构，用于本地冒烟测试
-- =========================================================
DROP TABLE IF EXISTS company;

CREATE TABLE company (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(128)   NOT NULL,
    code             VARCHAR(16),
    uscc             VARCHAR(18),
    industry_id      BIGINT         NOT NULL,
    industry_name    VARCHAR(64),
    industry         VARCHAR(64),
    main_business    CLOB,
    business         CLOB,
    address          VARCHAR(255),
    founded_at       DATE,
    description      CLOB,
    revenue          DECIMAL(20,2),
    profit           DECIMAL(20,2),
    financial_period VARCHAR(16),
    status           INT            NOT NULL DEFAULT 0,
    created_at       TIMESTAMP      NOT NULL,
    updated_at       TIMESTAMP      NOT NULL
);

CREATE UNIQUE INDEX uk_company_uscc ON company (uscc);
CREATE UNIQUE INDEX uk_company_code ON company (code);
CREATE INDEX idx_company_industry_id ON company (industry_id);
CREATE INDEX idx_company_name ON company (name);
