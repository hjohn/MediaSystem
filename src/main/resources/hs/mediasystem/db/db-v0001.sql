CREATE TABLE dbinfo (
  name varchar(50) NOT NULL,
  value varchar(50) NOT NULL,
  
  CONSTRAINT dbinfo_pk PRIMARY KEY (name)
);

INSERT INTO dbinfo (name, value) VALUES ('version', 0);

CREATE TABLE items (
  id serial4,
  "type" varchar(10) NOT NULL,
  provider varchar(20) NOT NULL,
  providerid varchar(20) NOT NULL,
  
  title varchar(100) NOT NULL,
  subtitle varchar(100),
  season integer,
  episode integer,
  releasedate date,
  releaseyear integer,
  plot varchar(2000),
  imdbid varchar(20),
  rating numeric(4,1),
  runtime integer,
  genres varchar(100),
  language varchar(20),
  tagline varchar(200),
  backgroundurl varchar(1000),
  bannerurl varchar(1000),
  posterurl varchar(1000),
  
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