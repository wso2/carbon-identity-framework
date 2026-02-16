-- ------------------------------------------
-- Procedure to cleanup tokens from IDN_OAUTH2_REFRESH_TOKEN and IDN_OAUTH2_REVOKED_TOKENS tables.
-- Procedure to cleanup expired revoked subject entities from IDN_SUBJECT_ENTITY_REVOKED_EVENT table.
-- !!! Note: Use this procedure only if you are using non-persistent access tokens.
-- ------------------------------------------

CREATE OR REPLACE PROCEDURE WSO2_NON_PERSISTENCE_ACCESS_TOKEN_CLEANUP_SP IS
  utcTime         TIMESTAMP WITH TIME ZONE := SYSTIMESTAMP AT TIME ZONE 'UTC';

  -- ------------------------------------------
  -- CONFIGURABLE ATTRIBUTES
  -- ------------------------------------------
  batchSize       PLS_INTEGER := 10000;     -- batch delete size
  chunkSize       PLS_INTEGER := 500000;    -- chunk table size
  checkCount      PLS_INTEGER := 100;       -- stop when chunk < checkCount
  backupTables    BOOLEAN     := TRUE;      -- create BAK_* tables
  sleepTime       NUMBER      := 2;         -- seconds (needs DBMS_LOCK privilege)
  safePeriodHours PLS_INTEGER := 2;         -- hours
  enableLog       BOOLEAN     := TRUE;
  logLevel        VARCHAR2(10):= 'TRACE';   -- TRACE / DEBUG
  enableAudit     BOOLEAN     := FALSE;     -- if TRUE, forces backupTables TRUE

  -- ------------------------------------------
  -- CONSTANTS / VARIABLES
  -- ------------------------------------------
  maxValidityPeriod NUMBER := 99999999999990; -- > 3170 years in ms => skip
  deleteTimeLimit   TIMESTAMP WITH TIME ZONE := utcTime - NUMTODSINTERVAL(safePeriodHours, 'HOUR');

  current_schema   VARCHAR2(128);
  rowCount         PLS_INTEGER := 0;
  chunkCountVar    PLS_INTEGER := 0;
  batchCountVar    PLS_INTEGER := 0;

  refreshTokenExpireTime NUMBER := 86400; -- seconds (fallback)

  PROCEDURE log_msg(p_msg VARCHAR2) IS
  BEGIN
    IF enableLog THEN
      EXECUTE IMMEDIATE
        'INSERT INTO LOG_WSO2_NONPERSIST_AT_CLEANUP (TS, LOG) VALUES (:1, :2)'
        USING TO_CHAR(SYSTIMESTAMP, 'DD.MM.YYYY HH24:MI:SS:FF4'), p_msg;
      COMMIT; -- keep if you want log persisted even if main work rolls back
    END IF;
  END;

  PROCEDURE ensure_log_table IS
    c PLS_INTEGER;
  BEGIN
    IF NOT enableLog THEN
      RETURN;
    END IF;

    SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') INTO current_schema FROM dual;

    SELECT COUNT(*) INTO c
    FROM ALL_TABLES
    WHERE OWNER = current_schema
      AND TABLE_NAME = 'LOG_WSO2_NONPERSIST_AT_CLEANUP';

    IF c = 1 THEN
      EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_NONPERSIST_AT_CLEANUP';
    END IF;

    EXECUTE IMMEDIATE
      'CREATE TABLE LOG_WSO2_NONPERSIST_AT_CLEANUP (TS VARCHAR2(250), LOG VARCHAR2(1000)) NOLOGGING';

    log_msg('WSO2_NON_PERSISTENCE_ACCESS_TOKEN_CLEANUP_SP STARTED');
    log_msg('USING SCHEMA: ' || current_schema);
  END;

  PROCEDURE drop_table_if_exists(p_table VARCHAR2) IS
    c PLS_INTEGER;
  BEGIN
    SELECT COUNT(*) INTO c
    FROM ALL_TABLES
    WHERE OWNER = current_schema
      AND TABLE_NAME = UPPER(p_table);

    IF c = 1 THEN
      EXECUTE IMMEDIATE 'DROP TABLE ' || p_table;
      -- DDL auto-commits
    END IF;
  END;

  PROCEDURE backup_table(p_table VARCHAR2) IS
    bak VARCHAR2(128);
    c   PLS_INTEGER;
  BEGIN
    bak := REPLACE(p_table, 'IDN_', 'BAK_');

    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('BACKUP START: ' || p_table || ' -> ' || bak);
    END IF;

    SELECT COUNT(*) INTO c
    FROM ALL_TABLES
    WHERE OWNER = current_schema
      AND TABLE_NAME = UPPER(bak);

    IF c = 1 THEN
      EXECUTE IMMEDIATE 'DROP TABLE ' || bak;
    END IF;

    EXECUTE IMMEDIATE 'CREATE TABLE ' || bak || ' AS SELECT * FROM ' || p_table;

    IF enableLog AND logLevel IN ('TRACE','DEBUG') THEN
      log_msg('BACKUP DONE: ' || p_table || ' -> ' || bak);
    END IF;
  END;

BEGIN
  SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') INTO current_schema FROM dual;

  IF enableAudit THEN
    backupTables := TRUE;
  END IF;

  ensure_log_table;

  -- ------------------------------------------
  -- BACKUP TABLES (optional)
  -- ------------------------------------------
  IF backupTables THEN
    log_msg('TABLE BACKUP STARTED');
    backup_table('IDN_OAUTH2_REFRESH_TOKEN');
    backup_table('IDN_OAUTH2_REFRESH_TOKEN_SCOPE');
    backup_table('IDN_OAUTH2_REVOKED_TOKENS');
    backup_table('IDN_SUBJECT_ENTITY_REVOKED_EVENT');
    log_msg('TABLE BACKUP COMPLETED');
  END IF;

  -- ------------------------------------------
  -- 1) CLEANUP: IDN_OAUTH2_REFRESH_TOKEN
  -- ------------------------------------------
  log_msg('DELETE START: IDN_OAUTH2_REFRESH_TOKEN');

  LOOP
    drop_table_if_exists('CHUNK_IDN_OAUTH2_REFRESH_TOKEN');
    EXECUTE IMMEDIATE
      'CREATE TABLE CHUNK_IDN_OAUTH2_REFRESH_TOKEN (RID ROWID PRIMARY KEY) NOLOGGING';

    EXECUTE IMMEDIATE
      'INSERT /*+ APPEND */ INTO CHUNK_IDN_OAUTH2_REFRESH_TOKEN (RID)
       SELECT ROWID
       FROM IDN_OAUTH2_REFRESH_TOKEN
       WHERE ROWNUM <= :1
         AND (
              TOKEN_STATE IN (''INACTIVE'',''REVOKED'',''EXPIRED'')
              OR (
                  TOKEN_STATE = ''ACTIVE''
                  AND REFRESH_TOKEN_VALIDITY_PERIOD > 0
                  AND REFRESH_TOKEN_VALIDITY_PERIOD < :2
                  AND :3 > (REFRESH_TOKEN_TIME_CREATED
                            + NUMTODSINTERVAL(REFRESH_TOKEN_VALIDITY_PERIOD/60000, ''MINUTE''))
              )
         )'
      USING chunkSize, maxValidityPeriod, deleteTimeLimit;

    chunkCountVar := SQL%ROWCOUNT;
    COMMIT;

    EXIT WHEN chunkCountVar < checkCount;

    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('CHUNK READY (REFRESH_TOKEN): ' || chunkCountVar);
    END IF;

    LOOP
      drop_table_if_exists('BATCH_IDN_OAUTH2_REFRESH_TOKEN');
      EXECUTE IMMEDIATE
        'CREATE TABLE BATCH_IDN_OAUTH2_REFRESH_TOKEN (RID ROWID PRIMARY KEY) NOLOGGING';

      EXECUTE IMMEDIATE
        'INSERT /*+ APPEND */ INTO BATCH_IDN_OAUTH2_REFRESH_TOKEN (RID)
         SELECT RID
         FROM CHUNK_IDN_OAUTH2_REFRESH_TOKEN
         WHERE ROWNUM <= :1'
        USING batchSize;

      batchCountVar := SQL%ROWCOUNT;
      COMMIT;

      EXIT WHEN batchCountVar = 0;

      EXECUTE IMMEDIATE
        'DELETE FROM IDN_OAUTH2_REFRESH_TOKEN
         WHERE ROWID IN (SELECT RID FROM BATCH_IDN_OAUTH2_REFRESH_TOKEN)';
      rowCount := SQL%ROWCOUNT;
      COMMIT;

      log_msg('BATCH DELETED (REFRESH_TOKEN): ' || rowCount);

      EXECUTE IMMEDIATE
        'DELETE FROM CHUNK_IDN_OAUTH2_REFRESH_TOKEN
         WHERE RID IN (SELECT RID FROM BATCH_IDN_OAUTH2_REFRESH_TOKEN)';
      COMMIT;

      IF rowCount > 0 THEN
        DBMS_LOCK.SLEEP(sleepTime);
      END IF;
    END LOOP;
  END LOOP;

  log_msg('DELETE DONE: IDN_OAUTH2_REFRESH_TOKEN');

  -- ------------------------------------------
  -- 2) CLEANUP: IDN_OAUTH2_REVOKED_TOKENS (expired)
  -- ------------------------------------------
  log_msg('DELETE START: IDN_OAUTH2_REVOKED_TOKENS');

  LOOP
    drop_table_if_exists('CHUNK_IDN_OAUTH2_REVOKED_TOKENS');
    EXECUTE IMMEDIATE
      'CREATE TABLE CHUNK_IDN_OAUTH2_REVOKED_TOKENS (RID ROWID PRIMARY KEY) NOLOGGING';

    EXECUTE IMMEDIATE
      'INSERT /*+ APPEND */ INTO CHUNK_IDN_OAUTH2_REVOKED_TOKENS (RID)
       SELECT ROWID
       FROM IDN_OAUTH2_REVOKED_TOKENS
       WHERE ROWNUM <= :1
         AND :2 > EXPIRY_TIMESTAMP'
      USING chunkSize, deleteTimeLimit;

    chunkCountVar := SQL%ROWCOUNT;
    COMMIT;

    EXIT WHEN chunkCountVar < checkCount;

    LOOP
      drop_table_if_exists('BATCH_IDN_OAUTH2_REVOKED_TOKENS');
      EXECUTE IMMEDIATE
        'CREATE TABLE BATCH_IDN_OAUTH2_REVOKED_TOKENS (RID ROWID PRIMARY KEY) NOLOGGING';

      EXECUTE IMMEDIATE
        'INSERT /*+ APPEND */ INTO BATCH_IDN_OAUTH2_REVOKED_TOKENS (RID)
         SELECT RID
         FROM CHUNK_IDN_OAUTH2_REVOKED_TOKENS
         WHERE ROWNUM <= :1'
        USING batchSize;

      batchCountVar := SQL%ROWCOUNT;
      COMMIT;

      EXIT WHEN batchCountVar = 0;

      EXECUTE IMMEDIATE
        'DELETE FROM IDN_OAUTH2_REVOKED_TOKENS
         WHERE ROWID IN (SELECT RID FROM BATCH_IDN_OAUTH2_REVOKED_TOKENS)';
      rowCount := SQL%ROWCOUNT;
      COMMIT;

      log_msg('BATCH DELETED (REVOKED_TOKENS): ' || rowCount);

      EXECUTE IMMEDIATE
        'DELETE FROM CHUNK_IDN_OAUTH2_REVOKED_TOKENS
         WHERE RID IN (SELECT RID FROM BATCH_IDN_OAUTH2_REVOKED_TOKENS)';
      COMMIT;

      IF rowCount > 0 THEN
        DBMS_LOCK.SLEEP(sleepTime);
      END IF;
    END LOOP;
  END LOOP;

  log_msg('DELETE DONE: IDN_OAUTH2_REVOKED_TOKENS');

  -- ------------------------------------------
  -- 3) CLEANUP: IDN_SUBJECT_ENTITY_REVOKED_EVENT
  -- ------------------------------------------
  BEGIN
    SELECT NVL(MAX(REFRESH_TOKEN_EXPIRE_TIME), 86400)
      INTO refreshTokenExpireTime
      FROM IDN_OAUTH_CONSUMER_APPS;
      
  EXCEPTION
    WHEN OTHERS THEN
      refreshTokenExpireTime := 86400;
  END;

  log_msg('DELETE START: IDN_SUBJECT_ENTITY_REVOKED_EVENT');

  LOOP
    drop_table_if_exists('CHUNK_IDN_SUBJECT_ENTITY_REVOKED_EVENT');
    EXECUTE IMMEDIATE
      'CREATE TABLE CHUNK_IDN_SUBJECT_ENTITY_REVOKED_EVENT (RID ROWID PRIMARY KEY) NOLOGGING';

    EXECUTE IMMEDIATE
      'INSERT /*+ APPEND */ INTO CHUNK_IDN_SUBJECT_ENTITY_REVOKED_EVENT (RID)
       SELECT ROWID
       FROM IDN_SUBJECT_ENTITY_REVOKED_EVENT
       WHERE ROWNUM <= :1
         AND :2 > (TIME_REVOKED + NUMTODSINTERVAL(:3/60, ''MINUTE''))'
      USING chunkSize, deleteTimeLimit, refreshTokenExpireTime;

    chunkCountVar := SQL%ROWCOUNT;
    COMMIT;

    EXIT WHEN chunkCountVar < checkCount;

    LOOP
      drop_table_if_exists('BATCH_IDN_SUBJECT_ENTITY_REVOKED_EVENT');
      EXECUTE IMMEDIATE
        'CREATE TABLE BATCH_IDN_SUBJECT_ENTITY_REVOKED_EVENT (RID ROWID PRIMARY KEY) NOLOGGING';

      EXECUTE IMMEDIATE
        'INSERT /*+ APPEND */ INTO BATCH_IDN_SUBJECT_ENTITY_REVOKED_EVENT (RID)
         SELECT RID
         FROM CHUNK_IDN_SUBJECT_ENTITY_REVOKED_EVENT
         WHERE ROWNUM <= :1'
        USING batchSize;

      batchCountVar := SQL%ROWCOUNT;
      COMMIT;

      EXIT WHEN batchCountVar = 0;

      EXECUTE IMMEDIATE
        'DELETE FROM IDN_SUBJECT_ENTITY_REVOKED_EVENT
         WHERE ROWID IN (SELECT RID FROM BATCH_IDN_SUBJECT_ENTITY_REVOKED_EVENT)';
      rowCount := SQL%ROWCOUNT;
      COMMIT;

      log_msg('BATCH DELETED (SUBJECT_ENTITY_REVOKED_EVENT): ' || rowCount);

      EXECUTE IMMEDIATE
        'DELETE FROM CHUNK_IDN_SUBJECT_ENTITY_REVOKED_EVENT
         WHERE RID IN (SELECT RID FROM BATCH_IDN_SUBJECT_ENTITY_REVOKED_EVENT)';
      COMMIT;

      IF rowCount > 0 THEN
        DBMS_LOCK.SLEEP(sleepTime);
      END IF;
    END LOOP;
  END LOOP;

  log_msg('DELETE DONE: IDN_SUBJECT_ENTITY_REVOKED_EVENT');

  log_msg('WSO2_NON_PERSISTENCE_ACCESS_TOKEN_CLEANUP_SP COMPLETED');

EXCEPTION
  WHEN OTHERS THEN
    IF enableLog THEN
      log_msg('ERROR: ' || SQLERRM);
    END IF;
    RAISE;
END;
