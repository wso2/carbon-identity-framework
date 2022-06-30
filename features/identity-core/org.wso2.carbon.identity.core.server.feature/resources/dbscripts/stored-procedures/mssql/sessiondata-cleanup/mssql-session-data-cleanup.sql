CREATE PROCEDURE CLEANUP_SESSION_DATA AS
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
DECLARE @deletedFederatedSessionMappings INT;
DECLARE @deletedOperationalFederatedSessionMappings INT;
DECLARE @deletedStoreOperations INT;
DECLARE @deletedDeleteOperations INT;
DECLARE @sessionCleanupCount INT;
DECLARE @sessionMappingsCleanupCount INT;
DECLARE @operationalSessionMappingsCleanupCount INT;
DECLARE @sessionAppInfoCleanupCount INT;
DECLARE @operationalSessionAppInfoCleanupCount INT;
DECLARE @sessionMetadataCleanupCount INT;
DECLARE @operationalSessionMetadataCleanupCount INT;
DECLARE @sessionFederatedMappingsCleanupCount INT;
DECLARE @operationalFederatedSessionMappingsCleanupCount INT;
DECLARE @operationCleanupCount INT;
DECLARE @tracingEnabled INT;
DECLARE @sleepTime AS VARCHAR(12);
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
DECLARE @totalsession INT;
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
SET @deletedOperationalFederatedSessionMappings = 1;
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
SET @sessionCleanupTime = cast((DATEDIFF_BIG(millisecond, '1970-01-01 00:00:00', GETUTCDATE()) - (1209600000))AS DECIMAL) * 1000000
-- Operational data older than 720 minutes(12 h) will be removed.
SET @operationCleanupTime = cast((DATEDIFF_BIG(millisecond, '1970-01-01 00:00:00', GETUTCDATE()) - (720*60000))AS DECIMAL) * 1000000

SET @SQL_SAFE_UPDATES = 0;
SET @OLD_SQL_SAFE_UPDATES=@SQL_SAFE_UPDATES;


-- ------------------------------------------
-- REMOVE SESSION DATA
-- ------------------------------------------

SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP_SESSION_DATA() STARTED .... !' AS 'INFO LOG';

SELECT '[' + convert(varchar, getdate(), 121) + '] GIVEN CHUNK LIMIT ' AS 'PARAMETER', @chunkLimit AS 'VALUE', 'AND BATCH SIZE ' AS 'PARAMETER', @batchSize AS 'VALUE';

SELECT @totalsession = COUNT(1) FROM IDN_AUTH_SESSION_STORE where TIME_CREATED < @sessionCleanupTime;
SELECT 	'[' + convert(varchar, getdate(), 121) + '] TOTAL SESSION COUNT IN IDN_AUTH_SESSION_STORE TO BE REMOVED' AS 'INFO LOG', @totalsession;

-- CLEANUP ANY EXISTING TEMP TABLES
DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
DROP TABLE IF EXISTS TEMP_SESSION_BATCH;

-- RUN UNTILL
WHILE (@sessionCleanUpTempTableCount > 0)
BEGIN

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_AUTH_SESSION_STORE_TMP]') AND TYPE IN (N'U'))
BEGIN
CREATE TABLE IDN_AUTH_SESSION_STORE_TMP( SESSION_ID VARCHAR (100), SESSION_TYPE VARCHAR(100), TIME_CREATED BIGINT);
INSERT INTO IDN_AUTH_SESSION_STORE_TMP (SESSION_ID, SESSION_TYPE, TIME_CREATED) SELECT TOP (@chunkLimit) SESSION_ID, SESSION_TYPE, TIME_CREATED FROM IDN_AUTH_SESSION_STORE where TIME_CREATED < @sessionCleanupTime;
CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID, SESSION_TYPE, TIME_CREATED);
END

SELECT @sessionCleanUpTempTableCount = COUNT(1) FROM IDN_AUTH_SESSION_STORE_TMP;
SELECT '[' + convert(varchar, getdate(), 121) + '] TEMPORARY SESSION CLEANUP TASK SNAPSHOT TABLE CREATED...!!' AS 'INFO LOG';
SET @sessionCleanupCount = 1;

WHILE (@sessionCleanupCount > 0)
BEGIN

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[TEMP_SESSION_BATCH]') AND TYPE IN (N'U'))
BEGIN
CREATE TABLE TEMP_SESSION_BATCH( SESSION_ID VARCHAR (100), SESSION_TYPE VARCHAR(100), TIME_CREATED BIGINT);
INSERT INTO TEMP_SESSION_BATCH (SESSION_ID, SESSION_TYPE, TIME_CREATED) (SELECT TOP (@batchSize) SESSION_ID, SESSION_TYPE, TIME_CREATED FROM IDN_AUTH_SESSION_STORE_TMP);
END

DELETE A
FROM IDN_AUTH_SESSION_STORE AS A
INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID AND A.SESSION_TYPE = B.SESSION_TYPE AND A.TIME_CREATED = B.TIME_CREATED;
SET @sessionCleanupCount = @@ROWCOUNT;

SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED SESSION COUNT...!!' AS 'INFO LOG', @sessionCleanupCount AS 'COUNT';

-- Deleting user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_USER_SESSION_MAPPING'))
BEGIN
    DELETE A
    FROM IDN_AUTH_USER_SESSION_MAPPING AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @sessionMappingsCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED USER-SESSION MAPPINGS ...!!' AS 'INFO LOG', @sessionMappingsCleanupCount AS 'COUNT';

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedUserSessionMappings = @deletedUserSessionMappings + @sessionMappingsCleanupCount;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED USER-SESSION MAPPINGS: ' AS 'INFO LOG', @deletedUserSessionMappings AS 'NO OF DELETED ENTRIES';
    END;
END

-- Deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_APP_INFO'))
BEGIN
    DELETE A
    FROM IDN_AUTH_SESSION_APP_INFO AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @sessionAppInfoCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED SESSION APP INFO ...!!' AS 'INFO LOG', @sessionAppInfoCleanupCount AS 'COUNT';


    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedSessionAppInfo = @deletedSessionAppInfo + @sessionAppInfoCleanupCount;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED SESSION APP INFO: ' AS 'INFO LOG', @deletedSessionAppInfo AS 'NO OF DELETED ENTRIES';
    END;
END

-- Deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_META_DATA'))
BEGIN
    DELETE A
    FROM IDN_AUTH_SESSION_META_DATA AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @sessionMetadataCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED SESSION METADATA ...!!' AS 'INFO LOG', @sessionMetadataCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedSessionMetadata = @deletedSessionMetadata + @sessionMetadataCleanupCount;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED SESSION METADATA: ' AS 'INFO LOG', @deletedSessionMetadata AS 'NO OF DELETED ENTRIES';
    END;
END

-- Deleting federated session mappings from 'IDN_FED_AUTH_SESSION_MAPPING' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_FED_AUTH_SESSION_MAPPING'))
BEGIN
    DELETE A
    FROM IDN_FED_AUTH_SESSION_MAPPING AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @sessionFederatedMappingsCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED FEDERATED SESSION MAPPINGS ...!!' AS 'INFO LOG', @sessionFederatedMappingsCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedFederatedSessionMappings = @deletedFederatedSessionMappings + @sessionFederatedMappingsCleanupCount;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED FEDERATED SESSION MAPPINGS: ' AS 'INFO LOG', @deletedFederatedSessionMappings AS 'NO OF DELETED ENTRIES';
    END;
END

DELETE A
FROM IDN_AUTH_SESSION_STORE_TMP AS A
INNER JOIN TEMP_SESSION_BATCH AS B
ON A.SESSION_ID = B.SESSION_ID AND A.SESSION_TYPE = B.SESSION_TYPE AND A.TIME_CREATED = B.TIME_CREATED;

SELECT '[' + convert(varchar, getdate(), 121) + '] END CLEANING UP IDS FROM TEMP SESSION DATA SNAPSHOT TABLE...!!' AS 'INFO LOG';

DROP TABLE TEMP_SESSION_BATCH;

IF (@tracingEnabled=1)
BEGIN
SET @deletedSessions = @deletedSessions + @sessionCleanupCount;
SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED SESSIONS: ' AS 'INFO LOG', @deletedSessions AS 'NO OF DELETED ENTRIES';
END;

END;

-- DROP THE CHUNK TO MOVE ON TO THE NEXT CHUNK IN THE SNAPSHOT TABLE.
DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;

-- Sleep for some time letting other threads to run.
WAITFOR DELAY @sleepTime;

END;

IF (@tracingEnabled=1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedSessions AS 'TOTAL NO OF DELETED ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION RECORDS REMOVED FROM IDN_AUTH_USER_SESSION_MAPPING: ' AS 'INFO LOG', @deletedUserSessionMappings AS 'TOTAL NO OF DELETED ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_APP_INFO: ' AS 'INFO LOG', @deletedSessionAppInfo AS 'TOTAL NO OF DELETED ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_META_DATA: ' AS 'INFO LOG', @deletedSessionMetadata AS 'TOTAL NO OF DELETED ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION RECORDS REMOVED FROM IDN_FED_AUTH_SESSION_MAPPING: ' AS 'INFO LOG', @deletedFederatedSessionMappings AS 'TOTAL NO OF DELETED ENTRIES';
END;

SELECT '[' + convert(varchar, getdate(), 121) + '] SESSION_CLEANUP_TASK ENDED .... !' AS 'INFO LOG';

-- --------------------------------------------
-- REMOVE OPERATIONAL DATA
-- --------------------------------------------

SELECT '[' + convert(varchar, getdate(), 121) + '] OPERATION_CLEANUP_TASK STARTED .... !' AS 'INFO LOG';
SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE STARTED .... ' AS 'INFO LOG';

DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;
DROP TABLE IF EXISTS TEMP_SESSION_BATCH;

-- RUN UNTILL
WHILE (@operationCleanUpTempTableCount > 0)
BEGIN

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[IDN_AUTH_SESSION_STORE_TMP]') AND TYPE IN (N'U'))
BEGIN
CREATE TABLE IDN_AUTH_SESSION_STORE_TMP( SESSION_ID VARCHAR (100), SESSION_TYPE VARCHAR(100));
INSERT INTO IDN_AUTH_SESSION_STORE_TMP (SESSION_ID,SESSION_TYPE) SELECT TOP (@chunkLimit) SESSION_ID,SESSION_TYPE FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = 'DELETE' AND TIME_CREATED < @operationCleanupTime;
CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID)
END

SELECT @operationCleanUpTempTableCount = COUNT(1) FROM IDN_AUTH_SESSION_STORE_TMP;
SELECT '[' + convert(varchar, getdate(), 121) + '] TEMPORARY SESSION CLEANUP TASK SNAPSHOT TABLE CREATED...!!' AS 'INFO LOG', @operationCleanUpTempTableCount;
SET @operationCleanupCount = 1;

WHILE (@operationCleanupCount > 0)
BEGIN

IF NOT  EXISTS (SELECT * FROM SYS.OBJECTS WHERE OBJECT_ID = OBJECT_ID(N'[DBO].[TEMP_SESSION_BATCH]') AND TYPE IN (N'U'))
BEGIN
CREATE TABLE TEMP_SESSION_BATCH( SESSION_ID VARCHAR (100),SESSION_TYPE VARCHAR(100));
INSERT INTO TEMP_SESSION_BATCH (SESSION_ID,SESSION_TYPE) SELECT TOP (@batchSize) SESSION_ID,SESSION_TYPE FROM IDN_AUTH_SESSION_STORE_TMP;
END

DELETE A
FROM IDN_AUTH_SESSION_STORE AS A
INNER JOIN TEMP_SESSION_BATCH AS B
ON A.SESSION_ID = B.SESSION_ID AND A.SESSION_TYPE = B.SESSION_TYPE;;
SET @operationCleanupCount = @@ROWCOUNT;

SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED STORE OPERATIONS COUNT...!!' AS 'INFO LOG', @operationCleanupCount;

-- Deleting session app info from 'IDN_AUTH_USER_SESSION_MAPPING' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_USER_SESSION_MAPPING'))
BEGIN
    DELETE A
    FROM IDN_AUTH_USER_SESSION_MAPPING AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @operationalSessionMappingsCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED OPERATION RELATED USER-SESSION MAPPINGS ...!!' AS 'INFO LOG', @operationalSessionMappingsCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedOperationalUserSessionMappings = @operationalSessionMappingsCleanupCount + @deletedOperationalUserSessionMappings;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED USER-SESSION MAPPING RECORDS: ' AS 'INFO LOG', @deletedOperationalUserSessionMappings AS 'NO OF DELETED STORE ENTRIES';
    END;
END

-- Deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_APP_INFO'))
BEGIN
    DELETE A
    FROM IDN_AUTH_SESSION_APP_INFO AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @operationalSessionAppInfoCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED SESSION APP INFO ...!!' AS 'INFO LOG', @operationalSessionAppInfoCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedOperationalSessionAppInfo = @operationalSessionAppInfoCleanupCount + @deletedOperationalSessionAppInfo;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED SESSION APP INFO RECORDS: ' AS 'INFO LOG', @deletedOperationalSessionAppInfo AS 'NO OF DELETED STORE ENTRIES';
    END;
END

-- Deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_AUTH_SESSION_META_DATA'))
BEGIN
    DELETE A
    FROM IDN_AUTH_SESSION_META_DATA AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @operationalSessionMetadataCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED SESSION METADATA ...!!' AS 'INFO LOG', @operationalSessionMetadataCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedOperationalSessionMetadata = @operationalSessionMetadataCleanupCount + @deletedOperationalSessionMetadata;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED SESSION METADATA RECORDS: ' AS 'INFO LOG', @deletedOperationalSessionMetadata AS 'NO OF DELETED STORE ENTRIES';
    END;
END

-- Deleting federated session mappings from 'IDN_FED_AUTH_SESSION_MAPPING' table
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_FED_AUTH_SESSION_MAPPING'))
BEGIN
    DELETE A
    FROM IDN_FED_AUTH_SESSION_MAPPING AS A
             INNER JOIN TEMP_SESSION_BATCH AS B ON A.SESSION_ID = B.SESSION_ID;
    SET @operationalFederatedSessionMappingsCleanupCount = @@ROWCOUNT;
    SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED FEDERATED SESSION MAPPINGS ...!!' AS 'INFO LOG', @operationalSessionMappingsCleanupCount;

    IF (@tracingEnabled=1)
    BEGIN
        SET @deletedOperationalFederatedSessionMappings = @deletedOperationalFederatedSessionMappings + @operationalFederatedSessionMappingsCleanupCount;
        SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED FEDERATED SESSION MAPPING RECORDS: ' AS 'INFO LOG', @deletedOperationalFederatedSessionMappings AS 'NO OF DELETED STORE ENTRIES';
    END;
END

IF (@tracingEnabled=1)
BEGIN
SET @deletedDeleteOperations = @operationCleanupCount + @deletedDeleteOperations;
SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED DELETE OPERATION RECORDS: ' AS 'INFO LOG', @deletedDeleteOperations AS 'NO OF DELETED DELETE ENTRIES';
END;

DELETE A
FROM IDN_AUTH_SESSION_STORE_TMP AS A
INNER JOIN TEMP_SESSION_BATCH AS B
ON A.SESSION_ID = B.SESSION_ID AND A.SESSION_TYPE = B.SESSION_TYPE;;

SELECT '[' + convert(varchar, getdate(), 121) + '] ENDED CLEANING UP IDS FROM TEMP OPERATIONAL DATA SNAPSHOT TABLE...!!' AS 'INFO LOG';

IF (@tracingEnabled=1)
BEGIN
SET @deletedStoreOperations = @operationCleanupCount + @deletedStoreOperations;
SELECT '[' + convert(varchar, getdate(), 121) + '] REMOVED STORE OPERATION RECORDS: ' AS 'INFO LOG', @deletedStoreOperations AS 'NO OF DELETED STORE ENTRIES';
END;

DROP TABLE TEMP_SESSION_BATCH;

END;
-- Sleep for some time letting other threads to run.
WAITFOR DELAY @sleepTime;
END;

DROP TABLE IF EXISTS IDN_AUTH_SESSION_STORE_TMP;

SELECT '[' + convert(varchar, getdate(), 121) + '] FLAG SET TO INDICATE END OF CLEAN UP TASK...!!' AS 'INFO LOG';

IF (@tracingEnabled=1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] STORE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedStoreOperations  AS 'TOTAL NO OF DELETED STORE ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] DELETE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedDeleteOperations AS 'TOTAL NO OF DELETED DELETE ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_USER_SESSION_MAPPING: ' AS 'INFO LOG', @deletedOperationalUserSessionMappings AS 'TOTAL NO OF DELETED DELETE ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_APP_INFO: ' AS 'INFO LOG', @deletedOperationalSessionAppInfo AS 'TOTAL NO OF DELETED DELETE ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_META_DATA: ' AS 'INFO LOG', @deletedOperationalSessionMetadata AS 'TOTAL NO OF DELETED DELETE ENTRIES';
SELECT '[' + convert(varchar, getdate(), 121) + '] DELETE OPERATION RELATED SESSION RECORDS REMOVED FROM IDN_FED_AUTH_SESSION_MAPPING: ' AS 'INFO LOG', @deletedOperationalFederatedSessionMappings AS 'TOTAL NO OF DELETED DELETE ENTRIES';
END;

SET @SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP_SESSION_DATA() ENDED .... !' AS 'INFO LOG';

END;
