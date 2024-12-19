CREATE OR ALTER PROCEDURE WSO2_PAR_REQUEST_CLEANUP_RESTORE AS
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
        SELECT '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED .... !' AS 'INFO_LOG'
    END

    SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('IDN_OAUTH_PAR')
    IF (@rowCount = 1)
    BEGIN
        INSERT INTO IDN_OAUTH_PAR SELECT A.* FROM BAK_IDN_OAUTH_PAR A LEFT JOIN IDN_OAUTH_PAR B ON A.REQ_URI_REF = B.REQ_URI_REF WHERE B.REQ_URI_REF IS NULL
        SET @rowCount = @@ROWCOUNT
        IF (@enableLog = 1)
        BEGIN
            SELECT CONCAT('CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH_PAR WITH ', @rowCount) AS 'INFO_LOG'
        END
    END

    IF (@enableLog = 1)
    BEGIN
        SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED .... !' AS 'INFO_LOG'
    END
END
