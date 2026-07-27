CREATE OR ALTER PROCEDURE WSO2_VC_NONCE_CLEANUP_RESTORE AS
BEGIN

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE @rowCount INT
    DECLARE @enableLog BIT

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    SET @enableLog = 1 -- ENABLE LOGGING [DEFAULT : TRUE]

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP DATA RESTORATION STARTED ... !' AS 'INFO LOG'
    END

    SELECT @rowCount = COUNT(1)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME IN ('IDN_VC_NONCE')

    IF (@rowCount = 1)
    BEGIN
        SELECT @rowCount = COUNT(1)
        FROM INFORMATION_SCHEMA.TABLES
        WHERE TABLE_NAME IN ('BAK_IDN_VC_NONCE')

        IF (@rowCount = 1)
        BEGIN
            INSERT INTO IDN_VC_NONCE
            SELECT A.*
            FROM BAK_IDN_VC_NONCE A
                     LEFT JOIN IDN_VC_NONCE B
                               ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE
            WHERE B.NONCE IS NULL

            SET @rowCount = @@ROWCOUNT

            IF (@enableLog = 1)
            BEGIN
                SELECT CONCAT('CLEANUP DATA RESTORATION COMPLETED ON IDN_VC_NONCE WITH ', @rowCount) AS 'INFO LOG'
            END
        END
    END

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ... !' AS 'INFO LOG'
    END
END
