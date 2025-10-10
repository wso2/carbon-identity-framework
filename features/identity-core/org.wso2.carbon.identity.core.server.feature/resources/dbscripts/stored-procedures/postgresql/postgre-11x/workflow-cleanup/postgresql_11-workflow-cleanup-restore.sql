CREATE OR REPLACE PROCEDURE WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP() AS $$

DECLARE

    rowcount bigint;
    totalRestored bigint;
    enableLog boolean;
    logLevel VARCHAR(10);
    backupTableExists int;

BEGIN

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog := TRUE;  -- ENABLE LOGGING [DEFAULT : TRUE]
    logLevel := 'TRACE'; -- SET LOG LEVELS : TRACE, INFO
    totalRestored := 0;

    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP STARTED...!';
        RAISE NOTICE '========================================';
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_REQUEST TABLE
    -- ------------------------------------------
    SELECT COUNT(1) INTO backupTableExists 
    FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() 
    AND TABLENAME = 'wf_request_backup';
    
    IF (backupTableExists = 1) THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'RESTORATION STARTED ON WF_REQUEST TABLE...';
        END IF;
        
        INSERT INTO wf_request 
        SELECT A.* 
        FROM wf_request_backup A 
        LEFT JOIN wf_request B ON A.UUID = B.UUID 
        WHERE B.UUID IS NULL;
        
        GET DIAGNOSTICS rowcount := ROW_COUNT;
        totalRestored := totalRestored + rowcount;
        
        IF (enableLog) THEN
            RAISE NOTICE '  Restored % records to WF_REQUEST', rowcount;
        END IF;
    ELSE
        IF (enableLog) THEN
            RAISE NOTICE '  Backup table wf_request_backup not found - skipping';
        END IF;
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_WORKFLOW_REQUEST_RELATION TABLE
    -- ------------------------------------------
    SELECT COUNT(1) INTO backupTableExists 
    FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() 
    AND TABLENAME = 'wf_workflow_request_relation_backup';
    
    IF (backupTableExists = 1) THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'RESTORATION STARTED ON WF_WORKFLOW_REQUEST_RELATION TABLE...';
        END IF;
        
        INSERT INTO wf_workflow_request_relation 
        SELECT A.* 
        FROM wf_workflow_request_relation_backup A 
        LEFT JOIN wf_workflow_request_relation B 
            ON A.RELATIONSHIP_ID = B.RELATIONSHIP_ID
        WHERE B.RELATIONSHIP_ID IS NULL;
        
        GET DIAGNOSTICS rowcount := ROW_COUNT;
        totalRestored := totalRestored + rowcount;
        
        IF (enableLog) THEN
            RAISE NOTICE '  Restored % records to WF_WORKFLOW_REQUEST_RELATION', rowcount;
        END IF;
    ELSE
        IF (enableLog) THEN
            RAISE NOTICE '  Backup table wf_workflow_request_relation_backup not found - skipping';
        END IF;
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_WORKFLOW_APPROVAL_RELATION TABLE
    -- ------------------------------------------
    SELECT COUNT(1) INTO backupTableExists 
    FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() 
    AND TABLENAME = 'wf_workflow_approval_relation_backup';
    
    IF (backupTableExists = 1) THEN
        IF (enableLog AND logLevel IN ('TRACE')) THEN
            RAISE NOTICE 'RESTORATION STARTED ON WF_WORKFLOW_APPROVAL_RELATION TABLE...';
        END IF;
        
        INSERT INTO wf_workflow_approval_relation 
        SELECT A.* 
        FROM wf_workflow_approval_relation_backup A 
        LEFT JOIN wf_workflow_approval_relation B 
            ON A.TASK_ID = B.TASK_ID
            AND A.APPROVER_TYPE = B.APPROVER_TYPE
            AND A.APPROVER_NAME = B.APPROVER_NAME
        WHERE B.TASK_ID IS NULL;
        
        GET DIAGNOSTICS rowcount := ROW_COUNT;
        totalRestored := totalRestored + rowcount;
        
        IF (enableLog) THEN
            RAISE NOTICE '  Restored % records to WF_WORKFLOW_APPROVAL_RELATION', rowcount;
        END IF;
    ELSE
        IF (enableLog) THEN
            RAISE NOTICE '  Backup table wf_workflow_approval_relation_backup not found - skipping';
        END IF;
    END IF;

    -- ------------------------------------------
    -- COMPLETION SUMMARY
    -- ------------------------------------------
    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP COMPLETED!';
        RAISE NOTICE 'Total records restored: %', totalRestored;
        RAISE NOTICE '========================================';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        IF (enableLog) THEN
            RAISE NOTICE 'Error occurred during restoration: %', SQLERRM;
        END IF;
        -- Re-raise the exception
        RAISE;

END;
$$
LANGUAGE 'plpgsql';

-- ------------------------------------------
-- UTILITY PROCEDURE TO DROP BACKUP TABLES
-- ------------------------------------------
CREATE OR REPLACE PROCEDURE WSO2_WF_REQUEST_DROP_BACKUP_TABLES() AS $$
DECLARE
    enableLog boolean;
    tablecount int;
BEGIN
    enableLog := TRUE;
    
    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'DROPPING WF BACKUP TABLES...';
        RAISE NOTICE '========================================';
    END IF;
    
    -- Drop each backup table if it exists
    SELECT COUNT(1) INTO tablecount FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME = 'wf_request_backup';
    IF (tablecount = 1) THEN
        DROP TABLE wf_request_backup;
        IF (enableLog) THEN
            RAISE NOTICE 'Dropped table: wf_request_backup';
        END IF;
    END IF;
    
    SELECT COUNT(1) INTO tablecount FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME = 'wf_workflow_request_relation_backup';
    IF (tablecount = 1) THEN
        DROP TABLE wf_workflow_request_relation_backup;
        IF (enableLog) THEN
            RAISE NOTICE 'Dropped table: wf_workflow_request_relation_backup';
        END IF;
    END IF;
    
    SELECT COUNT(1) INTO tablecount FROM PG_CATALOG.PG_TABLES 
    WHERE SCHEMANAME = CURRENT_SCHEMA() AND TABLENAME = 'wf_workflow_approval_relation_backup';
    IF (tablecount = 1) THEN
        DROP TABLE wf_workflow_approval_relation_backup;
        IF (enableLog) THEN
            RAISE NOTICE 'Dropped table: wf_workflow_approval_relation_backup';
        END IF;
    END IF;
    
    IF (enableLog) THEN
        RAISE NOTICE '========================================';
        RAISE NOTICE 'BACKUP TABLES DROPPED SUCCESSFULLY!';
        RAISE NOTICE '========================================';
    END IF;
    
END;
$$
LANGUAGE 'plpgsql';

-- To restore data from backup tables:
-- CALL WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP();

-- To drop backup tables after successful restoration:
-- CALL WSO2_WF_REQUEST_DROP_BACKUP_TABLES();
