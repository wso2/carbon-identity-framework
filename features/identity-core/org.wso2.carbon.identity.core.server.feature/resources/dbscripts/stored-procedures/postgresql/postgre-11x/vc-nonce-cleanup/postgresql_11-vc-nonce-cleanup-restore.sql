CREATE OR REPLACE PROCEDURE WSO2_VC_NONCE_RESTORE() AS $$
DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount int;
    enableLog boolean;

BEGIN

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]

    IF (enableLog) THEN
        RAISE NOTICE 'CLEANUP DATA RESTORATION STARTED .... !';
    END IF;

    EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = $1'
        INTO rowCount USING 'idn_vc_nonce';

    IF (rowCount = 1) THEN
        EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = $1'
            INTO rowCount USING 'bak_idn_vc_nonce';

        IF (rowCount = 1) THEN
            INSERT INTO idn_vc_nonce
            SELECT A.*
            FROM bak_idn_vc_nonce A
                     LEFT JOIN idn_vc_nonce B
                               ON A.tenant_id = B.tenant_id AND A.nonce = B.nonce
            WHERE B.nonce IS NULL;

            GET DIAGNOSTICS rowCount := ROW_COUNT;

            IF (enableLog) THEN
                RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON IDN_VC_NONCE WITH %', rowCount;
            END IF;
        END IF;
    END IF;

    IF (enableLog) THEN
        RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED .... !';
    END IF;

END;
$$
LANGUAGE 'plpgsql';
