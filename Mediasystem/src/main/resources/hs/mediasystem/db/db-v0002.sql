ALTER TABLE items ADD COLUMN viewed boolean NOT NULL DEFAULT FALSE;
ALTER TABLE items ADD COLUMN resumeposition integer NOT NULL DEFAULT 0;
ALTER TABLE items ADD COLUMN matchaccuracy float;

CREATE TABLE persons (
  id ${SerialType},
  name varchar(250) NOT NULL,
  photourl varchar(1000),
  photo ${BinaryType},
  
  CONSTRAINT persons_id PRIMARY KEY (id),
  CONSTRAINT persons_name UNIQUE (name)
);

CREATE TABLE castings (
  persons_id integer NOT NULL REFERENCES persons(id) ON DELETE RESTRICT,
  items_id integer NOT NULL REFERENCES items(id) ON DELETE CASCADE,
  role varchar(50) NOT NULL,
  charactername varchar(250),
  
  CONSTRAINT castings_pk PRIMARY KEY (persons_id, items_id)
);
