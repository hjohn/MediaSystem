CREATE TABLE settings (
  id serial4,
  
  system varchar(100) NOT NULL,
  persistlevel varchar(20) NOT NULL,
  key varchar(2000) NOT NULL,
  value varchar(2000) NOT NULL,

  lastupdated timestamp without time zone NOT NULL,
  
  CONSTRAINT settings_id PRIMARY KEY (id),
  CONSTRAINT settings_system_key UNIQUE (system, key)
);