-- 为 routes 表添加缺失的 tips 字段
ALTER TABLE routes ADD COLUMN tips TEXT DEFAULT NULL COMMENT '出行提示' AFTER budget;

-- 检查是否成功
DESCRIBE routes;
