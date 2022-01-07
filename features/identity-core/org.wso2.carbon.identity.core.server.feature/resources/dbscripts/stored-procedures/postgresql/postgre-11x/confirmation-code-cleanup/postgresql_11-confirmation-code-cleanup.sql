CREATE OR REPLACE PROCEDURE WSO2_CONFIRMATION_CODE_CLEANUP() AS $$
DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    batchSize int;
    chunkSize int;
    batchCount int;
    chunkCount int;
    rowCount int;
    enableLog boolean;
    backupTables boolean;

BEGIN

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    batchSize    := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize    := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    backupTables := FALSE; -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    enableLog    := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]

    batchCount := 1000;
    chunkCount := 1000;
    rowCount   := 0;

    IF (enableLog) THEN
        RAISE NOTICE 'WSO2_CONFIRMATION_CODE_CLEANUP() STARTED...!';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables)
    THEN
        EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowCount USING 'idn_recovery_data_backup';
        IF (rowCount = 1)
        THEN
            IF (enableLog) THEN
                RAISE NOTICE 'CREATING BACKUP TABLE BAK_IDN_RECOVERY_DATA...';
            END IF;
            DROP TABLE idn_recovery_data_backup;
            CREATE TABLE idn_recovery_data_backup AS SELECT * FROM idn_recovery_data;
        END IF;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        IF ((chunkCount = 0)) THEN
            EXIT;
        END IF;

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS idn_recovery_data_chunk_tmp;
        CREATE TABLE idn_recovery_data_chunk_tmp AS SELECT code FROM idn_recovery_data LIMIT chunkSize;
        GET DIAGNOSTICS chunkCount := ROW_COUNT;
        COMMIT;
        IF (enableLog) THEN
            RAISE NOTICE 'CREATED IDN_RECOVERY_DATA_CHUNK_TMP...';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS idn_recovery_data_batch_tmp;
            CREATE TABLE idn_recovery_data_batch_tmp AS
              SELECT code FROM idn_recovery_data_chunk_tmp LIMIT batchSize;
            GET diagnostics batchCount := ROW_COUNT;
            COMMIT;
            IF ((batchCount = 0)) THEN
                EXIT;
            END IF;
            IF (enableLog) THEN
                RAISE NOTICE 'CREATED IDN_RECOVERY_DATA_BATCH_TMP...';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE STARTED ON IDN_RECOVERY_DATA...';
            END IF;
            DELETE FROM idn_recovery_data WHERE code IN (SELECT code FROM idn_recovery_data_batch_tmp);
            GET DIAGNOSTICS rowCount := ROW_COUNT;
            commit;
            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_RECOVERY_DATA : %', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            DELETE FROM idn_recovery_data_chunk_tmp WHERE code IN (SELECT code FROM idn_recovery_data_batch_tmp);

        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS idn_recovery_data_chunk_tmp;
    DROP TABLE IF EXISTS idn_recovery_data_batch_tmp;

END;
$$
LANGUAGE 'plpgsql';
