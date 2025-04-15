CREATE TABLE IF NOT EXISTS IDN_ASYNC_OPERATION_STATUS (
	ID INTEGER NOT NULL AUTO_INCREMENT,
    OPERATION_ID CHARACTER(36) NOT NULL,
    CORRELATION_ID CHARACTER(36) NOT NULL,
    OPERATION_TYPE CHARACTER VARYING(63) NOT NULL,
    SUBJECT_TYPE CHARACTER VARYING(63) NOT NULL,
    SUBJECT_ID CHARACTER VARYING(255) NOT NULL,
    INITIATED_ORG_ID CHARACTER VARYING(255) NOT NULL,
    INITIATED_USER_ID CHARACTER VARYING(255) NOT NULL,
    STATUS CHARACTER VARYING(127) NOT NULL,
    CREATED_AT TIMESTAMP NOT NULL,
    LAST_MODIFIED TIMESTAMP NOT NULL,
    POLICY CHARACTER VARYING(127),
    CONSTRAINT IDN_OPERATION_PK PRIMARY KEY (ID)
);

CREATE TABLE IF NOT EXISTS IDN_ASYNC_OPERATION_STATUS_UNIT (
	ID INTEGER NOT NULL AUTO_INCREMENT,
    UNIT_OPERATION_ID CHARACTER(36) NOT NULL,
    OPERATION_ID INTEGER NOT NULL,
    RESIDENT_RESOURCE_ID CHARACTER VARYING(255) NOT NULL,
    TARGET_ORG_ID CHARACTER VARYING(255) NOT NULL,
    STATUS CHARACTER VARYING(63) NOT NULL,
    STATUS_MESSAGE CHARACTER VARYING(127),
    CREATED_AT TIMESTAMP NOT NULL,
    CONSTRAINT IDN_UNIT_OPERATION_PK PRIMARY KEY (ID),
    CONSTRAINT IDN_ASYNC_OPERATION_STATUS_UNIT_FK FOREIGN KEY (OPERATION_ID) REFERENCES PUBLIC.IDN_ASYNC_OPERATION_STATUS(ID) ON DELETE CASCADE ON UPDATE RESTRICT
);