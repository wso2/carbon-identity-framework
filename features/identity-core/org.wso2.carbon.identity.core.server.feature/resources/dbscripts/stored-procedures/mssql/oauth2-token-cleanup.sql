DROP PROCEDURE IF EXISTS WSO2_TOKEN_CLEANUP_SP;

CREATE PROCEDURE WSO2_TOKEN_CLEANUP_SP

AS
BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE @batchSize INT;
DECLARE @backupTables BIT;
DECLARE @sleepTime FLOAT;
DECLARE @safePeriod INT;
DECLARE @deleteTimeLimit DATETIME;
DECLARE @rowCount INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);
DECLARE @enableAudit BIT;

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
SET @backupTables = 'TRUE';    -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE     [DEFAULT : TRUE]
SET @sleepTime = 2;          -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
SET @safePeriod = 2;         -- SET SAFE PERIOD OF HOURS FOR TOKEN DELETE, SINCE TOKENS COULD BE CASHED    [DEFAULT : 2]
SET @deleteTimeLimit = DATEADD(HOUR, -(@safePeriod), GetUTCDate());    -- SET CURRENT TIME - safePeriod FOR BEGIN THE TOKEN DELETE
SET @rowCount = 0;
SET @enableLog = 'TRUE';       -- ENABLE LOGGING [DEFAULT : FALSE]
SET @logLevel = 'DEBUG';    -- SET LOG LEVELS : TRACE , DEBUG
SET @enableAudit = 'FALSE';


-- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_ACCESS_TOKEN TABLE
-- ------------------------------------------------------
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_OAUTH2_ACCESS_TOKEN_BAK'))
BEGIN
    DROP TABLE dbo.IDN_OAUTH2_ACCESS_TOKEN_BAK;
END

IF (@backupTables = 1)
BEGIN
    SELECT * INTO IDN_OAUTH2_ACCESS_TOKEN_BAK FROM dbo.IDN_OAUTH2_ACCESS_TOKEN;
END

-- ------------------------------------------------------

-- ------------------------------------------------------
-- BACKUP IDN_OAUTH2_ACCESS_TOKEN TABLE
-- ------------------------------------------------------
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'IDN_OAUTH2_AUTHORIZATION_CODE_BAK'))
BEGIN
    DROP TABLE dbo.IDN_OAUTH2_AUTHORIZATION_CODE_BAK;
END

IF (@backupTables = 1)
BEGIN
    SELECT * INTO IDN_OAUTH2_AUTHORIZATION_CODE_BAK FROM dbo.IDN_OAUTH2_AUTHORIZATION_CODE;
END

-- ------------------------------------------------------

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN
-- ------------------------------------------------------
IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
    SELECT 'TOTAL TOKENS SHOULD BE DELETED FROM IDN_OAUTH2_ACCESS_TOKEN' as 'DEBUG LOG', COUNT_BIG(*) as 'COUNT' FROM dbo.IDN_OAUTH2_ACCESS_TOKEN WHERE TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR (TOKEN_STATE='ACTIVE' AND (@deleteTimeLimit > DATEADD(MINUTE, ((VALIDITY_PERIOD/1000)/60), TIME_CREATED) AND (@deleteTimeLimit > DATEADD(MINUTE, ((REFRESH_TOKEN_VALIDITY_PERIOD/1000)/60), REFRESH_TOKEN_TIME_CREATED))));
END

SELECT @rowCount = @batchSize;

WHILE @rowCount > 0
BEGIN
    DELETE TOP (@batchSize) FROM dbo.IDN_OAUTH2_ACCESS_TOKEN WHERE TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR (TOKEN_STATE='ACTIVE' AND (@deleteTimeLimit > DATEADD(MINUTE, ((VALIDITY_PERIOD/1000)/60), TIME_CREATED) AND (@deleteTimeLimit > DATEADD(MINUTE, ((REFRESH_TOKEN_VALIDITY_PERIOD/1000)/60), REFRESH_TOKEN_TIME_CREATED))));
    SELECT @rowCount = @@rowcount;
END

-- ------------------------------------------------------

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE
-- ------------------------------------------------------
IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
    SELECT 'TOTAL TOKENS ON SHOULD BE DELETED FROM IDN_OAUTH2_AUTHORIZATION_CODE' as 'DEBUG LOG', COUNT_BIG(*) as 'COUNT' FROM IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN token WHERE token.TOKEN_ID = code.TOKEN_ID) OR STATE NOT IN ('ACTIVE') OR @deleteTimeLimit > DATEADD(MINUTE, ((VALIDITY_PERIOD/1000)/60), TIME_CREATED) OR TOKEN_ID IS NULL;
END

SELECT @rowCount = @batchSize;

WHILE @rowCount > 0
BEGIN
    DELETE TOP (@batchSize) FROM IDN_OAUTH2_AUTHORIZATION_CODE where CODE_ID in ( SELECT * FROM (select CODE_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN token WHERE token.TOKEN_ID = code.TOKEN_ID) OR code.STATE NOT IN ('ACTIVE') OR @deleteTimeLimit > DATEADD(MINUTE, ((VALIDITY_PERIOD/1000)/60), TIME_CREATED) OR code.TOKEN_ID IS NULL) as x);
    SELECT @rowCount = @@rowcount;
END

-- ------------------------------------------------------

IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
    SELECT 'TOTAL TOKENS ON IDN_OAUTH2_ACCESS_TOKEN TABLE AFTER DELETE' AS 'DEBUG LOG', COUNT_BIG(*) as 'COUNT' FROM IDN_OAUTH2_ACCESS_TOKEN;
END

IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
    SELECT 'TOTAL TOKENS ON IDN_OAUTH2_AUTHORIZATION_CODE TABLE AFTER DELETE' AS 'DEBUG LOG', COUNT_BIG(*) as 'COUNT' FROM IDN_OAUTH2_AUTHORIZATION_CODE;
END

SELECT 'TOKEN_CLEANUP_SP COMPLETED .... !' AS 'INFO LOG';

-- ------------------------------------------------------

END
