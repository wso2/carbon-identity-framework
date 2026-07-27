CREATE PROCEDURE WSO2_WF_REQUEST_CLEANUP_DATA_RESTORE_SP
AS

BEGIN

DECLARE @rowCount INT;
DECLARE @totalRestored INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);
DECLARE @backupTableExists INT;

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @enableLog = 1;         -- ENABLE LOGGING [DEFAULT : TRUE]
SET @logLevel = 'TRACE';    -- SET LOG LEVELS : TRACE, DEBUG
SET @totalRestored = 0;

IF (@enableLog = 1)
BEGIN
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_WF_REQUEST_CLEANUP_DATA_RESTORE_SP STARTED...!';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
END

-- ------------------------------------------
-- RESTORE WF_REQUEST TABLE
-- ------------------------------------------
SELECT @backupTableExists = COUNT(1) 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'BAK_WF_REQUEST';

IF (@backupTableExists = 1)
BEGIN
    IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] RESTORATION STARTED ON WF_REQUEST TABLE...';
    END
    
    INSERT INTO dbo.WF_REQUEST 
    SELECT A.* 
    FROM dbo.BAK_WF_REQUEST A 
    LEFT JOIN dbo.WF_REQUEST B ON A.UUID = B.UUID 
    WHERE B.UUID IS NULL;
    
    SET @rowCount = @@ROWCOUNT;
    SET @totalRestored = @totalRestored + @rowCount;
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Restored ' + CAST(@rowCount AS VARCHAR) + ' records to WF_REQUEST';
    END
END
ELSE
BEGIN
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Backup table BAK_WF_REQUEST not found - skipping';
    END
END

-- ------------------------------------------
-- RESTORE WF_WORKFLOW_REQUEST_RELATION TABLE
-- ------------------------------------------
SELECT @backupTableExists = COUNT(1) 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'BAK_WF_WORKFLOW_REQUEST_RELATION';

IF (@backupTableExists = 1)
BEGIN
    IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] RESTORATION STARTED ON WF_WORKFLOW_REQUEST_RELATION TABLE...';
    END
    
    INSERT INTO dbo.WF_WORKFLOW_REQUEST_RELATION 
    SELECT A.* 
    FROM dbo.BAK_WF_WORKFLOW_REQUEST_RELATION A 
    LEFT JOIN dbo.WF_WORKFLOW_REQUEST_RELATION B 
        ON A.RELATIONSHIP_ID = B.RELATIONSHIP_ID
    WHERE B.RELATIONSHIP_ID IS NULL;
    
    SET @rowCount = @@ROWCOUNT;
    SET @totalRestored = @totalRestored + @rowCount;
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Restored ' + CAST(@rowCount AS VARCHAR) + ' records to WF_WORKFLOW_REQUEST_RELATION';
    END
END
ELSE
BEGIN
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Backup table BAK_WF_WORKFLOW_REQUEST_RELATION not found - skipping';
    END
END

-- ------------------------------------------
-- RESTORE WF_WORKFLOW_APPROVAL_RELATION TABLE
-- ------------------------------------------
SELECT @backupTableExists = COUNT(1) 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_NAME = 'BAK_WF_WORKFLOW_APPROVAL_RELATION';

IF (@backupTableExists = 1)
BEGIN
    IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] RESTORATION STARTED ON WF_WORKFLOW_APPROVAL_RELATION TABLE...';
    END
    
    INSERT INTO dbo.WF_WORKFLOW_APPROVAL_RELATION 
    SELECT A.* 
    FROM dbo.BAK_WF_WORKFLOW_APPROVAL_RELATION A 
    LEFT JOIN dbo.WF_WORKFLOW_APPROVAL_RELATION B 
        ON A.TASK_ID = B.TASK_ID
        AND A.APPROVER_TYPE = B.APPROVER_TYPE
        AND A.APPROVER_NAME = B.APPROVER_NAME
    WHERE B.TASK_ID IS NULL;
    
    SET @rowCount = @@ROWCOUNT;
    SET @totalRestored = @totalRestored + @rowCount;
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Restored ' + CAST(@rowCount AS VARCHAR) + ' records to WF_WORKFLOW_APPROVAL_RELATION';
    END
END
ELSE
BEGIN
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + ']   Backup table BAK_WF_WORKFLOW_APPROVAL_RELATION not found - skipping';
    END
END

-- ------------------------------------------
-- COMPLETION SUMMARY
-- ------------------------------------------
IF (@enableLog = 1)
BEGIN
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_WF_REQUEST_CLEANUP_DATA_RESTORE_SP COMPLETED!';
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Total records restored: ' + CAST(@totalRestored AS VARCHAR);
    SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
END

END

GO

-- ------------------------------------------
-- UTILITY PROCEDURE TO DROP BACKUP TABLES
-- ------------------------------------------
CREATE PROCEDURE WSO2_WF_REQUEST_DROP_BACKUP_TABLES_SP
AS
BEGIN
    DECLARE @enableLog BIT;
    DECLARE @tableExists INT;
    
    SET @enableLog = 1;
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] DROPPING WF BACKUP TABLES...';
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    END
    
    -- Drop BAK_WF_REQUEST if it exists
    SELECT @tableExists = COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_NAME = 'BAK_WF_REQUEST';
    IF (@tableExists = 1)
    BEGIN
        DROP TABLE dbo.BAK_WF_REQUEST;
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Dropped table: BAK_WF_REQUEST';
        END
    END
    
    -- Drop BAK_WF_WORKFLOW_REQUEST_RELATION if it exists
    SELECT @tableExists = COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_NAME = 'BAK_WF_WORKFLOW_REQUEST_RELATION';
    IF (@tableExists = 1)
    BEGIN
        DROP TABLE dbo.BAK_WF_WORKFLOW_REQUEST_RELATION;
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Dropped table: BAK_WF_WORKFLOW_REQUEST_RELATION';
        END
    END
    
    -- Drop BAK_WF_WORKFLOW_APPROVAL_RELATION if it exists
    SELECT @tableExists = COUNT(1) FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_NAME = 'BAK_WF_WORKFLOW_APPROVAL_RELATION';
    IF (@tableExists = 1)
    BEGIN
        DROP TABLE dbo.BAK_WF_WORKFLOW_APPROVAL_RELATION;
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] Dropped table: BAK_WF_WORKFLOW_APPROVAL_RELATION';
        END
    END
    
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] BACKUP TABLES DROPPED SUCCESSFULLY!';
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] ========================================';
    END
END

GO

-- ------------------------------------------
-- USAGE EXAMPLES
-- ------------------------------------------
-- To execute the cleanup procedure:
-- EXEC WSO2_WF_REQUEST_CLEANUP_SP;

-- To restore data from backup tables:
-- EXEC WSO2_WF_REQUEST_CLEANUP_DATA_RESTORE_SP;

-- To drop backup tables after successful restoration:
-- EXEC WSO2_WF_REQUEST_DROP_BACKUP_TABLES_SP;
