CREATE PROCEDURE WSO2_JTI_CLEANUP_DATA_RESTORE_SP
AS

BEGIN

DECLARE @rowCount INT;
DECLARE @enableLog BIT;
DECLARE @logLevel VARCHAR(10);

-- ------------------------------------------
-- CONFIGURABLE ATTRIBUTES
-- ------------------------------------------
SET @enableLog = 'TRUE'; -- ENABLE LOGGING [DEFAULT : TRUE]
SET @logLevel = 'TRACE'; -- SET LOG LEVELS : TRACE


IF (@enableLog = 1) BEGIN
    SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED .... !';
END

------------------------

SELECT @rowCount = COUNT(1)  FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('IDN_OIDC_JTI');
IF (@rowCount = 1)
BEGIN
    IF (@enableLog = 1 AND @logLevel IN ('TRACE'))
    BEGIN
        SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION STARTED ON IDN_OIDC_JTI TABLE !';
    END
    INSERT INTO dbo.IDN_OIDC_JTI SELECT A.* FROM dbo.BAK_IDN_OIDC_JTI AS A LEFT JOIN dbo.IDN_OIDC_JTI AS B ON A.JWT_ID = B.JWT_ID AND A.TENANT_ID = B.TENANT_ID  WHERE B.JWT_ID IS NULL AND B.TENANT_ID IS NULL ;
    SELECT @rowCount =  @@rowcount;

    IF (@enableLog = 1 )
    BEGIN
        SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED ON IDN_OIDC_JTI WITH '+CAST(@rowCount as varchar)
    END
END

IF (@enableLog = 1) BEGIN
    SELECT  '[' + convert(varchar, getdate(), 121) + '] CLEANUP DATA RESTORATION COMPLETED .... !';
END

END

END
