-- ------------------------------------------
-- This PROCEDURE will cleanup the IDN_ACTION table by removing the entries which are not referenced by IDP_AUTHENTICATOR_PROPERTY table.
-- Refer for more information: https://github.com/wso2/product-is/issues/21944
-- ------------------------------------------

CREATE OR ALTER PROCEDURE WSO2_IDN_ACTION_CLEANUP AS
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

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize = 10000 -- SET BATCH SIZE TO AVOID TABLE LOCKS [DEFAULT : 10000]
    SET @chunkSize = 500000 -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET @enableLog = 0 -- ENABLE LOGGING [DEFAULT : 0]
    SET @backupTables = 0 -- SET IF IDN_ACTION TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : 0]

    SET @rowCount = 0
    SET @batchCount = 1
    SET @chunkCount = 1

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] WSO2_IDN_ACTION_CLEANUP() STARTED...!' AS 'INFO LOG'
    END;

    -- ------------------------------------------
    -- BACKUP DATA (IF REQUIRED)
    -- ------------------------------------------
    IF (@backupTables = 1)
    BEGIN
        IF (@enableLog = 1)
        BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END;

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_ACTION'))
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] DELETING OLD BACKUP...' AS 'INFO LOG'
            END
            DROP TABLE BAK_IDN_ACTION
        END
        SELECT * INTO BAK_IDN_ACTION FROM IDN_ACTION
    END

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------

    WHILE (@chunkCount > 0)
    BEGIN
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP
        CREATE TABLE IDN_ACTION_CHUNK_TMP(UUID CHAR(36))
        INSERT INTO IDN_ACTION_CHUNK_TMP(UUID)
        SELECT TOP (@chunkSize) UUID FROM IDN_ACTION
            WHERE TYPE = 'AUTHENTICATION' AND UUID NOT IN (
                SELECT DISTINCT PROPERTY_VALUE FROM IDP_AUTHENTICATOR_PROPERTY
                    WHERE PROPERTY_KEY = 'actionId');
        SET @chunkCount = @@ROWCOUNT

        IF (@chunkCount = 0)
        BEGIN
            BREAK;
        END

        CREATE INDEX IDN_ACTION_CHUNK_TMP_INDX ON IDN_ACTION_CHUNK_TMP (UUID)
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_ACTION_CHUNK_TMP...' AS 'INFO LOG'
        END

        -- BATCH LOOP
        SET @batchCount = 1
        WHILE (@batchCount > 0)
        BEGIN
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP
            CREATE TABLE IDN_ACTION_BATCH_TMP(UUID CHAR(36))
            INSERT INTO IDN_ACTION_BATCH_TMP(UUID)
            SELECT TOP (@batchSize) UUID FROM IDN_ACTION_CHUNK_TMP;
            SET @batchCount = @@ROWCOUNT

            IF (@batchCount = 0)
            BEGIN
                BREAK;
            END

            CREATE INDEX IDN_ACTION_BATCH_TMP_INDX ON IDN_ACTION_BATCH_TMP (UUID)
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] CREATED IDN_ACTION_BATCH_TMP...' AS 'INFO LOG'
            END

            -- BATCH DELETION
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + convert(varchar, getdate(), 121) + '] BATCH DELETE STARTED ON IDN_ACTION...' AS 'INFO LOG'
            END
            DELETE FROM IDN_ACTION WHERE UUID IN (SELECT UUID FROM IDN_ACTION_BATCH_TMP);
            SET @rowCount = @@ROWCOUNT
            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('BATCH DELETE FINISHED ON IDN_ACTION : ', @rowCount) AS 'INFO LOG'
            END

            -- DELETE FROM CHUNK
            DELETE FROM IDN_ACTION_CHUNK_TMP WHERE UUID IN (SELECT UUID FROM IDN_ACTION_BATCH_TMP);
        END
    END

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_ACTION_BATCH_TMP
    DROP TABLE IF EXISTS IDN_ACTION_CHUNK_TMP

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP COMPLETED...!' AS 'INFO_LOG'
    END
END
