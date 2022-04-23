CREATE OR REPLACE PROCEDURE WSO2_CONFIRMATION_CODE_CLEANUP IS
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    systime TIMESTAMP := systimestamp;
    utcTime TIMESTAMP := sys_extract_utc(systimestamp);
    batchCount INT := 1000;
    chunkCount INT := 1000;
    rowCount INT   := 0;
    current_schema VARCHAR(20);
    cleanUpCodesTimeLimit TIMESTAMP;

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    batchSize INT        := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize INT        := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    enableLog BOOLEAN    := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]
    backupTables BOOLEAN := FALSE; -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE [DEFAULT : FALSE], WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    cleanUpCodesTimeLimitInHrs INT := 720; -- SET SAFE PERIOD OF HOURS FOR CODE DELETE [DEFAULT : 720 hrs (30 days)]. CODES OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.

BEGIN

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;
    cleanUpCodesTimeLimit := utcTime - cleanUpCodesTimeLimitInHrs/24;

    IF ( enableLog ) THEN
        SELECT COUNT(*) INTO rowCount FROM all_tables WHERE owner = current_schema AND table_name = UPPER('LOG_WSO2_CONFIRMATION_CODE_CLEANUP');
        IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_CONFIRMATION_CODE_CLEANUP';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP VARCHAR(250) , LOG VARCHAR(250)) NOLOGGING';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''LOG_WSO2_CONFIRMATION_CODE_CLEANUP STARTED .... !'')';
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- BACKUP TABLES
    -- ------------------------------------------
    IF (backupTables)
    THEN

        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES WHERE OWNER = current_schema AND table_name = 'BAK_IDN_RECOVERY_DATA';
        IF (rowCount = 1)
        THEN
            IF (enableLog)
            THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETING OLD BACKUP...'')';
            END IF;
            EXECUTE IMMEDIATE 'DROP TABLE BAK_IDN_RECOVERY_DATA';
            COMMIT;
        END if;

        EXECUTE IMMEDIATE 'CREATE TABLE BAK_IDN_RECOVERY_DATA AS (SELECT * FROM IDN_RECOVERY_DATA)';
        rowCount:= sql%rowcount;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (chunkCount > 0) LOOP
        -- CREATE CHUNK TABLE
        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES WHERE OWNER = current_schema AND table_name = 'IDN_RECOVERY_DATA_CHUNK_TMP';
        IF (rowCount = 1)
        THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_RECOVERY_DATA_CHUNK_TMP';
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE IDN_RECOVERY_DATA_CHUNK_TMP AS (SELECT CODE FROM IDN_RECOVERY_DATA WHERE ROWNUM <= '||chunkSize||')';
        chunkCount:= sql%rowcount;
        COMMIT;
        IF (enableLog)
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATED IDN_RECOVERY_DATA_CHUNK_TMP...'')';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        WHILE (batchCount > 0) LOOP
            -- CREATE BATCH TABLE
            SELECT COUNT(*) INTO rowCount FROM ALL_TABLES WHERE OWNER = current_schema AND table_name = 'IDN_RECOVERY_DATA_BATCH_TMP';
            IF (rowCount = 1)
            THEN
                EXECUTE IMMEDIATE 'DROP TABLE IDN_RECOVERY_DATA_BATCH_TMP';
            END IF;
            EXECUTE IMMEDIATE 'CREATE TABLE IDN_RECOVERY_DATA_BATCH_TMP AS (SELECT * FROM IDN_RECOVERY_DATA_CHUNK_TMP WHERE ROWNUM <= '||batchSize||')';
            batchCount:= sql%rowcount;
            COMMIT;
            IF (enableLog)
            THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CREATED IDN_RECOVERY_DATA_BATCH_TMP...'')';
            END IF;

            -- BATCH DELETION
            IF (enableLog)
            THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE STARTED ON IDN_RECOVERY_DATA...'')';
            END IF;
            EXECUTE IMMEDIATE 'DELETE FROM IDN_RECOVERY_DATA WHERE code IN (SELECT code FROM IDN_RECOVERY_DATA_BATCH_TMP where (:cleanUpCodesTimeLimit  > TIME_CREATED))'
            using  cleanUpCodesTimeLimit;
            rowCount:= sql%rowcount;
            COMMIT;
            IF (enableLog)
            THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''BATCH DELETE FINISHED ON IDN_RECOVERY_DATA : '||rowCount||''')';
            END IF;

            -- DELETE FROM CHUNK
            EXECUTE IMMEDIATE 'DELETE FROM IDN_RECOVERY_DATA_CHUNK_TMP WHERE CODE IN (SELECT CODE FROM IDN_RECOVERY_DATA_BATCH_TMP)';
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    EXECUTE IMMEDIATE 'DROP TABLE IDN_RECOVERY_DATA_BATCH_TMP';
    EXECUTE IMMEDIATE 'DROP TABLE IDN_RECOVERY_DATA_CHUNK_TMP';

    IF (enableLog)
    THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_CONFIRMATION_CODE_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP COMPLETED...!'')';
    END IF;

    COMMIT;
END;
