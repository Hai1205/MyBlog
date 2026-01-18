-- Rename image columns to thumbnail columns in blogs table
ALTER TABLE blogs 
CHANGE COLUMN image_url thumbnail_url VARCHAR(255),
CHANGE COLUMN image_public_id thumbnail_public_id VARCHAR(255);
