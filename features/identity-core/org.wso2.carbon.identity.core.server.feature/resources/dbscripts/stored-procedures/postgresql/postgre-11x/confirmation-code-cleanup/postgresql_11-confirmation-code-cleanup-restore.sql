CREATE OR REPLACE PROCEDURE WSO2_CONFIRMATION_CODE_RESTORE() AS $$
DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount int;
    enableLog boolean;

BEGIN
    enableLog := false; -- ENABLE LOGGING [DEFAULT : FALSE]

    IF (enableLog) THEN
        RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED .... !';
    END IF;

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' INTO rowCount USING 'idn_recovery_flow_data';
    IF (rowCount = 1)
    THEN
        INSERT INTO idn_recovery_flow_data SELECT A.* FROM idn_recovery_flow_data_backup A LEFT JOIN idn_recovery_flow_data B ON A.recovery_flow_id = B.recovery_flow_id WHERE B.recovery_flow_id IS NULL;
        GET DIAGNOSTICS rowCount := ROW_COUNT;
        IF (enableLog) THEN
            RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON IDN_RECOVERY_FLOW_DATA WITH %', rowCount;
        END IF;
    END IF;

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' INTO rowCount USING 'idn_recovery_data';
    IF (rowCount = 1)
    THEN
        INSERT INTO idn_recovery_data SELECT A.* FROM idn_recovery_data_backup A LEFT JOIN idn_recovery_data B ON A.code = B.code WHERE B.code IS NULL;
        GET DIAGNOSTICS rowCount := ROW_COUNT;
        IF (enableLog) THEN
            RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON IDN_RECOVERY_DATA WITH %', rowCount;
        END IF;
    END IF;

END;
$$
LANGUAGE 'plpgsql';
