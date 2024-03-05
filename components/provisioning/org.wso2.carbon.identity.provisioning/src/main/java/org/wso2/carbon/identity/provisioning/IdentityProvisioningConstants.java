/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.provisioning;

import org.wso2.carbon.identity.application.mgt.ApplicationConstants;

/**
 * Identity provision related constants
 */
public class IdentityProvisioningConstants {

    public static final String WSO2_CARBON_DIALECT = "http://wso2.org/claims";
    public static final String USERNAME_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:username";
    public static final String ID_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:id";
    public static final String OLD_GROUP_NAME_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:group:name:old";
    public static final String NEW_GROUP_NAME_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:group:name:new";
    public static final String USER_STORE_DOMAIN_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:domain";
    public static final String NEW_USER_CLAIM_URI = "org:wso2:carbon:identity:provisioning:new:claim:user";
    public static final String DELETED_USER_CLAIM_URI = "org:wso2:carbon:identity:provisioning:deleted:claim:user";
    public static final String GROUP_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:group";
    public static final String NEW_GROUP_CLAIM_URI = "org:wso2:carbon:identity:provisioning:new:claim:group";
    public static final String DELETED_GROUP_CLAIM_URI = "org:wso2:carbon:identity:provisioning:deleted:claim:group";
    public static final String PASSWORD_CLAIM_URI = "org:wso2:carbon:identity:provisioning:claim:password";
    public static final String LOCAL_SP = ApplicationConstants.LOCAL_SP;
    public static final String JIT_PROVISIONING_ENABLED = "jitProvisioningEnabled";
    public static final String LAST_MODIFIED_CLAIM = "http://wso2.org/claims/modified";
    public static final String ASK_PASSWORD_CLAIM = "http://wso2.org/claims/identity/askPassword";
    public static final String SELF_SIGNUP_ROLE = "Internal/selfsignup";

    public static final String IS_TRUE_VALUE = "1";
    public static final String IS_FALSE_VALUE = "0";

    // Outbound provisioning constants.
    public static final String USE_USER_TENANT_DOMAIN_FOR_OUTBOUND_PROVISIONING_IN_SAAS_APPS = "OutboundProvisioning.useUserTenantDomainInSaasApps";
    public static final String APPLICATION_BASED_OUTBOUND_PROVISIONING_ENABLED = "OutboundProvisioning.enableApplicationBasedOutboundProvisioning";

    public static class SQLQueries {

        public static final String ADD_PROVISIONING_ENTITY_SQL = "INSERT INTO IDP_PROVISIONING_ENTITY " +
              "(PROVISIONING_CONFIG_ID, ENTITY_TYPE, ENTITY_LOCAL_USERSTORE, ENTITY_NAME, ENTITY_VALUE, TENANT_ID, " +
              "ENTITY_LOCAL_ID) VALUES (?, ?, ?, ?, ?, ?,?)";

        public static final String DELETE_PROVISIONING_ENTITY_SQL = "DELETE FROM IDP_PROVISIONING_ENTITY WHERE (PROVISIONING_CONFIG_ID=? "
                + "AND ENTITY_TYPE=? AND ENTITY_LOCAL_USERSTORE=? AND ENTITY_NAME=? AND TENANT_ID=?)";

        public static final String GET_PROVISIONING_ENTITY_SQL = "SELECT ENTITY_VALUE "
                + "FROM IDP_PROVISIONING_ENTITY WHERE PROVISIONING_CONFIG_ID=? AND ENTITY_TYPE=? AND "
                + "ENTITY_LOCAL_USERSTORE=? AND ENTITY_NAME=? AND TENANT_ID=?";

        public static final String GET_IDP_PROVISIONING_CONFIG_ID_SQL = "SELECT ID FROM IDP_PROVISIONING_CONFIG WHERE IDP_ID=? AND PROVISIONING_CONNECTOR_TYPE=?";

        public static final String GET_PROVISIONED_ENTITY_NAME_SQL = "SELECT ENTITY_NAME FROM IDP_PROVISIONING_ENTITY" +
                                                                     " WHERE ENTITY_LOCAL_ID=?";

        public static final String UPDATE_PROVISIONED_ENTITY_NAME_SQL = "UPDATE IDP_PROVISIONING_ENTITY SET " +
                                                                        "ENTITY_NAME=? WHERE ENTITY_LOCAL_ID=?";

        public static final String GET_SP_NAMES_OF_PROVISIONING_CONNECTORS_BY_IDP = "SELECT DISTINCT(APP.APP_NAME) " +
                                                                                    "FROM SP_PROVISIONING_CONNECTOR " +
                                                                                    "PC JOIN SP_APP APP ON APP.ID = " +
                                                                                    "PC.APP_ID WHERE PC.IDP_NAME = ? " +
                                                                                    "AND APP.TENANT_ID = PC.TENANT_ID" +
                                                                                    " AND APP.TENANT_ID = ?";

        private SQLQueries(){}
    }

    public class PropertyConfig {

        public static final String CONFIG_FILE_NAME = "identity-provision.properties";

        public static final String IDENTITY_PROVISIONING_REGISTORED_CONNECTORS = "Identity.Provisioning.Registored.Connectors";
        public static final String IDENTITY_PROVISIONING_CONNECTOR_NAME = "Identity.Provisioning.Connector.Name";
        public static final String IDENTITY_PROVISIONING_CONNECTOR_CACHE_NAME = "ProvisioningConnectorCache";
        public static final String IDENTITY_PROVISIONING_ENTITY_CACHE_NAME = "ProvisioningEntityCache";
        public static final String IDENTITY_PROVISIONING_SP_CONNECTOR_CACHE_NAME = "ServiceProviderProvisioningConnectorCache";

        public static final String PREFIX_IDENTITY_PROVISIONING_CONNECTOR = "Identity.Provisioning.Connector.";
        public static final String PREFIX_IDENTITY_PROVISIONING_CONNECTOR_ENABLE = "Identity.Provisioning.Connector.Enable.";
        public static final String PREFIX_IDENTITY_PROVISIONING_CONNECTOR_CLASS = "Identity.Provisioning.Connector.Class.";

        public static final String DELIMATOR = ",";

        private PropertyConfig(){}
    }
}
