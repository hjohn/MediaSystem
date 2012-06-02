ALTER TABLE castings DROP CONSTRAINT castings_pk;
ALTER TABLE castings ADD CONSTRAINT castings_pk PRIMARY KEY (persons_id, items_id, role);
