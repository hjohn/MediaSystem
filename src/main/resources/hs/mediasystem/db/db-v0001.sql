CREATE TABLE IF NOT EXISTS dbinfo
(
  name character varying(50) NOT NULL,
  value character varying(50) NOT NULL,
  
  CONSTRAINT dbinfo_pk PRIMARY KEY (name)
);

INSERT INTO dbinfo (name, value) VALUES ('version', 0);

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
  genres character varying(100),
  language character varying(20),
  tagline character varying(200),
  backgroundurl character varying(1000),
  bannerurl character varying(1000),
  posterurl character varying(1000),
  
  background bytea,
  banner bytea,
  poster bytea,
  
  lastupdated timestamp without time zone NOT NULL,
  lasthit timestamp without time zone NOT NULL,
  lastchecked timestamp without time zone NOT NULL,
  "version" integer NOT NULL,
  
  CONSTRAINT items_id PRIMARY KEY (id),
  CONSTRAINT items_providerkey UNIQUE ("type", provider, providerid)
);

CREATE TABLE IF NOT EXISTS identifiers
(
  surrogatename character varying(250) NOT NULL,
  "type" character varying(10) NOT NULL,
  provider character varying(20) NOT NULL,
  providerid character varying(20) NOT NULL,
  
  CONSTRAINT identifiers_surrogatename PRIMARY KEY (surrogatename)
);
