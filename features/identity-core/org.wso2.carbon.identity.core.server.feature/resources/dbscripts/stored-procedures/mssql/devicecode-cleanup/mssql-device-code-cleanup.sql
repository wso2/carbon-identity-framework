CREATE OR ALTER PROCEDURE WSO2_DEVICE_CODE_CLEANUP_SP AS
BEGIN

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE @batchSize INT
    DECLARE @chunkSize INT
    DECLARE @batchCount INT
    DECLARE @chunkCount INT
    DECLARE @rowCount INT
    DECLARE @cleanupCount INT
    DECLARE @deleteCount INT
    DECLARE @enableLog BIT
    DECLARE @logLevel VARCHAR(10)
    DECLARE @backupTables BIT
    DECLARE @enableAudit BIT
    DECLARE @checkCount INT
    DECLARE @safePeriod INT
    DECLARE @sleepTime VARCHAR(12)
    DECLARE @deleteTimeLimit DATETIME
    DECLARE @cusrBackupTable VARCHAR(100)
    DECLARE @backupTable VARCHAR(100)
    DECLARE @SQL NVARCHAR(MAX)

    DECLARE backupTablesCursor CURSOR FOR
    SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME IN ('IDN_OAUTH2_DEVICE_FLOW', 'IDN_OAUTH2_DEVICE_FLOW_SCOPES')

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize    = 10000 -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET @chunkSize    = 500000 -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET @checkCount   = 100 -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE DEVICE CODES COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
    SET @safePeriod   = 24 -- SET SAFE PERIOD OF HOURS FOR DEVICE CODE DELETE [DEFAULT : 24]. DEVICE CODES EXPIRED EARLIER THAN THIS ARE DELETED.
    SET @sleepTime    = '00:00:02.000' -- SET SLEEP TIME FOR AVOID TABLE LOCKS [DEFAULT : 2]
    SET @enableLog    = 1 -- ENABLE LOGGING [DEFAULT : 1]
    SET @logLevel     = 'TRACE' -- SET LOG LEVELS : TRACE , DEBUG
    SET @backupTables = 0 -- SET IF DEVICE FLOW TABLES NEED TO BE BACKED-UP BEFORE DELETE [DEFAULT : 0]. WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    SET @enableAudit  = 0 -- SET 1 FOR KEEP TRACK OF ALL THE DELETED DEVICE CODES USING A TABLE [DEFAULT : 0] [# IF YOU ENABLE THIS TABLE BACKUP WILL FORCEFULLY SET TO 1]

    SET @rowCount = 0
    SET @cleanupCount = 0
    SET @deleteCount = 0
    SET @deleteTimeLimit = DATEADD(HOUR, -(@safePeriod), GetUTCDate()) -- SET CURRENT TIME - safePeriod FOR BEGIN THE DEVICE CODE DELETE

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_DEVICE_CODE_CLEANUP_SP STARTED ... !' AS 'INFO LOG'
    END

    IF (@enableAudit = 1)
    BEGIN
        SET @backupTables = 1 -- BACKUP TABLES IS REQUIRED TO BE 1, HENCE THE AUDIT IS ENABLED.
    END

    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    -- BACKUP TABLES
    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (@backupTables = 1)
    BEGIN

        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + convert(varchar, getdate(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG'
        END

        OPEN backupTablesCursor
        FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable

        WHILE @@FETCH_STATUS = 0
        BEGIN
            SELECT @backupTable = 'BAK_' + @cusrBackupTable

            IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = @backupTable))
            BEGIN
                SELECT @SQL = 'DROP TABLE dbo.' + @backupTable
                EXEC sp_executesql @SQL
            END

            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT @SQL = 'SELECT ''BACKING UP ' + @cusrBackupTable + ' INTO ' + @backupTable + ' STARTED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.' + @cusrBackupTable
                EXEC sp_executesql @SQL
            END

            SELECT @SQL = 'SELECT * INTO ' + @backupTable + ' FROM dbo.' + @cusrBackupTable
            EXEC sp_executesql @SQL

            IF (@enableLog = 1 AND @logLevel IN ('DEBUG','TRACE'))
            BEGIN
                SELECT @SQL = 'SELECT ''BACKING UP ' + @cusrBackupTable + ' INTO ' + @backupTable + ' COMPLETED WITH : '' as ''DEBUG LOG'', COUNT_BIG(*) as ''COUNT'' FROM dbo.' + @backupTable
                EXEC sp_executesql @SQL
            END

            FETCH NEXT FROM backupTablesCursor INTO @cusrBackupTable
        END
        CLOSE backupTablesCursor
    END

    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    -- CREATING AUDIT TABLE FOR DEVICE CODE DELETION FOR THE FIRST TIME RUN
    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (@enableAudit = 1)
    BEGIN
        IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP'))
        BEGIN
            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] CREATING AUDIT TABLE AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP .. !' AS 'TRACE LOG'
            END
            SELECT * INTO dbo.AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP FROM dbo.IDN_OAUTH2_DEVICE_FLOW WHERE 1 = 2
        END
        ELSE
        BEGIN
            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] USING AUDIT TABLE AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP ..!' AS 'TRACE LOG'
            END
        END
    END

    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    -- CALCULATING DEVICE CODES IN IDN_OAUTH2_DEVICE_FLOW
    -- ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CALCULATING DEVICE CODES ON IDN_OAUTH2_DEVICE_FLOW .... !' AS 'INFO LOG'

        IF (@logLevel IN ('DEBUG','TRACE'))
        BEGIN
            SELECT @rowCount = COUNT(1) FROM IDN_OAUTH2_DEVICE_FLOW
            SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL DEVICE CODES ON IDN_OAUTH2_DEVICE_FLOW TABLE BEFORE DELETE : ' + CAST(@rowCount as varchar) AS 'DEBUG LOG'
        END

        IF (@logLevel IN ('TRACE'))
        BEGIN
            SELECT @cleanupCount = COUNT(1) FROM IDN_OAUTH2_DEVICE_FLOW WHERE (STATUS = 'EXPIRED' OR EXPIRY_TIME < @deleteTimeLimit)
            SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL DEVICE CODES SHOULD BE DELETED FROM IDN_OAUTH2_DEVICE_FLOW : ' + CAST(@cleanupCount as varchar) AS 'TRACE LOG'

            SELECT @rowCount = (@rowCount - @cleanupCount)
            SELECT '[' + convert(varchar, getdate(), 121) + '] TOTAL DEVICE CODES SHOULD BE RETAIN IN IDN_OAUTH2_DEVICE_FLOW : ' + CAST(@rowCount as varchar) AS 'TRACE LOG'
        END
    END

    -- ------------------------------------------------------
    -- BATCH DELETE IDN_OAUTH2_DEVICE_FLOW
    -- ------------------------------------------------------
    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] DEVICE CODE DELETE ON IDN_OAUTH2_DEVICE_FLOW STARTED .... !' AS 'INFO LOG'
    END

    WHILE (1 = 1)
    BEGIN
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP
        CREATE TABLE IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP (CODE_ID VARCHAR(255), CONSTRAINT IDN_DEVICE_FLOW_CHUNK_TMP_PRI PRIMARY KEY (CODE_ID))

        INSERT INTO IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP (CODE_ID)
        SELECT TOP (@chunkSize) CODE_ID FROM IDN_OAUTH2_DEVICE_FLOW
        WHERE (STATUS = 'EXPIRED' OR EXPIRY_TIME < @deleteTimeLimit)

        SET @chunkCount = @@ROWCOUNT

        IF (@chunkCount < @checkCount)
        BEGIN
            BREAK
        END

        IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
        BEGIN
            SELECT '[' + convert(varchar, getdate(), 121) + '] CHUNK TABLE IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP CREATED WITH : ' + CAST(@chunkCount as varchar) AS 'TRACE LOG'
        END

        IF (@enableAudit = 1)
        BEGIN
            INSERT INTO dbo.AUDITLOG_IDN_OAUTH2_DEVICE_FLOW_CLEANUP
            SELECT DEV.* FROM IDN_OAUTH2_DEVICE_FLOW DEV, IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP CHK
            WHERE DEV.CODE_ID = CHK.CODE_ID
        END

        -- BATCH LOOP
        WHILE (1 = 1)
        BEGIN
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP
            CREATE TABLE IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP (CODE_ID VARCHAR(255), CONSTRAINT IDN_DEVICE_FLOW_BATCH_TMP_PRI PRIMARY KEY (CODE_ID))

            INSERT INTO IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP (CODE_ID)
            SELECT TOP (@batchSize) CODE_ID FROM IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP

            SET @batchCount = @@ROWCOUNT

            IF (@batchCount = 0)
            BEGIN
                BREAK
            END

            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE START ON TABLE IDN_OAUTH2_DEVICE_FLOW WITH : ' + CAST(@batchCount as varchar) AS 'TRACE LOG'
            END

            -- ROWS IN IDN_OAUTH2_DEVICE_FLOW_SCOPES ARE REMOVED BY THE
            -- ON DELETE CASCADE FOREIGN KEY ON SCOPE_ID -> IDN_OAUTH2_DEVICE_FLOW(CODE_ID)
            DELETE FROM IDN_OAUTH2_DEVICE_FLOW
            WHERE EXISTS (SELECT 1 FROM IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP
                          WHERE IDN_OAUTH2_DEVICE_FLOW.CODE_ID = IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP.CODE_ID)

            SET @deleteCount = @@ROWCOUNT

            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE FINISHED ON IDN_OAUTH2_DEVICE_FLOW WITH : ' + CAST(@deleteCount as varchar) AS 'INFO LOG'
            END

            -- DELETE FROM CHUNK
            DELETE FROM IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP
            WHERE EXISTS (SELECT 1 FROM IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP
                          WHERE IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP.CODE_ID = IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP.CODE_ID)

            IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] DELETED BATCH ON IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP !' AS 'TRACE LOG'
            END

            IF (@deleteCount > 0)
            BEGIN
                IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
                BEGIN
                    SELECT '[' + convert(varchar, getdate(), 121) + '] SLEEPING ...' AS 'TRACE LOG'
                END
                WAITFOR DELAY @sleepTime
            END
        END
    END

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_OAUTH2_DEVICE_FLOW_BATCH_TMP
    DROP TABLE IF EXISTS IDN_OAUTH2_DEVICE_FLOW_CHUNK_TMP

    DEALLOCATE backupTablesCursor

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] DEVICE CODE DELETE ON IDN_OAUTH2_DEVICE_FLOW COMPLETED .... !' AS 'INFO LOG'
        SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_DEVICE_CODE_CLEANUP_SP COMPLETED .... !' AS 'INFO LOG'
    END
END
