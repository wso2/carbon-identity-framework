CREATE OR ALTER PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP_RESTORE AS
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
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP DATA RESTORATION STARTED .... !' AS 'INFO_LOG';
    END;

    SELECT @rowCount = COUNT(1)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_NAME IN ('IDN_FLOW_CONTEXT_STORE');

    IF (@rowCount = 1)
    BEGIN
        INSERT INTO IDN_FLOW_CONTEXT_STORE (ID, TENANT_ID, FLOW_TYPE, CREATED_AT, EXPIRES_AT, FLOW_STATE_JSON)
        SELECT A.ID, A.TENANT_ID, A.FLOW_TYPE, A.CREATED_AT, A.EXPIRES_AT, A.FLOW_STATE_JSON
        FROM BAK_IDN_FLOW_CONTEXT_STORE A
        LEFT JOIN IDN_FLOW_CONTEXT_STORE B ON A.ID = B.ID
        WHERE B.ID IS NULL;

        SET @rowCount = @@ROWCOUNT;

        IF (@enableLog = 1)
        BEGIN
            SELECT CONCAT('CLEANUP DATA RESTORATION COMPLETED ON IDN_FLOW_CONTEXT_STORE WITH ', @rowCount) AS 'INFO_LOG';
        END;
    END;

    IF (@enableLog = 1)
    BEGIN
        SELECT '[' + CONVERT(VARCHAR, GETDATE(), 121) + '] CLEANUP DATA RESTORATION COMPLETED .... !' AS 'INFO_LOG';
    END;
END;
