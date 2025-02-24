-- ------------------------------------------
-- This PROCEDURE will cleanup the IDN_ACTION table by removing the entries which are not referenced by IDP_AUTHENTICATOR_PROPERTY table.
-- Refer for more information: https://github.com/wso2/product-is/issues/21944
-- ------------------------------------------

DROP PROCEDURE IF EXISTS WSO2_IDN_ACTION_CLEANUP;
CREATE OR REPLACE PROCEDURE WSO2_IDN_ACTION_CLEANUP()
LANGUAGE plpgsql
AS $$
DECLARE
    batchSize INT := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize INT := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    batchCount INT := 1000;
    chunkCount INT := 1000;
    rowCount INT := 0;
    enableLog BOOLEAN := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]
    backupTables BOOLEAN := FALSE; -- SET IF IDN_ACTION TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE], WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    sleepTime FLOAT := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS [DEFAULT : 2]
BEGIN

    IF enableLog THEN
        RAISE NOTICE 'WSO2_IDN_ACTION_CLEANUP() STARTED...!';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF backupTables THEN
        IF enableLog THEN
            RAISE NOTICE 'TABLE BACKUP STARTED...!';
        END IF;

        IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'BAK_IDN_ACTION') THEN
            IF enableLog THEN
                RAISE NOTICE 'DELETING OLD BACKUP...';
            END IF;
            DROP TABLE BAK_IDN_ACTION;
        END IF;
        EXECUTE 'CREATE TABLE BAK_IDN_ACTION AS SELECT * FROM IDN_ACTION';
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        -- Create chunk table
        DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP;
        EXECUTE 'CREATE TABLE IDN_ACTION_CHUNK_TMP AS
            SELECT UUID FROM IDN_ACTION
            WHERE TYPE = ''AUTHENTICATION'' AND UUID NOT IN (
                SELECT DISTINCT PROPERTY_VALUE
                FROM IDP_AUTHENTICATOR_PROPERTY
                WHERE PROPERTY_KEY = ''actionId''
            ) LIMIT ' || chunkSize;

        -- Get the row count
        SELECT COUNT(*) INTO chunkCount FROM IDN_ACTION_CHUNK_TMP;
        CREATE INDEX IF NOT EXISTS IDN_ACTION_CHUNK_TMP_INDX ON IDN_ACTION_CHUNK_TMP (UUID);
        COMMIT;

        IF chunkCount = 0 THEN
            EXIT;
        END IF;

        IF enableLog THEN
            RAISE NOTICE 'CREATED IDN_ACTION_CHUNK_TMP...';
        END IF;

        -- Batch loop
        batchCount := 1;
        LOOP
            -- Create batch table
            DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP;
            EXECUTE 'CREATE TABLE IDN_ACTION_BATCH_TMP AS
                SELECT UUID FROM IDN_ACTION_CHUNK_TMP LIMIT ' || batchSize;

            -- Get row count for batch
            SELECT COUNT(*) INTO batchCount FROM IDN_ACTION_BATCH_TMP;
            COMMIT;

            IF batchCount = 0 THEN
                EXIT;
            END IF;

            IF enableLog THEN
                RAISE NOTICE 'CREATED IDN_ACTION_BATCH_TMP...';
            END IF;

            -- Batch deletion
            IF enableLog THEN
                RAISE NOTICE 'BATCH DELETE STARTED ON IDN_ACTION...';
            END IF;

            DELETE FROM IDN_ACTION A
            USING IDN_ACTION_BATCH_TMP B
            WHERE A.UUID = B.UUID;
            GET DIAGNOSTICS rowCount = ROW_COUNT;
            COMMIT;

            IF enableLog THEN
                RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_ACTION: %', rowCount;
            END IF;

            -- Delete from chunk table
            DELETE FROM IDN_ACTION_CHUNK_TMP A
            USING IDN_ACTION_BATCH_TMP B
            WHERE A.UUID = B.UUID;

            IF rowCount > 0 THEN
                PERFORM pg_sleep(sleepTime);
            END IF;

        END LOOP;
    END LOOP;

    -- Clean up temporary tables
    DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP;
    DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP;

    IF enableLog THEN
        RAISE NOTICE 'CLEANUP COMPLETED...!';
    END IF;

END;
$$;
