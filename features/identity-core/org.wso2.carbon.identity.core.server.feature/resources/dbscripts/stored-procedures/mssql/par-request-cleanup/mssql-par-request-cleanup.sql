CREATE OR ALTER PROCEDURE WSO2_PAR_REQUEST_CLEANUP AS
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
    DECLARE @cleanUpRequestsTimeLimit INT
    DECLARE @cleanUpDateTimeLimit DATETIME
    DECLARE @cleanUpDateTimeLimitInMillis BIGINT

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize    = 10000 -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET @chunkSize    = 500000 -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET @enableLog    = 0 -- ENABLE LOGGING [DEFAULT : 0]
    SET @backupTables = 0 -- SET IF OAUTH PAR TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : 0]. WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    SET @cleanUpRequestsTimeLimit = 24  -- SET SAFE PERIOD OF HOURS FOR REQUEST DELETE [DEFAULT : 24 hrs (1 day)]. REQUESTS OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.

    SET @rowCount = 0
    SET @batchCount = 1
    SET @chunkCount = 1
    SET @cleanUpDateTimeLimit = DATEADD(HOUR, -(@cleanUpRequestsTimeLimit), GetUTCDate())
    SET @cleanUpDateTimeLimitInMillis = DATEDIFF_BIG(MILLISECOND, '1970-01-01 00:00:00', @cleanUpDateTimeLimit);

    IF (@enableLog = 1)
    BEGIN
    SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_PAR_REQUEST_CLEANUP() STARTED...!' AS 'INFO LOG'
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

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_OAUTH_PAR'))
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] DELETING OLD BACKUP...' AS 'INFO LOG'
            END
            DROP TABLE BAK_IDN_OAUTH_PAR
        END
        SELECT * INTO BAK_IDN_OAUTH_PAR FROM IDN_OAUTH_PAR
    END

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------

    WHILE (@chunkCount > 0)
    BEGIN
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP
        CREATE TABLE IDN_OAUTH_PAR_CHUNK_TMP(REQ_URI_REF VARCHAR (255))
        INSERT INTO IDN_OAUTH_PAR_CHUNK_TMP(REQ_URI_REF) SELECT TOP (@chunkSize) REQ_URI_REF FROM IDN_OAUTH_PAR where (@cleanUpDateTimeLimitInMillis > SCHEDULED_EXPIRY);
        SET @chunkCount = @@ROWCOUNT

        IF (@chunkCount = 0)
        BEGIN
            BREAK;
        END

        CREATE INDEX IDN_OAUTH_PAR_CHUNK_TMP_INDX on IDN_OAUTH_PAR_CHUNK_TMP (REQ_URI_REF)
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_OAUTH_PAR_CHUNK_TMP...' AS 'INFO LOG'
        END

        -- BATCH LOOP
        SET @batchCount = 1
        WHILE (@batchCount > 0)
        BEGIN
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP
            CREATE TABLE IDN_OAUTH_PAR_BATCH_TMP(REQ_URI_REF VARCHAR (255))
            INSERT INTO IDN_OAUTH_PAR_BATCH_TMP(REQ_URI_REF) SELECT TOP (@batchSize) REQ_URI_REF FROM IDN_OAUTH_PAR_CHUNK_TMP
            SET @batchCount = @@ROWCOUNT

            IF (@batchCount = 0)
            BEGIN
                BREAK;
            END

            CREATE INDEX IDN_OAUTH_PAR_BATCH_TMP on IDN_OAUTH_PAR_BATCH_TMP (REQ_URI_REF)
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_OAUTH_PAR_BATCH_TMP...' AS 'INFO LOG'
            END

            -- BATCH DELETION
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE STARTED ON IDN_OAUTH_PAR...' AS 'INFO LOG'
            END
            DELETE FROM IDN_OAUTH_PAR WHERE REQ_URI_REF IN (SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_BATCH_TMP)
            SET @rowCount = @@ROWCOUNT
            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('BATCH DELETE FINISHED ON IDN_OAUTH_PAR : ', @rowCount) AS 'INFO LOG'
            END

            -- DELETE FROM CHUNK
            DELETE FROM IDN_OAUTH_PAR_CHUNK_TMP WHERE REQ_URI_REF IN (SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_BATCH_TMP)
        END
    END

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP
    DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP COMPLETED...!' AS 'INFO_LOG'
    END
END
