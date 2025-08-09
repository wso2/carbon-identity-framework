CREATE OR REPLACE PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP_RESTORE IS
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount INT := 0;
    current_schema VARCHAR(20);

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog BOOLEAN := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]

BEGIN

    SELECT SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;

    IF enableLog THEN
        EXECUTE IMMEDIATE '
            INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP (LOG_TIME, LOG)
            VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CLEANUP DATA RESTORATION STARTED...!'')';
    END IF;

    SELECT COUNT(*) INTO rowCount
    FROM ALL_TABLES
    WHERE OWNER = current_schema AND TABLE_NAME = 'IDN_FLOW_CONTEXT_STORE';

    IF rowCount = 1 THEN
        EXECUTE IMMEDIATE '
            INSERT INTO IDN_FLOW_CONTEXT_STORE
            SELECT A.* FROM BAK_IDN_FLOW_CONTEXT_STORE A
            LEFT JOIN IDN_FLOW_CONTEXT_STORE B ON A.ID = B.ID
            WHERE B.ID IS NULL';

        rowCount := SQL%ROWCOUNT;

        IF enableLog THEN
            EXECUTE IMMEDIATE '
                INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP (LOG_TIME, LOG)
                VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CLEANUP DATA RESTORATION COMPLETED ON IDN_FLOW_CONTEXT_STORE WITH ' || rowCount || ''')';
        END IF;

        COMMIT;
    END IF;

    IF enableLog THEN
        EXECUTE IMMEDIATE '
            INSERT INTO LOG_WSO2_FLOW_CONTEXT_CLEANUP (LOG_TIME, LOG)
            VALUES (TO_CHAR(SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS.FF3''), ''CLEANUP DATA RESTORATION COMPLETED...!'')';
        COMMIT;
    END IF;

END;
