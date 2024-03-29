CREATE OR REPLACE PROCEDURE WSO2_PAR_REQUEST_CLEANUP_RESTORE IS
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount INT := 0;
    current_schema VARCHAR(20);

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog BOOLEAN := TRUE; -- ENABLE LOGGING [DEFAULT : FALSE]

BEGIN

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;

    IF (enableLog)
    THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_PAR_REQUEST_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION STARTED...!'')';
    END IF;

    SELECT COUNT(*) INTO rowCount FROM ALL_TABLES WHERE OWNER = current_schema AND table_name = 'IDN_OAUTH_PAR';
    IF (rowCount = 1)
    THEN
        EXECUTE IMMEDIATE 'INSERT INTO IDN_OAUTH_PAR SELECT A.* FROM BAK_IDN_OAUTH_PAR A LEFT JOIN IDN_OAUTH_PAR B ON A.REQ_URI_REF = B.REQ_URI_REF WHERE B.REQ_URI_REF IS NULL';
        rowCount:= sql%rowcount;
        IF (enableLog)
        THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_PAR_REQUEST_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH_PAR WITH '||rowCount||''')';
        END IF;
        COMMIT;
    END IF;

    IF (enableLog)
    THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_PAR_REQUEST_CLEANUP (TIMESTAMP,LOG) VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''CLEANUP DATA RESTORATION COMPLETED...!'')';
        COMMIT;
    END IF;
END;
