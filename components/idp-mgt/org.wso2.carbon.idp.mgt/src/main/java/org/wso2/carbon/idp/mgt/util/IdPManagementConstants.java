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
    public static final String MULTI_VALUED_PROPERTY_CHARACTER = ".";
    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";
    public static final String MULTI_VALUED_PROPERT_IDENTIFIER_PATTERN = ".*\\" + MULTI_VALUED_PROPERTY_CHARACTER +
            "[0-9]+";
    public static final String META_DATA = "meta_data";
    public static class SQLQueries {

        public static final String GET_IDPS_SQL = "SELECT NAME, IS_PRIMARY, HOME_REALM_ID, DESCRIPTION, " +
                "IS_FEDERATION_HUB, IS_LOCAL_CLAIM_DIALECT, IS_ENABLED, DISPLAY_NAME, ID FROM IDP WHERE (TENANT_ID = ? OR" +
                " (TENANT_ID = ? AND NAME LIKE '" + SHARED_IDP_PREFIX + "%'))";

        public static final String GET_IDP_BY_NAME_SQL = "SELECT ID, IS_PRIMARY, HOME_REALM_ID, CERTIFICATE, ALIAS, " +
                "INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, ROLE_CLAIM_URI," +
                "DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION, IS_FEDERATION_HUB, " +
                "IS_LOCAL_CLAIM_DIALECT, PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, PASSWORD_PROV_ENABLED, "
                + "MODIFY_USERNAME_ENABLED FROM IDP WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME LIKE '"
                + SHARED_IDP_PREFIX + "%')) AND NAME = ?";

        public static final String GET_IDP_BY_ID_SQL = "SELECT NAME, IS_PRIMARY, HOME_REALM_ID, CERTIFICATE, ALIAS, " +
                "INBOUND_PROV_ENABLED, INBOUND_PROV_USER_STORE_ID, USER_CLAIM_URI, ROLE_CLAIM_URI," +
                "DEFAULT_AUTHENTICATOR_NAME,DEFAULT_PRO_CONNECTOR_NAME, DESCRIPTION, IS_FEDERATION_HUB, " +
                "IS_LOCAL_CLAIM_DIALECT, PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, PASSWORD_PROV_ENABLED, "
                + "MODIFY_USERNAME_ENABLED FROM IDP WHERE (TENANT_ID = ? OR (TENANT_ID = ? AND NAME "
                + "LIKE '" + SHARED_IDP_PREFIX + "%')) AND ID = ?";

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


        public static final String GET_LOCAL_IDP_DEFAULT_CLAIM_VALUES_SQL = "SELECT CLAIM_URI,DEFAULT_VALUE," +
                "IS_REQUESTED FROM IDP_LOCAL_CLAIM " + " WHERE IDP_ID = ? AND TENANT_ID =?";

        public static final String DELETE_PROVISIONING_CONNECTORS = "DELETE FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=?";

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
                "IS_FEDERATION_HUB=?, IS_LOCAL_CLAIM_DIALECT=?, PROVISIONING_ROLE=?, IS_ENABLED=?, DISPLAY_NAME=?, "
                + "PASSWORD_PROV_ENABLED=?, MODIFY_USERNAME_ENABLED=? WHERE TENANT_ID=? AND NAME=?";

        public static final String UPDATE_IDP_AUTH_SQL = "UPDATE IDP_AUTHENTICATOR SET IS_ENABLED=? WHERE IDP_ID=? " +
                "AND NAME=?";

        public static final String UPDATE_IDP_AUTH_PROP_SQL = "UPDATE IDP_AUTHENTICATOR_PROPERTY SET " +
                "PROPERTY_VALUE = ?, IS_SECRET = ? WHERE AUTHENTICATOR_ID = ? AND PROPERTY_KEY = ?";

        public static final String DELETE_IDP_AUTH_PROP_WITH_KEY_SQL = "DELETE FROM IDP_AUTHENTICATOR_PROPERTY "
                + "WHERE PROPERTY_KEY = ? AND TENANT_ID = ?";

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
                + "IS_LOCAL_CLAIM_DIALECT,PROVISIONING_ROLE, IS_ENABLED, DISPLAY_NAME, PASSWORD_PROV_ENABLED, "
                + "MODIFY_USERNAME_ENABLED) VALUES (?, ?, ?,?,?, ?, ?, ?, ?, ?,?,?, ?,?,? ,?, ?, ?, ?, ?)";

        public static final String ADD_IDP_AUTH_SQL = "INSERT INTO IDP_AUTHENTICATOR " +
                "(IDP_ID, TENANT_ID, IS_ENABLED, NAME, DISPLAY_NAME) VALUES (?,?,?,?,?)";

        public static final String ADD_IDP_AUTH_PROP_SQL = "INSERT INTO IDP_AUTHENTICATOR_PROPERTY " +
                "(AUTHENTICATOR_ID, TENANT_ID, PROPERTY_KEY, PROPERTY_VALUE, IS_SECRET) VALUES (?, ?, ?, ?, ?)";

        public static final String ADD_IDP_PROVISIONING_CONFIG_SQL = "INSERT INTO IDP_PROVISIONING_CONFIG (TENANT_ID, "
                + "IDP_ID, PROVISIONING_CONNECTOR_TYPE, IS_ENABLED, IS_BLOCKING) "
                + "VALUES (?, ?, ? ,?, ?)";

        public static final String ADD_IDP_PROVISIONING_PROPERTY_SQL = "INSERT INTO IDP_PROV_CONFIG_PROPERTY (TENANT_ID, "
                + "PROVISIONING_CONFIG_ID, PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_BLOB_VALUE, PROPERTY_TYPE, IS_SECRET) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        public static final String UPDATE_IDP_PROVISIONING_CONFIG_SQL = "UPDATE IDP_PROVISIONING_CONFIG SET "
                + "PROV_CONNECTOR_TYPE=?, PROV_CONFIG_KEY=? PROV_CONFIG_VALUE=?, "
                + "PROV_CONFIG_IS_SECRET = ? WHERE IDP_ID=?";

        public static final String DELETE_IDP_SQL = "DELETE FROM IDP WHERE (TENANT_ID=? AND NAME=?)";

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

        public static final String GET_IDP_BY_AUTHENTICATOR_PROPERTY =
                "SELECT idp.ID, idp.NAME, idp.IS_PRIMARY, " + "idp.HOME_REALM_ID, "
                        + "idp.CERTIFICATE, idp.ALIAS, idp.INBOUND_PROV_ENABLED, idp.PASSWORD_PROV_ENABLED, "
                        + "idp.MODIFY_USERNAME_ENABLED, idp.INBOUND_PROV_USER_STORE_ID, idp.USER_CLAIM_URI, "
                        + "idp.ROLE_CLAIM_URI, idp.DEFAULT_AUTHENTICATOR_NAME, idp.DEFAULT_PRO_CONNECTOR_NAME, "
                        + "idp.DESCRIPTION, "
                        + "idp.IS_FEDERATION_HUB, idp.IS_LOCAL_CLAIM_DIALECT, idp.PROVISIONING_ROLE, idp.IS_ENABLED, "
                        + "idp.DISPLAY_NAME "
                        + "FROM IDP idp INNER JOIN  IDP_AUTHENTICATOR idp_auth ON idp.ID = idp_auth.IDP_ID INNER JOIN "
                        + "IDP_AUTHENTICATOR_PROPERTY idp_auth_pro ON idp_auth.ID = idp_auth_pro.AUTHENTICATOR_ID "
                        + "WHERE  idp_auth_pro.PROPERTY_KEY =?  AND idp_auth_pro.PROPERTY_VALUE = ? AND idp_auth_pro.TENANT_ID =?";

        public static final String GET_IDP_BY_AUTHENTICATOR_PROPERTY_SQL =
                "SELECT idp.ID, idp.NAME, idp.IS_PRIMARY, " + "idp.HOME_REALM_ID, "
                        + "idp.CERTIFICATE, idp.ALIAS, idp.INBOUND_PROV_ENABLED, idp.PASSWORD_PROV_ENABLED, "
                        + "idp.MODIFY_USERNAME_ENABLED, idp.INBOUND_PROV_USER_STORE_ID, " + "idp.USER_CLAIM_URI, "
                        + "idp.ROLE_CLAIM_URI, idp.DEFAULT_AUTHENTICATOR_NAME, idp.DEFAULT_PRO_CONNECTOR_NAME, "
                        + "idp.DESCRIPTION, "
                        + "idp.IS_FEDERATION_HUB, idp.IS_LOCAL_CLAIM_DIALECT, idp.PROVISIONING_ROLE, idp.IS_ENABLED, "
                        + "idp.DISPLAY_NAME "
                        + "FROM IDP idp INNER JOIN  IDP_AUTHENTICATOR idp_auth ON idp.ID = idp_auth.IDP_ID INNER JOIN "
                        + "IDP_AUTHENTICATOR_PROPERTY idp_auth_pro ON idp_auth.ID = idp_auth_pro.AUTHENTICATOR_ID "
                        + "WHERE  idp_auth_pro.PROPERTY_KEY =?  AND idp_auth_pro.PROPERTY_VALUE = ? AND idp_auth_pro.TENANT_ID "
                        + "=? AND idp_auth.name =?";

        public static final String GET_SIMILAR_IDP_ENTITIY_IDS =
                "SELECT COUNT(prop.ID) FROM IDP_AUTHENTICATOR_PROPERTY prop INNER JOIN IDP_AUTHENTICATOR auth " +
                        "ON auth.ID = prop.AUTHENTICATOR_ID WHERE prop.PROPERTY_KEY=? " +
                        "AND prop.PROPERTY_VALUE=? AND prop.TENANT_ID=? AND auth.NAME = ?";

        public static final String GET_IDP_METADATA_BY_IDP_ID = "SELECT ID, NAME, VALUE, DISPLAY_NAME FROM " +
                "IDP_METADATA WHERE IDP_ID = ?";
        public static final String ADD_IDP_METADATA = "INSERT INTO IDP_METADATA (IDP_ID, NAME, VALUE, DISPLAY_NAME, " +
                "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
        public static final String DELETE_IDP_METADATA = "DELETE FROM IDP_METADATA WHERE IDP_ID = ?";
    }
}
