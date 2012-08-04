ALTER TABLE mediadata ADD CONSTRAINT mediadata_check_identifier CHECK ((type IS NULL AND provider IS NULL AND providerid IS NULL AND matchtype IS NULL AND matchaccuracy IS NULL) OR (type IS NOT NULL AND provider IS NOT NULL AND providerid IS NOT NULL AND matchtype IS NOT NULL AND matchaccuracy IS NOT NULL));
ALTER TABLE mediadata ALTER COLUMN type ${DropNotNull};
ALTER TABLE mediadata ALTER COLUMN provider ${DropNotNull};
ALTER TABLE mediadata ALTER COLUMN providerid ${DropNotNull};
ALTER TABLE mediadata ALTER COLUMN matchtype ${DropNotNull};
ALTER TABLE mediadata ALTER COLUMN matchaccuracy ${DropNotNull};
ALTER TABLE mediadata ADD COLUMN lastupdated timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP;
