CREATE OR REPLACE PROCEDURE WSO2_PAR_REQUEST_RESTORE() AS $$
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

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename =  $1' INTO rowCount USING 'idn_oauth_par';
    IF (rowCount = 1)
    THEN
        INSERT INTO idn_oauth_par SELECT A.* FROM idn_oauth_par_backup A LEFT JOIN idn_oauth_par B ON A.req_uri_ref = B.req_uri_ref WHERE B.req_uri_ref IS NULL;
        GET DIAGNOSTICS rowCount := ROW_COUNT;
        IF (enableLog) THEN
            RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON IDN_OAUTH_PAR WITH %', rowCount;
        END IF;
    END IF;

END;
$$
LANGUAGE 'plpgsql';
