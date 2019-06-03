CREATE OR REPLACE PROCEDURE WSO2_TOKEN_CLEANUP_SP() AS $$
DECLARE

batchSize int;
chunkSize int;
checkCount int;
backupTables boolean;
sleepTime float;
safePeriod int;
enableLog boolean;
logLevel VARCHAR(10);
enableAudit boolean;
deleteTillTime timestamp;
maxValidityPeriod bigint;
backupTable text;
indexTable text;
notice text;
cusrRecord record;
rowcount bigint :=0;
cleanupCount bigint :=0;
deleteCount INT := 0;
chunkCount INT := 0;
batchCount INT := 0;
enableReindexing boolean;
enableTblAnalyzing boolean;

tablesCursor CURSOR FOR SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
tablename  IN ('idn_oauth2_access_token', 'idn_oauth2_authorization_code', 'idn_oauth2_access_token_scope','idn_oidc_req_object_reference','idn_oidc_req_object_claims','idn_oidc_req_obj_claim_values');


BEGIN

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES     [DEFAULT : 500000]
checkCount := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE TOKENS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
backupTables := TRUE;   -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE  [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
sleepTime := 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
safePeriod := 2; -- SET SAFE PERIOD OF HOURS FOR TOKEN DELETE, SINCE TOKENS COULD BE CASHED    [DEFAULT : 2]
enableLog := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]
logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE , DEBUG
enableAudit := TRUE;  -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED TOKENS USING A TABLE    [DEFAULT : TRUE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
enableReindexing :=FALSE; -- SET TRUE FOR GATHER SCHEMA LEVEL STATS TO IMPROVE QUERY PERFOMANCE [DEFAULT : FALSE]
enableTblAnalyzing :=FALSE;	-- SET TRUE FOR Rebuild Indexes TO IMPROVE QUERY PERFOMANCE [DEFAULT : FALSE]

-- ------------------------------------------
-- CONSTANT VARIABLES
-- ------------------------------------------
deleteTillTime := timezone('UTC'::text, now()) - INTERVAL '1hour' * safePeriod;
maxValidityPeriod :=99999999999990;


-- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_ACCESS_TOKEN TABLE
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
-- CREATING auditlog_idn_oauth2_access_token_cleanup and auditlog_idn_oauth2_authorization_code_cleanup FOR DELETING
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (enableAudit)
THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'CREATING AUDIT TABLES ... !';
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('auditlog_idn_oauth2_access_token_cleanup');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP .. !';
        END IF;
        CREATE TABLE auditlog_idn_oauth2_access_token_cleanup as SELECT * FROM idn_oauth2_access_token WHERE 1 = 2;
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP ..!';
        END IF;
    END IF;

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('auditlog_idn_oauth2_authorization_code_cleanup');
    IF (rowcount = 0)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP .. !';
        END IF;
        CREATE TABLE auditlog_idn_oauth2_authorization_code_cleanup as SELECT * FROM idn_oauth2_authorization_code WHERE 1 = 2;
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP ..!';
        END IF;
    END IF;
END IF;

-- ------------------------------------------------------
-- DELETE IDN_OAUTH2_ACCESS_TOKEN CALCULATION
-- ------------------------------------------------------

IF (enableLog) THEN
    RAISE NOTICE '';
    RAISE NOTICE 'CALCULATING TOKENS ON IDN_OAUTH2_ACCESS_TOKEN .... !';

    IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
    SELECT COUNT(1) INTO rowcount FROM idn_oauth2_access_token;
    RAISE NOTICE 'TOTAL TOKENS ON IDN_OAUTH2_ACCESS_TOKEN TABLE BEFORE DELETE: %',rowcount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    SELECT COUNT(1) INTO cleanupCount FROM idn_oauth2_access_token WHERE TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR (TOKEN_STATE in('ACTIVE') AND
    (VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND (REFRESH_TOKEN_VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND
    (deleteTillTime > (TIME_CREATED +  INTERVAL '1minute' * ( VALIDITY_PERIOD / 60000))  ) AND
    (deleteTillTime > (REFRESH_TOKEN_TIME_CREATED +  INTERVAL '1minute' * ( REFRESH_TOKEN_VALIDITY_PERIOD / 60000 ))));
    RAISE NOTICE 'TOTAL TOKENS SHOULD BE DELETED FROM IDN_OAUTH2_ACCESS_TOKEN: %',cleanupCount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    RAISE NOTICE 'TOTAL TOKENS SHOULD BE RETAIN IN IDN_OAUTH2_ACCESS_TOKEN: %',rowcount;
    END IF;
END IF;

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN
-- ------------------------------------------------------

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'TOKEN DELETE ON IDN_OAUTH2_ACCESS_TOKEN STARTED .... !';
END IF;

LOOP

    SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('chunk_idn_oauth2_access_token');
    IF (rowcount = 1)
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'DROPPING EXISTING TABLE CHUNK_IDN_OAUTH2_ACCESS_TOKEN  !';
        END IF;
        DROP TABLE chunk_idn_oauth2_access_token;
    END IF;

    CREATE TABLE chunk_idn_oauth2_access_token (TOKEN_ID VARCHAR);

    INSERT INTO chunk_idn_oauth2_access_token (TOKEN_ID) SELECT TOKEN_ID FROM idn_oauth2_access_token WHERE (TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR
    (TOKEN_STATE in ('ACTIVE') AND (VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND (REFRESH_TOKEN_VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND
    ( deleteTillTime > (TIME_CREATED +  INTERVAL '1minute' *( VALIDITY_PERIOD / 60000)) ) AND
    ( deleteTillTime > (REFRESH_TOKEN_TIME_CREATED +  INTERVAL '1minute' *( REFRESH_TOKEN_VALIDITY_PERIOD / 60000 ))))) LIMIT chunkSize;
    GET diagnostics chunkCount := ROW_COUNT;

    IF (chunkCount < checkCount)
    THEN
    EXIT;
    END IF;

    CREATE INDEX idx_chnk_idn_oth_acs_tkn ON chunk_idn_oauth2_access_token (TOKEN_ID);


    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    RAISE NOTICE '';
    RAISE NOTICE 'PROCEEDING WITH NEW CHUNK TABLE CHUNK_IDN_OAUTH2_ACCESS_TOKEN  %',chunkCount;
    RAISE NOTICE '';
    END IF;

    IF (enableAudit)
    THEN
    INSERT INTO auditlog_idn_oauth2_access_token_cleanup SELECT TOK.* FROM idn_oauth2_access_token TOK , chunk_idn_oauth2_access_token CHK WHERE TOK.TOKEN_ID=CHK.TOKEN_ID;
   	COMMIT;
	END IF;


    LOOP
        SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('idn_oauth2_access_token_batch');
        IF (rowcount = 1)
        THEN
        DROP TABLE idn_oauth2_access_token_batch;
        END IF;

        CREATE TABLE idn_oauth2_access_token_batch (TOKEN_ID VARCHAR);

        INSERT INTO idn_oauth2_access_token_batch (TOKEN_ID) SELECT TOKEN_ID FROM chunk_idn_oauth2_access_token LIMIT batchSize;
        GET diagnostics batchCount := ROW_COUNT;

        IF ((batchCount = 0))
        THEN
        EXIT WHEN batchCount=0;
        END IF;

--         CREATE INDEX idx_idn_oth2_acs_tkn_bTh ON idn_oauth2_access_token_batch (TOKEN_ID);

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE '';
        RAISE NOTICE 'BATCH DELETE START ON TABLE IDN_OAUTH2_ACCESS_TOKEN WITH : %',batchCount;
        END IF;

        DELETE FROM idn_oauth2_access_token where TOKEN_ID in (select TOKEN_ID from idn_oauth2_access_token_batch);
        GET diagnostics deleteCount := ROW_COUNT;
		COMMIT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE')) THEN
        RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_OAUTH2_ACCESS_TOKEN WITH : %',deleteCount;
        END IF;

        DELETE FROM chunk_idn_oauth2_access_token WHERE TOKEN_ID in (select TOKEN_ID from idn_oauth2_access_token_batch);

        IF (enableLog AND logLevel IN ('TRACE')) THEN
        RAISE NOTICE 'DELETED BATCH ON  chunk_idn_oauth2_access_token !';
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
RAISE NOTICE 'TOKEN DELETE ON IDN_OAUTH2_ACCESS_TOKEN COMPLETED .... !';
END IF;

-- ------------------------------------------------------
-- DELETE IDN_OAUTH2_AUTHORIZATION_CODE CALCULATION
-- ------------------------------------------------------

IF (enableLog ) THEN
	RAISE NOTICE '';
    RAISE NOTICE 'CALCULATING CODES ON IDN_OAUTH2_AUTHORIZATION_CODE .... !';

    IF (enableLog  AND logLevel IN ('TRACE','DEBUG')) THEN
    SELECT count(1) INTO rowcount FROM idn_oauth2_authorization_code;
    RAISE NOTICE 'TOTAL AUTHORIZATION CODES ON IDN_OAUTH2_AUTHORIZATION_CODE TABLE BEFORE DELETE: %',rowCount;
    END IF;

    IF (enableLog  AND logLevel IN ('TRACE')) THEN

    SELECT COUNT(1) into cleanupCount FROM idn_oauth2_authorization_code WHERE CODE_ID IN
    (SELECT CODE_ID FROM idn_oauth2_authorization_code code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN tok where tok.TOKEN_ID = code.TOKEN_ID))
    AND (((VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND deleteTillTime > (TIME_CREATED + INTERVAL '1minute' *( VALIDITY_PERIOD / 60000 )))
    OR STATE ='INACTIVE');
    RAISE NOTICE 'TOTAL AUTHORIZATION CODES SHOULD BE DELETED FROM IDN_OAUTH2_AUTHORIZATION_CODE: %', cleanupCount;
    END IF;

    IF (enableLog  AND logLevel IN ('TRACE')) THEN
    rowcount := (rowcount - cleanupCount);
    RAISE NOTICE 'TOTAL AUTHORIZATION CODES SHOULD BE RETAIN IN THE IDN_OAUTH2_AUTHORIZATION_CODE: %', rowCount;
    END IF;
END IF;


-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE
-- ------------------------------------------------------

IF (enableLog)
THEN
RAISE NOTICE '';
RAISE NOTICE 'CODE DELETE ON IDN_OAUTH2_AUTHORIZATION_CODE STARTED .... !';
END IF;

LOOP
      SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('idn_oauth2_authorization_code_chunk');
      IF (rowcount = 1)
      THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE '';
            RAISE NOTICE 'DROPPING EXISTING TABLE IDN_OAUTH2_AUTHORIZATION_CODE_CHUNK !';
            END IF;
            DROP TABLE idn_oauth2_authorization_code_chunk;
      END IF;

      CREATE TABLE idn_oauth2_authorization_code_chunk (CODE_ID VARCHAR);

      INSERT INTO idn_oauth2_authorization_code_chunk (code_id) SELECT code_id FROM idn_oauth2_authorization_code WHERE CODE_ID IN
      (SELECT CODE_ID FROM idn_oauth2_authorization_code code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN tok where tok.TOKEN_ID = code.TOKEN_ID))
      AND (((VALIDITY_PERIOD BETWEEN 0 and maxValidityPeriod) AND deleteTillTime > (TIME_CREATED + INTERVAL '1minute' *( VALIDITY_PERIOD / 60000 )))
      OR STATE ='INACTIVE') LIMIT chunkSize;
      GET diagnostics chunkCount := ROW_COUNT;

      IF (chunkCount < checkCount)
      THEN
      EXIT;
      END IF;

      CREATE INDEX idx_idn_oauth2_authorization_code_chunk ON idn_oauth2_authorization_code_chunk (CODE_ID);

      IF (enableLog AND logLevel IN ('TRACE')) THEN
      RAISE NOTICE '';
      RAISE NOTICE 'PROCEEDING WITH NEW CHUNK TABLE IDN_OAUTH2_AUTHORIZATION_CODE_CHUNK WITH %',chunkCount;
      RAISE NOTICE '';
      END IF;

      IF (enableAudit)
      THEN
      INSERT INTO auditlog_idn_oauth2_authorization_code_cleanup SELECT code.* FROM idn_oauth2_authorization_code code , idn_oauth2_authorization_code_chunk CHK WHERE code.code_id=CHK.code_id;
      COMMIT;
	  END IF;


      LOOP

      SELECT count(1) INTO rowcount  from pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename IN ('idn_oath2_authrizatn_cde_batch');
      IF (rowcount = 1)
      THEN
      DROP TABLE idn_oath2_authrizatn_cde_batch;
      END IF;

      CREATE TABLE idn_oath2_authrizatn_cde_batch (CODE_ID VARCHAR);

      INSERT INTO idn_oath2_authrizatn_cde_batch (CODE_ID) SELECT CODE_ID FROM idn_oauth2_authorization_code_chunk LIMIT batchSize;
      GET diagnostics batchCount := ROW_COUNT;

      IF (batchCount = 0)
      THEN
      EXIT WHEN batchCount=0;
      END IF;

--       CREATE INDEX idx_idn_oath2_authrizatn_cde_batch ON idn_oath2_authrizatn_cde_batch (CODE_ID);


      IF (enableLog AND logLevel IN ('TRACE')) THEN
      RAISE NOTICE '';
      RAISE NOTICE 'BATCH DELETE START ON TABLE IDN_OAUTH2_AUTHORIZATION_CODE WITH : %',batchCount;
      END IF;

      DELETE FROM idn_oauth2_authorization_code where CODE_ID in (select CODE_ID from idn_oath2_authrizatn_cde_batch);
      GET diagnostics deleteCount := ROW_COUNT;
	  COMMIT;

      IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
      RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_OAUTH2_AUTHORIZATION_CODE WITH : %',deleteCount;
      END IF;

      DELETE FROM idn_oauth2_authorization_code_chunk WHERE CODE_ID in (select CODE_ID from idn_oath2_authrizatn_cde_batch);
      IF (enableLog AND logLevel IN ('TRACE')) THEN
      RAISE NOTICE 'DELETED BATCH ON IDN_OAUTH2_AUTHORIZATION_CODE_CHUNK !';
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
RAISE NOTICE 'CODE DELETE ON IDN_OAUTH2_AUTHORIZATION_CODE COMPLETED .... !';
END IF;

-- ------------------------------------------------------
-- REBUILDING INDEXES
-- ------------------------------------------------------
IF (enableReindexing)
THEN
    OPEN tablesCursor;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'INDEX REBUILDING STARTED ...!';
    END IF;
    LOOP
        FETCH tablesCursor INTO cusrRecord;
        EXIT WHEN NOT FOUND;
        IF (enableLog AND logLevel IN ('TRACE','DEBUG')) THEN
        RAISE NOTICE 'INDEX REBUILDING FOR TABLE %',cusrRecord.tablename;
        END IF;
        EXECUTE 'REINDEX TABLE '||quote_ident(cusrRecord.tablename);
    END LOOP;
    IF (enableLog AND logLevel IN ('TRACE')) THEN
    RAISE NOTICE 'INDEX REBUILDING COMPLETED ...!';
    END IF;
    CLOSE tablesCursor;
    RAISE NOTICE '';
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
RAISE NOTICE 'WSO2_TOKEN_CLEANUP_SP COMPLETED .... !';
RAISE NOTICE '';
END IF;

END;
$$
LANGUAGE 'plpgsql';
