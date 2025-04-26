-- 为article表添加cover_image列
ALTER TABLE article 
ADD COLUMN `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '文章封面图URL'; 