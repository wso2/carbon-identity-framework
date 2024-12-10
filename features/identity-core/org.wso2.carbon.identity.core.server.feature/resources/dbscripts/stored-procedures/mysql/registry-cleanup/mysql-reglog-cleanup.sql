DROP PROCEDURE IF EXISTS `WSO2_REG_LOG_CLEANUP`;

DELIMITER $$

CREATE PROCEDURE `WSO2_REG_LOG_CLEANUP`()
BEGIN

-- ------------------------------------------
-- DECLARE VARIABLES
-- ------------------------------------------
DECLARE batchSize INT;
DECLARE chunkSize INT;
DECLARE sleepTime FLOAT;
DECLARE checkCount INT;
DECLARE enableLog BOOLEAN;
DECLARE backupTables BOOLEAN;
DECLARE logLevel VARCHAR(10);
DECLARE rowCount INT;
DECLARE cleaupCount INT;

-- ------------------------------------------
-- CONFIGURABLE VARIABLES
-- ------------------------------------------

SET batchSize = 10000;      -- SET BATCH SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 10000]
SET chunkSize = 500000;    -- SET TEMP TABLE CHUNK SIZE FOR AVOID TABLE LOCKS    [DEFAULT : 500000]
SET sleepTime = 2;          -- Sleep time in seconds.[DEFAULT : 2] 
SET checkCount = 1000; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 1000]
SET enableLog = FALSE;	-- SET TRUE IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
SET backupTables = FALSE;    -- SET TRUE IF REG PROPROTIES TO BACKUP BEFORE DELETE [DEFAULT : FALSE].
SET logLevel = 'DEBUG';    -- SET LOG LEVELS : INFO, TRACE , DEBUG
SET rowCount=0;	
SET autocommit = 0;
SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

SELECT 'INFO : WSO2_REG_LOG_CLEANUP() STARTED .... !';


IF (backupTables)
THEN
    IF (NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'REG_LOG_BACKUP' and TABLE_SCHEMA in (SELECT DATABASE())))
    THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT 'TRACE : CREATING BACKUP TABLE REG_LOG_BACKUP .. !';
        END IF;
        CREATE TABLE REG_LOG_BACKUP SELECT * FROM REG_LOG WHERE 1 = 2;
    ELSE
        IF (enableLog AND logLevel IN ('TRACE')) THEN
        SELECT 'TRACR :USING PREVIOUS BACKUP TABLE REG_LOG_BACKUP ..!';
        END IF;
    END IF;
END IF;



-- ------------------------------------------------------
-- CALCULATING REG_LOG
-- ------------------------------------------------------
IF (enableLog)
THEN
    SELECT 'INFO : CALCULATING DELETE ELIGIBLE ON REG_LOG TABLE .... !';

    IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
    THEN
    SELECT  COUNT(1) into rowcount FROM REG_LOG;
    SELECT 'DEBUG : TOTAL REG_LOG TABLE BEFORE DELETE ', rowcount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
  SELECT COUNT(1) into cleaupCount FROM (
  (SELECT RL.REG_LOG_ID FROM REG_LOG RL LEFT JOIN (
  SELECT MAX(REG_LOG_ID) AS REG_LOG_ID FROM REG_LOG GROUP BY REG_PATH, REG_TENANT_ID) X
  ON RL.REG_LOG_ID = X.REG_LOG_ID
  WHERE X.REG_LOG_ID IS NULL)
  UNION
  (SELECT REG_LOG_ID FROM REG_LOG WHERE REG_ACTION = 7))A;
    SELECT 'TRACE : TOTAL REG_LOG SHOULD BE DELETED ON ', cleaupCount;
    END IF;

    IF (enableLog AND logLevel IN ('TRACE'))
    THEN
    SET rowcount  = (rowcount - cleaupCount);
    SELECT 'TRACE : TOTAL REG_LOG SHOULD BE RETAIN ', rowcount;
    END IF;
END IF;

-- ------------------------------------------
-- PURGE REG_LOG
-- ------------------------------------------

-- RUN UNTILL 
RL_CHUNK_LOOP: REPEAT

DROP TABLE IF EXISTS REG_LOG_CHUNK_TMP;

CREATE TABLE REG_LOG_CHUNK_TMP SELECT REG_LOG_ID FROM (
  (SELECT RL.REG_LOG_ID FROM REG_LOG RL LEFT JOIN (
  SELECT MAX(REG_LOG_ID) AS REG_LOG_ID FROM REG_LOG GROUP BY REG_PATH, REG_TENANT_ID) X
  ON RL.REG_LOG_ID = X.REG_LOG_ID
  WHERE X.REG_LOG_ID IS NULL)
  UNION
  (SELECT REG_LOG_ID FROM REG_LOG WHERE REG_ACTION = 7)
    ) A LIMIT chunkSize ;

SELECT count(1) INTO @chunkCount FROM REG_LOG_CHUNK_TMP;

IF (@chunkCount<checkCount OR @chunkCount=0)
THEN
IF (@chunkCount<checkCount)
THEN
SELECT 'INFO : EXIT LOOP HENCE DELETE ELIGIBLE COUNT IS LESS THAN CHECK_COUNT DIFINED ', @chunkCount AS 'DELETE ELIGIBLE' , checkCount  AS 'CHECK_COUNT' ;
END IF;
LEAVE RL_CHUNK_LOOP;
END IF;


IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
THEN
SELECT 'DEBUG : REG_LOG_CHUNK_TMP TABLE CREATED WITH ', @chunkCount ;
END IF;

CREATE INDEX REG_LOG_CHUNK_TMP_INDX on REG_LOG_CHUNK_TMP (REG_LOG_ID);

COMMIT;

        RL_BATCH_LOOP: REPEAT

		DROP TABLE IF EXISTS REG_LOG_BATCH_TMP;

        CREATE TABLE REG_LOG_BATCH_TMP SELECT REG_LOG_ID FROM REG_LOG_CHUNK_TMP LIMIT batchSize;

        SELECT count(1) INTO @batchCount FROM REG_LOG_BATCH_TMP;
        
		IF (@batchCount=0 )
        THEN
        LEAVE RL_BATCH_LOOP;
        END IF;

        IF (backupTables)
        THEN
        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        SELECT 'TRACE : BACKING UP TABLES REG_LOG_BACKUP' ;
        END IF;
        INSERT INTO REG_LOG_BACKUP SELECT RL.* FROM  REG_LOG AS RL INNER JOIN  REG_LOG_BATCH_TMP AS RLB WHERE RL.REG_LOG_ID = RLB.REG_LOG_ID;
        COMMIT;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        SELECT 'TRACE : BATCH DELETE STARTED ON REG_LOG :',  @batchCount;
        END IF;

        DELETE A
        FROM REG_LOG AS A
        INNER JOIN REG_LOG_BATCH_TMP AS B
        ON A.REG_LOG_ID = B.REG_LOG_ID;
	
        SELECT row_count() INTO rowCount;
		COMMIT;
        IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
        THEN
        SELECT 'DEBUG : BATCH DELETE FINISHED ON REG_LOG :', rowCount;
        END IF;

        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        SELECT 'TRACE : BATCH DELETE STARTED ON REG_LOG_CHUNK_TMP :',  @batchCount;
        END IF;

        DELETE A
        FROM REG_LOG_CHUNK_TMP AS A
        INNER JOIN REG_LOG_BATCH_TMP AS B
        ON A.REG_LOG_ID = B.REG_LOG_ID;
		COMMIT;
        IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        SELECT 'TRACE : BATCH DELETE FINISHED ON REG_LOG_CHUNK_TMP :';
        END IF;

        IF ((rowCount > 0))
        THEN
            IF (enableLog AND logLevel IN ('TRACE'))
        THEN
        SELECT 'TRACE : SLEEPING FOR SECONDS :',  sleepTime;
        END IF;
        DO SLEEP(sleepTime);
        END IF;

        UNTIL @batchCount=0 END REPEAT;

UNTIL @chunkCount=0 END REPEAT;


-- CLEANUP ANY EXISTING TEMP TABLES
IF (enableLog AND logLevel IN ('TRACE'))
THEN
SELECT 'TRACE : DROP TEMP TABLES REG_LOG_CHUNK_TMP';
DROP TABLE IF EXISTS REG_LOG_CHUNK_TMP;
DROP TABLE IF EXISTS REG_LOG_BATCH_TMP;
END IF;

-- ------------------------------------------------------
-- CALCULATING REG_LOG
-- ------------------------------------------------------
IF (enableLog AND logLevel IN ('DEBUG','TRACE'))
THEN
    SELECT  COUNT(1) into rowcount FROM REG_LOG;
    SELECT 'DEBUG : TOTAL REG_LOG TABLE AFTER DELETE ', rowcount;

END IF;

SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

IF (enableLog)
THEN
SELECT 'INFO : WSO2_REG_LOG_CLEANUP TASK COMPLETED .... !';
END IF;


END$$

DELIMITER ;
