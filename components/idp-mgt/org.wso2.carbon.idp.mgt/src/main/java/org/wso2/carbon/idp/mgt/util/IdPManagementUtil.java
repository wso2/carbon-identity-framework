/*
 * Copyright (c) 2014-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementClientException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementServerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.idp.mgt.dao.IdPManagementDAO;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.identity.base.IdentityConstants.ServerConfig.PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ASK_PASSWORD_SEND_EMAIL_OTP;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ASK_PASSWORD_SEND_SMS_OTP;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.NOTIFICATION_PASSWORD_ENABLE_PROPERTY;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.PRESERVE_LOCALLY_ADDED_CLAIMS;
import static org.wso2.carbon.idp.mgt.util.IdPManagementConstants.SMS_OTP_PASSWORD_RECOVERY_PROPERTY;

public class IdPManagementUtil {

    private static final Log log = LogFactory.getLog(IdPManagementUtil.class);
    private static final CacheBackedIdPMgtDAO CACHE_BACKED_IDP_MGT_DAO =
            new CacheBackedIdPMgtDAO(new IdPManagementDAO());

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

    public static boolean getPreserveCurrentSessionAtPasswordUpdate(String tenantDomain) {

        IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();
        boolean preserveSessionAtPasswordUpdate = Boolean.parseBoolean(IdentityUtil.getProperty(
                PRESERVE_LOGGED_IN_SESSION_AT_PASSWORD_UPDATE));

        try {
            IdentityProvider identityProvider = identityProviderManager.getResidentIdP(tenantDomain);
            IdentityProviderProperty idpProperty = IdentityApplicationManagementUtil.getProperty(
                    identityProvider.getIdpProperties(),
                    IdentityApplicationConstants.PRESERVE_CURRENT_SESSION_AT_PASSWORD_UPDATE);
            if (idpProperty != null) {
                preserveSessionAtPasswordUpdate = Boolean.parseBoolean(idpProperty.getValue());
            }
        } catch (IdentityProviderManagementException e) {
            log.error("Error when accessing the IdentityProviderManager for tenant : " + tenantDomain, e);
        }
        return preserveSessionAtPasswordUpdate;
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
     * Utility method to clear the cache for a specific identity provider
     *
     * @param idpName      Name of the Identity Provider.
     * @param tenantDomain Tenant Domain of the Identity Provider.
     */
    public static void clearIdPCache(String idpName, String tenantDomain) {

        try {
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            CACHE_BACKED_IDP_MGT_DAO.clearIdpCache(idpName, tenantId, tenantDomain);
        } catch (IdentityProviderManagementException | IdentityRuntimeException e) {
            log.error("Error while clearing the cache for the Identity Provider: " + idpName + " in tenant: "
                    + tenantDomain, e);
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

    /**
     * This method is used to validate the password recovery property values.
     *
     * @param configurationDetails Configuration updates for governance configuration.
     */
    public static void validatePasswordRecoveryPropertyValues(Map<String, String> configurationDetails)
            throws IdentityProviderManagementClientException {

        if (configurationDetails.containsKey(NOTIFICATION_PASSWORD_ENABLE_PROPERTY) ||
                configurationDetails.containsKey(EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY) ||
                configurationDetails.containsKey(EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY) ||
                configurationDetails.containsKey(SMS_OTP_PASSWORD_RECOVERY_PROPERTY)) {
            // Perform process only if notification based password recovery connector or options are updated.
            String recoveryNotificationPasswordProp = configurationDetails.get(NOTIFICATION_PASSWORD_ENABLE_PROPERTY);
            String emailLinkForPasswordRecoveryProp = configurationDetails.get(EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
            String emailOtpForPasswordRecoveryProp = configurationDetails.get(EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY);
            String smsOtpForPasswordRecoveryProp = configurationDetails.get(SMS_OTP_PASSWORD_RECOVERY_PROPERTY);

            boolean isRecoveryNotificationPasswordEnabled = Boolean.parseBoolean(recoveryNotificationPasswordProp);
            boolean isEmailLinkPasswordRecoveryEnabled = Boolean.parseBoolean(emailLinkForPasswordRecoveryProp);
            boolean isEmailOtpPasswordRecoveryEnabled = Boolean.parseBoolean(emailOtpForPasswordRecoveryProp);
            boolean isSmsOtpPasswordRecoveryEnabled = Boolean.parseBoolean(smsOtpForPasswordRecoveryProp);

            if (isRecoveryNotificationPasswordEnabled &&
                    StringUtils.isNotBlank(emailLinkForPasswordRecoveryProp) && !isEmailLinkPasswordRecoveryEnabled &&
                    StringUtils.isNotBlank(emailOtpForPasswordRecoveryProp) && !isEmailOtpPasswordRecoveryEnabled &&
                    StringUtils.isNotBlank(smsOtpForPasswordRecoveryProp) && !isSmsOtpPasswordRecoveryEnabled) {
                // Disabling all recovery options when recovery connector is enabled is not allowed.
                // WARNING : Be mindful about compatibility of earlier recovery api versions when changing
                // this behaviour.
                throw IdPManagementUtil
                        .handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                                "Disabling all recovery options when recovery connector is enabled, is not allowed.");
            }
            if (StringUtils.isNotBlank(recoveryNotificationPasswordProp) && !isRecoveryNotificationPasswordEnabled &&
                    (isEmailLinkPasswordRecoveryEnabled || isSmsOtpPasswordRecoveryEnabled ||
                            isEmailOtpPasswordRecoveryEnabled)) {
                // Enabling any recovery options when connector is disabled is not allowed.
                // WARNING : Be mindful about compatibility of earlier recovery api versions when changing
                // this behaviour.
                throw IdPManagementUtil
                        .handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                                "Enabling recovery options when connector is disabled, is not allowed.");
            }
            if (isEmailLinkPasswordRecoveryEnabled && isEmailOtpPasswordRecoveryEnabled) {
                throw IdPManagementUtil.handleClientException(
                        IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                        "Enabling both email link and email otp options are not allowed.");
            }
        }
    }

    public static void validatePasswordRecoveryWithCurrentAndPreviousConfigs(Map<String, String> configurationDetails,
                                                                   IdentityProviderProperty[] identityMgtProperties)
            throws IdentityProviderManagementClientException {

        // Validate updating configs.
        validatePasswordRecoveryPropertyValues(configurationDetails);


        // Check weather current configurations include email OTP or Link since enabling both at same time is not
        // allowed.
        if (configurationDetails.containsKey(EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY) ||
                configurationDetails.containsKey(EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY)) {

            String emailLinkForPasswordRecoveryProp = configurationDetails.get(EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY);
            String emailOtpForPasswordRecoveryProp = configurationDetails.get(EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY);

            boolean isEmailLinkPasswordRecoveryEnabled = Boolean.parseBoolean(emailLinkForPasswordRecoveryProp);
            boolean isEmailOtpPasswordRecoveryEnabled = Boolean.parseBoolean(emailOtpForPasswordRecoveryProp);

            // Checks for already existing configurations.
            boolean isEmailLinkCurrentlyEnabled = false;
            boolean isEmailOtpCurrentlyEnabled = false;

            for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
                if (EMAIL_LINK_PASSWORD_RECOVERY_PROPERTY.equals(identityMgtProperty.getName())) {
                    isEmailLinkCurrentlyEnabled = Boolean.parseBoolean(identityMgtProperty.getValue());
                } else if (EMAIL_OTP_PASSWORD_RECOVERY_PROPERTY.equals(identityMgtProperty.getName())) {
                    isEmailOtpCurrentlyEnabled = Boolean.parseBoolean(identityMgtProperty.getValue());
                }
            }

            if (((isEmailLinkCurrentlyEnabled && StringUtils.isBlank(emailLinkForPasswordRecoveryProp)) ||
                    isEmailLinkPasswordRecoveryEnabled) && isEmailOtpPasswordRecoveryEnabled) {
                throw IdPManagementUtil.handleClientException(
                        IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                        "Enabling email OTP while email link is enabled is not allowed.");
            }
            if (((isEmailOtpCurrentlyEnabled && StringUtils.isBlank(emailOtpForPasswordRecoveryProp)) ||
                    isEmailOtpPasswordRecoveryEnabled) && isEmailLinkPasswordRecoveryEnabled) {
                throw IdPManagementUtil.handleClientException(
                        IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                        "Enabling email link while email OTP is enabled is not allowed.");
            }
        }
    }

    /**
     * This method is used to validate the username recovery related property values.
     *
     * @param configurationDetails Configuration updates for governance configuration
     * @throws IdentityProviderManagementClientException if configurations contain invalid configurations.
     */
    public static void validateUsernameRecoveryPropertyValues(Map<String, String> configurationDetails)
            throws IdentityProviderManagementClientException {

        if (configurationDetails.containsKey(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY) ||
                configurationDetails.containsKey(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY) ||
                configurationDetails.containsKey(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY)) {
            // Perform process only if notification based username recovery connector or options are updated.
            String usernameRecoveryProp = configurationDetails.get(IdPManagementConstants.USERNAME_RECOVERY_PROPERTY);
            String usernameRecoveryEmailProp =
                    configurationDetails.get(IdPManagementConstants.EMAIL_USERNAME_RECOVERY_PROPERTY);
            String usernameRecoverySmsProp =
                    configurationDetails.get(IdPManagementConstants.SMS_USERNAME_RECOVERY_PROPERTY);

            boolean usernameRecoveryProperty = Boolean.parseBoolean(usernameRecoveryProp);
            boolean usernameRecoveryEmailProperty = Boolean.parseBoolean(usernameRecoveryEmailProp);
            boolean usernameRecoverySmsProperty = Boolean.parseBoolean(usernameRecoverySmsProp);

            if (usernameRecoveryProperty &&
                    !usernameRecoveryEmailProperty && StringUtils.isNotBlank(usernameRecoveryEmailProp) &&
                    !usernameRecoverySmsProperty && StringUtils.isNotBlank(usernameRecoverySmsProp)) {
                /*
                 Disabling all recovery options when recovery connector is enabled is not allowed.
                 WARNING : Be mindful about compatibility of earlier recovery api versions when changing this behaviour.
                 */
                throw IdPManagementUtil
                        .handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                                "Disabling all recovery options when recovery connector is enabled, is not allowed.");

            }
            if (StringUtils.isNotBlank(usernameRecoveryProp) && !usernameRecoveryProperty &&
                    (usernameRecoveryEmailProperty || usernameRecoverySmsProperty)) {
                /*
                 Enabling any recovery options when connector is disabled is not allowed.
                 WARNING : Be mindful about compatibility of earlier recovery api versions when changing this behaviour.
                 */

                throw IdPManagementUtil
                        .handleClientException(
                                IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                                "Enabling recovery options when connector is disabled, is not allowed.");
            }
        }
    }

    /**
     * This method validates the forced password related configs with current and previous configurations.
     *
     * @param configurationDetails  Configuration updates for governance configurations.
     * @param identityMgtProperties Existing identity provider properties.
     * @throws IdentityProviderManagementClientException When invalid configurations have passed.
     */
    public static void validateAdminPasswordResetWithCurrentAndPreviousConfigs(
            Map<String, String> configurationDetails,
            IdentityProviderProperty[] identityMgtProperties)
            throws IdentityProviderManagementClientException {

        if (configurationDetails.containsKey(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY) ||
                configurationDetails.containsKey(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY) ||
                configurationDetails.containsKey(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY) ||
                configurationDetails.containsKey(ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY)) {

            String adminPasswordResetOfflineProp =
                    configurationDetails.get(ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY);
            String adminPasswordResetEmailOtpProp =
                    configurationDetails.get(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY);
            String adminPasswordResetEmailLinkProp =
                    configurationDetails.get(ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY);
            String adminPasswordResetSmsOtpProp =
                    configurationDetails.get(ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY);

            boolean isAdminPasswordResetOfflineEnabled = Boolean.parseBoolean(adminPasswordResetOfflineProp);
            boolean isAdminPasswordResetEmailOtpEnabled = Boolean.parseBoolean(adminPasswordResetEmailOtpProp);
            boolean isAdminPasswordResetEmailLinkEnabled = Boolean.parseBoolean(adminPasswordResetEmailLinkProp);
            boolean isAdminPasswordResetSmsOtpEnabled = Boolean.parseBoolean(adminPasswordResetSmsOtpProp);

            validateAdminPasswordResetCurrentConfigs(isAdminPasswordResetOfflineEnabled,
                    isAdminPasswordResetEmailOtpEnabled, isAdminPasswordResetEmailLinkEnabled,
                    isAdminPasswordResetSmsOtpEnabled);

            validateAdminPasswordResetWithExistingConfigs(identityMgtProperties,
                    isAdminPasswordResetOfflineEnabled, adminPasswordResetOfflineProp,
                    isAdminPasswordResetEmailOtpEnabled, adminPasswordResetEmailOtpProp,
                    isAdminPasswordResetEmailLinkEnabled, adminPasswordResetEmailLinkProp,
                    isAdminPasswordResetSmsOtpEnabled, adminPasswordResetSmsOtpProp);
        }
    }

    /**
     * This method validates the ask password related configs with current and previous configurations.
     *
     * @param configurationDetails  Configuration updates for governance configurations.
     * @param identityMgtProperties Existing identity provider properties.
     * @throws IdentityProviderManagementClientException When invalid configurations have passed.
     */
    public static void validateAskPasswordBasedPasswordSetWithCurrentAndPreviousConfigs(
            Map<String, String> configurationDetails,
            IdentityProviderProperty[] identityMgtProperties)
            throws IdentityProviderManagementClientException {

        if (configurationDetails.containsKey(ASK_PASSWORD_SEND_EMAIL_OTP) ||
                configurationDetails.containsKey(ASK_PASSWORD_SEND_SMS_OTP)) {

            boolean isAskPasswordEmailOTPEnabled = Boolean.parseBoolean(configurationDetails
                    .get(ASK_PASSWORD_SEND_EMAIL_OTP));
            String askPasswordEmailOTPProperty = configurationDetails.get(ASK_PASSWORD_SEND_EMAIL_OTP);

            boolean isAskPasswordSMSOTPEnabled = Boolean.parseBoolean(configurationDetails
                    .get(ASK_PASSWORD_SEND_SMS_OTP));
            String askPasswordSMSOTP = configurationDetails.get(ASK_PASSWORD_SEND_SMS_OTP);

            validateAskPasswordCurrentConfigs(isAskPasswordEmailOTPEnabled, isAskPasswordSMSOTPEnabled);
            validateAskPasswordWithExistingConfigs(identityMgtProperties, askPasswordEmailOTPProperty,
                    askPasswordSMSOTP);
        }
    }

    /**
     * This method is used to validate user enabling multiple admin password reset options at the same time.
     *
     * @param isAdminPasswordResetOfflineEnabled is admin password reset offline enabled.
     * @param isAdminPasswordResetEmailOtpEnabled is admin password reset email OTP enabled.
     * @param isAdminPasswordResetEmailLinkEnabled is admin password reset email link enabled.
     * @param isAdminPasswordResetSmsOtpEnabled is admin password reset sms OTP enabled.
     * @throws IdentityProviderManagementClientException when more than one ask password reset option is enabled.
     */
    private static void validateAdminPasswordResetCurrentConfigs(boolean isAdminPasswordResetOfflineEnabled,
                                                                 boolean isAdminPasswordResetEmailOtpEnabled,
                                                                 boolean isAdminPasswordResetEmailLinkEnabled,
                                                                 boolean isAdminPasswordResetSmsOtpEnabled)
            throws IdentityProviderManagementClientException {

        List<Boolean> configs = Arrays.asList(isAdminPasswordResetOfflineEnabled,
                isAdminPasswordResetEmailOtpEnabled, isAdminPasswordResetEmailLinkEnabled,
                isAdminPasswordResetSmsOtpEnabled);

        long enabledConfigCount = configs.stream().filter(Boolean::booleanValue).count();

        if (enabledConfigCount > 1) {
            throw IdPManagementUtil.handleClientException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                    "Enabling more than one admin password reset option is not allowed");
        }
    }

    private static void validateAdminPasswordResetWithExistingConfigs(IdentityProviderProperty[] identityMgtProperties,
                                                                      boolean isAdminPasswordResetOfflineEnabled,
                                                                      String adminPasswordResetOfflineProp,
                                                                      boolean isAdminPasswordResetEmailOtpEnabled,
                                                                      String adminPasswordResetEmailOtpProp,
                                                                      boolean isAdminPasswordResetEmailLinkEnabled,
                                                                      String adminPasswordResetEmailLinkProp,
                                                                      boolean isAdminPasswordResetSmsOtpEnabled,
                                                                      String adminPasswordResetSmsOtpProp)
            throws IdentityProviderManagementClientException {

        boolean isAdminPasswordResetOfflineCurrentlyEnabled = false;
        boolean isAdminPasswordResetEmailOtpCurrentlyEnabled = false;
        boolean isAdminPasswordResetEmailLinkCurrentlyEnabled = false;
        boolean isAdminPasswordResetSmsOtpCurrentlyEnabled = false;

        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            if (ENABLE_ADMIN_PASSWORD_RESET_OFFLINE_PROPERTY.equals(identityMgtProperty.getName())) {
                isAdminPasswordResetOfflineCurrentlyEnabled =
                        Boolean.parseBoolean(identityMgtProperty.getValue());
            } else if (ENABLE_ADMIN_PASSWORD_RESET_EMAIL_OTP_PROPERTY.equals(identityMgtProperty.getName())) {
                isAdminPasswordResetEmailOtpCurrentlyEnabled =
                        Boolean.parseBoolean(identityMgtProperty.getValue());
            } else if (ENABLE_ADMIN_PASSWORD_RESET_EMAIL_LINK_PROPERTY.equals(identityMgtProperty.getName())) {
                isAdminPasswordResetEmailLinkCurrentlyEnabled =
                        Boolean.parseBoolean(identityMgtProperty.getValue());
            } else if (ENABLE_ADMIN_PASSWORD_RESET_SMS_OTP_PROPERTY.equals(identityMgtProperty.getName())) {
                isAdminPasswordResetSmsOtpCurrentlyEnabled =
                        Boolean.parseBoolean(identityMgtProperty.getValue());
            }
        }

        // Update the admin password reset config values based on the existing config values.
        if (StringUtils.isBlank(adminPasswordResetOfflineProp)) {
            isAdminPasswordResetOfflineEnabled = isAdminPasswordResetOfflineCurrentlyEnabled;
        }
        if (StringUtils.isBlank(adminPasswordResetEmailLinkProp)) {
            isAdminPasswordResetEmailLinkEnabled = isAdminPasswordResetEmailLinkCurrentlyEnabled;
        }
        if (StringUtils.isBlank(adminPasswordResetEmailOtpProp)) {
            isAdminPasswordResetEmailOtpEnabled = isAdminPasswordResetEmailOtpCurrentlyEnabled;
        }
        if (StringUtils.isBlank(adminPasswordResetSmsOtpProp)) {
            isAdminPasswordResetSmsOtpEnabled = isAdminPasswordResetSmsOtpCurrentlyEnabled;
        }

        List<Boolean> configs = Arrays.asList(isAdminPasswordResetOfflineEnabled,
                isAdminPasswordResetEmailOtpEnabled, isAdminPasswordResetEmailLinkEnabled,
                isAdminPasswordResetSmsOtpEnabled);

        long enabledConfigCount = configs.stream().filter(Boolean::booleanValue).count();

        if (enabledConfigCount > 1) {
            throw IdPManagementUtil.handleClientException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                    "Enabling admin forced password reset option while other options are enabled is not allowed");
        }
        else if (enabledConfigCount == 0) {
            throw IdPManagementUtil.handleClientException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                    "Disabling all admin forced password reset options is not allowed");
        }
    }

    /**
     * This method is used to validate user enabling multiple ask password set options at the same time.
     *
     * @param isAskPasswordEmailOTPEnabled is ask password email OTP enabled.
     * @param isAskPasswordSMSOTPEnabled is ask password SMS OTP enabled.
     *
     * @throws IdentityProviderManagementClientException when more than one ask password set option is enabled.
     */
    private static void validateAskPasswordCurrentConfigs(boolean isAskPasswordEmailOTPEnabled,
                                                                 boolean isAskPasswordSMSOTPEnabled)
            throws IdentityProviderManagementClientException {

        if (isAskPasswordEmailOTPEnabled && isAskPasswordSMSOTPEnabled) {
            throw IdPManagementUtil.handleClientException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                    "Enabling more than one ask password set option is not allowed.");
        }
    }

    private static void validateAskPasswordWithExistingConfigs(IdentityProviderProperty[] identityMgtProperties,
                                                                      String askPasswordEmailOTP,
                                                               String askPasswordSMSOTP)
            throws IdentityProviderManagementClientException {

        boolean isAskPasswordEmailOTPCurrentlyEnabled = false;
        boolean isAskPasswordSMSOTPCurrentlyEnabled = false;

        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            if (ASK_PASSWORD_SEND_EMAIL_OTP.equals(identityMgtProperty.getName())) {
                isAskPasswordEmailOTPCurrentlyEnabled = Boolean.parseBoolean(identityMgtProperty.getValue());
            } else if (ASK_PASSWORD_SEND_SMS_OTP.equals(identityMgtProperty.getName())) {
                isAskPasswordSMSOTPCurrentlyEnabled = Boolean.parseBoolean(identityMgtProperty.getValue());
            }
        }

        if (askPasswordEmailOTP != null) {
            isAskPasswordEmailOTPCurrentlyEnabled = Boolean.parseBoolean(askPasswordEmailOTP);
        }
        if (askPasswordSMSOTP != null) {
            isAskPasswordSMSOTPCurrentlyEnabled = Boolean.parseBoolean(askPasswordSMSOTP);
        }

        List<Boolean> configs = Arrays.asList(isAskPasswordEmailOTPCurrentlyEnabled,
                isAskPasswordSMSOTPCurrentlyEnabled);

        long enabledConfigCount = configs.stream().filter(Boolean::booleanValue).count();

        if(enabledConfigCount > 1) {
            throw IdPManagementUtil.handleClientException(
                    IdPManagementConstants.ErrorMessage.ERROR_CODE_INVALID_CONNECTOR_CONFIGURATION,
                    "Enabling more than one ask password set option is not allowed.");
        }
    }
}
