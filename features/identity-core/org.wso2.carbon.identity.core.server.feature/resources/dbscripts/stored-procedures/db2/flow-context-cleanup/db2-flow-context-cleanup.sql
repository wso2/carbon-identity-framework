CREATE OR REPLACE PROCEDURE WSO2_IDN_FLOW_CONTEXT_CLEANUP
BEGIN
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE batchSize INT;
    DECLARE chunkSize INT;
    DECLARE batchCount INT;
    DECLARE chunkCount INT;
    DECLARE rowCount INT;
    DECLARE v_rowCount INT;
    DECLARE enableLog SMALLINT;
    DECLARE backupTables SMALLINT;
    DECLARE cleanUpTimeLimit INT;
    DECLARE cleanUpDateTimeLimit TIMESTAMP;

    -- ------------------------------------------
    -- CONFIGURABLE VARIABLES
    -- ------------------------------------------
    SET batchSize = 10000;
    SET chunkSize = 500000;
    SET enableLog = 0;
    SET backupTables = 0;
    SET cleanUpTimeLimit = 24;

    SET rowCount = 0;
    SET batchCount = 1;
    SET chunkCount = 1;
    SET cleanUpDateTimeLimit = CURRENT TIMESTAMP - cleanUpTimeLimit HOURS;

    IF (enableLog = 1) THEN
        CALL DBMS_OUTPUT.PUT_LINE('WSO2_IDN_FLOW_CONTEXT_CLEANUP() STARTED...!');
    END IF;

    -- ------------------------------------------
    -- BACKUP DATA
    -- ------------------------------------------
    IF (backupTables = 1) THEN
        IF (enableLog = 1) THEN
            CALL DBMS_OUTPUT.PUT_LINE('TABLE BACKUP STARTED...!');
        END IF;

        IF EXISTS (SELECT 1 FROM SYSIBM.SYSTABLES WHERE NAME = 'BAK_IDN_FLOW_CONTEXT_STORE') THEN
            IF (enableLog = 1) THEN
                CALL DBMS_OUTPUT.PUT_LINE('DELETING OLD BACKUP...');
            END IF;
            DROP TABLE BAK_IDN_FLOW_CONTEXT_STORE;
        END IF;

        CREATE TABLE BAK_IDN_FLOW_CONTEXT_STORE AS (
            SELECT * FROM IDN_FLOW_CONTEXT_STORE
        ) WITH DATA;
    END IF;

    -- ------------------------------------------
    -- CLEANUP DATA
    -- ------------------------------------------
    WHILE (chunkCount > 0) DO
        DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;
        CREATE TABLE IDN_FLOW_CONTEXT_CHUNK_TMP AS (
            SELECT ID FROM IDN_FLOW_CONTEXT_STORE
        ) WITH NO DATA;

        INSERT INTO IDN_FLOW_CONTEXT_CHUNK_TMP
        SELECT ID FROM IDN_FLOW_CONTEXT_STORE
        WHERE EXPIRES_AT < cleanUpDateTimeLimit
        FETCH FIRST chunkSize ROWS ONLY;

        GET DIAGNOSTICS v_rowCount = ROW_COUNT;
        SET chunkCount = v_rowCount;

        CREATE INDEX IDX_FLOW_CONTEXT_CHUNK_TMP ON IDN_FLOW_CONTEXT_CHUNK_TMP (ID);

        IF (enableLog = 1) THEN
            CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_FLOW_CONTEXT_CHUNK_TMP...');
        END IF;

        -- BATCH LOOP
        SET batchCount = 1;
        WHILE (batchCount > 0) DO
            DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;
            CREATE TABLE IDN_FLOW_CONTEXT_BATCH_TMP (ID VARCHAR(255));

            INSERT INTO IDN_FLOW_CONTEXT_BATCH_TMP
            SELECT ID FROM IDN_FLOW_CONTEXT_CHUNK_TMP
            FETCH FIRST batchSize ROWS ONLY;

            GET DIAGNOSTICS v_rowCount = ROW_COUNT;
            SET batchCount = v_rowCount;

            CREATE INDEX IDX_FLOW_CONTEXT_BATCH_TMP ON IDN_FLOW_CONTEXT_BATCH_TMP (ID);

            IF (enableLog = 1) THEN
                CALL DBMS_OUTPUT.PUT_LINE('CREATED IDN_FLOW_CONTEXT_BATCH_TMP...');
            END IF;

            -- DELETE FROM MAIN TABLE
            DELETE FROM IDN_FLOW_CONTEXT_STORE
            WHERE ID IN (
                SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP
            );

            GET DIAGNOSTICS rowCount = ROW_COUNT;

            IF (enableLog = 1) THEN
                CALL DBMS_OUTPUT.PUT_LINE('BATCH DELETE EXECUTED...');
            END IF;

            -- DELETE FROM CHUNK
            DELETE FROM IDN_FLOW_CONTEXT_CHUNK_TMP
            WHERE ID IN (
                SELECT ID FROM IDN_FLOW_CONTEXT_BATCH_TMP
            );
        END WHILE;
    END WHILE;

    -- CLEANUP TEMP TABLES
    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_BATCH_TMP;
    DROP TABLE IF EXISTS IDN_FLOW_CONTEXT_CHUNK_TMP;

    IF (enableLog = 1) THEN
        CALL DBMS_OUTPUT.PUT_LINE('CLEANUP COMPLETED...!');
    END IF;
END;
