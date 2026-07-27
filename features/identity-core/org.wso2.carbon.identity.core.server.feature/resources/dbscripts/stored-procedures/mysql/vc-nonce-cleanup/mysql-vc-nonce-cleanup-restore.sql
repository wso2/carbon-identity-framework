DROP PROCEDURE IF EXISTS `WSO2_VC_NONCE_CLEANUP_RESTORE`;
DELIMITER $$
CREATE PROCEDURE `WSO2_VC_NONCE_CLEANUP_RESTORE`()
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
        SELECT 'CLEANUP DATA RESTORATION STARTED...!' AS 'INFO LOG';
    END IF;

    SELECT COUNT(1)
    INTO rowCount
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA IN (SELECT DATABASE())
      AND TABLE_NAME IN ('IDN_VC_NONCE');

    IF (rowCount = 1) THEN

        SELECT COUNT(1)
        INTO rowCount
        FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_SCHEMA IN (SELECT DATABASE())
          AND TABLE_NAME IN ('BAK_IDN_VC_NONCE');

        IF (rowCount = 1) THEN
            INSERT INTO IDN_VC_NONCE
            SELECT A.*
            FROM BAK_IDN_VC_NONCE A
                     LEFT JOIN IDN_VC_NONCE B
                               ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE
            WHERE B.NONCE IS NULL;

            SELECT ROW_COUNT() INTO rowCount;

            IF (enableLog) THEN
                SELECT CONCAT('CLEANUP DATA RESTORATION COMPLETED ON IDN_VC_NONCE WITH ', rowCount) AS 'INFO LOG';
            END IF;
        END IF;
    END IF;

    IF (enableLog) THEN
        SELECT 'CLEANUP DATA RESTORATION COMPLETED...!' AS 'INFO LOG';
    END IF;

END$$

DELIMITER ;
