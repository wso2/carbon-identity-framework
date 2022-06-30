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

package org.wso2.carbon.identity.application.mgt.dao.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.IdentityApplicationRegistrationFailureException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ConsentConfig;
import org.wso2.carbon.identity.application.common.model.ConsentPurpose;
import org.wso2.carbon.identity.application.common.model.ConsentPurposeConfigs;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.InboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.JustInTimeProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.LocalRole;
import org.wso2.carbon.identity.application.common.model.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.application.mgt.AbstractInboundAuthenticatorConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants.ApplicationTableColumns;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtSystemConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.PaginatableFilterableApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.CertificateRetrievingException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.ADVANCED_CONFIG;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.APPLICATION_ALREADY_EXISTS;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.APPLICATION_NOT_DISCOVERABLE;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_LIMIT;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_OFFSET;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.SORTING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.IS_MANAGEMENT_APP_SP_PROPERTY_DISPLAY_NAME;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.IS_MANAGEMENT_APP_SP_PROPERTY_NAME;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.JWKS_URI_SP_PROPERTY_NAME;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.TEMPLATE_ID_SP_PROPERTY_DISPLAY_NAME;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.TEMPLATE_ID_SP_PROPERTY_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.LOCAL_SP;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.ADD_CERTIFICATE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.ADD_SP_CONSENT_PURPOSE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.ADD_SP_METADATA;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.ADD_SP_METADATA_H2;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.DELETE_SP_CONSENT_PURPOSES;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.DELETE_SP_DIALECTS_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.DELETE_SP_METADATA;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_CERTIFICATE_BY_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_CERTIFICATE_ID_BY_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_SP_METADATA_BY_SP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.GET_SP_METADATA_BY_SP_ID_H2;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.IS_APP_BY_TENANT_AND_UUID_DISCOVERABLE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_BY_TENANT_AND_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_BY_TENANT_AND_UUID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_COUNT_BY_TENANT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_COUNT_BY_TENANT_AND_APP_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_ID_BY_APP_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_ID_BY_UUID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_DB2SQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_INFORMIX;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MSSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MYSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_ORACLE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_POSTGRESQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_DB2SQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAMES_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_APP_NAME_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_AUTH_TYPE_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_BASIC_APP_INFO_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_BASIC_APP_INFO_BY_APP_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_CLAIM_CONIFG_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_CLAIM_MAPPING_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_CLAIM_MAPPING_BY_APP_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_CLIENTS_INFO_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_DB2;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_INFORMIX;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_MSSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_MYSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_ORACLE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_POSTGRESQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_DB2SQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_INFORMIX;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_MSSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_MYSQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_ORACLE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APPS_BY_TENANT_POSTGRESQL;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APP_COUNT_BY_APP_NAME_AND_TENANT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_DISCOVERABLE_APP_COUNT_BY_TENANT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_HUB_IDP_BY_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_IDP_AND_AUTHENTICATOR_NAMES;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_IDP_AUTHENTICATOR_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_LOCAL_AND_OUTBOUND_CONFIG_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_PRO_CONNECTORS_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_PRO_PROPERTIES_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_REQ_PATH_AUTHENTICATORS_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_ROLE_MAPPING_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_SCRIPT_BY_APP_ID_QUERY;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_SP_CONSENT_PURPOSES;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_SP_DIALECTS_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_STEPS_INFO_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_UM_PERMISSIONS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_UM_PERMISSIONS_W;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.LOAD_UUID_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_APPS_FROM_APPMGT_APP_BY_TENANT_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_APP_FROM_APPMGT_APP;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_APP_FROM_APPMGT_APP_WITH_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_APP_FROM_SP_APP_WITH_UUID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_AUTH_SCRIPT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_CERTIFICATE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_CERTIFICATES_BY_TENANT_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_CLAIM_MAPPINGS_FROM_APPMGT_CLAIM_MAPPING;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_CLIENT_FROM_APPMGT_CLIENT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_PRO_CONNECTORS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_REQ_PATH_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_ROLE_MAPPINGS_FROM_APPMGT_ROLE_MAPPING;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_STEP_FROM_APPMGT_STEP;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_UM_PERMISSIONS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.REMOVE_UM_ROLE_PERMISSION;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_BASIC_APPINFO;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_CLAIM_MAPPING;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_CLIENT_INFO;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_LOCAL_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_PRO_CONNECTORS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_REQ_PATH_AUTHENTICATORS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_ROLE_MAPPING;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_SP_AUTH_SCRIPT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_SP_DIALECTS_BY_APP_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_STEP_IDP_AUTH;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.STORE_STEP_INFO;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_AUTH_TYPE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_CLAIM_DIALEECT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_ENABLE_AUTHORIZATION;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_OWNER_UPDATE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_PRO_PROPERTIES;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_ROLE_CLAIM;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SEND_AUTH_LIST_OF_IDPS;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SEND_LOCAL_SUB_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_SUBJECT_CLAIM_URI;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_USE_TENANT_DOMAIN_LOCAL_SUBJECT_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APPINFO_WITH_USE_USERSTORE_DOMAIN_LOCAL_SUBJECT_ID;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_BASIC_APP_INFO_WITH_CONSENT_ENABLED;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_CERTIFICATE;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtDBQueries.UPDATE_SP_PERMISSIONS;
import static org.wso2.carbon.identity.base.IdentityConstants.SKIP_CONSENT;
import static org.wso2.carbon.identity.base.IdentityConstants.SKIP_CONSENT_DISPLAY_NAME;
import static org.wso2.carbon.identity.base.IdentityConstants.SKIP_LOGOUT_CONSENT;
import static org.wso2.carbon.identity.base.IdentityConstants.SKIP_LOGOUT_CONSENT_DISPLAY_NAME;
import static org.wso2.carbon.identity.core.util.JdbcUtils.isH2DB;

/**
 * This class access the IDN_APPMGT database to store/update and delete application configurations.
 * The IDN_APPMGT database contains few tables
 * <ul>
 * <li>IDN_APPMGT_APP</li>
 * <li>IDN_APPMGT_CLIENT</li>
 * <li>IDN_APPMGT_STEP</li>
 * <li>IDN_APPMGT_STEP_IDP</li>
 * <li>IDN_APPMGT_CLAIM_MAPPING</li>
 * <li>IDN_APPMGT_ROLE_MAPPING</li>
 * </ul>
 */
public class ApplicationDAOImpl extends AbstractApplicationDAOImpl implements PaginatableFilterableApplicationDAO {

    private static final String SP_PROPERTY_NAME_CERTIFICATE = "CERTIFICATE";
    private static final String APPLICATION_NAME_CONSTRAINT = "APPLICATION_NAME_CONSTRAINT";

    private Log log = LogFactory.getLog(ApplicationDAOImpl.class);
    private static final Log AUDIT_LOG = CarbonConstants.AUDIT_LOG;
    private static final String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Data : { %s } | Result :  %s ";
    private static final String AUDIT_SUCCESS = "Success";
    private static final String AUDIT_FAIL = "Fail";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    private List<String> standardInboundAuthTypes;
    public static final String USE_DOMAIN_IN_ROLES = "USE_DOMAIN_IN_ROLES";
    public static final String USE_DOMAIN_IN_ROLE_DISPLAY_NAME = "DOMAIN_IN_ROLES";

    public ApplicationDAOImpl() {

        standardInboundAuthTypes = new ArrayList<String>();
        standardInboundAuthTypes.add("oauth2");
        standardInboundAuthTypes.add("wstrust");
        standardInboundAuthTypes.add("samlsso");
        standardInboundAuthTypes.add("openid");
        standardInboundAuthTypes.add("passivests");
        standardInboundAuthTypes.add("kerberos");
    }

    private boolean isCustomInboundAuthType(String authType) {

        return !standardInboundAuthTypes.contains(authType);
    }

    /**
     * Get Service provider properties
     *
     * @param dbConnection database connection
     * @param spId         SP Id
     * @return service provider properties
     */
    private List<ServiceProviderProperty> getServicePropertiesBySpId(Connection dbConnection, int spId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<ServiceProviderProperty> idpProperties = new ArrayList<ServiceProviderProperty>();
        try {
            prepStmt = isH2DB() ? dbConnection.prepareStatement(GET_SP_METADATA_BY_SP_ID_H2) :
                    dbConnection.prepareStatement(GET_SP_METADATA_BY_SP_ID);
            prepStmt.setInt(1, spId);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                ServiceProviderProperty property = new ServiceProviderProperty();
                property.setName(rs.getString("NAME"));
                property.setValue(rs.getString("VALUE"));
                property.setDisplayName(rs.getString("DISPLAY_NAME"));
                idpProperties.add(property);
            }
        } catch (DataAccessException e) {
            throw new SQLException("Error while retrieving SP metadata for SP ID: " + spId, e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
            IdentityApplicationManagementUtil.closeResultSet(rs);
        }
        return idpProperties;
    }

    /**
     * Add Service provider properties
     *
     * @param dbConnection
     * @param spId
     * @param properties
     * @throws SQLException
     */
    private void addServiceProviderProperties(Connection dbConnection, int spId,
                                              List<ServiceProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = isH2DB() ? dbConnection.prepareStatement(ADD_SP_METADATA_H2) :
                    dbConnection.prepareStatement(ADD_SP_METADATA);
            for (ServiceProviderProperty property : properties) {
                if (StringUtils.isNotBlank(property.getValue())) {
                    prepStmt.setInt(1, spId);
                    prepStmt.setString(2, property.getName());
                    prepStmt.setString(3, property.getValue());
                    prepStmt.setString(4, property.getDisplayName());
                    prepStmt.setInt(5, tenantId);
                    prepStmt.addBatch();
                } else {
                    if (log.isDebugEnabled()) {
                        String msg = "SP property '%s' of Sp with id: %d of tenantId: %d is empty or null. " +
                                "Not adding the property to 'SP_METADATA' table.";
                        log.debug(String.format(msg, property.getName(), spId, tenantId));
                    }
                }
            }
            prepStmt.executeBatch();
        } catch (DataAccessException e) {
            String errorMsg = "Error while adding SP properties for SP ID: " + spId + " and tenant ID: " + tenantId;
            throw new SQLException(errorMsg, e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Update Service provider properties
     *
     * @param dbConnection
     * @param spId
     * @param properties
     * @throws SQLException
     */
    private void updateServiceProviderProperties(Connection dbConnection, int spId,
                                                 List<ServiceProviderProperty> properties, int tenantId)
            throws SQLException {

        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(DELETE_SP_METADATA);
            prepStmt.setInt(1, spId);
            prepStmt.executeUpdate();

            addServiceProviderProperties(dbConnection, spId, properties, tenantId);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    /**
     * Stores basic application information and meta-data such as the application name, creator and
     * tenant.
     *
     * @param application
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int createApplication(ServiceProvider application, String tenantDomain)
            throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            ApplicationCreateResult result = persistBasicApplicationInformation(connection, application, tenantDomain);
            IdentityDatabaseUtil.commitTransaction(connection);
            return result.getApplicationId();
        } catch (SQLException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            if (isApplicationConflict(e)) {
                throw new IdentityApplicationManagementClientException(APPLICATION_ALREADY_EXISTS.getCode(),
                        "Application already exists with name: " + application.getApplicationName()
                                + " in tenantDomain: " + tenantDomain);
            }
            throw new IdentityApplicationManagementException("Error while Creating Application", e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    private boolean isApplicationConflict(Exception e) {

        if (!(e instanceof SQLException)) {
            return false;
        }

        // We Detect constraint violations in JDBC drivers which don't throw SQLIntegrityConstraintViolationException
        // by looking at the error message.
        return e instanceof SQLIntegrityConstraintViolationException ||
                StringUtils.containsIgnoreCase(e.getMessage(), APPLICATION_NAME_CONSTRAINT);
    }

    private ApplicationCreateResult persistBasicApplicationInformation(Connection connection,
                                                                       ServiceProvider application,
                                                                       String tenantDomain) throws SQLException {

        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        String qualifiedUsername = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (LOCAL_SP.equals(application.getApplicationName())) {
            qualifiedUsername = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }

        String username = UserCoreUtil.removeDomainFromName(qualifiedUsername);
        String userStoreDomain = IdentityUtil.extractDomainFromName(qualifiedUsername);
        String applicationName = application.getApplicationName();
        String description = application.getDescription();

        if (log.isDebugEnabled()) {
            log.debug("Creating Application " + applicationName + " for user " + qualifiedUsername);
        }

        PreparedStatement storeAppPrepStmt = null;
        ResultSet results = null;
        try {
            String resourceId = generateApplicationResourceId(application);
            String dbProductName = connection.getMetaData().getDatabaseProductName();
            storeAppPrepStmt = connection.prepareStatement(
                    STORE_BASIC_APPINFO,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});

            // TENANT_ID, APP_NAME, USER_STORE, USERNAME, DESCRIPTION, AUTH_TYPE
            storeAppPrepStmt.setInt(1, tenantID);
            storeAppPrepStmt.setString(2, applicationName);
            storeAppPrepStmt.setString(3, userStoreDomain);
            storeAppPrepStmt.setString(4, username);
            storeAppPrepStmt.setString(5, description);
            // by default authentication type would be default.
            // default authenticator is defined system-wide - in the configuration file.
            storeAppPrepStmt.setString(6, ApplicationConstants.AUTH_TYPE_DEFAULT);
            storeAppPrepStmt.setString(7, "0");
            storeAppPrepStmt.setString(8, "0");
            storeAppPrepStmt.setString(9, "0");
            storeAppPrepStmt.setString(10, resourceId);
            storeAppPrepStmt.setString(11, application.getImageUrl());
            storeAppPrepStmt.setString(12, application.getAccessUrl());
            storeAppPrepStmt.execute();

            results = storeAppPrepStmt.getGeneratedKeys();

            int applicationId = 0;
            if (results.next()) {
                applicationId = results.getInt(1);
            }
            // some JDBC Drivers returns this in the result, some don't
            if (applicationId == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the application id, executing Select operation");
                }
                applicationId = getApplicationIDByName(applicationName, tenantID, connection);
            }

            ServiceProviderProperty[] spProperties = application.getSpProperties();
            List<ServiceProviderProperty> serviceProviderProperties = new ArrayList<>(Arrays.asList(spProperties));

            ServiceProviderProperty isManagementAppProperty = buildIsManagementAppProperty(application);
            serviceProviderProperties.add(isManagementAppProperty);

            addServiceProviderProperties(connection, applicationId, serviceProviderProperties, tenantID);


            if (log.isDebugEnabled()) {
                log.debug("Application Stored successfully with applicationId: " + applicationId +
                        " and applicationResourceId: " + resourceId);
            }

            return new ApplicationCreateResult(resourceId, applicationId);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
        }
    }

    @Override
    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        int applicationId = serviceProvider.getApplicationID();
        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            deleteApplicationConfigurations(connection, serviceProvider, applicationId);
            addApplicationConfigurations(connection, serviceProvider, tenantDomain);

            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | UserStoreException | IdentityApplicationManagementException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            throw new IdentityApplicationManagementException("Failed to update application id: " + applicationId, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    private void addApplicationConfigurations(Connection connection, ServiceProvider serviceProvider,
                                              String tenantDomain)
            throws SQLException, UserStoreException, IdentityApplicationManagementException {

        int applicationId = serviceProvider.getApplicationID();
        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);

        if (ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                serviceProvider.getApplicationName())) {
            throw new IdentityApplicationManagementClientException(APPLICATION_ALREADY_EXISTS.getCode(),
                    "Application with name: " + serviceProvider.getApplicationName() + "loaded from the file system.");
        }

        // update basic information of the application.
        // you can change application name, description, isSasApp...
        updateBasicApplicationData(serviceProvider, connection);

        updateApplicationCertificate(serviceProvider, tenantID, connection);

        updateInboundProvisioningConfiguration(applicationId, serviceProvider.getInboundProvisioningConfig(),
                connection);

        // update all in-bound authentication requests.
        updateInboundAuthRequestConfiguration(serviceProvider.getApplicationID(), serviceProvider
                .getInboundAuthenticationConfig(), connection);

        // update local and out-bound authentication configuration.
        updateLocalAndOutboundAuthenticationConfiguration(serviceProvider.getApplicationID(),
                serviceProvider.getLocalAndOutBoundAuthenticationConfig(), connection);

        updateRequestPathAuthenticators(applicationId, serviceProvider.getRequestPathAuthenticatorConfigs(),
                connection);

        updateClaimConfiguration(serviceProvider.getApplicationID(), serviceProvider.getClaimConfig(),
                applicationId, connection);

        updateOutboundProvisioningConfiguration(applicationId,
                serviceProvider.getOutboundProvisioningConfig(), connection);

        if (serviceProvider.getPermissionAndRoleConfig() != null) {
            updatePermissionAndRoleConfiguration(serviceProvider.getApplicationID(),
                    serviceProvider.getPermissionAndRoleConfig(), connection);
            deleteAssignedPermissions(connection, serviceProvider.getApplicationName(),
                    serviceProvider.getPermissionAndRoleConfig().getPermissions());
        }

        updateConfigurationsAsServiceProperties(serviceProvider);
        if (ArrayUtils.isNotEmpty(serviceProvider.getSpProperties())) {
            ServiceProviderProperty[] spProperties = serviceProvider.getSpProperties();
            updateServiceProviderProperties(connection, applicationId, Arrays.asList(spProperties), tenantID);
        }

        // Will be supported with 'Advance Consent Management Feature'.
            /*
            if (serviceProvider.getConsentConfig() != null) {
                updateConsentPurposeConfiguration(connection, applicationId, serviceProvider.getConsentConfig(),
                        tenantID);
            }
            */
    }

    private void deleteApplicationConfigurations(Connection connection, ServiceProvider serviceProvider,
                                                 int applicationId) throws SQLException {

        // delete all in-bound authentication requests.
        deleteInboundAuthRequestConfiguration(serviceProvider.getApplicationID(), connection);
        // delete local and out-bound authentication configuration.
        deleteLocalAndOutboundAuthenticationConfiguration(applicationId, connection);
        deleteRequestPathAuthenticators(applicationId, connection);
        deleteClaimConfiguration(applicationId, connection);
        deleteOutboundProvisioningConfiguration(applicationId, connection);
        deletePermissionAndRoleConfiguration(applicationId, connection);
        // deleteConsentPurposeConfiguration(connection, applicationId, tenantID);
    }

    /**
     * Delete existing consent purpose configurations of the application.
     *
     * @param connection
     * @param applicationId
     * @param tenantId
     */
    private void deleteConsentPurposeConfiguration(Connection connection, int applicationId, int tenantId)
            throws IdentityApplicationManagementException {

        try (PreparedStatement ps = connection.prepareStatement(DELETE_SP_CONSENT_PURPOSES)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, tenantId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while removing existing consent purposes for " +
                    "ApplicationId: " + applicationId + " and TenantId: " +
                    tenantId, e);
        }
    }

    /**
     * Updates the consent purpose configurations of the application.
     *
     * @param connection
     * @param applicationId
     * @param consentConfig
     * @param tenantID
     */
    private void updateConsentPurposeConfiguration(Connection connection, int applicationId,
                                                   ConsentConfig consentConfig, int tenantID)
            throws IdentityApplicationManagementException {

        try (PreparedStatement pst = connection.prepareStatement(UPDATE_BASIC_APP_INFO_WITH_CONSENT_ENABLED)) {
            pst.setString(1, consentConfig.isEnabled() ? "1" : "0");
            pst.setInt(2, tenantID);
            pst.setInt(3, applicationId);
            pst.executeUpdate();
        } catch (SQLException e) {
            String error = String.format("Error while setting consentEnabled: %s for applicationId: %s in tenantId: " +
                    "%s", Boolean.toString(consentConfig.isEnabled()), applicationId, tenantID);
            throw new IdentityApplicationManagementException(error, e);
        }

        ConsentPurposeConfigs consentPurposeConfigs = consentConfig.getConsentPurposeConfigs();
        if (isNull(consentPurposeConfigs)) {
            if (log.isDebugEnabled()) {
                log.debug("ConsentPurposeConfigs entry is null for application ID: " + applicationId);
            }
            return;
        }

        ConsentPurpose[] consentPurposes = consentPurposeConfigs.getConsentPurpose();
        if (isNull(consentPurposes)) {
            if (log.isDebugEnabled()) {
                log.debug("ConsentPurpose entry is null for application ID: " + applicationId);
            }
            return;
        }

        for (ConsentPurpose consentPurpose : consentPurposes) {
            try (PreparedStatement ps = connection.prepareStatement(ADD_SP_CONSENT_PURPOSE)) {
                ps.setInt(1, applicationId);
                ps.setInt(2, consentPurpose.getPurposeId());
                ps.setInt(3, consentPurpose.getDisplayOrder());
                ps.setInt(4, tenantID);
                ps.executeUpdate();
            } catch (SQLException e) {
                String error = String.format("Error while persisting consent purposeId: %s for applicationId: %s " +
                                "in tenantId: %s", consentPurpose.getPurposeId(), applicationId,
                        tenantID);
                throw new IdentityApplicationManagementException(error, e);
            }
        }
    }

    /**
     * Updates the application certificate record in the database, with the certificate in the given service provider
     * object. If the certificate content is available in the given service provider and a reference is not available,
     * create a new database record for the certificate and add the reference to the given service provider object.
     *
     * @param serviceProvider
     * @param tenantID
     * @param connection
     * @throws SQLException
     */
    private void updateApplicationCertificate(ServiceProvider serviceProvider, int tenantID,
                                              Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        // If the certificate content is empty, remove the certificate reference property if exists.
        // And remove the certificate.
        if (StringUtils.isBlank(serviceProvider.getCertificateContent())) {

            ServiceProviderProperty[] serviceProviderProperties = serviceProvider.getSpProperties();

            if (serviceProviderProperties != null) {

                // Get the index of the certificate reference property index in the properties array.
                int certificateReferenceIdIndex = -1;
                String certificateReferenceId = null;
                for (int i = 0; i < serviceProviderProperties.length; i++) {
                    if ("CERTIFICATE".equals(serviceProviderProperties[i].getName())) {
                        certificateReferenceIdIndex = i;
                        certificateReferenceId = serviceProviderProperties[i].getValue();
                        break;
                    }
                }

                // If there is a certificate reference, remove it from the properties array.
                // Removing will be done by creating a new array and copying the elements other than the
                // certificate reference from the existing array,
                if (certificateReferenceIdIndex > -1) {

                    ServiceProviderProperty[] propertiesWithoutCertificateReference =
                            new ServiceProviderProperty[serviceProviderProperties.length - 1];

                    System.arraycopy(serviceProviderProperties, 0, propertiesWithoutCertificateReference,
                            0, certificateReferenceIdIndex);
                    System.arraycopy(serviceProviderProperties, certificateReferenceIdIndex + 1,
                            propertiesWithoutCertificateReference, certificateReferenceIdIndex,
                            propertiesWithoutCertificateReference.length - certificateReferenceIdIndex);

                    serviceProvider.setSpProperties(propertiesWithoutCertificateReference);
                    deleteCertificate(connection, Integer.parseInt(certificateReferenceId));
                }
            }
        } else {
            // First get the certificate reference from the application properties.
            ServiceProviderProperty[] serviceProviderProperties = serviceProvider.getSpProperties();

            String certificateReferenceIdString = getCertificateReferenceID(serviceProviderProperties);

            // If there is a reference, update the relevant certificate record.
            if (certificateReferenceIdString != null) { // Update the existing record.
                PreparedStatement statementToUpdateCertificate = null;
                try {
                    statementToUpdateCertificate = connection.prepareStatement(UPDATE_CERTIFICATE);
                    setBlobValue(serviceProvider.getCertificateContent(), statementToUpdateCertificate, 1);
                    statementToUpdateCertificate.setInt(2, Integer.parseInt(certificateReferenceIdString));

                    statementToUpdateCertificate.executeUpdate();
                } catch (IOException e) {
                    throw new IdentityApplicationManagementException("An error occurred while processing content " +
                            "stream of certificate.", e);
                } finally {
                    IdentityApplicationManagementUtil.closeStatement(statementToUpdateCertificate);
                }
            } else {
                /*
                 There is no existing reference.
                 Persisting the certificate in the given service provider as a new record.
                  */
                persistApplicationCertificate(serviceProvider, tenantID, connection);
            }
        }
    }

    /**
     * Returns the certificate reference ID from the given service provider properties.
     *
     * @param serviceProviderProperties
     * @return
     */
    private String getCertificateReferenceID(ServiceProviderProperty[] serviceProviderProperties) {

        String certificateReferenceId = null;
        if (serviceProviderProperties != null) {
            for (ServiceProviderProperty property : serviceProviderProperties) {
                if (SP_PROPERTY_NAME_CERTIFICATE.equals(property.getName())) {
                    certificateReferenceId = property.getValue();
                }
            }
        }
        return certificateReferenceId;
    }

    /**
     * Persists the certificate content of the given service provider object,
     * and adds ID of the newly added certificate as a property of the service provider object.
     *
     * @param serviceProvider
     * @param tenantID
     * @param connection
     * @throws SQLException
     */
    private void persistApplicationCertificate(ServiceProvider serviceProvider, int tenantID,
                                               Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        // Configure the prepared statement to collect the auto generated id of the database record.
        PreparedStatement statementToAddCertificate = null;
        ResultSet results = null;
        try {

            String dbProductName = connection.getMetaData().getDatabaseProductName();
            statementToAddCertificate = connection.prepareStatement(ADD_CERTIFICATE,
                    new String[]{DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});

            statementToAddCertificate.setString(1, serviceProvider.getApplicationName());
            setBlobValue(serviceProvider.getCertificateContent(), statementToAddCertificate, 2);
            statementToAddCertificate.setInt(3, tenantID);
            statementToAddCertificate.execute();

            results = statementToAddCertificate.getGeneratedKeys();

            int newlyAddedCertificateID = 0;
            if (results.next()) {
                newlyAddedCertificateID = results.getInt(1);
            }

            // Not all JDBC drivers support getting the auto generated database ID.
            // So if the ID is not returned, get the ID by querying the database passing the certificate name.
            if (newlyAddedCertificateID == 0) {
                if (log.isDebugEnabled()) {
                    log.debug("JDBC Driver did not return the application id, executing Select operation");
                }
                newlyAddedCertificateID = getCertificateIDByName(serviceProvider.getApplicationName(),
                        tenantID, connection);
            }
            addApplicationCertificateReferenceAsServiceProviderProperty(serviceProvider, newlyAddedCertificateID);
        } catch (IOException e) {
            throw new IdentityApplicationManagementException("An error occurred while processing content stream " +
                    "of certificate.", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(statementToAddCertificate);
        }
    }

    /**
     * Add the given certificate ID as a property of the given service provider object.
     *
     * @param serviceProvider
     * @param newlyAddedCertificateID
     */
    private void addApplicationCertificateReferenceAsServiceProviderProperty(ServiceProvider serviceProvider,
                                                                             int newlyAddedCertificateID) {

        ServiceProviderProperty[] serviceProviderProperties = serviceProvider.getSpProperties();
        ServiceProviderProperty[] newServiceProviderProperties;
        if (serviceProviderProperties != null) {
            newServiceProviderProperties = new ServiceProviderProperty[
                    serviceProviderProperties.length + 1];

            for (int i = 0; i < serviceProviderProperties.length; i++) {
                newServiceProviderProperties[i] = serviceProviderProperties[i];
            }
        } else {
            newServiceProviderProperties = new ServiceProviderProperty[1];
        }

        ServiceProviderProperty propertyForCertificate = new ServiceProviderProperty();
        propertyForCertificate.setDisplayName("CERTIFICATE");
        propertyForCertificate.setName("CERTIFICATE");
        propertyForCertificate.setValue(String.valueOf(newlyAddedCertificateID));

        newServiceProviderProperties[newServiceProviderProperties.length - 1] = propertyForCertificate;

        serviceProvider.setSpProperties(newServiceProviderProperties);
    }

    /**
     * Returns the database ID of the certificate with the given certificate name and the tenant ID.
     *
     * @param applicationName
     * @param tenantID
     * @param connection
     * @return
     * @throws SQLException
     */
    private int getCertificateIDByName(String applicationName, int tenantID, Connection connection)
            throws SQLException {

        PreparedStatement statementToGetCertificateId = null;
        ResultSet results = null;
        try {
            statementToGetCertificateId = connection.prepareStatement(GET_CERTIFICATE_ID_BY_NAME);
            statementToGetCertificateId.setString(1, applicationName);
            statementToGetCertificateId.setInt(2, tenantID);

            results = statementToGetCertificateId.executeQuery();

            int applicationId = -1;
            while (results.next()) {
                applicationId = results.getInt(1);
            }

            return applicationId;
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(results);
            IdentityApplicationManagementUtil.closeStatement(statementToGetCertificateId);
        }
    }

    /**
     * @param serviceProvider
     * @param connection
     * @throws SQLException
     * @throws UserStoreException
     * @throws IdentityApplicationManagementException
     */

    private void updateBasicApplicationData(ServiceProvider serviceProvider, Connection connection)
            throws SQLException, UserStoreException, IdentityApplicationManagementException {

        int applicationId = serviceProvider.getApplicationID();
        String applicationName = serviceProvider.getApplicationName();
        String description = serviceProvider.getDescription();
        boolean isSaasApp = serviceProvider.isSaasApp();
        boolean isDiscoverable = serviceProvider.isDiscoverable();
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String storedAppName = null;

        if (applicationName == null) {
            // check for required attributes.
            throw new IdentityApplicationManagementException("Application Name is required.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Updating Application with id: " + applicationId);
        }
        // reads back the Application Name. This is to check if the Application
        // has been renamed
        storedAppName = getApplicationName(applicationId, connection);

        if (log.isDebugEnabled()) {
            log.debug("Stored application name for id: " + applicationId + " is " + storedAppName);
        }

        boolean validateRoles = ApplicationMgtUtil.validateRoles();
        // only if the application has been renamed TODO: move to OSGi layer
        if (!StringUtils.equals(applicationName, storedAppName) && validateRoles) {
            String applicationNameforRole = IdentityUtil.addDomainToName(applicationName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            String storedAppNameforRole = IdentityUtil.addDomainToName(storedAppName, ApplicationConstants.
                    APPLICATION_DOMAIN);
            // rename the role
            ApplicationMgtUtil.renameRole(storedAppNameforRole, applicationNameforRole);
            if (log.isDebugEnabled()) {
                log.debug("Renaming application role from " + storedAppName + " to "
                        + applicationName);
            }
            Map<String, String> applicationPermissions = readApplicationPermissions(storedAppName);
            for (Map.Entry<String, String> entry : applicationPermissions.entrySet()) {
                updatePermissionPath(entry.getKey(), entry.getValue().replace(storedAppName.toLowerCase(),
                        applicationName.toLowerCase()));
            }
        }

        boolean isValidUserForOwnerUpdate = ApplicationMgtUtil.isValidApplicationOwner(serviceProvider);
        String sql;
        if (isValidUserForOwnerUpdate) {
            sql = UPDATE_BASIC_APPINFO_WITH_OWNER_UPDATE;
        } else {
            sql = UPDATE_BASIC_APPINFO;
        }

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, sql)) {
            statement.setString(ApplicationTableColumns.APP_NAME, applicationName);
            statement.setString(ApplicationTableColumns.DESCRIPTION, description);
            statement.setString(ApplicationTableColumns.IS_SAAS_APP, isSaasApp ? "1" : "0");
            statement.setString(ApplicationTableColumns.IS_DISCOVERABLE, isDiscoverable ? "1" : "0");
            statement.setString(ApplicationTableColumns.IMAGE_URL, serviceProvider.getImageUrl());
            statement.setString(ApplicationTableColumns.ACCESS_URL, serviceProvider.getAccessUrl());
            if (isValidUserForOwnerUpdate) {
                User owner = serviceProvider.getOwner();
                statement.setString(ApplicationTableColumns.USERNAME, owner.getUserName());
                statement.setString(ApplicationTableColumns.USER_STORE, owner.getUserStoreDomain());
            }
            statement.setInt(ApplicationTableColumns.TENANT_ID, tenantID);
            statement.setInt(ApplicationTableColumns.ID, applicationId);

            statement.executeUpdate();
        }

        if (log.isDebugEnabled()) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
            log.debug("Application with name: " + applicationName + " , id: " + applicationId + " in tenantDomain: "
                    + tenantDomain + " updated successfully.");
        }
    }

    private List<Property> filterEmptyProperties(Property[] propertiesArray) {

        List<Property> propertyArrayList = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(propertiesArray)) {
            for (Property property : propertiesArray) {
                if (property != null && StringUtils.isNotBlank(property.getValue())) {
                    propertyArrayList.add(property);
                }
            }
        }
        return propertyArrayList;
    }

    /**
     * @param applicationId
     * @param inBoundAuthenticationConfig
     * @param connection
     * @throws SQLException
     */
    private void updateInboundAuthRequestConfiguration(int applicationId, InboundAuthenticationConfig
            inBoundAuthenticationConfig, Connection connection) throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement inboundAuthReqConfigPrepStmt = null;

        try {
            if (inBoundAuthenticationConfig == null
                    || inBoundAuthenticationConfig.getInboundAuthenticationRequestConfigs() == null
                    || inBoundAuthenticationConfig.getInboundAuthenticationRequestConfigs().length == 0) {
                // no in-bound authentication requests defined.
                return;
            }

            inboundAuthReqConfigPrepStmt = connection.prepareStatement(STORE_CLIENT_INFO);
            InboundAuthenticationRequestConfig[] authRequests = inBoundAuthenticationConfig
                    .getInboundAuthenticationRequestConfigs();

            for (InboundAuthenticationRequestConfig authRequest : authRequests) {
                if (authRequest == null || authRequest.getInboundAuthType() == null) {
                    log.warn("Invalid in-bound authentication request");
                    // not a valid authentication request. Must have client and a type.
                    continue;
                }

                Property[] propertiesArray = authRequest.getProperties();
                List<Property> propertyArrayList = new ArrayList<>();

                String authKey = null;
                String inboundConfigType = ApplicationConstants.STANDARD_APPLICATION;
                if (standardInboundAuthTypes.contains(authRequest.getInboundAuthType())) {
                    authKey = authRequest.getInboundAuthKey();
                    propertyArrayList = filterEmptyProperties(propertiesArray);
                } else {
                    AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig =
                            ApplicationManagementServiceComponentHolder.getInboundAuthenticatorConfig(authRequest
                                    .getInboundAuthType() + ":" + authRequest.getInboundConfigType());
                    if (inboundAuthenticatorConfig != null &&
                            StringUtils.isNotBlank(inboundAuthenticatorConfig.getRelyingPartyKey())) {
                        if (propertiesArray != null && propertiesArray.length > 0) {
                            for (Property prop : propertiesArray) {
                                if (inboundAuthenticatorConfig.getRelyingPartyKey().equals(prop.getName())) {
                                    if (StringUtils.isNotBlank(prop.getValue())) {
                                        authKey = prop.getValue();
                                    }
                                } else {
                                    if (StringUtils.isNotBlank(prop.getValue())) {
                                        propertyArrayList.add(prop);
                                    }
                                }
                            }
                        }
                    } else {
                        propertyArrayList = filterEmptyProperties(propertiesArray);
                    }
                }
                if (StringUtils.isBlank(authKey)) {
                    String applicationName = getApplicationName(applicationId, connection);
                    if (StringUtils.isNotBlank(applicationName)) {
                        authKey = applicationName;
                    }
                }
                if (StringUtils.isNotBlank(authRequest.getInboundConfigType())) {
                    inboundConfigType = authRequest.getInboundConfigType();
                }
                if (!propertyArrayList.isEmpty()) {
                    for (Property prop : propertyArrayList) {
                        inboundAuthReqConfigPrepStmt.setInt(1, tenantID);
                        inboundAuthReqConfigPrepStmt.setString(2, authKey);
                        inboundAuthReqConfigPrepStmt.setString(3, authRequest.getInboundAuthType());
                        inboundAuthReqConfigPrepStmt.setString(4, prop.getName());
                        inboundAuthReqConfigPrepStmt.setString(5, prop.getValue());
                        inboundAuthReqConfigPrepStmt.setInt(6, applicationId);
                        inboundAuthReqConfigPrepStmt.setString(7, inboundConfigType);
                        inboundAuthReqConfigPrepStmt.addBatch();
                    }
                } else {
                    inboundAuthReqConfigPrepStmt.setInt(1, tenantID);
                    inboundAuthReqConfigPrepStmt.setString(2, authKey);
                    inboundAuthReqConfigPrepStmt.setString(3, authRequest.getInboundAuthType());
                    inboundAuthReqConfigPrepStmt.setString(4, null);
                    inboundAuthReqConfigPrepStmt.setString(5, null);
                    inboundAuthReqConfigPrepStmt.setInt(6, applicationId);
                    inboundAuthReqConfigPrepStmt.setString(7, inboundConfigType);
                    inboundAuthReqConfigPrepStmt.addBatch();
                }

                if (log.isDebugEnabled()) {
                    log.debug("Updating inbound authentication request configuration of the application "
                            + applicationId
                            + "inbound auth key: "
                            + authRequest.getInboundAuthKey()
                            + " inbound auth type: "
                            + authRequest.getInboundAuthType());
                }
            }

            inboundAuthReqConfigPrepStmt.executeBatch();
        } catch (SQLException e) {
            log.error("Error occurred while updating the Inbound Authentication Request Configuration.", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundAuthReqConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param inBoundProvisioningConfig
     * @param connection
     * @throws SQLException
     */
    private void updateInboundProvisioningConfiguration(int applicationId,
                                                        InboundProvisioningConfig inBoundProvisioningConfig,
                                                        Connection connection)
            throws SQLException {

        if (inBoundProvisioningConfig == null) {
            return;
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement inboundProConfigPrepStmt = null;

        try {
            inboundProConfigPrepStmt = connection.prepareStatement(UPDATE_BASIC_APPINFO_WITH_PRO_PROPERTIES);

            // PROVISIONING_USERSTORE_DOMAIN=?
            inboundProConfigPrepStmt.setString(1, inBoundProvisioningConfig.getProvisioningUserStore());
            inboundProConfigPrepStmt.setString(2, inBoundProvisioningConfig.isDumbMode() ? "1" : "0");
            inboundProConfigPrepStmt.setInt(3, tenantID);
            inboundProConfigPrepStmt.setInt(4, applicationId);
            inboundProConfigPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundProConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param outBoundProvisioningConfig
     * @param connection
     * @throws SQLException
     */
    private void updateOutboundProvisioningConfiguration(int applicationId,
                                                         OutboundProvisioningConfig outBoundProvisioningConfig,
                                                         Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement outboundProConfigPrepStmt = null;

        if (outBoundProvisioningConfig != null) {
            IdentityProvider[] proProviders = outBoundProvisioningConfig
                    .getProvisioningIdentityProviders();

            try {
                if (ArrayUtils.isEmpty(proProviders)) {
                    // no in-bound authentication requests defined.
                    return;
                }

                outboundProConfigPrepStmt = connection.prepareStatement(STORE_PRO_CONNECTORS);
                // TENANT_ID, IDP_NAME, CONNECTOR_NAME, APP_ID

                for (IdentityProvider proProvider : proProviders) {
                    if (proProvider != null) {
                        ProvisioningConnectorConfig proConnector = proProvider
                                .getDefaultProvisioningConnectorConfig();
                        if (proConnector == null) {
                            continue;
                        }

                        String jitEnabled = "0";

                        if (proProvider.getJustInTimeProvisioningConfig() != null
                                && proProvider.getJustInTimeProvisioningConfig()
                                .isProvisioningEnabled()) {
                            jitEnabled = "1";
                        }

                        String blocking = "0";

                        if (proProvider.getDefaultProvisioningConnectorConfig() != null
                                && proProvider.getDefaultProvisioningConnectorConfig().isBlocking()) {
                            blocking = "1";
                        }

                        String ruleEnabled = "0";

                        if (proProvider.getDefaultProvisioningConnectorConfig() != null
                                && proProvider.getDefaultProvisioningConnectorConfig().isRulesEnabled()) {
                            ruleEnabled = "1";
                        }

                        outboundProConfigPrepStmt.setInt(1, tenantID);
                        outboundProConfigPrepStmt.setString(2, proProvider.getIdentityProviderName());
                        outboundProConfigPrepStmt.setString(3, proConnector.getName());
                        outboundProConfigPrepStmt.setInt(4, applicationId);
                        outboundProConfigPrepStmt.setString(5, jitEnabled);
                        outboundProConfigPrepStmt.setString(6, blocking);
                        outboundProConfigPrepStmt.setString(7, ruleEnabled);
                        outboundProConfigPrepStmt.addBatch();

                    }
                }

                outboundProConfigPrepStmt.executeBatch();

            } finally {
                IdentityApplicationManagementUtil.closeStatement(outboundProConfigPrepStmt);
            }
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private InboundProvisioningConfig getInboundProvisioningConfiguration(int applicationId,
                                                                          Connection connection, int tenantID)
            throws SQLException {

        PreparedStatement inboundProConfigPrepStmt = null;
        InboundProvisioningConfig inBoundProvisioningConfig = new InboundProvisioningConfig();
        ResultSet resultSet = null;

        try {

            inboundProConfigPrepStmt = connection.prepareStatement(LOAD_PRO_PROPERTIES_BY_APP_ID);
            // PROVISIONING_USERSTORE_DOMAIN
            inboundProConfigPrepStmt.setInt(1, tenantID);
            inboundProConfigPrepStmt.setInt(2, applicationId);
            resultSet = inboundProConfigPrepStmt.executeQuery();

            while (resultSet.next()) {
                inBoundProvisioningConfig.setProvisioningUserStore(resultSet.getString(1));
                inBoundProvisioningConfig.setDumbMode("1".equals(resultSet.getString(2)));
            }

        } finally {
            IdentityApplicationManagementUtil.closeStatement(inboundProConfigPrepStmt);
        }
        return inBoundProvisioningConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private OutboundProvisioningConfig getOutboundProvisioningConfiguration(int applicationId,
                                                                            Connection connection, int tenantID)
            throws SQLException {

        PreparedStatement outboundProConfigPrepStmt = null;
        OutboundProvisioningConfig outBoundProvisioningConfig = new OutboundProvisioningConfig();
        ResultSet resultSet = null;
        List<IdentityProvider> idpProConnectors = new ArrayList<IdentityProvider>();

        try {

            outboundProConfigPrepStmt = connection.prepareStatement(LOAD_PRO_CONNECTORS_BY_APP_ID);
            // IDP_NAME, CONNECTOR_NAM
            outboundProConfigPrepStmt.setInt(1, applicationId);
            outboundProConfigPrepStmt.setInt(2, tenantID);
            resultSet = outboundProConfigPrepStmt.executeQuery();

            while (resultSet.next()) {
                ProvisioningConnectorConfig proConnector = null;
                IdentityProvider fedIdp = null;

                fedIdp = new IdentityProvider();
                fedIdp.setIdentityProviderName(resultSet.getString(1));

                proConnector = new ProvisioningConnectorConfig();
                proConnector.setName(resultSet.getString(2));

                if ("1".equals(resultSet.getString(3))) {
                    JustInTimeProvisioningConfig jitConfig = new JustInTimeProvisioningConfig();
                    jitConfig.setProvisioningEnabled(true);
                    fedIdp.setJustInTimeProvisioningConfig(jitConfig);
                }

                if ("1".equals(resultSet.getString(4))) {
                    proConnector.setBlocking(true);
                } else {
                    proConnector.setBlocking(false);
                }

                if ("1".equals(resultSet.getString(5))) {
                    proConnector.setRulesEnabled(true);
                } else {
                    proConnector.setRulesEnabled(false);
                }

                fedIdp.setDefaultProvisioningConnectorConfig(proConnector);
                idpProConnectors.add(fedIdp);

            }

            outBoundProvisioningConfig.setProvisioningIdentityProviders(idpProConnectors.toArray(new
                    IdentityProvider[idpProConnectors.size()]));

        } finally {
            IdentityApplicationManagementUtil.closeStatement(outboundProConfigPrepStmt);
        }
        return outBoundProvisioningConfig;
    }

    /**
     * @param applicationId
     * @param localAndOutboundAuthConfig
     * @param connection
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private void updateLocalAndOutboundAuthenticationConfiguration(int applicationId,
                       LocalAndOutboundAuthenticationConfig localAndOutboundAuthConfig,
                       Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (localAndOutboundAuthConfig == null) {
            // no local or out-bound configuration for this service provider.
            return;
        }

        updateAuthenticationScriptConfiguration(applicationId, localAndOutboundAuthConfig, connection, tenantID);

        PreparedStatement updateAuthTypePrepStmt = null;

        PreparedStatement storeSendAuthListOfIdPsPrepStmt = null;
        try {
            storeSendAuthListOfIdPsPrepStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_SEND_AUTH_LIST_OF_IDPS);
            // IS_SEND_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeSendAuthListOfIdPsPrepStmt.setString(1, localAndOutboundAuthConfig
                    .isAlwaysSendBackAuthenticatedListOfIdPs() ? "1" : "0");
            storeSendAuthListOfIdPsPrepStmt.setInt(2, tenantID);
            storeSendAuthListOfIdPsPrepStmt.setInt(3, applicationId);
            storeSendAuthListOfIdPsPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSendAuthListOfIdPsPrepStmt);
        }

        PreparedStatement storeUseTenantDomainInLocalSubjectIdStmt = null;
        try {
            storeUseTenantDomainInLocalSubjectIdStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_USE_TENANT_DOMAIN_LOCAL_SUBJECT_ID);
            // IS_USE_TENANT_DIMAIN_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeUseTenantDomainInLocalSubjectIdStmt.setString(1, localAndOutboundAuthConfig
                    .isUseTenantDomainInLocalSubjectIdentifier() ? "1" : "0");
            storeUseTenantDomainInLocalSubjectIdStmt.setInt(2, tenantID);
            storeUseTenantDomainInLocalSubjectIdStmt.setInt(3, applicationId);
            storeUseTenantDomainInLocalSubjectIdStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeUseTenantDomainInLocalSubjectIdStmt);
        }

        PreparedStatement storeUseUserstoreDomainInLocalSubjectIdStmt = null;
        try {
            storeUseUserstoreDomainInLocalSubjectIdStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_USE_USERSTORE_DOMAIN_LOCAL_SUBJECT_ID);
            // IS_USE_USERSTORE_DIMAIN_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeUseUserstoreDomainInLocalSubjectIdStmt.setString(1, localAndOutboundAuthConfig
                    .isUseUserstoreDomainInLocalSubjectIdentifier() ? "1" : "0");
            storeUseUserstoreDomainInLocalSubjectIdStmt.setInt(2, tenantID);
            storeUseUserstoreDomainInLocalSubjectIdStmt.setInt(3, applicationId);
            storeUseUserstoreDomainInLocalSubjectIdStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeUseUserstoreDomainInLocalSubjectIdStmt);
        }

        PreparedStatement enableAuthzStmt = null;
        try {
            enableAuthzStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_ENABLE_AUTHORIZATION);
            enableAuthzStmt.setString(1, localAndOutboundAuthConfig.isEnableAuthorization() ? "1" : "0");
            enableAuthzStmt.setInt(2, tenantID);
            enableAuthzStmt.setInt(3, applicationId);
            enableAuthzStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(enableAuthzStmt);
        }

        PreparedStatement storeSubjectClaimUri = null;
        try {
            storeSubjectClaimUri = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_SUBJECT_CLAIM_URI);
            // SUBJECT_CLAIM_URI=? WHERE TENANT_ID= ? AND ID = ?
            storeSubjectClaimUri.setString(1, localAndOutboundAuthConfig.getSubjectClaimUri());
            storeSubjectClaimUri.setInt(2, tenantID);
            storeSubjectClaimUri.setInt(3, applicationId);
            storeSubjectClaimUri.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSubjectClaimUri);
        }

        AuthenticationStep[] authSteps = localAndOutboundAuthConfig.getAuthenticationSteps();

        if (authSteps == null || authSteps.length == 0) {
            // if no authentication steps defined - it should be the default behavior.
            localAndOutboundAuthConfig
                    .setAuthenticationType(ApplicationConstants.AUTH_TYPE_DEFAULT);
        }

        try {
            if (localAndOutboundAuthConfig.getAuthenticationType() == null) {
                // no authentication type defined - set to default.
                localAndOutboundAuthConfig
                        .setAuthenticationType(ApplicationConstants.AUTH_TYPE_DEFAULT);
            }

            updateAuthTypePrepStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_AUTH_TYPE);
            // AUTH_TYPE=? WHERE TENANT_ID= ? AND ID = ?
            updateAuthTypePrepStmt.setString(1, localAndOutboundAuthConfig.getAuthenticationType());
            updateAuthTypePrepStmt.setInt(2, tenantID);
            updateAuthTypePrepStmt.setInt(3, applicationId);
            updateAuthTypePrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(updateAuthTypePrepStmt);
        }

        if (authSteps != null && authSteps.length > 0) {
            // we have authentications steps defined.
            PreparedStatement storeStepIDPAuthnPrepStmt = null;
            storeStepIDPAuthnPrepStmt = connection
                    .prepareStatement(STORE_STEP_IDP_AUTH);
            try {

                if (ApplicationConstants.AUTH_TYPE_LOCAL
                        .equalsIgnoreCase(localAndOutboundAuthConfig.getAuthenticationType())) {
                    // for local authentication there can only be only one authentication step and
                    // only one local authenticator.
                    if (authSteps.length != 1
                            || authSteps[0] == null
                            || authSteps[0].getLocalAuthenticatorConfigs() == null
                            || authSteps[0].getLocalAuthenticatorConfigs().length != 1
                            || (authSteps[0].getFederatedIdentityProviders() != null && authSteps[0]
                            .getFederatedIdentityProviders().length >= 1)) {
                        String errorMessage = "Invalid local authentication configuration."
                                + " For local authentication there can only be only one authentication step and"
                                + " only one local authenticator";
                        throw new IdentityApplicationManagementException(errorMessage);
                    }
                } else if (ApplicationConstants.AUTH_TYPE_FEDERATED
                        .equalsIgnoreCase(localAndOutboundAuthConfig.getAuthenticationType())) {
                    // for federated authentication there can only be only one authentication step
                    // and only one federated authenticator - which is the default authenticator of
                    // the corresponding authenticator.
                    if (authSteps.length != 1 || authSteps[0] == null
                            || authSteps[0].getFederatedIdentityProviders() == null
                            || authSteps[0].getFederatedIdentityProviders().length != 1
                            || authSteps[0].getLocalAuthenticatorConfigs().length > 0) {
                        String errorMessage = "Invalid federated authentication configuration."
                                + " For federated authentication there can only be only one authentication step and"
                                + " only one federated authenticator";
                        throw new IdentityApplicationManagementException(errorMessage);
                    }

                    IdentityProvider fedIdp = authSteps[0].getFederatedIdentityProviders()[0];

                    if (fedIdp.getDefaultAuthenticatorConfig() == null ||
                            fedIdp.getFederatedAuthenticatorConfigs() == null) {
                        IdentityProviderDAO idpDAO = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();

                        String defualtAuthName = idpDAO.getDefaultAuthenticator(fedIdp
                                .getIdentityProviderName());

                        // set the default authenticator.
                        FederatedAuthenticatorConfig defaultAuth = new FederatedAuthenticatorConfig();
                        defaultAuth.setName(defualtAuthName);
                        fedIdp.setDefaultAuthenticatorConfig(defaultAuth);
                        fedIdp.setFederatedAuthenticatorConfigs(new FederatedAuthenticatorConfig[]{defaultAuth});
                    }
                }

                // iterating through each step.
                for (AuthenticationStep authStep : authSteps) {
                    int stepId = 0;

                    IdentityProvider[] federatedIdps = authStep.getFederatedIdentityProviders();

                    // an authentication step should have at least one federated identity
                    // provider or a local authenticator.
                    if ((federatedIdps == null || federatedIdps.length == 0)
                            && (authStep.getLocalAuthenticatorConfigs() == null || authStep
                            .getLocalAuthenticatorConfigs().length == 0)) {
                        String errorMesssage = "Invalid authentication configuration."
                                + "An authentication step should have at least one federated identity "
                                + "provider or a local authenticator";
                        throw new IdentityApplicationManagementException(errorMesssage);
                    }

                    // we have valid federated identity providers.
                    PreparedStatement storeStepPrepStmtz = null;
                    ResultSet result = null;

                    try {
                        String dbProductName = connection.getMetaData().getDatabaseProductName();
                        storeStepPrepStmtz = connection.prepareStatement(
                                STORE_STEP_INFO, new String[]{
                                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});
                        // TENANT_ID, STEP_ORDER, APP_ID
                        storeStepPrepStmtz.setInt(1, tenantID);
                        storeStepPrepStmtz.setInt(2, authStep.getStepOrder());
                        storeStepPrepStmtz.setInt(3, applicationId);
                        storeStepPrepStmtz.setString(4, authStep.isSubjectStep() ? "1" : "0");
                        storeStepPrepStmtz.setString(5, authStep.isAttributeStep() ? "1" : "0");
                        storeStepPrepStmtz.execute();

                        result = storeStepPrepStmtz.getGeneratedKeys();

                        if (result.next()) {
                            stepId = result.getInt(1);
                        }
                    } finally {
                        IdentityApplicationManagementUtil.closeResultSet(result);
                        IdentityApplicationManagementUtil.closeStatement(storeStepPrepStmtz);
                    }

                    if (authStep.getLocalAuthenticatorConfigs() != null
                            && authStep.getLocalAuthenticatorConfigs().length > 0) {

                        for (LocalAuthenticatorConfig lclAuthenticator : authStep
                                .getLocalAuthenticatorConfigs()) {
                            // set the identity provider name to LOCAL.
                            int authenticatorId = getAuthentictorID(connection, tenantID,
                                    ApplicationConstants.LOCAL_IDP_NAME, lclAuthenticator.getName());
                            if (authenticatorId < 0) {
                                authenticatorId = addAuthenticator(connection, tenantID,
                                        ApplicationConstants.LOCAL_IDP_NAME,
                                        lclAuthenticator.getName(),
                                        lclAuthenticator.getDisplayName());
                            }
                            if (authenticatorId > 0) {
                                // ID, TENANT_ID, AUTHENTICATOR_ID
                                storeStepIDPAuthnPrepStmt.setInt(1, stepId);
                                storeStepIDPAuthnPrepStmt.setInt(2, tenantID);
                                storeStepIDPAuthnPrepStmt.setInt(3, authenticatorId);
                                storeStepIDPAuthnPrepStmt.addBatch();
                            }

                            if (log.isDebugEnabled()) {
                                log.debug("Updating Local IdP of Application " + applicationId
                                        + " Step Order: " + authStep.getStepOrder() + " IdP: "
                                        + ApplicationConstants.LOCAL_IDP + " Authenticator: "
                                        + lclAuthenticator.getName());
                            }
                        }
                    }

                    // we have federated identity providers.
                    if (federatedIdps != null && federatedIdps.length > 0) {

                        // iterating through each IDP of the step
                        for (IdentityProvider federatedIdp : federatedIdps) {
                            String idpName = federatedIdp.getIdentityProviderName();

                            // the identity provider name wso2carbon-local-idp is reserved.
                            if (ApplicationConstants.LOCAL_IDP.equalsIgnoreCase(idpName)) {
                                throw new IdentityApplicationManagementException(
                                        "The federated IdP name cannot be equal to "
                                                + ApplicationConstants.LOCAL_IDP);
                            }

                            FederatedAuthenticatorConfig[] authenticators = federatedIdp
                                    .getFederatedAuthenticatorConfigs();

                            if (authenticators != null && authenticators.length > 0) {

                                for (FederatedAuthenticatorConfig authenticator : authenticators) {
                                    // ID, TENANT_ID, AUTHENTICATOR_ID
                                    if (authenticator != null) {
                                        int authenticatorId = getAuthentictorID(connection, tenantID,
                                                idpName, authenticator.getName());
                                        if (authenticatorId > 0) {
                                            storeStepIDPAuthnPrepStmt.setInt(1, stepId);
                                            storeStepIDPAuthnPrepStmt.setInt(2, tenantID);
                                            storeStepIDPAuthnPrepStmt.setInt(3, authenticatorId);
                                            storeStepIDPAuthnPrepStmt.addBatch();

                                            if (log.isDebugEnabled()) {
                                                log.debug("Updating Federated IdP of Application "
                                                        + applicationId + " Step Order: "
                                                        + authStep.getStepOrder() + " IdP: "
                                                        + idpName + " Authenticator: "
                                                        + authenticator);
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                }

                storeStepIDPAuthnPrepStmt.executeBatch();
            } finally {
                IdentityApplicationManagementUtil.closeStatement(storeStepIDPAuthnPrepStmt);
            }
        }
    }

    /**
     * @param applicationId
     * @param claimConfiguration
     * @param applicationID
     * @param connection
     * @throws SQLException
     */
    private void updateClaimConfiguration(int applicationId, ClaimConfig claimConfiguration,
                                          int applicationID, Connection connection) throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement storeRoleClaimPrepStmt = null;
        PreparedStatement storeSPDialectsPrepStmt = null;
        PreparedStatement storeClaimDialectPrepStmt = null;
        PreparedStatement storeSendLocalSubIdPrepStmt = null;

        if (claimConfiguration == null) {
            return;
        }

        try {
            // update the application data
            String roleClaim = claimConfiguration.getRoleClaimURI();
            if (roleClaim != null) {
                storeRoleClaimPrepStmt = connection
                        .prepareStatement(UPDATE_BASIC_APPINFO_WITH_ROLE_CLAIM);
                // ROLE_CLAIM=? WHERE TENANT_ID= ? AND ID =
                storeRoleClaimPrepStmt.setString(1, roleClaim);
                storeRoleClaimPrepStmt.setInt(2, tenantID);
                storeRoleClaimPrepStmt.setInt(3, applicationId);
                storeRoleClaimPrepStmt.executeUpdate();
            }

        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeRoleClaimPrepStmt);
        }

        try {
            // update the application data with SP dialects
            String[] spClaimDialects = claimConfiguration.getSpClaimDialects();

            if (ArrayUtils.isNotEmpty(spClaimDialects)) {
                storeSPDialectsPrepStmt = connection
                        .prepareStatement(STORE_SP_DIALECTS_BY_APP_ID);

                for (String spClaimDialect : spClaimDialects) {
                    if (spClaimDialect != null && !spClaimDialect.isEmpty()) {
                        storeSPDialectsPrepStmt.setInt(1, tenantID);
                        storeSPDialectsPrepStmt.setString(2, spClaimDialect);
                        storeSPDialectsPrepStmt.setInt(3, applicationId);
                        storeSPDialectsPrepStmt.addBatch();

                        if (log.isDebugEnabled()) {
                            log.debug("Storing SP Dialect: " + spClaimDialect);
                        }
                    }
                }
                storeSPDialectsPrepStmt.executeBatch();
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSPDialectsPrepStmt);
        }

        try {
            storeClaimDialectPrepStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_CLAIM_DIALEECT);
            // IS_LOCAL_CLAIM_DIALECT=? WHERE TENANT_ID= ? AND ID = ?
            storeClaimDialectPrepStmt.setString(1, claimConfiguration.isLocalClaimDialect() ? "1"
                    : "0");
            storeClaimDialectPrepStmt.setInt(2, tenantID);
            storeClaimDialectPrepStmt.setInt(3, applicationId);
            storeClaimDialectPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeClaimDialectPrepStmt);
        }

        try {
            storeSendLocalSubIdPrepStmt = connection
                    .prepareStatement(UPDATE_BASIC_APPINFO_WITH_SEND_LOCAL_SUB_ID);
            // IS_SEND_LOCAL_SUBJECT_ID=? WHERE TENANT_ID= ? AND ID = ?
            storeSendLocalSubIdPrepStmt.setString(1,
                    claimConfiguration.isAlwaysSendMappedLocalSubjectId() ? "1" : "0");
            storeSendLocalSubIdPrepStmt.setInt(2, tenantID);
            storeSendLocalSubIdPrepStmt.setInt(3, applicationId);
            storeSendLocalSubIdPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeSendLocalSubIdPrepStmt);
        }

        if (claimConfiguration.getClaimMappings() == null
                || claimConfiguration.getClaimMappings().length == 0) {
            return;
        }

        List<ClaimMapping> claimMappings = Arrays.asList(claimConfiguration.getClaimMappings());

        if (claimMappings.isEmpty()) {
            log.debug("No claim mapping found, Skipping ..");
            return;
        }

        PreparedStatement storeClaimMapPrepStmt = null;
        try {
            storeClaimMapPrepStmt = connection
                    .prepareStatement(STORE_CLAIM_MAPPING);

            for (ClaimMapping mapping : claimMappings) {
                if (mapping.getLocalClaim() == null
                        || mapping.getLocalClaim().getClaimUri() == null
                        || mapping.getRemoteClaim().getClaimUri() == null
                        || mapping.getRemoteClaim() == null) {
                    continue;
                }
                // TENANT_ID, IDP_CLAIM, SP_CLAIM, APP_ID, IS_REQUESTED
                storeClaimMapPrepStmt.setInt(1, tenantID);
                storeClaimMapPrepStmt.setString(2, mapping.getLocalClaim().getClaimUri());
                storeClaimMapPrepStmt.setString(3, mapping.getRemoteClaim().getClaimUri());
                storeClaimMapPrepStmt.setInt(4, applicationID);
                if (mapping.isRequested()) {
                    storeClaimMapPrepStmt.setString(5, "1");
                } else {
                    storeClaimMapPrepStmt.setString(5, "0");
                }
                if (mapping.isMandatory()) {
                    storeClaimMapPrepStmt.setString(6, "1");
                } else {
                    storeClaimMapPrepStmt.setString(6, "0");
                }
                storeClaimMapPrepStmt.setString(7, mapping.getDefaultValue());
                storeClaimMapPrepStmt.addBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Storing Claim Mapping. Local Claim: "
                            + mapping.getLocalClaim().getClaimUri() + " SPClaim: "
                            + mapping.getRemoteClaim().getClaimUri());
                }
            }

            storeClaimMapPrepStmt.executeBatch();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeClaimMapPrepStmt);
        }
    }

    /**
     * @param applicationID
     * @param permissionsAndRoleConfiguration
     * @param connection
     * @throws SQLException
     */
    private void updatePermissionAndRoleConfiguration(int applicationID,
                                                      PermissionsAndRoleConfig permissionsAndRoleConfiguration,
                                                      Connection connection)
            throws SQLException {

        if (permissionsAndRoleConfiguration == null || permissionsAndRoleConfiguration.getRoleMappings() == null ||
                ArrayUtils.isEmpty(permissionsAndRoleConfiguration.getRoleMappings())) {
            return;
        }

        RoleMapping[] roleMappings = permissionsAndRoleConfiguration.getRoleMappings();
        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement storeRoleMapPrepStmt = null;
        try {
            storeRoleMapPrepStmt = connection
                    .prepareStatement(STORE_ROLE_MAPPING);
            for (RoleMapping roleMapping : roleMappings) {
                // TENANT_ID, IDP_ROLE, SP_ROLE, APP_ID
                storeRoleMapPrepStmt.setInt(1, tenantID);
                storeRoleMapPrepStmt.setString(2, roleMapping.getLocalRole().getLocalRoleName());
                storeRoleMapPrepStmt.setString(3, roleMapping.getRemoteRole());
                storeRoleMapPrepStmt.setInt(4, applicationID);
                storeRoleMapPrepStmt.addBatch();

                if (log.isDebugEnabled()) {
                    log.debug("Storing Claim Mapping. IDPRole: " + roleMapping.getLocalRole()
                            + " SPRole: " + roleMapping.getRemoteRole());
                }
            }

            storeRoleMapPrepStmt.executeBatch();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeRoleMapPrepStmt);
        }
    }

    @Override
    public ServiceProvider getApplication(String applicationName,
                                          String tenantDomain) throws IdentityApplicationManagementException {

        int applicationId = getApplicationIdByName(applicationName, tenantDomain);
        if (isApplicationNotFound(applicationId) && LOCAL_SP.equals(applicationName)) {
            // Looking for the resident sp. Create the resident sp for the tenant.
            if (log.isDebugEnabled()) {
                log.debug("The application: " + applicationName + " trying to retrieve is not available, which is" +
                        " identified as the Local Service Provider. Therefore, creating the application: "
                        + applicationName);
            }
            ServiceProvider localServiceProvider = new ServiceProvider();
            localServiceProvider.setApplicationName(applicationName);
            localServiceProvider.setDescription("Local Service Provider");
            applicationId = createServiceProvider(tenantDomain, localServiceProvider);
        }
        return getApplication(applicationId);
    }

    /**
     * Determines whether the application is available based on the internal application id.
     *
     * @param applicationId internal application id.
     * @return
     */
    private boolean isApplicationNotFound(int applicationId) {

        // A valid application should have an id > 0 since its an auto increment id.
        return applicationId <= 0;
    }

    private ConsentPurposeConfigs getConsentPurposeConfigs(Connection connection, int applicationId, int tenantId)
            throws
            IdentityApplicationManagementException {

        ConsentPurposeConfigs consentPurposeConfigs = new ConsentPurposeConfigs();
        List<ConsentPurpose> consentPurposes = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(LOAD_SP_CONSENT_PURPOSES)) {
            ps.setInt(1, applicationId);
            ps.setInt(2, tenantId);
            try (ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    ConsentPurpose consentPurpose = new ConsentPurpose();
                    consentPurpose.setPurposeId(resultSet.getInt(2));
                    consentPurpose.setDisplayOrder(resultSet.getInt(3));
                    consentPurposes.add(consentPurpose);
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving consent purpose configurations " +
                    "for application ID: " + applicationId, e);
        }
        consentPurposeConfigs.setConsentPurpose(consentPurposes.toArray(new ConsentPurpose[0]));
        return consentPurposeConfigs;
    }

    /**
     * Retrieves the certificate content from the database using the certificate reference id property of a
     * service provider.
     *
     * @param serviceProviderProperties
     * @param connection
     * @return
     * @throws CertificateRetrievingException
     */
    private String getCertificateContent(List<ServiceProviderProperty> serviceProviderProperties, Connection connection)
            throws CertificateRetrievingException {

        String certificateReferenceId = null;
        for (ServiceProviderProperty property : serviceProviderProperties) {
            if ("CERTIFICATE".equals(property.getName())) {
                certificateReferenceId = property.getValue();
            }
        }

        if (certificateReferenceId != null) {

            PreparedStatement statementForFetchingCertificate = null;
            ResultSet results = null;
            try {
                statementForFetchingCertificate = connection.prepareStatement(
                        GET_CERTIFICATE_BY_ID);
                statementForFetchingCertificate.setInt(1, Integer.parseInt(certificateReferenceId));

                results = statementForFetchingCertificate.executeQuery();

                String certificateContent = null;
                while (results.next()) {
                    certificateContent = getBlobValue(results.getBinaryStream("CERTIFICATE_IN_PEM"));
                }

                if (certificateContent != null) {
                    return certificateContent;
                }
            } catch (SQLException | IOException e) {
                String errorMessage = "An error occurred while retrieving the certificate for the " +
                        "application.";
                log.error(errorMessage);
                throw new CertificateRetrievingException(errorMessage, e);
            } finally {
                IdentityApplicationManagementUtil.closeResultSet(results);
                IdentityApplicationManagementUtil.closeStatement(statementForFetchingCertificate);
            }
        }
        return null;
    }

    /**
     * @param applicationName
     * @param connection
     * @return
     * @throws SQLException
     */
    private ServiceProvider getBasicApplicationData(String applicationName, Connection connection, int tenantID)
            throws SQLException, IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;

        if (log.isDebugEnabled()) {
            log.debug("Loading Basic Application Data of " + applicationName);
        }

        PreparedStatement loadBasicAppInfoStmt = null;
        ResultSet basicAppDataResultSet = null;
        try {
            loadBasicAppInfoStmt = connection.prepareStatement(LOAD_BASIC_APP_INFO_BY_APP_NAME);
            // SELECT * FROM IDN_APPMGT_APP WHERE APP_NAME = ? AND TENANT_ID = ?
            loadBasicAppInfoStmt.setString(1, applicationName);
            loadBasicAppInfoStmt.setInt(2, tenantID);
            basicAppDataResultSet = loadBasicAppInfoStmt.executeQuery();
            // ID, TENANT_ID, APP_NAME, USER_STORE, USERNAME, DESCRIPTION, ROLE_CLAIM, AUTH_TYPE,
            // PROVISIONING_USERSTORE_DOMAIN, IS_LOCAL_CLAIM_DIALECT, IS_SEND_LOCAL_SUBJECT_ID,
            // IS_SEND_AUTH_LIST_OF_IDPS, SUBJECT_CLAIM_URI, IS_SAAS_APP

            if (basicAppDataResultSet.next()) {
                serviceProvider = new ServiceProvider();
                serviceProvider.setApplicationID(basicAppDataResultSet.getInt(1));
                serviceProvider.setApplicationResourceId(basicAppDataResultSet.getString(ApplicationTableColumns.UUID));
                serviceProvider.setApplicationName(basicAppDataResultSet.getString(3));
                serviceProvider.setDescription(basicAppDataResultSet.getString(6));
                serviceProvider.setImageUrl(basicAppDataResultSet.getString(ApplicationTableColumns.IMAGE_URL));
                serviceProvider.setAccessUrl(basicAppDataResultSet.getString(ApplicationTableColumns.ACCESS_URL));
                serviceProvider.setDiscoverable(getBooleanValue(basicAppDataResultSet.getString(ApplicationTableColumns
                        .IS_DISCOVERABLE)));

                String tenantDomain;
                try {
                    tenantDomain = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                            .getTenantManager()
                            .getDomain(
                                    basicAppDataResultSet.getInt(2));
                } catch (UserStoreException e) {
                    log.error("Error while reading tenantDomain", e);
                    throw new IdentityApplicationManagementException("Error while reading tenant " +
                            "domain for application " +
                            applicationName);
                }

                User owner = new User();
                owner.setUserName(basicAppDataResultSet.getString(5));
                owner.setTenantDomain(tenantDomain);
                owner.setUserStoreDomain(basicAppDataResultSet.getString(4));
                serviceProvider.setOwner(owner);

                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setRoleClaimURI(basicAppDataResultSet.getString(7));
                claimConfig.setLocalClaimDialect("1".equals(basicAppDataResultSet.getString(10)));
                claimConfig.setAlwaysSendMappedLocalSubjectId("1".equals(basicAppDataResultSet
                        .getString(11)));
                serviceProvider.setClaimConfig(claimConfig);

                LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                        new LocalAndOutboundAuthenticationConfig();
                localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs("1"
                        .equals(basicAppDataResultSet.getString(14)));
                localAndOutboundAuthenticationConfig.setEnableAuthorization("1".equals(basicAppDataResultSet
                        .getString(15)));
                localAndOutboundAuthenticationConfig.setSubjectClaimUri(basicAppDataResultSet
                        .getString(16));
                serviceProvider
                        .setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

                serviceProvider.setSaasApp("1".equals(basicAppDataResultSet.getString(17)));

                // Will be supported with 'Advance Consent Management Feature'.
                /*
                ConsentConfig consentConfig = new ConsentConfig();
                consentConfig.setEnabled("1".equals(basicAppDataResultSet.getString(18)));
                serviceProvider.setConsentConfig(consentConfig);
                */
                if (log.isDebugEnabled()) {
                    log.debug("ApplicationID: " + serviceProvider.getApplicationID()
                            + " ApplicationName: " + serviceProvider.getApplicationName()
                            + " UserName: " + serviceProvider.getOwner().getUserName()
                            + " TenantDomain: " + serviceProvider.getOwner().getTenantDomain());
                }
            }

            return serviceProvider;
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(basicAppDataResultSet);
            IdentityApplicationManagementUtil.closeStatement(loadBasicAppInfoStmt);
        }
    }

    @Override
    public ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(int pageNumber, String filter)
            throws IdentityApplicationManagementException {

        validateRequestedPageNumber(pageNumber);

        int limit = ApplicationMgtUtil.getItemsPerPage();
        int offset = (pageNumber - 1) * limit;

        return getApplicationBasicInfo(filter, offset, limit);
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter, int offset, int limit)
            throws IdentityApplicationManagementException {

        validateAttributesForPagination(offset, limit);

        if ("*".equals(filter)) {
            return getApplicationBasicInfo(offset, limit);
        }

        validateAttributesForPagination(offset, limit);

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;
        String sqlQuery;
        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<>();

        try {

            String filterResolvedForSQL = resolveSQLFilter(filter);

            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.contains("MySQL")
                    || databaseProductName.contains("MariaDB")
                    || databaseProductName.contains("H2")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MYSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateApplicationSearchQuery(getAppNamesStmt, tenantID, filterResolvedForSQL, offset, limit);
            } else if (databaseProductName.contains("Oracle")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_ORACLE;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateApplicationSearchQuery(getAppNamesStmt, tenantID, filterResolvedForSQL, offset + limit,
                        offset);
            } else if (databaseProductName.contains("Microsoft")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_MSSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateApplicationSearchQuery(getAppNamesStmt, tenantID, filterResolvedForSQL, offset, limit);
            } else if (databaseProductName.contains("PostgreSQL")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_POSTGRESQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateApplicationSearchQuery(getAppNamesStmt, tenantID, filterResolvedForSQL, limit, offset);
            } else if (databaseProductName.contains("DB2")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_DB2SQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateApplicationSearchQuery(getAppNamesStmt, tenantID, filterResolvedForSQL, offset + 1,
                        offset + limit);
            } else if (databaseProductName.contains("INFORMIX")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME_INFORMIX;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, offset);
                getAppNamesStmt.setInt(2, limit);
                getAppNamesStmt.setInt(3, tenantID);
                getAppNamesStmt.setString(4, filterResolvedForSQL);
                getAppNamesStmt.setString(5, LOCAL_SP);

            } else {
                log.error("Error while loading applications from DB: Database driver could not be identified or " +
                        "not supported.");
                throw new IdentityApplicationManagementException("Error while loading applications from DB:" +
                        "Database driver could not be identified or not supported.");
            }

            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                appInfo.add(buildApplicationBasicInfo(appNameResultSet));
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                    e.getMessage(), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ApplicationBasicInfo[0]);
    }

    /**
     * Set values to the prepare statement for searching applications
     * @param getAppNamesStmt
     * @param tenantID
     * @param filterResolvedForSQL
     * @param start
     * @param end
     * @throws SQLException
     */
    private void populateApplicationSearchQuery(PreparedStatement getAppNamesStmt, int tenantID, String
            filterResolvedForSQL, int start, int end) throws SQLException {
        getAppNamesStmt.setInt(1, tenantID);
        getAppNamesStmt.setString(2, filterResolvedForSQL);
        getAppNamesStmt.setString(3, LOCAL_SP);
        getAppNamesStmt.setInt(4, start);
        getAppNamesStmt.setInt(5, end);
    }

    @Override
    public ServiceProvider getApplication(int applicationId) throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {

            // Load basic application data
            ServiceProvider serviceProvider = getBasicApplicationData(applicationId, connection);
            if (serviceProvider == null) {
                return null;
            }
            int tenantID = IdentityTenantUtil.getTenantId(serviceProvider.getOwner().getTenantDomain());
            List<ServiceProviderProperty> propertyList = getServicePropertiesBySpId(connection, applicationId);

            serviceProvider.setJwksUri(getJwksUri(propertyList));
            serviceProvider.setTemplateId(getTemplateId(propertyList));
            serviceProvider.setManagementApp(getIsManagementApp(propertyList));
            serviceProvider.setInboundAuthenticationConfig(getInboundAuthenticationConfig(
                    applicationId, connection, tenantID));
            serviceProvider
                    .setLocalAndOutBoundAuthenticationConfig(getLocalAndOutboundAuthenticationConfig(
                            applicationId, connection, tenantID, propertyList));

            serviceProvider.setInboundProvisioningConfig(getInboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            serviceProvider.setOutboundProvisioningConfig(getOutboundProvisioningConfiguration(
                    applicationId, connection, tenantID));

            // Load Claim Mapping
            serviceProvider.setClaimConfig(getClaimConfiguration(applicationId, connection,
                    tenantID));

            // Load Role Mappings
            List<RoleMapping> roleMappings = getRoleMappingOfApplication(applicationId, connection,
                    tenantID);
            PermissionsAndRoleConfig permissionAndRoleConfig = new PermissionsAndRoleConfig();
            permissionAndRoleConfig.setRoleMappings(roleMappings.toArray(new RoleMapping[0]));
            serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);

            RequestPathAuthenticatorConfig[] requestPathAuthenticators = getRequestPathAuthenticators(
                    applicationId, connection, tenantID);
            serviceProvider.setRequestPathAuthenticatorConfigs(requestPathAuthenticators);

            serviceProvider.setSpProperties(propertyList.toArray(new ServiceProviderProperty[0]));
            serviceProvider.setCertificateContent(getCertificateContent(propertyList, connection));

            // Will be supported with 'Advance Consent Management Feature'.
            /*
            ConsentConfig consentConfig = serviceProvider.getConsentConfig();
            if (isNull(consentConfig)) {
                consentConfig = new ConsentConfig();
            }
            consentConfig.setConsentPurposeConfigs(getConsentPurposeConfigs(connection, applicationId, tenantID));
            serviceProvider.setConsentConfig(consentConfig);
            */

            String serviceProviderName = serviceProvider.getApplicationName();
            loadApplicationPermissions(serviceProviderName, serviceProvider);
            return serviceProvider;
        } catch (SQLException | CertificateRetrievingException e) {
            throw new IdentityApplicationManagementException("Failed to get service provider with id: " + applicationId,
                    e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    @Override
    public ServiceProvider getApplicationWithRequiredAttributes(int applicationId, List<String> requiredAttributes)
            throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            // Load basic application data
            ServiceProvider serviceProvider = getBasicApplicationData(applicationId, connection);
            if (CollectionUtils.isNotEmpty(requiredAttributes)) {
                List<ServiceProviderProperty> propertyList = getServicePropertiesBySpId(connection, applicationId);
                for (String requiredAttribute : requiredAttributes) {
                    if (ADVANCED_CONFIG.equals(requiredAttribute)) {
                        readAndSetConfigurationsFromProperties(propertyList,
                                serviceProvider.getLocalAndOutBoundAuthenticationConfig());
                        serviceProvider.setSpProperties(propertyList.toArray(new ServiceProviderProperty[0]));
                        serviceProvider.setCertificateContent(getCertificateContent(propertyList, connection));
                    }
                    if (TEMPLATE_ID_SP_PROPERTY_NAME.equals(requiredAttribute)) {
                        serviceProvider.setTemplateId(getTemplateId(propertyList));
                    }
                }
            }
            return serviceProvider;
        } catch (SQLException | CertificateRetrievingException e) {
            throw new IdentityApplicationManagementException("Failed to gather required attributes for application " +
                        "with id: " + applicationId, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    private boolean getIsManagementApp(List<ServiceProviderProperty> propertyList) {

        String value = propertyList.stream()
                .filter(property -> IS_MANAGEMENT_APP_SP_PROPERTY_NAME.equals(property.getName()))
                .findFirst()
                .map(ServiceProviderProperty::getValue)
                .orElse(StringUtils.EMPTY);
        if (StringUtils.EMPTY.equals(value)) {
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    private String getTemplateId(List<ServiceProviderProperty> propertyList) {

        return propertyList.stream()
                .filter(property -> TEMPLATE_ID_SP_PROPERTY_NAME.equals(property.getName()))
                .findFirst()
                .map(ServiceProviderProperty::getValue)
                .orElse(StringUtils.EMPTY);
    }

    private String getJwksUri(List<ServiceProviderProperty> propertyList) {

        return propertyList.stream()
                .filter(property -> JWKS_URI_SP_PROPERTY_NAME.equals(property.getName()))
                .findFirst()
                .map(ServiceProviderProperty::getValue)
                .orElse(StringUtils.EMPTY);
    }

    /**
     * @param appId
     * @param connection
     * @return
     * @throws SQLException
     */
    private ServiceProvider getBasicApplicationData(int appId, Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;

        if (log.isDebugEnabled()) {
            log.debug("Loading Basic Application Data of application ID: " + appId);
        }

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = connection.prepareStatement(LOAD_BASIC_APP_INFO_BY_APP_ID);
            prepStmt.setInt(1, appId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                serviceProvider = new ServiceProvider();
                serviceProvider.setApplicationID(rs.getInt(ApplicationTableColumns.ID));
                serviceProvider.setApplicationResourceId(rs.getString(ApplicationTableColumns.UUID));
                serviceProvider.setApplicationName(rs.getString(ApplicationTableColumns.APP_NAME));
                serviceProvider.setDescription(rs.getString(ApplicationTableColumns.DESCRIPTION));
                serviceProvider.setImageUrl(rs.getString(ApplicationTableColumns.IMAGE_URL));
                serviceProvider.setAccessUrl(rs.getString(ApplicationTableColumns.ACCESS_URL));
                serviceProvider.setDiscoverable(getBooleanValue(rs.getString(ApplicationTableColumns.IS_DISCOVERABLE)));

                User owner = new User();
                owner.setUserName(rs.getString(ApplicationTableColumns.USERNAME));
                owner.setUserStoreDomain(rs.getString(ApplicationTableColumns.USER_STORE));
                owner.setTenantDomain(IdentityTenantUtil.getTenantDomain(rs.getInt(ApplicationTableColumns.TENANT_ID)));
                serviceProvider.setOwner(owner);

                ClaimConfig claimConfig = new ClaimConfig();
                claimConfig.setRoleClaimURI(rs.getString(ApplicationTableColumns.ROLE_CLAIM));
                claimConfig.setLocalClaimDialect(
                        getBooleanValue(rs.getString(ApplicationTableColumns.IS_LOCAL_CLAIM_DIALECT)));
                claimConfig.setAlwaysSendMappedLocalSubjectId(
                        getBooleanValue(rs.getString(ApplicationTableColumns.IS_SEND_LOCAL_SUBJECT_ID)));
                serviceProvider.setClaimConfig(claimConfig);

                LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig =
                        new LocalAndOutboundAuthenticationConfig();
                localAndOutboundAuthenticationConfig.setAlwaysSendBackAuthenticatedListOfIdPs(
                        getBooleanValue(rs.getString(ApplicationTableColumns.IS_SEND_AUTH_LIST_OF_IDPS)));
                localAndOutboundAuthenticationConfig.setEnableAuthorization(
                        getBooleanValue(rs.getString(ApplicationTableColumns.ENABLE_AUTHORIZATION)));
                localAndOutboundAuthenticationConfig.setSubjectClaimUri(
                        rs.getString(ApplicationTableColumns.SUBJECT_CLAIM_URI));
                serviceProvider.setLocalAndOutBoundAuthenticationConfig(localAndOutboundAuthenticationConfig);

                serviceProvider.setSaasApp(getBooleanValue(rs.getString(ApplicationTableColumns.IS_SAAS_APP)));

                // Will be supported with 'Advance Consent Management Feature'.
                /*
                ConsentConfig consentConfig = new ConsentConfig();
                consentConfig.setEnabled("1".equals(rs.getString(18)));
                serviceProvider.setConsentConfig(consentConfig);
                */

                if (log.isDebugEnabled()) {
                    log.debug("ApplicationID: " + serviceProvider.getApplicationID()
                            + " ApplicationName: " + serviceProvider.getApplicationName()
                            + " UserName: " + serviceProvider.getOwner().getUserName()
                            + " TenantDomain: " + serviceProvider.getOwner().getTenantDomain());
                }
            }

            return serviceProvider;
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(rs);
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
    }

    private boolean getBooleanValue(String booleanValueAsString) throws SQLException {

        return "1".equals(booleanValueAsString);
    }

    private String getStringValueForBoolean(boolean booleanValue) throws SQLException {

        return booleanValue ? "1" : "0";
    }

    /**
     * @param applicationid
     * @param connection
     * @return
     * @throws SQLException
     */
    private String getAuthenticationType(int applicationid, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement authTypeStmt = null;
        ResultSet authTypeResultSet = null;
        try {
            authTypeStmt = connection
                    .prepareStatement(LOAD_AUTH_TYPE_BY_APP_ID);
            authTypeStmt.setInt(1, applicationid);
            authTypeStmt.setInt(2, tenantID);
            authTypeResultSet = authTypeStmt.executeQuery();

            if (authTypeResultSet.next()) {
                return authTypeResultSet.getString(1);
            }

            return ApplicationConstants.AUTH_TYPE_DEFAULT;

        } finally {
            IdentityApplicationManagementUtil.closeResultSet(authTypeResultSet);
            IdentityApplicationManagementUtil.closeStatement(authTypeStmt);
        }

    }

    /**
     * This method will be heavily used by the Authentication Framework. The framework would ask for
     * application data with the given client key and secrete
     *
     * @param clientId
     * @param type
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    public ServiceProvider getApplicationData(String clientId, String type, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Loading Application Data of Client " + clientId);
        }

        int tenantID = -123;

        try {
            tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e1) {
            log.error("Error while reading application", e1);
            throw new IdentityApplicationManagementException("Error while reading application", e1);
        }

        String applicationName = null;

        // Reading application name from the database
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement storeAppPrepStmt = null;
        ResultSet appNameResult = null;
        try {
            storeAppPrepStmt = connection
                    .prepareStatement(LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE);
            storeAppPrepStmt.setString(1, clientId);
            storeAppPrepStmt.setString(2, type);
            storeAppPrepStmt.setInt(3, tenantID);
            appNameResult = storeAppPrepStmt.executeQuery();
            if (appNameResult.next()) {
                applicationName = appNameResult.getString(1);
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while reading application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appNameResult);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return getApplication(applicationName, tenantDomain);
    }

    /**
     * @param applicationID
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public String getApplicationName(int applicationID)
            throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            return getApplicationName(applicationID, connection);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Failed loading the application with "
                    + applicationID, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    /**
     * Returns the stored application name for a given application id.
     *
     * @param applicationID
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private String getApplicationName(int applicationID, Connection connection) throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantID);
            log.debug("Loading application name for id: " + applicationID + " in tenantDomain: " + tenantDomain);
        }

        String applicationName = null;
        try (PreparedStatement loadBasicAppInfoStmt = connection.prepareStatement(LOAD_APP_NAME_BY_APP_ID)) {

            loadBasicAppInfoStmt.setInt(1, applicationID);
            loadBasicAppInfoStmt.setInt(2, tenantID);

            try (ResultSet appNameResultSet = loadBasicAppInfoStmt.executeQuery()) {
                if (appNameResultSet.next()) {
                    applicationName = appNameResultSet.getString(1);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Application name for id: " + applicationID + " is '" + applicationName + "'");
            }
            return applicationName;
        }
    }

    private int getApplicationIdByName(String applicationName,
                                       String tenantDomain) throws IdentityApplicationManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            return getApplicationIDByName(applicationName, tenantId, connection);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error retrieving id for application: "
                    + applicationName + " in tenantDomain: " + tenantDomain);
        }
    }

    /**
     * Returns the application ID for a given application name
     *
     * @param applicationName
     * @param tenantID
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private int getApplicationIDByName(String applicationName, int tenantID,
                                       Connection connection) throws SQLException {

        int applicationId = 0;
        PreparedStatement getAppIDPrepStmt = null;
        ResultSet appidResult = null;

        try {
            getAppIDPrepStmt = connection.prepareStatement(LOAD_APP_ID_BY_APP_NAME);
            getAppIDPrepStmt.setString(1, applicationName);
            getAppIDPrepStmt.setInt(2, tenantID);
            appidResult = getAppIDPrepStmt.executeQuery();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

            if (appidResult.next()) {
                applicationId = appidResult.getInt(1);
            }

        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appidResult);
            IdentityApplicationManagementUtil.closeStatement(getAppIDPrepStmt);
        }

        return applicationId;
    }

    /**
     * Reading the mapping of properties.
     *
     * @param customAuthenticator
     * @param propertyName
     * @return
     */
    private Property getMappedProperty(AbstractInboundAuthenticatorConfig customAuthenticator, String propertyName) {

        Property property = null;
        if (customAuthenticator != null) {
            Property[] confProps = customAuthenticator.getConfigurationProperties();
            if (confProps != null) {
                for (Property confProp : confProps) {
                    if (propertyName != null && propertyName.equals(confProp.getName())) {
                        property = confProp;
                    }
                }
            }
        }
        return property;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws SQLException
     */
    private InboundAuthenticationConfig getInboundAuthenticationConfig(int applicationId, Connection connection, int
            tenantID) throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Reading Clients of Application " + applicationId);
        }
        Map<String, InboundAuthenticationRequestConfig> inboundAuthenticationRequestConfigMap =
                new HashMap<String, InboundAuthenticationRequestConfig>();

        PreparedStatement getClientInfo = null;
        ResultSet resultSet = null;

        try {
            getClientInfo = connection
                    .prepareStatement(LOAD_CLIENTS_INFO_BY_APP_ID);

            getClientInfo.setInt(1, applicationId);
            getClientInfo.setInt(2, tenantID);
            resultSet = getClientInfo.executeQuery();

            while (resultSet.next()) {
                String authKey = resultSet.getString(1);
                //this is done to handle empty string added to oracle database as null.
                if (authKey == null) {
                    authKey = new String();
                }
                String authType = resultSet.getString(2);
                String propName = resultSet.getString(3);
                String propValue = resultSet.getString(4);
                String configType = resultSet.getString(5);

                String mapKey = authType + ":" + authKey;

                InboundAuthenticationRequestConfig inboundAuthRequest = null;
                if ((inboundAuthRequest = inboundAuthenticationRequestConfigMap.get(mapKey)) == null) {
                    inboundAuthRequest = new InboundAuthenticationRequestConfig();
                }
                inboundAuthRequest.setInboundAuthKey(authKey);
                inboundAuthRequest.setInboundAuthType(authType);
                inboundAuthRequest.setInboundConfigType(configType);

                boolean isCustomAuthenticator = isCustomInboundAuthType(authType);
                AbstractInboundAuthenticatorConfig customAuthenticator = ApplicationManagementServiceComponentHolder
                        .getInboundAuthenticatorConfig(authType + ":" + configType);
                if (isCustomAuthenticator && customAuthenticator != null) {
                    inboundAuthRequest.setFriendlyName(customAuthenticator.getFriendlyName());
                }
                if (propName != null) {
                    Property prop = new Property();
                    prop.setName(propName);
                    prop.setValue(propValue);
                    if (isCustomAuthenticator && customAuthenticator != null) {
                        Property mappedProperty = getMappedProperty(customAuthenticator, propName);
                        if (mappedProperty != null) {
                            prop.setDisplayName(mappedProperty.getDisplayName());
                        }
                    }
                    inboundAuthRequest.setProperties((ApplicationMgtUtil.concatArrays(new Property[]{prop},
                            inboundAuthRequest.getProperties())));
                }
                inboundAuthenticationRequestConfigMap.put(mapKey, inboundAuthRequest);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClientInfo);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }
        Map<String, AbstractInboundAuthenticatorConfig> allCustomAuthenticators = new HashMap<>
                (ApplicationManagementServiceComponentHolder.getAllInboundAuthenticatorConfig());
        for (Map.Entry<String, InboundAuthenticationRequestConfig> entry : inboundAuthenticationRequestConfigMap
                .entrySet()) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig = entry.getValue();
            AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig = allCustomAuthenticators.remove
                    (inboundAuthenticationRequestConfig.getInboundAuthType() + ":" +
                            inboundAuthenticationRequestConfig.getInboundConfigType());
            if (inboundAuthenticatorConfig != null && inboundAuthenticationRequestConfig != null) {
                Property[] sources = inboundAuthenticatorConfig.getConfigurationProperties();
                Property[] destinations = inboundAuthenticationRequestConfig.getProperties();
                Map<String, Property> destinationMap = new HashMap<>();
                for (Property destination : destinations) {
                    destinationMap.put(destination.getName(), destination);
                }
                for (Property source : sources) {
                    Property property = destinationMap.get(source.getName());
                    if (property == null) {
                        if (isCustomInboundAuthType(inboundAuthenticationRequestConfig.getInboundAuthType())) {
                            if (inboundAuthenticatorConfig.isRelyingPartyKeyConfigured()) {
                                if (StringUtils.equals(inboundAuthenticatorConfig.getRelyingPartyKey(), source
                                        .getName())) {
                                    source.setValue(inboundAuthenticationRequestConfig.getInboundAuthKey());
                                }
                            }
                        }
                        destinationMap.put(source.getName(), source);
                    } else {
                        property.setConfidential(source.isConfidential());
                        property.setDefaultValue(source.getDefaultValue());
                        property.setAdvanced(source.isAdvanced());
                        property.setDescription(source.getDescription());
                        property.setDisplayOrder(source.getDisplayOrder());
                        property.setRequired(source.isRequired());
                        property.setType(source.getType());
                    }
                }

                inboundAuthenticationRequestConfig
                        .setProperties(destinationMap.values().toArray(new Property[destinationMap.size()]));
            }
        }
        List<InboundAuthenticationRequestConfig> returnList = new ArrayList<>(inboundAuthenticationRequestConfigMap
                .values());
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(returnList.toArray(new
                InboundAuthenticationRequestConfig[returnList.size()]));
        return inboundAuthenticationConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @param propertyList
     * @return
     * @throws SQLException
     */
    private LocalAndOutboundAuthenticationConfig getLocalAndOutboundAuthenticationConfig(
            int applicationId, Connection connection, int tenantId, List<ServiceProviderProperty> propertyList)
            throws SQLException, IdentityApplicationManagementException {

        PreparedStatement getStepInfoPrepStmt = null;
        ResultSet stepInfoResultSet = null;

        if (log.isDebugEnabled()) {
            log.debug("Reading Steps of Application " + applicationId);
        }

        try {
            getStepInfoPrepStmt = connection
                    .prepareStatement(LOAD_STEPS_INFO_BY_APP_ID);
            // STEP_ORDER, AUTHENTICATOR_ID, IS_SUBJECT_STEP, IS_ATTRIBUTE_STEP
            getStepInfoPrepStmt.setInt(1, applicationId);
            stepInfoResultSet = getStepInfoPrepStmt.executeQuery();

            Map<String, AuthenticationStep> authSteps = new HashMap<>();
            Map<String, Map<String, List<FederatedAuthenticatorConfig>>> stepFedIdPAuthenticators = new HashMap<>();
            Map<String, List<LocalAuthenticatorConfig>> stepLocalAuth = new HashMap<>();

            while (stepInfoResultSet.next()) {

                String step = String.valueOf(stepInfoResultSet.getInt(1));
                AuthenticationStep authStep;

                if (authSteps.containsKey(step)) {
                    authStep = authSteps.get(step);
                } else {
                    authStep = new AuthenticationStep();
                    authStep.setStepOrder(stepInfoResultSet.getInt(1));
                    stepLocalAuth.put(step, new ArrayList<LocalAuthenticatorConfig>());
                    stepFedIdPAuthenticators.put(step,
                            new HashMap<String, List<FederatedAuthenticatorConfig>>());
                }

                int authenticatorId = stepInfoResultSet.getInt(2);
                Map<String, String> authenticatorInfo = getAuthenticatorInfo(connection, tenantId,
                        authenticatorId);

                if (authenticatorInfo != null
                        && authenticatorInfo.get(ApplicationConstants.IDP_NAME) != null
                        && ApplicationConstants.LOCAL_IDP_NAME.equals(authenticatorInfo
                        .get("idpName"))) {
                    LocalAuthenticatorConfig localAuthenticator = new LocalAuthenticatorConfig();
                    localAuthenticator.setName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_NAME));
                    localAuthenticator.setDisplayName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME));
                    stepLocalAuth.get(step).add(localAuthenticator);
                } else {
                    Map<String, List<FederatedAuthenticatorConfig>> stepFedIdps = stepFedIdPAuthenticators
                            .get(step);

                    if (!stepFedIdps.containsKey(authenticatorInfo
                            .get(ApplicationConstants.IDP_NAME))) {
                        stepFedIdps.put(authenticatorInfo.get(ApplicationConstants.IDP_NAME),
                                new ArrayList<FederatedAuthenticatorConfig>());
                    }

                    List<FederatedAuthenticatorConfig> idpAuths = stepFedIdps.get(authenticatorInfo
                            .get(ApplicationConstants.IDP_NAME));
                    FederatedAuthenticatorConfig fedAuthenticator = new FederatedAuthenticatorConfig();
                    fedAuthenticator.setName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_NAME));
                    fedAuthenticator.setDisplayName(authenticatorInfo
                            .get(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME));
                    idpAuths.add(fedAuthenticator);
                }

                authStep.setSubjectStep("1".equals(stepInfoResultSet.getString(3)));
                authStep.setAttributeStep("1".equals(stepInfoResultSet.getString(4)));

                authSteps.put(step, authStep);
            }

            LocalAndOutboundAuthenticationConfig localAndOutboundConfiguration
                    = new LocalAndOutboundAuthenticationConfig();

            AuthenticationStep[] authenticationSteps = new AuthenticationStep[authSteps.size()];

            int authStepCount = 0;

            for (Entry<String, AuthenticationStep> entry : authSteps.entrySet()) {

                AuthenticationStep authStep = entry.getValue();
                String stepId = entry.getKey();

                List<LocalAuthenticatorConfig> localAuthenticatorList = stepLocalAuth.get(stepId);

                if (localAuthenticatorList != null && localAuthenticatorList.size() > 0) {
                    authStep.setLocalAuthenticatorConfigs(
                            localAuthenticatorList.toArray(
                                    new LocalAuthenticatorConfig[localAuthenticatorList.size()]));
                }

                Map<String, List<FederatedAuthenticatorConfig>> idpList = stepFedIdPAuthenticators
                        .get(stepId);

                if (idpList != null && idpList.size() > 0) {
                    IdentityProvider[] fedIdpList = new IdentityProvider[idpList.size()];
                    int idpCount = 0;

                    for (Entry<String, List<FederatedAuthenticatorConfig>> idpEntry : idpList
                            .entrySet()) {
                        String idpName = idpEntry.getKey();
                        List<FederatedAuthenticatorConfig> fedAuthenticators = idpEntry.getValue();
                        IdentityProvider idp = new IdentityProvider();
                        idp.setIdentityProviderName(idpName);
                        idp.setFederationHub(isFederationHubIdP(idpName, connection, tenantId));
                        idp.setFederatedAuthenticatorConfigs(
                                fedAuthenticators.toArray(new FederatedAuthenticatorConfig[fedAuthenticators.size()]));
                        idp.setDefaultAuthenticatorConfig(idp.getFederatedAuthenticatorConfigs()[0]);
                        fedIdpList[idpCount++] = idp;
                    }
                    authStep.setFederatedIdentityProviders(fedIdpList);
                }

                authenticationSteps[authStepCount++] = authStep;
            }

            Arrays.sort(authenticationSteps, Comparator.comparingInt(AuthenticationStep::getStepOrder));

            int numSteps = authenticationSteps.length;
            // We check if the steps have consecutive step numbers.
            if (numSteps > 0 && authenticationSteps[numSteps - 1].getStepOrder() != numSteps) {
                if (log.isDebugEnabled()) {
                    log.debug("Authentication steps of Application with id: " + applicationId + "  do not have " +
                            "consecutive numbers. This was possibility due to a IDP force deletion. Fixing the step " +
                            "order.");
                }
                // Iterate through the steps and fix step order.
                int count = 1;
                for (AuthenticationStep step : authenticationSteps) {
                    step.setStepOrder(count++);
                }
            }

            localAndOutboundConfiguration.setAuthenticationSteps(authenticationSteps);

            String authType = getAuthenticationType(applicationId, connection);
            if (StringUtils.equalsIgnoreCase(authType, ApplicationConstants.AUTH_TYPE_FEDERATED)
                    || StringUtils.equalsIgnoreCase(authType, ApplicationConstants.AUTH_TYPE_FLOW)) {
                if (ArrayUtils.isEmpty(authenticationSteps)) {
                    // Although auth type is 'federated' or 'flow' we don't have any authentication steps. This can
                    // happen due to a force delete of a federated identity provider referred by the SP. So we change
                    // the authType to 'default'.
                    if (log.isDebugEnabled()) {
                        log.debug("Authentication type is '" + authType + "' eventhough the application with id: " +
                                applicationId +
                                " has zero authentication step. This was possibility due to a IDP force deletion. " +
                                " Defaulting authentication type to " + ApplicationConstants.AUTH_TYPE_DEFAULT);
                    }
                    authType = ApplicationConstants.AUTH_TYPE_DEFAULT;
                }
            }

            localAndOutboundConfiguration.setAuthenticationType(authType);

            AuthenticationScriptConfig authenticationScriptConfig = getScriptConfiguration(applicationId, connection);
            if (authenticationScriptConfig != null) {
                localAndOutboundConfiguration.setAuthenticationScriptConfig(authenticationScriptConfig);
            }

            PreparedStatement localAndOutboundConfigPrepStmt = null;
            ResultSet localAndOutboundConfigResultSet = null;

            try {
                localAndOutboundConfigPrepStmt = connection
                        .prepareStatement(LOAD_LOCAL_AND_OUTBOUND_CONFIG_BY_APP_ID);
                localAndOutboundConfigPrepStmt.setInt(1, tenantId);
                localAndOutboundConfigPrepStmt.setInt(2, applicationId);
                localAndOutboundConfigResultSet = localAndOutboundConfigPrepStmt.executeQuery();

                if (localAndOutboundConfigResultSet.next()) {
                    localAndOutboundConfiguration.setUseTenantDomainInLocalSubjectIdentifier("1"
                            .equals(localAndOutboundConfigResultSet.getString(1)));
                    localAndOutboundConfiguration.setUseUserstoreDomainInLocalSubjectIdentifier("1"
                            .equals(localAndOutboundConfigResultSet.getString(2)));
                    localAndOutboundConfiguration.setEnableAuthorization("1"
                            .equals(localAndOutboundConfigResultSet.getString(3)));
                    localAndOutboundConfiguration.setAlwaysSendBackAuthenticatedListOfIdPs("1"
                            .equals(localAndOutboundConfigResultSet.getString(4)));
                    localAndOutboundConfiguration.setSubjectClaimUri(localAndOutboundConfigResultSet
                            .getString(5));

                    readAndSetConfigurationsFromProperties(propertyList, localAndOutboundConfiguration);
                }
            } finally {
                IdentityApplicationManagementUtil.closeStatement(localAndOutboundConfigPrepStmt);
                IdentityApplicationManagementUtil.closeResultSet(localAndOutboundConfigResultSet);
            }

            return localAndOutboundConfiguration;
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getStepInfoPrepStmt);
            IdentityApplicationManagementUtil.closeResultSet(stepInfoResultSet);
        }
    }

    private void readAndSetConfigurationsFromProperties(List<ServiceProviderProperty> propertyList,
                                                        LocalAndOutboundAuthenticationConfig localAndOutboundConfig) {
        // Override with changed values.
        if (CollectionUtils.isNotEmpty(propertyList)) {
            for (ServiceProviderProperty serviceProviderProperty : propertyList) {

                String name = serviceProviderProperty.getName();
                String value = serviceProviderProperty.getValue();

                if (USE_DOMAIN_IN_ROLES.equals(name)) {
                    localAndOutboundConfig.setUseUserstoreDomainInRoles(value == null || Boolean.parseBoolean(value));
                } else if (SKIP_CONSENT.equals(name)) {
                    localAndOutboundConfig.setSkipConsent(Boolean.parseBoolean(value));
                } else if (SKIP_LOGOUT_CONSENT.equals(name)) {
                    localAndOutboundConfig.setSkipLogoutConsent(Boolean.parseBoolean(value));
                }
            }
        }
    }

    private AuthenticationScriptConfig getScriptConfiguration(int applicationId, Connection connection)
            throws SQLException, IdentityApplicationManagementException {

        try (PreparedStatement localAndOutboundConfigScriptPrepStmt = connection
                .prepareStatement(LOAD_SCRIPT_BY_APP_ID_QUERY);) {

            localAndOutboundConfigScriptPrepStmt.setInt(1, applicationId);
            try (ResultSet localAndOutboundConfigScriptResultSet = localAndOutboundConfigScriptPrepStmt
                    .executeQuery()) {
                if (localAndOutboundConfigScriptResultSet.next()) {
                    AuthenticationScriptConfig authenticationScriptConfig = new AuthenticationScriptConfig();

                    try {
                        boolean isEnabled = "1".equals(localAndOutboundConfigScriptResultSet.getString(2));
                        InputStream scriptBinaryStream = localAndOutboundConfigScriptResultSet.getBinaryStream(1);
                        String targetString = StringUtils.EMPTY;
                        if (scriptBinaryStream != null) {
                            targetString = IOUtils.toString(scriptBinaryStream);
                        }
                        authenticationScriptConfig.setContent(targetString);
                        authenticationScriptConfig.setEnabled(isEnabled);
                    } catch (IOException e) {
                        throw new IdentityApplicationManagementException(
                                "Could not read the Script for application : " + applicationId, e);
                    }

                    return authenticationScriptConfig;
                }
            }
        }
        return null;
    }

    private boolean isFederationHubIdP(String idPName, Connection connection, int tenantId)
            throws SQLException {

        PreparedStatement get = null;
        ResultSet resultSet = null;

        try {
            get = connection.prepareStatement(LOAD_HUB_IDP_BY_NAME);

            get.setString(1, idPName);
            get.setInt(2, tenantId);
            resultSet = get.executeQuery();

            if (resultSet.next()) {
                return "1".equals(resultSet.getString(1));
            }

            return false;
        } finally {
            IdentityApplicationManagementUtil.closeStatement(get);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }

    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private ClaimConfig getClaimConfiguration(int applicationId, Connection connection, int tenantID)
            throws IdentityApplicationManagementException {

        ClaimConfig claimConfig = new ClaimConfig();
        ArrayList<ClaimMapping> claimMappingList = new ArrayList<ClaimMapping>();
        List<String> spDialectList = new ArrayList<String>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + applicationId);
        }

        PreparedStatement get = null;
        ResultSet resultSet = null;
        try {
            get = connection.prepareStatement(LOAD_CLAIM_MAPPING_BY_APP_ID);
            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            get.setInt(1, applicationId);
            get.setInt(2, tenantID);
            resultSet = get.executeQuery();

            while (resultSet.next()) {
                ClaimMapping claimMapping = new ClaimMapping();
                Claim localClaim = new Claim();
                Claim remoteClaim = new Claim();

                localClaim.setClaimUri(resultSet.getString(1));
                remoteClaim.setClaimUri(resultSet.getString(2));

                String requested = resultSet.getString(3);

                if ("1".equalsIgnoreCase(requested)) {
                    claimMapping.setRequested(true);
                } else {
                    claimMapping.setRequested(false);
                }

                String mandatory = resultSet.getString(4);

                if ("1".equalsIgnoreCase(mandatory)) {
                    claimMapping.setMandatory(true);
                } else {
                    claimMapping.setMandatory(false);
                }

                if (remoteClaim.getClaimUri() == null
                        || remoteClaim.getClaimUri().trim().length() == 0) {
                    remoteClaim.setClaimUri(localClaim.getClaimUri());
                }

                if (localClaim.getClaimUri() == null
                        || localClaim.getClaimUri().trim().length() == 0) {
                    localClaim.setClaimUri(remoteClaim.getClaimUri());
                }

                claimMapping.setDefaultValue(resultSet.getString(5));

                claimMapping.setLocalClaim(localClaim);
                claimMapping.setRemoteClaim(remoteClaim);

                claimMappingList.add(claimMapping);

                if (log.isDebugEnabled()) {
                    log.debug("Local Claim: " + claimMapping.getLocalClaim().getClaimUri()
                            + " SPClaim: " + claimMapping.getRemoteClaim().getClaimUri());
                }
            }

            claimConfig.setClaimMappings(claimMappingList.toArray(new ClaimMapping[claimMappingList
                    .size()]));
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(get);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }

        PreparedStatement loadClaimConfigsPrepStmt = null;
        ResultSet loadClaimConfigsResultSet = null;

        try {
            loadClaimConfigsPrepStmt = connection
                    .prepareStatement(LOAD_CLAIM_CONIFG_BY_APP_ID);
            loadClaimConfigsPrepStmt.setInt(1, tenantID);
            loadClaimConfigsPrepStmt.setInt(2, applicationId);
            loadClaimConfigsResultSet = loadClaimConfigsPrepStmt.executeQuery();

            while (loadClaimConfigsResultSet.next()) {
                claimConfig.setRoleClaimURI(loadClaimConfigsResultSet.getString(1));
                claimConfig.setLocalClaimDialect("1".equals(loadClaimConfigsResultSet.getString(2)));
                claimConfig.setAlwaysSendMappedLocalSubjectId("1".equals(loadClaimConfigsResultSet
                        .getString(3)));
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(loadClaimConfigsPrepStmt);
            IdentityApplicationManagementUtil.closeResultSet(loadClaimConfigsResultSet);
        }

        PreparedStatement loadSPDialectsPrepStmt = null;
        ResultSet loadSPDialectsResultSet = null;

        try {
            loadSPDialectsPrepStmt = connection
                    .prepareStatement(LOAD_SP_DIALECTS_BY_APP_ID);
            loadSPDialectsPrepStmt.setInt(1, tenantID);
            loadSPDialectsPrepStmt.setInt(2, applicationId);
            loadSPDialectsResultSet = loadSPDialectsPrepStmt.executeQuery();

            while (loadSPDialectsResultSet.next()) {
                String spDialect = loadSPDialectsResultSet.getString(1);
                if (spDialect != null && !spDialect.isEmpty()) {
                    spDialectList.add(spDialect);
                }
            }
            claimConfig.setSpClaimDialects(spDialectList.toArray(new String[spDialectList.size()]));
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(loadClaimConfigsPrepStmt);
            IdentityApplicationManagementUtil.closeResultSet(loadClaimConfigsResultSet);
        }

        return claimConfig;
    }

    /**
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private RequestPathAuthenticatorConfig[] getRequestPathAuthenticators(int applicationId,
                                                                          Connection connection, int tenantID)
            throws IdentityApplicationManagementException {

        PreparedStatement loadReqPathAuthenticators = null;
        ResultSet authResultSet = null;
        List<RequestPathAuthenticatorConfig> authenticators = new ArrayList<RequestPathAuthenticatorConfig>();

        try {
            loadReqPathAuthenticators = connection
                    .prepareStatement(LOAD_REQ_PATH_AUTHENTICATORS_BY_APP_ID);
            loadReqPathAuthenticators.setInt(1, applicationId);
            loadReqPathAuthenticators.setInt(2, tenantID);
            authResultSet = loadReqPathAuthenticators.executeQuery();

            while (authResultSet.next()) {
                RequestPathAuthenticatorConfig reqAuth = new RequestPathAuthenticatorConfig();
                reqAuth.setName(authResultSet.getString(1));
                authenticators.add(reqAuth);
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(loadReqPathAuthenticators);
            IdentityApplicationManagementUtil.closeResultSet(authResultSet);
        }

        return authenticators.toArray(new RequestPathAuthenticatorConfig[authenticators.size()]);
    }

    /**
     * @param applicationId
     * @param authenticators
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void updateRequestPathAuthenticators(int applicationId,
                                                 RequestPathAuthenticatorConfig[] authenticators, Connection connection)
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement storeReqPathAuthenticators = null;

        try {
            storeReqPathAuthenticators = connection
                    .prepareStatement(STORE_REQ_PATH_AUTHENTICATORS);
            if (authenticators != null && authenticators.length > 0) {
                for (RequestPathAuthenticatorConfig auth : authenticators) {
                    // TENANT_ID, AUTHENTICATOR_NAME, APP_ID
                    storeReqPathAuthenticators.setInt(1, tenantID);
                    storeReqPathAuthenticators.setString(2, auth.getName());
                    storeReqPathAuthenticators.setInt(3, applicationId);
                    storeReqPathAuthenticators.addBatch();
                }
                storeReqPathAuthenticators.executeBatch();
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(storeReqPathAuthenticators);
        }
    }

    /**
     * @param applicationID
     * @param connection
     * @throws SQLException
     */
    private void deleteRequestPathAuthenticators(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting request path authenticators " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement deleteReqAuthPrepStmt = null;
        try {
            deleteReqAuthPrepStmt = connection
                    .prepareStatement(REMOVE_REQ_PATH_AUTHENTICATOR);
            deleteReqAuthPrepStmt.setInt(1, applicationID);
            deleteReqAuthPrepStmt.setInt(2, tenantID);
            deleteReqAuthPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteReqAuthPrepStmt);
        }
    }

    /**
     * Reads the claim mappings for a given appID
     *
     * @param applicationId
     * @param connection
     * @return
     * @throws IdentityApplicationManagementException
     */
    private List<RoleMapping> getRoleMappingOfApplication(int applicationId, Connection connection,
                                                          int tenantID) throws IdentityApplicationManagementException {

        ArrayList<RoleMapping> roleMappingList = new ArrayList<RoleMapping>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Role Mapping of Application " + applicationId);
        }

        PreparedStatement getClientInfo = null;
        ResultSet resultSet = null;
        try {
            getClientInfo = connection
                    .prepareStatement(LOAD_ROLE_MAPPING_BY_APP_ID);
            // IDP_ROLE, SP_ROLE
            getClientInfo.setInt(1, applicationId);
            getClientInfo.setInt(2, tenantID);
            resultSet = getClientInfo.executeQuery();

            while (resultSet.next()) {
                RoleMapping roleMapping = new RoleMapping();
                LocalRole localRole = new LocalRole();
                localRole.setLocalRoleName(resultSet.getString(1));
                roleMapping.setLocalRole(localRole);
                roleMapping.setRemoteRole(resultSet.getString(2));
                roleMappingList.add(roleMapping);

                if (log.isDebugEnabled()) {
                    log.debug("Local Role: " + roleMapping.getLocalRole().getLocalRoleName()
                            + " SPRole: " + roleMapping.getRemoteRole());
                }
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving all application");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClientInfo);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
        }
        return roleMappingList;
    }

    /**
     * Get count of applications for user
     *
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int getCountOfAllApplications() throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int count;

        if (log.isDebugEnabled()) {
            log.debug("Getting the count of all applications for the tenantID: " + tenantID);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;

        try {
            getAppNamesStmt = connection
                    .prepareStatement(LOAD_APP_COUNT_BY_TENANT);
            getAppNamesStmt.setInt(1, tenantID);
            getAppNamesStmt.setString(2, LOCAL_SP);
            appNameResultSet = getAppNamesStmt.executeQuery();
            appNameResultSet.next();
            count = Integer.parseInt(appNameResultSet.getString(1));
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while getting the count of all Applications for the tenantID: " + tenantID, e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return count;
    }

    /**
     * Get application Names for user
     *
     * @return
     * @throws IdentityApplicationManagementException
     */
    public ApplicationBasicInfo[] getAllApplicationBasicInfo()
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Reading all Applications of Tenant " + tenantID);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;

        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<ApplicationBasicInfo>();

        try {
            getAppNamesStmt = connection
                    .prepareStatement(LOAD_APP_NAMES_BY_TENANT);
            getAppNamesStmt.setInt(1, tenantID);
            getAppNamesStmt.setString(2, LOCAL_SP);
            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                ApplicationBasicInfo basicInfo = new ApplicationBasicInfo();
                basicInfo.setApplicationId(appNameResultSet.getInt("ID"));
                basicInfo.setApplicationName(appNameResultSet.getString("APP_NAME"));
                basicInfo.setDescription(appNameResultSet.getString("DESCRIPTION"));
                appInfo.add(basicInfo);
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while Reading all Applications");
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ApplicationBasicInfo[0]);
    }

    private String resolveSQLFilter(String filter) {

        //To avoid any issues when the filter string is blank or null, assigning "%" to SQLFilter.
        String sqlfilter = "%";
        if (StringUtils.isNotBlank(filter)) {
            sqlfilter = filter.trim()
                    .replace("*", "%")
                    .replace("?", "_");
        }

        if (log.isDebugEnabled()) {
            log.debug("Input filter: " + filter + " resolved for SQL filter: " + sqlfilter);
        }

        return sqlfilter;
    }

    /**
     * Get count of applications for user which has the filter string
     *
     * @param filter
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int getCountOfApplications(String filter) throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        int count;

        if (log.isDebugEnabled()) {
            log.debug("Getting the count of all applications for the tenantID: " + tenantID);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;

        try {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            getAppNamesStmt = connection
                    .prepareStatement(LOAD_APP_COUNT_BY_TENANT_AND_APP_NAME);
            getAppNamesStmt.setInt(1, tenantID);
            getAppNamesStmt.setString(2, filterResolvedForSQL);
            getAppNamesStmt.setString(3, LOCAL_SP);
            appNameResultSet = getAppNamesStmt.executeQuery();
            appNameResultSet.next();
            count = Integer.parseInt(appNameResultSet.getString(1));
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while Reading all Applications for the tenantID: " + tenantID, e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return count;
    }

    /**
     * Get application Names for user which has the filter string
     *
     * @param filter
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter)
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Getting the all applications for the tenant: " + tenantID + " with filter: " + filter);
        }

        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;

        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<ApplicationBasicInfo>();

        try {
            String filterResolvedForSQL = resolveSQLFilter(filter);
            getAppNamesStmt = connection
                    .prepareStatement(LOAD_APP_NAMES_BY_TENANT_AND_APP_NAME);
            getAppNamesStmt.setInt(1, tenantID);
            getAppNamesStmt.setString(2, filterResolvedForSQL);
            getAppNamesStmt.setString(3, LOCAL_SP);
            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                ApplicationBasicInfo basicInfo = new ApplicationBasicInfo();
                basicInfo.setApplicationId(appNameResultSet.getInt("ID"));
                basicInfo.setApplicationName(appNameResultSet.getString("APP_NAME"));
                basicInfo.setDescription(appNameResultSet.getString("DESCRIPTION"));
                appInfo.add(basicInfo);
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while getting applications from DB with filter: " + filter, e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ApplicationBasicInfo[0]);
    }

    @Override
    public ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(int pageNumber)
            throws IdentityApplicationManagementException {

        validateRequestedPageNumber(pageNumber);

        int limit = ApplicationMgtUtil.getItemsPerPage();
        int offset = (pageNumber - 1) * limit;

        return getApplicationBasicInfo(offset, limit);
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(int offset,
                                                          int limit) throws IdentityApplicationManagementException {

        validateAttributesForPagination(offset, limit);

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        Connection connection = IdentityDatabaseUtil.getDBConnection();
        PreparedStatement getAppNamesStmt = null;
        ResultSet appNameResultSet = null;
        String sqlQuery;
        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<ApplicationBasicInfo>();

        try {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            if (databaseProductName.contains("MySQL")
                    || databaseProductName.contains("MariaDB")
                    || databaseProductName.contains("H2")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_MYSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateListAppNamesQueryValues(tenantID, offset, limit, getAppNamesStmt);
            } else if (databaseProductName.contains("Oracle")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_ORACLE;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateListAppNamesQueryValues(tenantID, offset + limit, offset, getAppNamesStmt);
            } else if (databaseProductName.contains("Microsoft")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_MSSQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateListAppNamesQueryValues(tenantID, offset, limit, getAppNamesStmt);
            } else if (databaseProductName.contains("PostgreSQL")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_POSTGRESQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateListAppNamesQueryValues(tenantID, limit, offset, getAppNamesStmt);
            } else if (databaseProductName.contains("DB2")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_DB2SQL;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                populateListAppNamesQueryValues(tenantID, offset + 1, offset + limit, getAppNamesStmt);
            } else if (databaseProductName.contains("INFORMIX")) {
                sqlQuery = LOAD_APP_NAMES_BY_TENANT_INFORMIX;
                getAppNamesStmt = connection.prepareStatement(sqlQuery);
                getAppNamesStmt.setInt(1, offset);
                getAppNamesStmt.setInt(2, limit);
                getAppNamesStmt.setInt(3, tenantID);
                getAppNamesStmt.setString(4, LOCAL_SP);

            } else {
                log.error("Error while loading applications from DB: Database driver could not be identified or " +
                        "not supported.");
                throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                        "Database driver could not be identified or not supported.");
            }

            appNameResultSet = getAppNamesStmt.executeQuery();

            while (appNameResultSet.next()) {
                appInfo.add(buildApplicationBasicInfo(appNameResultSet));
            }

        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while loading applications from DB: " +
                    e.getMessage(), e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getAppNamesStmt);
            IdentityApplicationManagementUtil.closeResultSet(appNameResultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return appInfo.toArray(new ApplicationBasicInfo[0]);
    }

    /**
     * Set values to the prepare statement for listing application names
     * @param start
     * @param end
     * @param tenantID
     * @param getAppNamesStmt
     * @throws SQLException
     */
    private void populateListAppNamesQueryValues(int tenantID, int start, int end,  PreparedStatement
            getAppNamesStmt) throws SQLException {
        getAppNamesStmt.setInt(1, tenantID);
        getAppNamesStmt.setString(2, LOCAL_SP);
        getAppNamesStmt.setInt(3, start);
        getAppNamesStmt.setInt(4, end);
    }

    /**
     * Deletes the application from IDN_APPMGT_APP table. Cascade deletes with foreign key
     * constraints should delete the corresponding entries from the tables
     *
     * @param appName
     * @throws IdentityApplicationManagementException
     */
    public void deleteApplication(String appName) throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Connection connection = IdentityDatabaseUtil.getDBConnection();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application " + appName);
        }
        // Now, delete the application
        try {

            // Delete the application certificate if there is any.
            deleteCertificate(connection, appName, tenantID);

            // First, delete all the clients of the application
            int applicationID = getApplicationIDByName(appName, tenantID, connection);
            InboundAuthenticationConfig clients = getInboundAuthenticationConfig(applicationID, connection, tenantID);
            for (InboundAuthenticationRequestConfig client : clients.getInboundAuthenticationRequestConfigs()) {
                handleClientDeletion(client.getInboundAuthKey(), client.getInboundAuthType());
            }
            handleDeleteServiceProvider(connection, appName, tenantID);
            IdentityDatabaseUtil.commitTransaction(connection);
        } catch (SQLException | UserStoreException | IdentityApplicationManagementException e) {
            IdentityDatabaseUtil.rollbackTransaction(connection);
            String errorMessege = "An error occured while delete the application : " + appName;
            log.error(errorMessege, e);
            throw new IdentityApplicationManagementException(errorMessege, e);
        } finally {
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
    }

    private void handleDeleteServiceProvider(Connection connection, String appName, int tenantId)
            throws IdentityApplicationManagementException {

        try {
            deleteServiceProvider(connection, appName, tenantId);
        } catch (IdentityApplicationManagementException e) {
            /*
             * For more information read https://github.com/wso2/product-is/issues/12579. This is to overcome the
             * above issue.
             */
            log.error(String.format("Error occurred while trying to deleting service provider: %s in tenant: %s. " +
                    "Retrying again", appName, tenantId), e);
            boolean isOperationFailed = true;
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    Thread.sleep(1000);
                    deleteServiceProvider(connection, appName, tenantId);
                    isOperationFailed = false;
                    log.info(String.format("Service provider: %s in tenant: %s deleted in the retry attempt: %s",
                            appName, tenantId, attempt));
                    break;
                } catch (Exception exception) {
                    log.error(String.format("Retry attempt: %s failed to delete service provider: %s in tenant: %s",
                            attempt, attempt, tenantId), exception);
                }
            }
            if (isOperationFailed) {
                throw new IdentityApplicationManagementException(String.format("Error while deleting service " +
                        "provider: %s in tenant: %s", appName, tenantId), e);
            }
        }
    }

    private void deleteServiceProvider(Connection connection, String appName, int tenantId)
            throws IdentityApplicationManagementException {

        try (PreparedStatement deleteClientPrepStmt = connection.prepareStatement(REMOVE_APP_FROM_APPMGT_APP)) {
            deleteClientPrepStmt.setString(1, appName);
            deleteClientPrepStmt.setInt(2, tenantId);
            deleteClientPrepStmt.execute();
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(String.format("Error while deleting application: %s " +
                    "in tenant: %s from SP_APP", appName, tenantId));
        }
    }

    /**
     * Deletes the Application with application ID
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    public void deleteApplication(int applicationID, Connection connection)
            throws IdentityApplicationManagementException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application " + applicationID);
        }

        // Now, delete the application
        PreparedStatement deleteClientPrepStmt = null;
        try {

            // delete clients
            InboundAuthenticationConfig clients = getInboundAuthenticationConfig(applicationID,
                    connection, tenantID);
            for (InboundAuthenticationRequestConfig client : clients
                    .getInboundAuthenticationRequestConfigs()) {
                handleClientDeletion(client.getInboundAuthKey(), client.getInboundAuthType());
            }

            String applicationName = getApplicationName(applicationID, connection);
            // delete roles
            ApplicationMgtUtil.deleteAppRole(applicationName);

            deleteClientPrepStmt = connection
                    .prepareStatement(REMOVE_APP_FROM_APPMGT_APP_WITH_ID);
            deleteClientPrepStmt.setInt(1, applicationID);
            deleteClientPrepStmt.setInt(2, tenantID);
            deleteClientPrepStmt.execute();

            if (!connection.getAutoCommit()) {
                connection.commit();
            }

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new IdentityApplicationManagementException("Error deleting application");

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteClientPrepStmt);
        }

    }

    /**
     * Delete all applications of a given tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws IdentityApplicationManagementException throws when an error occurs in deleting applications.
     */
    @Override
    public void deleteApplications(int tenantId) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting all applications of the tenant: " + tenantId);
        }

        String auditData = "\"" + "Tenant Id" + "\" : \"" + tenantId + "\"";

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {

            // Delete the application certificates of the tenant.
            deleteCertificatesByTenantId(connection, tenantId);

            try (PreparedStatement deleteClientPrepStmt = connection
                    .prepareStatement(REMOVE_APPS_FROM_APPMGT_APP_BY_TENANT_ID)) {
                deleteClientPrepStmt.setInt(1, tenantId);
                deleteClientPrepStmt.execute();
                IdentityDatabaseUtil.commitTransaction(connection);
                audit("Delete all applications of a tenant", auditData, AUDIT_SUCCESS);
            }
        } catch (SQLException e) {
            audit("Delete all applications of a tenant", auditData, AUDIT_FAIL);
            String msg = "An error occurred while delete all the applications of the tenant: " + tenantId;
            log.error(msg, e);
            throw new IdentityApplicationManagementException(msg, e);
        }
    }

    /**
     * Deleting Clients of the Application
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void deleteInboundAuthRequestConfiguration(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Clients of the Application " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        PreparedStatement deleteClientPrepStmt = null;

        try {
            deleteClientPrepStmt = connection.prepareStatement(REMOVE_CLIENT_FROM_APPMGT_CLIENT);
            // APP_ID = ? AND TENANT_ID = ?
            deleteClientPrepStmt.setInt(1, applicationID);
            deleteClientPrepStmt.setInt(2, tenantID);
            deleteClientPrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteClientPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @throws SQLException
     */
    private void deleteLocalAndOutboundAuthenticationConfiguration(int applicationId,
                                                                   Connection connection) throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Steps of Application " + applicationId);
        }

        PreparedStatement deleteLocalAndOutboundAuthConfigPrepStmt = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            deleteLocalAndOutboundAuthConfigPrepStmt = connection
                    .prepareStatement(REMOVE_STEP_FROM_APPMGT_STEP);
            deleteLocalAndOutboundAuthConfigPrepStmt.setInt(1, applicationId);
            deleteLocalAndOutboundAuthConfigPrepStmt.setInt(2, tenantId);
            deleteLocalAndOutboundAuthConfigPrepStmt.execute();
            deleteAuthenticationScript(applicationId, connection);
        } finally {
            IdentityApplicationManagementUtil
                    .closeStatement(deleteLocalAndOutboundAuthConfigPrepStmt);
        }
    }

    /**
     * @param applicationId
     * @param connection
     * @throws SQLException
     */
    private void deleteOutboundProvisioningConfiguration(int applicationId, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Steps of Application " + applicationId);
        }

        PreparedStatement deleteOutboundProConfigPrepStmt = null;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            deleteOutboundProConfigPrepStmt = connection
                    .prepareStatement(REMOVE_PRO_CONNECTORS);
            deleteOutboundProConfigPrepStmt.setInt(1, applicationId);
            deleteOutboundProConfigPrepStmt.setInt(2, tenantId);
            deleteOutboundProConfigPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteOutboundProConfigPrepStmt);
        }
    }

    /**
     * Deletes clients using the service stubs
     *
     * @param clientIdentifier
     * @param type
     * @throws IdentityApplicationManagementException
     */
    private void deleteClient(String clientIdentifier, String type)
            throws IdentityApplicationManagementException {

        if ("samlsso".equalsIgnoreCase(type)) {
            new SAMLApplicationDAOImpl().removeServiceProviderConfiguration(clientIdentifier);
        } else if ("oauth2".equalsIgnoreCase(type)) {
            new OAuthApplicationDAOImpl().removeOAuthApplication(clientIdentifier);
        }
    }

    /**
     * Handle client deletion. Retry if an error occurred while deleting an client.
     *
     * @param clientIdentifier Client identifier.
     * @param inboundAuthType  Inbound auth type.
     * @throws IdentityApplicationManagementException If an error occurred while deleting the client.
     */
    private void handleClientDeletion(String clientIdentifier, String inboundAuthType)
            throws IdentityApplicationManagementException {

        try {
            deleteClient(clientIdentifier, inboundAuthType);
        } catch (Exception e) {
            /*
             * For more information read https://github.com/wso2/product-is/issues/12579. This is to overcome the
             * above issue.
             */
            log.error(String.format("Error occurred during the initial attempt to delete client with identifier: " +
                    "%s with auth type: %s", clientIdentifier, inboundAuthType), e);
            boolean isOperationFailed = true;
            for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
                try {
                    Thread.sleep(1000);
                    deleteClient(clientIdentifier, inboundAuthType);
                    isOperationFailed = false;
                    log.info(String.format("Successfully deleted application with identifier: %s with auth type: %s " +
                            "during the delete attempt: %s", clientIdentifier, inboundAuthType, attempt));
                    break;
                } catch (Exception exception) {
                    log.error(String.format("Retry attempt: %s failed to delete application with identifier: %s " +
                            "with auth type: %s", attempt, clientIdentifier, inboundAuthType), exception);
                }
            }
            if (isOperationFailed) {
                throw new IdentityApplicationManagementException(String.format("application with identifier: %s " +
                        "with auth type: %s" + clientIdentifier, inboundAuthType), e);
            }
        }
    }

    /**
     * Delete Claim Mapping of the Application
     *
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    private void deleteClaimConfiguration(int applicationID, Connection connection)
            throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application Claim Mapping " + applicationID);
        }

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        PreparedStatement deleteCliamPrepStmt = null;
        PreparedStatement deleteSpDialectPrepStmt = null;
        try {
            deleteCliamPrepStmt = connection
                    .prepareStatement(REMOVE_CLAIM_MAPPINGS_FROM_APPMGT_CLAIM_MAPPING);
            deleteCliamPrepStmt.setInt(1, applicationID);
            deleteCliamPrepStmt.setInt(2, tenantID);
            deleteCliamPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteCliamPrepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application SP Dialects " + applicationID);
        }

        try {
            deleteSpDialectPrepStmt = connection
                    .prepareStatement(DELETE_SP_DIALECTS_BY_APP_ID);
            deleteSpDialectPrepStmt.setInt(1, applicationID);
            deleteSpDialectPrepStmt.setInt(2, tenantID);
            deleteSpDialectPrepStmt.execute();

        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteSpDialectPrepStmt);
        }
    }

    /**
     * @param applicationID
     * @param connection
     * @throws IdentityApplicationManagementException
     */
    public void deletePermissionAndRoleConfiguration(int applicationID, Connection connection)
            throws SQLException {

        int tenantID = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("Deleting Role Mapping of Application " + applicationID);
        }

        PreparedStatement deleteRoleMappingPrepStmt = null;
        try {
            deleteRoleMappingPrepStmt = connection
                    .prepareStatement(REMOVE_ROLE_MAPPINGS_FROM_APPMGT_ROLE_MAPPING);
            deleteRoleMappingPrepStmt.setInt(1, applicationID);
            deleteRoleMappingPrepStmt.setInt(2, tenantID);
            deleteRoleMappingPrepStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteRoleMappingPrepStmt);
        }
    }

    /**
     * Delete the certificate of the given application if there is one.
     *
     * @param connection
     * @param appName
     * @param tenantID
     * @throws UserStoreException
     * @throws IdentityApplicationManagementException
     * @throws SQLException
     */
    private void deleteCertificate(Connection connection, String appName, int tenantID)
            throws UserStoreException, IdentityApplicationManagementException, SQLException {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

        if (tenantID != MultitenantConstants.SUPER_TENANT_ID) {
            Tenant tenant = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenant(tenantID);
            tenantDomain = tenant.getDomain();
        }

        ServiceProvider application = getApplication(appName, tenantDomain);
        String certificateReferenceID = getCertificateReferenceID(application.getSpProperties());

        if (certificateReferenceID != null) {
            deleteCertificate(connection, Integer.parseInt(certificateReferenceID));
        }
    }

    /**
     * Deletes the certificate for given ID from the database.
     *
     * @param connection
     * @param id
     */
    private void deleteCertificate(Connection connection, int id) throws SQLException {

        PreparedStatement statementToRemoveCertificate = null;
        try {

            statementToRemoveCertificate = connection.prepareStatement(REMOVE_CERTIFICATE);
            statementToRemoveCertificate.setInt(1, id);
            statementToRemoveCertificate.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(statementToRemoveCertificate);
        }
    }

    /**
     * Deletes all certificates of a given tenant id from the database.
     *
     * @param connection The database connection.
     * @param tenantId The id of the tenant.
     */
    private void deleteCertificatesByTenantId(Connection connection, int tenantId) throws SQLException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting all application certificates of tenant: " + tenantId);
        }

        PreparedStatement deleteCertificatesStmt = null;

        try {
            deleteCertificatesStmt = connection.prepareStatement(REMOVE_CERTIFICATES_BY_TENANT_ID);
            deleteCertificatesStmt.setInt(1, tenantId);
            deleteCertificatesStmt.execute();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteCertificatesStmt);
        }
    }

    /**
     * Delete assigned role permission mappings for deleted permissions
     *
     * @param connection
     * @param applicationName
     * @param permissions
     * @throws IdentityApplicationManagementException
     * @throws SQLException
     */
    public void deleteAssignedPermissions(Connection connection, String applicationName,
                                          ApplicationPermission[] permissions)
            throws IdentityApplicationManagementException, SQLException {

        List<ApplicationPermission> loadPermissions = ApplicationMgtUtil.loadPermissions(applicationName);
        List<ApplicationPermission> removedPermissions = null;
        if (!CollectionUtils.isEmpty(loadPermissions)) {
            if (ArrayUtils.isEmpty(permissions)) {
                removedPermissions = new ArrayList<ApplicationPermission>(loadPermissions);
            } else {
                removedPermissions = new ArrayList<ApplicationPermission>();
                for (ApplicationPermission storedPermission : loadPermissions) {
                    boolean isStored = false;
                    for (ApplicationPermission applicationPermission : permissions) {
                        if (applicationPermission.getValue().equals(storedPermission.getValue())) {
                            isStored = true;
                            break;
                        }
                    }
                    if (!isStored) {
                        removedPermissions.add(storedPermission);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(removedPermissions)) {
            //delete permissions
            for (ApplicationPermission applicationPermission : removedPermissions) {
                String permissionValue = ApplicationMgtUtil.PATH_CONSTANT +
                        ApplicationMgtUtil.getApplicationPermissionPath() +
                        ApplicationMgtUtil.PATH_CONSTANT +
                        applicationName + ApplicationMgtUtil.PATH_CONSTANT +
                        applicationPermission.getValue();
                int permisionId = getPermissionId(permissionValue.toLowerCase());
                deleteRolePermissionMapping(permisionId);
                deletePermission(permisionId);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO#getServiceProviderNameByClientId
     * (java.lang.String, java.lang.String, java.lang.String)
     */
    public String getServiceProviderNameByClientId(String clientId, String clientType,
                                                   String tenantDomain) throws IdentityApplicationManagementException {

        int tenantID = MultitenantConstants.SUPER_TENANT_ID;

        if (StringUtils.isEmpty(clientId)) {
            return null;
        }

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }

        String applicationName = null;

        // Reading application name from the database
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        PreparedStatement storeAppPrepStmt = null;
        ResultSet appNameResult = null;
        try {
            storeAppPrepStmt = connection
                    .prepareStatement(LOAD_APPLICATION_NAME_BY_CLIENT_ID_AND_TYPE);
            storeAppPrepStmt.setString(1, clientId);
            storeAppPrepStmt.setString(2, clientType);
            storeAppPrepStmt.setInt(3, tenantID);
            storeAppPrepStmt.setInt(4, tenantID);
            appNameResult = storeAppPrepStmt.executeQuery();
            if (appNameResult.next()) {
                applicationName = appNameResult.getString(1);
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error while reading application", e);
        } finally {
            IdentityApplicationManagementUtil.closeResultSet(appNameResult);
            IdentityApplicationManagementUtil.closeStatement(storeAppPrepStmt);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return applicationName;
    }

    /**
     * @param serviceProviderName
     * @param tenantDomain
     * @param localIdpAsKey
     * @return
     * @throws SQLException
     * @throws IdentityApplicationManagementException
     */
    private Map<String, String> getClaimMapping(String serviceProviderName, String tenantDomain,
                                                boolean localIdpAsKey)
            throws SQLException, IdentityApplicationManagementException {

        int tenantID = -123;

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }
        Map<String, String> claimMapping = new HashMap<String, String>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + serviceProviderName);
        }

        PreparedStatement getClaimPreStmt = null;
        ResultSet resultSet = null;
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {
            getClaimPreStmt = connection.prepareStatement(LOAD_CLAIM_MAPPING_BY_APP_NAME);
            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            getClaimPreStmt.setString(1, serviceProviderName);
            getClaimPreStmt.setInt(2, tenantID);
            resultSet = getClaimPreStmt.executeQuery();

            while (resultSet.next()) {
                if (localIdpAsKey) {
                    claimMapping.put(resultSet.getString(1), resultSet.getString(2));
                } else {
                    claimMapping.put(resultSet.getString(2), resultSet.getString(1));
                }
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClaimPreStmt);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }

        return claimMapping;
    }

    @Override
    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                        String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            return getClaimMapping(serviceProviderName, tenantDomain, false);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving claim mapping", e);
        }
    }

    @Override
    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                        String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            return getClaimMapping(serviceProviderName, tenantDomain, true);
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving claim mapping", e);
        }
    }

    @Override
    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                               String tenantDomain)
            throws IdentityApplicationManagementException {

        int tenantID = -123;

        if (tenantDomain != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantDomain);
            } catch (UserStoreException e1) {
                throw new IdentityApplicationManagementException("Error while reading application");
            }
        }
        List<String> reqClaimUris = new ArrayList<String>();

        if (log.isDebugEnabled()) {
            log.debug("Reading Claim Mappings of Application " + serviceProviderName);
        }

        PreparedStatement getClaimPreStmt = null;
        ResultSet resultSet = null;
        Connection connection = IdentityDatabaseUtil.getDBConnection(false);
        try {

            getClaimPreStmt = connection
                    .prepareStatement(LOAD_CLAIM_MAPPING_BY_APP_NAME);

            // IDP_CLAIM, SP_CLAIM, IS_REQUESTED
            getClaimPreStmt.setString(1, serviceProviderName);
            getClaimPreStmt.setInt(2, tenantID);
            resultSet = getClaimPreStmt.executeQuery();
            while (resultSet.next()) {
                if ("1".equalsIgnoreCase(resultSet.getString(3))) {
                    reqClaimUris.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException(
                    "Error while retrieving requested claims", e);
        } finally {
            IdentityApplicationManagementUtil.closeStatement(getClaimPreStmt);
            IdentityApplicationManagementUtil.closeResultSet(resultSet);
            IdentityApplicationManagementUtil.closeConnection(connection);
        }
        return reqClaimUris;

    }

    @Override
    public boolean isApplicationExists(String serviceProviderName, String tenantName) throws
            IdentityApplicationManagementException {

        int tenantID = MultitenantConstants.SUPER_TENANT_ID;
        if (tenantName != null) {
            try {
                tenantID = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                        .getTenantManager().getTenantId(tenantName);
            } catch (UserStoreException e1) {
                log.error("Error in reading application", e1);
                throw new IdentityApplicationManagementException("Error while reading application", e1);
            }
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (PreparedStatement checkAppExistence = connection
                    .prepareStatement(LOAD_BASIC_APP_INFO_BY_APP_NAME)) {
                checkAppExistence.setString(1, serviceProviderName);
                checkAppExistence.setInt(2, tenantID);

                try (ResultSet resultSet = checkAppExistence.executeQuery()) {
                    if (resultSet.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Failed to check whether the service provider exists with"
                    + serviceProviderName, e);
        }
        return false;
    }

    /**
     * @param conn
     * @param tenantId
     * @param idpName
     * @param authenticatorName
     * @return
     * @throws SQLException
     */
    private int getAuthentictorID(Connection conn, int tenantId, String idpName,
                                  String authenticatorName) throws SQLException {

        if (idpName == null || idpName.isEmpty()) {
            return -1;
        }
        int authId = -1;

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = LOAD_IDP_AUTHENTICATOR_ID;
        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setString(1, authenticatorName);
            prepStmt.setString(2, idpName);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, tenantId);
            prepStmt.setInt(5, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                authId = rs.getInt(1);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return authId;
    }

    /**
     * @param conn
     * @param tenantId
     * @param authenticatorId
     * @return
     * @throws SQLException
     */
    private Map<String, String> getAuthenticatorInfo(Connection conn, int tenantId,
                                                     int authenticatorId) throws SQLException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlStmt = LOAD_IDP_AND_AUTHENTICATOR_NAMES;
        Map<String, String> returnData = new HashMap<String, String>();
        try {
            prepStmt = conn.prepareStatement(sqlStmt);
            prepStmt.setInt(1, authenticatorId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setInt(4, MultitenantConstants.SUPER_TENANT_ID);
            prepStmt.setInt(5, MultitenantConstants.SUPER_TENANT_ID);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                returnData.put(ApplicationConstants.IDP_NAME, rs.getString(1));
                returnData.put(ApplicationConstants.IDP_AUTHENTICATOR_NAME, rs.getString(2));
                returnData
                        .put(ApplicationConstants.IDP_AUTHENTICATOR_DISPLAY_NAME, rs.getString(3));
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return returnData;
    }

    /**
     * @param conn
     * @param tenantId
     * @param idpName
     * @param authenticatorName
     * @param authenticatorDispalyName
     * @return
     * @throws SQLException
     */
    private int addAuthenticator(Connection conn, int tenantId, String idpName,
                                 String authenticatorName, String authenticatorDispalyName) throws SQLException {

        int authenticatorId = -1;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        // TENANT_ID, IDP_ID, NAME,IS_ENABLED, DISPLAY_NAME
        String sqlStmt = STORE_LOCAL_AUTHENTICATOR;
        try {
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            prepStmt = conn.prepareStatement(sqlStmt, new String[]{
                    DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "ID")});
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, idpName);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, authenticatorName);
            prepStmt.setString(5, "1");
            prepStmt.setString(6, authenticatorDispalyName);
            prepStmt.execute();
            rs = prepStmt.getGeneratedKeys();
            if (rs.next()) {
                authenticatorId = rs.getInt(1);
            }
        } finally {
            IdentityApplicationManagementUtil.closeStatement(prepStmt);
        }
        return authenticatorId;
    }

    /**
     * Read application role permissions for a given application name
     *
     * @param applicationName Application name
     * @return Map of key value pairs. key is UM table id and value is permission
     * @throws SQLException
     */
    private Map<String, String> readApplicationPermissions(String applicationName) throws SQLException {

        PreparedStatement readPermissionsPrepStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        Map<String, String> permissions = new HashMap<>();
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            readPermissionsPrepStmt = connection.prepareStatement(LOAD_UM_PERMISSIONS);
            readPermissionsPrepStmt.setString(1, "%" + ApplicationMgtUtil.getApplicationPermissionPath() + "%");
            resultSet = readPermissionsPrepStmt.executeQuery();
            while (resultSet.next()) {
                String umId = resultSet.getString(1);
                String permission = resultSet.getString(2);
                if (permission.contains(ApplicationMgtUtil.getApplicationPermissionPath() +
                        ApplicationMgtUtil.PATH_CONSTANT + applicationName.toLowerCase())) {
                    permissions.put(umId, permission);
                }
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(readPermissionsPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return permissions;
    }

    /**
     * Update the permission path for a given id
     *
     * @param id            Id
     * @param newPermission New permission path value
     * @throws SQLException
     */
    private void updatePermissionPath(String id, String newPermission) throws SQLException {

        PreparedStatement updatePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            updatePermissionPrepStmt = connection.prepareStatement(UPDATE_SP_PERMISSIONS);
            updatePermissionPrepStmt.setString(1, newPermission);
            updatePermissionPrepStmt.setString(2, id);
            updatePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityDatabaseUtil.closeStatement(updatePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Get permission id for a given permission path
     *
     * @param permission Permission path
     * @return Permission id
     * @throws SQLException
     */
    private int getPermissionId(String permission) throws SQLException {

        PreparedStatement loadPermissionsPrepStmt = null;
        ResultSet resultSet = null;
        Connection connection = null;
        int id = -1;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            loadPermissionsPrepStmt = connection.prepareStatement(LOAD_UM_PERMISSIONS_W);
            loadPermissionsPrepStmt.setString(1, permission.toLowerCase());
            resultSet = loadPermissionsPrepStmt.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt(1);
            }
        } finally {
            IdentityDatabaseUtil.closeResultSet(resultSet);
            IdentityDatabaseUtil.closeStatement(loadPermissionsPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
        return id;
    }

    /**
     * Delete role permission mapping for a given permission id
     *
     * @param id Permission id
     * @throws SQLException
     */
    private void deleteRolePermissionMapping(int id) throws SQLException {

        PreparedStatement deleteRolePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            deleteRolePermissionPrepStmt = connection.prepareStatement(REMOVE_UM_ROLE_PERMISSION);
            deleteRolePermissionPrepStmt.setInt(1, id);
            deleteRolePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deleteRolePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Delete permission entry for a given id
     *
     * @param entryId Entry id
     * @throws SQLException
     */
    private void deletePermission(int entryId) throws SQLException {

        PreparedStatement deletePermissionPrepStmt = null;
        Connection connection = null;
        try {

            connection = IdentityDatabaseUtil.getUserDBConnection();
            deletePermissionPrepStmt = connection.prepareStatement(REMOVE_UM_PERMISSIONS);
            deletePermissionPrepStmt.setInt(1, entryId);
            deletePermissionPrepStmt.executeUpdate();
        } finally {
            IdentityApplicationManagementUtil.closeStatement(deletePermissionPrepStmt);
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    /**
     * Updates the authentication script configuration.
     *
     * @param applicationId
     * @param localAndOutboundAuthConfig
     * @param connection
     * @param tenantID
     * @throws SQLException
     */
    private void updateAuthenticationScriptConfiguration(int applicationId,
                                                     LocalAndOutboundAuthenticationConfig localAndOutboundAuthConfig,
                                                     Connection connection, int tenantID)
            throws SQLException {

        if (localAndOutboundAuthConfig.getAuthenticationScriptConfig() != null) {
            AuthenticationScriptConfig authenticationScriptConfig = localAndOutboundAuthConfig
                    .getAuthenticationScriptConfig();
            try (PreparedStatement storeAuthScriptPrepStmt = connection
                    .prepareStatement(STORE_SP_AUTH_SCRIPT)) {

                storeAuthScriptPrepStmt.setInt(1, tenantID);
                storeAuthScriptPrepStmt.setInt(2, applicationId);
                storeAuthScriptPrepStmt.setString(3, authenticationScriptConfig.getLanguage());
                setBlobValue(authenticationScriptConfig.getContent(), storeAuthScriptPrepStmt, 4);
                storeAuthScriptPrepStmt.setString(5, authenticationScriptConfig.isEnabled() ? "1" : "0");
                storeAuthScriptPrepStmt.execute();
            } catch (IOException ex) {
                log.error("Error occurred while updating authentication script configuration.", ex);
            }
        }
    }

    /**
     * Deletes the authentication Script, given the application (SP) ID.
     *
     * @param applicationId
     * @param connection
     * @throws SQLException
     */
    private void deleteAuthenticationScript(int applicationId, Connection connection) throws SQLException {

        PreparedStatement deleteLocalAndOutboundAuthScriptConfigPrepStmt;
        deleteLocalAndOutboundAuthScriptConfigPrepStmt = connection
                .prepareStatement(REMOVE_AUTH_SCRIPT);
        deleteLocalAndOutboundAuthScriptConfigPrepStmt.setInt(1, applicationId);
        deleteLocalAndOutboundAuthScriptConfigPrepStmt.execute();
    }

    /**
     * Set given string as Blob for the given index into the prepared-statement
     *
     * @param value    string value to be converted to blob
     * @param prepStmt Prepared statement
     * @param index    column index
     * @throws SQLException
     * @throws IOException
     */
    private void setBlobValue(String value, PreparedStatement prepStmt, int index) throws SQLException, IOException {

        if (value != null) {
            InputStream inputStream = new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
            prepStmt.setBinaryStream(index, inputStream, inputStream.available());
        } else {
            prepStmt.setBinaryStream(index, new ByteArrayInputStream(new byte[0]), 0);
        }
    }

    /**
     * Get string from inputStream of a blob
     *
     * @param is input stream
     * @return
     * @throws IOException
     */
    private String getBlobValue(InputStream is) throws IOException {

        if (is != null) {
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        log.error("Error in retrieving the Blob value", e);
                    }
                }
            }

            return sb.toString();
        }
        return null;
    }

    private void updateConfigurationsAsServiceProperties(ServiceProvider sp) {

        if (sp.getSpProperties() == null) {
            sp.setSpProperties(new ServiceProviderProperty[0]);
        }

        Map<String, ServiceProviderProperty> spPropertyMap = Arrays.stream(sp.getSpProperties())
                .collect(Collectors.toMap(ServiceProviderProperty::getName, Function.identity()));

        // Add use user store domain in roles property.
        if (sp.getLocalAndOutBoundAuthenticationConfig() != null) {
            ServiceProviderProperty userUserStoreDomainInRoles = buildUserStoreDomainInRolesProperty(sp);
            spPropertyMap.put(userUserStoreDomainInRoles.getName(), userUserStoreDomainInRoles);

            ServiceProviderProperty skipConsentProperty = buildSkipConsentProperty(sp);
            spPropertyMap.put(skipConsentProperty.getName(), skipConsentProperty);

            ServiceProviderProperty skipLogoutConsentProperty = buildSkipLogoutConsentProperty(sp);
            spPropertyMap.put(skipLogoutConsentProperty.getName(), skipLogoutConsentProperty);
        }

        ServiceProviderProperty jwksUri = buildJwksProperty(sp);
        spPropertyMap.put(jwksUri.getName(), jwksUri);

        ServiceProviderProperty templateIdProperty = buildTemplateIdProperty(sp);
        spPropertyMap.put(templateIdProperty.getName(), templateIdProperty);

        ServiceProviderProperty isManagementAppProperty = buildIsManagementAppProperty(sp);
        spPropertyMap.put(isManagementAppProperty.getName(), isManagementAppProperty);

        sp.setSpProperties(spPropertyMap.values().toArray(new ServiceProviderProperty[0]));
    }

    private ServiceProviderProperty buildIsManagementAppProperty(ServiceProvider sp) {

        ServiceProviderProperty isManagementAppProperty = new ServiceProviderProperty();
        isManagementAppProperty.setName(IS_MANAGEMENT_APP_SP_PROPERTY_NAME);
        isManagementAppProperty.setDisplayName(IS_MANAGEMENT_APP_SP_PROPERTY_DISPLAY_NAME);
        isManagementAppProperty.setValue(String.valueOf(sp.isManagementApp()));
        return isManagementAppProperty;
    }

    private ServiceProviderProperty buildTemplateIdProperty(ServiceProvider sp) {

        ServiceProviderProperty templateIdProperty = new ServiceProviderProperty();
        templateIdProperty.setName(TEMPLATE_ID_SP_PROPERTY_NAME);
        templateIdProperty.setDisplayName(TEMPLATE_ID_SP_PROPERTY_DISPLAY_NAME);
        templateIdProperty
                .setValue(StringUtils.isNotBlank(sp.getTemplateId()) ? sp.getTemplateId() : StringUtils.EMPTY);
        return templateIdProperty;
    }

    private ServiceProviderProperty buildJwksProperty(ServiceProvider sp) {

        ServiceProviderProperty jwksUri = new ServiceProviderProperty();
        jwksUri.setName(JWKS_URI_SP_PROPERTY_NAME);
        jwksUri.setDisplayName(JWKS_URI_SP_PROPERTY_NAME);
        jwksUri.setValue(StringUtils.isNotBlank(sp.getJwksUri()) ? sp.getJwksUri() : StringUtils.EMPTY);
        return jwksUri;
    }

    private ServiceProviderProperty buildSkipConsentProperty(ServiceProvider sp) {

        ServiceProviderProperty skipConsentProperty = new ServiceProviderProperty();
        skipConsentProperty.setName(SKIP_CONSENT);
        skipConsentProperty.setDisplayName(SKIP_CONSENT_DISPLAY_NAME);

        skipConsentProperty.setValue(String.valueOf(sp.getLocalAndOutBoundAuthenticationConfig().isSkipConsent()));
        return skipConsentProperty;
    }

    private ServiceProviderProperty buildSkipLogoutConsentProperty(ServiceProvider sp) {

        ServiceProviderProperty skipLogoutConsentProperty = new ServiceProviderProperty();
        skipLogoutConsentProperty.setName(SKIP_LOGOUT_CONSENT);
        skipLogoutConsentProperty.setDisplayName(SKIP_LOGOUT_CONSENT_DISPLAY_NAME);

        skipLogoutConsentProperty.setValue(
                String.valueOf(sp.getLocalAndOutBoundAuthenticationConfig().isSkipLogoutConsent()));
        return skipLogoutConsentProperty;
    }

    private ServiceProviderProperty buildUserStoreDomainInRolesProperty(ServiceProvider sp) {

        ServiceProviderProperty property = new ServiceProviderProperty();
        property.setName(USE_DOMAIN_IN_ROLES);
        property.setDisplayName(USE_DOMAIN_IN_ROLE_DISPLAY_NAME);

        property.setValue(String.valueOf(sp.getLocalAndOutBoundAuthenticationConfig().isUseUserstoreDomainInRoles()));
        return property;
    }

    private void loadApplicationPermissions(String serviceProviderName, ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        try {
            ApplicationMgtUtil.startTenantFlow(serviceProvider.getOwner().getTenantDomain());
            List<ApplicationPermission> permissionList = ApplicationMgtUtil.loadPermissions(serviceProviderName);

            if (permissionList != null) {
                PermissionsAndRoleConfig permissionAndRoleConfig;
                if (serviceProvider.getPermissionAndRoleConfig() == null) {
                    permissionAndRoleConfig = new PermissionsAndRoleConfig();
                } else {
                    permissionAndRoleConfig = serviceProvider.getPermissionAndRoleConfig();
                }
                permissionAndRoleConfig.setPermissions(permissionList.toArray(new ApplicationPermission[0]));
                serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);
            }
        } finally {
            ApplicationMgtUtil.endTenantFlow();
        }
    }

    /**
     * Validates the offset and limit values for pagination.
     *
     * @param offset Starting index.
     * @param limit  Count value.
     * @throws IdentityApplicationManagementException
     */
    private void validateAttributesForPagination(int offset, int limit) throws IdentityApplicationManagementException {

        if (offset < 0) {
            throw new IdentityApplicationManagementClientException(INVALID_OFFSET.getCode(),
                    "Invalid offset requested. Offset value should be zero or greater than zero.");
        }

        if (limit <= 0) {
            throw new IdentityApplicationManagementClientException(INVALID_LIMIT.getCode(),
                    "Invalid limit requested. Limit value should be greater than zero.");
        }
    }

    /**
     * Validates whether the requested page number for pagination is not zero or negative.
     *
     * @param pageNumber Page number.
     * @throws IdentityApplicationManagementException
     */
    private void validateRequestedPageNumber(int pageNumber) throws IdentityApplicationManagementException {

        // Validate whether the page number is not zero or a negative number.
        if (pageNumber < 1) {
            throw new IdentityApplicationManagementException("Invalid page number requested. The page number should "
                    + "be a value greater than 0.");
        }
    }

    private void validateForUnImplementedSortingAttributes(String sortOrder, String sortBy) throws
            IdentityApplicationManagementServerException {

        if (StringUtils.isNotBlank(sortBy) || StringUtils.isNotBlank(sortOrder)) {
            throw new IdentityApplicationManagementServerException(SORTING_NOT_IMPLEMENTED.getCode(),
                    "Sorting not supported.");
        }
    }

    @Override
    public ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting application basic information for resourceId: " + resourceId
                    + " in tenantDomain: " + tenantDomain);
        }

        ApplicationBasicInfo applicationBasicInfo = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection, LOAD_APP_BY_TENANT_AND_UUID)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.UUID, resourceId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfo = buildApplicationBasicInfo(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            String message = "Error while getting application basic information for resourceId: %s in " +
                    "tenantDomain: %s";
            throw new IdentityApplicationManagementException(String.format(message, resourceId, tenantDomain), e);
        }

        return applicationBasicInfo;
    }

    @Override
    public ApplicationBasicInfo getApplicationBasicInfoByName(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting application basic information for name: " + name
                    + " in tenantDomain: " + tenantDomain);
        }

        ApplicationBasicInfo applicationBasicInfo = null;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection, LOAD_APP_BY_TENANT_AND_NAME)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.APP_NAME, name);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfo = buildApplicationBasicInfo(resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            String message = "Error while getting application basic information for name: %s in " +
                    "tenantDomain: %s";
            throw new IdentityApplicationManagementException(String.format(message, name, tenantDomain), e);
        }
        return applicationBasicInfo;
    }

    public String addApplication(ServiceProvider application,
                                 String tenantDomain) throws IdentityApplicationManagementException {

        Connection connection = IdentityDatabaseUtil.getDBConnection(true);
        try {
            // Create basic application.
            ApplicationCreateResult result = persistBasicApplicationInformation(connection, application, tenantDomain);
            int applicationId = result.getApplicationId();
            String resourceId = result.getApplicationResourceId();

            if (log.isDebugEnabled()) {
                log.debug("Application with name: " + application.getApplicationName() + " in tenantDomain: "
                        + tenantDomain + " has been created with appId: " + applicationId + " and resourceId: "
                        + resourceId);
            }

            // Before calling update we set the appId and resourceId to the application.
            application.setApplicationID(applicationId);
            application.setApplicationResourceId(resourceId);

            addApplicationConfigurations(connection, application, tenantDomain);
            IdentityDatabaseUtil.commitTransaction(connection);
            return resourceId;
        } catch (SQLException | UserStoreException | IdentityApplicationManagementException e) {
            log.error("Error while creating the application with name: " + application.getApplicationName()
                    + " in tenantDomain: " + tenantDomain + ". Rolling back created application information.");
            IdentityDatabaseUtil.rollbackTransaction(connection);
            if (isApplicationConflict(e)) {
                throw new IdentityApplicationManagementClientException(APPLICATION_ALREADY_EXISTS.getCode(),
                        "Application already exists with name: " + application.getApplicationName()
                                + " in tenantDomain: " + tenantDomain);
            }
            throw new IdentityApplicationManagementException("Error while creating an application: "
                    + application.getApplicationName() + " in tenantDomain: " + tenantDomain, e);
        } finally {
            IdentityDatabaseUtil.closeConnection(connection);
        }
    }

    @Override
    public void updateApplicationByResourceId(String resourceId,
                                              String tenantDomain,
                                              ServiceProvider updatedApp)
            throws IdentityApplicationManagementException {

        try {
            int appIdUsingResourceId = getAppIdUsingResourceId(resourceId, tenantDomain);
            updatedApp.setApplicationID(appIdUsingResourceId);

            updateApplication(updatedApp, tenantDomain);
        } catch (IdentityApplicationManagementException ex) {
            // Send error code.
            throw new IdentityApplicationManagementServerException("Error while updating application with resourceId: "
                    + resourceId + " in tenantDomain: " + tenantDomain, ex);
        }
    }

    @Override
    public ServiceProvider getApplicationByResourceId(String resourceId,
                                                      String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            int appId = getAppIdUsingResourceId(resourceId, tenantDomain);
            ServiceProvider application = getApplication(appId);
            if (application == null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Cannot find an application for resourceId:" + resourceId +
                                    ", tenantDomain:" + tenantDomain);
                }
            }
            return application;
        } catch (IdentityApplicationManagementException ex) {
            throw new IdentityApplicationManagementServerException("Error while retrieving application with " +
                    "resourceId: " + resourceId + " in tenantDomain: " + tenantDomain, ex);
        }
    }

    @Override
    public void deleteApplicationByResourceId(String resourceId,
                                              String tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Deleting Application with resourceId: " + resourceId + " in tenantDomain: " + tenantDomain);
        }

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            ServiceProvider application = getApplicationByResourceId(resourceId, tenantDomain);

            if (application != null) {
                // Delete the application certificate if there is any
                deleteApplicationCertificate(connection, application);

                try (NamedPreparedStatement deleteAppStatement =
                             new NamedPreparedStatement(connection, REMOVE_APP_FROM_SP_APP_WITH_UUID)) {

                    deleteAppStatement.setString(ApplicationTableColumns.UUID, resourceId);
                    int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
                    deleteAppStatement.setInt(ApplicationTableColumns.TENANT_ID, tenantId);
                    deleteAppStatement.execute();

                    IdentityDatabaseUtil.commitTransaction(connection);
                } catch (SQLException ex) {
                    IdentityDatabaseUtil.rollbackTransaction(connection);
                    String msg = "Error occurred while deleting application with resourceId: %s in tenantDomain: %s.";
                    throw new IdentityApplicationManagementException(String.format(msg, resourceId, tenantDomain), ex);
                }
            } else {
                if (log.isDebugEnabled()) {
                    String msg = "Trying to delete a non-existing application with resourceId: %s in " +
                            "tenantDomain: %s.";
                    log.debug(String.format(msg, resourceId, tenantDomain));
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while deleting application with resourceId: %s in tenantDomain: %s.";
            throw new IdentityApplicationManagementException(String.format(msg, resourceId, tenantDomain), e);
        }
    }

    @Override
    public List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String filter,
                                                                          String sortOrder, String sortBy, String
                                                                                  tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Retrieving application basic information of discoverable applications for limit: " + limit +
                    " offset: " + offset + " filter: " + filter + " sortOrder: " + sortOrder + " sortBy: " + sortBy +
                    " in tenantDomain: " + tenantDomain);
        }

        validateForUnImplementedSortingAttributes(sortOrder, sortBy);
        validateAttributesForPagination(offset, limit);

        // TODO: 11/5/19 : Enforce a max limit
        if (StringUtils.isBlank(filter) || "*".equals(filter)) {
            return getDiscoverableApplicationBasicInfo(limit, offset, tenantDomain);
        }

        String filterResolvedForSQL = resolveSQLFilter(filter);

        List<ApplicationBasicInfo> applicationBasicInfoList = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String databaseVendorType = connection.getMetaData().getDatabaseProductName();

            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection,
                                 getDBVendorSpecificDiscoverableAppRetrievalQueryByAppName(databaseVendorType))) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.APP_NAME, filterResolvedForSQL);
                statement.setInt(ApplicationConstants.OFFSET, offset);
                statement.setInt(ApplicationConstants.LIMIT, limit);
                statement.setInt(ApplicationConstants.ZERO_BASED_START_INDEX, offset);
                statement.setInt(ApplicationConstants.ONE_BASED_START_INDEX, offset + 1);
                statement.setInt(ApplicationConstants.END_INDEX, offset + limit);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfoList.add(buildApplicationBasicInfo(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting application basic information" +
                    " for discoverable applications in tenantDomain: " + tenantDomain, e);
        }

        return Collections.unmodifiableList(applicationBasicInfoList);
    }

    @Override
    public int getCountOfDiscoverableApplications(String filter, String tenantDomain) throws
            IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting count of discoverable applications matching filter: " + filter + " in tenantDomain: "
                    + tenantDomain);
        }

        if (StringUtils.isBlank(filter) || "*".equals(filter)) {
            return getCountOfDiscoverableApplications(tenantDomain);
        }

        int count = 0;
        String filterResolvedForSQL = resolveSQLFilter(filter);
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection, LOAD_DISCOVERABLE_APP_COUNT_BY_APP_NAME_AND_TENANT)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.APP_NAME, filterResolvedForSQL);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting count of discoverable " +
                    "applications matching filter:" + filter + " in tenantDomain: " + tenantDomain, e);
        }

        return count;
    }

    @Override
    public ApplicationBasicInfo getDiscoverableApplicationBasicInfoByResourceId(String resourceId, String
            tenantDomain) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Getting application basic information for resourceId: " + resourceId
                    + " in tenantDomain: " + tenantDomain + " if discoverable.");
        }

        ApplicationBasicInfo applicationBasicInfo = null;
        boolean isDiscoverable = false;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    LOAD_APP_BY_TENANT_AND_UUID)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.UUID, resourceId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfo = buildApplicationBasicInfo(resultSet);
                        isDiscoverable = getBooleanValue(resultSet.getString(ApplicationTableColumns.IS_DISCOVERABLE));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting discoverable application " +
                    "basic information for resourceId: " + resourceId + " in tenantDomain: " + tenantDomain, e);
        }

        if (applicationBasicInfo != null && !isDiscoverable) {
            throw new IdentityApplicationManagementClientException(APPLICATION_NOT_DISCOVERABLE.getCode(),
                    "Requested application resource " + resourceId + " is not discoverable.");
        }

        return applicationBasicInfo;

    }

    @Override
    public boolean isApplicationDiscoverable(String resourceId, String tenantDomain) throws
            IdentityApplicationManagementException {

        int count = 0;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    IS_APP_BY_TENANT_AND_UUID_DISCOVERABLE)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setString(ApplicationTableColumns.UUID, resourceId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        count = resultSet.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting discoverable application " +
                    "basic information for resourceId: " + resourceId + " in tenantDomain: " + tenantDomain, e);
        }
        return count > 0;
    }

    @Override
    public boolean isClaimReferredByAnySp(Connection dbConnection, String claimUri, int tenantId)
            throws IdentityApplicationManagementException {

        boolean dbConnInitialized = true;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isClaimReferred = false;
        if (dbConnection == null) {
            dbConnection = IdentityDatabaseUtil.getDBConnection(false);
        } else {
            dbConnInitialized = false;
        }

        try {
            String sqlStmt = ApplicationMgtDBQueries.GET_TOTAL_SP_CLAIM_USAGES;
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setInt(1, tenantId);
            prepStmt.setString(2, claimUri);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                isClaimReferred = rs.getInt(1) > 0;
            }
            return isClaimReferred;
        } catch (SQLException e) {
            throw new IdentityApplicationManagementException("Error occurred while retrieving application usages of " +
                    "the claim " + claimUri, e);
        } finally {
            if (dbConnInitialized) {
                IdentityDatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            } else {
                IdentityDatabaseUtil.closeAllConnections(null, rs, prepStmt);
            }
        }
    }

    private List<ApplicationBasicInfo> getDiscoverableApplicationBasicInfo(int limit, int offset, String
            tenantDomain) throws IdentityApplicationManagementException {

        List<ApplicationBasicInfo> applicationBasicInfoList = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            String databaseVendorType = connection.getMetaData().getDatabaseProductName();

            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection,
                                 getDBVendorSpecificDiscoverableAppRetrievalQuery(databaseVendorType))) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));
                statement.setInt(ApplicationConstants.OFFSET, offset);
                statement.setInt(ApplicationConstants.LIMIT, limit);
                statement.setInt(ApplicationConstants.ZERO_BASED_START_INDEX, offset);
                statement.setInt(ApplicationConstants.ONE_BASED_START_INDEX, offset + 1);
                statement.setInt(ApplicationConstants.END_INDEX, offset + limit);

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        applicationBasicInfoList.add(buildApplicationBasicInfo(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting application basic information" +
                    " for discoverable applications in tenantDomain: " + tenantDomain, e);
        }

        return Collections.unmodifiableList(applicationBasicInfoList);
    }

    private int getCountOfDiscoverableApplications(String tenantDomain) throws IdentityApplicationManagementException {

        int count;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            try (NamedPreparedStatement statement =
                         new NamedPreparedStatement(connection, LOAD_DISCOVERABLE_APP_COUNT_BY_TENANT)) {
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));

                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new IdentityApplicationManagementServerException("Error while getting count of discoverable " +
                    "applications in tenantDomain: " + tenantDomain, e);
        }

        return count;
    }

    private String getDBVendorSpecificDiscoverableAppRetrievalQueryByAppName(String dbVendorType) throws
            IdentityApplicationManagementException {

        if ("MySQL".equals(dbVendorType)
                || "MariaDB".equals(dbVendorType)
                || "H2".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_MYSQL;
        } else if ("Oracle".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_ORACLE;
        } else if ("Microsoft SQL Server".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_MSSQL;
        } else if ("PostgreSQL".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_POSTGRESQL;
        } else if (dbVendorType != null && dbVendorType.contains("DB2")) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_DB2;
        } else if ("INFORMIX".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_AND_APP_NAME_INFORMIX;
        }

        throw new IdentityApplicationManagementException("Error while loading discoverable applications from " +
                "DB. Database driver for " + dbVendorType + "could not be identified or not supported.");
    }

    private String getDBVendorSpecificDiscoverableAppRetrievalQuery(String dbVendorType) throws
            IdentityApplicationManagementException {

        if ("MySQL".equals(dbVendorType)
                || "MariaDB".equals(dbVendorType)
                || "H2".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_MYSQL;
        } else if ("Oracle".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_ORACLE;
        } else if ("Microsoft SQL Server".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_MSSQL;
        } else if ("PostgreSQL".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_POSTGRESQL;
        } else if (dbVendorType != null && dbVendorType.contains("DB2")) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_DB2SQL;
        } else if ("INFORMIX".equals(dbVendorType)) {
            return LOAD_DISCOVERABLE_APPS_BY_TENANT_INFORMIX;
        }

        throw new IdentityApplicationManagementException("Error while loading discoverable applications from " +
                "DB. Database driver for " + dbVendorType + "could not be identified or not supported.");
    }

    private ApplicationBasicInfo buildApplicationBasicInfo(ResultSet appNameResultSet)
            throws SQLException {

        ApplicationBasicInfo basicInfo = new ApplicationBasicInfo();
        basicInfo.setApplicationId(appNameResultSet.getInt(ApplicationTableColumns.ID));
        basicInfo.setApplicationName(appNameResultSet.getString(ApplicationTableColumns.APP_NAME));
        basicInfo.setDescription(appNameResultSet.getString(ApplicationTableColumns.DESCRIPTION));

        basicInfo.setApplicationResourceId(appNameResultSet.getString(ApplicationTableColumns.UUID));
        basicInfo.setImageUrl(appNameResultSet.getString(ApplicationTableColumns.IMAGE_URL));
        basicInfo.setAccessUrl(appNameResultSet.getString(ApplicationTableColumns.ACCESS_URL));

        String username = appNameResultSet.getString(ApplicationTableColumns.USERNAME);
        String userStoreDomain = appNameResultSet.getString(ApplicationTableColumns.USER_STORE);
        int tenantId = appNameResultSet.getInt(ApplicationTableColumns.TENANT_ID);

        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(userStoreDomain)
                && !(tenantId == MultitenantConstants.INVALID_TENANT_ID)) {
            User appOwner = new User();
            appOwner.setUserStoreDomain(userStoreDomain);
            appOwner.setUserName(username);
            appOwner.setTenantDomain(IdentityTenantUtil.getTenantDomain(tenantId));

            basicInfo.setAppOwner(appOwner);
        }

        return basicInfo;
    }

    /**
     * Returns the internal application id for a given resourceId in a tenant.
     *
     * @param resourceId
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    private int getAppIdUsingResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        int applicationId = 0;
        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, LOAD_APP_ID_BY_UUID)) {

                statement.setString(ApplicationTableColumns.UUID, resourceId);
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        applicationId = resultSet.getInt(ApplicationTableColumns.ID);
                    }
                }
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the application id for resourceId: %s in tenantDomain:  %s";
            throw new IdentityApplicationManagementException(String.format(msg, resourceId, tenantDomain), e);
        }

        return applicationId;
    }

    /**
     * Returns the internal application id for a given resourceId in a tenant.
     *
     * @param appId        Internal Application ID
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    private String getResourceIdUsingAppId(int appId,
                                           String tenantDomain) throws IdentityApplicationManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {

            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, LOAD_UUID_BY_APP_ID)) {

                statement.setInt(ApplicationTableColumns.ID, appId);
                statement.setInt(ApplicationTableColumns.TENANT_ID, IdentityTenantUtil.getTenantId(tenantDomain));

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(ApplicationTableColumns.UUID);
                    } else {
                        String msg = "Cannot find the application resourceId for appId: %s in tenantDomain: %s";
                        throw new IdentityApplicationManagementException(String.format(msg, appId, tenantDomain));
                    }
                }
            }

        } catch (SQLException e) {
            String msg = "Error while retrieving the application resourceId for appId: %s in tenantDomain: %s";
            throw new IdentityApplicationManagementException(String.format(msg, appId, tenantDomain), e);
        }
    }

    private void deleteApplicationCertificate(Connection connection, ServiceProvider application) throws SQLException {

        String certificateReferenceID = getCertificateReferenceID(application.getSpProperties());
        if (certificateReferenceID != null) {
            deleteCertificate(connection, Integer.parseInt(certificateReferenceID));
        }
    }

    private String generateApplicationResourceId(ServiceProvider serviceProvider) {

        return UUID.randomUUID().toString();
    }

    /**
     * Container for passing the two uniques ids related to application creation.
     */
    private static class ApplicationCreateResult {

        private String applicationResourceId;

        private int applicationId;

        ApplicationCreateResult(String applicationResourceId, int applicationId) {

            this.applicationResourceId = applicationResourceId;
            this.applicationId = applicationId;
        }

        String getApplicationResourceId() {

            return applicationResourceId;
        }

        int getApplicationId() {

            return applicationId;
        }
    }

    private boolean isApplicationAlreadyExistsError(IdentityApplicationManagementException ex) {

        return ex instanceof IdentityApplicationRegistrationFailureException
                && APPLICATION_ALREADY_EXISTS.getCode().contains(ex.getErrorCode());
    }

    private int createServiceProvider(String tenantDomain, ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        int applicationId;
        try {
            applicationId = createApplication(serviceProvider, tenantDomain);
        } catch (IdentityApplicationManagementException e) {
            if (!isApplicationAlreadyExistsError(e)) {
                throw new IdentityApplicationManagementException("Failed to retrieve application: "
                        + serviceProvider.getApplicationName() + " in tenantDomain: " + tenantDomain, e);
            }

            // Although application does not exists at this point, it could already be created by the time where db
            // queries are committed, if concurrent requests were made in-between.
            if (log.isDebugEnabled()) {
                log.debug("The service provider: " + serviceProvider.getApplicationName() + ", tried " +
                        "to create, is already exists. Therefore, this duplication attempt error is ignored and " +
                        "existing application id is used", e);
            }
            applicationId = getApplicationIdByName(serviceProvider.getApplicationName(), tenantDomain);
        }
        return applicationId;
    }

    /**
     * Add audit log entry.
     *
     * @param action The action of the log.
     * @param data   Data of the action to log.
     * @param result The success of fail state of the action.
     */
    private void audit(String action, String data, String result) {

        String loggedInUser = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isBlank(loggedInUser)) {
            loggedInUser = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        loggedInUser = UserCoreUtil.addTenantDomainToEntry(loggedInUser, tenantDomain);

        AUDIT_LOG.info(String.format(AUDIT_MESSAGE, loggedInUser, action, data, result));
    }
}
