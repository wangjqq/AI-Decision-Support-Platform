-- ============================================================
-- 一次性补丁：把历史数据里 status 误写为 1 的行改成 0
-- (旧 schema 用了 DEFAULT 1，与 MyBatis-Plus @TableLogic 不一致)
-- ============================================================
USE aidsp;

-- 安全检查：先看多少行需要修
SELECT 'before' AS phase, COUNT(*) AS cnt FROM company WHERE status = 1;

-- 把所有 status=1 改成 0
UPDATE company SET status = 0 WHERE status = 1;

SELECT 'after'  AS phase, COUNT(*) AS cnt FROM company WHERE status = 1;
SELECT 'visible' AS phase, COUNT(*) AS cnt FROM company WHERE status = 0;
