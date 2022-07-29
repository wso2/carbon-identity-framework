CREATE OR ALTER PROCEDURE WSO2_CONFIRMATION_CODE_CLEANUP AS
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
    DECLARE @cleanUpCodesTimeLimit INT
    DECLARE @cleanUpDateTimeLimit DATETIME

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize    = 10000 -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET @chunkSize    = 500000 -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET @enableLog    = 0 -- ENABLE LOGGING [DEFAULT : 0]
    SET @backupTables = 0 -- SET IF RECOVERY DATA TABLE NEEDS TO BACKED-UP BEFORE DELETE [DEFAULT : 0]. WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    SET @cleanUpCodesTimeLimit = 720  -- SET SAFE PERIOD OF HOURS FOR CODE DELETE [DEFAULT : 720 hrs (30 days)]. CODES OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.

    SET @rowCount = 0
    SET @batchCount = 1
    SET @chunkCount = 1
    SET @cleanUpDateTimeLimit = DATEADD(HOUR, -(@cleanUpCodesTimeLimit), GetUTCDate())

    IF (@enableLog = 1)
    BEGIN
    SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_CONFIRMATION_CODE_CLEANUP() STARTED...!' AS 'INFO LOG'
    END;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (@backupTables = 1)
    BEGIN
        IF (@enableLog = 1)
        BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END;

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_RECOVERY_DATA'))
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] DELETING OLD BACKUP...' AS 'INFO LOG'
            END
            DROP TABLE BAK_IDN_RECOVERY_DATA
        END
        SELECT * INTO BAK_IDN_RECOVERY_DATA FROM IDN_RECOVERY_DATA
    END

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------

    WHILE (@chunkCount > 0)
    BEGIN
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_RECOVERY_DATA_CHUNK_TMP
        CREATE TABLE IDN_RECOVERY_DATA_CHUNK_TMP(CODE VARCHAR (255))
        INSERT INTO IDN_RECOVERY_DATA_CHUNK_TMP(CODE) SELECT TOP (@chunkSize) CODE FROM IDN_RECOVERY_DATA where (@cleanUpDateTimeLimit > TIME_CREATED);
        SET @chunkCount = @@ROWCOUNT

        IF (@chunkCount = 0)
        BEGIN
            BREAK;
        END

        CREATE INDEX IDN_RECOVERY_DATA_CHUNK_TMP_INDX on IDN_RECOVERY_DATA_CHUNK_TMP (CODE)
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_RECOVERY_DATA_CHUNK_TMP...' AS 'INFO LOG'
        END

        -- BATCH LOOP
        SET @batchCount = 1
        WHILE (@batchCount > 0)
        BEGIN
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_RECOVERY_DATA_BATCH_TMP
            CREATE TABLE IDN_RECOVERY_DATA_BATCH_TMP(CODE VARCHAR (255))
            INSERT INTO IDN_RECOVERY_DATA_BATCH_TMP(CODE) SELECT TOP (@batchSize) CODE FROM IDN_RECOVERY_DATA_CHUNK_TMP
            SET @batchCount = @@ROWCOUNT

            IF (@batchCount = 0)
            BEGIN
                BREAK;
            END

            CREATE INDEX IDN_RECOVERY_DATA_BATCH_TMP on IDN_RECOVERY_DATA_BATCH_TMP (CODE)
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_RECOVERY_DATA_BATCH_TMP...' AS 'INFO LOG'
            END

            -- BATCH DELETION
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE STARTED ON IDN_RECOVERY_DATA...' AS 'INFO LOG'
            END
            DELETE FROM IDN_RECOVERY_DATA WHERE CODE IN (SELECT CODE FROM IDN_RECOVERY_DATA_BATCH_TMP)
            SET @rowCount = @@ROWCOUNT
            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('BATCH DELETE FINISHED ON IDN_RECOVERY_DATA : ', @rowCount) AS 'INFO LOG'
            END

            -- DELETE FROM CHUNK
            DELETE FROM IDN_RECOVERY_DATA_CHUNK_TMP WHERE CODE IN (SELECT CODE FROM IDN_RECOVERY_DATA_BATCH_TMP)
        END
    END

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_RECOVERY_DATA_BATCH_TMP
    DROP TABLE IF EXISTS IDN_RECOVERY_DATA_CHUNK_TMP

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP COMPLETED...!' AS 'INFO_LOG'
    END
END
