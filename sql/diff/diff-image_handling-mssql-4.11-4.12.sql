
DELETE FROM images_cache WHERE meta_id > 0;
ALTER TABLE images_cache DROP CONSTRAINT images_cache_pk;
ALTER TABLE images_cache DROP COLUMN meta_id;
ALTER TABLE images_cache DROP COLUMN image_index;

ALTER TABLE images_cache ADD CONSTRAINT images_cache_pk PRIMARY KEY (id);
