CREATE OR REPLACE FUNCTION WSO2_WF_REQUEST_CLEANUP() RETURNS TEXT AS $$

DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    batchSize int;
    chunkSize int;
    batchCount int;
    chunkCount int;
    rowCount int;
    totalDeleted int;
    enableLog boolean;
    backupTables boolean;
    cleanUpRequestsTimeLimit int;
    cleanUpDateTimeLimit timestamp;
    backupTable text;
    cusrRecord record;
    batchStatus VARCHAR(10) := 'CONTINUE';
    
    tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
    tablename IN ('wf_request', 'wf_workflow_request_relation', 'wf_workflow_approval_relation');

BEGIN

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    batchSize    := 10000;  -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize    := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    backupTables := FALSE;  -- SET IF WF TABLES NEED TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
    enableLog    := TRUE;   -- ENABLE LOGGING [DEFAULT : TRUE]
    
    batchCount := 1000;
    chunkCount := 1000;
    rowCount   := 0;
    totalDeleted := 0;
    cleanUpRequestsTimeLimit := 60; -- SET SAFE PERIOD IN DAYS FOR REQUEST DELETE [DEFAULT : 60 days (2 months)]
    cleanUpDateTimeLimit := CURRENT_TIMESTAMP - (cleanUpRequestsTimeLimit || ' days')::INTERVAL;
    
    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'WSO2_WF_REQUEST_CLEANUP() STARTED...!';
        RAISE NOTICE 'Cleanup Period: % days', cleanUpRequestsTimeLimit;
        RAISE NOTICE 'Cleanup Date Limit: %', cleanUpDateTimeLimit;
        RAISE NOTICE 'Target Statuses: APPROVED, REJECTED, FAILED, ABORTED';
        RAISE NOTICE '========================================';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        IF (enableLog) THEN
            RAISE NOTICE 'Starting backup process...';
        END IF;
        
        OPEN tablesCursor;
        LOOP
            FETCH tablesCursor INTO cusrRecord;
            EXIT WHEN NOT FOUND;
            backupTable := cusrRecord.tablename||'_backup';
            
            EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = $1' 
                INTO rowcount USING backupTable;
            IF (rowcount = 1) THEN
                EXECUTE 'DROP TABLE '||quote_ident(backupTable);
                IF (enableLog) THEN
                    RAISE NOTICE 'Dropped existing backup table: %', backupTable;
                END IF;
            END IF;
            
            -- Create backup only for records that will be deleted
            IF cusrRecord.tablename = 'wf_request' THEN
                EXECUTE 'CREATE TABLE '||quote_ident(backupTable)||' AS 
                         SELECT * FROM '||quote_ident(cusrRecord.tablename)||' 
                         WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') 
                         AND UPDATED_AT < $1' USING cleanUpDateTimeLimit;
            ELSE
                -- For related tables, backup records that reference the requests to be deleted
                EXECUTE 'CREATE TABLE '||quote_ident(backupTable)||' AS 
                         SELECT t.* FROM '||quote_ident(cusrRecord.tablename)||' t
                         WHERE EXISTS (
                             SELECT 1 FROM wf_request r 
                             WHERE r.UUID = t.'||
                             CASE cusrRecord.tablename
                                 WHEN 'wf_workflow_request_relation' THEN 'REQUEST_ID'
                                 WHEN 'wf_workflow_approval_relation' THEN 'EVENT_ID'
                             END ||'
                             AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'')
                             AND r.UPDATED_AT < $1
                         )' USING cleanUpDateTimeLimit;
            END IF;
            
            GET DIAGNOSTICS rowCount := ROW_COUNT;
            IF (enableLog) THEN
                RAISE NOTICE 'Created backup table % with % records', backupTable, rowCount;
            END IF;
        END LOOP;
        CLOSE tablesCursor;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    IF (enableLog) THEN
        RAISE NOTICE 'Starting cleanup process...';
    END IF;
    
    LOOP
        IF ((chunkCount = 0)) THEN
            EXIT;
        END IF;

        -- CREATE CHUNK TABLE WITH ELIGIBLE REQUEST IDs
        EXECUTE 'DROP TABLE IF EXISTS wf_request_chunk_tmp';
        EXECUTE 'CREATE TABLE wf_request_chunk_tmp AS 
            SELECT UUID FROM wf_request
            WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'')
            AND UPDATED_AT < $1
            LIMIT $2' USING cleanUpDateTimeLimit, chunkSize;
        GET DIAGNOSTICS chunkCount := ROW_COUNT;
        
        IF (enableLog) THEN
            RAISE NOTICE 'Created WF_REQUEST_CHUNK_TMP with % records', chunkCount;
        END IF;
        
        IF (chunkCount = 0) THEN
            EXIT;
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            -- CREATE BATCH TABLE
            EXECUTE 'DROP TABLE IF EXISTS wf_request_batch_tmp';
            EXECUTE 'CREATE TABLE wf_request_batch_tmp AS
                SELECT UUID FROM wf_request_chunk_tmp LIMIT $1' USING batchSize;
            GET DIAGNOSTICS batchCount := ROW_COUNT;
            
            IF ((batchCount = 0)) THEN
                EXIT;
            END IF;
            
            IF (enableLog) THEN
                RAISE NOTICE 'Processing batch with % records...', batchCount;
            END IF;

            -- DELETE FROM WF_REQUEST (CASCADE DELETE will handle child tables automatically)
            EXECUTE 'DELETE FROM wf_request
            WHERE UUID IN (SELECT UUID FROM wf_request_batch_tmp)';
            GET DIAGNOSTICS rowCount := ROW_COUNT;
            totalDeleted := totalDeleted + rowCount;
            
            IF (enableLog) THEN
                RAISE NOTICE '  Deleted % records from WF_REQUEST (and cascaded to child tables)', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            EXECUTE 'DELETE FROM wf_request_chunk_tmp 
            WHERE UUID IN (SELECT UUID FROM wf_request_batch_tmp)';

        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    EXECUTE 'DROP TABLE IF EXISTS wf_request_chunk_tmp';
    EXECUTE 'DROP TABLE IF EXISTS wf_request_batch_tmp';

    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'WSO2_WF_REQUEST_CLEANUP() COMPLETED!';
        RAISE NOTICE 'Total WF_REQUEST records deleted: %', totalDeleted;
        RAISE NOTICE '========================================';
    END IF;

    RETURN (batchStatus);

EXCEPTION
    WHEN OTHERS THEN
        -- Cleanup temp tables in case of error
        EXECUTE 'DROP TABLE IF EXISTS wf_request_chunk_tmp';
        EXECUTE 'DROP TABLE IF EXISTS wf_request_batch_tmp';
        
        IF (enableLog) THEN
            RAISE NOTICE 'Error occurred: %', SQLERRM;
        END IF;
        
        -- Re-raise the exception
        RAISE;

END;
$$
LANGUAGE 'plpgsql';

-- To execute the cleanup function
-- SELECT WSO2_WF_REQUEST_CLEANUP();
