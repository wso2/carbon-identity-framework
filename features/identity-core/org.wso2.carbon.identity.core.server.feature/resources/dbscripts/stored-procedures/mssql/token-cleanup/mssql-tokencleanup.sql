CREATE PROCEDURE WSO2_TOKEN_CLEANUP_SP
AS

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE @batchSize INT;
DECLARE @chunkSize INT;
DECLARE @checkCount INT;
DECLARE @backupTables BIT;
DECLARE @sleepTime AS VARCHAR(12);
DECLARE @safePeriod INT;
DECLARE @deleteTimeLimit DATETIME;
DECLARE @rowCount INT;
DECLARE @cleaupCount INT;
DECLARE @maxValidityPeriod bigint;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);
DECLARE @enableAudit BIT;
DECLARE @cusrBackupTable VARCHAR(100);
DECLARE @SQL NVARCHAR(MAX);
DECLARE @backupTable VARCHAR(100);
DECLARE @chunkCount INT;
DECLARE @batchCount INT;
DECLARE @deleteCount INT;
DECLARE @rebuildIndexes BIT;
DECLARE @updateStats BIT;

SET @maxValidityPeriod = 99999999999990;

DECLARE backupTablesCursor CURSOR FOR
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE
TABLE_NAME IN ('IDN_OAUTH2_ACCESS_TOKEN', 'IDN_OAUTH2_AUTHORIZATION_CODE', 'IDN_OAUTH2_ACCESS_TOKEN_SCOPE','IDN_OIDC_REQ_OBJECT_REFERENCE','IDN_OIDC_REQ_OBJECT_CLAIMS','IDN_OIDC_REQ_OBJ_CLAIM_VALUES')

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
SET @chunkSize = 500000;      -- CHUNK WISE DELETE FOR LARGE TABLES [DEFULT : 500000]
SET @checkCount = 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE TOKENS COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
SET @backupTables = 'TRUE';    -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE  [DEFAULT : TRUE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
SET @sleepTime = '00:00:02.000';  -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
SET @safePeriod = 2;         -- SET SAFE PERIOD OF HOURS FOR TOKEN DELETE, SINCE TOKENS COULD BE CASHED    [DEFAULT : 2]
SET @deleteTimeLimit = DATEADD(HOUR, -(@safePeriod), GetUTCDate());    -- SET CURRENT TIME - safePeriod FOR BEGIN THE TOKEN DELETE
SET @enableLog = 'TRUE';       -- ENABLE LOGGING [DEFAULT : FALSE]
SET @logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE , DEBUG
SET @enableAudit = 'FALSE'; -- SET TRUE FOR  KEEP TRACK OF ALL THE DELETED TOKENS USING A TABLE    [DEFAULT : FALSE] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO TRUE]
SET @rebuildIndexes = 'FALSE'; -- SET TRUE FOR REBUILD INDEXES TO IMPROVE QUERY PERFOMANCE [DEFAULT : FALSE]
SET @updateStats = 'FALSE'; -- SET TRUE FOR GATHER TABLE STATS TO IMPROVE QUERY PERFOMANCE [DEFAULT : FALSE]


IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_TOKEN_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
END;

IF (@enableAudit = 1)
BEGIN
SET @backupTables = 'TRUE'; -- BACKUP TABLES IS REQUIRED BE TRUE, HENCE THE AUDIT IS ENABLED.
END;

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- BACKUP TABLES
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@backupTables = 1)
BEGIN

	IF (@enableLog = 1)
	BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG';
	END;

	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN
		SELECT @backupTable = 'BAK_'+@cusrBackupTable;
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = @backupTable))
		BEGIN
		SELECT @SQL = 'DROP TABLE dbo.' +@backupTable;
		EXEC sp_executesql @SQL;
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT @SQL = 'SELECT ''BACKING UP '+@cusrBackupTable+' INTO '+@backupTable+' STARTED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.'+@cusrBackupTable;
		EXEC sp_executesql @SQL;
		END

		SELECT @SQL = 'SELECT * INTO '+@backupTable+' FROM dbo.' +@cusrBackupTable;
		EXEC sp_executesql @SQL;

		IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
		BEGIN
		SELECT @SQL = 'SELECT ''BACKING UP '+@cusrBackupTable+' INTO '+@backupTable+' COMPLETED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.'+@backupTable;
		EXEC sp_executesql @SQL;
		END
		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

END

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CREATING AUDIT TABLES FOR TOKENS DELETION FOR THE FIRST TIME RUN
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableAudit = 1)
BEGIN
	IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP'))
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP .. !';
			END
			Select * into dbo.AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP  from  dbo.IDN_OAUTH2_ACCESS_TOKEN where 1 =2;
	END
	ELSE
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP ..!';
			END
	END

	IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP'))
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP .. !';
			END
			Select * into dbo.AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP  from  dbo.IDN_OAUTH2_AUTHORIZATION_CODE where 1 =2;
	END
	ELSE
	BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP ..!';
			END
	END
END


---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
---- CALCULATING TOKENS TYPES IN IDN_OAUTH2_ACCESS_TOKEN
---- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

IF (@enableLog = 1)
BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING TOKEN TYPES IN IDN_OAUTH2_ACCESS_TOKEN TABLE .... !';

		IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
		BEGIN
		SELECT @rowcount = COUNT(1) FROM IDN_OAUTH2_ACCESS_TOKEN;
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL TOKENS ON IDN_OAUTH2_ACCESS_TOKEN TABLE BEFORE DELETE :'+CAST(@rowCount as varchar);
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT @cleaupCount = COUNT(1) FROM IDN_OAUTH2_ACCESS_TOKEN WHERE (VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod ) AND (REFRESH_TOKEN_VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod) AND (TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR
		(TOKEN_STATE in('ACTIVE') AND(@deleteTimeLimit > DATEADD(MINUTE, (VALIDITY_PERIOD/60000), TIME_CREATED) AND (@deleteTimeLimit > DATEADD(MINUTE, (REFRESH_TOKEN_VALIDITY_PERIOD/60000), REFRESH_TOKEN_TIME_CREATED)))));
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL TOKENS SHOULD BE DELETED FROM IDN_OAUTH2_ACCESS_TOKEN : '+ CAST(@cleaupCount as varchar);
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		select @rowcount  = (@rowcount - @cleaupCount);
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL TOKENS SHOULD BE RETAIN IN IDN_OAUTH2_ACCESS_TOKEN : '+CAST(@rowCount as varchar);
		END
END

---- ------------------------------------------------------
---- BATCH DELETE IDN_OAUTH2_ACCESS_TOKEN
---- ------------------------------------------------------

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] TOKEN DELETE ON IDN_OAUTH2_ACCESS_TOKEN STARTED .... !';
END


WHILE (1=1)
BEGIN
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHUNK_IDN_OAUTH2_ACCESS_TOKEN'))
		BEGIN
		DROP TABLE CHUNK_IDN_OAUTH2_ACCESS_TOKEN;
		END

		CREATE TABLE CHUNK_IDN_OAUTH2_ACCESS_TOKEN (TOKEN_ID VARCHAR (255),CONSTRAINT IDN_CHNK_OATH_ACCS_TOK_PRI PRIMARY KEY (TOKEN_ID));

		INSERT INTO CHUNK_IDN_OAUTH2_ACCESS_TOKEN (TOKEN_ID) SELECT TOP (@chunkSize) TOKEN_ID FROM IDN_OAUTH2_ACCESS_TOKEN WHERE (VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod) AND (REFRESH_TOKEN_VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod) AND (TOKEN_STATE IN ('EXPIRED','INACTIVE','REVOKED') OR
		(TOKEN_STATE in('ACTIVE') AND
		(@deleteTimeLimit > DATEADD(MINUTE, (VALIDITY_PERIOD/60000), TIME_CREATED) AND (@deleteTimeLimit > DATEADD(MINUTE, (REFRESH_TOKEN_VALIDITY_PERIOD/60000), REFRESH_TOKEN_TIME_CREATED)))));
		SELECT @chunkCount =  @@rowcount;

		IF (@chunkCount < @checkCount)
		BEGIN
		BREAK;
		END

		IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE CHUNK_IDN_OAUTH2_ACCESS_TOKEN CREATED WITH : '+CAST(@chunkCount as varchar);
		END

		IF (@enableAudit=1)
		BEGIN
		INSERT INTO dbo.AUDITLOG_IDN_OAUTH2_ACCESS_TOKEN_CLEANUP SELECT TOK.* FROM IDN_OAUTH2_ACCESS_TOKEN TOK , CHUNK_IDN_OAUTH2_ACCESS_TOKEN CHK WHERE TOK.TOKEN_ID=CHK.TOKEN_ID;
		END

		WHILE (1=1)
		BEGIN
			IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BATCH_IDN_OAUTH2_ACCESS_TOKEN'))
			BEGIN
			DROP TABLE BATCH_IDN_OAUTH2_ACCESS_TOKEN;
			END

			CREATE TABLE BATCH_IDN_OAUTH2_ACCESS_TOKEN (TOKEN_ID VARCHAR (255),CONSTRAINT IDN_BATCH_OATH_ACCS_TOK_PRI PRIMARY KEY (TOKEN_ID));

			INSERT INTO BATCH_IDN_OAUTH2_ACCESS_TOKEN (TOKEN_ID) SELECT TOP (@batchSize) TOKEN_ID FROM CHUNK_IDN_OAUTH2_ACCESS_TOKEN ;
			SELECT @batchCount =  @@rowcount;

			IF(@batchCount = 0)
			BEGIN
			BREAK;
			END

			IF ((@batchCount > 0))
			BEGIN

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON TABLE IDN_OAUTH2_ACCESS_TOKEN WITH : '+CAST(@batchCount as varchar);
				END

				DELETE IDN_OAUTH2_ACCESS_TOKEN where TOKEN_ID in (select TOKEN_ID from  BATCH_IDN_OAUTH2_ACCESS_TOKEN);
				SELECT  @deleteCount= @@rowcount;

				IF (@enableLog = 1)
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON IDN_OAUTH2_ACCESS_TOKEN WITH : '+CAST(@deleteCount as varchar);
				END

				DELETE CHUNK_IDN_OAUTH2_ACCESS_TOKEN WHERE TOKEN_ID in (select TOKEN_ID from BATCH_IDN_OAUTH2_ACCESS_TOKEN);

				IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON  CHUNK_IDN_OAUTH2_ACCESS_TOKEN !';
				END

				IF ((@deleteCount > 0))
				BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] SLEEPING ...';
				WAITFOR DELAY @sleepTime;
				END
			END
		END
END

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] TOKEN DELETE ON IDN_OAUTH2_ACCESS_TOKEN COMPLETED .... !';
END


-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CALCULATING CODE TYPES IN IDN_OAUTH2_AUTHORIZATION_CODE
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
IF (@enableLog = 1 )
BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING CODE TYPES IN IDN_OAUTH2_AUTHORIZATION_CODE TABLE .... !';

	IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
	BEGIN
	SELECT @rowcount = count(1) FROM IDN_OAUTH2_AUTHORIZATION_CODE;
	SELECT 'TOTAL CODES ON IDN_OAUTH2_AUTHORIZATION_CODE TABLE BEFORE DELETE : '+CAST(@rowCount as varchar);
	END

-- -------------
	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
	SELECT @cleaupCount = COUNT(1) FROM IDN_OAUTH2_AUTHORIZATION_CODE WHERE (VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod) AND (CODE_ID IN
	(SELECT CODE_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN tok where tok.TOKEN_ID = code.TOKEN_ID))
	AND ((@deleteTimeLimit > DATEADD(MINUTE, (VALIDITY_PERIOD/60000), TIME_CREATED)) OR STATE ='INACTIVE'));
	SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CODES SHOULD BE DELETED FROM IDN_OAUTH2_AUTHORIZATION_CODE : '+CAST(@cleaupCount as varchar);
	END
-- -------------

	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
	select @rowcount  = (@rowcount - @cleaupCount);
	SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL CODES SHOULD BE RETAIN IN IDN_OAUTH2_AUTHORIZATION_CODE : '+CAST(@rowCount as varchar);
	END
END

----

-- ------------------------------------------------------
-- BATCH DELETE IDN_OAUTH2_AUTHORIZATION_CODE
-- -- ------------------------------------------------------

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] CODE DELETE ON IDN_OAUTH2_AUTHORIZATION_CODE TABLE STARTED ... !';
END

----
WHILE (1=1)
BEGIN

	IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHNK_IDN_OATH_AUTHRIZATN_CODE'))
	BEGIN
	DROP TABLE CHNK_IDN_OATH_AUTHRIZATN_CODE;
	END

	CREATE TABLE CHNK_IDN_OATH_AUTHRIZATN_CODE (CODE_ID VARCHAR (255),CONSTRAINT IDN_CHNK_OATH_AUTHRIZN_PRI PRIMARY KEY (CODE_ID));


	INSERT INTO CHNK_IDN_OATH_AUTHRIZATN_CODE (CODE_ID) SELECT TOP (@chunkSize) CODE_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE WHERE (VALIDITY_PERIOD BETWEEN 0 and @maxValidityPeriod) AND (CODE_ID IN
	(SELECT CODE_ID FROM IDN_OAUTH2_AUTHORIZATION_CODE code WHERE NOT EXISTS (SELECT * FROM IDN_OAUTH2_ACCESS_TOKEN tok where tok.TOKEN_ID = code.TOKEN_ID))
	AND ((@deleteTimeLimit > DATEADD(MINUTE, (VALIDITY_PERIOD/60000), TIME_CREATED)) OR STATE ='INACTIVE'));

	SELECT @chunkCount=  @@rowcount;

	IF (@chunkCount < @checkCount)
	BEGIN
	BREAK;
	END

	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE CHNK_IDN_OATH_AUTHRIZATN_CODE CREATED WITH : '+CAST(@chunkCount as varchar);
	END

	IF (@enableAudit = 1)
	BEGIN
	INSERT INTO dbo.AUDITLOG_IDN_OAUTH2_AUTHORIZATION_CODE_CLEANUP SELECT CODE.* FROM IDN_OAUTH2_AUTHORIZATION_CODE CODE , CHNK_IDN_OATH_AUTHRIZATN_CODE CHK WHERE CODE.CODE_ID=CHK.CODE_ID;
	END

	WHILE (1=1)
	BEGIN

		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BATCH_IDN_OATH2_AUTHRIZATN_CDE'))
		BEGIN
		DROP TABLE BATCH_IDN_OATH2_AUTHRIZATN_CDE;
		END

		CREATE TABLE BATCH_IDN_OATH2_AUTHRIZATN_CDE (CODE_ID VARCHAR (255), CONSTRAINT IDN_BATCH_OATH_AUTHRIZN_PRI PRIMARY KEY (CODE_ID));

		INSERT INTO BATCH_IDN_OATH2_AUTHRIZATN_CDE (CODE_ID) SELECT TOP (@batchSize) CODE_ID FROM CHNK_IDN_OATH_AUTHRIZATN_CODE;
		SELECT  @batchCount= @@rowcount;

		IF (@batchCount = 0)
		BEGIN
		BREAK;
		END

		IF ((@batchCount > 0))
		BEGIN
			IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
			BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON TABLE IDN_OAUTH2_AUTHORIZATION_CODE WITH : '+CAST(@batchCount as varchar);
			END

			DELETE FROM IDN_OAUTH2_AUTHORIZATION_CODE where CODE_ID in (select CODE_ID from BATCH_IDN_OATH2_AUTHRIZATN_CDE);
			SELECT @deleteCount= @@rowcount;

			IF (@enableLog = 1)
			BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON IDN_OAUTH2_AUTHORIZATION_CODE WITH : '+CAST(@deleteCount as varchar);
			END

			DELETE CHNK_IDN_OATH_AUTHRIZATN_CODE WHERE CODE_ID in (select CODE_ID from BATCH_IDN_OATH2_AUTHRIZATN_CDE);

			IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
			BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON  CHNK_IDN_OATH_AUTHRIZATN_CODE !';
			END

			IF ((@deleteCount > 0))
			BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] SLEEPING ...';
			WAITFOR DELAY @sleepTime;
			END
		END
	END
END

--
IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] CODE DELETE ON IDN_OAUTH2_AUTHORIZATION_CODE COMPLETED .... !';
END

IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
BEGIN
SELECT @rowcount = COUNT(1) FROM IDN_OAUTH2_ACCESS_TOKEN;
SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL TOKENS ON IDN_OAUTH2_ACCESS_TOKEN TABLE AFTER DELETE :'+CAST(@rowCount as varchar);

SELECT @rowcount = COUNT(1) FROM IDN_OAUTH2_AUTHORIZATION_CODE;
SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL TOKENS ON IDN_OAUTH2_AUTHORIZATION_CODE TABLE AFTER DELETE :'+CAST(@rowCount as varchar);
END

-- ------------------------------------------------------
-- REBUILDING INDEXES
-- ------------------------------------------------------

IF (@rebuildIndexes = 1)
BEGIN

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING STARTED ...!';

END
	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN

		IF (@enableLog = 1)
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING FOR TABLE :'+@cusrBackupTable;
		END

		SELECT @SQL = 'ALTER INDEX ALL ON '+@cusrBackupTable+' REBUILD WITH (ONLINE = ON)';
		EXEC sp_executesql @SQL;

		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] INDEX REBUILDING FINISHED ...!';
END

END

-- ------------------------------------------------------
-- UPDATE TABLE STATS
-- ------------------------------------------------------

IF (@updateStats = 1)
BEGIN

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE DATABSE STATICTICS JOB STARTED ...!';

END
	OPEN backupTablesCursor;
	FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

	WHILE @@FETCH_STATUS = 0
	BEGIN

		IF (@enableLog = 1)
		BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE TABLE STATICTICS :'+@cusrBackupTable;
		END

		SELECT @SQL = 'UPDATE STATISTICS '+@cusrBackupTable;
		EXEC sp_executesql @SQL;

		FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
	END
	CLOSE backupTablesCursor;

IF (@enableLog = 1)
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] UPDATE DATABSE STATICTICS JOB FINISHED ...!';
END

END



deallocate backupTablesCursor;

IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
BEGIN
SELECT '[' + convert(varchar, getdate(), 121) + '] TOKEN_CLEANUP_SP COMPLETED .... !' AS 'INFO LOG';
END

END
