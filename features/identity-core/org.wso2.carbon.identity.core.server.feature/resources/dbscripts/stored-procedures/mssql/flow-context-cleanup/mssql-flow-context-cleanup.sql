CREATE OR ALTER PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP AS
BEGIN
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE @batchSize INT
    DECLARE @chunkSize INT
    DECLARE @batchCount INT
    DECLARE @chunkCount INT
    DECLARE @rowCount INT
    DECLARE @enableLog BIT
    DECLARE @backupTables BIT
    DECLARE @cleanUpContextsTimeLimit INT
    DECLARE @cleanUpDateTimeLimit DATETIME

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize    = 10000       -- [DEFAULT : 10000]
    SET @chunkSize    = 500000      -- [DEFAULT : 500000]
    SET @enableLog    = 0           -- [DEFAULT : 0]
    SET @backupTables = 0           -- [DEFAULT : 0]
    SET @cleanUpContextsTimeLimit = 24 -- [DEFAULT : 24 hrs]

    SET @rowCount = 0
    SET @batchCount = 1
    SET @chunkCount = 1
    SET @cleanUpDateTimeLimit = DATEADD(HOUR, -(@cleanUpContextsTimeLimit), GETUTCDATE())

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_IDN_FLOW_CONTEXT_CLEANUP() STARTED...!' AS 'INFO LOG'
    END;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (@backupTables = 1)
    BEGIN
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END;

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_FLOW_CONTEXT_STORE'))
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] DELETING OLD BACKUP...' AS 'INFO LOG';
            END;
            DROP TABLE BAK_IDN_FLOW_CONTEXT_STORE;
        END;

        SELECT * INTO BAK_IDN_FLOW_CONTEXT_STORE FROM IDN_FLOW_CONTEXT_STORE;
    END

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (@chunkCount > 0)
    BEGIN
        DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;
        CREATE TABLE IDN_FLOW_CONTEXT_CHUNK_TMP (ID VARCHAR(255));

        INSERT INTO IDN_FLOW_CONTEXT_CHUNK_TMP (ID)
        SELECT TOP (@chunkSize) ID FROM IDN_FLOW_CONTEXT_STORE
        WHERE EXPIRES_AT < @cleanUpDateTimeLimit;

        SET @chunkCount = @@ROWCOUNT;

        IF (@chunkCount = 0)
        BEGIN
            BREAK;
        END;

        CREATE INDEX IDX_FLOW_CONTEXT_CHUNK ON IDN_FLOW_CONTEXT_CHUNK_TMP (ID);

        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CREATED IDN_FLOW_CONTEXT_CHUNK_TMP...' AS 'INFO LOG';
        END;

        -- BATCH LOOP
        SET @batchCount = 1;
        WHILE (@batchCount > 0)
        BEGIN
            DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;
            CREATE TABLE IDN_FLOW_CONTEXT_BATCH_TMP (ID VARCHAR(255));

            INSERT INTO IDN_FLOW_CONTEXT_BATCH_TMP (ID)
            SELECT TOP (@batchSize) ID FROM IDN_FLOW_CONTEXT_CHUNK_TMP;

            SET @batchCount = @@ROWCOUNT;

            IF (@batchCount = 0)
            BEGIN
                BREAK;
            END;

            CREATE INDEX IDX_FLOW_CONTEXT_BATCH ON IDN_FLOW_CONTEXT_BATCH_TMP (ID);

            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CREATED IDN_FLOW_CONTEXT_BATCH_TMP...' AS 'INFO LOG';
            END;

            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] BATCH DELETE STARTED ON IDN_FLOW_CONTEXT_STORE...' AS 'INFO LOG';
            END;

            DELETE FROM IDN_FLOW_CONTEXT_STORE
            WHERE ID IN (SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP);

            SET @rowCount = @@ROWCOUNT;

            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('BATCH DELETE FINISHED ON IDN_FLOW_CONTEXT_STORE : ', @rowCount) AS 'INFO LOG';
            END;

            DELETE FROM IDN_FLOW_CONTEXT_CHUNK_TMP
            WHERE ID IN (SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP);
        END
    END

    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;
    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP COMPLETED...!' AS 'INFO_LOG';
    END;
END
