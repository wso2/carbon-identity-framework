CREATE PROCEDURE WSO2_WF_REQUEST_CLEANUP_SP
AS

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE @batchSize INT;
DECLARE @chunkSize INT;
DECLARE @chunkCount INT;
DECLARE @batchCount INT;
DECLARE @rowCount INT;
DECLARE @totalDeleted INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);
DECLARE @backupTables BIT;
DECLARE @cleanUpRequestsTimeLimit INT;
DECLARE @deleteTimeLimit DATETIME;
DECLARE @SQL NVARCHAR(MAX);
DECLARE @backupTable VARCHAR(100);
DECLARE @currentTable VARCHAR(100);
DECLARE @sourceTableExists INT;
DECLARE @eligibleRecords INT;

-- Table cursor for backup operations
DECLARE backupTablesCursor CURSOR FOR
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME IN ('WF_REQUEST', 'WF_WORKFLOW_REQUEST_RELATION', 'WF_WORKFLOW_APPROVAL_RELATION');

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @batchSize = 10000;                     -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
SET @chunkSize = 500000;                    -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
SET @backupTables = 0;                      -- SET IF WF TABLES NEED TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
SET @enableLog = 1;                         -- ENABLE LOGGING [DEFAULT : TRUE]
SET @logLevel = 'TRACE';                    -- SET LOG LEVELS : TRACE, DEBUG
SET @cleanUpRequestsTimeLimit = 60;         -- SET SAFE PERIOD IN DAYS FOR REQUEST DELETE [DEFAULT : 60 days (2 months)]
SET @deleteTimeLimit = DATEADD(DAY, -(@cleanUpRequestsTimeLimit), GETUTCDATE());

-- Initialize counters
SET @totalDeleted = 0;

IF (@enableLog = 1)
BEGIN
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_WF_REQUEST_CLEANUP_SP STARTED...!';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Cleanup Period: ' + CAST(@cleanUpRequestsTimeLimit AS VARCHAR) + ' days';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Cleanup Date Limit: ' + CONVERT(VARCHAR, @deleteTimeLimit, 121);
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Target Statuses: APPROVED, REJECTED, FAILED, DELETED, ABORTED';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
END

-- ------------------------------------------
-- BACKUP TABLES
-- ------------------------------------------
IF (@backupTables = 1)
BEGIN
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Starting backup process...';
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Backup enabled: ' + CASE WHEN @backupTables = 1 THEN 'TRUE' ELSE 'FALSE' END;
    END

    OPEN backupTablesCursor;
    FETCH NEXT FROM backupTablesCursor INTO @currentTable;

    WHILE @@FETCH_STATUS = 0
    BEGIN
        SET @backupTable = 'BAK_' + @currentTable;
        
        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Processing table: ' + @currentTable;
        END
        
        -- Check if source table exists and has records        
        SELECT @sourceTableExists = COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = @currentTable;
        
        IF (@sourceTableExists = 0)
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WARNING: Source table ' + @currentTable + ' does not exist - skipping backup';
            END
            GOTO NextTable
        END
        
        -- Drop existing backup table if exists
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = @backupTable))
        BEGIN
            SET @SQL = 'DROP TABLE dbo.' + @backupTable;
            EXEC sp_executesql @SQL;
            
            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Dropped existing backup table: ' + @backupTable;
            END
        END

        -- Create backup table with records that will be deleted
        SET @SQL = '';
        IF (@currentTable = 'WF_REQUEST')
        BEGIN
            -- First check how many eligible records exist
            SET @SQL = 'SELECT @count = COUNT(*) FROM dbo.' + @currentTable + 
                      ' WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                      ' AND UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + '''';
            EXEC sp_executesql @SQL, N'@count INT OUTPUT', @count = @eligibleRecords OUTPUT;
            
            IF (@eligibleRecords > 0)
            BEGIN
                SET @SQL = 'SELECT * INTO dbo.' + @backupTable + 
                          ' FROM dbo.' + @currentTable + 
                          ' WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                          ' AND UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + '''';
            END
        END
        ELSE IF (@currentTable = 'WF_WORKFLOW_REQUEST_RELATION')
        BEGIN
            -- First check how many eligible records exist
            SET @SQL = 'SELECT @count = COUNT(*) FROM dbo.' + @currentTable + ' t ' +
                      ' WHERE EXISTS ( ' +
                      '     SELECT 1 FROM dbo.WF_REQUEST r ' +
                      '     WHERE r.UUID = t.REQUEST_ID ' +
                      '     AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                      '     AND r.UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + ''' ' +
                      ' )';
            EXEC sp_executesql @SQL, N'@count INT OUTPUT', @count = @eligibleRecords OUTPUT;
            
            IF (@eligibleRecords > 0)
            BEGIN
                SET @SQL = 'SELECT t.* INTO dbo.' + @backupTable + 
                          ' FROM dbo.' + @currentTable + ' t ' +
                          ' WHERE EXISTS ( ' +
                          '     SELECT 1 FROM dbo.WF_REQUEST r ' +
                          '     WHERE r.UUID = t.REQUEST_ID ' +
                          '     AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                          '     AND r.UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + ''' ' +
                          ' )';
            END
        END
        ELSE IF (@currentTable = 'WF_WORKFLOW_APPROVAL_RELATION')
        BEGIN
            -- First check how many eligible records exist
            SET @SQL = 'SELECT @count = COUNT(*) FROM dbo.' + @currentTable + ' t ' +
                      ' WHERE EXISTS ( ' +
                      '     SELECT 1 FROM dbo.WF_REQUEST r ' +
                      '     WHERE r.UUID = t.EVENT_ID ' +
                      '     AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                      '     AND r.UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + ''' ' +
                      ' )';
            EXEC sp_executesql @SQL, N'@count INT OUTPUT', @count = @eligibleRecords OUTPUT;
            
            IF (@eligibleRecords > 0)
            BEGIN
                SET @SQL = 'SELECT t.* INTO dbo.' + @backupTable + 
                          ' FROM dbo.' + @currentTable + ' t ' +
                          ' WHERE EXISTS ( ' +
                          '     SELECT 1 FROM dbo.WF_REQUEST r ' +
                          '     WHERE r.UUID = t.EVENT_ID ' +
                          '     AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
                          '     AND r.UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + ''' ' +
                          ' )';
            END
        END

        IF (@eligibleRecords > 0 AND @SQL != '')
        BEGIN
            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Found ' + CAST(@eligibleRecords AS VARCHAR) + ' eligible records in ' + @currentTable;
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Executing backup SQL: ' + LEFT(@SQL, 200) + '...';
            END
            
            EXEC sp_executesql @SQL;
            SET @rowCount = @@ROWCOUNT;

            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Created backup table ' + @backupTable + ' with ' + CAST(@rowCount AS VARCHAR) + ' records';
            END
        END
        ELSE
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] No eligible records found for backup in table: ' + @currentTable + ' (found ' + CAST(@eligibleRecords AS VARCHAR) + ' records)';
            END
        END

        NextTable:
        FETCH NEXT FROM backupTablesCursor INTO @currentTable;
    END

    CLOSE backupTablesCursor;
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Backup process completed.';
    END
END

-- ------------------------------------------
-- CLEANUP DATA
-- ------------------------------------------
IF (@enableLog = 1)
BEGIN
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Starting cleanup process...';
END

WHILE (1=1)
BEGIN
    -- Create chunk table for eligible request IDs
    IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'WF_REQUEST_CHUNK_TMP'))
    BEGIN
        DROP TABLE WF_REQUEST_CHUNK_TMP
    END

    CREATE TABLE WF_REQUEST_CHUNK_TMP (
        UUID VARCHAR(255),
        CONSTRAINT PK_WF_REQUEST_CHUNK_TMP PRIMARY KEY (UUID)
    );

    SET @SQL = 'INSERT INTO WF_REQUEST_CHUNK_TMP (UUID) ' +
               'SELECT TOP (' + CAST(@chunkSize AS VARCHAR) + ') UUID FROM dbo.WF_REQUEST ' +
               'WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ' +
               'AND UPDATED_AT < ''' + CONVERT(VARCHAR, @deleteTimeLimit, 121) + '''';
    
    EXEC sp_executesql @SQL;
    SET @chunkCount = @@ROWCOUNT;

    IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Created WF_REQUEST_CHUNK_TMP with ' + CAST(@chunkCount AS VARCHAR) + ' records';
    END

    IF (@chunkCount = 0)
    BEGIN
        BREAK
    END

    -- Batch processing loop
    WHILE (1=1)
    BEGIN
        -- Create batch table
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'WF_REQUEST_BATCH_TMP'))
        BEGIN
            DROP TABLE WF_REQUEST_BATCH_TMP
        END

        CREATE TABLE WF_REQUEST_BATCH_TMP (
            UUID VARCHAR(255),
            CONSTRAINT PK_WF_REQUEST_BATCH_TMP PRIMARY KEY (UUID)
        );

        SET @SQL = 'INSERT INTO WF_REQUEST_BATCH_TMP (UUID) ' +
                   'SELECT TOP (' + CAST(@batchSize AS VARCHAR) + ') UUID FROM WF_REQUEST_CHUNK_TMP';
        
        EXEC sp_executesql @SQL;
        SET @batchCount = @@ROWCOUNT;

        IF (@batchCount = 0)
        BEGIN
            BREAK
        END

        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Processing batch with ' + CAST(@batchCount AS VARCHAR) + ' records...';
        END

        -- Delete related records first (child tables)
        DELETE FROM dbo.WF_WORKFLOW_APPROVAL_RELATION 
        WHERE EVENT_ID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);
        
        SET @rowCount = @@ROWCOUNT;
        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Deleted ' + CAST(@rowCount AS VARCHAR) + ' records from WF_WORKFLOW_APPROVAL_RELATION';
        END

        DELETE FROM dbo.WF_WORKFLOW_REQUEST_RELATION 
        WHERE REQUEST_ID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);
        
        SET @rowCount = @@ROWCOUNT;
        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Deleted ' + CAST(@rowCount AS VARCHAR) + ' records from WF_WORKFLOW_REQUEST_RELATION';
        END

        -- Delete from main table (parent table)
        DELETE FROM dbo.WF_REQUEST 
        WHERE UUID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);
        
        SET @rowCount = @@ROWCOUNT;
        SET @totalDeleted = @totalDeleted + @rowCount;
        
        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Deleted ' + CAST(@rowCount AS VARCHAR) + ' records from WF_REQUEST';
        END

        -- Remove processed records from chunk table
        DELETE FROM WF_REQUEST_CHUNK_TMP 
        WHERE UUID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);

        -- Add small delay to avoid table locks
        WAITFOR DELAY '00:00:01'
    END
END

-- Cleanup temporary tables
IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'WF_REQUEST_CHUNK_TMP'))
BEGIN
    DROP TABLE WF_REQUEST_CHUNK_TMP
END

IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'WF_REQUEST_BATCH_TMP'))
BEGIN
    DROP TABLE WF_REQUEST_BATCH_TMP
END

-- Cleanup completed
IF (@enableLog = 1)
BEGIN
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_WF_REQUEST_CLEANUP_SP COMPLETED!';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Total WF_REQUEST records deleted: ' + CAST(@totalDeleted AS VARCHAR);
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
END

DEALLOCATE backupTablesCursor

END
