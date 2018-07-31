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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.consent.mgt.core.ConsentManager;
import org.wso2.carbon.consent.mgt.core.exception.ConsentManagementException;
import org.wso2.carbon.consent.mgt.core.model.Purpose;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.ApplicationPermission;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.ConsentConfig;
import org.wso2.carbon.identity.application.common.model.ConsentPurpose;
import org.wso2.carbon.identity.application.common.model.ConsentPurposeConfigs;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheEntry;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.OAuthApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.SAMLApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.FileBasedApplicationDAO;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.wso2.carbon.consent.mgt.core.constant.ConsentConstants.ErrorMessages.ERROR_CODE_PURPOSE_ID_INVALID;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.PURPOSE_GROUP_SHARED;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.PURPOSE_GROUP_TYPE_SP;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.PURPOSE_GROUP_TYPE_SYSTEM;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.isRegexValidated;
import static org.wso2.carbon.identity.core.util.IdentityUtil.isValidPEMCertificate;

/**
 * Application management service implementation
 */
public class ApplicationManagementServiceImpl extends ApplicationManagementService {

    private static Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);
    private static volatile ApplicationManagementServiceImpl appMgtService;
    private ThreadLocal<Boolean> isImportSP = ThreadLocal.withInitial(() -> false);


    /**
     * Private constructor which will not allow to create objects of this class from outside
     */
    private ApplicationManagementServiceImpl() {

    }

    /**
     * Singleton method
     *
     * @return ApplicationManagementServiceImpl
     */
    public static ApplicationManagementServiceImpl getInstance() {

        if (appMgtService == null) {
            synchronized (ApplicationManagementServiceImpl.class) {
                if (appMgtService == null) {
                    appMgtService = new ApplicationManagementServiceImpl();
                }
            }
        }
        return appMgtService;
    }

    @Override
    public void createApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        doAddApplication(serviceProvider, tenantDomain, username);
    }

    @Override
    public ServiceProvider addApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        return doAddApplication(serviceProvider, tenantDomain, username);
    }

    @Override
    public ServiceProvider getApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = null;
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetApplicationExcludingFileBasedSPs(applicationName, tenantDomain)) {
                return null;
            }
        }

        try {

            startTenantFlow(tenantDomain);

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
            if (serviceProvider != null) {
                loadApplicationPermissions(applicationName, serviceProvider);
            }

        } catch (Exception e) {
            String error = "Error occurred while retrieving the application, " + applicationName;
            log.error(error, e);
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }

        // invoking the listeners
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetApplicationExcludingFileBasedSPs(serviceProvider, applicationName, tenantDomain)) {
                return null;
            }
        }

        return serviceProvider;
    }

    @Override
    public ApplicationBasicInfo[] getAllApplicationBasicInfo(String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = null;
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetAllApplicationBasicInfo(tenantDomain, username)) {
                return null;
            }
        }

        try {
            startTenantFlow(tenantDomain, username);
            appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();

        } catch (Exception e) {
            String error = "Error occurred while retrieving all the applications";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }

        // invoking the listeners
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetAllApplicationBasicInfo(appDAO, tenantDomain, username)) {
                return null;
            }
        }

        return appDAO.getAllApplicationBasicInfo();
    }

    @Override
    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateApplication(serviceProvider, tenantDomain, username)) {
                throw new IdentityApplicationManagementException("Pre Update application failed");
            }
        }
        String applicationName = serviceProvider.getApplicationName();

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    applicationName, tenantDomain);

            IdentityServiceProviderCache.getInstance().clearCacheEntry(cacheKey);

        } finally {
            endTenantFlow();
        }

        try {
            // check whether user is authorized to update the application.
            startTenantFlow(tenantDomain, username);
            if (!ApplicationConstants.LOCAL_SP.equals(applicationName) &&
                    !ApplicationMgtUtil.isUserAuthorized(applicationName, username,
                            serviceProvider.getApplicationID())) {
                log.warn("Illegal Access! User " +
                        CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have access to the application " +
                        applicationName);
                throw new IdentityApplicationManagementException("User not authorized");
            }

            if (!isRegexValidated(serviceProvider.getApplicationName())) {
                throw new IdentityApplicationManagementException("The Application name " +
                        serviceProvider.getApplicationName() + " is not valid! It is not adhering " +
                        "to the regex " + ApplicationMgtUtil.APP_NAME_VALIDATING_REGEX);
            }

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            String storedAppName = appDAO.getApplicationName(serviceProvider.getApplicationID());

            // Will be supported with 'Advance Consent Management Feature'.
            // validateConsentPurposes(serviceProvider);
            appDAO.updateApplication(serviceProvider, tenantDomain);
            if (isOwnerUpdateRequest(serviceProvider)) {
                //It is not required to validate the user here, as the user is validating inside the updateApplication
                // method above. Hence assign application role to the app owner.
                assignApplicationRole(serviceProvider.getApplicationName(), serviceProvider.getOwner().getUserName());
            }

            if (!isValidPEMCertificate(serviceProvider.getCertificateContent())) {
                String errorMessage = "Application certificate of the service provider " +
                        serviceProvider.getApplicationName() + " is malformed";
                log.error(errorMessage);
                throw new IdentityApplicationManagementException(errorMessage);
            }

            ApplicationPermission[] permissions = serviceProvider.getPermissionAndRoleConfig().getPermissions();
            String applicationNode = ApplicationMgtUtil.getApplicationPermissionPath() + RegistryConstants
                    .PATH_SEPARATOR + storedAppName;
            org.wso2.carbon.registry.api.Registry tenantGovReg = CarbonContext.getThreadLocalCarbonContext()
                    .getRegistry(RegistryType.USER_GOVERNANCE);

            boolean exist = tenantGovReg.resourceExists(applicationNode);
            if (exist && !StringUtils.equals(storedAppName, applicationName)) {
                ApplicationMgtUtil.renameAppPermissionPathNode(storedAppName, applicationName);
            }

            if (serviceProvider.getPermissionAndRoleConfig() != null &&
                    ArrayUtils.isNotEmpty(serviceProvider.getPermissionAndRoleConfig().getPermissions())) {
                ApplicationMgtUtil.updatePermissions(applicationName,
                        serviceProvider.getPermissionAndRoleConfig().getPermissions());
            }
        } catch (Exception e) {
            String error = "Error occurred while updating the application: " + applicationName;
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostUpdateApplication(serviceProvider, tenantDomain, username)) {
                return;
            }
        }
    }

    // Will be supported with 'Advance Consent Management Feature'.
    /*
    private void validateConsentPurposes(ServiceProvider serviceProvider) throws
            IdentityApplicationManagementException {

        ConsentManager consentManager = ApplicationManagementServiceComponentHolder.getInstance().getConsentManager();
        ConsentConfig consentConfig = serviceProvider.getConsentConfig();
        if (nonNull(consentConfig)) {
            ConsentPurposeConfigs consentPurposeConfigs = consentConfig.getConsentPurposeConfigs();
            if (nonNull(consentPurposeConfigs)) {
                ConsentPurpose[] consentPurposes = consentPurposeConfigs.getConsentPurpose();
                if (nonNull(consentPurposes)) {
                    for (ConsentPurpose consentPurpose : consentPurposes) {
                        int purposeId = consentPurpose.getPurposeId();
                        try {
                            Purpose purpose = consentManager.getPurpose(purposeId);
                            if (isNull(purpose)) {
                                if (log.isDebugEnabled()) {
                                    log.debug("ConsentManager returned null for Purpose ID: " + purposeId);
                                }
                                throw new IdentityApplicationManagementException("Invalid purpose ID: " + purposeId);
                            }

                            if (!isSPSpecificPurpose(serviceProvider, purpose) && !isSharedPurpose(purpose)) {
                                String message = "Purpose: %s with ID: %s is not defined under purposes for SP:" +
                                                 " %s or 'SHARED' purposes.";
                                String error = String.format(message, purpose.getName(), purpose.getId(),
                                                             serviceProvider.getApplicationName());
                                throw new IdentityApplicationManagementException(error);
                            }
                        } catch (ConsentManagementException e) {
                            if (ERROR_CODE_PURPOSE_ID_INVALID.getCode().equals(e.getErrorCode())) {
                                throw new IdentityApplicationManagementException("Invalid purpose ID: " + purposeId, e);
                            }
                            throw new IdentityApplicationManagementException("Error while retrieving consent purpose " +
                                                                             "with ID: " + purposeId, e);
                        }
                    }
                }
            }
        }
    }


    private boolean isSharedPurpose(Purpose purpose) {

        return PURPOSE_GROUP_SHARED.equals(purpose.getGroup()) && PURPOSE_GROUP_TYPE_SYSTEM.equals(
                purpose.getGroupType());
    }

    private boolean isSPSpecificPurpose(ServiceProvider serviceProvider, Purpose purpose) {

        return serviceProvider.getApplicationName().equals(purpose.getGroup())&& PURPOSE_GROUP_TYPE_SP.equals(
                purpose.getGroupType());
    }
    */

    private void startTenantFlow(String tenantDomain) throws IdentityApplicationManagementException {

        int tenantId;
        try {
            tenantId = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error when setting tenant domain. ", e);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
    }

    private void startTenantFlow(String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        int tenantId;
        try {
            tenantId = ApplicationManagementServiceComponentHolder.getInstance().getRealmService()
                    .getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error when setting tenant domain. ", e);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
    }

    private void endTenantFlow() {

        PrivilegedCarbonContext.endTenantFlow();
    }

    @Override
    public void deleteApplication(String applicationName, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteApplication(applicationName, tenantDomain, username)) {
                return;
            }
        }

        try {
            startTenantFlow(tenantDomain, username);

            if (!ApplicationMgtUtil.isUserAuthorized(applicationName, username)) {
                log.warn("Illegal Access! User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have access to the application " + applicationName);
                throw new IdentityApplicationManagementException("User not authorized");
            }

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            ServiceProvider serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
            appDAO.deleteApplication(applicationName);

            ApplicationMgtUtil.deleteAppRole(applicationName);
            ApplicationMgtUtil.deletePermissions(applicationName);

            if (serviceProvider != null &&
                    serviceProvider.getInboundAuthenticationConfig() != null &&
                    serviceProvider.getInboundAuthenticationConfig().getInboundAuthenticationRequestConfigs() != null) {

                InboundAuthenticationRequestConfig[] configs = serviceProvider.getInboundAuthenticationConfig()
                        .getInboundAuthenticationRequestConfigs();

                for (InboundAuthenticationRequestConfig config : configs) {

                    if (IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.
                            equalsIgnoreCase(config.getInboundAuthType()) && config.getInboundAuthKey() != null) {

                        SAMLApplicationDAO samlDAO = ApplicationMgtSystemConfig.getInstance().getSAMLClientDAO();
                        samlDAO.removeServiceProviderConfiguration(config.getInboundAuthKey());

                    } else if (IdentityApplicationConstants.OAuth2.NAME.equalsIgnoreCase(config.getInboundAuthType()) &&
                            config.getInboundAuthKey() != null) {
                        OAuthApplicationDAO oathDAO = ApplicationMgtSystemConfig.getInstance().getOAuthOIDCClientDAO();
                        oathDAO.removeOAuthApplication(config.getInboundAuthKey());

                    }
                }
            }

        } catch (Exception e) {
            String error = "Error occurred while deleting the application: " + applicationName;
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteApplication(applicationName, tenantDomain, username)) {
                return;
            }
        }
    }

    @Override
    public IdentityProvider getIdentityProvider(String federatedIdPName, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
            return idpdao.getIdentityProvider(federatedIdPName);
        } catch (Exception e) {
            String error = "Error occurred while retrieving Identity Provider: " + federatedIdPName;
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public IdentityProvider[] getAllIdentityProviders(String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
            List<IdentityProvider> fedIdpList = idpdao.getAllIdentityProviders();
            if (fedIdpList != null) {
                return fedIdpList.toArray(new IdentityProvider[fedIdpList.size()]);
            }
            return new IdentityProvider[0];
        } catch (Exception e) {
            String error = "Error occurred while retrieving all Identity Providers";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public LocalAuthenticatorConfig[] getAllLocalAuthenticators(String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
            List<LocalAuthenticatorConfig> localAuthenticators = idpdao.getAllLocalAuthenticators();
            if (localAuthenticators != null) {
                return localAuthenticators.toArray(new LocalAuthenticatorConfig[localAuthenticators.size()]);
            }
            return new LocalAuthenticatorConfig[0];
        } catch (Exception e) {
            String error = "Error occurred while retrieving all Local Authenticators";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators(String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
            List<RequestPathAuthenticatorConfig> reqPathAuthenticators = idpdao.getAllRequestPathAuthenticators();
            if (reqPathAuthenticators != null) {
                return reqPathAuthenticators.toArray(new RequestPathAuthenticatorConfig[reqPathAuthenticators.size()]);
            }
            return new RequestPathAuthenticatorConfig[0];
        } catch (Exception e) {
            String error = "Error occurred while retrieving all Request Path Authenticators";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public String[] getAllLocalClaimUris(String tenantDomain) throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain);
            String claimDialect = ApplicationMgtSystemConfig.getInstance().getClaimDialect();
            ClaimMapping[] claimMappings = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getClaimManager()
                    .getAllClaimMappings(claimDialect);
            List<String> claimUris = new ArrayList<>();
            for (ClaimMapping claimMap : claimMappings) {
                claimUris.add(claimMap.getClaim().getClaimUri());
            }
            String[] allLocalClaimUris = (claimUris.toArray(new String[claimUris.size()]));
            if (ArrayUtils.isNotEmpty(allLocalClaimUris)) {
                Arrays.sort(allLocalClaimUris);
            }
            return allLocalClaimUris;
        } catch (Exception e) {
            String error = "Error while reading system claims";
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public String getServiceProviderNameByClientIdExcludingFileBasedSPs(String clientId, String type, String
            tenantDomain)
            throws IdentityApplicationManagementException {

        String name = null;

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderNameByClientIdExcludingFileBasedSPs(name, clientId, type, tenantDomain)) {
                return null;
            }
        }

        try {
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            name = appDAO.getServiceProviderNameByClientId(clientId, type, tenantDomain);

        } catch (Exception e) {
            String error = "Error occurred while retrieving the service provider for client id :  " + clientId;
            throw new IdentityApplicationManagementException(error, e);
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetServiceProviderNameByClientIdExcludingFileBasedSPs(name, clientId, type, tenantDomain)) {
                return null;
            }
        }

        return name;
    }

    /**
     * [sp-claim-uri,local-idp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public Map<String, String> getServiceProviderToLocalIdPClaimMapping(String serviceProviderName,
                                                                        String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        Map<String, String> claimMap = appDAO.getServiceProviderToLocalIdPClaimMapping(
                serviceProviderName, tenantDomain);

        if (claimMap == null
                || claimMap.isEmpty()
                && ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                serviceProviderName)) {
            return new FileBasedApplicationDAO().getServiceProviderToLocalIdPClaimMapping(
                    serviceProviderName, tenantDomain);
        }

        return claimMap;
    }

    /**
     * [local-idp-claim-uri,sp-claim-uri]
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public Map<String, String> getLocalIdPToServiceProviderClaimMapping(String serviceProviderName,
                                                                        String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        Map<String, String> claimMap = appDAO.getLocalIdPToServiceProviderClaimMapping(
                serviceProviderName, tenantDomain);

        if (claimMap == null
                || claimMap.isEmpty()
                && ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                serviceProviderName)) {
            return new FileBasedApplicationDAO().getLocalIdPToServiceProviderClaimMapping(
                    serviceProviderName, tenantDomain);
        }
        return claimMap;

    }

    /**
     * Returns back the requested set of claims by the provided service provider in local idp claim
     * dialect.
     *
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public List<String> getAllRequestedClaimsByServiceProvider(String serviceProviderName,
                                                               String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        List<String> reqClaims = appDAO.getAllRequestedClaimsByServiceProvider(serviceProviderName,
                tenantDomain);

        if (reqClaims == null
                || reqClaims.isEmpty()
                && ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                serviceProviderName)) {
            return new FileBasedApplicationDAO().getAllRequestedClaimsByServiceProvider(
                    serviceProviderName, tenantDomain);
        }

        return reqClaims;
    }

    /**
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public String getServiceProviderNameByClientId(String clientId, String clientType,
                                                   String tenantDomain) throws IdentityApplicationManagementException {

        String name = null;

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderNameByClientId(clientId, clientType, tenantDomain)) {
                return null;
            }
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        name = appDAO.getServiceProviderNameByClientId(clientId, clientType, tenantDomain);

        if (name == null) {
            name = new FileBasedApplicationDAO().getServiceProviderNameByClientId(clientId,
                    clientType, tenantDomain);
        }

        if (name == null) {
            ServiceProvider defaultSP = ApplicationManagementServiceComponent.getFileBasedSPs()
                    .get(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
            name = defaultSP.getApplicationName();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetServiceProviderNameByClientId(name, clientId, clientType, tenantDomain)) {
                return null;
            }
        }

        return name;

    }

    /**
     * @param serviceProviderName
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ServiceProvider getServiceProvider(String serviceProviderName, String tenantDomain)
            throws IdentityApplicationManagementException {

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProvider(serviceProviderName, tenantDomain)) {
                return null;
            }
        }

        ServiceProvider serviceProvider;
        try {
            startTenantFlow(tenantDomain);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            serviceProvider = appDAO.getApplication(serviceProviderName, tenantDomain);

            if (serviceProvider != null) {
                loadApplicationPermissions(serviceProviderName, serviceProvider);
            }

            if (serviceProvider == null && ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(
                    serviceProviderName)) {
                serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs().get(serviceProviderName);
            }
        } finally {
            endTenantFlow();
        }

        // invoking the listeners
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() &&
                    !listener.doPostGetServiceProvider(serviceProvider, serviceProviderName, tenantDomain)) {
                return null;
            }
        }
        return serviceProvider;
    }

    /**
     * @param appId
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ServiceProvider getServiceProvider(int appId) throws IdentityApplicationManagementException {

        // TODO: Need to have pre listeners. Don't have them because we didn't want to add listener methods to the
        // TODO: ApplicationMgtListener interface since we didn't want to change APIs. Also pre listener aren't vital
        // TODO: for getters. Mostly post listeners are enough.

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider serviceProvider = appDAO.getApplication(appId);

        if (serviceProvider == null) {
            throw new IdentityApplicationManagementException(
                    "Error while getting the service provider for appId: " + appId);
        }
        String serviceProviderName = serviceProvider.getApplicationName();
        String tenantDomain = serviceProvider.getOwner().getTenantDomain();

        try {
            startTenantFlow(tenantDomain);
            loadApplicationPermissions(serviceProviderName, serviceProvider);

        } finally {
            endTenantFlow();
        }

        // TODO: Since we didn't add post listener methods to the ApplicationMgtListener API to avoid API changes, we
        // TODO: are invoking doPostGetServiceProvider(serviceProvider, serviceProviderName, tenantDomain) listener
        // TODO: method here as well.
        // invoking the post listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetServiceProvider(serviceProvider, serviceProviderName, tenantDomain)) {
                return null;
            }
        }
        return serviceProvider;
    }

    /**
     * @param clientId
     * @param clientType
     * @param tenantDomain
     * @return
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ServiceProvider getServiceProviderByClientId(String clientId, String clientType, String tenantDomain)
            throws IdentityApplicationManagementException {

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderByClientId(clientId, clientType, tenantDomain)) {
                return null;
            }
        }
        // client id can contain the @ to identify the tenant domain.
        if (clientId != null && clientId.contains("@")) {
            clientId = clientId.split("@")[0];
        }

        String serviceProviderName;
        ServiceProvider serviceProvider = null;

        serviceProviderName = getServiceProviderNameByClientId(clientId, clientType, tenantDomain);

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    serviceProviderName, tenantDomain);
            IdentityServiceProviderCacheEntry entry = IdentityServiceProviderCache.getInstance().
                    getValueFromCache(cacheKey);

            if (entry != null) {
                return entry.getServiceProvider();
            }

        } finally {
            endTenantFlow();
        }

        try {
            startTenantFlow(tenantDomain);
            if (serviceProviderName != null) {
                ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
                serviceProvider = appDAO.getApplication(serviceProviderName, tenantDomain);

                if (serviceProvider != null) {
                    // if "Authentication Type" is "Default" we must get the steps from the default SP
                    AuthenticationStep[] authenticationSteps = serviceProvider
                            .getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();

                    loadApplicationPermissions(serviceProviderName, serviceProvider);

                    if (authenticationSteps == null || authenticationSteps.length == 0) {
                        ServiceProvider defaultSP = ApplicationManagementServiceComponent
                                .getFileBasedSPs().get(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
                        authenticationSteps = defaultSP.getLocalAndOutBoundAuthenticationConfig()
                                .getAuthenticationSteps();
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                                .setAuthenticationSteps(authenticationSteps);
                    }
                }
            }

            if (serviceProvider == null && serviceProviderName != null && ApplicationManagementServiceComponent
                    .getFileBasedSPs().containsKey(serviceProviderName)) {
                serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs().get(serviceProviderName);
            }
        } finally {
            endTenantFlow();
        }

        try {
            startTenantFlow(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);

            IdentityServiceProviderCacheKey cacheKey = new IdentityServiceProviderCacheKey(
                    serviceProviderName, tenantDomain);
            IdentityServiceProviderCacheEntry entry = new IdentityServiceProviderCacheEntry();
            entry.setServiceProvider(serviceProvider);
            IdentityServiceProviderCache.getInstance().addToCache(cacheKey, entry);
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetServiceProviderByClientId(serviceProvider, clientId, clientType, tenantDomain)) {
                return null;
            }
        }

        return serviceProvider;
    }

    public ImportResponse importSPApplication(SpFileContent spFileContent, String tenantDomain, String username,
                                              boolean isUpdate) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Importing service provider from file " + spFileContent.getFileName());
        }

        ImportResponse importResponse = new ImportResponse();
        ServiceProvider serviceProvider = unmarshalSP(spFileContent, tenantDomain);


        Collection<ApplicationMgtListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();

        ServiceProvider savedSP = null;
        try {
            if (isUpdate) {
                savedSP = getApplicationExcludingFileBasedSPs(serviceProvider.getApplicationName(), tenantDomain);
                if (savedSP == null) {
                    String errorMsg = String.format("Service provider %s@%s is not found",
                            serviceProvider.getApplicationName(), tenantDomain);
                    log.error(errorMsg);
                    throw new IdentityApplicationManagementException(errorMsg);
                }
            }

            if (!isUpdate) {
                ServiceProvider basicApplication = new ServiceProvider();
                basicApplication.setApplicationName(serviceProvider.getApplicationName());
                basicApplication.setDescription(serviceProvider.getDescription());
                savedSP = addApplication(basicApplication, tenantDomain, username);
            }
            serviceProvider.setApplicationID(savedSP.getApplicationID());
            serviceProvider.setOwner(getUser(tenantDomain, username));

            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.onPreCreateInbound(serviceProvider, isUpdate);
                }
            }

            updateApplication(serviceProvider, tenantDomain, username);

            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.doImportServiceProvider(serviceProvider);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(String.format("Service provider %s@%s created successfully from file %s",
                        serviceProvider.getApplicationName(), tenantDomain, spFileContent.getFileName()));
            }
            importResponse.setResponseCode(ImportResponse.CREATED);
            importResponse.setApplicationName(serviceProvider.getApplicationName());
            importResponse.setErrors(new String[0]);
            return importResponse;
        } catch (IdentityApplicationManagementValidationException e) {
            deleteCreatedSP(savedSP, tenantDomain, username, isUpdate);
            importResponse.setResponseCode(ImportResponse.FAILED);
            importResponse.setApplicationName(null);
            importResponse.setErrors(e.getValidationMsg());
            return importResponse;
        } catch (IdentityApplicationManagementException e) {
           deleteCreatedSP(savedSP, tenantDomain, username, isUpdate);
            String errorMsg = String.format("Error in importing provided service provider %s@%s from file ",
                    serviceProvider.getApplicationName(), tenantDomain);
            log.error(errorMsg, e);
            throw new IdentityApplicationManagementException(errorMsg);
        }
    }

    public String exportSPApplication(String applicationName, boolean exportSecrets, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable()) {
                listener.doExportServiceProvider(serviceProvider, exportSecrets);
            }
        }
        return marshalSP(serviceProvider, tenantDomain);
    }

    private void loadApplicationPermissions(String serviceProviderName, ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        List<ApplicationPermission> permissionList = ApplicationMgtUtil.loadPermissions(serviceProviderName);

        if (permissionList != null) {
            PermissionsAndRoleConfig permissionAndRoleConfig;
            if (serviceProvider.getPermissionAndRoleConfig() == null) {
                permissionAndRoleConfig = new PermissionsAndRoleConfig();
            } else {
                permissionAndRoleConfig = serviceProvider.getPermissionAndRoleConfig();
            }
            permissionAndRoleConfig.setPermissions(permissionList.toArray(
                    new ApplicationPermission[permissionList.size()]));
            serviceProvider.setPermissionAndRoleConfig(permissionAndRoleConfig);
        }
    }

    /**
     * Get axis config
     *
     * @return axis configuration
     */
    private AxisConfiguration getAxisConfig() {

        return ApplicationManagementServiceComponentHolder.getInstance().getConfigContextService()
                .getServerConfigContext().getAxisConfiguration();
    }

    /**
     * Get config system registry
     *
     * @return config system registry
     * @throws org.wso2.carbon.registry.api.RegistryException
     */
    private Registry getConfigSystemRegistry() throws RegistryException {

        return (Registry) ApplicationManagementServiceComponentHolder.getInstance().getRegistryService()
                .getConfigSystemRegistry();
    }

    private void deleteApplicationPermission(String applicationName) {

        try {
            ApplicationMgtUtil.deletePermissions(applicationName);
        } catch (IdentityApplicationManagementException e) {
            log.error("Failed to delete the permissions for: " + applicationName, e);
        }
    }

    private void deleteApplicationRole(String applicationName) {

        try {
            ApplicationMgtUtil.deleteAppRole(applicationName);
        } catch (IdentityApplicationManagementException e) {
            log.error("Failed to delete the application role for: " + applicationName, e);
        }
    }

    private ServiceProvider doAddApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        // Invoking the listeners.
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent
                .getApplicationMgtListeners();

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreCreateApplication(serviceProvider, tenantDomain, username)) {
                return serviceProvider;
            }
        }

        String applicationName = serviceProvider.getApplicationName();
        if (!isRegexValidated(serviceProvider.getApplicationName())) {
            throw new IdentityApplicationManagementException("The Application name " +
                    serviceProvider.getApplicationName() + " is not valid! It is not adhering " +
                    "to the regex " + ApplicationMgtUtil.APP_NAME_VALIDATING_REGEX);
        }
        if (ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(applicationName)) {
            throw new IdentityApplicationManagementException(
                    "Application with the same name loaded from the file system.");
        }

        try {
            startTenantFlow(tenantDomain, username);

            // First we need to create a role with the application name. Only the users in this role will be able to
            // edit/update the application.
            ApplicationMgtUtil.createAppRole(applicationName, username);
            try {
                ApplicationMgtUtil.storePermissions(applicationName, username,
                        serviceProvider.getPermissionAndRoleConfig());
            } catch (IdentityApplicationManagementException e) {
                deleteApplicationRole(applicationName);
                throw e;
            }
            try {
                ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
                int applicationId = appDAO.createApplication(serviceProvider, tenantDomain);
                serviceProvider.setApplicationID(applicationId);
            } catch (IdentityApplicationManagementException e) {
                deleteApplicationRole(applicationName);
                deleteApplicationPermission(applicationName);
                throw e;
            }
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostCreateApplication(serviceProvider, tenantDomain, username)) {
                return serviceProvider;
            }
        }
        return serviceProvider;
    }

    private boolean isOwnerUpdateRequest(ServiceProvider serviceProvider) {

        return serviceProvider.getOwner() != null && StringUtils.isNotEmpty(serviceProvider.getOwner().getUserName())
                && !CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(serviceProvider.getOwner().getUserName());
    }

    private void assignApplicationRole(String applicationName, String username)
            throws IdentityApplicationManagementException {

        String roleName = getAppRoleName(applicationName);
        String[] newRoles = {roleName};

        try {
            // assign new application role to the user.
            UserRealm realm = PrivilegedCarbonContext.getThreadLocalCarbonContext().getUserRealm();
            if (realm != null) {
                String[] roleListOfUser = realm.getUserStoreManager().getRoleListOfUser(username);
                if (ArrayUtils.contains(roleListOfUser, roleName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("The user: " + username + " is already having the role: " + roleName);
                    }
                } else {
                    realm.getUserStoreManager().updateRoleListOfUser(username, null, newRoles);
                    if (log.isDebugEnabled()) {
                        log.debug("Assigning application role : " + roleName + " to the user : " + username);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while assigning application role: " + roleName +
                    " to the user: " + username, e);
        }
    }

    private static String getAppRoleName(String applicationName) {

        return ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + applicationName;
    }

    /**
     * Convert xml file of service provider to object.
     *
     * @param spFileContent xml string of the SP and file name
     * @param tenantDomain tenant domain name
     * @return Service Provider
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private ServiceProvider unmarshalSP(SpFileContent spFileContent, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (StringUtils.isEmpty(spFileContent.getContent())) {
            throw new IdentityApplicationManagementException(String.format("Empty Service Provider configuration file" +
                    " %s uploaded by tenant: %s", spFileContent.getFileName(), tenantDomain));
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceProvider.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ServiceProvider) unmarshaller.unmarshal(new ByteArrayInputStream(
                    spFileContent.getContent().getBytes(StandardCharsets.UTF_8)));

        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException(String.format("Error in reading Service Provider " +
                    "configuration file %s uploaded by tenant: %s", spFileContent.getFileName(), tenantDomain), e);
        }
    }

    /**
     * Convert service provider object of service provider to xml formatted string
     *
     * @param serviceProvider service provider to be marshaled
     * @param tenantDomain    tenant domain
     * @return xml formatted string of the service provider
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    private String marshalSP(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceProvider.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            DocumentBuilderFactory docBuilderFactory = IdentityUtil.getSecuredDocumentBuilderFactory();
            Document document = docBuilderFactory.newDocumentBuilder().newDocument();
            marshaller.marshal(serviceProvider, document);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
                    "AuthenticationScript inboundConfiguration");

            StringWriter stringBuilder = new StringWriter();
            StreamResult result = new StreamResult(stringBuilder);
            transformer.transform(new DOMSource(document), result);
            return stringBuilder.getBuffer().toString();
        } catch (JAXBException | ParserConfigurationException | TransformerException e) {
            throw new IdentityApplicationManagementException(String.format("Error in exporting Service Provider %s@%s",
                    serviceProvider.getApplicationName(), tenantDomain), e);
        }
    }

    /**
     * Create user object from user name and tenantDomain.
     *
     * @param tenantDomain tenantDomain
     * @param username     username
     * @return User
     */
    private User getUser(String tenantDomain, String username) {

        User user = new User();
        user.setUserName(UserCoreUtil.removeDomainFromName(username));
        user.setUserStoreDomain(UserCoreUtil.extractDomainFromName(username));
        user.setTenantDomain(tenantDomain);
        return user;
    }

    /**
     * Delete the newly created application, if there is an error
     *
     * @param savedSP      saved SP
     * @param tenantDomain tenant Domain
     * @param username     username
     * @throws IdentityApplicationManagementException
     */
    private void deleteCreatedSP(ServiceProvider savedSP, String tenantDomain, String username, boolean isUpdate)
            throws IdentityApplicationManagementException {

        if (savedSP != null && !isUpdate) {

            try {
                log.warn(String.format("Remove newly imported %s@%s application as error occurred ",
                        savedSP.getApplicationName(), tenantDomain));
                deleteApplication(savedSP.getApplicationName(), tenantDomain, username);

            } catch (IdentityApplicationManagementException e) {
                String errorMsg = String.format("Error occurred when removing newly imported service provider %s@%s",
                        savedSP.getApplicationName(), tenantDomain);
                log.error(errorMsg, e);
                throw new IdentityApplicationManagementException(errorMsg, e);
            }
        }
    }
}
