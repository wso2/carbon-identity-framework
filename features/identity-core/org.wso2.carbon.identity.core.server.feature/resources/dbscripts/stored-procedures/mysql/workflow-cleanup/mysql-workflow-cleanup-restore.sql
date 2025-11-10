DROP PROCEDURE IF EXISTS `WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP`;

DELIMITER $$

CREATE PROCEDURE `WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP`()

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE rowCount INT;
DECLARE totalRestored INT;
DECLARE enableLog BOOLEAN;
DECLARE logLevel VARCHAR(10);
DECLARE backupTableExists INT;

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET enableLog = TRUE;   -- ENABLE LOGGING [DEFAULT : TRUE]
SET logLevel = 'TRACE'; -- SET LOG LEVELS : TRACE, INFO
SET totalRestored = 0;

IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP STARTED...!' AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

-- ------------------------------------------
-- RESTORE WF_REQUEST TABLE
-- ------------------------------------------
SELECT COUNT(1) INTO backupTableExists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_REQUEST_BACKUP';

IF (backupTableExists = 1) THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT 'RESTORATION STARTED ON WF_REQUEST TABLE...' AS 'TRACE LOG';
    END IF;
    
    INSERT INTO WF_REQUEST 
    SELECT A.* 
    FROM WF_REQUEST_BACKUP A 
    LEFT JOIN WF_REQUEST B ON A.UUID = B.UUID 
    WHERE B.UUID IS NULL;
    
    SELECT ROW_COUNT() INTO rowCount;
    SET totalRestored = totalRestored + rowCount;
    
    IF (enableLog) THEN
        SELECT CONCAT('  Restored ', rowCount, ' records to WF_REQUEST') AS 'INFO LOG';
    END IF;
ELSE
    IF (enableLog) THEN
        SELECT '  Backup table WF_REQUEST_BACKUP not found - skipping' AS 'INFO LOG';
    END IF;
END IF;

-- ------------------------------------------
-- RESTORE WF_WORKFLOW_REQUEST_RELATION TABLE
-- ------------------------------------------
SELECT COUNT(1) INTO backupTableExists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_WORKFLOW_REQUEST_RELATION_BACKUP';

IF (backupTableExists = 1) THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT 'RESTORATION STARTED ON WF_WORKFLOW_REQUEST_RELATION TABLE...' AS 'TRACE LOG';
    END IF;
    
    INSERT INTO WF_WORKFLOW_REQUEST_RELATION 
    SELECT A.* 
    FROM WF_WORKFLOW_REQUEST_RELATION_BACKUP A 
    LEFT JOIN WF_WORKFLOW_REQUEST_RELATION B 
        ON A.RELATIONSHIP_ID = B.RELATIONSHIP_ID
    WHERE B.RELATIONSHIP_ID IS NULL;
    
    SELECT ROW_COUNT() INTO rowCount;
    SET totalRestored = totalRestored + rowCount;
    
    IF (enableLog) THEN
        SELECT CONCAT('  Restored ', rowCount, ' records to WF_WORKFLOW_REQUEST_RELATION') AS 'INFO LOG';
    END IF;
ELSE
    IF (enableLog) THEN
        SELECT '  Backup table WF_WORKFLOW_REQUEST_RELATION_BACKUP not found - skipping' AS 'INFO LOG';
    END IF;
END IF;

-- ------------------------------------------
-- RESTORE WF_WORKFLOW_APPROVAL_RELATION TABLE
-- ------------------------------------------
SELECT COUNT(1) INTO backupTableExists 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_WORKFLOW_APPROVAL_RELATION_BACKUP';

IF (backupTableExists = 1) THEN
    IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT 'RESTORATION STARTED ON WF_WORKFLOW_APPROVAL_RELATION TABLE...' AS 'TRACE LOG';
    END IF;
    
    INSERT INTO WF_WORKFLOW_APPROVAL_RELATION 
    SELECT A.* 
    FROM WF_WORKFLOW_APPROVAL_RELATION_BACKUP A 
    LEFT JOIN WF_WORKFLOW_APPROVAL_RELATION B 
        ON A.TASK_ID = B.TASK_ID
        AND A.APPROVER_TYPE = B.APPROVER_TYPE
        AND A.APPROVER_NAME = B.APPROVER_NAME
    WHERE B.TASK_ID IS NULL;
    
    SELECT ROW_COUNT() INTO rowCount;
    SET totalRestored = totalRestored + rowCount;
    
    IF (enableLog) THEN
        SELECT CONCAT('  Restored ', rowCount, ' records to WF_WORKFLOW_APPROVAL_RELATION') AS 'INFO LOG';
    END IF;
ELSE
    IF (enableLog) THEN
        SELECT '  Backup table WF_WORKFLOW_APPROVAL_RELATION_BACKUP not found - skipping' AS 'INFO LOG';
    END IF;
END IF;

-- ------------------------------------------
-- COMPLETION SUMMARY
-- ------------------------------------------
IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP COMPLETED!' AS 'INFO LOG';
    SELECT CONCAT('Total records restored: ', totalRestored) AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

END$$

DELIMITER ;

-- ------------------------------------------
-- UTILITY PROCEDURE TO DROP BACKUP TABLES
-- ------------------------------------------
DROP PROCEDURE IF EXISTS `WSO2_WF_REQUEST_DROP_BACKUP_TABLES`;

DELIMITER $$

CREATE PROCEDURE `WSO2_WF_REQUEST_DROP_BACKUP_TABLES`()

BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE enableLog BOOLEAN;
DECLARE tableCount INT;

SET enableLog = TRUE;

IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'DROPPING WF BACKUP TABLES...' AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

-- Drop each backup table if it exists.
SELECT COUNT(1) INTO tableCount 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_REQUEST_BACKUP';

IF (tableCount = 1) THEN
    DROP TABLE WF_REQUEST_BACKUP;
    IF (enableLog) THEN
        SELECT 'Dropped table: WF_REQUEST_BACKUP' AS 'INFO LOG';
    END IF;
END IF;

SELECT COUNT(1) INTO tableCount 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_WORKFLOW_REQUEST_RELATION_BACKUP';

IF (tableCount = 1) THEN
    DROP TABLE WF_WORKFLOW_REQUEST_RELATION_BACKUP;
    IF (enableLog) THEN
        SELECT 'Dropped table: WF_WORKFLOW_REQUEST_RELATION_BACKUP' AS 'INFO LOG';
    END IF;
END IF;

SELECT COUNT(1) INTO tableCount 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA IN (SELECT DATABASE()) 
AND TABLE_NAME = 'WF_WORKFLOW_APPROVAL_RELATION_BACKUP';

IF (tableCount = 1) THEN
    DROP TABLE WF_WORKFLOW_APPROVAL_RELATION_BACKUP;
    IF (enableLog) THEN
        SELECT 'Dropped table: WF_WORKFLOW_APPROVAL_RELATION_BACKUP' AS 'INFO LOG';
    END IF;
END IF;

IF (enableLog) THEN
    SELECT '========================================' AS 'INFO LOG';
    SELECT 'BACKUP TABLES DROPPED SUCCESSFULLY!' AS 'INFO LOG';
    SELECT '========================================' AS 'INFO LOG';
END IF;

END$$

DELIMITER ;

-- To restore data from backup tables:
-- CALL WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP();

-- To drop backup tables after successful restoration:
-- CALL WSO2_WF_REQUEST_DROP_BACKUP_TABLES();
