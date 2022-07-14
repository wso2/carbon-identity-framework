CREATE OR REPLACE PROCEDURE WSO2_SESSION_DATA_CLEANUP_SP() AS $$
DECLARE

batchSize int;
chunkSize int;
checkCount int;
backupTables boolean;
sleepTime float;
enableLog boolean;
logLevel VARCHAR(10);
unix_timestamp bigint;
newChunk boolean;
backupTable text;
notice text;
cusrRecord record;
rowcount bigint :=0;
cleanupCount bigint :=0;
sessionCleanupTime bigint :=0;
operationCleanupTime bigint :=0;
deleteCount INT := 0;
deleteMappingCount INT := 0;
deleteAppInfoCount INT := 0;
deleteMetadataCount INT := 0;
deleteFederatedSessionMappingCount INT := 0;
chunkCount INT := 0;
batchCount INT := 0;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('idn_auth_session_store');

purgingTable text;
purgingSessionUserMappingTable text;
purgingSessionAppInfoTable text;
purgingSessionMetadataTable text;
purgingFederatedAuthSessionMappingTable text;
purgingChunkTable text;
purgingBatchTable text;
purgeBseColmn text;
purgeBseColmnType text;
purgingCondition text;
operationName text;

BEGIN

-- ------------------------------------------
-- USER CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES     [DEFAULT : 500000]
checkCount := 500; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE RECORDS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 500]
backupTables := FALSE;   -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE  [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG

-- ------------------------------------------
-- CONSTANT VARIABLES
-- ------------------------------------------
unix_timestamp := TRUNC(extract(epoch from now() at time zone 'utc'));

purgingTable := 'idn_auth_session_store';
purgingSessionUserMappingTable = 'idn_auth_user_session_mapping';
purgingSessionAppInfoTable = 'idn_auth_session_app_info';
purgingSessionMetadataTable = 'idn_auth_session_meta_data';
purgingFederatedAuthSessionMappingTable = 'idn_fed_auth_session_mapping';
purgeBseColmn := 'session_id';
purgeBseColmnType := 'varchar';

purgingChunkTable := purgingTable||'_chnuk';
purgingBatchTable := purgingTable||'_batch';

-- ------------------------------------------------------
-- CREATING BACKUP TABLE
-- ------------------------------------------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_SESSION_DATA_CLEANUP_SP STARTED .... !';
RAISE NOTICE '';
END IF;

IF (backupTables)
THEN
    IF (enableLog) THEN
    RAISE NOTICE 'TABLE BACKUP STARTED ... !';
    END IF;

    OPEN tablesCursor;
    LOOP
        FETCH tablesCursor INTO cusrRecord;
        EXIT WHEN NOT FOUND;
        backupTable := cusrRecord.tablename||'_backup';

        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING backupTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'TABLE ALREADY EXISTS HENCE DROPPING TABLE %',backupTable;
            END IF;
            EXECUTE 'DROP TABLE '||quote_ident(backupTable);
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        EXECUTE 'SELECT COUNT(1) FROM '||quote_ident(cusrRecord.tablename) INTO rowcount;
        notice := cusrRecord.tablename||' NUMBER OF RECORDS: '||rowcount;
        RAISE NOTICE 'BACKING UP %',notice;
        END IF;

        EXECUTE 'CREATE TABLE '||quote_ident(backupTable)||' as SELECT * FROM '||quote_ident(cusrRecord.tablename);

        IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
        EXECUTE 'SELECT COUNT(1) FROM '||quote_ident(backupTable) INTO rowcount;
        notice := cusrRecord.tablename||' TABLE INTO '||backupTable||' TABLE COMPLETED WITH : '||rowcount;
        RAISE NOTICE 'BACKING UP %',notice;
        RAISE NOTICE '';
        END IF;
    END LOOP;
    CLOSE tablesCursor;
END IF;


-- ------------------------------------------
-- REMOVE SESSION DATA
-- ------------------------------------------
-- Session data older than 20160 minutes(14 days) will be removed.
sessionCleanupTime := (unix_timestamp*1000000000) - (20160*60000000000);

purgingCondition := 'select session_id from idn_auth_session_store where time_created < '||sessionCleanupTime||'';

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'SESSION DATA PURGING STARTED .... !';
    RAISE NOTICE '';

    notice := 'CALCULATING PURGING RECORDS ON TABLE '|| purgingTable || '.... ! ';
    RAISE NOTICE '%',notice;

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    EXECUTE 'SELECT count(1) from '||purgingTable into rowcount;
    notice := 'TOTAL RECODES ON '|| purgingTable || ' TABLE BEFORE DELETE : ' || rowcount;
    RAISE NOTICE '%',notice;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE 'SELECT count(1) from ( '||purgingCondition||' ) a' into cleanupcount;
    notice := 'TOTAL RECODES ON '|| purgingTable || ' SHOULD BE DELETED  : ' || cleanupCount;
    RAISE NOTICE '%',notice;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    notice := 'TOTAL RECODES ON '|| purgingTable || ' SHOULD BE RETAIN  : ' || rowcount;
    RAISE NOTICE '%',notice;
    END IF;
END IF;

LOOP

    EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingChunkTable;
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'DROPPING TABLE '|| purgingChunkTable;
        RAISE NOTICE '%',notice;
        END IF;
        EXECUTE 'DROP TABLE '||quote_ident(purgingChunkTable);
    END IF;


    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    notice := 'PROCEED WITH NEW CHUNK TABLE '|| purgingChunkTable;
    RAISE NOTICE '%',notice;
    END IF;

    EXECUTE 'CREATE TABLE '||quote_ident(purgingChunkTable)||' ('||quote_ident(purgeBseColmn)||' '||purgeBseColmnType||')';
    EXECUTE 'CREATE INDEX '||quote_ident(purgingChunkTable)||'_idx ON '||quote_ident(purgingChunkTable)||'('||quote_ident(purgeBseColmn)||')';

    EXECUTE 'insert into '||quote_ident(purgingChunkTable)||' ('||quote_ident(purgeBseColmn)||') '||purgingCondition||' limit '||chunksize;
    get diagnostics chunkcount := row_count;

    IF (chunkCount < checkCount)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'SESSION DATA PURGING FINISHED SINCE NEW CHUNK CREATED WITH '||chunkCount||' AND ITS LESS THAN THE CHECK COUNT DEFINED : '||checkCount;
        RAISE NOTICE '%',notice;
        END IF;
        EXIT;
    END IF;

    LOOP
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingBatchTable;
        IF (rowcount = 1)
        THEN
        EXECUTE 'DROP TABLE '||quote_ident(purgingBatchTable);
        END IF;

        EXECUTE 'CREATE TABLE '||quote_ident(purgingBatchTable)||' ('||quote_ident(purgeBseColmn)||' '||purgeBseColmnType||')';
        EXECUTE 'CREATE INDEX '||quote_ident(purgingBatchTable)||'_idx ON '||quote_ident(purgingBatchTable)||'('||quote_ident(purgeBseColmn)||')';

        EXECUTE 'insert into '||quote_ident(purgingBatchTable)||' ('||quote_ident(purgeBseColmn)||') select '||quote_ident(purgeBseColmn)||' from '||purgingChunkTable||' limit '||batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        EXIT WHEN batchCount=0;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'BATCH DELETE START ON TABLE '||purgingTable||' WITH :'||batchCount;
        RAISE NOTICE '%',notice;
        END IF;

        EXECUTE 'DELETE from '||quote_ident(purgingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
        GET diagnostics deleteCount := ROW_COUNT;
        COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        notice := 'BATCH DELETE FINISHED ON TABLE '||purgingTable||' WITH :'||deleteCount;
        RAISE NOTICE '%',notice;
        END IF;

        -- Deleting user-session mappings from 'idn_auth_user_session_mapping' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionUserMappingTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            notice := 'BATCH DELETE START ON TABLE '||purgingSessionUserMappingTable||' WITH :'||batchCount;
            RAISE NOTICE '%',notice;
            END IF;

            EXECUTE 'DELETE from '||quote_ident(purgingSessionUserMappingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteMappingCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
            notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionUserMappingTable||' WITH :'||deleteMappingCount;
            RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting user-session mappings from 'idn_auth_user_session_mapping' table

        -- Deleting session app info from 'idn_auth_session_app_info' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionAppInfoTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            notice := 'BATCH DELETE START ON TABLE '||purgingSessionAppInfoTable||' WITH :'||batchCount;
            RAISE NOTICE '%',notice;
            END IF;
            EXECUTE 'DELETE from '||quote_ident(purgingSessionAppInfoTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteAppInfoCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
            notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionAppInfoTable||' WITH :'||deleteAppInfoCount;
            RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting session app info from 'idn_auth_session_app_info' table

        -- Deleting session metadata from 'idn_auth_session_meta_data' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionMetadataTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            notice := 'BATCH DELETE START ON TABLE '||purgingSessionMetadataTable||' WITH :'||batchCount;
            RAISE NOTICE '%',notice;
            END IF;
            EXECUTE 'DELETE from '||quote_ident(purgingSessionMetadataTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteMetadataCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
            notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionMetadataTable||' WITH :'||deleteMetadataCount;
            RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting session metadata from 'idn_auth_session_meta_data' table

        -- Deleting federated session mappings from 'idn_fed_auth_session_mapping' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingFederatedAuthSessionMappingTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            notice := 'BATCH DELETE START ON TABLE '||purgingFederatedAuthSessionMappingTable||' WITH :'||batchCount;
            RAISE NOTICE '%',notice;
            END IF;

            EXECUTE 'DELETE from '||quote_ident(purgingFederatedAuthSessionMappingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteFederatedSessionMappingCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
            notice := 'BATCH DELETE FINISHED ON TABLE '||purgingFederatedAuthSessionMappingTable||' WITH :'||deleteFederatedSessionMappingCount;
            RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of federated session mappings from 'idn_fed_auth_session_mapping' table

        EXECUTE ' DELETE from '||quote_ident(purgingChunkTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'DELETED BATCH ON TABLE '||purgingChunkTable||' WITH :'||batchCount;
        RAISE NOTICE '%',notice;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'SLEEPING ...';
        END IF;
        perform pg_sleep(sleepTime);

    END LOOP;
END LOOP;




-- --------------------------------------------
-- REMOVE OPERATIONAL DATA
-- --------------------------------------------
-- Operational data older than 720 minutes(12 h) will be removed.
operationCleanupTime = (unix_timestamp*1000000000) - (720*60000000000);

purgingCondition := 'select session_id from idn_auth_session_store where operation = ''DELETE'' and time_created < '||operationCleanupTime||'';

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'OPERATIONAL DATA PURGING STARTED .... !';
    RAISE NOTICE '';

    notice := 'CALCULATING PURGING RECORDS ON TABLE '|| purgingTable || '.... ! ';
    RAISE NOTICE '%',notice;

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    EXECUTE 'SELECT count(1) from '||purgingTable into rowcount;
    notice := 'TOTAL RECODES ON '|| purgingTable || ' TABLE BEFORE DELETE : ' || rowcount;
    RAISE NOTICE '%',notice;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    EXECUTE 'SELECT count(1) from ( '||purgingCondition||' ) a' into cleanupcount;
    notice := 'TOTAL RECODES ON '|| purgingTable || ' SHOULD BE DELETED  : ' || cleanupCount;
    RAISE NOTICE '%',notice;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    notice := 'TOTAL RECODES ON '|| purgingTable || ' SHOULD BE RETAIN  : ' || rowcount;
    RAISE NOTICE '%',notice;
    END IF;
END IF;

LOOP

    EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingChunkTable;
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'DROPPING TABLE '|| purgingChunkTable;
        RAISE NOTICE '%',notice;
        END IF;
        EXECUTE 'DROP TABLE '||quote_ident(purgingChunkTable);
    END IF;


    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    notice := 'PROCEED WITH NEW CHUNK TABLE '|| purgingChunkTable;
    RAISE NOTICE '%',notice;
    END IF;

    EXECUTE 'CREATE TABLE '||quote_ident(purgingChunkTable)||' ('||quote_ident(purgeBseColmn)||' '||purgeBseColmnType||')';
    EXECUTE 'CREATE INDEX '||quote_ident(purgingChunkTable)||'_idx ON '||quote_ident(purgingChunkTable)||'('||quote_ident(purgeBseColmn)||')';

    EXECUTE 'insert into '||quote_ident(purgingChunkTable)||' ('||quote_ident(purgeBseColmn)||') '||purgingCondition||' limit '||chunksize;
    get diagnostics chunkcount := row_count;

    IF (chunkCount < checkCount)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'OPERATIONAL DATA PURGING FINISHED SINCE NEW CHUNK CREATED WITH '||chunkCount||' AND ITS LESS THAN THE CHECK COUNT DEFINED : '||checkCount;
        RAISE NOTICE '%',notice;
        END IF;
        EXIT;
    END IF;

    LOOP
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingBatchTable;
        IF (rowcount = 1)
        THEN
        EXECUTE 'DROP TABLE '||quote_ident(purgingBatchTable);
        END IF;

        EXECUTE 'CREATE TABLE '||quote_ident(purgingBatchTable)||' ('||quote_ident(purgeBseColmn)||' '||purgeBseColmnType||')';
        EXECUTE 'CREATE INDEX '||quote_ident(purgingBatchTable)||'_idx ON '||quote_ident(purgingBatchTable)||'('||quote_ident(purgeBseColmn)||')';

        EXECUTE 'insert into '||quote_ident(purgingBatchTable)||' ('||quote_ident(purgeBseColmn)||') select '||quote_ident(purgeBseColmn)||' from '||purgingChunkTable||' limit '||batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        EXIT WHEN batchCount=0;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'BATCH DELETE START ON TABLE '||purgingTable||' WITH :'||batchCount;
        RAISE NOTICE '%',notice;
        END IF;

        EXECUTE 'DELETE from '||quote_ident(purgingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
        GET diagnostics deleteCount := ROW_COUNT;
        COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        notice := 'BATCH DELETE FINISHED ON TABLE '||purgingTable||' WITH :'||deleteCount;
        RAISE NOTICE '%',notice;
        END IF;

        -- Deleting user-session mappings from 'idn_auth_user_session_mapping' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionUserMappingTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                notice := 'BATCH DELETE START ON TABLE '||purgingSessionUserMappingTable||' WITH :'||batchCount;
                RAISE NOTICE '%',notice;
            END IF;

            EXECUTE 'DELETE from '||quote_ident(purgingSessionUserMappingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteMappingCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
                notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionUserMappingTable||' WITH :'||deleteMappingCount;
                RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting user-session mappings from 'idn_auth_user_session_mapping' table

        -- Deleting session app info from 'idn_auth_session_app_info' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionAppInfoTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                notice := 'BATCH DELETE START ON TABLE '||purgingSessionAppInfoTable||' WITH :'||batchCount;
                RAISE NOTICE '%',notice;
            END IF;
            EXECUTE 'DELETE from '||quote_ident(purgingSessionAppInfoTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteAppInfoCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
                notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionAppInfoTable||' WITH :'||deleteAppInfoCount;
                RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting session app info from 'idn_auth_session_app_info' table

        -- Deleting session metadata from 'idn_auth_session_meta_data' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingSessionMetadataTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                notice := 'BATCH DELETE START ON TABLE '||purgingSessionMetadataTable||' WITH :'||batchCount;
                RAISE NOTICE '%',notice;
            END IF;
            EXECUTE 'DELETE from '||quote_ident(purgingSessionMetadataTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteMetadataCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
                notice := 'BATCH DELETE FINISHED ON TABLE '||purgingSessionMetadataTable||' WITH :'||deleteMetadataCount;
                RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting session metadata from 'idn_auth_session_meta_data' table

        -- Deleting federated session mappings from 'idn_fed_auth_session_mapping' table
        EXECUTE 'SELECT count(1) from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' into rowcount USING purgingFederatedAuthSessionMappingTable;
        IF (rowcount = 1)
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
                notice := 'BATCH DELETE START ON TABLE '||purgingFederatedAuthSessionMappingTable||' WITH :'||batchCount;
                RAISE NOTICE '%',notice;
            END IF;
            EXECUTE 'DELETE from '||quote_ident(purgingFederatedAuthSessionMappingTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
            GET diagnostics deleteFederatedSessionMappingCount := ROW_COUNT;
            COMMIT;

            IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
                notice := 'BATCH DELETE FINISHED ON TABLE '||purgingFederatedAuthSessionMappingTable||' WITH :'||deleteFederatedSessionMappingCount;
                RAISE NOTICE '%',notice;
            END IF;
        END IF;
        -- End of deleting federated session mappings from 'idn_fed_auth_session_mapping' table

        EXECUTE ' DELETE from '||quote_ident(purgingChunkTable)||' a USING '||purgingBatchTable||' b where a.'||purgeBseColmn||' = b.'||purgeBseColmn||'';
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        notice := 'DELETED BATCH ON TABLE '||purgingChunkTable||' WITH :'||batchCount;
        RAISE NOTICE '%',notice;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'SLEEPING ...';
        END IF;
        perform pg_sleep(sleepTime);

    END LOOP;
END LOOP;

END;
$$
LANGUAGE 'plpgsql';
