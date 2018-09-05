DROP PROCEDURE IF EXISTS `CLEANUP_SESSION_DATA`;

DELIMITER $$

CREATE PROCEDURE `CLEANUP_SESSION_DATA`()
BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------

DECLARE deletedSessions INT;
DECLARE deletedStoreOperations INT;
DECLARE deletedDeleteOperations INT;
DECLARE tracingEnabled BOOLEAN;
DECLARE sleepTime FLOAT;

-- ------------------------------------------
-- CONFIGURABLE VARIABLES
-- ------------------------------------------

SET @deletedSessions = 0;
SET @deletedStoreOperations = 0;
SET @deletedDeleteOperations = 0;
SET @sessionCleanupCount = 1;
SET @operationCleanupCount =1;
SET tracingEnabled = FALSE;	-- SET IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
SET sleepTime = 2;          -- Sleep time in seconds.

-- Session data older than 20160 minutes(14 days) will be removed.
SET @sessionCleanupTime = unix_timestamp()*1000000000 - (20160*60000000000);
-- Operational data older than 720 minutes(12 h) will be removed.
SET @operationCleanupTime = unix_timestamp()*1000000000 - (720*60000000000);

SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ------------------------------------------
-- REMOVE SESSION DATA
-- ------------------------------------------

SELECT 'CLEANUP_SESSION_DATA() STARTED .... !' AS 'INFO LOG';

WHILE (@sessionCleanupCount > 0) DO

    DELETE FROM IDN_AUTH_SESSION_STORE WHERE  TIME_CREATED < @sessionCleanupTime LIMIT 5000;
    SET @sessionCleanupCount = row_count();

    IF (tracingEnabled)
	THEN
		SET @deletedSessions = @deletedSessions + @sessionCleanupCount;
        SELECT 'REMOVED SESSIONS: ' AS 'INFO LOG', @deletedSessions;
	END IF;

	DO SLEEP(sleepTime);    -- Sleep for some time letting other threads to run.

END WHILE;

IF (tracingEnabled)
THEN
	SELECT 'SESSION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedSessions;
END IF;

SELECT 'SESSION_CLEANUP_TASK ENDED .... !' AS 'INFO LOG';

-- --------------------------------------------
-- REMOVE OPERATIONAL DATA
-- --------------------------------------------

SELECT 'OPERATION_CLEANUP_TASK STARTED .... !' AS 'INFO LOG';

WHILE (@operationCleanupCount > 0) DO

CREATE TABLE IF NOT EXISTS TEMP_SESSION_IDS SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE OPERATION = 'DELETE' AND TIME_CREATED < @operationCleanupTime LIMIT 5000;
SELECT 'TEMPORARY TABLE CREATED...!!' AS 'INFO LOG', @operationCleanupCount;

DELETE A
FROM IDN_AUTH_SESSION_STORE AS A
INNER JOIN TEMP_SESSION_IDS AS B
    ON A.SESSION_ID = B.SESSION_ID
WHERE OPERATION = 'STORE';

SELECT 'DELETE DONE...!!' AS 'INFO LOG', @operationCleanupCount;

SET @operationCleanupCount = row_count();
SELECT 'ROW COUNT...!!' AS 'INFO LOG', @operationCleanupCount;

IF (tracingEnabled)
THEN
	SET @deletedStoreOperations = @operationCleanupCount + @deletedStoreOperations;
    SELECT 'REMOVED STORE OPERATION RECORDS: ' AS 'INFO LOG', @deletedStoreOperations;
END IF;

SET @operationCleanupCount = 0;

DELETE A
FROM IDN_AUTH_SESSION_STORE AS A
INNER JOIN TEMP_SESSION_IDS AS B
    ON A.SESSION_ID = B.SESSION_ID ;

SET @operationCleanupCount = row_count();

IF (tracingEnabled)
THEN
	SET @deletedDeleteOperations = @operationCleanupCount + @deletedDeleteOperations;
    SELECT 'REMOVED DELETE OPERATION RECORDS: ' AS 'INFO LOG', @deletedDeleteOperations;
END IF;

DROP TABLE TEMP_SESSION_IDS;
DO SLEEP(sleepTime);   -- Sleep for some time letting other threads to run.
END WHILE;

IF (tracingEnabled)
THEN
	SELECT 'STORE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedStoreOperations;
	SELECT 'DELETE OPERATION RECORDS REMOVED FROM IDN_AUTH_SESSION_STORE: ' AS 'INFO LOG', @deletedDeleteOperations;
END IF;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

SELECT 'CLEANUP_SESSION_DATA() ENDED .... !' AS 'INFO LOG';

END$$

DELIMITER ;
