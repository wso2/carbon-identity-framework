CREATE OR REPLACE PROCEDURE SESSION_CLEANUP_SP

BEGIN
	-- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
	DECLARE @deletedSessions INT;
	DECLARE @deletedUserSessionMappings INT;
	DECLARE @deletedOperationalUserSessionMappings INT;
	DECLARE @deletedSessionAppInfo INT;
	DECLARE @deletedOperationalSessionAppInfo INT;
	DECLARE @deletedSessionMetadata INT;
	DECLARE @deletedOperationalSessionMetadata INT;
	DECLARE @deletedStoreOperations INT;
	DECLARE @deletedDeleteOperations INT;
	DECLARE @sessionCleanupCount INT;
	DECLARE @sessionMappingsCleanupCount INT;
	DECLARE @operationalSessionMappingsCleanupCount INT;
	DECLARE @sessionAppInfoCleanupCount INT;
	DECLARE @operationalSessionAppInfoCleanupCount INT;
	DECLARE @sessionMetadataCleanupCount INT;
	DECLARE @operationalSessionMetadataCleanupCount INT;
	DECLARE @operationCleanupCount INT;
	DECLARE @sessionFederatedMappingsCleanupCount INT;
	DECLARE @operationalFederatedSessionMappingsCleanupCount INT;
	DECLARE @tracingEnabled INT;
	DECLARE @sleepTime VARCHAR(8);
	DECLARE @batchSize INT;
	DECLARE @chunkLimit INT;

	DECLARE @sessionCleanUpTempTableCount INT;
	DECLARE @operationCleanUpTempTableCount INT;
	DECLARE @cleanUpCompleted INT;
	DECLARE @autocommit INT;
	DECLARE @sessionCleanupTime bigint;
	DECLARE @operationCleanupTime bigint;
	DECLARE @OLD_SQL_SAFE_UPDATES INT;
	DECLARE @SQL_SAFE_UPDATES INT;

	DECLARE @message VARCHAR(32672);
	DECLARE @status integer default 0;

	-- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
	SET @batchSize = 5000;
	-- This defines the number of entries from IDN_AUTH_SESSION_STORE that are taken into a SNAPSHOT
	SET @chunkLimit=1000000;
	SET @deletedSessions = 0;
	SET @deletedUserSessionMappings = 0;
	SET @deletedOperationalUserSessionMappings = 0;
	SET @deletedSessionAppInfo = 0;
	SET @deletedOperationalSessionAppInfo = 0;
	SET @deletedSessionMetadata = 0;
	SET @deletedOperationalSessionMetadata = 0;
	SET @deletedFederatedSessionMappings = 0;
	SET @deletedOperationalFederatedSessionMappings = 0;
	SET @deletedStoreOperations = 0;
	SET @deletedDeleteOperations = 0;
	SET @sessionCleanupCount = 1;
	SET @sessionMappingsCleanupCount = 1;
	SET @operationalSessionMappingsCleanupCount = 1;
	SET @sessionAppInfoCleanupCount = 1;
	SET @operationalSessionAppInfoCleanupCount = 1;
	SET @sessionMetadataCleanupCount = 1;
	SET @operationalSessionMetadataCleanupCount = 1;
	SET @sessionFederatedMappingsCleanupCount = 1;
	SET @operationalFederatedSessionMappingsCleanupCount = 1;
	SET @operationCleanupCount = 1;
	SET @tracingEnabled = 1;	-- SET IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
	SET @sleepTime = '00:00:02.000';          -- Sleep time in seconds.
	SET @autocommit = 0;

	SET @sessionCleanUpTempTableCount = 1;
	SET @operationCleanUpTempTableCount = 1;
	SET @cleanUpCompleted = 1;

	-- Session data older than 20160 minutes(14 days) will be removed.
	SET @sessionCleanupTime = BIGINT(CURRENT TIMESTAMP - 14 DAYS);
	-- Operational data older than 720 minutes(12 h) will be removed.
	SET @operationCleanupTime = BIGINT(CURRENT TIMESTAMP - (12) HOUR);

	SET @SQL_SAFE_UPDATES = 0;
	SET @OLD_SQL_SAFE_UPDATES=@SQL_SAFE_UPDATES;

	-- ------------------------------------------
	-- REMOVE SESSION DATA
	-- ------------------------------------------

	-- CLEANUP ANY EXISTING TEMP TABLES
	DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
	DROP TABLE IF EXISTS TEMP_SESSION_BATCH;
	COMMIT;

	-- RUN UNTIL
	WHILE (@sessionCleanUpTempTableCount > 0) DO
		IF NOT (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_STORE_TMP')) THEN
		    CREATE TABLE IDN_AUTH_SESSION_STORE_TMP AS (SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE) WITH NO DATA;
			INSERT INTO IDN_AUTH_SESSION_STORE_TMP SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < @sessionCleanupTime LIMIT @chunkLimit;
		 	CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID);
		 	COMMIT;
		END IF;
		SELECT count(1) INTO @sessionCleanUpTempTableCount FROM IDN_AUTH_SESSION_STORE_TMP;
		SET @sessionCleanupCount = 1;

		WHILE (@sessionCleanupCount > 0) DO
			IF NOT (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'TEMP_SESSION_BATCH')) THEN
				CREATE TABLE TEMP_SESSION_BATCH AS (SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP) WITH NO DATA;
				INSERT INTO TEMP_SESSION_BATCH SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP LIMIT @batchSize;
				COMMIT;
			END IF;
			DELETE FROM IDN_AUTH_SESSION_STORE WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);
			GET DIAGNOSTICS @sessionCleanupCount = ROW_COUNT;

			-- Deleting user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_USER_SESSION_MAPPING') THEN
			    DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);
			    GET DIAGNOSTICS @sessionMappingsCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_APP_INFO') THEN
				DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);
			    GET DIAGNOSTICS @sessionAppInfoCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_META_DATA') THEN
				DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);
			    GET DIAGNOSTICS @sessionMetadataCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting federation auth session mappings from 'IDN_FED_AUTH_SESSION_MAPPING' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_FED_AUTH_SESSION_MAPPING') THEN
				DELETE FROM IDN_FED_AUTH_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);
				GET DIAGNOSTICS @sessionFederatedMappingsCleanupCount = ROW_COUNT;
			END IF;

			DELETE FROM IDN_AUTH_SESSION_STORE_TMP WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH);

			DROP TABLE TEMP_SESSION_BATCH;
		END WHILE;
		CALL dbms_alert.waitone('sleeping',@message,@status,@sleepTime);
	END WHILE;

	-- DROP THE CHUNK TO MOVE ON TO THE NEXT CHUNK IN THE SNAPSHOT TABLE.
	DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;

	-- --------------------------------------------
	-- REMOVE OPERATIONAL DATA
	-- --------------------------------------------

	DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
	DROP TABLE IF EXISTS TEMP_SESSION_BATCH;
	COMMIT;

	-- RUN UNTIL
	WHILE (@operationCleanUpTempTableCount > 0) DO
		IF NOT (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_STORE_TMP2')) THEN
			CREATE TABLE IDN_AUTH_SESSION_STORE_TMP2 AS (SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE) WITH NO DATA;
			INSERT INTO IDN_AUTH_SESSION_STORE_TMP2 SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE WHERE TIME_CREATED < @operationCleanupTime LIMIT @chunkLimit;
		 	CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP2 (SESSION_ID);
		 	COMMIT;
		END IF;

		SELECT count(1) INTO @operationCleanUpTempTableCount FROM IDN_AUTH_SESSION_STORE_TMP2;
		SET @operationCleanupCount = 1;

		WHILE (@operationCleanupCount > 0) DO
			IF NOT (EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'TEMP_SESSION_BATCH2')) THEN
				CREATE TABLE TEMP_SESSION_BATCH2 AS (SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE_TMP2) WITH NO DATA;
				INSERT INTO TEMP_SESSION_BATCH2 SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE_TMP2 LIMIT @batchSize;
				COMMIT;
			END IF;

			DELETE FROM IDN_AUTH_SESSION_STORE WHERE EXISTS(SELECT 1 FROM TEMP_SESSION_BATCH2 WHERE IDN_AUTH_SESSION_STORE.SESSION_ID = TEMP_SESSION_BATCH2.SESSION_ID AND IDN_AUTH_SESSION_STORE.SESSION_TYPE = TEMP_SESSION_BATCH2.SESSION_TYPE);
			GET DIAGNOSTICS @operationCleanupCount = ROW_COUNT;

			-- Deleting session app info from 'IDN_AUTH_USER_SESSION_MAPPING' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_USER_SESSION_MAPPING') THEN
			    DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH2);
			    GET DIAGNOSTICS @operationalSessionMappingsCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_APP_INFO') THEN
				DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH2);
			    GET DIAGNOSTICS @operationalSessionAppInfoCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_AUTH_SESSION_META_DATA') THEN
				DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH2);
			    GET DIAGNOSTICS @operationalSessionMetadataCleanupCount = ROW_COUNT;
			END IF;

			-- Deleting federated session mappings from 'IDN_FED_AUTH_SESSION_MAPPING' table
			IF EXISTS (SELECT NAME FROM SYSIBM.SYSTABLES WHERE NAME= 'IDN_FED_AUTH_SESSION_MAPPING') THEN
			    DELETE FROM IDN_FED_AUTH_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH2);
			    GET DIAGNOSTICS @operationalFederatedSessionMappingsCleanupCount = ROW_COUNT;
			END IF;

			DELETE FROM IDN_AUTH_SESSION_STORE_TMP2 WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH2);
			DROP TABLE TEMP_SESSION_BATCH2;

		END WHILE;
		CALL dbms_alert.waitone('sleeping',@message,@status,@sleepTime);
	END WHILE;

	DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP2;

	SET @SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;
END
