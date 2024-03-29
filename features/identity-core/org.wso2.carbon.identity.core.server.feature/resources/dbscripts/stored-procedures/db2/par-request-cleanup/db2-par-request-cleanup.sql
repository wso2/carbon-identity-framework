CREATE OR REPLACE PROCEDURE WSO2_PAR_REQUEST_CLEANUP

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE batchSize INT;
DECLARE chunkSize INT;
DECLARE batchCount INT;
DECLARE chunkCount INT;
DECLARE rowCount INT;
DECLARE v_rowCount INT;
DECLARE enableLog SMALLINT;
DECLARE backupTables SMALLINT;
DECLARE cleanUpRequestsTimeLimit INT;
DECLARE cleanUpDateTimeLimit DATETIME;
DECLARE cleanUpDateTimeLimitInMillis BIGINT;
DECLARE SQLStatement VARCHAR(100);

-- ------------------------------------------
-- CONFIGURABLE VARIABLES
-- ------------------------------------------
SET batchSize    = 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
SET chunkSize    = 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
SET enableLog    = 0; -- ENABLE LOGGING [DEFAULT : 0]
SET backupTables = 0; -- SET IF OAUTH PAR TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : 0]. WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
SET cleanUpRequestsTimeLimit = 24;  -- SET SAFE PERIOD OF HOURS FOR REQUEST DELETE [DEFAULT : 24 hrs (1 day)]. REQUESTS OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.

SET rowCount = 0;
SET batchCount = 1;
SET chunkCount = 1;
SET cleanUpDateTimeLimit = CURRENT TIMESTAMP - cleanUpRequestsTimeLimit HOUR;
SET cleanUpDateTimeLimitInMillis = TIMESTAMP_TO_MILLIS(cleanUpDateTimeLimit);

IF (enableLog = 1) THEN
	CALL DBMS_OUTPUT.PUT_LINE('WSO2_PAR_REQUEST_CLEANUP() STARTED...!');
END IF;

-- ------------------------------------------
-- BACKUP DATA
-- ------------------------------------------

IF (backupTables = 1)
THEN
    IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('TABLE BACKUP STARTED...!');
    END IF;

    IF (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME = 'BAK_IDN_OAUTH_PAR'))
    THEN
        IF (enableLog = 1)
        THEN
            CALL DBMS_OUTPUT.PUT_LINE('DELETING OLD BACKUP...');
        END IF;
        DROP TABLE BAK_IDN_OAUTH_PAR;
    END IF;
    CREATE TABLE BAK_IDN_OAUTH_PAR AS (SELECT * FROM IDN_OAUTH_PAR) WITH DATA;
END IF;

-- ------------------------------------------
-- CLEANUP DATA
-- ------------------------------------------

WHILE (chunkCount > 0)
DO
	-- CREATE CHUNK TABLE
	DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP;
	CREATE TABLE IDN_OAUTH_PAR_CHUNK_TMP AS (SELECT REQ_URI_REF FROM IDN_OAUTH_PAR) WITH NO DATA;
	INSERT INTO IDN_OAUTH_PAR_CHUNK_TMP SELECT REQ_URI_REF FROM IDN_OAUTH_PAR WHERE cleanUpDateTimeLimitInMillis > SCHEDULED_EXPIRY LIMIT chunkSize;
	GET DIAGNOSTICS v_rowCount = ROW_COUNT;
	SET chunkCount = v_rowCount;
	CREATE INDEX IDN_OAUTH_PAR_CHUNK_TMP_INDX on IDN_OAUTH_PAR_CHUNK_TMP (REQ_URI_REF);

	IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_OAUTH_PAR_CHUNK_TMP...');
    END IF;

    -- BATCH LOOP
    SET batchCount = 1;
    WHILE (batchCount > 0)
    DO
    	-- CREATE BATCH TABLE
        DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP;
        CREATE TABLE IDN_OAUTH_PAR_BATCH_TMP(REQ_URI_REF VARCHAR (255));
        INSERT INTO IDN_OAUTH_PAR_BATCH_TMP SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_CHUNK_TMP LIMIT batchSize;
        GET DIAGNOSTICS v_rowCount = ROW_COUNT;
        SET batchCount = v_rowCount;

        CREATE INDEX IDN_OAUTH_PAR_BATCH_TMP on IDN_OAUTH_PAR_BATCH_TMP (REQ_URI_REF);

        IF (enableLog = 1)
	    THEN
	    	CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_OAUTH_PAR_BATCH_TMP...');
	    END IF;

	    -- BATCH DELETION
	    IF (enableLog = 1)
	    THEN
	    	CALL DBMS_OUTPUT.PUT_LINE('BATCH DELETE STARTED ON IDN_OAUTH_PAR...');
	    END IF;
	    DELETE FROM IDN_OAUTH_PAR WHERE REQ_URI_REF IN (SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_BATCH_TMP);
	    GET DIAGNOSTICS rowCount = ROW_COUNT;

	    -- DELETE FROM CHUNK
	    DELETE FROM IDN_OAUTH_PAR_CHUNK_TMP WHERE REQ_URI_REF IN (SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_BATCH_TMP);
    END WHILE;
END WHILE;

-- DELETE TEMP TABLES
DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP;
DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP;

IF (enableLog = 1)
THEN
	CALL DBMS_OUTPUT.PUT_LINE('CLEANUP COMPLETED...!');
END IF;

END/
