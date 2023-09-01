DROP PROCEDURE IF EXISTS `WSO2_PAR_REQUEST_CLEANUP`;
DELIMITER $$
CREATE PROCEDURE `WSO2_PAR_REQUEST_CLEANUP`()
BEGIN

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE batchSize INT;
    DECLARE chunkSize INT;
    DECLARE batchCount INT;
    DECLARE chunkCount INT;
    DECLARE rowCount INT;
    DECLARE enableLog BOOLEAN;
    DECLARE backupTables BOOLEAN;
    DECLARE sleepTime FLOAT;
    DECLARE cleanUpRequestsTimeLimit INT;
    DECLARE cleanUpDateTimeLimit DATETIME;
    DECLARE cleanUpDateTimeLimitInMillis BIGINT;

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    SET batchSize    = 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET chunkSize    = 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET enableLog    = FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]
    SET backupTables = FALSE; -- SET IF OAUTH PAR TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION
    SET sleepTime = 2; -- SET SLEEP TIME FOR AVOID TABLE LOCKS     [DEFAULT : 2]
    SET cleanUpRequestsTimeLimit = 24;  -- SET SAFE PERIOD OF HOURS FOR REQUEST DELETE [DEFAULT : 24 hrs (1 day)]. REQUESTS OLDER THAN THE NUMBER OF HOURS DEFINED HERE WILL BE DELETED.

    SET batchCount = 1000;
    SET chunkCount = 1000;
    SET rowCount   = 0;
    SET cleanUpDateTimeLimit = DATE_ADD(NOW(), INTERVAL -(cleanUpRequestsTimeLimit) HOUR);
    SET cleanUpDateTimeLimitInMillis = UNIX_TIMESTAMP(cleanUpDateTimeLimit) * 1000;

    IF (enableLog)
    THEN
        SELECT 'WSO2_PAR_REQUEST_CLEANUP() STARTED...!' AS 'INFO LOG';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables)
    THEN
        IF (enableLog)
        THEN
            SELECT 'TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END IF;

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_OAUTH_PAR' and TABLE_SCHEMA in (SELECT DATABASE())))
        THEN
            IF (enableLog)
            THEN
                SELECT 'DELETING OLD BACKUP...' AS 'INFO LOG';
            END IF;
            DROP TABLE BAK_IDN_OAUTH_PAR;
        END IF;
        CREATE TABLE BAK_IDN_OAUTH_PAR AS SELECT * FROM IDN_OAUTH_PAR;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    CONFIRMATION_CHUNK_LOOP : WHILE (chunkCount > 0) DO
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP;
        CREATE TABLE IDN_OAUTH_PAR_CHUNK_TMP AS SELECT REQ_URI_REF FROM IDN_OAUTH_PAR WHERE (cleanUpDateTimeLimitInMillis > SCHEDULED_EXPIRY) LIMIT chunkSize;
        SELECT row_count() INTO chunkCount;
        CREATE INDEX IDN_OAUTH_PAR_CHUNK_TMP_INDX on IDN_OAUTH_PAR_CHUNK_TMP (REQ_URI_REF);
        COMMIT;

        IF (chunkCount = 0)
        THEN
            LEAVE CONFIRMATION_CHUNK_LOOP;
        END IF;

        IF (enableLog) THEN
            SELECT 'CREATED IDN_OAUTH_PAR_CHUNK_TMP...' AS 'INFO LOG';
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        CONFIRMATION_BATCH_LOOP : WHILE (batchCount > 0) DO
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP;
            CREATE TABLE IDN_OAUTH_PAR_BATCH_TMP AS
              SELECT REQ_URI_REF FROM IDN_OAUTH_PAR_CHUNK_TMP LIMIT batchSize;
            SELECT row_count() INTO batchCount;
            COMMIT;

            IF (batchCount = 0)
            THEN
                LEAVE CONFIRMATION_BATCH_LOOP;
            END IF;

            IF (enableLog) THEN
                SELECT 'CREATED IDN_OAUTH_PAR_BATCH_TMP...' AS 'INFO LOG';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                SELECT 'BATCH DELETE STARTED ON IDN_OAUTH_PAR...' AS 'INFO LOG';
            END IF;

            DELETE A
              FROM IDN_OAUTH_PAR AS A
              INNER JOIN IDN_OAUTH_PAR_BATCH_TMP AS B
              ON A.REQ_URI_REF = B.REQ_URI_REF;
            SELECT row_count() INTO rowCount;
            COMMIT;

            IF (enableLog) THEN
                SELECT 'BATCH DELETE FINISHED ON IDN_OAUTH_PAR : ' AS 'INFO LOG', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            DELETE A
              FROM IDN_OAUTH_PAR_CHUNK_TMP AS A
              INNER JOIN IDN_OAUTH_PAR_BATCH_TMP AS B
              ON A.REQ_URI_REF = B.REQ_URI_REF;

            IF ((rowCount > 0))
            THEN
                DO SLEEP(sleepTime);
            END IF;

        END WHILE;
    END WHILE;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_OAUTH_PAR_CHUNK_TMP;
    DROP TABLE IF EXISTS IDN_OAUTH_PAR_BATCH_TMP;

    IF (enableLog) THEN
        SELECT 'CLEANUP COMPLETED...!' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
