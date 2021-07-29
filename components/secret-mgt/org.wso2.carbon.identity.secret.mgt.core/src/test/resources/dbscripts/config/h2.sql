-- -----------------------------------------------------
-- Table IDN_SECRET
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS IDN_SECRET (
  ID            VARCHAR(255) NOT NULL,
  TENANT_ID     INT          NOT NULL,
  NAME          VARCHAR(255) NOT NULL,
  VALUE          VARCHAR(255) NOT NULL,
  CREATED_TIME  TIMESTAMP    NOT NULL,
  LAST_MODIFIED TIMESTAMP    NOT NULL,
  UNIQUE (NAME, TENANT_ID),
  PRIMARY KEY (ID)
);
