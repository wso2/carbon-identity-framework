
CREATE OR REPLACE FUNCTION TOKEN_CLEANUP_SP() RETURNS void LANGUAGE plpgsql AS $$

DECLARE
  batchSize int;
  cursorLimit int;
  backupTables int;
  sleepTime float;
  safePeriod int;
  rowCount int;
  enableLog boolean;
  logLevel VARCHAR(10);
  enableAudit int;
  deleteTillTime timestamp;
  timeIncrementFromGMT float;

BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
  batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
  backupTables := 1;    -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE     [DEFAULT : TRUE]
  sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
  safePeriod := 2; -- SET SAFE PERIOD OF HOURS FOR TOKEN DELETE, SINCE TOKENS COULD BE CASHED    [DEFAULT : 2]
  rowCount := 0;
  enableLog := true; -- ENABLE LOGGING [DEFAULT : FALSE]
  logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG
  enableAudit := 1;  -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED TOKENS USING A TABLE    [DEFAULT : TRUE]
  deleteTillTime := CURRENT_timestamp - interval '1hour' * safePeriod;
  timeIncrementFromGMT := 5.30; --Time difference from GMT time to the current time zone.

  -- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_ACCESS_TOKEN TABLE
-- ------------------------------------------------------

  IF (backupTables = 1)
  THEN
    IF (EXISTS (SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'idn_oauth2_access_token_bak' and TABLE_SCHEMA ='public'))
    THEN
        DROP TABLE IDN_OAUTH2_ACCESS_TOKEN_BAK;
    END IF;

    CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_BAK as SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN;

    -- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_AUTHORIZATION_CODE TABLE
-- ------------------------------------------------------

  	IF (EXISTS (SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'idn_oauth2_authorization_code_bak' and TABLE_SCHEMA ='public'))
    THEN
        DROP TABLE IDN_OAUTH2_AUTHORIZATION_CODE_BAK;
    END IF;

    CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE_BAK as SELECT * FROM IDN_OAUTH2_AUTHORIZATION_CODE;

  END IF;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING IDN_OAUTH2_ACCESS_TOKEN_CLEANUP_AUDITLOG a nd IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP_AUDITLOGFOR DELETING
--TOKENS and authorization codes
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (enableAudit)
THEN
    IF (NOT EXISTS (SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'idn_oauth2_access_token_cleanup_auditlog' and TABLE_SCHEMA ='public'))
    THEN
          CREATE TABLE IDN_OAUTH2_ACCESS_TOKEN_CLEANUP_AUDITLOG as SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN WHERE 1 = 2;
    END IF;

        IF (NOT EXISTS (SELECT * FROM information_schema.TABLES WHERE TABLE_NAME = 'idn_oauth2_authorization_code_cleanup_auditlog' and TABLE_SCHEMA ='public'))
    THEN
          CREATE TABLE IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP_AUDITLOG as SELECT * FROM IDN_OAUTH2_AUTHORIZATION_CODE WHERE 1 = 2;
    END IF;

END IF;

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN
-- ------------------------------------------------------

IF (enableAudit)
THEN
  INSERT INTO IDN_OAUTH2_ACCESS_TOKEN_CLEANUP_AUDITLOG SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN WHERE TOKEN_STATE IN
  ('EXPIRED','INACTIVE','REVOKED') OR (TOKEN_STATE='ACTIVE' AND (deleteTillTime > TIME_CREATED + interval '1minute' *
   ((VALIDITY_PERIOD/1000)/60) + interval '1hour' * timeIncrementFromGMT) AND (deleteTillTime > REFRESH_TOKEN_TIME_CREATED + INTERVAL
    '1minute' * ((REFRESH_TOKEN_VALIDITY_PERIOD/1000)/60) + interval '1hour' * timeIncrementFromGMT));
END IF;

LOOP
IF rowCount > 0
THEN
    perform pg_sleep(sleepTime);
END IF;
DELETE FROM IDN_OAUTH2_ACCESS_TOKEN WHERE TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR (TOKEN_STATE='ACTIVE'
AND (deleteTillTime > TIME_CREATED + interval '1minute' * ((VALIDITY_PERIOD/1000)/60) + interval '1hour' *
timeIncrementFromGMT) AND (deleteTillTime > REFRESH_TOKEN_TIME_CREATED + interval '1minute' * (
(REFRESH_TOKEN_VALIDITY_PERIOD/1000)/60) + interval '1hour' * timeIncrementFromGMT));
GET diagnostics rowCount := ROW_COUNT;
exit when rowCount=0;
end loop;

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE
-- ------------------------------------------------------

IF (enableAudit)
THEN
  INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP_AUDITLOG  SELECT * FROM IDN_OAUTH2_AUTHORIZATION_CODE acode WHERE
   NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN tok WHERE tok.TOKEN_ID = acode.TOKEN_ID) OR STATE NOT IN
   ('ACTIVE') OR deleteTillTime > (TIME_CREATED + INTERVAL '1minute' * ((VALIDITY_PERIOD/1000)/60) + interval '1hour' * timeIncrementFromGMT) OR TOKEN_ID IS
    NULL;
  INSERT INTO IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP_AUDITLOG  SELECT * FROM IDN_OAUTH2_AUTHORIZATION_CODE where
  CODE_ID in ( SELECT * FROM ( select CODE_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS ( SELECT *
  FROM IDN_OAUTH2_ACCESS_TOKEN token WHERE token.TOKEN_ID = code.TOKEN_ID AND token.TOKEN_STATE = 'ACTIVE') AND code
  .STATE NOT IN ( 'ACTIVE' ) ) as x) OR  deleteTillTime > ( TIME_CREATED + INTERVAL '1minute' * (( VALIDITY_PERIOD / 1000 )/ 60 ) + interval '1hour' * timeIncrementFromGMT );

END IF;

LOOP
IF rowCount > 0
THEN
    perform pg_sleep(sleepTime);
END IF;
    DELETE FROM IDN_OAUTH2_AUTHORIZATION_CODE where CODE_ID in ( SELECT * FROM ( select CODE_ID FROM
    IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS ( SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN token WHERE token
    .TOKEN_ID = code.TOKEN_ID AND token.TOKEN_STATE = 'ACTIVE') AND code.STATE NOT IN ( 'ACTIVE' ) ) as x) OR deleteTillTime > ( TIME_CREATED + INTERVAL '1minute' * (( VALIDITY_PERIOD / 1000 )/ 60 ) + interval '1hour' * timeIncrementFromGMT);
GET diagnostics rowCount := ROW_COUNT;
exit when rowCount=0;
end loop;

END
$$;
