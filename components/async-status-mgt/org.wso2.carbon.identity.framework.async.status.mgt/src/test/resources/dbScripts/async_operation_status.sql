CREATE TABLE IF NOT EXISTS IDN_ASYNC_OPERATION_STATUS (
	IDN_OPERATION_ID INTEGER NOT NULL AUTO_INCREMENT,
	IDN_OPERATION_TYPE CHARACTER VARYING(63) NOT NULL,
	IDN_OPERATION_SUBJECT_TYPE CHARACTER VARYING(63) NOT NULL,
	IDN_OPERATION_SUBJECT_ID CHARACTER VARYING(255) NOT NULL,
	IDN_OPERATION_INITIATED_ORG_ID CHARACTER VARYING(255) NOT NULL,
	IDN_OPERATION_INITIATED_USER_ID CHARACTER VARYING(255) NOT NULL,
	IDN_OPERATION_STATUS CHARACTER VARYING(127) NOT NULL,
	IDN_CREATED_TIME TIMESTAMP NOT NULL,
	IDN_LAST_MODIFIED TIMESTAMP NOT NULL,
	IDN_OPERATION_POLICY CHARACTER VARYING(127),
	CONSTRAINT IDN_OPERATION_PK PRIMARY KEY (IDN_OPERATION_ID)
);

CREATE TABLE IF NOT EXISTS IDN_ASYNC_OPERATION_STATUS_UNIT (
	IDN_UNIT_OPERATION_ID INTEGER NOT NULL AUTO_INCREMENT,
	IDN_OPERATION_ID INTEGER NOT NULL,
	IDN_RESIDENT_RESOURCE_ID CHARACTER VARYING(255) NOT NULL,
	IDN_TARGET_ORG_ID CHARACTER VARYING(255) NOT NULL,
	IDN_UNIT_OPERATION_STATUS CHARACTER VARYING(63) NOT NULL,
	IDN_OPERATION_STATUS_MESSAGE CHARACTER VARYING(127),
	IDN_CREATED_AT TIMESTAMP NOT NULL,
	CONSTRAINT IDN_UNIT_OPERATION_PK PRIMARY KEY (IDN_UNIT_OPERATION_ID),
	CONSTRAINT IDN_ASYNC_OPERATION_STATUS_UNIT_IDN_ASYNC_OPERATION_STATUS_FK FOREIGN KEY (IDN_OPERATION_ID) REFERENCES PUBLIC.IDN_ASYNC_OPERATION_STATUS(IDN_OPERATION_ID) ON DELETE CASCADE ON UPDATE RESTRICT
);