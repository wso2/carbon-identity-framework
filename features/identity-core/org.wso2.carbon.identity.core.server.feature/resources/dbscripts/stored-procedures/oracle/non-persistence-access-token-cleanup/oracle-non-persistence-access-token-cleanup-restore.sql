-- ------------------------------------------
-- Procedure to restore the data from backup tables to the original tables deleted by WSO2_NON_PERSISTENCE_ACCESS_TOKEN_CLEANUP_SP.
-- !!! Note: Use this procedure only if you are using non-persistent access tokens.
-- ------------------------------------------

CREATE OR REPLACE PROCEDURE WSO2_NON_PERSISTENCE_ACCESS_TOKEN_DATA_RESTORE_SP IS

  rowCount        PLS_INTEGER := 0;
  enableLog       BOOLEAN     := TRUE;
  logLevel        VARCHAR2(10):= 'TRACE';
  current_schema  VARCHAR2(128);
  tableCount      PLS_INTEGER := 0;

  PROCEDURE log_msg(p_msg VARCHAR2) IS
  BEGIN
    IF enableLog THEN
      DBMS_OUTPUT.PUT_LINE(p_msg);
    END IF;
  END;

BEGIN
  SELECT SYS_CONTEXT('USERENV','CURRENT_SCHEMA') INTO current_schema FROM dual;

  IF enableLog THEN
    log_msg('NON_PERSISTENCE_ACCESS_TOKEN CLEANUP DATA RESTORATION STARTED .... !');
  END IF;

  -- ---------------------
  -- Restore IDN_OAUTH2_REFRESH_TOKEN
  -- ---------------------
  SELECT COUNT(*) INTO tableCount
  FROM ALL_TABLES
  WHERE OWNER = current_schema
    AND TABLE_NAME = 'BAK_OAUTH2_REFRESH_TOKEN';

  IF tableCount = 1 THEN
    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('CLEANUP DATA RESTORATION STARTED ON IDN_OAUTH2_REFRESH_TOKEN TABLE !');
    END IF;

    INSERT INTO IDN_OAUTH2_REFRESH_TOKEN
    SELECT A.*
    FROM BAK_OAUTH2_REFRESH_TOKEN A
    LEFT JOIN IDN_OAUTH2_REFRESH_TOKEN B
      ON A.REFRESH_TOKEN_ID = B.REFRESH_TOKEN_ID
    WHERE B.REFRESH_TOKEN_ID IS NULL;

    rowCount := SQL%ROWCOUNT;
    COMMIT;

    IF enableLog THEN
      log_msg('CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH2_REFRESH_TOKEN WITH ' || rowCount || ' rows');
    END IF;
  END IF;

  -- ---------------------
  -- Restore IDN_OAUTH2_REFRESH_TOKEN_SCOPE
  -- ---------------------
  SELECT COUNT(*) INTO tableCount
  FROM ALL_TABLES
  WHERE OWNER = current_schema
    AND TABLE_NAME = 'BAK_OAUTH2_REFRESH_TOKEN_SCOPE';

  IF tableCount = 1 THEN
    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('CLEANUP DATA RESTORATION STARTED ON IDN_OAUTH2_REFRESH_TOKEN_SCOPE TABLE !');
    END IF;

    INSERT INTO IDN_OAUTH2_REFRESH_TOKEN_SCOPE
    SELECT A.*
    FROM BAK_OAUTH2_REFRESH_TOKEN_SCOPE A
    LEFT JOIN IDN_OAUTH2_REFRESH_TOKEN_SCOPE B
      ON A.REFRESH_TOKEN_ID = B.REFRESH_TOKEN_ID
      AND A.TOKEN_SCOPE = B.TOKEN_SCOPE
    WHERE B.REFRESH_TOKEN_ID IS NULL;

    rowCount := SQL%ROWCOUNT;
    COMMIT;

    IF enableLog THEN
      log_msg('CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH2_REFRESH_TOKEN_SCOPE WITH ' || rowCount || ' rows');
    END IF;
  END IF;

  -- ---------------------
  -- Restore IDN_OAUTH2_REVOKED_TOKENS
  -- ---------------------
  SELECT COUNT(*) INTO tableCount
  FROM ALL_TABLES
  WHERE OWNER = current_schema
    AND TABLE_NAME = 'BAK_OAUTH2_REVOKED_TOKENS';

  IF tableCount = 1 THEN
    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('CLEANUP DATA RESTORATION STARTED ON IDN_OAUTH2_REVOKED_TOKENS TABLE !');
    END IF;

    INSERT INTO IDN_OAUTH2_REVOKED_TOKENS
    SELECT A.*
    FROM BAK_OAUTH2_REVOKED_TOKENS A
    LEFT JOIN IDN_OAUTH2_REVOKED_TOKENS B
      ON A.UUID = B.UUID
    WHERE B.UUID IS NULL;

    rowCount := SQL%ROWCOUNT;
    COMMIT;

    IF enableLog THEN
      log_msg('CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH2_REVOKED_TOKENS WITH ' || rowCount || ' rows');
    END IF;
  END IF;

  -- ---------------------
  -- Restore IDN_SUBJECT_ENTITY_REVOKED_EVENT
  -- ---------------------
  SELECT COUNT(*) INTO tableCount
  FROM ALL_TABLES
  WHERE OWNER = current_schema
    AND TABLE_NAME = 'BAK_SUBJECT_ENTITY_REVOKED_EVENT';

  IF tableCount = 1 THEN
    IF enableLog AND logLevel = 'TRACE' THEN
      log_msg('CLEANUP DATA RESTORATION STARTED ON IDN_SUBJECT_ENTITY_REVOKED_EVENT TABLE !');
    END IF;

    INSERT INTO IDN_SUBJECT_ENTITY_REVOKED_EVENT
    SELECT A.*
    FROM BAK_SUBJECT_ENTITY_REVOKED_EVENT A
    LEFT JOIN IDN_SUBJECT_ENTITY_REVOKED_EVENT B
      ON A.EVENT_ID = B.EVENT_ID
    WHERE B.EVENT_ID IS NULL;

    rowCount := SQL%ROWCOUNT;
    COMMIT;

    IF enableLog THEN
      log_msg('CLEANUP DATA RESTORATION COMPLETED ON IDN_SUBJECT_ENTITY_REVOKED_EVENT WITH ' || rowCount || ' rows');
    END IF;
  END IF;

  IF enableLog THEN
    log_msg('NON_PERSISTENCE_ACCESS_TOKEN CLEANUP DATA RESTORATION COMPLETED .... !');
  END IF;

EXCEPTION
  WHEN OTHERS THEN
    IF enableLog THEN
      log_msg('ERROR: ' || SQLERRM);
    END IF;
    RAISE;
END;
/
