/*
 * Copyright (c) 2014 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.idp.mgt.util;

/**
 * This class is used to keep the identity provider management related constants.
 */
public class IdPManagementConstants {

    public static final String SHARED_IDP_PREFIX = "SHARED_";
    public static final String SCOPE_LIST_PLACEHOLDER = "_SCOPE_LIST_";
    public static final String MULTI_VALUED_PROPERTY_CHARACTER = ".";
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";
    public static final String MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN = ".*\\" + MULTI_VALUED_PROPERTY_CHARACTER +
            "[0-9]+";
    public static final String META_DATA = "meta_data";
    public static final String DEFAULT_SORT_BY = "ID";
    public static final String DESC_SORT_ORDER = "DESC";
    public static final String ASC_SORT_ORDER = "ASC";
    public static final String DEFAULT_SORT_ORDER = "ASC";
    public static final String EMPTY_STRING = "";
    public static final String ID = "ID";
    public static final String MySQL = "MySQL";

    public static final String RESIDENT_IDP = "LOCAL";
    public static final String EQ = "eq";
    public static final String SW = "sw";
    public static final String EW = "ew";
    public static final String CO = "co";

    // Idp basic attributes.
    public static final String NAME = "NAME";
    public static final String IDP_NAME = "name";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String IDP_DESCRIPTION = "description";
    public static final String IDP_UUID = "id";
    public static final String UUID = "UUID";
    public static final String IDP_IS_ENABLED = "isEnabled";
    public static final String IS_ENABLED = "IS_ENABLED";
    public static final String IDP_IMAGE_URL = "image";

    // IdP optional attributes constants.
    public static final String IDP_IS_PRIMARY = "isPrimary";
    public static final String IS_PRIMARY = "IS_PRIMARY";
    public static final String IDP_HOME_REALM_ID = "homeRealmIdentifier";
    public static final String HOME_REALM_ID = "HOME_REALM_ID";
    public static final String IDP_IS_FEDERATION_HUB = "isFederationHub";
    public static final String IS_FEDERATION_HUB = "IS_FEDERATION_HUB";
    public static final String IDP_CERTIFICATE = "certificate";
    public static final String CERTIFICATE = "CERTIFICATE";
    public static final String IDP_ALIAS = "alias";
    public static final String ALIAS = "ALIAS";
    public static final String IDP_CLAIMS = "claims";
    public static final String CLAIMS = "USER_CLAIM_URI, ROLE_CLAIM_URI, IS_LOCAL_CLAIM_DIALECT";
    public static final String IDP_ROLES = "roles";
    public static final String ROLES = "PROVISIONING_ROLE";
    public static final String IDP_FEDERATED_AUTHENTICATORS = "federatedAuthenticators";
    public static final String FEDERATED_AUTHENTICATORS = "DEFAULT_AUTHENTICATOR_NAME";
    public static final String IDP_PROVISIONING = "provisioning";
    public static final String PROVISIONING =
            "INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, DEFAULT_PRO_CONNECTOR_NAME";

    //Default SAML endpoints.
    public static final String SSO_ARTIFACT_URL = "samlartresolve";
    public static final String SAML_ECP_URL = "samlecp";
    public static final String SAMLSSO = "samlsso";

    // JIT provisioning properties for password provisioning and username provisioning.
    public static final String PASSWORD_PROVISIONING_ENABLED = "PASSWORD_PROVISIONING_ENABLED";
    public static final String MODIFY_USERNAME_ENABLED = "MODIFY_USERNAME_ENABLED";
    public static final String PROMPT_CONSENT_ENABLED = "PROMPT_CONSENT_ENABLED";
    public static final String ASSOCIATE_LOCAL_USER_ENABLED = "ASSOCIATE_LOCAL_USER_ENABLED";

    public static final String TEMPLATE_ID_IDP_PROPERTY_NAME = "templateId";
    public static final String TEMPLATE_ID_IDP_PROPERTY_DISPLAY_NAME = "Template Id";
    public static final String RESET_PROVISIONING_ENTITIES_ON_CONFIG_UPDATE = "OutboundProvisioning"
            + ".ResetProvisioningEntitiesOnConfigUpdate";

    // Outbound Provisioning Connectors
    public static final String GOOGLE = "googleapps";
    public static final String SALESFORCE = "salesforce";
    public static final String SCIM = "scim";
    public static final String SCIM2 = "SCIM2";

    public static class SQLQueries {

        public static final String GET_IDPS_SQL = "SELECT NAME, IS_PRIMARY, HOME_REALM_ID, DESCRIPTION, " +
                "IS_FEDERATION_HUB, IS_LOCAL_CLAIM_DIALECT, IS_ENABLED, DISPLAY_NAME, ID, IMAGE_URL, UUID FROM IDP " +
                "WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%'))";

        public static final String GET_IDPS_NAME_SQL = "SELECT NAME, IS_PRIMARY, HOME_REALM_ID, DESCRIPTION, " +
                "IS_FEDERATION_HUB, IS_LOCAL_CLAIM_DIALECT, IS_ENABLED, DISPLAY_NAME, ID, IMAGE_URL, UUID FROM IDP " +
                "WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND NAME LIKE ?";

        public static final String GET_IDPS_BY_IDP_ID_LIST = "SELECT ID, NAME FROM IDP WHERE TENANT_ID = ? " +
                "AND ID IN (" + SCOPE_LIST_PLACEHOLDER + ")";

        public static final String FROM_IDP_WHERE = "FROM IDP WHERE ";

        public static final String GET_IDP_BY_TENANT_MYSQL =
                "SELECT ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, UUID ";

        public static final String GET_IDP_BY_TENANT_MYSQL_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "' ORDER BY %s LIMIT ?, ?";

        public static final String GET_IDP_BY_TENANT_DB2SQL =
                "SELECT ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, UUID ";

        public static final String GET_IDP_BY_TENANT_DB2SQL_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "' ORDER BY %s LIMIT ? OFFSET ?";

        public static final String GET_IDP_BY_TENANT_MSSQL =
                "SELECT ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, UUID ";

        public static final String GET_IDP_BY_TENANT_MSSQL_TAIL = "TENANT_ID =? AND NAME != '" + RESIDENT_IDP + "' ORDER BY %s OFFSET ? ROWS FETCH NEXT ?" +
                " ROWS ONLY";

        public static final String GET_IDP_BY_TENANT_ORACLE = "SELECT ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, " +
                "UUID, TENANT_ID ";

        public static final String FROM_IDP_WHERE_ORACLE = "FROM IDP ORDER BY %s ) WHERE ";

        public static final String ROWNUM_FOR_ORACLE = ", ROWNUM AS rnum FROM (";

        public static final String FROM = " FROM ( ";

        public static final String GET_IDP_BY_TENANT_ORACLE_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "' AND ROWNUM <= ?) WHERE rnum > ?";

        public static final String GET_IDP_BY_TENANT_POSTGRESQL =
                "SELECT ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, UUID ";

        public static final String GET_IDP_BY_TENANT_POSTGRESQL_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "' ORDER BY %s LIMIT ? OFFSET ?";

        public static final String GET_IDP_BY_TENANT_INFORMIX =
                "SELECT SKIP ? FIRST ? ID, NAME, DESCRIPTION, IS_ENABLED, IMAGE_URL, UUID ";

        public static final String GET_IDP_BY_TENANT_INFORMIX_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "' ORDER BY %s";

        public static final String GET_IDP_COUNT_SQL = "SELECT COUNT(*) FROM IDP WHERE ";

        public static final String GET_IDP_COUNT_SQL_TAIL = "TENANT_ID = ? AND NAME != '" + RESIDENT_IDP + "'";

        public static final String GET_IDP_BY_NAME_SQL = "SELECT ID, NAME, IS_PRIMARY, HOME_REALM_ID, CERTIFICATE, " +
                "ALIAS, INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, ROLE_CLAIM_URI," +
                "DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION, IS_FEDERATION_HUB, " +
                "IS_LOCAL_CLAIM_DIALECT, PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, IMAGE_URL, UUID FROM IDP " +
                "WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND NAME = ?";

        public static final String GET_IDP_BY_ID_SQL = "SELECT ID, NAME, IS_PRIMARY, HOME_REALM_ID, CERTIFICATE, " +
                "ALIAS, INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, ROLE_CLAIM_URI," +
                "DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION, IS_FEDERATION_HUB, " +
                "IS_LOCAL_CLAIM_DIALECT, PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, IMAGE_URL, UUID FROM IDP WHERE " +
                "(TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND ID = ?";

        public static final String GET_IDP_BY_RESOURCE_ID_SQL = "SELECT ID, NAME, IS_PRIMARY, HOME_REALM_ID, " +
                "CERTIFICATE, ALIAS, INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, " +
                "ROLE_CLAIM_URI, DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION, " +
                "IS_FEDERATION_HUB, IS_LOCAL_CLAIM_DIALECT, PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, IMAGE_URL, " +
                "UUID FROM IDP WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) " +
                "AND UUID = ?";

        public static final String GET_IDP_NAME_BY_RESOURCE_ID_SQL = "SELECT NAME FROM IDP WHERE UUID = ?";

        public static final String GET_IDP_ID_BY_NAME_SQL = "SELECT ID "
                + "FROM IDP WHERE TENANT_ID=? AND NAME=?";

        public static final String GET_ALL_IDP_AUTH_SQL = "SELECT ID, NAME, IS_ENABLED, DISPLAY_NAME FROM " +
                "IDP_AUTHENTICATOR WHERE IDP_ID = ?";

        public static final String GET_IDP_AUTH_SQL = "SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = ? AND NAME = ?";

        public static final String GET_IDP_AUTH_PROPS_SQL = "SELECT PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET FROM " +
                "IDP_AUTHENTICATOR_PROPERTY WHERE AUTHENTICATOR_ID = ?";

        public static final String GET_IDP_PROVISIONING_CONFIGS_SQL = "SELECT ID, TENANT_ID, "
                + "IDP_ID, PROVISIONING_CONNECTOR_TYPE, IS_ENABLED, IS_BLOCKING "
                + " FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=?";

        public static final String GET_IDP_PROVISIONING_PROPERTY_SQL = "SELECT TENANT_ID, "
                + "PROVISIONING_CONFIG_ID, PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_BLOB_VALUE, PROPERTY_TYPE, " +
                "IS_SECRET FROM IDP_PROV_CONFIG_PROPERTY WHERE TENANT_ID=? AND PROVISIONING_CONFIG_ID=?";

        public static final String GET_IDP_PROVISIONING_CONFIGS_ID = "SELECT ID " +
                "FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=?";

        public static final String GET_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL = "SELECT CLAIM_URI,DEFAULT_VALUE," +
                "IS_REQUESTED FROM IDP_LOCAL_CLAIM " + " WHERE IDP_ID = ? AND TENANT_ID =?";

        public static final String DELETE_PROVISIONING_CONNECTORS = "DELETE FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=?";

        public static final String DELETE_IDP_PROV_CONFIG_PROPERTY = "DELETE FROM IDP_PROV_CONFIG_PROPERTY WHERE " +
                "PROVISIONING_CONFIG_ID=?";

        public static final String GET_IDP_NAME_BY_REALM_ID_SQL = "SELECT NAME FROM IDP WHERE (TENANT_ID = ? OR " +
                "(TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND HOME_REALM_ID=?";

        public static final String GET_IDP_CLAIM_MAPPINGS_SQL = "SELECT IDP_CLAIM.CLAIM," +
                " IDP_CLAIM_MAPPING.LOCAL_CLAIM, IDP_CLAIM_MAPPING.DEFAULT_VALUE, IDP_CLAIM_MAPPING.IS_REQUESTED "
                + "FROM IDP_CLAIM_MAPPING INNER JOIN IDP_CLAIM ON IDP_CLAIM_MAPPING.IDP_CLAIM_ID= IDP_CLAIM.ID " +
                "WHERE IDP_CLAIM.IDP_ID=?";

        public static final String GET_IDP_ROLE_MAPPINGS_SQL = "SELECT IDP_ROLE_MAPPING.USER_STORE_ID, " +
                "IDP_ROLE_MAPPING.LOCAL_ROLE, IDP_ROLE.ROLE "
                + "FROM IDP_ROLE_MAPPING  INNER JOIN IDP_ROLE ON IDP_ROLE_MAPPING.IDP_ROLE_ID=IDP_ROLE.ID "
                + "WHERE IDP_ROLE.IDP_ID=?";

        public static final String UPDATE_IDP_SQL = "UPDATE IDP SET NAME=?, IS_PRIMARY=?, "
                + "HOME_REALM_ID=?, CERTIFICATE=?, ALIAS=?, INBOUND_PROV_ENABLED=?, "
                + "INBOUND_PROV_USER_STORE_ID=?,USER_CLAIM_URI=?, ROLE_CLAIM_URI=?, " +
                "DEFAULT_AUTHENTICATOR_NAME=?, DEFAULT_PRO_CONNECTOR_NAME=?, DESCRIPTION=?, " +
                "IS_FEDERATION_HUB=?, IS_LOCAL_CLAIM_DIALECT=?, PROVISIONING_ROLE=?, IS_ENABLED=?, DISPLAY_NAME=?  " +
                "WHERE TENANT_ID=? AND NAME=?";

        public static final String UPDATE_IDP_BY_RESOURCE_ID_SQL = "UPDATE IDP SET NAME=?, IS_PRIMARY=?, "
                + "HOME_REALM_ID=?, CERTIFICATE=?, ALIAS=?, INBOUND_PROV_ENABLED=?, "
                + "INBOUND_PROV_USER_STORE_ID=?,USER_CLAIM_URI=?, ROLE_CLAIM_URI=?, " +
                "DEFAULT_AUTHENTICATOR_NAME=?, DEFAULT_PRO_CONNECTOR_NAME=?, DESCRIPTION=?, " +
                "IS_FEDERATION_HUB=?, IS_LOCAL_CLAIM_DIALECT=?, PROVISIONING_ROLE=?, IS_ENABLED=?, DISPLAY_NAME=?, " +
                "IMAGE_URL=? WHERE UUID=?";

        public static final String UPDATE_IDP_AUTH_SQL = "UPDATE IDP_AUTHENTICATOR SET IS_ENABLED=? WHERE IDP_ID=? " +
                "AND NAME=?";

        public static final String UPDATE_IDP_AUTH_PROP_SQL = "UPDATE IDP_AUTHENTICATOR_PROPERTY SET " +
                "PROPERTY_VALUE = ?, IS_SECRET = ? WHERE AUTHENTICATOR_ID = ? AND PROPERTY_KEY = ?";

        public static final String DELETE_IDP_AUTH_PROP_WITH_KEY_SQL = "DELETE FROM IDP_AUTHENTICATOR_PROPERTY "
                + "WHERE PROPERTY_KEY = ? AND TENANT_ID = ? AND AUTHENTICATOR_ID = ?";

        public static final String ADD_IDP_CLAIMS_SQL = "INSERT INTO IDP_CLAIM (IDP_ID, TENANT_ID, CLAIM) "
                + "VALUES (?, ?, ?)";

        public static final String DELETE_IDP_CLAIMS_SQL = "DELETE FROM IDP_CLAIM "
                + "WHERE (IDP_ID=? AND CLAIM=?)";

        public static final String UPDATE_IDP_CLAIMS_SQL = "UPDATE IDP_CLAIM SET CLAIM=?, "
                + "WHERE (IDP_ID=? AND CLAIM=?)";

        public static final String GET_IDP_CLAIMS_SQL = "SELECT ID, CLAIM FROM IDP_CLAIM WHERE IDP_ID=?";

        public static final String GET_USER_ROLE_CLAIMS_SQL = "SELECT USER_CLAIM_URI, ROLE_CLAIM_URI FROM IDP WHERE " +
                "IDP_ID=?";

        public static final String DELETE_IDP_CLAIM_MAPPINGS_SQL = "DELETE FROM IDP_CLAIM_MAPPING "
                + "WHERE (IDP_CLAIM_ID=? AND TENANT_ID=? AND LOCAL_CLAIM=?)";

        public static final String ADD_IDP_CLAIM_MAPPINGS_SQL = "INSERT INTO IDP_CLAIM_MAPPING "
                + "(IDP_CLAIM_ID, TENANT_ID, LOCAL_CLAIM,DEFAULT_VALUE, IS_REQUESTED) VALUES (?, ?, ?, ?, ?)";

        public static final String ADD_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL = "INSERT INTO IDP_LOCAL_CLAIM "
                + "(IDP_ID, CLAIM_URI,DEFAULT_VALUE,TENANT_ID, IS_REQUESTED) VALUES (?, ?, ?, ?, ?)";

        public static final String ADD_IDP_ROLES_SQL = "INSERT INTO IDP_ROLE (IDP_ID, TENANT_ID, ROLE) "
                + "VALUES (?, ?, ?)";

        public static final String DELETE_IDP_ROLES_SQL = "DELETE FROM IDP_ROLE "
                + "WHERE (IDP_ID=? AND ROLE=?)";

        public static final String UPDATE_IDP_ROLES_SQL = "UPDATE IDP_ROLE SET ROLE=? "
                + "WHERE (IDP_ID=? AND ROLE=?)";

        public static final String GET_IDP_ROLES_SQL = "SELECT ID, ROLE  FROM IDP_ROLE "
                + "WHERE IDP_ID=?";

        public static final String DELETE_IDP_ROLE_MAPPINGS_SQL = "DELETE FROM IDP_ROLE_MAPPING "
                + "WHERE (IDP_ROLE_ID=? AND TENANT_ID=? AND USER_STORE_ID = ? AND LOCAL_ROLE=?)";

        public static final String ADD_IDP_ROLE_MAPPINGS_SQL = "INSERT INTO IDP_ROLE_MAPPING "
                + "(IDP_ROLE_ID, TENANT_ID, USER_STORE_ID, LOCAL_ROLE) VALUES (?, ?, ?, ?)";

        public static final String ADD_IDP_SQL = "INSERT INTO IDP (TENANT_ID, NAME, IS_PRIMARY, "
                + "HOME_REALM_ID, CERTIFICATE, ALIAS, INBOUND_PROV_ENABLED, "
                + "INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, ROLE_CLAIM_URI," +
                "DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION,IS_FEDERATION_HUB,"
                + "IS_LOCAL_CLAIM_DIALECT,PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, IMAGE_URL, UUID) " +
                "VALUES (?, ?, ?,?,?, ?, ?, ?, ?, ?,?,?, ?,?,? ,?, ?, ?, ?, ?)";

        public static final String ADD_IDP_AUTH_SQL = "INSERT INTO IDP_AUTHENTICATOR " +
                "(IDP_ID, TENANT_ID, IS_ENABLED, NAME, DISPLAY_NAME) VALUES (?,?,?,?,?)";

        public static final String DELETE_IDP_AUTH_SQL = "DELETE FROM IDP_AUTHENTICATOR WHERE IDP_ID=? AND NAME=?";

        public static final String ADD_IDP_AUTH_PROP_SQL = "INSERT INTO IDP_AUTHENTICATOR_PROPERTY " +
                "(AUTHENTICATOR_ID, TENANT_ID, PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET) VALUES (?, ?, ?, ?, ?)";

        public static final String ADD_IDP_PROVISIONING_CONFIG_SQL = "INSERT INTO IDP_PROVISIONING_CONFIG (TENANT_ID, "
                + "IDP_ID, PROVISIONING_CONNECTOR_TYPE, IS_ENABLED, IS_BLOCKING) "
                + "VALUES (?, ?, ? ,?, ?)";

        public static final String ADD_IDP_PROVISIONING_PROPERTY_SQL = "INSERT INTO IDP_PROV_CONFIG_PROPERTY (TENANT_ID, "
                + "PROVISIONING_CONFIG_ID, PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_BLOB_VALUE, PROPERTY_TYPE, IS_SECRET) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        public static final String UPDATE_IDP_PROVISIONING_CONFIG_PROPERTY_SQL = "UPDATE IDP_PROV_CONFIG_PROPERTY SET "
                + "PROPERTY_KEY=?, PROPERTY_VALUE=?, PROPERTY_BLOB_VALUE=?, PROPERTY_TYPE = ?, IS_SECRET=? "
                + "WHERE PROVISIONING_CONFIG_ID=(SELECT ID FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=? "
                + "AND TENANT_ID = ? AND PROVISIONING_CONNECTOR_TYPE=?) AND TENANT_ID = ? AND PROPERTY_KEY=?";

        public static final String UPDATE_IDP_PROVISIONING_CONFIG_SQL = "UPDATE IDP_PROVISIONING_CONFIG SET "
                + "IS_ENABLED=?, IS_BLOCKING=? WHERE (IDP_ID=? AND PROVISIONING_CONNECTOR_TYPE=? AND TENANT_ID=?)";

        public static final String GET_IDP_PROVISIONING_CONFIGS_FOR_CONNECTOR_TYPE_SQL = "SELECT ID FROM "
                + "IDP_PROVISIONING_CONFIG WHERE (IDP_ID=? AND PROVISIONING_CONNECTOR_TYPE=? AND TENANT_ID=?)";

        public static final String DELETE_IDP_SQL = "DELETE FROM IDP WHERE (TENANT_ID=? AND NAME=?)";

        public static final String DELETE_ALL_IDP_BY_TENANT_ID_SQL = "DELETE FROM IDP WHERE TENANT_ID = ?";

        public static final String DELETE_IDP_BY_RESOURCE_ID_SQL = "DELETE FROM IDP WHERE UUID=?";

        public static final String GET_IDP_CONFIGS_ID_FROM_UUID = "SELECT ID FROM IDP WHERE UUID=?";

        public static final String GET_IDP_CONFIGS_ID_FROM_TENANT_ID_AND_NAME =
                "SELECT ID FROM IDP WHERE TENANT_ID=? AND NAME=?";

        public static final String DELETE_IDP_SP_AUTH_ASSOCIATIONS = "DELETE FROM SP_FEDERATED_IDP WHERE " +
                "AUTHENTICATOR_ID in (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID=(SELECT ID FROM IDP WHERE NAME=? " +
                "AND TENANT_ID=?))";

        public static final String REMOVE_EMPTY_SP_AUTH_STEPS =
                "DELETE FROM SP_AUTH_STEP WHERE ID NOT IN (SELECT ID FROM SP_FEDERATED_IDP)";

        public static final String DELETE_IDP_SP_PROVISIONING_ASSOCIATIONS = "DELETE FROM SP_PROVISIONING_CONNECTOR " +
                "WHERE IDP_NAME=? AND TENANT_ID=?";

        public static final String GET_IDP_ROW_ID_SQL = "SELECT ID FROM IDP WHERE ((TENANT_ID = ? OR (TENANT_ID = ? " +
                "AND NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND NAME = ?)";

        public static final String SWITCH_IDP_PRIMARY_SQL = "UPDATE IDP SET IS_PRIMARY=? "
                + "WHERE (TENANT_ID=? AND IS_PRIMARY=?)";

        public static final String SWITCH_IDP_PRIMARY_ON_DELETE_SQL = "UPDATE IDP SET IS_PRIMARY=? "
                + "WHERE (TENANT_ID=? AND NAME=? AND IS_PRIMARY=?)";

        public static final String DELETE_ALL_ROLES_SQL = "DELETE FROM IDP_ROLE "
                + "WHERE IDP_ID=?";

        public static final String DELETE_ROLE_LISTENER_SQL = "DELETE FROM IDP_ROLE "
                + "WHERE TENANT_ID=? AND ROLE=?";

        public static final String RENAME_ROLE_LISTENER_SQL = "UPDATE IDP_ROLE_MAPPING SET LOCAL_ROLE=? "
                + "WHERE (TENANT_ID=? AND LOCAL_ROLE=?)";

        public static final String DELETE_ALL_CLAIMS_SQL = "DELETE FROM IDP_CLAIM "
                + "WHERE IDP_ID=?";

        public static final String DELETE_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL = "DELETE FROM  IDP_LOCAL_CLAIM "
                + "WHERE (IDP_ID=? AND TENANT_ID=?)";

        public static final String RENAME_CLAIM_SQL = "UPDATE IDP_CLAIM_MAPPING SET LOCAL_CLAIM=? "
                + "WHERE (TENANT_ID=? AND LOCAL_CLAIM=?)";

        public static final String GET_SP_FEDERATED_IDP_REFS = "SELECT COUNT(*) FROM SP_FEDERATED_IDP A JOIN " +
                "IDP_AUTHENTICATOR B ON A.AUTHENTICATOR_ID = B.ID WHERE B.IDP_ID = (SELECT ID FROM IDP C WHERE (C" +
                ".TENANT_ID = ? OR (C.TENANT_ID = ? AND C.NAME LIKE '" + SHARED_IDP_PREFIX + "%')) AND C.NAME = ?)";

        public static final String GET_SP_PROVISIONING_CONNECTOR_REFS = "SELECT COUNT(*) FROM SP_PROVISIONING_CONNECTOR "
                + "WHERE (TENANT_ID=? AND IDP_NAME=?)";

        public static final String GET_IDP_BY_AUTHENTICATOR_PROPERTY = "SELECT idp.ID, idp.NAME, idp.IS_PRIMARY, " +
                "idp.HOME_REALM_ID, " +
                "idp.CERTIFICATE, idp.ALIAS, idp.INBOUND_PROV_ENABLED, idp.INBOUND_PROV_USER_STORE_ID, " +
                "idp.USER_CLAIM_URI, " +
                "idp.ROLE_CLAIM_URI, idp.DEFAULT_AUTHENTICATOR_NAME, idp.DEFAULT_PRO_CONNECTOR_NAME, " +
                "idp.DESCRIPTION, " +
                "idp.IS_FEDERATION_HUB, idp.IS_LOCAL_CLAIM_DIALECT, idp.PROVISIONING_ROLE, idp.IS_ENABLED, " +
                "idp.DISPLAY_NAME " +
                "FROM IDP idp INNER JOIN  IDP_AUTHENTICATOR idp_auth ON idp.ID = idp_auth.IDP_ID INNER JOIN " +
                "IDP_AUTHENTICATOR_PROPERTY idp_auth_pro ON idp_auth.ID = idp_auth_pro.AUTHENTICATOR_ID " +
                "WHERE  idp_auth_pro.PROPERTY_KEY =?  AND idp_auth_pro.PROPERTY_VALUE = ? AND idp_auth_pro.TENANT_ID =?";

        public static final String GET_IDP_BY_AUTHENTICATOR_PROPERTY_SQL = "SELECT idp.ID, idp.NAME, idp.IS_PRIMARY, " +
                "idp.HOME_REALM_ID, " +
                "idp.CERTIFICATE, idp.ALIAS, idp.INBOUND_PROV_ENABLED, idp.INBOUND_PROV_USER_STORE_ID, " +
                "idp.USER_CLAIM_URI, " +
                "idp.ROLE_CLAIM_URI, idp.DEFAULT_AUTHENTICATOR_NAME, idp.DEFAULT_PRO_CONNECTOR_NAME, " +
                "idp.DESCRIPTION, " +
                "idp.IS_FEDERATION_HUB, idp.IS_LOCAL_CLAIM_DIALECT, idp.PROVISIONING_ROLE, idp.IS_ENABLED, " +
                "idp.DISPLAY_NAME " +
                "FROM IDP idp INNER JOIN  IDP_AUTHENTICATOR idp_auth ON idp.ID = idp_auth.IDP_ID INNER JOIN " +
                "IDP_AUTHENTICATOR_PROPERTY idp_auth_pro ON idp_auth.ID = idp_auth_pro.AUTHENTICATOR_ID " +
                "WHERE  idp_auth_pro.PROPERTY_KEY =?  AND idp_auth_pro.PROPERTY_VALUE = ? AND idp_auth_pro.TENANT_ID " +
                "=? AND idp_auth.name =?";

        public static final String GET_SIMILAR_IDP_ENTITIY_IDS =
                "SELECT COUNT(prop.ID) FROM IDP_AUTHENTICATOR_PROPERTY prop INNER JOIN IDP_AUTHENTICATOR auth " +
                        "ON auth.ID = prop.AUTHENTICATOR_ID WHERE prop.PROPERTY_KEY=? " +
                        "AND prop.PROPERTY_VALUE=? AND prop.TENANT_ID=? AND auth.NAME = ?";

        public static final String GET_IDP_METADATA_BY_IDP_ID = "SELECT ID, NAME, VALUE, DISPLAY_NAME FROM " +
                "IDP_METADATA WHERE IDP_ID = ?";
        public static final String GET_IDP_METADATA_BY_IDP_ID_H2 = "SELECT ID, NAME, `VALUE`, DISPLAY_NAME FROM " +
                "IDP_METADATA WHERE IDP_ID = ?";
        public static final String ADD_IDP_METADATA = "INSERT INTO IDP_METADATA (IDP_ID, NAME, VALUE, DISPLAY_NAME, " +
                "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
        public static final String ADD_IDP_METADATA_H2 = "INSERT INTO IDP_METADATA (IDP_ID, NAME, `VALUE`, DISPLAY_NAME, " +
                "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
        public static final String DELETE_IDP_METADATA = "DELETE FROM IDP_METADATA WHERE IDP_ID = ?";

        public static final String GET_CONNECTED_APPS_MYSQL = "SELECT UUID FROM (SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID) INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?)) UNION SELECT SP_APP.UUID FROM SP_PROVISIONING_CONNECTOR INNER JOIN " +
                "SP_APP ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER JOIN IDP ON IDP_NAME = IDP.NAME WHERE IDP.UUID = ? LIMIT ?,?;";
        public static final String GET_CONNECTED_APPS_ORACLE = "SELECT UUID FROM (SELECT UUID, ROWNUM AS RNUM FROM ( " +
                "SELECT UUID FROM (SP_AUTH_STEP INNER JOIN SP_FEDERATED_IDP ON SP_AUTH_STEP.ID = SP_FEDERATED_IDP.ID)" +
                " INNER JOIN SP_APP ON SP_AUTH_STEP.APP_ID = SP_APP.ID WHERE AUTHENTICATOR_ID IN ( SELECT ID FROM " +
                "IDP_AUTHENTICATOR WHERE IDP_ID = ( SELECT ID FROM IDP WHERE UUID = ?)) UNION SELECT SP_APP.UUID FROM" +
                " SP_PROVISIONING_CONNECTOR INNER JOIN SP_APP ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER " +
                "JOIN IDP ON IDP_NAME = IDP.NAME WHERE IDP.UUID = ?) WHERE ROWNUM <= ?) WHERE RNUM > ?";
        public static final String GET_CONNECTED_APPS_MSSQL = "(SELECT UUID, SP_APP.ID FROM SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?))) UNION (SELECT SP_APP.UUID, SP_APP.ID FROM SP_PROVISIONING_CONNECTOR " +
                "INNER JOIN SP_APP ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER JOIN IDP ON IDP_NAME = IDP" +
                ".NAME WHERE IDP.UUID = ?) ORDER BY SP_APP.ID OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        public static final String GET_CONNECTED_APPS_POSTGRESSQL = "SELECT UUID FROM (SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID) INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?)) UNION SELECT SP_APP.UUID FROM SP_PROVISIONING_CONNECTOR INNER JOIN " +
                "SP_APP ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER JOIN IDP ON IDP_NAME = IDP.NAME WHERE " +
                "IDP.UUID = ? LIMIT ? OFFSET ?";
        public static final String GET_CONNECTED_APPS_DB2SQL = "SELECT UUID FROM (SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID) INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?)) UNION SELECT SP_APP.UUID FROM SP_PROVISIONING_CONNECTOR INNER JOIN SP_APP " +
                "ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER JOIN IDP ON IDP_NAME = IDP.NAME WHERE IDP.UUID" +
                " = ? LIMIT ? OFFSET ?";
        public static final String GET_CONNECTED_APPS_INFORMIX = "SELECT SKIP ? FIRST ? UUID FROM (SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID) INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?))";
        public static final String CONNECTED_APPS_TOTAL_COUNT_SQL = "SELECT COUNT(1) FROM (SELECT UUID FROM (SP_AUTH_STEP INNER JOIN " +
                "SP_FEDERATED_IDP ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID) INNER JOIN SP_APP ON SP_AUTH_STEP" +
                ".APP_ID=SP_APP.ID WHERE AUTHENTICATOR_ID IN (SELECT ID FROM IDP_AUTHENTICATOR WHERE IDP_ID = (SELECT" +
                " ID FROM IDP WHERE UUID = ?)) UNION SELECT SP_APP.UUID FROM SP_PROVISIONING_CONNECTOR INNER JOIN " +
                "SP_APP ON SP_PROVISIONING_CONNECTOR.APP_ID = SP_APP.ID INNER JOIN IDP ON IDP_NAME = IDP.NAME WHERE " +
                "IDP.UUID = ?) APP";
        public static final String GET_IDP_NAME_BY_METADATA = "SELECT IDP.NAME FROM IDP INNER JOIN IDP_METADATA ON " +
                "IDP.ID = IDP_METADATA.IDP_ID WHERE IDP_METADATA.NAME = ? AND IDP_METADATA.VALUE = ? AND " +
                "IDP_METADATA.TENANT_ID = ?";
        public static final String GET_IDP_NAME_BY_METADATA_H2 = "SELECT IDP.NAME FROM IDP INNER JOIN IDP_METADATA ON " +
                "IDP.ID = IDP_METADATA.IDP_ID WHERE IDP_METADATA.NAME = ? AND IDP_METADATA.`VALUE` = ? AND " +
                "IDP_METADATA.TENANT_ID = ?";
        public static final String GET_TOTAL_IDP_CLAIM_USAGES = "SELECT COUNT(*) FROM IDP_CLAIM_MAPPING WHERE " +
                "TENANT_ID = ? AND LOCAL_CLAIM = ?";
    }

    public enum ErrorMessage {

        // Client Errors.
        ERROR_CODE_IDP_ALREADY_EXISTS("IDP-60001", "Identity Provider with the name: %s already exists."),
        ERROR_CODE_IDP_DOES_NOT_EXIST("IDP-60002", "Identity Provider with resource ID: %s does not exists."),
        ERROR_CODE_IDP_NAME_DOES_NOT_EXIST("IDP-60002", "Identity Provider with name: %s does not exists."),
        ERROR_CODE_IDP_ADD_REQUEST_INVALID("IDP-60003", "Identity Provider add request validation failed. %s"),
        ERROR_CODE_IDP_GET_REQUEST_INVALID("IDP-60004", "Identity Provider get request validation failed. %s"),
        ERROR_CODE_IDP_DELETE_REQUEST_INVALID("IDP-60005", "Identity Provider delete request validation failed. %s"),
        ERROR_CODE_IDP_UPDATE_REQUEST_INVALID("IDP-60006", "Identity Provider update request validation failed. %s"),
        ERROR_CODE_SEARCH_REQUEST_INVALID("IDP-60007", "Search request validation failed. Invalid search filter. %s"),
        ERROR_CODE_GET_CONNECTED_APPS_REQUEST_INVALID("IDP-60008", "Identity Provider get connected apps request " +
                "validation failed. %s"),
        DUPLICATE_OUTBOUND_CONNECTOR_PROPERTIES("IDP-60009", "Duplicate properties in Outbound " +
                "Provisioning Connector configurations for connector : %s"),
        ERROR_CODE_IDP_NAME_INVALID("IDP-60098", "Identity Provider name is invalid. %s"),
        ERROR_CODE_IDP_ATTRIBUTE_INVALID("IDP-60009", "Invalid attribute of Identity Provider. %s"),
        ERROR_CODE_NOT_EXISTING_OUTBOUND_PROVISIONING_ROLE("IDP-60010", "One or more outbound " +
                "provisioning roles does not exist"),

        // Server Errors.
        ERROR_CODE_UNEXPECTED("IDP-65001", "Unexpected Error"),
        ERROR_CODE_ADD_IDP("IDP-65002", "Error while adding the Identity Provider: %s."),
        ERROR_CODE_RETRIEVE_IDP("IDP-65003", "Error while getting the Identity Provider: %s."),
        ERROR_CODE_DELETE_IDP("IDP-65004", "Error while deleting Identity Provider: %s."),
        ERROR_CODE_UPDATE_IDP("IDP-65005", "Error while updating Identity Provider: %s."),
        ERROR_CODE_CONNECTING_DATABASE("IDP-65006", "Error while connecting database. %s"),
        ERROR_CODE_RETRIEVE_IDP_CONNECTED_APPS("IDP-65007", "Error while retrieving connected " +
                "applications of Identity Provider with resource ID: %s."),
        ERROR_CODE_VALIDATING_OUTBOUND_PROVISIONING_ROLES("IDP-65008", "Error while validating " +
                "the outbound provisioning roles");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }
}
