CREATE OR REPLACE PROCEDURE WSO2_WF_REQUEST_CLEANUP_RESTORE
BEGIN
    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    DECLARE rowCount INT;
    DECLARE totalRestored INT;
    DECLARE enableLog SMALLINT;
    DECLARE backupTableExists INT;

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    SET enableLog = 1; -- ENABLE LOGGING [DEFAULT : 1]
    SET totalRestored = 0;

    IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('========================================');
    	CALL DBMS_OUTPUT.PUT_LINE('WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP STARTED...!');
    	CALL DBMS_OUTPUT.PUT_LINE('========================================');
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_REQUEST TABLE
    -- ------------------------------------------
    SELECT COUNT(*) INTO rowCount FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'WF_REQUEST';
    IF (rowCount = 1)
    THEN
        -- Check if backup table exists
        SELECT COUNT(*) INTO backupTableExists FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'BAK_WF_REQUEST';
        IF (backupTableExists = 1)
        THEN
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('RESTORATION STARTED ON WF_REQUEST TABLE...');
            END IF;
            
            INSERT INTO WF_REQUEST SELECT A.* FROM BAK_WF_REQUEST A LEFT JOIN WF_REQUEST B ON A.UUID = B.UUID WHERE B.UUID IS NULL;
            GET DIAGNOSTICS rowCount = ROW_COUNT;
            SET totalRestored = totalRestored + rowCount;
            
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  RESTORED ' || rowCount || ' RECORDS TO WF_REQUEST');
            END IF;
        ELSE
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  BACKUP TABLE BAK_WF_REQUEST NOT FOUND - SKIPPING');
            END IF;
        END IF;
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_WORKFLOW_REQUEST_RELATION TABLE
    -- ------------------------------------------
    SELECT COUNT(*) INTO rowCount FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'WF_WORKFLOW_REQUEST_RELATION';
    IF (rowCount = 1)
    THEN
        -- Check if backup table exists
        SELECT COUNT(*) INTO backupTableExists FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'BAK_WF_WORKFLOW_REQUEST_RELATION';
        IF (backupTableExists = 1)
        THEN
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('RESTORATION STARTED ON WF_WORKFLOW_REQUEST_RELATION TABLE...');
            END IF;
            
            INSERT INTO WF_WORKFLOW_REQUEST_RELATION 
            SELECT A.* FROM BAK_WF_WORKFLOW_REQUEST_RELATION A 
            LEFT JOIN WF_WORKFLOW_REQUEST_RELATION B ON A.RELATIONSHIP_ID = B.RELATIONSHIP_ID 
            WHERE B.RELATIONSHIP_ID IS NULL;
            GET DIAGNOSTICS rowCount = ROW_COUNT;
            SET totalRestored = totalRestored + rowCount;
            
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  RESTORED ' || rowCount || ' RECORDS TO WF_WORKFLOW_REQUEST_RELATION');
            END IF;
        ELSE
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  BACKUP TABLE BAK_WF_WORKFLOW_REQUEST_RELATION NOT FOUND - SKIPPING');
            END IF;
        END IF;
    END IF;

    -- ------------------------------------------
    -- RESTORE WF_WORKFLOW_APPROVAL_RELATION TABLE
    -- ------------------------------------------
    SELECT COUNT(*) INTO rowCount FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'WF_WORKFLOW_APPROVAL_RELATION';
    IF (rowCount = 1)
    THEN
        -- Check if backup table exists
        SELECT COUNT(*) INTO backupTableExists FROM SYSIBM.SYSTABLES WHERE CREATOR = CURRENT SCHEMA AND NAME = 'BAK_WF_WORKFLOW_APPROVAL_RELATION';
        IF (backupTableExists = 1)
        THEN
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('RESTORATION STARTED ON WF_WORKFLOW_APPROVAL_RELATION TABLE...');
            END IF;
            
            INSERT INTO WF_WORKFLOW_APPROVAL_RELATION 
            SELECT A.* FROM BAK_WF_WORKFLOW_APPROVAL_RELATION A 
            LEFT JOIN WF_WORKFLOW_APPROVAL_RELATION B 
            ON A.TASK_ID = B.TASK_ID AND A.APPROVER_TYPE = B.APPROVER_TYPE AND A.APPROVER_NAME = B.APPROVER_NAME 
            WHERE B.TASK_ID IS NULL;
            GET DIAGNOSTICS rowCount = ROW_COUNT;
            SET totalRestored = totalRestored + rowCount;
            
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  RESTORED ' || rowCount || ' RECORDS TO WF_WORKFLOW_APPROVAL_RELATION');
            END IF;
        ELSE
            IF (enableLog = 1)
            THEN
            	CALL DBMS_OUTPUT.PUT_LINE('  BACKUP TABLE BAK_WF_WORKFLOW_APPROVAL_RELATION NOT FOUND - SKIPPING');
            END IF;
        END IF;
    END IF;

    -- ------------------------------------------
    -- COMPLETION SUMMARY
    -- ------------------------------------------
    IF (enableLog = 1)
    THEN
    	CALL DBMS_OUTPUT.PUT_LINE('========================================');
    	CALL DBMS_OUTPUT.PUT_LINE('WSO2_WF_REQUEST_CLEANUP_DATA_RESTORATION_SP COMPLETED!');
    	CALL DBMS_OUTPUT.PUT_LINE('Total records restored: ' || totalRestored);
    	CALL DBMS_OUTPUT.PUT_LINE('========================================');
    END IF;

END/
