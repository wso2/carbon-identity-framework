-- ------------------------------------------
-- This PROCEDURE will cleanup the IDN_ACTION table by removing the entries which are not referenced by IDP_AUTHENTICATOR_PROPERTY table.
-- Refer for more information: https://github.com/wso2/product-is/issues/21944
-- ------------------------------------------

DROP PROCEDURE WSO2_IDN_ACTION_CLEANUP;
/

CREATE OR REPLACE PROCEDURE WSO2_IDN_ACTION_CLEANUP AS

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    batchSize        NUMBER := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize        NUMBER := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    batchCount       NUMBER := 1000;
    chunkCount       NUMBER := 1000;
    rowCount         NUMBER := 0;
    enableLog        BOOLEAN := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]
    backupTables     BOOLEAN := FALSE; -- SET IF IDN_ACTION TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
    sleepTime        NUMBER := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS [DEFAULT : 2]

BEGIN
    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    IF (enableLog) THEN
        DBMS_OUTPUT.PUT_LINE('WSO2_IDN_ACTION_CLEANUP() STARTED...!');
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        IF (enableLog) THEN
            DBMS_OUTPUT.PUT_LINE('TABLE BACKUP STARTED ... !');
        END IF;

        BEGIN
            EXECUTE IMMEDIATE 'DROP TABLE BAK_IDN_ACTION';
        EXCEPTION
            WHEN OTHERS THEN
                NULL; -- Ignore if table does not exist
        END;

        EXECUTE IMMEDIATE 'CREATE TABLE BAK_IDN_ACTION AS SELECT * FROM IDN_ACTION';
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        -- CREATE CHUNK TABLE
        BEGIN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_ACTION_CHUNK_TMP';
        EXCEPTION
            WHEN OTHERS THEN
                NULL; -- Ignore if table does not exist
        END;

        EXECUTE IMMEDIATE 'CREATE TABLE IDN_ACTION_CHUNK_TMP AS
                            SELECT UUID FROM IDN_ACTION
                            WHERE TYPE = ''AUTHENTICATION'' AND UUID NOT IN (
                                SELECT DISTINCT PROPERTY_VALUE
                                FROM IDP_AUTHENTICATOR_PROPERTY
                                WHERE PROPERTY_KEY = ''actionId'')';

        SELECT COUNT(*) INTO chunkCount FROM IDN_ACTION_CHUNK_TMP;
        EXECUTE IMMEDIATE 'CREATE INDEX IDN_ACTION_CHUNK_TMP_INDX ON IDN_ACTION_CHUNK_TMP (UUID)';
        COMMIT;

        IF (chunkCount = 0) THEN
            EXIT;
        END IF;

        IF (enableLog) THEN
            DBMS_OUTPUT.PUT_LINE('CREATED IDN_ACTION_CHUNK_TMP...');
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            -- CREATE BATCH TABLE
            BEGIN
                EXECUTE IMMEDIATE 'DROP TABLE IDN_ACTION_BATCH_TMP';
            EXCEPTION
                WHEN OTHERS THEN
                    NULL; -- Ignore if table does not exist
            END;

            EXECUTE IMMEDIATE 'CREATE TABLE IDN_ACTION_BATCH_TMP AS
                                SELECT UUID FROM IDN_ACTION_CHUNK_TMP FETCH FIRST :batchSize ROWS ONLY' USING batchSize;

            SELECT COUNT(*) INTO batchCount FROM IDN_ACTION_BATCH_TMP;
            COMMIT;

            IF (batchCount = 0) THEN
                EXIT;
            END IF;

            IF (enableLog) THEN
                DBMS_OUTPUT.PUT_LINE('CREATED IDN_ACTION_BATCH_TMP...');
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                DBMS_OUTPUT.PUT_LINE('BATCH DELETE STARTED ON IDN_ACTION...');
            END IF;

            DELETE A
              FROM IDN_ACTION A
              INNER JOIN IDN_ACTION_BATCH_TMP B
              ON A.UUID = B.UUID;

            SELECT SQL%ROWCOUNT INTO rowCount FROM DUAL;
            COMMIT;

            IF (enableLog) THEN
                DBMS_OUTPUT.PUT_LINE('BATCH DELETE FINISHED ON IDN_ACTION : ' || rowCount);
            END IF;

            -- DELETE FROM CHUNK
            DELETE A
              FROM IDN_ACTION_CHUNK_TMP A
              INNER JOIN IDN_ACTION_BATCH_TMP B
              ON A.UUID = B.UUID;

            IF (rowCount > 0) THEN
                DBMS_LOCK.sleep(sleepTime);
            END IF;
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE IDN_ACTION_CHUNK_TMP';
    EXCEPTION
        WHEN OTHERS THEN
            NULL; -- Ignore if table does not exist
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE IDN_ACTION_BATCH_TMP';
    EXCEPTION
        WHEN OTHERS THEN
            NULL; -- Ignore if table does not exist
    END;

    IF (enableLog) THEN
        DBMS_OUTPUT.PUT_LINE('CLEANUP COMPLETED...!');
    END IF;

END;
/
