-- 为routes表添加is_draft字段和tips字段（如果不存在）
-- 如果字段已存在，这些语句会失败，可以忽略

ALTER TABLE routes ADD COLUMN IF NOT EXISTS tips TEXT DEFAULT NULL COMMENT '出行提示';
ALTER TABLE routes ADD COLUMN IF NOT EXISTS is_draft TINYINT(1) DEFAULT 0 COMMENT '是否为草稿：0-已发布，1-草稿';
ALTER TABLE routes ADD INDEX IF NOT EXISTS idx_is_draft (is_draft);

