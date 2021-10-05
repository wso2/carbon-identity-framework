-- -----------------------------------------------------
-- Table IDN_SECRET
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS IDN_SECRET (
  ID            VARCHAR(255) NOT NULL,
  TENANT_ID     INT          NOT NULL,
  SECRET_NAME          VARCHAR(255) NOT NULL,
  SECRET_VALUE          VARCHAR(4096) NOT NULL,
  CREATED_TIME  TIMESTAMP    NOT NULL,
  LAST_MODIFIED TIMESTAMP    NOT NULL,
  TYPE_ID       VARCHAR(255) NOT NULL,
  DESCRIPTION VARCHAR(1023) NULL,
  UNIQUE (SECRET_NAME, TENANT_ID, TYPE_ID),
  PRIMARY KEY (ID)
);
