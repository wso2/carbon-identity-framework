CREATE OR REPLACE PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP IS
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    systime TIMESTAMP := SYSTIMESTAMP;
    utcTime TIMESTAMP := SYS_EXTRACT_UTC(SYSTIMESTAMP);
    unixEpoch TIMESTAMP := TO_TIMESTAMP('1970-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS');
    batchCount INT := 1000;
    chunkCount INT := 1000;
    rowCount INT := 0;
    current_schema VARCHAR2(30);
    cleanUpTimeLimit TIMESTAMP;

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    batchSize INT := 10000;
    chunkSize INT := 500000;
    enableLog BOOLEAN := FALSE;
    backupTables BOOLEAN := FALSE;
    cleanUpTimeLimitInHrs INT := 24;

BEGIN
    SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;
    cleanUpTimeLimit := utcTime - cleanUpTimeLimitInHrs / 24;

    -- ------------------------------------------
    -- LOG TABLE INIT
    -- ------------------------------------------
    IF enableLog THEN
        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND TABLE_NAME = 'LOG_WSO2_FLOW_CONTEXT_CLEANUP';

        IF rowCount = 1 THEN
            EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_FLOW_CONTEXT_CLEANUP PURGE';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_FLOW_CONTEXT_CLEANUP (LOG_TIME VARCHAR2(250),LOG VARCHAR2(250)) NOLOGGING';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP (LOG_TIME, LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''WSO2_IDN_FLOW_CONTEXT_CLEANUP STARTED .... !'')';

        COMMIT;
    END IF;

    -- ------------------------------------------
    -- BACKUP TABLE
    -- ------------------------------------------
    IF backupTables THEN
        IF enableLog THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUPVALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''TABLE BACKUP STARTED ... !'')';
        END IF;

        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND TABLE_NAME = 'BAK_IDN_FLOW_CONTEXT_STORE';

        IF rowCount = 1 THEN
            IF enableLog THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''DELETING OLD BACKUP...'')';
            END IF;
            EXECUTE IMMEDIATE 'DROP TABLE BAK_IDN_FLOW_CONTEXT_STORE PURGE';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE BAK_IDN_FLOW_CONTEXT_STORE AS SELECT * FROM IDN_FLOW_CONTEXT_STORE';
        rowCount := SQL%ROWCOUNT;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE chunkCount > 0 LOOP
        -- DROP & CREATE CHUNK TEMP TABLE
        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND TABLE_NAME = 'IDN_FLOW_CONTEXT_CHUNK_TMP';

        IF rowCount = 1 THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_FLOW_CONTEXT_CHUNK_TMP PURGE';
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE IDN_FLOW_CONTEXT_CHUNK_TMP AS SELECT ID FROM IDN_FLOW_CONTEXT_STORE WHERE EXPIRES_AT < TO_TIMESTAMP(''' || TO_CHAR(cleanUpTimeLimit, 'YYYY-MM-DD HH24:MI:SS') || ''', ''YYYY-MM-DD HH24:MI:SS'') AND ROWNUM <= ' || chunkSize;

        chunkCount := SQL%ROWCOUNT;
        COMMIT;

        IF chunkCount = 0 THEN
            EXIT;
        END IF;

        IF enableLog THEN
            EXECUTE IMMEDIATE '
                INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP
                VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CREATED IDN_FLOW_CONTEXT_CHUNK_TMP...'')';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        WHILE batchCount > 0 LOOP
            SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
            WHERE OWNER = current_schema AND TABLE_NAME = 'IDN_FLOW_CONTEXT_BATCH_TMP';

            IF rowCount = 1 THEN
                EXECUTE IMMEDIATE 'DROP TABLE IDN_FLOW_CONTEXT_BATCH_TMP PURGE';
            END IF;

            EXECUTE IMMEDIATE '
                CREATE TABLE IDN_FLOW_CONTEXT_BATCH_TMP AS
                SELECT * FROM IDN_FLOW_CONTEXT_CHUNK_TMP
                WHERE ROWNUM <= ' || batchSize;

            rowCount := SQL%ROWCOUNT;
            batchCount := rowCount;
            COMMIT;

            IF batchCount = 0 THEN
                EXIT;
            END IF;

            IF enableLog THEN
                EXECUTE IMMEDIATE '
                    INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP
                    VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CREATED IDN_FLOW_CONTEXT_BATCH_TMP...'')';

                EXECUTE IMMEDIATE '
                    INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP
                    VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''BATCH DELETE STARTED ON IDN_FLOW_CONTEXT_STORE...'')';
            END IF;

            EXECUTE IMMEDIATE '
                DELETE FROM IDN_FLOW_CONTEXT_STORE
                WHERE ID IN (SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP)';

            rowCount := SQL%ROWCOUNT;
            COMMIT;

            IF enableLog THEN
                EXECUTE IMMEDIATE '
                    INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP
                    VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''BATCH DELETE FINISHED ON IDN_FLOW_CONTEXT_STORE : ' || rowCount || ''')';
            END IF;

            EXECUTE IMMEDIATE '
                DELETE FROM IDN_FLOW_CONTEXT_CHUNK_TMP
                WHERE ID IN (SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP)';

            rowCount := SQL%ROWCOUNT;
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    EXECUTE IMMEDIATE 'DROP TABLE IDN_FLOW_CONTEXT_BATCH_TMP PURGE';
    EXECUTE IMMEDIATE 'DROP TABLE IDN_FLOW_CONTEXT_CHUNK_TMP PURGE';

    IF enableLog THEN
        EXECUTE IMMEDIATE '
            INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP
            VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CLEANUP COMPLETED...!'')';
    END IF;

    COMMIT;
END;
/
