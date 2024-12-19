CREATE OR REPLACE PROCEDURE WSO2_PAR_REQUEST_CLEANUP_RESTORE
BEGIN
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE rowCount INT;
    DECLARE enableLog SMALLINT;

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    SET enableLog = 1; -- ENABLE LOGGING [DEFAULT : TRUE]

    IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION STARTED .... !');
    END IF;

    SELECT COUNT(*) INTO rowCount FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'IDN_OAUTH_PAR';
    IF (rowCount = 1)
    THEN
        INSERT INTO IDN_OAUTH_PAR SELECT A.* FROM BAK_IDN_OAUTH_PAR A LEFT JOIN IDN_OAUTH_PAR B ON A.REQ_URI_REF = B.REQ_URI_REF WHERE B.REQ_URI_REF IS NULL;
        GET DIAGNOSTICS rowCount = ROW_COUNT;
        IF (enableLog = 1)
        THEN
        	CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH_PAR');
        END IF;
    END IF;

    IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION COMPLETED .... !');
    END IF;
END/
