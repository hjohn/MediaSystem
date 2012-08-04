CREATE TABLE mediadata (
  id ${SerialType},
  
  type varchar(10) NOT NULL,
  provider varchar(20) NOT NULL,
  providerid varchar(20) NOT NULL,
  
  uri varchar(2000) NOT NULL,
  
  filelength bigint NOT NULL,
  filetime bigint NOT NULL,
  hash ${Sha256Type},
  oshash bigint,
  
  matchtype varchar(20) NOT NULL,
  matchaccuracy real NOT NULL,
  resumeposition integer NOT NULL,
  viewed boolean NOT NULL,
  
  CONSTRAINT mediadata_id PRIMARY KEY (id),
  CONSTRAINT mediadata_hash UNIQUE (hash),
  CONSTRAINT mediadata_uri UNIQUE (uri)
);
