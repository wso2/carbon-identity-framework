CREATE OR REPLACE PROCEDURE WSO2_PAR_REQUEST_CLEANUP() AS $$
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
    cleanUpDateTimeLimit bigint;
    backupTable text;
    cusrRecord record;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('idn_oauth_par');

BEGIN

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    batchSize    := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize    := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    backupTables := FALSE; -- SET IF PAR TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    enableLog    := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]

    batchCount := 1000;
    chunkCount := 1000;
    rowCount   := 0;
    cleanUpRequestsTimeLimit := 24; -- SET SAFE PERIOD OF HOURS FOR REQUEST DELETE [DEFAULT : 24 hrs (1 day)]. REQUESTS OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.
    cleanUpDateTimeLimit := (extract(epoch from timezone('UTC'::text, now())) - (3600 * cleanUpRequestsTimeLimit)) * 1000;
    backupTable = 'idn_oauth_par_backup';

    IF (enableLog) THEN
        RAISE NOTICE 'WSO2_PAR_REQUEST_CLEANUP() STARTED...!';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables)
    THEN
        OPEN tablesCursor;
        LOOP
            FETCH tablesCursor INTO cusrRecord;
            EXIT WHEN NOT FOUND;
            backupTable := cusrRecord.tablename||'_backup';

            EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING backupTable;
            IF (rowcount = 1)
            THEN
                EXECUTE 'DROP TABLE '||quote_ident(backupTable);
            END IF;

            EXECUTE 'CREATE TABLE '||quote_ident(backupTable)||' as SELECT * FROM '||quote_ident(cusrRecord.tablename);

        END LOOP;
        CLOSE tablesCursor;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        IF ((chunkCount = 0)) THEN
            EXIT;
        END IF;

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS idn_oauth_par_chunk_tmp;
        CREATE TABLE idn_oauth_par_chunk_tmp AS SELECT req_uri_ref FROM idn_oauth_par
        WHERE (cleanUpDateTimeLimit > SCHEDULED_EXPIRY) LIMIT chunkSize;
        GET DIAGNOSTICS chunkCount := ROW_COUNT;
        COMMIT;
        IF (enableLog) THEN
            RAISE NOTICE 'CREATED IDN_OAUTH_PAR_CHUNK_TMP...';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS idn_oauth_par_batch_tmp;
            CREATE TABLE idn_oauth_par_batch_tmp AS
              SELECT req_uri_ref FROM idn_oauth_par_chunk_tmp LIMIT batchSize;
            GET diagnostics batchCount := ROW_COUNT;
            COMMIT;
            IF ((batchCount = 0)) THEN
                EXIT;
            END IF;
            IF (enableLog) THEN
                RAISE NOTICE 'CREATED IDN_OAUTH_PAR_BATCH_TMP...';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE STARTED ON IDN_OAUTH_PAR...';
            END IF;
            DELETE
            FROM idn_oauth_par
            WHERE req_uri_ref IN (SELECT req_uri_ref FROM idn_oauth_par_batch_tmp);
            GET DIAGNOSTICS rowCount := ROW_COUNT;
            commit;
            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_OAUTH_PAR : %', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            DELETE FROM idn_oauth_par_chunk_tmp WHERE req_uri_ref IN (SELECT req_uri_ref FROM idn_oauth_par_batch_tmp);

        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS idn_oauth_par_chunk_tmp;
    DROP TABLE IF EXISTS idn_oauth_par_batch_tmp;

END;
$$
LANGUAGE 'plpgsql';
