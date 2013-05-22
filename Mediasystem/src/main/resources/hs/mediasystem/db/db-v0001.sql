CREATE TABLE dbinfo (
  name varchar(50) NOT NULL,
  value varchar(50) NOT NULL,
  
  CONSTRAINT dbinfo_pk PRIMARY KEY (name)
);

INSERT INTO dbinfo (name, value) VALUES ('version', '0');

CREATE TABLE items (
  id ${SerialType},
  type varchar(10) NOT NULL,
  provider varchar(20) NOT NULL,
  providerid varchar(20) NOT NULL,
  
  title varchar(100) NOT NULL,
  subtitle varchar(100),
  season integer,
  episode integer,
  releasedate date,
  releaseyear integer,
  plot varchar(8000),
  imdbid varchar(20),
  rating real,
  runtime integer,
  genres varchar(100),
  language varchar(20),
  tagline varchar(200),
  backgroundurl varchar(1000),
  bannerurl varchar(1000),
  posterurl varchar(1000),
  
  background ${BinaryType},
  banner ${BinaryType},
  poster ${BinaryType},
  
  lastupdated timestamp NOT NULL,
  lasthit timestamp NOT NULL,
  lastchecked timestamp NOT NULL,
  version integer NOT NULL,
  
  CONSTRAINT items_id PRIMARY KEY (id),
  CONSTRAINT items_providerkey UNIQUE (type, provider, providerid)
);