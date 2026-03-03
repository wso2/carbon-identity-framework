CREATE OR REPLACE PROCEDURE WSO2_VC_NONCE_CLEANUP_RESTORE
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

    IF (enableLog = 1) THEN
        CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION STARTED .... !');
    END IF;

    SELECT COUNT(*) INTO rowCount
    FROM SYSIBM.SYSTABLES
    WHERE CREATOR = CURRENT SCHEMA
      AND NAME = 'IDN_VC_NONCE';

    IF (rowCount = 1) THEN

        SELECT COUNT(*) INTO rowCount
        FROM SYSIBM.SYSTABLES
        WHERE CREATOR = CURRENT SCHEMA
          AND NAME = 'BAK_IDN_VC_NONCE';

        IF (rowCount = 1) THEN
            INSERT INTO IDN_VC_NONCE
            SELECT A.*
            FROM BAK_IDN_VC_NONCE A
                     LEFT JOIN IDN_VC_NONCE B
                               ON A.TENANT_ID = B.TENANT_ID AND A.NONCE = B.NONCE
            WHERE B.NONCE IS NULL;

            GET DIAGNOSTICS rowCount = ROW_COUNT;

            IF (enableLog = 1) THEN
                CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION COMPLETED ON IDN_VC_NONCE');
            END IF;
        END IF;
    END IF;

    IF (enableLog = 1) THEN
        CALL DBMS_OUTPUT.PUT_LINE('CLEANUP DATA RESTORATION COMPLETED .... !');
    END IF;
END/
