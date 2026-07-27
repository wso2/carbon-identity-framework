CREATE OR ALTER PROCEDURE WSO2_VC_NONCE_CLEANUP AS
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
    DECLARE @cleanUpDateTimeLimit DATETIME

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET @batchSize = 10000 -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET @chunkSize = 500000 -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET @enableLog = 0 -- ENABLE LOGGING [DEFAULT : 0]
    SET @backupTables = 0 -- SET IF VC NONCE TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : 0]

    SET @rowCount = 0
    SET @batchCount = 1
    SET @chunkCount = 1
    SET @cleanUpDateTimeLimit = GETUTCDATE()

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] WSO2_VC_NONCE_CLEANUP() STARTED...!' AS 'INFO LOG'
    END

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (@backupTables = 1)
    BEGIN
        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] TABLE BACKUP STARTED ... !' AS 'INFO LOG'
        END

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_VC_NONCE'))
        BEGIN
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] DELETING OLD BACKUP...' AS 'INFO LOG'
            END
            DROP TABLE BAK_IDN_VC_NONCE
        END

        SELECT * INTO BAK_IDN_VC_NONCE FROM IDN_VC_NONCE
    END

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (@chunkCount > 0)
    BEGIN

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_VC_NONCE_CHUNK_TMP
        CREATE TABLE IDN_VC_NONCE_CHUNK_TMP(TENANT_ID INT, NONCE VARCHAR(255))

        INSERT INTO IDN_VC_NONCE_CHUNK_TMP(TENANT_ID, NONCE)
        SELECT TOP (@chunkSize) TENANT_ID, NONCE
        FROM IDN_VC_NONCE
        WHERE EXPIRY_TIME < @cleanUpDateTimeLimit

        SET @chunkCount = @@ROWCOUNT

        IF (@chunkCount = 0)
        BEGIN
            BREAK
        END

        CREATE INDEX IDN_VC_NONCE_CHUNK_TMP_INDX ON IDN_VC_NONCE_CHUNK_TMP (TENANT_ID, NONCE)

        IF (@enableLog = 1)
        BEGIN
            SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CREATED IDN_VC_NONCE_CHUNK_TMP...' AS 'INFO LOG'
        END

        -- BATCH LOOP
        SET @batchCount = 1
        WHILE (@batchCount > 0)
        BEGIN

            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_VC_NONCE_BATCH_TMP
            CREATE TABLE IDN_VC_NONCE_BATCH_TMP(TENANT_ID INT, NONCE VARCHAR(255))

            INSERT INTO IDN_VC_NONCE_BATCH_TMP(TENANT_ID, NONCE)
            SELECT TOP (@batchSize) TENANT_ID, NONCE
            FROM IDN_VC_NONCE_CHUNK_TMP

            SET @batchCount = @@ROWCOUNT

            IF (@batchCount = 0)
            BEGIN
                BREAK
            END

            CREATE INDEX IDN_VC_NONCE_BATCH_TMP_INDX ON IDN_VC_NONCE_BATCH_TMP (TENANT_ID, NONCE)

            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CREATED IDN_VC_NONCE_BATCH_TMP...' AS 'INFO LOG'
            END

            -- BATCH DELETION
            IF (@enableLog = 1)
            BEGIN
                SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] BATCH DELETE STARTED ON IDN_VC_NONCE...' AS 'INFO LOG'
            END

            DELETE A
            FROM IDN_VC_NONCE A
                     INNER JOIN IDN_VC_NONCE_BATCH_TMP B
                                ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE

            SET @rowCount = @@ROWCOUNT

            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('BATCH DELETE FINISHED ON IDN_VC_NONCE : ', @rowCount) AS 'INFO LOG'
            END

            -- DELETE FROM CHUNK
            DELETE A
            FROM IDN_VC_NONCE_CHUNK_TMP A
                     INNER JOIN IDN_VC_NONCE_BATCH_TMP B
                                ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE
        END
    END

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_VC_NONCE_BATCH_TMP
    DROP TABLE IF EXISTS IDN_VC_NONCE_CHUNK_TMP

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP COMPLETED...!' AS 'INFO LOG'
    END
END
