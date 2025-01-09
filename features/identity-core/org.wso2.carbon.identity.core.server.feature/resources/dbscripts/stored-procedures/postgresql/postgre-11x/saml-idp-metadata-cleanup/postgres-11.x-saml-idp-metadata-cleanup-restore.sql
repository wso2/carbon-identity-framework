DO $$
DECLARE

    -- ------------------------------------------
    -- DECLARE VARIABLES
    -- ------------------------------------------
    rowCount INT;
    enableLog BOOLEAN;

BEGIN

    -- ------------------------------------------
    -- CONFIGURABLE ATTRIBUTES
    -- ------------------------------------------
    enableLog    := TRUE; -- ENABLE LOGGING [DEFAULT : TRUE]

    IF enableLog THEN
        RAISE NOTICE 'SAML IDP METADATA CLEANUP DATA RESTORATION STARTED .... !';
    END IF;

    SELECT COUNT(*) INTO rowCount FROM pg_catalog.pg_tables WHERE schemaname = current_schema() AND
     tablename = 'reg_resource';
    IF (rowCount = 1)
    THEN
        BEGIN
            INSERT INTO reg_content (reg_content_id, reg_content_data, reg_tenant_id)
                SELECT a.reg_content_id, a.reg_content_data, a.reg_tenant_id
                FROM bak_reg_content a
                LEFT JOIN reg_content b ON a.reg_content_id = b.reg_content_id
                WHERE b.reg_content_id IS NULL;

            INSERT INTO reg_resource (
                reg_path_id, reg_name, reg_version, reg_media_type, reg_creator, reg_created_time,
                reg_last_updator, reg_last_updated_time, reg_description, reg_content_id, reg_tenant_id, reg_uuid
            )
            SELECT
                a.reg_path_id, a.reg_name, a.reg_version, a.reg_media_type, a.reg_creator, a.reg_created_time,
                a.reg_last_updator, a.reg_last_updated_time, a.reg_description, a.reg_content_id, a.reg_tenant_id, a.reg_uuid
            FROM bak_reg_resource a
            LEFT JOIN reg_resource b ON a.reg_version = b.reg_version AND a.reg_tenant_id = b.reg_tenant_id
            WHERE b.reg_version IS NULL;
            GET DIAGNOSTICS rowCount := ROW_COUNT;

            IF enableLog THEN
                RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED ON REG_RESOURCE WITH %', rowCount;
            END IF;
        END;
    END IF;

    IF enableLog THEN
        RAISE NOTICE 'CLEANUP DATA RESTORATION COMPLETED .... !';
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        IF enableLog THEN
            RAISE NOTICE 'ERROR OCCURRED: %', SQLERRM;
        END IF;
END;
$$
LANGUAGE 'plpgsql';
