DROP PROCEDURE IF EXISTS `WSO2_CONFIRMATION_CODE_CLEANUP`;
DELIMITER $$
CREATE PROCEDURE `WSO2_CONFIRMATION_CODE_CLEANUP`()
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

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    SET batchSize    = 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    SET chunkSize    = 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    SET enableLog    = FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]
    SET backupTables = FALSE; -- SET IF TOKEN TABLE NEEDS TO BACKUP BEFORE DELETE [DEFAULT : FALSE] , WILL DROP THE PREVIOUS BACKUP TABLES IN NEXT ITERATION

    SET batchCount = 1000;
    SET chunkCount = 1000;
    SET rowCount   = 0;

    SELECT 'WSO2_CONFIRMATION_CODE_CLEANUP() STARTED...!' AS 'INFO LOG';

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables)
    THEN
        IF (EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'BAK_IDN_RECOVERY_DATA' and TABLE_SCHEMA in (SELECT DATABASE())))
        THEN
            IF (enableLog)
            THEN
                SELECT 'DELETING OLD BACKUP...' AS 'INFO LOG';
            END IF;
            DROP TABLE BAK_IDN_RECOVERY_DATA;
        END IF;
        CREATE TABLE BAK_IDN_RECOVERY_DATA AS SELECT * FROM IDN_RECOVERY_DATA;
        COMMIT;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (chunkCount > 0) DO
        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS IDN_RECOVERY_DATA_CHUNK_TMP;
        CREATE TABLE IDN_RECOVERY_DATA_CHUNK_TMP AS SELECT CODE FROM IDN_RECOVERY_DATA LIMIT chunkSize;
        SELECT row_count() INTO chunkCount;
        CREATE INDEX IDN_RECOVERY_DATA_CHUNK_TMP_INDX on IDN_RECOVERY_DATA_CHUNK_TMP (CODE);
        COMMIT;
        IF (enableLog) THEN
            SELECT 'CREATED IDN_RECOVERY_DATA_CHUNK_TMP...' AS 'INFO LOG';
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        WHILE (batchCount > 0) DO
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS IDN_RECOVERY_DATA_BATCH_TMP;
            CREATE TABLE IDN_RECOVERY_DATA_BATCH_TMP AS
              SELECT CODE FROM IDN_RECOVERY_DATA_CHUNK_TMP LIMIT batchSize;
            SELECT row_count() INTO batchCount;
            COMMIT;
            IF (enableLog) THEN
                SELECT 'CREATED IDN_RECOVERY_DATA_BATCH_TMP...' AS 'INFO LOG';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                SELECT 'BATCH DELETE STARTED ON IDN_RECOVERY_DATA...' AS 'INFO LOG';
            END IF;

            DELETE A
              FROM IDN_RECOVERY_DATA AS A
              INNER JOIN IDN_RECOVERY_DATA_BATCH_TMP AS B
              ON A.CODE = B.CODE;
            SELECT row_count() INTO rowCount;
            COMMIT;

            IF (enableLog) THEN
                SELECT 'BATCH DELETE FINISHED ON IDN_RECOVERY_DATA : ' + rowCount AS 'INFO LOG';
            END IF;

            -- DELETE FROM CHUNK
            DELETE A
              FROM IDN_RECOVERY_DATA_CHUNK_TMP AS A
              INNER JOIN IDN_RECOVERY_DATA_BATCH_TMP AS B
              ON A.CODE = B.CODE;

        END WHILE;
    END WHILE;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS IDN_RECOVERY_DATA_CHUNK_TMP;
    DROP TABLE IF EXISTS IDN_RECOVERY_DATA_BATCH_TMP;

    IF (enableLog) THEN
        SELECT 'CLEANUP COMPLETED...!' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
