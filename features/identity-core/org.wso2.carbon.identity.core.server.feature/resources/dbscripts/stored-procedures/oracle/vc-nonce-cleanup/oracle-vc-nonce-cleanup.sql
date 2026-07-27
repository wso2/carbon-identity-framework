CREATE OR REPLACE PROCEDURE WSO2_VC_NONCE_CLEANUP IS

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    batchCount INT := 1000;
    chunkCount INT := 1000;
    rowCount INT := 0;
    current_schema VARCHAR2(30);
    cleanUpDateTimeLimit TIMESTAMP;
    cleanUpDateTimeLimitStr VARCHAR2(40);

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    batchSize INT := 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize INT := 500000;     -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    enableLog BOOLEAN := FALSE;  -- ENABLE LOGGING [DEFAULT : FALSE]
    backupTables BOOLEAN := FALSE; -- SET IF VC NONCE TABLE NEEDS TO BACKUP BEFORE DELETE [DEFAULT : FALSE]

BEGIN

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;
    cleanUpDateTimeLimit := sys_extract_utc(systimestamp);
    cleanUpDateTimeLimitStr := TO_CHAR(cleanUpDateTimeLimit, 'YYYY-MM-DD HH24:MI:SS.FF6');

    IF (enableLog) THEN
        SELECT COUNT(*) INTO rowCount FROM all_tables
        WHERE owner = current_schema AND table_name = UPPER('LOG_WSO2_VC_NONCE_CLEANUP');

        IF (rowCount = 1) THEN
            EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_VC_NONCE_CLEANUP';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP VARCHAR2(250), LOG VARCHAR2(250)) NOLOGGING';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                          || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                          || '''LOG_WSO2_VC_NONCE_CLEANUP STARTED .... !'')';
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- BACKUP TABLE
    -- ------------------------------------------
    IF (backupTables) THEN

        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND table_name = 'BAK_IDN_VC_NONCE';

        IF (rowCount = 1) THEN
            IF (enableLog) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                                  || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                                  || '''DELETING OLD BACKUP...'')';
            END IF;

            EXECUTE IMMEDIATE 'DROP TABLE BAK_IDN_VC_NONCE';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE BAK_IDN_VC_NONCE AS (SELECT * FROM IDN_VC_NONCE)';
        rowCount := SQL%rowcount;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (chunkCount > 0) LOOP

        -- CREATE CHUNK TABLE
        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND table_name = 'IDN_VC_NONCE_CHUNK_TMP';

        IF (rowCount = 1) THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_VC_NONCE_CHUNK_TMP';
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE IDN_VC_NONCE_CHUNK_TMP AS '
                          || '(SELECT TENANT_ID, NONCE FROM IDN_VC_NONCE '
                          || 'WHERE EXPIRY_TIME < TO_TIMESTAMP(''' || cleanUpDateTimeLimitStr || ''','
                          || '''YYYY-MM-DD HH24:MI:SS.FF6'') AND ROWNUM <= ' || chunkSize || ')';

        chunkCount := SQL%rowcount;
        COMMIT;

        EXIT WHEN chunkCount = 0;

        IF (enableLog) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                              || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                              || '''CREATED IDN_VC_NONCE_CHUNK_TMP...'')';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        WHILE (batchCount > 0) LOOP

            -- CREATE BATCH TABLE
            SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
            WHERE OWNER = current_schema AND table_name = 'IDN_VC_NONCE_BATCH_TMP';

            IF (rowCount = 1) THEN
                EXECUTE IMMEDIATE 'DROP TABLE IDN_VC_NONCE_BATCH_TMP';
            END IF;

            EXECUTE IMMEDIATE 'CREATE TABLE IDN_VC_NONCE_BATCH_TMP AS '
                              || '(SELECT TENANT_ID, NONCE FROM IDN_VC_NONCE_CHUNK_TMP '
                              || 'WHERE ROWNUM <= ' || batchSize || ')';

            batchCount := SQL%rowcount;
            COMMIT;

            EXIT WHEN batchCount = 0;

            IF (enableLog) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                                  || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                                  || '''CREATED IDN_VC_NONCE_BATCH_TMP...'')';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                                  || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                                  || '''BATCH DELETE STARTED ON IDN_VC_NONCE...'')';
            END IF;

            EXECUTE IMMEDIATE 'DELETE FROM IDN_VC_NONCE T '
                              || 'WHERE EXISTS (SELECT 1 FROM IDN_VC_NONCE_BATCH_TMP B '
                              || 'WHERE B.TENANT_ID = T.TENANT_ID AND B.NONCE = T.NONCE)';

            rowCount := SQL%rowcount;
            COMMIT;

            IF (enableLog) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                                  || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                                  || '''BATCH DELETE FINISHED ON IDN_VC_NONCE : ' || rowCount || ''')';
            END IF;

            -- DELETE FROM CHUNK
            EXECUTE IMMEDIATE 'DELETE FROM IDN_VC_NONCE_CHUNK_TMP T '
                              || 'WHERE EXISTS (SELECT 1 FROM IDN_VC_NONCE_BATCH_TMP B '
                              || 'WHERE B.TENANT_ID = T.TENANT_ID AND B.NONCE = T.NONCE)';
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
    WHERE OWNER = current_schema AND table_name = 'IDN_VC_NONCE_BATCH_TMP';

    IF (rowCount = 1) THEN
        EXECUTE IMMEDIATE 'DROP TABLE IDN_VC_NONCE_BATCH_TMP';
    END IF;

    SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
    WHERE OWNER = current_schema AND table_name = 'IDN_VC_NONCE_CHUNK_TMP';

    IF (rowCount = 1) THEN
        EXECUTE IMMEDIATE 'DROP TABLE IDN_VC_NONCE_CHUNK_TMP';
    END IF;

    IF (enableLog) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                          || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                          || '''CLEANUP COMPLETED...!'')';
    END IF;

    COMMIT;
END;
