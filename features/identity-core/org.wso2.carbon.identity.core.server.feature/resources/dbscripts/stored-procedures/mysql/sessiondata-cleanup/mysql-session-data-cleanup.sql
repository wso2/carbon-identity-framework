DROP PROCEDURE IF EXISTS `CLEANUP_SESSION_DATA`;

DELIMITER $$

CREATE PROCEDURE `CLEANUP_SESSION_DATA`()
  BEGIN

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE deletedSessions INT;
    DECLARE deletedUserSessionMappings INT;
    DECLARE deletedOperationUserSessionMappings INT;
    DECLARE deletedOperationSessionAppInfo INT;
    DECLARE deletedOperationalSessionMetadata INT;
    DECLARE deletedOperationFederatedSessionMappings INT;
    DECLARE deletedStoreOperations INT;
    DECLARE deletedDeleteOperations INT;
    DECLARE tracingEnabled BOOLEAN;
    DECLARE sleepTime FLOAT;
    DECLARE batchSize INT;
    DECLARE chunkLimit INT;

    DECLARE sessionCleanUpTempTableCount INT;
    DECLARE operationCleanUpTempTableCount INT;
    DECLARE cleanUpCompleted BOOLEAN;

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------

    SET batchSize = 5000;
    -- This defines the number of entries from IDN_AUTH_SESSION_STORE that are taken into a SNAPSHOT
    SET chunkLimit=1000000;
    SET @deletedSessions = 0;
    SET @deletedUserSessionMappings = 0;
    SET @deletedSessionAppInfo = 0;
    SET @deletedSessionMetadata = 0;
    SET @deletedFederatedSessionMappings = 0;
    SET @deletedOperationUserSessionMappings = 0;
    SET @deletedOperationSessionAppInfo = 0;
    SET @deletedOperationalSessionMetadata = 0;
    SET @deletedOperationFederatedSessionMappings = 0;
    SET @deletedStoreOperations = 0;
    SET @deletedDeleteOperations = 0;
    SET @sessionCleanupCount = 1;
    SET @sessionMappingsCleanupCount = 1;
    SET @sessionAppInfoCleanupCount = 1;
    SET @sessionMetadataCleanupCount = 1;
    SET @sessionFederatedMappingsCleanupCount = 1;
    SET @operationCleanupCount = 1;
    SET @operationalSessionMappingsCleanupCount = 1;
    SET @operationalAppInfoCleanupCount = 1;
    SET @operationalMetadataCleanupCount = 1;
    SET @operationalFederatedMappingsCleanupCount = 1;
    SET tracingEnabled = FALSE;	-- SET IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
    SET sleepTime = 2;          -- Sleep time in seconds.
    SET autocommit = 0;

    SET @sessionCleanUpTempTableCount = 1;
    SET @operationCleanUpTempTableCount = 1;
    SET cleanUpCompleted = FALSE;

    -- Session data older than 20160 minutes(14 days) will be removed.
    SET @sessionCleanupTime = unix_timestamp()*1000000000 - (20160*60000000000);
    -- Operational data older than 720 minutes(12 h) will be removed.
    SET @operationCleanupTime = unix_timestamp()*1000000000 - (720*60000000000);

    SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
    SET SQL_SAFE_UPDATES = 0;

    -- ------------------------------------------
    -- REMOVE SESSION DATA
    -- ------------------------------------------

    SELECT 'CLEANUP_SESSION_DATA() STARTED .... !' AS 'INFO LOG', NOW() AS 'STARTING TIMESTAMP';


    -- CLEANUP ANY EXISTING TEMP TABLES
    DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
    DROP TABLE IF EXISTS TEMP_SESSION_BATCH;
    COMMIT;

    -- RUN UNTILL
    WHILE (@sessionCleanUpTempTableCount > 0) DO

      CREATE TABLE IF NOT EXISTS IDN_AUTH_SESSION_STORE_TMP AS SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE where TIME_CREATED < @sessionCleanupTime limit chunkLimit;
      CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID);
      COMMIT;

      SELECT count(1) INTO @sessionCleanUpTempTableCount FROM IDN_AUTH_SESSION_STORE_TMP;
      SELECT 'TEMPORARY SESSION CLEANUP TASK SNAPSHOT TABLE CREATED...!!' AS 'INFO LOG', @sessionCleanUpTempTableCount;

      SET @sessionCleanupCount = 1;
      WHILE (@sessionCleanupCount > 0) DO

        CREATE TABLE IF NOT EXISTS TEMP_SESSION_BATCH AS SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP LIMIT batchSize;
        COMMIT;

        DELETE A
        FROM IDN_AUTH_SESSION_STORE AS A
          INNER JOIN TEMP_SESSION_BATCH AS B ON
                                               A.SESSION_ID = B.SESSION_ID;
        SET @sessionCleanupCount = row_count();
        COMMIT;

        SELECT 'DELETED SESSION COUNT...!!' AS 'INFO LOG', @sessionCleanupCount;

        -- Clean up session data from IDN_AUTH_USER_SESSION_MAPPING table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_USER_SESSION_MAPPING'))
        THEN
            DELETE A
            FROM IDN_AUTH_USER_SESSION_MAPPING AS A
                    INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @sessionMappingsCleanupCount = row_count();
            SET @deletedUserSessionMappings = @deletedUserSessionMappings + @sessionMappingsCleanupCount;
            COMMIT;
            SELECT 'DELETED USER-SESSION MAPPINGS...!!' AS 'INFO LOG', @sessionMappingsCleanupCount ;
        END IF;

        -- Clean up session data from IDN_AUTH_SESSION_APP_INFO table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_APP_INFO'))
        THEN
            DELETE A
            FROM IDN_AUTH_SESSION_APP_INFO AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @sessionAppInfoCleanupCount = row_count();
            SET @deletedSessionAppInfo = @deletedSessionAppInfo + @sessionAppInfoCleanupCount;
            COMMIT;
            SELECT 'DELETED SESSION APP INFO...!!' AS 'INFO LOG', @sessionAppInfoCleanupCount ;
        END IF;

        -- Clean up session data from IDN_AUTH_SESSION_META_DATA table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_META_DATA'))
        THEN
            DELETE A
            FROM IDN_AUTH_SESSION_META_DATA AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @sessionMetadataCleanupCount = row_count();
            SET @deletedSessionMetadata = @deletedSessionMetadata + @sessionMetadataCleanupCount;
            COMMIT;
            SELECT 'DELETED SESSION METADATA...!!' AS 'INFO LOG', @sessionMetadataCleanupCount ;
        END IF;

        -- Clean up federated session mappings from IDN_FED_AUTH_SESSION_MAPPING table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_FED_AUTH_SESSION_MAPPING'))
        THEN
            DELETE A
            FROM IDN_FED_AUTH_SESSION_MAPPING AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @sessionFederatedMappingsCleanupCount = row_count();
            SET @deletedFederatedSessionMappings = @deletedFederatedSessionMappings + @sessionFederatedMappingsCleanupCount;
            COMMIT;
            SELECT 'DELETED FEDERATED SESSION MAPPINGS...!!' AS 'INFO LOG', @sessionFederatedMappingsCleanupCount ;
        END IF;

        DELETE A
        FROM IDN_AUTH_SESSION_STORE_TMP AS A
          INNER JOIN TEMP_SESSION_BATCH AS B
            ON A.SESSION_ID = B.SESSION_ID;
        COMMIT;
        SELECT 'END CLEANING UP IDS FROM TEMP SESSION DATA SNAPSHOT TABLE...!!' AS 'INFO LOG' ;

        DROP TABLE TEMP_SESSION_BATCH;
        COMMIT;

        IF (tracingEnabled) THEN SET
        @deletedSessions = @deletedSessions + @sessionCleanupCount;
          SELECT 'REMOVED SESSIONS: ' AS 'INFO LOG', @deletedSessions AS 'NO OF DELETED ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED USER-SESSION MAPPINGS: ' AS 'INFO LOG', @deletedUserSessionMappings AS 'NO OF DELETED ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED SESSION APP INFO: ' AS 'INFO LOG', @deletedSessionAppInfo AS 'NO OF DELETED ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED SESSION METADATA: ' AS 'INFO LOG', @deletedSessionMetadata AS 'NO OF DELETED ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED FEDERATED SESSION MAPPINGS: ' AS 'INFO LOG', @deletedFederatedSessionMappings AS 'NO OF DELETED ENTRIES', NOW() AS 'TIMESTAMP';
        END IF;

        DO SLEEP(sleepTime);
        -- Sleep for some time letting other threads to run.
      END WHILE;

      -- DROP THE CHUNK TO MOVE ON TO THE NEXT CHUNK IN THE SNAPSHOT TABLE.
      DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
      COMMIT;

    END WHILE;

    IF (tracingEnabled)
    THEN
      SELECT 'SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedSessions AS 'TOTAL NO OF DELETED ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'SESSION RECORDS REMOVED FROM IDN_AUTH_USER_SESSION_MAPPING: ' AS 'INFO LOG', @deletedUserSessionMappings AS 'TOTAL NO OF DELETED ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_APP_INFO: ' AS 'INFO LOG', @deletedSessionAppInfo AS 'TOTAL NO OF DELETED ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_META_DATA: ' AS 'INFO LOG', @deletedSessionMetadata AS 'TOTAL NO OF DELETED ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'SESSION RECORDS REMOVED FROM IDN_FED_AUTH_SESSION_MAPPING: ' AS 'INFO LOG', @deletedFederatedSessionMappings AS 'TOTAL NO OF DELETED ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
    END IF;

    SELECT 'SESSION_CLEANUP_TASK ENDED .... !' AS 'INFO LOG';

    -- --------------------------------------------
    -- REMOVE OPERATIONAL DATA
    -- --------------------------------------------

    SELECT 'OPERATION_CLEANUP_TASK STARTED .... !' AS 'INFO LOG', NOW() AS 'STARTING TIMESTAMP';
    SELECT 'BATCH DELETE STARTED .... ' AS 'INFO LOG';

    DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
    DROP TABLE IF EXISTS TEMP_SESSION_BATCH;
    COMMIT;

    WHILE (@operationCleanUpTempTableCount > 0) DO

      CREATE TABLE IF NOT EXISTS IDN_AUTH_SESSION_STORE_TMP AS SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = 'DELETE' AND TIME_CREATED < @operationCleanupTime limit chunkLimit;
      CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID);
      COMMIT;

      SELECT count(1) INTO @operationCleanUpTempTableCount FROM IDN_AUTH_SESSION_STORE_TMP;
      SELECT 'TEMPORARY OPERATION CLEANUP SNAPSHOT TABLE CREATED...!!' AS 'INFO LOG', @operationCleanUpTempTableCount;

      SET @operationCleanupCount = 1;
      WHILE (@operationCleanupCount > 0) DO

        CREATE TABLE IF NOT EXISTS TEMP_SESSION_BATCH AS SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE_TMP LIMIT batchSize;
        COMMIT;

        DELETE A
        FROM IDN_AUTH_SESSION_STORE AS A
          INNER JOIN TEMP_SESSION_BATCH AS B
            ON A.SESSION_ID = B.SESSION_ID
               AND A.SESSION_TYPE = B.SESSION_TYPE;
        SET @operationCleanupCount = row_count();
        COMMIT;

        SELECT 'DELETED STORE OPERATIONS COUNT...!!' AS 'INFO LOG', @operationCleanupCount ;

        -- Clean up operational session data from IDN_AUTH_USER_SESSION_MAPPING table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_USER_SESSION_MAPPING'))
        THEN
            DELETE A
            FROM IDN_AUTH_USER_SESSION_MAPPING AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @operationalSessionMappingsCleanupCount = row_count();
            SET @deletedOperationUserSessionMappings = @operationalSessionMappingsCleanupCount + @deletedOperationUserSessionMappings;
            COMMIT;
            SELECT 'DELETED OPERATIONAL USER-SESSION MAPPINGS ...!!' AS 'INFO LOG', @operationalSessionMappingsCleanupCount ;
        END IF;

        -- Clean up operational session data from IDN_AUTH_SESSION_APP_INFO table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_APP_INFO'))
        THEN
            DELETE A
            FROM IDN_AUTH_SESSION_APP_INFO AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @operationalAppInfoCleanupCount = row_count();
            SET @deletedOperationSessionAppInfo = @operationalAppInfoCleanupCount + @deletedOperationSessionAppInfo;
            COMMIT;
            SELECT 'DELETED OPERATIONAL SESSION APP INFO ...!!' AS 'INFO LOG', @operationalAppInfoCleanupCount ;
        END IF;

        -- Clean up operational session data from IDN_AUTH_SESSION_META_DATA table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_META_DATA'))
        THEN
            DELETE A
            FROM IDN_AUTH_SESSION_META_DATA AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @operationalMetadataCleanupCount = row_count();
            SET @deletedOperationalSessionMetadata = @operationalMetadataCleanupCount + @deletedOperationalSessionMetadata;
            COMMIT;
            SELECT 'DELETED OPERATIONAL SESSION METADATA ...!!' AS 'INFO LOG', @operationalMetadataCleanupCount ;
        END IF;

        -- Clean up operational session data from IDN_FED_AUTH_SESSION_MAPPING table.
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_FED_AUTH_SESSION_MAPPING'))
        THEN
            DELETE A
            FROM IDN_FED_AUTH_SESSION_MAPPING AS A
                     INNER JOIN TEMP_SESSION_BATCH AS B ON
                    A.SESSION_ID = B.SESSION_ID;
            SET @operationalFederatedMappingsCleanupCount = row_count();
            SET @deletedOperationFederatedSessionMappings = @operationalFederatedMappingsCleanupCount + @deletedOperationFederatedSessionMappings;
            COMMIT;
            SELECT 'DELETED OPERATIONAL FEDERATED AUTH MAPPINGS ...!!' AS 'INFO LOG', @operationalFederatedMappingsCleanupCount ;
        END IF;

        IF (tracingEnabled)
        THEN
          SET @deletedDeleteOperations = @operationCleanupCount + @deletedDeleteOperations;
          SELECT 'REMOVED DELETE OPERATION RECORDS: ' AS 'INFO LOG', @deletedDeleteOperations AS 'NO OF DELETED DELETE ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED USER-SESSION MAPPINGS RECORDS: ' AS 'INFO LOG', @deletedOperationUserSessionMappings AS 'NO OF DELETED DELETE ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED SESSION APP INFO RECORDS: ' AS 'INFO LOG', @deletedOperationSessionAppInfo AS 'NO OF DELETED DELETE ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED SESSION METADATA RECORDS: ' AS 'INFO LOG', @deletedOperationalSessionMetadata AS 'NO OF DELETED DELETE ENTRIES', NOW() AS 'TIMESTAMP';
          SELECT 'REMOVED FEDERATED SESSION RECORDS: ' AS 'INFO LOG', @deletedOperationFederatedSessionMappings AS 'NO OF DELETED DELETE ENTRIES', NOW() AS 'TIMESTAMP';
        END IF;


        DELETE A
        FROM IDN_AUTH_SESSION_STORE_TMP AS A
          INNER JOIN TEMP_SESSION_BATCH AS B
            ON A.SESSION_ID = B.SESSION_ID;
        COMMIT;

        SELECT 'ENDED CLEANING UP IDS FROM TEMP OPERATIONAL DATA SNAPSHOT TABLE...!!' AS 'INFO LOG' ;

        IF (tracingEnabled)
        THEN
          SET @deletedStoreOperations = @operationCleanupCount + @deletedStoreOperations;
          SELECT 'REMOVED STORE OPERATION RECORDS: ' AS 'INFO LOG', @deletedStoreOperations AS 'NO OF DELETED STORE ENTRIES', NOW() AS 'TIMESTAMP';
        END IF;

        DROP TABLE TEMP_SESSION_BATCH;
        COMMIT;
        DO SLEEP(sleepTime);   -- Sleep for some time letting other threads to run.
      END WHILE;

      DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
      COMMIT;
    END WHILE;

    SELECT 'FLAG SET TO INDICATE END OF CLEAN UP TASK...!!' AS 'INFO LOG' ;

    IF (tracingEnabled)
    THEN
      SELECT 'STORE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedStoreOperations  AS 'TOTAL NO OF DELETED STORE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'DELETE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedDeleteOperations AS 'TOTAL NO OF DELETED DELETE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_USER_SESSION_MAPPING: ' AS 'INFO LOG', @deletedOperationUserSessionMappings AS 'TOTAL NO OF DELETED DELETE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_APP_INFO: ' AS 'INFO LOG', @deletedOperationSessionAppInfo AS 'TOTAL NO OF DELETED DELETE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_META_DATA: ' AS 'INFO LOG', @deletedOperationalSessionMetadata AS 'TOTAL NO OF DELETED DELETE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
      SELECT 'DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_FED_AUTH_SESSION_MAPPING: ' AS 'INFO LOG', @deletedOperationFederatedSessionMappings AS 'TOTAL NO OF DELETED DELETE ENTRIES', NOW() AS 'COMPLETED_TIMESTAMP';
    END IF;

    SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

    SELECT 'CLEANUP_SESSION_DATA() ENDED .... !' AS 'INFO LOG', NOW() AS 'ENDING TIMESTAMP';

  END$$

DELIMITER ;
