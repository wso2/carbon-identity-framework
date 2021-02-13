CREATE OR REPLACE PROCEDURE wso2_session_cleanup_sp IS


-- ------------------------------------------
-- DECLARE VARIABLES (DO NO CHANGE THESE VALUES)
-- ------------------------------------------

    deletedsessions                  INT := 0;
    deletedmappingsessions           INT := 0;
    deletedsessionapppinfo           INT := 0;
    deletedsessionmetadata           INT := 0;
    deletedstoreoperations           INT := 0;
    deletedoperationalsessionmappings   INT := 0;
    deletedoperationalsessionapppinfo   INT := 0;
    deletedoperationalsessionmetadata   INT := 0;
    unixtime                         INT := 0;
    rowcount                         INT := 0;
    sessioncleanuptime               INT := 0;
    operationcleanuptime             INT := 0;
    current_schema                   VARCHAR(20);
    sessioncleanupcount              INT := 0;
    sessionmappingcleanupcount       INT := 0;
    operationalmappingcleanupcount   INT := 0;
    sessionappinfocleanupcount       INT := 0;
    operationalappinfocleanupcount   INT := 0;
    sessionmetadatacleanupcount      INT := 0;
    operationalmetadatacleanupcount  INT := 0;
    operationcleanupcount            INT := 0;
    expiredsessioncount              INT := 0;

-- ------------------------------------------
-- CONFIGURABLE VARIABLES
-- ------------------------------------------
    batchsize                        INT := 10000; -- BATCH WISE DELETE [DEFULT : 10000]
    chunklimit                       INT := 500000; -- CHUNK WISE DELETE FOR LARGE TABLES [DEFULT : 500000]
    checkcount                       INT := 100; -- SET CHECK COUNT FOR FINISH CLEANUP SCRIPT (CLEANUP ELIGIBLE SESSION COUNT SHOULD BE HIGHER THAN checkCount TO CONTINUE) [DEFAULT : 100]
    tracingenabled                   BOOLEAN := TRUE; --  IF TRACE LOGGING IS ENABLED [DEFAULT : FALSE]
    sleeptime                        INT := 2; -- Sleep time in seconds.
    sessnclntiminminits              INT := 20160; -- Session data older than 20160 minutes (14 days) will be removed.
    opertnclntimeinminits            INT := 720; -- Operational data older than 720 minutes (12 h) will be removed.



BEGIN


    SELECT trunc((TO_DATE(TO_CHAR(sys_extract_utc(systimestamp), 'YYYY-MM-DD HH24:MI:SS'), 'YYYY-MM-DD HH24:MI:SS') - TO_DATE('1970-01-01 00:00:00','YYYY-MM-DD HH24:MI:SS')) * 86400) INTO unixtime FROM DUAL;

    sessioncleanuptime := unixtime * 1000000000 - ( sessnclntiminminits * 60000000000 );
    operationcleanuptime := unixtime * 1000000000 - ( opertnclntimeinminits * 60000000000 );

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO current_schema FROM DUAL;

    IF ( tracingenabled ) THEN
        SELECT COUNT(*) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = upper('LOG_WSO2_SESSION_CLEANUP_SP');
        IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE LOG_WSO2_SESSION_CLEANUP_SP';
            COMMIT;
        END IF;
        EXECUTE IMMEDIATE 'CREATE TABLE LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP VARCHAR(250) , LOG VARCHAR(250)) NOLOGGING';
        COMMIT;
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''WSO2_SESSION_CLEANUP_SP STARTED .... !'')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''USING SCHEMA :'|| current_schema|| ''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION DATA CLEANUP TIME AFTER : '||sessioncleanuptime|| ''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''OPERATIONS DATA CLEANUP TIME AFTER : '||operationcleanuptime|| ''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        COMMIT;
    END IF;


-- ------------------------------------------
-- REMOVE SESSION DATA
-- ------------------------------------------
    IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION_CLEANUP_TASK STARTED .... !'')';
        COMMIT;
    END IF;


    LOOP
        SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'IDN_AUTH_SESSION_STORE_TMP';
        IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_AUTH_SESSION_STORE_TMP';
            COMMIT;
        END IF;
        
--        EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_SESSION_STORE_TMP AS SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE 1=2';
        
        EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_SESSION_STORE_TMP (ROW_ID rowid,SESSION_ID varchar(100),CONSTRAINT CHNK_IDN_OATH_ACCS_TOK_PRI PRIMARY KEY (ROW_ID)) NOLOGGING';

        EXECUTE IMMEDIATE 'INSERT INTO IDN_AUTH_SESSION_STORE_TMP SELECT rowid,SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE  rownum <= :chunkLimit AND TIME_CREATED < :sessionCleanupTime'
        USING chunklimit, sessioncleanuptime;
        rowcount := SQL%rowcount;

--        EXECUTE IMMEDIATE 'CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID)';
        COMMIT;
        
        EXIT WHEN rowcount <= checkcount;
        
        IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY IDN_AUTH_SESSION_STORE_TMP CHUNK TABLE CREATED WITH '||rowcount||''')';
        COMMIT;
        END IF;

        LOOP
            SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'TEMP_SESSION_BATCH';

            IF ( rowcount = 1 ) THEN
                EXECUTE IMMEDIATE 'DROP TABLE TEMP_SESSION_BATCH';
                COMMIT;
            END IF;
            
          EXECUTE IMMEDIATE 'CREATE TABLE TEMP_SESSION_BATCH (ROW_ID rowid,SESSION_ID varchar(100),CONSTRAINT BATCH_IDN_OATH_ACCS_TOK_PRI PRIMARY KEY (ROW_ID)) NOLOGGING';
          COMMIT;
--            EXECUTE IMMEDIATE 'CREATE TABLE  TEMP_SESSION_BATCH AS SELECT SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP WHERE 1=2';

            EXECUTE IMMEDIATE 'INSERT INTO TEMP_SESSION_BATCH SELECT ROW_ID,SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP WHERE rownum <= :batchSize'
            USING batchsize;
            rowcount := SQL%rowcount;
            COMMIT;

            EXIT WHEN rowcount = 0;

            IF ( tracingenabled ) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY TEMP_SESSION_BATCH TABBLE CREATED WITH '|| rowcount||''')';
            COMMIT;
            END IF;

            EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_STORE WHERE rowid IN (SELECT ROW_ID FROM TEMP_SESSION_BATCH)';
            sessioncleanupcount := SQL%rowcount;
            COMMIT;

            IF ( tracingenabled ) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED SESSIONS COMPLETED WITH '|| sessioncleanupcount||''')';
            COMMIT;
            END IF;

            EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_STORE_TMP WHERE ROW_ID IN (SELECT ROW_ID FROM TEMP_SESSION_BATCH)';
            COMMIT;

            IF ( tracingenabled ) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''REMOVED THE BATCH FROM CHUNK TABLE IDN_AUTH_SESSION_STORE_TMP'')';
            COMMIT;
            END IF;

            IF ( tracingenabled ) THEN
            deletedsessions := deletedsessions + sessioncleanupcount;
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED SESSIONS COMPLETED WITH '|| deletedsessions||''')';
            COMMIT;
            END IF;

            dbms_lock.sleep(sleeptime);
        END LOOP;

    END LOOP;

    IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION_CLEANUP_TASK COMPLETED REMOVING '||deletedsessions||' SESSIONS'')';
        COMMIT;
    END IF;

-- --------------------------------------------
-- REMOVE USER SESSION DATA FROM IDN_AUTH_USER_SESSION_MAPPING, IDN_AUTH_SESSION_APP_INFO, IDN_AUTH_SESSION_META_DATA TABLES
-- --------------------------------------------

    IF ( tracingenabled ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''USER_SESSION_TABLES_CLEANUP_TASK STARTED .... !'')';
    COMMIT;
    END IF;

    SELECT COUNT(*) INTO rowcount from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_USER_SESSION_MAPPING');
    IF (rowcount = 1) THEN

        SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'IDN_AUTH_EXPIRED_SESSION_TMP';
        IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_AUTH_EXPIRED_SESSION_TMP';
            COMMIT;
        END IF;

        EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_EXPIRED_SESSION_TMP (SESSION_ID varchar(100),CONSTRAINT IDN_AUTH_EXP_SESS_PRI PRIMARY KEY (SESSION_ID)) NOLOGGING';
        EXECUTE IMMEDIATE 'INSERT INTO IDN_AUTH_EXPIRED_SESSION_TMP SELECT IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID FROM IDN_AUTH_USER_SESSION_MAPPING LEFT OUTER JOIN IDN_AUTH_SESSION_STORE ON IDN_AUTH_USER_SESSION_MAPPING.SESSION_ID = IDN_AUTH_SESSION_STORE.SESSION_ID WHERE IDN_AUTH_SESSION_STORE.SESSION_ID IS NULL';
        expiredsessioncount := SQL%rowcount;
        COMMIT;

        IF ( tracingenabled ) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY IDN_AUTH_EXPIRED_SESSION_TMP TABLE CREATED WITH '||expiredsessioncount||''')';
            COMMIT;
        END IF;

        IF (expiredsessioncount > 0) THEN
        LOOP

            SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'IDN_AUTH_USER_SESSION_TMP';
            IF ( rowcount = 1 ) THEN
                EXECUTE IMMEDIATE 'DROP TABLE IDN_AUTH_USER_SESSION_TMP';
                COMMIT;
            END IF;

            EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_USER_SESSION_TMP (SESSION_ID varchar(100),CONSTRAINT CHNK_IDN_USER_SESS_PRI PRIMARY KEY (SESSION_ID)) NOLOGGING';
            EXECUTE IMMEDIATE 'INSERT INTO IDN_AUTH_USER_SESSION_TMP SELECT SESSION_ID FROM IDN_AUTH_EXPIRED_SESSION_TMP WHERE rownum <= :chunkLimit'
            USING chunklimit;
            rowcount := SQL%rowcount;
            COMMIT;

            EXIT WHEN rowcount <= checkcount;

            IF ( tracingenabled ) THEN
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
            EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY IDN_AUTH_USER_SESSION_TMP CHUNK TABLE CREATED WITH '||rowcount||''')';
            COMMIT;
            END IF;

            LOOP
                SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'TEMP_USER_SESSION_BATCH';

                IF ( rowcount = 1 ) THEN
                    EXECUTE IMMEDIATE 'DROP TABLE TEMP_USER_SESSION_BATCH';
                    COMMIT;
                END IF;

                EXECUTE IMMEDIATE 'CREATE TABLE TEMP_USER_SESSION_BATCH (SESSION_ID varchar(100),CONSTRAINT BATCH_IDN_USER_SESS_PRI PRIMARY KEY (SESSION_ID)) NOLOGGING';
                COMMIT;

                EXECUTE IMMEDIATE 'INSERT INTO TEMP_USER_SESSION_BATCH SELECT SESSION_ID FROM IDN_AUTH_USER_SESSION_TMP WHERE rownum <= :batchSize'
                USING batchsize;
                rowcount := SQL%rowcount;
                COMMIT;

                EXIT WHEN rowcount = 0;

                IF ( tracingenabled ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY TEMP_USER_SESSION_BATCH TABLE CREATED WITH '|| rowcount||''')';
                COMMIT;
                END IF;

                -- Deleting user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table
                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_USER_SESSION_BATCH)';
                sessionmappingcleanupcount := SQL%rowcount;
                COMMIT;
                IF ( tracingenabled ) THEN
                    deletedmappingsessions := deletedmappingsessions + sessionmappingcleanupcount;
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED USER-SESSION MAPPINGS COMPLETED WITH '|| sessionmappingcleanupcount||''')';
                    COMMIT;
                END IF;
                -- End of deleting user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table

                -- Deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table
                SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_SESSION_APP_INFO');
                IF (ROWCOUNT = 1) then
                    EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_USER_SESSION_BATCH)';
                    sessionappinfocleanupcount := SQL%rowcount;
                    COMMIT;
                    IF ( tracingenabled ) THEN
                        deletedsessionapppinfo := deletedsessionapppinfo + sessionappinfocleanupcount;
                        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED SESSION APP INFO COMPLETED WITH '|| sessionappinfocleanupcount||''')';
                        COMMIT;
                    END IF;
                END if;
                -- End of deleting session app info from 'IDN_AUTH_SESSION_APP_INFO' table

                -- Deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table
                SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_SESSION_META_DATA');
                IF (ROWCOUNT = 1) then
                    EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_USER_SESSION_BATCH)';
                    sessionmetadatacleanupcount := SQL%rowcount;
                    COMMIT;
                    IF ( tracingenabled ) THEN
                        deletedsessionmetadata := deletedsessionmetadata + sessionmetadatacleanupcount;
                        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED SESSION METADATA COMPLETED WITH '|| sessionmetadatacleanupcount||''')';
                        COMMIT;
                    END IF;
                END if;
                -- End of deleting session metadata from 'IDN_AUTH_SESSION_META_DATA' table

                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_USER_SESSION_TMP WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_USER_SESSION_BATCH)';
                COMMIT;

                IF ( tracingenabled ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''REMOVED THE BATCH FROM TABLE IDN_AUTH_USER_SESSION_TMP'')';
                COMMIT;
                END IF;

                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_EXPIRED_SESSION_TMP WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_USER_SESSION_BATCH)';
                COMMIT;

                IF ( tracingenabled ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''REMOVED THE BATCH FROM TABLE IDN_AUTH_EXPIRED_SESSION_TMP'')';
                COMMIT;
                END IF;

                dbms_lock.sleep(sleeptime);
            END LOOP;

        END LOOP;

        END IF;
    END IF;

    IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION_CLEANUP_TASK COMPLETED REMOVING '||deletedmappingsessions||' USER-SESSION MAPPINGS'')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION_CLEANUP_TASK COMPLETED REMOVING '||deletedsessionapppinfo||' SESSION APP INFO ENTRIES'')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''SESSION_CLEANUP_TASK COMPLETED REMOVING '||deletedsessionmetadata||' SESSION METADATA ENTRIES'')';
        COMMIT;
    END IF;

-- --------------------------------------------
-- REMOVE OPERATIONAL DATA
-- --------------------------------------------

    IF ( tracingenabled ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''OPERATION_CLEANUP_TASK STARTED .... !'')';
        COMMIT;
    END IF;


    LOOP

        SELECT COUNT(1) INTO rowcount FROM all_tables WHERE owner = current_schema AND table_name = 'IDN_AUTH_SESSION_STORE_TMP';

        IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE IDN_AUTH_SESSION_STORE_TMP';
            COMMIT;
        END IF;
        
--        EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_SESSION_STORE_TMP AS SELECT SESSION_ID,SESSION_TYPE FROM IDN_AUTH_SESSION_STORE WHERE 1=2';

         EXECUTE IMMEDIATE 'CREATE TABLE IDN_AUTH_SESSION_STORE_TMP (ROW_ID rowid,SESSION_ID varchar(100),CONSTRAINT CHNK_IDN_OATH_ACCS_TOK_PRI PRIMARY KEY (ROW_ID)) NOLOGGING';


        EXECUTE IMMEDIATE 'INSERT INTO IDN_AUTH_SESSION_STORE_TMP SELECT rowid,SESSION_ID FROM IDN_AUTH_SESSION_STORE WHERE rownum <= :chunkLimit AND OPERATION = ''DELETE'' AND TIME_CREATED < :operationCleanupTime'
        USING chunklimit,operationcleanuptime;
        rowcount := SQL%rowcount;
        
--        EXECUTE IMMEDIATE 'CREATE INDEX idn_auth_session_tmp_idx on IDN_AUTH_SESSION_STORE_TMP (SESSION_ID)';
--        COMMIT;
        
        EXIT WHEN rowcount <= checkcount;
        
        IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TEMPORARY IDN_AUTH_SESSION_STORE_TMP CHUNK TABLE CREATED WITH OPERATIONAL DATA '||rowcount||''')';
        COMMIT;
        END IF;

        LOOP

            SELECT COUNT(1) INTO rowcount  FROM all_tables WHERE owner = current_schema AND table_name = 'TEMP_SESSION_BATCH';

            IF ( rowcount = 1 ) THEN
            EXECUTE IMMEDIATE 'DROP TABLE TEMP_SESSION_BATCH';
            COMMIT;
            END IF;
            
--            EXECUTE IMMEDIATE 'CREATE TABLE TEMP_SESSION_BATCH AS SELECT SESSION_ID, SESSION_TYPE FROM IDN_AUTH_SESSION_STORE_TMP WHERE 1=2';
              EXECUTE IMMEDIATE 'CREATE TABLE TEMP_SESSION_BATCH (ROW_ID rowid,SESSION_ID varchar(100),CONSTRAINT BATCH_IDN_OATH_ACCS_TOK_PRI PRIMARY KEY (ROW_ID)) NOLOGGING';
              COMMIT;

            EXECUTE IMMEDIATE 'INSERT INTO TEMP_SESSION_BATCH SELECT ROW_ID,SESSION_ID FROM IDN_AUTH_SESSION_STORE_TMP WHERE rownum <= :batchSize'
            USING batchsize;
            rowcount := SQL%rowcount;
            COMMIT;

            EXIT WHEN rowcount = 0;

            EXECUTE IMMEDIATE 'DELETE IDN_AUTH_SESSION_STORE WHERE rowid in (select ROW_ID from TEMP_SESSION_BATCH)';
            operationcleanupcount := SQL%rowcount;
            COMMIT;

            IF ( tracingenabled ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED  STORE OPERATIONS COMPLETED WITH '||operationcleanupcount||''')';
                COMMIT;
            END IF;

            -- Deleting operational data related user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table
            SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_USER_SESSION_MAPPING');
            IF (ROWCOUNT = 1) then
                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_USER_SESSION_MAPPING WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH)';
                operationalmappingcleanupcount := SQL%rowcount;
                COMMIT;
                IF ( tracingenabled ) THEN
                    deletedoperationalsessionmappings := deletedoperationalsessionmappings + operationalmappingcleanupcount;
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED OPERATIONAL USER-SESSION MAPPINGS COMPLETED WITH '|| operationalmappingcleanupcount||''')';
                    COMMIT;
                END IF;
            END if;
            -- End of deleting operational data related user-session mappings from 'IDN_AUTH_USER_SESSION_MAPPING' table

            -- Deleting operational data related session app info from 'IDN_AUTH_SESSION_APP_INFO' table
            SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_SESSION_APP_INFO');
            IF (ROWCOUNT = 1) then
                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_APP_INFO WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH)';
                operationalappinfocleanupcount := SQL%rowcount;
                COMMIT;
                IF ( tracingenabled ) THEN
                    deletedoperationalsessionapppinfo := deletedoperationalsessionapppinfo + operationalappinfocleanupcount;
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED OPERATIONAL SESSION APP INFO COMPLETED WITH '|| operationalappinfocleanupcount||''')';
                    COMMIT;
                END IF;
            END if;
            -- End of deleting operational data related session app info from 'IDN_AUTH_SESSION_APP_INFO' table

            -- Deleting operational data related session metadata from 'IDN_AUTH_SESSION_META_DATA' table
            SELECT COUNT(*) INTO ROWCOUNT from ALL_TABLES where OWNER = CURRENT_SCHEMA AND table_name = upper('IDN_AUTH_SESSION_META_DATA');
            IF (ROWCOUNT = 1) then
                EXECUTE IMMEDIATE 'DELETE FROM IDN_AUTH_SESSION_META_DATA WHERE SESSION_ID IN (SELECT SESSION_ID FROM TEMP_SESSION_BATCH)';
                operationalmetadatacleanupcount := SQL%rowcount;
                COMMIT;

                IF ( tracingenabled ) THEN
                    deletedoperationalsessionmetadata := deletedoperationalsessionmetadata + operationalmetadatacleanupcount;
                    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''DELETED OPERATIONAL SESSION METADATA COMPLETED WITH '|| operationalmetadatacleanupcount||''')';
                    COMMIT;
                END IF;
            END if;
            -- End of deleting operational data related session metadata from 'IDN_AUTH_SESSION_META_DATA' table

            EXECUTE IMMEDIATE 'DELETE IDN_AUTH_SESSION_STORE_TMP where ROW_ID in (select ROW_ID from TEMP_SESSION_BATCH)';
            COMMIT;

            IF ( tracingenabled ) THEN
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''REMOVED THE BATCH FROM CHUNK TABLE IDN_AUTH_SESSION_STORE_TMP'')';
                COMMIT;
            END IF;

            IF ( tracingenabled ) THEN
                deletedstoreoperations := deletedstoreoperations + operationcleanupcount;
                EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED OPERATIONS COMPLETED WITH '||deletedstoreoperations||''')';
                COMMIT;
            END IF;

            EXECUTE IMMEDIATE 'DROP TABLE TEMP_SESSION_BATCH';
            COMMIT;

            dbms_lock.sleep(sleeptime);

        END LOOP;

    END LOOP;
    
        IF ( tracingenabled ) THEN
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''OPERATION_CLEANUP_TASK COMPLETED REMOVING '||deletedstoreoperations||' OPERATIONS'')';
        COMMIT;
    END IF;

    IF ( tracingenabled ) THEN
    EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),'' '')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED SESSIONS COMPLETED WITH '||deletedsessions||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED OPERATIONS COMPLETED WITH '||deletedstoreoperations||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED OPERATION RELATED USER SESSION MAPPINGS COMPLETED WITH '||deletedoperationalsessionmappings||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED OPERATION RELATED SESSION APP INFO COMPLETED WITH '||deletedoperationalsessionapppinfo||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''TOTAL REMOVED OPERATION RELATED SESSION METADATA COMPLETED WITH '||deletedoperationalsessionmetadata||''')';
        EXECUTE IMMEDIATE 'INSERT INTO LOG_WSO2_SESSION_CLEANUP_SP (TIMESTAMP,LOG) VALUES (TO_CHAR( SYSTIMESTAMP, ''DD.MM.YYYY HH24:MI:SS:FF4''),''WSO2_SESSION_CLEANUP_SP COMPLETED .... ! '')';
        COMMIT;
    END IF;

END;
