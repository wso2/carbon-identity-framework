-- ------------------------------------------
-- This PROCEDURE will cleanup the IDN_ACTION table by removing the entries which are not referenced by IDP_AUTHENTICATOR_PROPERTY table.
-- Refer for more information: https://github.com/wso2/product-is/issues/21944
-- ------------------------------------------

CREATE OR REPLACE PROCEDURE WSO2_IDN_ACTION_CLEANUP

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
DECLARE SQLStatement VARCHAR(100);

-- ------------------------------------------
-- CONFIGURABLE VARIABLES
-- ------------------------------------------
SET batchSize    = 10000;
SET chunkSize    = 500000;
SET enableLog    = 0;
SET backupTables = 0;

SET rowCount = 0;
SET batchCount = 1;
SET chunkCount = 1;

IF (enableLog = 1) THEN
	CALL DBMS_OUTPUT.PUT_LINE('WSO2_IDN_ACTION_CLEANUP() STARTED...!');
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

    IF (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME = 'BAK_IDN_ACTION'))
    THEN
        IF (enableLog = 1)
        THEN
            CALL DBMS_OUTPUT.PUT_LINE('DELETING OLD BACKUP...');
        END IF;
        DROP TABLE BAK_IDN_ACTION;
    END IF;
    CREATE TABLE BAK_IDN_ACTION AS (SELECT * FROM IDN_ACTION) WITH DATA;
END IF;

-- ------------------------------------------
-- CLEANUP DATA
-- ------------------------------------------

WHILE (chunkCount > 0)
DO
	-- CREATE CHUNK TABLE
	DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP;
	CREATE TABLE IDN_ACTION_CHUNK_TMP AS (SELECT UUID FROM IDN_ACTION) WITH NO DATA;
	INSERT INTO IDN_ACTION_CHUNK_TMP
	SELECT UUID FROM IDN_ACTION
	    WHERE TYPE = 'AUTHENTICATION' AND UUID NOT IN (
	        SELECT PROPERTY_VALUE FROM IDP_AUTHENTICATOR_PROPERTY
	        WHERE PROPERTY_KEY = 'actionId')
	    LIMIT chunkSize;
	GET DIAGNOSTICS v_rowCount = ROW_COUNT;
	SET chunkCount = v_rowCount;
	CREATE INDEX IDN_ACTION_CHUNK_TMP_INDX on IDN_ACTION_CHUNK_TMP (UUID);

	IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_ACTION_CHUNK_TMP...');
    END IF;

    -- BATCH LOOP
    SET batchCount = 1;
    WHILE (batchCount > 0)
    DO
    	-- CREATE BATCH TABLE
        DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP;
        CREATE TABLE IDN_ACTION_BATCH_TMP(UUID CHAR(36));
        INSERT INTO IDN_ACTION_BATCH_TMP SELECT UUID FROM IDN_ACTION_CHUNK_TMP LIMIT batchSize;
        GET DIAGNOSTICS v_rowCount = ROW_COUNT;
        SET batchCount = v_rowCount;

        CREATE INDEX IDN_ACTION_BATCH_TMP on IDN_ACTION_BATCH_TMP (UUID);

        IF (enableLog = 1)
	    THEN
	    	CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_ACTION_BATCH_TMP...');
	    END IF;

	    -- BATCH DELETION
	    IF (enableLog = 1)
	    THEN
	    	CALL DBMS_OUTPUT.PUT_LINE('BATCH DELETE STARTED ON IDN_ACTION...');
	    END IF;
	    DELETE FROM IDN_ACTION WHERE UUID IN (SELECT UUID FROM IDN_ACTION_BATCH_TMP);
	    GET DIAGNOSTICS rowCount = ROW_COUNT;

	    -- DELETE FROM CHUNK
	    DELETE FROM IDN_ACTION_CHUNK_TMP WHERE UUID IN (SELECT UUID FROM IDN_ACTION_BATCH_TMP);
    END WHILE;
END WHILE;

-- DELETE TEMP TABLES
DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP;
DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP;

IF (enableLog = 1)
THEN
	CALL DBMS_OUTPUT.PUT_LINE('CLEANUP COMPLETED...!');
END IF;

END/
