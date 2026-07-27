CREATE OR REPLACE FUNCTION WSO2_DEVICE_CODE_CLEANUP_SP() RETURNS VOID AS $$
DECLARE

batchSize int;
chunkSize int;
checkCount int;
backupTables boolean;
safePeriod int;
sleepTime float;
enableLog boolean;
logLevel VARCHAR(10);
enableAudit boolean;
backupTable text;
notice text;
cusrRecord record;
rowcount bigint :=0;
cleanupCount bigint :=0;
deleteCount INT := 0;
chunkCount INT := 0;
batchCount INT := 0;
enableTblAnalyzing boolean;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('idn_oauth2_device_flow', 'idn_oauth2_device_flow_scopes');

BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES     [DEFAULT : 500000]
checkCount := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE TOKENS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
backupTables := FALSE;   -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE  [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
safePeriod := 24; -- SET SAFE PERIOD FOR DELETE EXPIRED CODE [DEFAULT : 24]
sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG
enableAudit := FALSE;  -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED TOKENS USING A TABLE    [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
enableTblAnalyzing :=FALSE;	-- SET TRUE FOR Rebuild Indexes TO IMPROVE QUERY PERFOMANCE [DEFAULT : FALSE]

-- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_DEVICE_FLOW TABLE
-- ------------------------------------------------------

IF (enableLog) THEN
RAISE NOTICE 'WSO2_TOKEN_CLEANUP_SP STARTED .... !';
RAISE NOTICE '';
END IF;

IF (enableAudit)
THEN
backupTables:=TRUE;
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
          backupTable := 'bak_'||cusrRecord.tablename;

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
          notice := cusrRecord.tablename||' NUMBER OF TOKENS: '||rowcount;
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

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING auditlog_idn_oauth2_device_flow_cleanup FOR DELETING
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (enableAudit)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'CREATING AUDIT TABLES ... !';
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('auditlog_idn_oauth2_device_flow_cleanup');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP .. !';
        END IF;
        CREATE TABLE auditlog_idn_oauth2_device_flow_cleanup as SELECT * FROM idn_oauth2_device_flow WHERE 1 = 2;
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP ..!';
        END IF;
    END IF;
END IF;

-- ------------------------------------------------------
-- DELETE IDN_OAUTH2_DEVICE_FLOW CALCULATION
-- ------------------------------------------------------

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'CALCULATING DEVICE CODES ON IDN_OAUTH2_DEVICE_FLOW .... !';

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    SELECT COUNT(1) INTO rowcount FROM idn_oauth2_device_flow;
    RAISE NOTICE 'TOTAL DEVICE CODES ON IDN_OAUTH2_DEVICE_FLOW TABLE BEFORE DELETE: %',rowcount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    SELECT COUNT(1) INTO cleanupCount FROM idn_oauth2_device_flow WHERE STATUS IN ('EXPIRED')
    OR timezone('UTC'::text, now()) >  (EXPIRY_TIME  + (INTERVAL '1hour' * safePeriod));
    RAISE NOTICE 'TOTAL DEVICE CODES SHOULD BE DELETED FROM IDN_OAUTH2_DEVICE_FLOW: %',cleanupCount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    RAISE NOTICE 'TOTAL DEVICE CODES SHOULD BE RETAIN IN IDN_OAUTH2_DEVICE_FLOW: %',rowcount;
    END IF;
END IF;

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_DEVICE_FLOW
-- ------------------------------------------------------

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'DEVICE CODE DELETE ON IDN_OAUTH2_DEVICE_FLOW STARTED .... !';
END IF;

LOOP

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('chunk_idn_oauth2_device_flow');
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'DROPPING EXISTING TABLE CHUNK_IDN_OAUTH2_DEVICE_FLOW  !';
        END IF;
        DROP TABLE chunk_idn_oauth2_device_flow;
    END IF;

    CREATE TABLE chunk_idn_oauth2_device_flow (CODE_ID VARCHAR);

    INSERT INTO chunk_idn_oauth2_device_flow (CODE_ID) SELECT CODE_ID FROM idn_oauth2_device_flow WHERE STATUS IN ('EXPIRED')
    OR timezone('UTC'::text, now()) >  (EXPIRY_TIME  + (INTERVAL '1hour' * safePeriod)) LIMIT chunkSize;
    GET diagnostics chunkCount := ROW_COUNT;

    IF (chunkCount < checkCount)
    THEN
    EXIT;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    RAISE NOTICE '';
    RAISE NOTICE 'PROCEEDING WITH NEW CHUNK TABLE CHUNK_IDN_OAUTH2_DEVICE_FLOW  %',chunkCount;
    RAISE NOTICE '';
    END IF;

     IF (enableAudit)
     THEN
     INSERT INTO auditlog_idn_oauth2_device_flow_cleanup SELECT TOK.* FROM idn_oauth2_device_flow TOK , chunk_idn_oauth2_device_flow CHK WHERE TOK.CODE_ID=CHK.CODE_ID;
     END IF;

     LOOP
        SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('idn_oauth2_device_flow_batch');
        IF (rowcount = 1)
        THEN
        DROP TABLE idn_oauth2_device_flow_batch;
        END IF;

        CREATE TABLE idn_oauth2_device_flow_batch (CODE_ID VARCHAR);

        INSERT INTO idn_oauth2_device_flow_batch (CODE_ID) SELECT CODE_ID FROM chunk_idn_oauth2_device_flow LIMIT batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        IF ((batchCount = 0))
        THEN
        EXIT WHEN batchCount=0;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'BATCH DELETE START ON TABLE IDN_OAUTH2_DEVICE_FLOW WITH : %',batchCount;
        END IF;

        DELETE FROM idn_oauth2_device_flow where CODE_ID in (select CODE_ID from idn_oauth2_device_flow_batch);
        GET diagnostics deleteCount := ROW_COUNT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_OAUTH2_DEVICE_FLOW WITH : %',deleteCount;
        END IF;

        DELETE FROM chunk_idn_oauth2_device_flow WHERE CODE_ID in (select CODE_ID from idn_oauth2_device_flow_batch);

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'DELETED BATCH ON  chunk_idn_oauth2_device_flow !';
        END IF;

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'SLEEPING ...';
        END IF;
        perform pg_sleep(sleepTime);

    END LOOP;
END LOOP;

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'DEVICE CODE DELETE ON IDN_OAUTH2_DEVICE_FLOW COMPLETED .... !';
END IF;

-- ------------------------------------------------------
-- ANALYSING TABLES
-- ------------------------------------------------------
IF (enableTblAnalyzing)
THEN
    OPEN tablesCursor;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'TABLE ANALYZING STARTED ...!';
    END IF;
    LOOP
        FETCH tablesCursor INTO cusrRecord;
        EXIT WHEN NOT FOUND;
        IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
        RAISE NOTICE 'TABLE ANALYZING FOR TABLE %',cusrRecord.tablename;
        END IF;
        EXECUTE 'ANALYZE '||quote_ident(cusrRecord.tablename);
    END LOOP;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'TABLE ANALYZING COMPLETED ...!';
    END IF;
    CLOSE tablesCursor;
    RAISE NOTICE '';
END IF;

IF (enableLog) THEN
RAISE NOTICE '';
RAISE NOTICE 'WSO2_DEVICE_CODE_CLEANUP_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
