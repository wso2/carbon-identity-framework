CREATE OR REPLACE PROCEDURE WSO2_VC_NONCE_CLEANUP() AS $$
DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    batchSize int;
    chunkSize int;
    batchCount int;
    chunkCount int;
    rowCount int;
    enableLog boolean;
    backupTables boolean;
    cleanUpDateTimeLimit timestamp;

BEGIN

    -- ------------------------------------------
    -- CONFIGURE VARIABLES
    -- ------------------------------------------
    batchSize := 10000; -- SET BATCH SIZE FOR AVOID TABLE LOCKS [DEFAULT : 10000]
    chunkSize := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFAULT : 500000]
    backupTables := FALSE; -- SET IF VC NONCE TABLE NEEDS TO BE BACKED-UP BEFORE DELETE [DEFAULT : FALSE]
    enableLog := FALSE; -- ENABLE LOGGING [DEFAULT : FALSE]

    batchCount := 1000;
    chunkCount := 1000;
    rowCount := 0;
    cleanUpDateTimeLimit := timezone('UTC'::text, now());

    IF (enableLog) THEN
        RAISE NOTICE 'WSO2_VC_NONCE_CLEANUP() STARTED...!';
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables) THEN
        EXECUTE 'SELECT count(1) FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND tablename = $1'
            INTO rowCount USING 'bak_idn_vc_nonce';

        IF (rowCount = 1) THEN
            EXECUTE 'DROP TABLE bak_idn_vc_nonce';
        END IF;

        EXECUTE 'CREATE TABLE bak_idn_vc_nonce AS SELECT * FROM idn_vc_nonce';
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    LOOP
        EXIT WHEN chunkCount = 0;

        -- CREATE CHUNK TABLE
        DROP TABLE IF EXISTS idn_vc_nonce_chunk_tmp;
        CREATE TABLE idn_vc_nonce_chunk_tmp AS
        SELECT tenant_id, nonce
        FROM idn_vc_nonce
        WHERE expiry_time < cleanUpDateTimeLimit
        LIMIT chunkSize;

        GET DIAGNOSTICS chunkCount := ROW_COUNT;
        COMMIT;

        EXIT WHEN chunkCount = 0;

        IF (enableLog) THEN
            RAISE NOTICE 'CREATED IDN_VC_NONCE_CHUNK_TMP...';
        END IF;

        -- BATCH LOOP
        batchCount := 1;
        LOOP
            -- CREATE BATCH TABLE
            DROP TABLE IF EXISTS idn_vc_nonce_batch_tmp;
            CREATE TABLE idn_vc_nonce_batch_tmp AS
            SELECT tenant_id, nonce
            FROM idn_vc_nonce_chunk_tmp
            LIMIT batchSize;

            GET DIAGNOSTICS batchCount := ROW_COUNT;
            COMMIT;

            EXIT WHEN batchCount = 0;

            IF (enableLog) THEN
                RAISE NOTICE 'CREATED IDN_VC_NONCE_BATCH_TMP...';
            END IF;

            -- BATCH DELETION
            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE STARTED ON IDN_VC_NONCE...';
            END IF;

            DELETE FROM idn_vc_nonce A
            USING idn_vc_nonce_batch_tmp B
            WHERE A.tenant_id = B.tenant_id
              AND A.nonce = B.nonce;

            GET DIAGNOSTICS rowCount := ROW_COUNT;
            COMMIT;

            IF (enableLog) THEN
                RAISE NOTICE 'BATCH DELETE FINISHED ON IDN_VC_NONCE : %', rowCount;
            END IF;

            -- DELETE FROM CHUNK
            DELETE FROM idn_vc_nonce_chunk_tmp A
            USING idn_vc_nonce_batch_tmp B
            WHERE A.tenant_id = B.tenant_id
              AND A.nonce = B.nonce;
        END LOOP;
    END LOOP;

    -- DELETE TEMP TABLES
    DROP TABLE IF EXISTS idn_vc_nonce_chunk_tmp;
    DROP TABLE IF EXISTS idn_vc_nonce_batch_tmp;

    IF (enableLog) THEN
        RAISE NOTICE 'CLEANUP COMPLETED...!';
    END IF;

END;
$$
LANGUAGE 'plpgsql';
