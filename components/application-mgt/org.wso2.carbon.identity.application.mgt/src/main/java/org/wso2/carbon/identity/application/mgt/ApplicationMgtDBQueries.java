/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.mgt;

/**
 * This class contains default SQL queries
 * <p/>
 * TODO : Make the queries configurable from a file TODO : Use transactions and joins
 */
public class ApplicationMgtDBQueries {

    // STORE Queries
    public static final String STORE_BASIC_APPINFO = "INSERT INTO SP_APP (TENANT_ID, APP_NAME, USER_STORE, USERNAME, " +
            "DESCRIPTION, AUTH_TYPE) VALUES (?,?,?,?,?,?)";
    public static final String UPDATE_BASIC_APPINFO = "UPDATE SP_APP SET APP_NAME=?, DESCRIPTION=?, IS_SAAS_APP=? " +
            "WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_ROLE_CLAIM = "UPDATE SP_APP SET ROLE_CLAIM=? WHERE TENANT_ID" +
            "= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_CLAIM_DIALEECT = "UPDATE SP_APP SET IS_LOCAL_CLAIM_DIALECT=? "+
            "WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_SEND_LOCAL_SUB_ID = "UPDATE SP_APP SET IS_SEND_LOCAL_SUBJECT_"+
            "ID=? WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_USE_TENANT_DOMAIN_LOCAL_SUBJECT_ID = "UPDATE SP_APP SET " +
            "IS_USE_TENANT_DOMAIN_SUBJECT=? WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_USE_USERSTORE_DOMAIN_LOCAL_SUBJECT_ID = "UPDATE SP_APP SET " +
            "IS_USE_USER_DOMAIN_SUBJECT=? WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_SEND_AUTH_LIST_OF_IDPS = "UPDATE SP_APP SET " +
            "IS_SEND_AUTH_LIST_OF_IDPS=? WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_SUBJECT_CLAIM_URI = "UPDATE SP_APP SET SUBJECT_CLAIM_URI=? " +
            "WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_AUTH_TYPE = "UPDATE SP_APP SET AUTH_TYPE=? WHERE TENANT_ID= ? "+
            "AND ID = ?";
    public static final String UPDATE_BASIC_APPINFO_WITH_PRO_PROPERTIES = "UPDATE SP_APP SET PROVISIONING_USERSTORE_" +
            "DOMAIN=?, IS_DUMB_MODE=? WHERE TENANT_ID= ? AND ID = ?";
    public static final String UPDATE_SP_PERMISSIONS = "UPDATE UM_PERMISSION SET UM_RESOURCE_ID=? WHERE UM_ID=?";

    public static final String STORE_CLIENT_INFO = "INSERT INTO SP_INBOUND_AUTH (TENANT_ID, INBOUND_AUTH_KEY," +
            "INBOUND_AUTH_TYPE,PROP_NAME, PROP_VALUE, APP_ID) VALUES (?,?,?,?,?,?)";
    public static final String STORE_STEP_INFO = "INSERT INTO SP_AUTH_STEP (TENANT_ID, STEP_ORDER, APP_ID, " +
            "IS_SUBJECT_STEP, IS_ATTRIBUTE_STEP) VALUES (?,?,?,?,?)";
    public static final String STORE_STEP_IDP_AUTH = "INSERT INTO SP_FEDERATED_IDP (ID, TENANT_ID, AUTHENTICATOR_ID) " +
            "VALUES (?,?,?)";
    public static final String STORE_CLAIM_MAPPING = "INSERT INTO SP_CLAIM_MAPPING (TENANT_ID, IDP_CLAIM, SP_CLAIM, " +
            "APP_ID, IS_REQUESTED,DEFAULT_VALUE) VALUES (?,?,?,?,?,?)";
    public static final String STORE_ROLE_MAPPING = "INSERT INTO SP_ROLE_MAPPING (TENANT_ID, IDP_ROLE, SP_ROLE, APP_ID)"+
            " VALUES (?,?,?,?)";
    public static final String STORE_REQ_PATH_AUTHENTICATORS = "INSERT INTO SP_REQ_PATH_AUTHENTICATOR (TENANT_ID, " +
            "AUTHENTICATOR_NAME, APP_ID) VALUES (?,?,?)";
    public static final String STORE_PRO_CONNECTORS = "INSERT INTO SP_PROVISIONING_CONNECTOR (TENANT_ID, IDP_NAME, " +
            "CONNECTOR_NAME, APP_ID,IS_JIT_ENABLED, BLOCKING) VALUES (?,?,?,?,?,?)";


    // LOAD Queries
    public static final String LOAD_APP_ID_BY_APP_NAME = "SELECT ID FROM SP_APP WHERE APP_NAME = ? AND TENANT_ID = ?";
    public static final String LOAD_APP_NAMES_BY_TENANT = "SELECT APP_NAME, DESCRIPTION FROM SP_APP WHERE TENANT_ID = ?";
    public static final String LOAD_APP_ID_BY_CLIENT_ID_AND_TYPE = "SELECT APP_ID FROM SP_AUTH_STEP WHERE CLIENT_ID = ? "
            + "AND CLIENT_TYPE= ? AND TENANT_ID = ?";
    public static final String LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE = "SELECT APP_NAME "
            + "FROM SP_APP INNER JOIN SP_INBOUND_AUTH "
            + "ON SP_APP.ID = SP_INBOUND_AUTH.APP_ID "
            + "WHERE INBOUND_AUTH_KEY = ? AND INBOUND_AUTH_TYPE = ? AND SP_APP.TENANT_ID = ? AND SP_INBOUND_AUTH.TENANT_"
            + "ID=?";

    public static final String LOAD_BASIC_APP_INFO_BY_APP_NAME = "SELECT ID, TENANT_ID, APP_NAME, USER_STORE, " +
            "USERNAME, DESCRIPTION, ROLE_CLAIM, AUTH_TYPE, PROVISIONING_USERSTORE_DOMAIN, IS_LOCAL_CLAIM_DIALECT," +
            "IS_SEND_LOCAL_SUBJECT_ID, IS_SEND_AUTH_LIST_OF_IDPS, IS_USE_TENANT_DOMAIN_SUBJECT, " +
            "IS_USE_USER_DOMAIN_SUBJECT, SUBJECT_CLAIM_URI, IS_SAAS_APP FROM SP_APP WHERE APP_NAME = ? AND TENANT_ID=" +
            " ?";
    public static final String LOAD_AUTH_TYPE_BY_APP_ID = "SELECT AUTH_TYPE FROM SP_APP WHERE ID = ? AND TENANT_ID = ?";
    public static final String LOAD_APP_NAME_BY_APP_ID = "SELECT APP_NAME FROM SP_APP WHERE ID = ? AND TENANT_ID = ?";
    public static final String LOAD_CLIENTS_INFO_BY_APP_ID = "SELECT INBOUND_AUTH_KEY, INBOUND_AUTH_TYPE, PROP_NAME, " +
            "PROP_VALUE FROM  SP_INBOUND_AUTH WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String LOAD_STEPS_INFO_BY_APP_ID = "SELECT STEP_ORDER, AUTHENTICATOR_ID, IS_SUBJECT_STEP, " +
            "IS_ATTRIBUTE_STEP "
            + "FROM SP_AUTH_STEP INNER JOIN SP_FEDERATED_IDP "
            + "ON SP_AUTH_STEP.ID=SP_FEDERATED_IDP.ID "
            + "WHERE APP_ID = ?";
    public static final String LOAD_STEP_ID_BY_APP_ID = "SELECT ID FROM SP_AUTH_STEP WHERE APP_ID = ?";
    public static final String LOAD_HUB_IDP_BY_NAME = "SELECT IS_FEDERATION_HUB FROM IDP WHERE NAME = ? AND TENANT_ID " +
            "= ?";

    public static final String LOAD_CLAIM_MAPPING_BY_APP_ID = "SELECT IDP_CLAIM, SP_CLAIM, IS_REQUESTED,DEFAULT_VALUE " +
            "FROM SP_CLAIM_MAPPING WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String LOAD_CLAIM_MAPPING_BY_APP_NAME = "SELECT IDP_CLAIM, SP_CLAIM, IS_REQUESTED,DEFAULT_VALUE "
            + "FROM SP_CLAIM_MAPPING WHERE APP_ID = (SELECT ID FROM SP_APP WHERE APP_NAME = ?) AND TENANT_ID = ?";
    public static final String LOAD_ROLE_MAPPING_BY_APP_ID = "SELECT IDP_ROLE, SP_ROLE FROM SP_ROLE_MAPPING WHERE APP_ID"+
            " = ? AND TENANT_ID = ?";

    public static final String LOAD_CLAIM_CONIFG_BY_APP_ID = "SELECT ROLE_CLAIM, IS_LOCAL_CLAIM_DIALECT, " +
            "IS_SEND_LOCAL_SUBJECT_ID FROM SP_APP WHERE TENANT_ID= ? AND ID = ?";

    public static final String LOAD_LOCAL_AND_OUTBOUND_CONFIG_BY_APP_ID = "SELECT IS_USE_TENANT_DOMAIN_SUBJECT, " +
            "IS_USE_USER_DOMAIN_SUBJECT, IS_SEND_AUTH_LIST_OF_IDPS, SUBJECT_CLAIM_URI FROM SP_APP WHERE TENANT_ID= ? " +
            "AND ID = ?";

    public static final String LOAD_REQ_PATH_AUTHENTICATORS_BY_APP_ID = "SELECT AUTHENTICATOR_NAME FROM " +
            "SP_REQ_PATH_AUTHENTICATOR WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String LOAD_PRO_PROPERTIES_BY_APP_ID = "SELECT PROVISIONING_USERSTORE_DOMAIN, IS_DUMB_MODE FROM " +
            "SP_APP WHERE TENANT_ID= ? AND ID = ?";
    public static final String LOAD_PRO_CONNECTORS_BY_APP_ID = "SELECT IDP_NAME, CONNECTOR_NAME, IS_JIT_ENABLED, " +
            "BLOCKING FROM SP_PROVISIONING_CONNECTOR WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String LOAD_UM_PERMISSIONS = "SELECT UM_ID, UM_RESOURCE_ID FROM UM_PERMISSION WHERE " +
            "UM_RESOURCE_ID LIKE ?";
    public static final String LOAD_UM_PERMISSIONS_W = "SELECT UM_ID FROM UM_PERMISSION WHERE UM_RESOURCE_ID = ?";

    // DELETE queries
    public static final String REMOVE_APP_FROM_APPMGT_APP = "DELETE FROM SP_APP WHERE APP_NAME = ? AND TENANT_ID = ?";
    public static final String REMOVE_APP_FROM_APPMGT_APP_WITH_ID = "DELETE FROM SP_APP WHERE ID = ? AND TENANT_ID = ?";
    public static final String REMOVE_CLIENT_FROM_APPMGT_CLIENT = "DELETE FROM SP_INBOUND_AUTH WHERE APP_ID = ? " +
            "AND TENANT_ID = ?";
    public static final String REMOVE_STEP_FROM_APPMGT_STEP = "DELETE FROM SP_AUTH_STEP WHERE APP_ID = ? AND " +
            "TENANT_ID = ?";
    public static final String REMOVE_CLAIM_MAPPINGS_FROM_APPMGT_CLAIM_MAPPING = "DELETE FROM SP_CLAIM_MAPPING " +
            "WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String REMOVE_ROLE_MAPPINGS_FROM_APPMGT_ROLE_MAPPING = "DELETE FROM SP_ROLE_MAPPING " +
            "WHERE APP_ID = ? AND TENANT_ID = ?";
    public static final String REMOVE_REQ_PATH_AUTHENTICATOR = "DELETE FROM SP_REQ_PATH_AUTHENTICATOR WHERE " +
            "APP_ID = ? AND TENANT_ID = ?";
    public static final String REMOVE_PRO_CONNECTORS = "DELETE FROM SP_PROVISIONING_CONNECTOR WHERE APP_ID = ? AND " +
            "TENANT_ID = ?";
    public static final String REMOVE_UM_PERMISSIONS = "DELETE FROM UM_PERMISSION WHERE UM_ID = ?";
    public static final String REMOVE_UM_ROLE_PERMISSION = "DELETE FROM UM_ROLE_PERMISSION WHERE UM_PERMISSION_ID = ?";

    // DELETE query - Oauth
    public static final String REMOVE_OAUTH_APPLICATION = "DELETE FROM IDN_OAUTH_CONSUMER_APPS WHERE CONSUMER_KEY=?";

    public static final String LOAD_IDP_AUTHENTICATOR_ID = "SELECT A.ID FROM IDP_AUTHENTICATOR A JOIN IDP B ON A" +
            ".IDP_ID= B.ID WHERE A.NAME =? AND B.NAME=? AND ((A.TENANT_ID =? AND B.TENANT_ID =?) OR (B.TENANT_ID=? " +
            "AND B.NAME LIKE 'SHARED_%'))";
    public static final String LOAD_IDP_AND_AUTHENTICATOR_NAMES = "SELECT A.NAME, B.NAME, " +
            "B.DISPLAY_NAME FROM IDP A JOIN IDP_AUTHENTICATOR B ON A.ID = B.IDP_ID WHERE B.ID =? AND ((A.TENANT_ID =?" +
            " AND B.TENANT_ID =?) OR  (A.TENANT_ID=? AND A.NAME LIKE 'SHARED_%' AND B.TENANT_ID=?))";
    public static final String STORE_LOCAL_AUTHENTICATOR = "INSERT INTO IDP_AUTHENTICATOR (TENANT_ID, IDP_ID, NAME," +
            "IS_ENABLED, DISPLAY_NAME) VALUES (?, (SELECT ID FROM IDP WHERE IDP.NAME=? AND IDP.TENANT_ID =?), ?, ?, ?)";

    public static final String GET_SP_METADATA_BY_SP_ID = "SELECT ID, NAME, VALUE, DISPLAY_NAME FROM SP_METADATA " +
            "WHERE SP_ID = ?";
    public static final String ADD_SP_METADATA = "INSERT INTO SP_METADATA (SP_ID, NAME, VALUE, DISPLAY_NAME, " +
            "TENANT_ID) VALUES (?, ?, ?, ?, ?)";
    public static final String DELETE_SP_METADATA = "DELETE FROM SP_METADATA WHERE SP_ID = ?";


}
