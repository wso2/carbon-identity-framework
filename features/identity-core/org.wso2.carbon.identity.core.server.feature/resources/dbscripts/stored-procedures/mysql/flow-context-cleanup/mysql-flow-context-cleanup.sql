DROP PROCEDURE IF EXISTS `WSO2_IDN_FLOW_CONTEXT_CLEANUP`;
DELIMITER $$

CREATE PROCEDURE `WSO2_IDN_FLOW_CONTEXT_CLEANUP`()
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
    DECLARE cleanUpTimeLimit INT;
    DECLARE cleanUpDateTimeLimit DATETIME;

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    SET batchSize    = 10000;
    SET chunkSize    = 500000;
    SET enableLog    = FALSE;
    SET backupTables = FALSE;
    SET sleepTime    = 2;
    SET cleanUpTimeLimit = 24;  -- in hours

    SET batchCount = 1000;
    SET chunkCount = 1000;
    SET rowCount   = 0;
    SET cleanUpDateTimeLimit = DATE_SUB(NOW(), INTERVAL cleanUpTimeLimit HOUR);

    IF (enableLog) THEN
        SELECT 'WSO2_IDN_FLOW_CONTEXT_CLEANUP() STARTED...!' AS 'INFO LOG';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        IF (enableLog) THEN
            SELECT 'TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END IF;

        IF EXISTS (
            SELECT * FROM INFORMATION_SCHEMA.TABLES
            WHERE TABLE_NAME = 'BAK_IDN_FLOW_CONTEXT_STORE' AND TABLE_SCHEMA = DATABASE()
        ) THEN
            IF (enableLog) THEN
                SELECT 'DELETING OLD BACKUP...' AS 'INFO LOG';
            END IF;
            DROP TABLE BAK_IDN_FLOW_CONTEXT_STORE;
        END IF;

        CREATE TABLE BAK_IDN_FLOW_CONTEXT_STORE AS
            SELECT * FROM IDN_FLOW_CONTEXT_STORE;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    CLEANUP_CHUNK_LOOP: WHILE (chunkCount > 0) DO
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;
        CREATE TABLE IDN_FLOW_CONTEXT_CHUNK_TMP AS
            SELECT ID FROM IDN_FLOW_CONTEXT_STORE
            WHERE EXPIRES_AT < cleanUpDateTimeLimit
            LIMIT chunkSize;

        SELECT ROW_COUNT() INTO chunkCount;
        CREATE INDEX IDX_FLOW_CONTEXT_CHUNK ON IDN_FLOW_CONTEXT_CHUNK_TMP (ID);
        COMMIT;

        IF (chunkCount = 0) THEN
            LEAVE CLEANUP_CHUNK_LOOP;
        END IF;

        IF (enableLog) THEN
            SELECT 'CREATED IDN_FLOW_CONTEXT_CHUNK_TMP...' AS 'INFO LOG';
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        CLEANUP_BATCH_LOOP: WHILE (batchCount > 0) DO
            DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;
            CREATE TABLE IDN_FLOW_CONTEXT_BATCH_TMP AS
                SELECT ID FROM IDN_FLOW_CONTEXT_CHUNK_TMP LIMIT batchSize;

            SELECT ROW_COUNT() INTO batchCount;
            COMMIT;

            IF (batchCount = 0) THEN
                LEAVE CLEANUP_BATCH_LOOP;
            END IF;

            IF (enableLog) THEN
                SELECT 'CREATED IDN_FLOW_CONTEXT_BATCH_TMP...' AS 'INFO LOG';
            END IF;

            IF (enableLog) THEN
                SELECT 'BATCH DELETE STARTED ON IDN_FLOW_CONTEXT_STORE...' AS 'INFO LOG';
            END IF;

            DELETE A FROM IDN_FLOW_CONTEXT_STORE A
            INNER JOIN IDN_FLOW_CONTEXT_BATCH_TMP B ON A.ID = B.ID;
            SELECT ROW_COUNT() INTO rowCount;
            COMMIT;

            IF (enableLog) THEN
                SELECT 'BATCH DELETE FINISHED ON IDN_FLOW_CONTEXT_STORE: ' AS 'INFO LOG', rowCount;
            END IF;

            DELETE A FROM IDN_FLOW_CONTEXT_CHUNK_TMP A
            INNER JOIN IDN_FLOW_CONTEXT_BATCH_TMP B ON A.ID = B.ID;

            IF (rowCount > 0) THEN
                DO SLEEP(sleepTime);
            END IF;

        END WHILE;
    END WHILE;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;
    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;

    IF (enableLog) THEN
        SELECT 'CLEANUP COMPLETED...!' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
