CREATE OR ALTER PROCEDURE WSO2_CONFIRMATION_CODE_CLEANUP_RESTORE AS
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

    SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('IDN_RECOVERY_DATA')
    IF (@rowCount = 1)
    BEGIN
        INSERT INTO IDN_RECOVERY_DATA SELECT A.* FROM BAK_IDN_RECOVERY_DATA A LEFT JOIN IDN_RECOVERY_DATA B ON A.CODE = B.CODE WHERE B.CODE IS NULL
        SET @rowCount = @@ROWCOUNT
        IF (@enableLog = 1)
        BEGIN
            SELECT CONCAT('CLEANUP DATA RESTORATION COMPLETED ON IDN_RECOVERY_DATA WITH ', @rowCount) AS 'INFO_LOG'
        END
    END

    IF (@enableLog = 1)
    BEGIN
        SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED .... !' AS 'INFO_LOG'
    END
END
