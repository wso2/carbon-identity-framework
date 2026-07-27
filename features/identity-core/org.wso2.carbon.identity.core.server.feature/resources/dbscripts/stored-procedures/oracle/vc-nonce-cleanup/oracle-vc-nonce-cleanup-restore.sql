CREATE OR REPLACE PROCEDURE WSO2_VC_NONCE_CLEANUP_RESTORE IS

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount INT := 0;
    current_schema VARCHAR2(30);
    logTableCount INT := 0;

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog BOOLEAN := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]

BEGIN

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;

    IF (enableLog) THEN
        SELECT COUNT(*) INTO logTableCount FROM all_tables
        WHERE owner = current_schema AND table_name = UPPER('LOG_WSO2_VC_NONCE_CLEANUP');

        IF (logTableCount = 0) THEN
            EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP VARCHAR2(250), LOG VARCHAR2(250)) NOLOGGING';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                          || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                          || '''CLEANUP DATA RESTORATION STARTED...!'')';
    END IF;

    SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
    WHERE OWNER = current_schema AND table_name = 'IDN_VC_NONCE';

    IF (rowCount = 1) THEN

        SELECT COUNT(*) INTO rowCount FROM ALL_TABLES
        WHERE OWNER = current_schema AND table_name = 'BAK_IDN_VC_NONCE';

        IF (rowCount = 1) THEN
            EXECUTE IMMEDIATE 'INSERT INTO IDN_VC_NONCE '
                              || 'SELECT A.* FROM BAK_IDN_VC_NONCE A '
                              || 'LEFT JOIN IDN_VC_NONCE B '
                              || 'ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE '
                              || 'WHERE B.NONCE IS NULL';

            rowCount := SQL%rowcount;

            IF (enableLog) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                                  || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                                  || '''CLEANUP DATA RESTORATION COMPLETED ON IDN_VC_NONCE WITH ' || rowCount || ''')';
            END IF;
            COMMIT;
        END IF;
    END IF;

    IF (enableLog) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_VC_NONCE_CLEANUP (TIMESTAMP, LOG) VALUES '
                          || '(TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''), '
                          || '''CLEANUP DATA RESTORATION COMPLETED...!'')';
        COMMIT;
    END IF;
END;
