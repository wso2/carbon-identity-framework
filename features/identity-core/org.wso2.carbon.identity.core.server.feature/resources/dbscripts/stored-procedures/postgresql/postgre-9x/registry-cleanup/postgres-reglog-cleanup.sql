CREATE OR REPLACE FUNCTION WSO2_REG_LOG_CLEANUP(IN operationid int) RETURNS TEXT AS $$
DECLARE

batchSize int;
chunkSize int;
checkCount int;
backupTables boolean;
sleepTime float;
enableLog boolean;
logLevel VARCHAR(10);
rowcount bigint := 0;
cleaupCount int;
chunkCount int := 0;
batchCount int := 0;
batchStatus VARCHAR(10) := 'CONTINUE';

BEGIN
    batchSize := 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
    chunkSize := 500000;    -- SET TEMP TABLE CHUNK SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 500000]
    sleepTime := 2;          -- Sleep time in seconds.[DEFAULT : 2]
    checkCount := 1000; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 1000]
    enableLog := TRUE;	-- SET TRUE IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
    backupTables := TRUE;    -- SET TRUE IF REG PROPERTIES TO BACKUP BEFORE DELETE [DEFAULT : FALSE].
    logLevel := 'TRACE';    -- SET LOG LEVELS : INFO, TRACE , DEBUG
    rowCount := 0;

RAISE NOTICE 'INFO : WSO2_REG_LOG_CLEANUP() STARTED .... !';

-- ------------------------------------------
--  TABLE BACKUP
-- ------------------------------------------
IF (operationid = 0)
THEN
    IF (backupTables)
    THEN
        IF (enableLog) THEN
        RAISE NOTICE 'TABLE BACKUP STARTED ... !';
        END IF;

        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'REG_LOG_BACKUP' and TABLE_SCHEMA in (current_database())))
        THEN
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'TRACE : CREATING BACKUP TABLE REG_LOG_BACKUP .. !';
            END IF;
            CREATE TABLE IF NOT EXISTS REG_LOG_BACKUP AS SELECT * FROM REG_LOG WHERE 1 = 2;
        ELSE
            IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'TRACE :USING PREVIOUS BACKUP TABLE REG_LOG_BACKUP ..!';
            END IF;
        END IF;
    END IF;
END IF;

-- ------------------------------------------------------
-- CALCULATING REG_LOG
-- ------------------------------------------------------
IF (operationid = 1)
THEN
    IF (enableLog)
    THEN
        RAISE NOTICE 'INFO : CALCULATING DELETE ELIGIBLE ON REG_LOG TABLE .... !';

        IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
        THEN
        SELECT  COUNT(1) into rowcount FROM REG_LOG;
        RAISE NOTICE 'DEBUG : TOTAL REG_LOG TABLE BEFORE DELETE: %', rowcount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
      SELECT COUNT(1) into cleaupCount FROM (
      (SELECT RL.REG_LOG_ID FROM REG_LOG RL LEFT JOIN (
      SELECT MAX(REG_LOG_ID) AS REG_LOG_ID FROM REG_LOG GROUP BY REG_PATH, REG_TENANT_ID) X
      ON RL.REG_LOG_ID = X.REG_LOG_ID
      WHERE X.REG_LOG_ID IS NULL)
      UNION
      (SELECT REG_LOG_ID FROM REG_LOG WHERE REG_ACTION = 7)) A;
        RAISE NOTICE 'TRACE : TOTAL REG_LOG SHOULD BE DELETED ON: %', cleaupCount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        rowcount  := (rowcount - cleaupCount);
        RAISE NOTICE 'TRACE : TOTAL REG_LOG SHOULD BE RETAIN: %', rowcount;
        END IF;
    END IF;
END IF;

-- ------------------------------------------
-- PURGE REG_LOG
-- ------------------------------------------
IF (operationid = 2)
THEN
    DROP TABLE IF EXISTS REG_LOG_CHUNK_TMP;
    CREATE TABLE REG_LOG_CHUNK_TMP AS SELECT REG_LOG_ID FROM (
      (SELECT RL.REG_LOG_ID FROM REG_LOG RL LEFT JOIN (
      SELECT MAX(REG_LOG_ID) AS REG_LOG_ID FROM REG_LOG GROUP BY REG_PATH, REG_TENANT_ID) X
      ON RL.REG_LOG_ID = X.REG_LOG_ID
      WHERE X.REG_LOG_ID IS NULL)
      UNION
      (SELECT REG_LOG_ID FROM REG_LOG WHERE REG_ACTION = 7)
        ) A LIMIT chunkSize ;

    GET diagnostics chunkCount := ROW_COUNT;
    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
        RAISE NOTICE 'DEBUG : REG_LOG_CHUNK_TMP TABLE CREATED WITH %', chunkCount ;
    END IF;

    IF (chunkCount < checkCount OR chunkCount = 0)
    THEN
        IF (chunkCount < checkCount)
        THEN
            RAISE NOTICE 'INFO : DELETE ELIGIBLE COUNT IS LESS THAN CHECK_COUNT DEFINED.';
            RAISE NOTICE 'DELETE ELIGIBLE: %', chunkCount;
            RAISE NOTICE 'CHECK_COUNT: %', checkCount;
            batchStatus := 'FINISHED';
        END IF;
    END IF;
    RAISE NOTICE 'INFO : CREATING INDEX REG_LOG_CHUNK_TMP_INDX';
    CREATE INDEX REG_LOG_CHUNK_TMP_INDX on REG_LOG_CHUNK_TMP (REG_LOG_ID);

    LOOP
        DROP TABLE IF EXISTS REG_LOG_BATCH_TMP;
        CREATE TABLE REG_LOG_BATCH_TMP AS SELECT REG_LOG_ID FROM REG_LOG_CHUNK_TMP LIMIT batchSize;

        GET diagnostics batchCount := ROW_COUNT;

        IF (batchCount = 0)
        THEN
            EXIT;
        END IF;

        IF (backupTables)
        THEN
            IF (enableLog AND logLevel IN ('TRACE'))
            THEN
                RAISE NOTICE 'TRACE : BACKING UP TABLES REG_LOG_BACKUP' ;
            END IF;
            INSERT INTO REG_LOG_BACKUP SELECT RL.* FROM  REG_LOG AS RL INNER JOIN REG_LOG_BATCH_TMP AS RLB ON RL.REG_LOG_ID = RLB.REG_LOG_ID;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            RAISE NOTICE 'TRACE : BATCH DELETE STARTED ON REG_LOG: %',  batchCount;
        END IF;

        DELETE FROM REG_LOG where REG_LOG_ID in (SELECT REG_LOG_ID from REG_LOG_BATCH_TMP);
        GET diagnostics rowCount := ROW_COUNT;

        IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
        THEN
            RAISE NOTICE 'DEBUG : BATCH DELETE FINISHED ON REG_LOG: %', rowCount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            RAISE NOTICE 'TRACE : BATCH DELETE STARTED ON REG_LOG_CHUNK_TMP: %',  batchCount;
        END IF;

        DELETE FROM REG_LOG_CHUNK_TMP where REG_LOG_ID in (SELECT REG_LOG_ID from REG_LOG_BATCH_TMP);

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
            RAISE NOTICE 'TRACE : BATCH DELETE FINISHED ON REG_LOG_CHUNK_TMP';
        END IF;

        IF ((rowCount > 0))
        THEN
          IF (enableLog AND logLevel IN ('TRACE'))
          THEN
            RAISE NOTICE 'TRACE : SLEEPING FOR SECONDS: %',  sleepTime;
          END IF;
          perform pg_sleep(sleepTime);
          batchStatus := 'CONTINUE';
        END IF;
    END LOOP;
END IF;

-- ------------------------------------------------------
-- CLEANUP ANY EXISTING TEMP TABLES
-- ------------------------------------------------------
IF (operationid = 3)
    THEN
    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
        RAISE NOTICE 'TRACE : DROP TEMP TABLES REG_LOG_CHUNK_TMP';
        DROP TABLE IF EXISTS REG_LOG_CHUNK_TMP;
        DROP TABLE IF EXISTS REG_LOG_BATCH_TMP;
    END IF;
END IF;

-- ------------------------------------------------------
-- CALCULATING REG_LOG
-- ------------------------------------------------------
IF (operationid = 4)
    THEN
    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
        SELECT  COUNT(1) into rowcount FROM REG_LOG;
        RAISE NOTICE 'DEBUG : TOTAL REG_LOG TABLE AFTER DELETE %', rowcount;

    END IF;

    IF (enableLog)
    THEN
        RAISE NOTICE 'INFO : WSO2_REG_LOG_CLEANUP TASK COMPLETED .... !';
    END IF;
END IF;

RETURN (batchStatus);
END;
$$
LANGUAGE 'plpgsql';
