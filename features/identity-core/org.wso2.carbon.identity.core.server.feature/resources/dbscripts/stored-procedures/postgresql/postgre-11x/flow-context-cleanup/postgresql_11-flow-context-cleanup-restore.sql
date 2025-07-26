CREATE OR REPLACE PROCEDURE WSO2_IDN_FLOW_CONTEXT_RESTORE() AS $$
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

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = $1' INTO rowCount USING 'idn_flow_context_store';

    IF (rowCount = 1) THEN
        INSERT INTO idn_flow_context_store
        SELECT A.*
        FROM idn_flow_context_store_backup A
        LEFT JOIN idn_flow_context_store B ON A.id = B.id
        WHERE B.id IS NULL;

        GET DIAGNOSTICS rowCount := ROW_COUNT;

        IF (enableLog) THEN
            RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON IDN_FLOW_CONTEXT_STORE WITH %', rowCount;
        END IF;
    END IF;

END;
$$ LANGUAGE 'plpgsql';
