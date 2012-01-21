CREATE TABLE IF NOT EXISTS items
(
  id serial4,
  "type" character varying(10) NOT NULL,
  provider character varying(20) NOT NULL,
  providerid character varying(20) NOT NULL,
  title character varying(100) NOT NULL,
  subtitle character varying(100),
  season integer,
  episode integer,
  releasedate date,
  releaseyear integer,
  plot character varying(2000),
  imdbid character varying(20),
  rating numeric(4,1),
  runtime integer,
  lastupdated timestamp without time zone NOT NULL,
  lasthit timestamp without time zone NOT NULL,
  lastchecked timestamp without time zone NOT NULL,
  "version" integer NOT NULL,
  poster bytea,
  background bytea,
  banner bytea,
  CONSTRAINT items_id PRIMARY KEY (id),
  CONSTRAINT items_providerkey UNIQUE ("type", provider, providerid)
);

CREATE TABLE IF NOT EXISTS identifiers
(
  surrogatename character varying(250) NOT NULL,
  "type" character varying(10) NOT NULL,
  provider character varying(20) NOT NULL,
  providerid character varying(20) NOT NULL,
  CONSTRAINT queries_surrogatename PRIMARY KEY (surrogatename)
)