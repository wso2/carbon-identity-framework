DROP PROCEDURE IF EXISTS `WSO2_PAR_REQUEST_CLEANUP_RESTORE`;
DELIMITER $$
CREATE PROCEDURE `WSO2_PAR_REQUEST_CLEANUP_RESTORE`()
BEGIN
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE rowCount INT;
    DECLARE enableLog BOOLEAN;

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    SET enableLog = FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]

    IF (enableLog) THEN
        SELECT  'CLEANUP DATA RESTORATION STARTED...!' AS 'INFO LOG';
    END IF;

    SELECT COUNT(1) INTO rowcount  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA IN (SELECT DATABASE())  AND TABLE_NAME IN ('IDN_OAUTH_PAR');
    IF (rowcount = 1)
    THEN
        INSERT INTO IDN_OAUTH_PAR SELECT A.* FROM BAK_IDN_OAUTH_PAR A LEFT JOIN IDN_OAUTH_PAR B ON A.REQ_URI_REF = B.REQ_URI_REF WHERE B.REQ_URI_REF IS NULL;
        SELECT row_count() INTO rowCount;
        IF (enableLog) THEN
            SELECT 'CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH_PAR WITH %' + rowCount AS 'INFO LOG';
        END IF;
    END IF;

    IF (enableLog) THEN
        SELECT  'CLEANUP DATA RESTORATION COMPLETED...!';
    END IF;

END$$

DELIMITER ;
