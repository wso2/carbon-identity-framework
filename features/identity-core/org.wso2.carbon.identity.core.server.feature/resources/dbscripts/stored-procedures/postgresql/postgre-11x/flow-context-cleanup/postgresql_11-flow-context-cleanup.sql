CREATE OR REPLACE PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP() AS $$
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
    cleanUpRequestsTimeLimit int;
    cleanUpDateTimeLimit timestamp;
    backupTable text;
    cusrRecord record;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables
WHERE schemaname = current_schema()
  AND tablename = 'idn_flow_context_store';

BEGIN

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    batchSize    := 10000;
    chunkSize    := 500000;
    backupTables := FALSE;
    enableLog    := FALSE;

    batchCount := 1000;
    chunkCount := 1000;
    rowCount   := 0;
    cleanUpRequestsTimeLimit := 24;
    cleanUpDateTimeLimit := timezone('UTC', now()) - (cleanUpRequestsTimeLimit || ' hours')::interval;
    backupTable := 'idn_flow_context_store_backup';

    IF (enableLog) THEN
        RAISE NOTICE 'WSO2_IDN_FLOW_CONTEXT_CLEANUP() STARTED...!';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        OPEN tablesCursor;
        LOOP
            FETCH tablesCursor INTO cusrRecord;
            EXIT WHEN NOT FOUND;
            backupTable := cusrRecord.tablename || '_backup';

            EXECUTE format(
                'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = %L',
                backupTable
            ) INTO rowCount;

            IF (rowCount = 1) THEN
                EXECUTE format('DROP TABLE IF EXISTS %I', backupTable);
            END IF;

            EXECUTE format('CREATE TABLE %I AS SELECT * FROM %I', backupTable, cusrRecord.tablename);
        END LOOP;
        CLOSE tablesCursor;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        IF (chunkCount = 0) THEN
            EXIT;
        END IF;

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS idn_flow_context_chunk_tmp;
        CREATE TABLE idn_flow_context_chunk_tmp AS
        SELECT id FROM idn_flow_context_store
        WHERE expires_at < cleanUpDateTimeLimit
        LIMIT chunkSize;
        GET DIAGNOSTICS chunkCount := ROW_COUNT;
        COMMIT;

        IF (chunkCount = 0) THEN
            EXIT;
        END IF;

        IF (enableLog) THEN
            RAISE NOTICE 'CREATED IDN_FLOW_CONTEXT_CHUNK_TMP...';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            DROP TABLE IF EXISTS idn_flow_context_batch_tmp;
            CREATE TABLE idn_flow_context_batch_tmp AS
            SELECT id FROM idn_flow_context_chunk_tmp
            LIMIT batchSize;
            GET DIAGNOSTICS batchCount := ROW_COUNT;
            COMMIT;

            IF (batchCount = 0) THEN
                EXIT;
            END IF;

            IF (enableLog) THEN
                RAISE NOTICE 'CREATED IDN_FLOW_CONTEXT_BATCH_TMP...';
                RAISE NOTICE 'BATCH DELETE STARTED ON IDN_FLOW_CONTEXT_STORE...';
            END IF;

            DELETE FROM idn_flow_context_store
            WHERE id IN (SELECT id FROM idn_flow_context_batch_tmp);
            GET DIAGNOSTICS rowCount := ROW_COUNT;
            COMMIT;

            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_FLOW_CONTEXT_STORE : %', rowCount;
            END IF;

            DELETE FROM idn_flow_context_chunk_tmp
            WHERE id IN (SELECT id FROM idn_flow_context_batch_tmp);
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS idn_flow_context_batch_tmp;
    DROP TABLE IF EXISTS idn_flow_context_chunk_tmp;

END;
$$ LANGUAGE 'plpgsql';
