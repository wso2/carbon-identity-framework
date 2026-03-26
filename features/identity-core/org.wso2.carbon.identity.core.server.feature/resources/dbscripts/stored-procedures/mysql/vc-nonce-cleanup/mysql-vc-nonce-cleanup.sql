DROP PROCEDURE IF EXISTS `WSO2_VC_NONCE_CLEANUP`;
DELIMITER $$
CREATE PROCEDURE `WSO2_VC_NONCE_CLEANUP`()
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
    DECLARE cleanUpDateTimeLimit DATETIME;

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    SET batchSize    = 10000;  -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET chunkSize    = 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET enableLog    = FALSE;  -- ENABLE LOGGING [DEFAULT : FALSE]
    SET backupTables = FALSE;  -- SET IF VC NONCE TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
    SET sleepTime    = 2;      -- SET SLEEP TIME FOR AVOID TABLE LOCKS [DEFAULT : 2]

    SET batchCount = 1000;
    SET chunkCount = 1000;
    SET rowCount   = 0;
    SET cleanUpDateTimeLimit = UTC_TIMESTAMP();

    IF (enableLog) THEN
        SELECT 'WSO2_VC_NONCE_CLEANUP() STARTED...!' AS 'INFO LOG';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        IF (enableLog) THEN
            SELECT 'TABLE BACKUP STARTED ... !' AS 'INFO LOG';
        END IF;

        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
                    WHERE TABLE_NAME = 'BAK_IDN_VC_NONCE' AND TABLE_SCHEMA IN (SELECT DATABASE()))) THEN
            IF (enableLog) THEN
                SELECT 'DELETING OLD BACKUP...' AS 'INFO LOG';
            END IF;
            DROP TABLE BAK_IDN_VC_NONCE;
        END IF;

        CREATE TABLE BAK_IDN_VC_NONCE AS SELECT * FROM IDN_VC_NONCE;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    NONCE_CHUNK_LOOP : WHILE (chunkCount > 0) DO

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_VC_NONCE_CHUNK_TMP;
        CREATE TABLE IDN_VC_NONCE_CHUNK_TMP AS
        SELECT TENANT_ID, NONCE
        FROM IDN_VC_NONCE
        WHERE EXPIRY_TIME < cleanUpDateTimeLimit
        LIMIT chunkSize;

        SELECT ROW_COUNT() INTO chunkCount;
        IF (chunkCount = 0) THEN
            LEAVE NONCE_CHUNK_LOOP;
        END IF;

        CREATE INDEX IDN_VC_NONCE_CHUNK_TMP_INDX ON IDN_VC_NONCE_CHUNK_TMP (TENANT_ID, NONCE);
        COMMIT;

        IF (enableLog) THEN
            SELECT 'CREATED IDN_VC_NONCE_CHUNK_TMP...' AS 'INFO LOG';
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        NONCE_BATCH_LOOP : WHILE (batchCount > 0) DO

            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_VC_NONCE_BATCH_TMP;
            CREATE TABLE IDN_VC_NONCE_BATCH_TMP AS
            SELECT TENANT_ID, NONCE
            FROM IDN_VC_NONCE_CHUNK_TMP
            LIMIT batchSize;

            SELECT ROW_COUNT() INTO batchCount;
            COMMIT;

            IF (batchCount = 0) THEN
                LEAVE NONCE_BATCH_LOOP;
            END IF;

            IF (enableLog) THEN
                SELECT 'CREATED IDN_VC_NONCE_BATCH_TMP...' AS 'INFO LOG';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                SELECT 'BATCH DELETE STARTED ON IDN_VC_NONCE...' AS 'INFO LOG';
            END IF;

            DELETE A
            FROM IDN_VC_NONCE AS A
                     INNER JOIN IDN_VC_NONCE_BATCH_TMP AS B
                                ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE;

            SELECT ROW_COUNT() INTO rowCount;
            COMMIT;

            IF (enableLog) THEN
                SELECT 'BATCH DELETE FINISHED ON IDN_VC_NONCE : ' AS 'INFO LOG', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            DELETE A
            FROM IDN_VC_NONCE_CHUNK_TMP AS A
                     INNER JOIN IDN_VC_NONCE_BATCH_TMP AS B
                                ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE;

            IF (rowCount > 0) THEN
                DO SLEEP(sleepTime);
            END IF;

        END WHILE;
    END WHILE;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_VC_NONCE_CHUNK_TMP;
    DROP TABLE IF EXISTS IDN_VC_NONCE_BATCH_TMP;

    IF (enableLog) THEN
        SELECT 'CLEANUP COMPLETED...!' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
