CREATE TABLE images (
  url varchar(1000) NOT NULL,
  image bytea NOT NULL,
  
  CONSTRAINT images_url PRIMARY KEY (url)
);

INSERT INTO images SELECT backgroundurl AS url, background AS image FROM items WHERE backgroundurl IS NOT NULL AND background IS NOT NULL;
INSERT INTO images SELECT bannerurl AS url, banner AS image FROM items WHERE bannerurl IS NOT NULL AND banner IS NOT NULL;
INSERT INTO images SELECT posterurl AS url, poster AS image FROM items WHERE posterurl IS NOT NULL AND poster IS NOT NULL;

INSERT INTO images SELECT photourl AS url, photo AS image FROM persons WHERE photourl IS NOT NULL AND photo IS NOT NULL;

ALTER TABLE items DROP COLUMN background;
ALTER TABLE items DROP COLUMN banner;
ALTER TABLE items DROP COLUMN poster;

ALTER TABLE persons DROP COLUMN photo;
