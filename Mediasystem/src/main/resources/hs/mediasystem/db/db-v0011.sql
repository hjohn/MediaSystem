ALTER TABLE mediadata ADD CONSTRAINT mediadata_check_identifier CHECK (("type" IS NULL AND provider IS NULL AND providerid IS NULL AND matchtype IS NULL AND matchaccuracy IS NULL) OR ("type" IS NOT NULL AND provider IS NOT NULL AND providerid IS NOT NULL AND matchtype IS NOT NULL AND matchaccuracy IS NOT NULL));
ALTER TABLE mediadata ALTER COLUMN "type" DROP NOT NULL;
ALTER TABLE mediadata ALTER COLUMN provider DROP NOT NULL;
ALTER TABLE mediadata ALTER COLUMN providerid DROP NOT NULL;
ALTER TABLE mediadata ALTER COLUMN matchtype DROP NOT NULL;
ALTER TABLE mediadata ALTER COLUMN matchaccuracy DROP NOT NULL;
ALTER TABLE mediadata ADD COLUMN lastupdated timestamp without time zone NOT NULL DEFAULT now();
