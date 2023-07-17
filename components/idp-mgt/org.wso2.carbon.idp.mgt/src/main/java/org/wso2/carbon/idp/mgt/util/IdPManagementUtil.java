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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;
import org.wso2.carbon.identity.application.common.processors.RandomPasswordProcessor;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.PRESERVE_LOCALLY_ADDED_CLAIMS;

public class IdPManagementUtil {

    private static final Log log = LogFactory.getLog(IdPManagementUtil.class);

    private static String tenantContext;
    private static String tenantParameter;

    /**
     * Get the tenant id of the given tenant domain.
     *
     * @param tenantDomain Tenant Domain
     * @return Tenant Id of domain user belongs to.
     * @throws UserStoreException Error when getting tenant id from tenant domain
     */
    public static int getTenantIdOfDomain(String tenantDomain) throws UserStoreException {

        if (tenantDomain != null) {
            TenantManager tenantManager = IdPManagementServiceComponent.getRealmService()
                    .getTenantManager();
            int tenantId = tenantManager.getTenantId(tenantDomain);
            return tenantId;
        } else {
            log.debug("Invalid tenant domain: \'NULL\'");
            throw new IllegalArgumentException("Invalid tenant domain: \'NULL\'");
        }
    }

    /**
     +     * Get the resident entity id configured in identity.xml.
     +     *
     +     */
    public static String getResidentIdPEntityId() {
        String localEntityId = IdentityUtil.getProperty("SSOService.EntityId");
            if (localEntityId == null || localEntityId.trim().isEmpty()) {
                localEntityId = "localhost";
            }
        return localEntityId;
    }

    /**
     * Check whether the preserve locally added claims config is enabled for the Jit provisioned users.
     * If the config is false this will keep the current default behavior. So it Deletes the existing local claims that
     * are not coming in the federated login after the provisioning.
     * If the above config is true this will preserve the locally added claims of Jit provisioned users. This will stop
     * deleting the attributes that are not coming in the federated login after the provisioning.
     *
     * @return true if the preserve locally added claim config is enabled, else return false.
     */
    public static boolean isPreserveLocallyAddedClaims() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(PRESERVE_LOCALLY_ADDED_CLAIMS));
    }

    public static int getIdleSessionTimeOut(String tenantDomain) {

        IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();
        int timeout = Integer.parseInt(IdentityApplicationConstants.SESSION_IDLE_TIME_OUT_DEFAULT);

        try {
            IdentityProvider identityProvider = identityProviderManager.getResidentIdP(tenantDomain);
            IdentityProviderProperty idpProperty = IdentityApplicationManagementUtil.getProperty(
                    identityProvider.getIdpProperties(), IdentityApplicationConstants.SESSION_IDLE_TIME_OUT);
            if (idpProperty != null) {
                timeout = Integer.parseInt(idpProperty.getValue());
            }
        } catch (IdentityProviderManagementException e) {
            log.error("Error when accessing the IdentityProviderManager for tenant : " + tenantDomain, e);
        }
        return timeout * 60;
    }

    public static int getRememberMeTimeout(String tenantDomain) {

        IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();
        int rememberMeTimeout = Integer.parseInt(IdentityApplicationConstants.REMEMBER_ME_TIME_OUT_DEFAULT);

        try {
            IdentityProvider identityProvider = identityProviderManager.getResidentIdP(tenantDomain);
            IdentityProviderProperty idpProperty = IdentityApplicationManagementUtil.getProperty(
                    identityProvider.getIdpProperties(), IdentityApplicationConstants.REMEMBER_ME_TIME_OUT);
            if (idpProperty != null) {
                rememberMeTimeout = Integer.parseInt(idpProperty.getValue());
            }
        } catch (IdentityProviderManagementException e) {
            log.error("Error when accessing the IdentityProviderManager for tenant : " + tenantDomain, e);
        }
        return rememberMeTimeout * 60;
    }

    /**
     * Use this method to replace original passwords with random passwords before sending to UI front-end
     * @param identityProvider
     * @return
     */
    public static void removeOriginalPasswords(IdentityProvider identityProvider) {

        if (identityProvider == null || identityProvider.getProvisioningConnectorConfigs() == null) {
            return;
        }

        for (ProvisioningConnectorConfig provisioningConnectorConfig : identityProvider
                .getProvisioningConnectorConfigs()) {
            Property[] properties = provisioningConnectorConfig.getProvisioningProperties();
            if (ArrayUtils.isEmpty(properties)) {
                continue;
            }
            properties = RandomPasswordProcessor.getInstance().removeOriginalPasswords(properties);
            provisioningConnectorConfig.setProvisioningProperties(properties);
        }
    }

    /**
     * Use this method to replace random passwords with original passwords when original passwords are required  
     * @param identityProvider
     * @param withCacheClear
     */
    public static void removeRandomPasswords(IdentityProvider identityProvider, boolean withCacheClear) {

        if (identityProvider == null || identityProvider.getProvisioningConnectorConfigs() == null) {
            return;
        }
        for (ProvisioningConnectorConfig provisioningConnectorConfig : identityProvider
                .getProvisioningConnectorConfigs()) {
            Property[] properties = provisioningConnectorConfig.getProvisioningProperties();
            if (ArrayUtils.isEmpty(properties)) {
                continue;
            }
            properties = RandomPasswordProcessor.getInstance().removeRandomPasswords(properties, withCacheClear);
            provisioningConnectorConfig.setProvisioningProperties(properties);
        }
    }


    /**
     * Set tenantContext and tenantParameter specific to the tenant domain.
     *
     * @deprecated Setting tenant context and tenant parameter in static method will replace already set value with
     * new value for two concurrent logins in different tenant domains.
     * Can use local parameters to resolve this.
     * @param tenantDomain of requested resident IdP
     */
    @Deprecated
    public static void setTenantSpecifiers(String tenantDomain) {

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain)) {
            tenantContext = MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/";
            tenantParameter = "?" + MultitenantConstants.TENANT_DOMAIN + "=" + tenantDomain;
        } else {
            tenantContext = "";
            tenantParameter = "";
        }
    }

    /**
     * Get the tenant context specific to the resident IdP tenant domain.
     *
     * @deprecated Setter is deprecated.
     * @return the tenantContext
     */
    @Deprecated
    public static String getTenantContext() {

        return tenantContext;
    }

    /**
     * Get the tenant parameter specific to the resident IdP tenant domain to be appended with the endpoint URL.
     *
     * @deprecated Setter is deprecated.
     * @return the tenantParameter
     */
    @Deprecated
    public static String getTenantParameter() {

        return tenantParameter;
    }

    /**
     * This method can be used to generate a IdentityProviderManagementClientException from
     * IdPManagementConstants.ErrorMessage object when no exception is thrown.
     *
     * @param error IdPManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return IdentityProviderManagementClientException.
     */
    public static IdentityProviderManagementClientException handleClientException(IdPManagementConstants.ErrorMessage
                                                                                          error, String data) {

        String message = includeData(error, data);
        return new IdentityProviderManagementClientException(error.getCode(), message);
    }

    public static IdentityProviderManagementClientException handleClientException(IdPManagementConstants.ErrorMessage
                                                                                          error, String data,
                                                                                  Throwable e) {

        String message = includeData(error, data);
        return new IdentityProviderManagementClientException(error.getCode(), message, e);
    }

    /**
     * This method can be used to generate a IdentityProviderManagementServerException from
     * IdPManagementConstants.ErrorMessage object when no exception is thrown.
     *
     * @param error IdPManagementConstants.ErrorMessage.
     * @param data  data to replace if message needs to be replaced.
     * @return IdentityProviderManagementServerException.
     */
    public static IdentityProviderManagementServerException handleServerException(IdPManagementConstants.ErrorMessage
                                                                                      error, String data) {

        String message = includeData(error, data);
        return new IdentityProviderManagementServerException(error.getCode(), message);
    }

    public static IdentityProviderManagementServerException handleServerException(IdPManagementConstants.ErrorMessage error,
                                                                               String data, Throwable e) {

        String message = includeData(error, data);
        return new IdentityProviderManagementServerException(error.getCode(), message, e);
    }

    private static String includeData(IdPManagementConstants.ErrorMessage error, String data) {

        String message;
        if (StringUtils.isNotBlank(data)) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }
}
