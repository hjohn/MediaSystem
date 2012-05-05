ALTER TABLE items ADD COLUMN viewed boolean NOT NULL DEFAULT FALSE;
ALTER TABLE items ADD COLUMN resumeposition integer NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN matchaccuracy float;

CREATE TABLE persons
(
  id serial4,
  name character varying(250) NOT NULL,
  photourl character varying(1000),
  photo bytea,
  
  CONSTRAINT persons_id PRIMARY KEY (id),
  CONSTRAINT persons_name UNIQUE (name)
);

CREATE TABLE castings
(
  persons_id int4 NOT NULL REFERENCES persons(id) ON DELETE RESTRICT,
  items_id int4 NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  role character varying(50) NOT NULL,
  charactername character varying(250),
  
  CONSTRAINT castings_pk PRIMARY KEY (persons_id, items_id)
);
