DROP PROCEDURE IF EXISTS `WSO2_WF_REQUEST_CLEANUP`;

DELIMITER $$

CREATE PROCEDURE `WSO2_WF_REQUEST_CLEANUP`()

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE batchSize INT;
DECLARE chunkSize INT;
DECLARE batchCount INT;
DECLARE chunkCount INT;
DECLARE rowCount INT;
DECLARE totalDeleted INT;
DECLARE enableLog BOOLEAN;
DECLARE backupTables BOOLEAN;
DECLARE cleanUpRequestsTimeLimit INT;
DECLARE deleteTillTime DATETIME;
DECLARE backupTable VARCHAR(255);
DECLARE cursorTable VARCHAR(255);
DECLARE cursorLoopFinished INTEGER DEFAULT 0;

DECLARE tablesCursor CURSOR FOR 
    SELECT TABLE_NAME 
    FROM INFORMATION_SCHEMA.TABLES 
    WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
    AND TABLE_NAME IN ('WF_REQUEST', 'WF_WORKFLOW_REQUEST_RELATION', 'WF_WORKFLOW_APPROVAL_RELATION');

DECLARE CONTINUE HANDLER FOR NOT FOUND SET cursorLoopFinished = 1;

-- ------------------------------------------
-- CONFIGURE VARIABLES
-- ------------------------------------------
SET batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
SET chunkSize = 500000;     -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
SET backupTables = FALSE;   -- SET IF WF TABLES NEED TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
SET enableLog = TRUE;       -- ENABLE LOGGING [DEFAULT : TRUE]

SET batchCount = 1000;
SET chunkCount = 1000;
SET rowCount = 0;
SET totalDeleted = 0;
SET cleanUpRequestsTimeLimit = 60; -- SET SAFE PERIOD IN DAYS FOR REQUEST DELETE [DEFAULT : 60 days (2 months)]
SET deleteTillTime = DATE_SUB(NOW(), INTERVAL cleanUpRequestsTimeLimit DAY);

IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'WSO2_WF_REQUEST_CLEANUP() STARTED...!' AS 'INFO LOG';
    SELECT CONCAT('Cleanup Period: ', cleanUpRequestsTimeLimit, ' days') AS 'INFO LOG';
    SELECT CONCAT('Cleanup Date Limit: ', deleteTillTime) AS 'INFO LOG';
    SELECT 'Target Statuses: APPROVED, REJECTED, FAILED, ABORTED' AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

-- ------------------------------------------
-- BACKUP DATA
-- ------------------------------------------
IF (backupTables) THEN
    IF (enableLog) THEN
        SELECT 'Starting backup process...' AS 'INFO LOG';
    END IF;
    
    OPEN tablesCursor;
    backupLoop: LOOP
        FETCH tablesCursor INTO cursorTable;
        
        IF cursorLoopFinished = 1 THEN
            LEAVE backupLoop;
        END IF;
        
        SET backupTable = CONCAT(cursorTable, '_BACKUP');
        
        -- Check if backup table exists and drop it.
        SET @dropTab = CONCAT('DROP TABLE IF EXISTS ', backupTable);
        PREPARE stmtDrop FROM @dropTab;
        EXECUTE stmtDrop;
        DEALLOCATE PREPARE stmtDrop;
        
        IF (enableLog) THEN
            SELECT CONCAT('Dropped existing backup table: ', backupTable) AS 'INFO LOG';
        END IF;
        
        -- Create backup only for records that will be deleted.
        IF (cursorTable = 'WF_REQUEST') THEN
            SET @createTab = CONCAT(
                'CREATE TABLE ', backupTable, ' AS ',
                'SELECT * FROM ', cursorTable, ' ',
                'WHERE STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ',
                'AND UPDATED_AT < ''', deleteTillTime, ''''
            );
        ELSEIF (cursorTable = 'WF_WORKFLOW_REQUEST_RELATION') THEN
            SET @createTab = CONCAT(
                'CREATE TABLE ', backupTable, ' AS ',
                'SELECT t.* FROM ', cursorTable, ' t ',
                'WHERE EXISTS (',
                '    SELECT 1 FROM WF_REQUEST r ',
                '    WHERE r.UUID = t.REQUEST_ID ',
                '    AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ',
                '    AND r.UPDATED_AT < ''', deleteTillTime, '''',
                ')'
            );
        ELSEIF (cursorTable = 'WF_WORKFLOW_APPROVAL_RELATION') THEN
            SET @createTab = CONCAT(
                'CREATE TABLE ', backupTable, ' AS ',
                'SELECT t.* FROM ', cursorTable, ' t ',
                'WHERE EXISTS (',
                '    SELECT 1 FROM WF_REQUEST r ',
                '    WHERE r.UUID = t.EVENT_ID ',
                '    AND r.STATUS IN (''APPROVED'', ''REJECTED'', ''FAILED'', ''DELETED'', ''ABORTED'') ',
                '    AND r.UPDATED_AT < ''', deleteTillTime, '''',
                ')'
            );
        END IF;
        
        PREPARE stmtCreate FROM @createTab;
        EXECUTE stmtCreate;
        DEALLOCATE PREPARE stmtCreate;
        
        SET @rowCountQuery = CONCAT('SELECT COUNT(1) INTO @backupCount FROM ', backupTable);
        PREPARE stmtCount FROM @rowCountQuery;
        EXECUTE stmtCount;
        DEALLOCATE PREPARE stmtCount;
        
        IF (enableLog) THEN
            SELECT CONCAT('Created backup table ', backupTable, ' with ', @backupCount, ' records') AS 'INFO LOG';
        END IF;
    END LOOP;
    CLOSE tablesCursor;
END IF;

-- ------------------------------------------
-- CLEANUP DATA
-- ------------------------------------------
IF (enableLog) THEN
    SELECT 'Starting cleanup process...' AS 'INFO LOG';
END IF;

chunkLoop: REPEAT
    -- CREATE CHUNK TABLE WITH ELIGIBLE REQUEST IDs.
    DROP TEMPORARY TABLE IF EXISTS WF_REQUEST_CHUNK_TMP;
    CREATE TEMPORARY TABLE WF_REQUEST_CHUNK_TMP AS 
        SELECT UUID FROM WF_REQUEST
        WHERE STATUS IN ('APPROVED', 'REJECTED', 'FAILED', 'DELETED', 'ABORTED')
        AND UPDATED_AT < deleteTillTime
        LIMIT chunkSize;
    
    SELECT COUNT(1) INTO chunkCount FROM WF_REQUEST_CHUNK_TMP;
    
    IF (enableLog) THEN
        SELECT CONCAT('Created WF_REQUEST_CHUNK_TMP with ', chunkCount, ' records') AS 'INFO LOG';
    END IF;
    
    IF (chunkCount = 0) THEN
        LEAVE chunkLoop;
    END IF;
    
    -- CREATE INDEX ON CHUNK TABLE.
    CREATE INDEX IDX_WF_REQUEST_CHUNK_TMP ON WF_REQUEST_CHUNK_TMP(UUID);
    
    -- BATCH LOOP.
    batchLoop: REPEAT
        -- CREATE BATCH TABLE.
        DROP TEMPORARY TABLE IF EXISTS WF_REQUEST_BATCH_TMP;
        CREATE TEMPORARY TABLE WF_REQUEST_BATCH_TMP AS
            SELECT UUID FROM WF_REQUEST_CHUNK_TMP LIMIT batchSize;
        
        SELECT COUNT(1) INTO batchCount FROM WF_REQUEST_BATCH_TMP;
        
        IF (batchCount = 0) THEN
            LEAVE batchLoop;
        END IF;
        
        IF (enableLog) THEN
            SELECT CONCAT('Processing batch with ', batchCount, ' records...') AS 'INFO LOG';
        END IF;
        
        -- DELETE FROM WF_REQUEST (CASCADE DELETE will handle child tables automatically).
        DELETE FROM WF_REQUEST
        WHERE UUID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);
        
        SELECT ROW_COUNT() INTO rowCount;
        SET totalDeleted = totalDeleted + rowCount;
        
        IF (enableLog) THEN
            SELECT CONCAT('  Deleted ', rowCount, ' records from WF_REQUEST (and cascaded to child tables)') AS 'INFO LOG';
        END IF;
        
        -- DELETE FROM CHUNK.
        DELETE FROM WF_REQUEST_CHUNK_TMP 
        WHERE UUID IN (SELECT UUID FROM WF_REQUEST_BATCH_TMP);
        
    UNTIL batchCount = 0 END REPEAT;
    
UNTIL chunkCount = 0 END REPEAT;

-- DELETE TEMP TABLES.
DROP TEMPORARY TABLE IF EXISTS WF_REQUEST_CHUNK_TMP;
DROP TEMPORARY TABLE IF EXISTS WF_REQUEST_BATCH_TMP;

IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'WSO2_WF_REQUEST_CLEANUP() COMPLETED!' AS 'INFO LOG';
    SELECT CONCAT('Total WF_REQUEST records deleted: ', totalDeleted) AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

END$$

DELIMITER ;

-- To execute the cleanup procedure:
-- CALL WSO2_WF_REQUEST_CLEANUP();
