CREATE PROCEDURE WSO2_JTI_CLEANUP_SP
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
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('IDN_OIDC_JTI');

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
SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_JTI_CLEANUP_SP STARTED ... !' AS 'INFO LOG';
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
	IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_IDN_OIDC_JTI_CLEANUP'))
	BEGIN
		IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_IDN_OIDC_JTI_CLEANUP .. !';
		END
		Select * into dbo.AUDITLOG_IDN_OIDC_JTI_CLEANUP  from  dbo.IDN_OIDC_JTI where 1 =2;
	END
	ELSE
	BEGIN
		IF (@enableLog = 1 AND @logLevel IN ('TRACE')) BEGIN
			SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_IDN_OIDC_JTI_CLEANUP ..!';
		END
	END
END

-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
-- CALCULATING JWT ID TYPES IN IDN_OIDC_JTI
-- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
IF (@enableLog = 1)
BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING JWT ID TYPES IN IDN_OIDC_JTI TABLE .... !';

	IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
	BEGIN
		SELECT @rowcount = count(1) FROM IDN_OIDC_JTI;
		SELECT 'TOTAL JTIS ON IDN_OIDC_JTI TABLE BEFORE DELETE : '+CAST(@rowCount as varchar);
	END

-- -------------
	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
		SELECT @cleaupCount = COUNT(1) FROM IDN_OIDC_JTI WHERE (CONVERT(DATE, EXP_TIME) < CONVERT(DATE, CURRENT_TIMESTAMP));
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL JTIS SHOULD BE DELETED FROM IDN_OIDC_JTI : '+CAST(@cleaupCount as varchar);
	END

-- -------------
	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
		SELECT @rowcount  = (@rowcount - @cleaupCount);
		SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL JTIS SHOULD BE RETAIN IN IDN_OIDC_JTI : '+CAST(@rowCount as varchar);
	END
END


---- ------------------------------------------------------
---- BATCH DELETE IDN_OIDC_JTI
---- ------------------------------------------------------

IF (@enableLog = 1)
BEGIN
	SELECT '[' + convert(varchar, getdate(), 121) + '] JTIS DELETE ON IDN_OIDC_JTI STARTED .... !';
END


WHILE (1=1)
BEGIN
	IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'CHUNK_IDN_OIDC_JTI'))
	BEGIN
		DROP TABLE CHUNK_IDN_OIDC_JTI;
	END

	CREATE TABLE CHUNK_IDN_OIDC_JTI (JWT_ID VARCHAR (255),CONSTRAINT CHNK_IDN_OIDC_JTI_PRI PRIMARY KEY (JWT_ID));

	INSERT INTO CHUNK_IDN_OIDC_JTI (JWT_ID) SELECT TOP (@chunkSize) JWT_ID FROM IDN_OIDC_JTI WHERE (CONVERT(DATE, EXP_TIME) < CONVERT(DATE, CURRENT_TIMESTAMP));
	SELECT @chunkCount =  @@rowcount;

	IF (@chunkCount < @checkCount)
	BEGIN
		BREAK;
	END

	IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
	BEGIN
		SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE CHUNK_IDN_OIDC_JTI CREATED WITH : '+CAST(@chunkCount as varchar);
	END

	IF (@enableAudit=1)
	BEGIN
		INSERT INTO dbo.AUDITLOG_IDN_OIDC_JTI_CLEANUP SELECT JTIS.* FROM IDN_OIDC_JTI JTIS , CHUNK_IDN_OIDC_JTI CHK WHERE JTIS.JWT_ID=CHK.JWT_ID;
	END

	WHILE (1=1)
	BEGIN
		IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BATCH_IDN_OIDC_JTI'))
		BEGIN
			DROP TABLE BATCH_IDN_OIDC_JTI;
		END

		CREATE TABLE BATCH_IDN_OIDC_JTI (JWT_ID VARCHAR (255),CONSTRAINT BATCH_IDN_OIDC_JTI_PRI PRIMARY KEY (JWT_ID));

		INSERT INTO BATCH_IDN_OIDC_JTI (JWT_ID) SELECT TOP (@batchSize) JWT_ID FROM CHUNK_IDN_OIDC_JTI;
		SELECT @batchCount =  @@rowcount;

		IF(@batchCount = 0)
		BEGIN
			BREAK;
		END

		IF ((@batchCount > 0))
		BEGIN

			IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
			BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON TABLE IDN_OIDC_JTI WITH : '+CAST(@batchCount as varchar);
			END

			DELETE IDN_OIDC_JTI where JWT_ID in (select JWT_ID from  BATCH_IDN_OIDC_JTI);
			SELECT  @deleteCount= @@rowcount;

			IF (@enableLog = 1)
			BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON IDN_OIDC_JTI WITH : '+CAST(@deleteCount as varchar);
			END

			DELETE CHUNK_IDN_OIDC_JTI WHERE JWT_ID in (select JWT_ID from BATCH_IDN_OIDC_JTI);

			IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
			BEGIN
				SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON  CHUNK_IDN_OIDC_JTI !';
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
	SELECT '[' + convert(varchar, getdate(), 121) + '] JTIS DELETE ON IDN_OIDC_JTI COMPLETED .... !';
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
