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
import org.apache.xerces.impl.Constants;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.IdentityApplicationRegistrationFailureException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderTemplateCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderTemplateCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationTemplateDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.OAuthApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.PaginatableFilterableApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.SAMLApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.AbstractApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.FileBasedApplicationDAO;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtException;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtService;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.endTenantFlow;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.isRegexValidated;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.startTenantFlow;
import static org.wso2.carbon.identity.core.util.IdentityUtil.isValidPEMCertificate;

/**
 * Application management service implementation.
 */
public class ApplicationManagementServiceImpl extends ApplicationManagementService {

    private static final Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);
    private static volatile ApplicationManagementServiceImpl appMgtService;
    private ApplicationMgtValidator applicationMgtValidator = new ApplicationMgtValidator();
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

        createApplicationWithTemplate(serviceProvider, tenantDomain, username, null);
    }

    @Override
    public ServiceProvider addApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        return createApplicationWithTemplate(serviceProvider, tenantDomain, username, null);
    }

    @Override
    public ServiceProvider createApplicationWithTemplate(ServiceProvider serviceProvider, String tenantDomain,
                                                         String username, String templateName)
            throws IdentityApplicationManagementException {

        SpTemplate spTemplate = this.getApplicationTemplate(templateName, tenantDomain);

        ServiceProvider initialSP = new ServiceProvider();
        initialSP.setApplicationName(serviceProvider.getApplicationName());
        initialSP.setDescription(serviceProvider.getDescription());
        updateSPFromTemplate(serviceProvider, tenantDomain, spTemplate);

        ServiceProvider addedSP = doAddApplication(initialSP, tenantDomain, username);
        serviceProvider.setApplicationID(addedSP.getApplicationID());
        serviceProvider.setOwner(getUser(tenantDomain, username));
        if (spTemplate != null && spTemplate.getContent() != null) {
            updateApplication(serviceProvider, tenantDomain, username);
        }

        return serviceProvider;
    }

    @Override
    public ServiceProvider getApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider;
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

        return getApplicationBasicInfo(tenantDomain, username, "*");
    }

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = null;
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent
                .getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetApplicationBasicInfo(tenantDomain, username, filter)) {
                return new ApplicationBasicInfo[0];
            }
        }

        try {
            startTenantFlow(tenantDomain, username);
            appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        } finally {
            endTenantFlow();
        }

        if (!(appDAO instanceof AbstractApplicationDAOImpl)) {
            log.error("Get application basic info service is not supported.");
            throw new IdentityApplicationManagementException("This service is not supported.");
        }

        // invoking the listeners
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetApplicationBasicInfo(appDAO, tenantDomain, username,
                    filter)) {
                return new ApplicationBasicInfo[0];
            }
        }

        return ((AbstractApplicationDAOImpl) appDAO).getApplicationBasicInfo(filter);
    }

    /**
     * Get All Application Basic Information with pagination
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param pageNumber   Number of the page
     * @return ApplicationBasicInfo[]
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(String tenantDomain, String username, int
            pageNumber) throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] applicationBasicInfoArray;

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();

            if (appDAO instanceof PaginatableFilterableApplicationDAO) {

                // invoking pre listeners
                Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPreGetPaginatedApplicationBasicInfo
                                    (tenantDomain, username, pageNumber)) {
                        return new ApplicationBasicInfo[0];
                    }
                }

                applicationBasicInfoArray = ((PaginatableFilterableApplicationDAO) appDAO)
                        .getAllPaginatedApplicationBasicInfo(pageNumber);
                // invoking post listeners
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPostGetPaginatedApplicationBasicInfo
                                    (tenantDomain, username, pageNumber, applicationBasicInfoArray)) {
                        return new ApplicationBasicInfo[0];
                    }
                }

            } else {
                throw new UnsupportedOperationException("Application pagination is not supported. Tenant domain: " +
                        tenantDomain);
            }
        } finally {
            endTenantFlow();
        }

        return applicationBasicInfoArray;
    }

    /**
     * Get all basic application information with pagination based on the offset and limit.
     *
     * @param tenantDomain Tenant Domain.
     * @param username     User name.
     * @param offset       Starting index of the count.
     * @param limit        Counting value.
     * @return An array of {@link ApplicationBasicInfo} instances within the limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     */
    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, int offset,
                                                          int limit) throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] applicationBasicInfoArray;

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();

            if (appDAO instanceof PaginatableFilterableApplicationDAO) {
                // Invoking pre listeners.
                Collection<ApplicationMgtListener> listeners =
                        ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPreGetApplicationBasicInfo
                                    (tenantDomain, username, offset, limit)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Invoking pre listener: " + listener.getClass().getName());
                        }
                        return new ApplicationBasicInfo[0];
                    }
                }

                applicationBasicInfoArray = ((PaginatableFilterableApplicationDAO) appDAO)
                        .getApplicationBasicInfo(offset, limit);

                // Invoking post listeners.
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPostGetApplicationBasicInfo
                                    (tenantDomain, username, offset, limit, applicationBasicInfoArray)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Invoking post listener: " + listener.getClass().getName());
                        }
                        return new ApplicationBasicInfo[0];
                    }
                }
            } else {
                throw new UnsupportedOperationException("Application pagination is not supported in " +
                        appDAO.getClass().getName() + " with tenant domain: " + tenantDomain);
            }
        } finally {
            endTenantFlow();
        }

        return applicationBasicInfoArray;
    }

    /**
     * Get all basic application information for a matching filter with pagination.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param filter       Application name filter
     * @param pageNumber   Number of the page
     * @return Application Basic Information array
     * @throws IdentityApplicationManagementException
     */
    @Override
    public ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(String tenantDomain, String username, int
            pageNumber, String filter) throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] applicationBasicInfoArray;

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            // invoking pre listeners
            if (appDAO instanceof PaginatableFilterableApplicationDAO) {
                Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent
                        .getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPreGetPaginatedApplicationBasicInfo(tenantDomain, username,
                                    pageNumber, filter)) {
                        return new ApplicationBasicInfo[0];
                    }
                }

                applicationBasicInfoArray = ((PaginatableFilterableApplicationDAO) appDAO)
                        .getPaginatedApplicationBasicInfo(pageNumber, filter);

                // invoking post listeners
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPostGetPaginatedApplicationBasicInfo
                                    (tenantDomain, username, pageNumber, filter, applicationBasicInfoArray)) {
                        return new ApplicationBasicInfo[0];
                    }
                }
            } else {
                throw new UnsupportedOperationException("Application filtering and pagination not supported. " +
                        "Tenant domain: " + tenantDomain);
            }
        } finally {
            endTenantFlow();
        }

        return applicationBasicInfoArray;
    }

    /**
     * Get all basic application information for a matching filter with pagination based on the offset and limit.
     *
     * @param tenantDomain Tenant Domain.
     * @param username     User name.
     * @param filter       Application name filter.
     * @param offset       Starting index of the count.
     * @param limit        Counting value.
     * @return An array of {@link ApplicationBasicInfo} instances within the limit.
     * @throws IdentityApplicationManagementException Error in retrieving basic application information.
     */
    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfo(String tenantDomain, String username, String filter,
                                                          int offset, int limit)
            throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] applicationBasicInfoArray;

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();

            if (appDAO instanceof PaginatableFilterableApplicationDAO) {
                // Invoking pre listeners.
                Collection<ApplicationMgtListener> listeners =
                        ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPreGetApplicationBasicInfo
                                    (tenantDomain, username, filter, offset, limit)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Invoking pre listener: " + listener.getClass().getName());
                        }
                        return new ApplicationBasicInfo[0];
                    }
                }

                applicationBasicInfoArray = ((PaginatableFilterableApplicationDAO) appDAO).
                        getApplicationBasicInfo(filter, offset, limit);

                // Invoking post listeners.
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPostGetApplicationBasicInfo
                                    (tenantDomain, username, filter, offset, limit, applicationBasicInfoArray)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Invoking post listener: " + listener.getClass().getName());
                        }
                        return new ApplicationBasicInfo[0];
                    }
                }
            } else {
                throw new UnsupportedOperationException("Application filtering and pagination not supported in " +
                        appDAO.getClass().getName() + " with tenant domain: " + tenantDomain);
            }
        } finally {
            endTenantFlow();
        }

        return applicationBasicInfoArray;
    }

    /**
     * Get count of all Application Basic Information.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @return int
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int getCountOfAllApplications(String tenantDomain, String username) throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            if (appDAO instanceof PaginatableFilterableApplicationDAO) {
                return ((PaginatableFilterableApplicationDAO) appDAO).getCountOfAllApplications();
            } else {
                throw new UnsupportedOperationException("Application count is not supported. " + "Tenant domain: " +
                        tenantDomain);
            }
        } finally {
            endTenantFlow();
        }
    }

    /**
     * Get count of all basic application information for a matching filter.
     *
     * @param tenantDomain Tenant Domain
     * @param username     User Name
     * @param filter       Application name filter
     * @return int
     * @throws IdentityApplicationManagementException
     */
    @Override
    public int getCountOfApplications(String tenantDomain, String username, String filter) throws
            IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain, username);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            if (appDAO instanceof PaginatableFilterableApplicationDAO) {
                return ((PaginatableFilterableApplicationDAO) appDAO).getCountOfApplications(filter);
            } else {
                throw new UnsupportedOperationException("Application count is not supported. " + "Tenant domain: " +
                        tenantDomain);
            }
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public void updateApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        try {
            applicationMgtValidator.validateSPConfigurations(serviceProvider, tenantDomain, username);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error("Validation error when updating the application  " + serviceProvider.getApplicationName() + "@" +
                    tenantDomain);
            for (String msg : e.getValidationMsg()) {
                log.error(msg);
            }
            throw e;
        }

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateApplication(serviceProvider, tenantDomain, username)) {
                throw new IdentityApplicationManagementException("Pre Update application failed");
            }
        }
        String applicationName = serviceProvider.getApplicationName();

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
                        "to the regex " + ApplicationMgtUtil.getSPValidatorRegex());
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
            String error = "Error occurred while updating the application: " + applicationName + ". " + e.getMessage();
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

    @Override
    public void deleteApplication(String applicationName, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider;
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
            serviceProvider = appDAO.getApplication(applicationName, tenantDomain);
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
            String error = "Error occurred while deleting the application: " + applicationName + ". " + e.getMessage();
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable()) {
                listener.doPostDeleteApplication(applicationName, tenantDomain, username);
                listener.doPostDeleteApplication(serviceProvider, tenantDomain, username);
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
            String error = "Error occurred while retrieving Identity Provider: " + federatedIdPName + ". " +
                    e.getMessage();
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
            String error = "Error occurred while retrieving all Identity Providers" + ". " + e.getMessage();
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
            String error = "Error occurred while retrieving all Local Authenticators" + ". " + e.getMessage();
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
            String error = "Error occurred while retrieving all Request Path Authenticators" + ". " + e.getMessage();
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
            String error = "Error while reading system claims" + ". " + e.getMessage();
            throw new IdentityApplicationManagementException(error, e);
        } finally {
            endTenantFlow();
        }
    }

    @Override
    public String getServiceProviderNameByClientIdExcludingFileBasedSPs(String clientId, String type, String
            tenantDomain) throws IdentityApplicationManagementException {

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
            String error = "Error occurred while retrieving the service provider for client id :  " + clientId + ". "
                    + e.getMessage();
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

        if (StringUtils.isNotEmpty(clientId)) {
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            name = appDAO.getServiceProviderNameByClientId(clientId, clientType, tenantDomain);

            if (name == null) {
                name = new FileBasedApplicationDAO().getServiceProviderNameByClientId(clientId,
                        clientType, tenantDomain);
            }
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

        ServiceProvider serviceProvider = null;
        try {
            startTenantFlow(tenantDomain);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            serviceProvider = appDAO.getApplication(serviceProviderName, tenantDomain);
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
        String serviceProviderName = serviceProvider.getApplicationName();
        String tenantDomain = serviceProvider.getOwner().getTenantDomain();

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
            startTenantFlow(tenantDomain);
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            serviceProvider = appDAO.getApplication(serviceProviderName, tenantDomain);

            if (serviceProvider != null) {
                // if "Authentication Type" is "Default" we must get the steps from the default SP
                AuthenticationStep[] authenticationSteps = serviceProvider
                        .getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();

                if (authenticationSteps == null || authenticationSteps.length == 0) {
                    ServiceProvider defaultSP = ApplicationManagementServiceComponent
                            .getFileBasedSPs().get(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
                    authenticationSteps = defaultSP.getLocalAndOutBoundAuthenticationConfig()
                            .getAuthenticationSteps();
                    AuthenticationScriptConfig scriptConfig = defaultSP.getLocalAndOutBoundAuthenticationConfig()
                            .getAuthenticationScriptConfig();
                    serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                            .setAuthenticationSteps(authenticationSteps);
                    if (scriptConfig != null) {
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                                .setAuthenticationScriptConfig(scriptConfig);
                        serviceProvider.getLocalAndOutBoundAuthenticationConfig()
                                .setAuthenticationType(ApplicationConstants.AUTH_TYPE_FLOW);
                    }
                }
            }
        } finally {
            endTenantFlow();
        }

        if (serviceProvider == null && serviceProviderName != null && ApplicationManagementServiceComponent
                .getFileBasedSPs().containsKey(serviceProviderName)) {
            serviceProvider = ApplicationManagementServiceComponent.getFileBasedSPs().get(serviceProviderName);
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

        ServiceProvider serviceProvider = unmarshalSP(spFileContent, tenantDomain);
        ImportResponse importResponse = this.importSPApplication(serviceProvider, tenantDomain, username, isUpdate);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Service provider %s@%s created successfully from file %s",
                    serviceProvider.getApplicationName(), tenantDomain, spFileContent.getFileName()));
        }

        return importResponse;
    }

    public ImportResponse importSPApplication(ServiceProvider serviceProvider, String tenantDomain, String username,
                                              boolean isUpdate) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Importing service provider from object " + serviceProvider.getApplicationName());
        }

        ImportResponse importResponse = this.importSPApplicationFromObject(serviceProvider, tenantDomain,
                username, isUpdate);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Service provider %s@%s created successfully from object",
                    serviceProvider.getApplicationName(), tenantDomain));
        }

        return importResponse;
    }

    private ImportResponse importSPApplicationFromObject(ServiceProvider serviceProvider, String tenantDomain, String username,
                                                         boolean isUpdate) throws IdentityApplicationManagementException {

        ImportResponse importResponse = new ImportResponse();

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
                savedSP = createApplicationWithTemplate(basicApplication, tenantDomain, username, null);
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
            throw new IdentityApplicationManagementException(errorMsg, e);
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

    @Override
    public void createApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            ServiceProvider serviceProvider = unmarshalSPTemplate(spTemplate.getContent());
            validateSPTemplateExists(spTemplate, tenantDomain);
            validateUnsupportedTemplateConfigs(serviceProvider);
            applicationMgtValidator.validateSPConfigurations(serviceProvider, tenantDomain,
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            Collection<ApplicationMgtListener> listeners =
                    ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.doPreCreateApplicationTemplate(serviceProvider, tenantDomain);
                }
            }
            doAddApplicationTemplate(spTemplate, tenantDomain);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error("Validation error when creating the application template: " + spTemplate.getName() + " in:" +
                    tenantDomain);
            for (String msg : e.getValidationMsg()) {
                log.error(msg);
            }
            throw new IdentityApplicationManagementClientException(e.getValidationMsg());
        } catch (IdentityApplicationManagementException e) {
            String errorMsg = String.format("Error when creating the application template: %s in tenant: %s",
                    spTemplate.getName(), tenantDomain);
            throw new IdentityApplicationManagementException(errorMsg, e);
        }
    }

    @Override
    public void createApplicationTemplateFromSP(ServiceProvider serviceProvider, SpTemplate spTemplate,
                                                String tenantDomain)
            throws IdentityApplicationManagementException {

        if (serviceProvider != null) {
            try {
                validateSPTemplateExists(spTemplate, tenantDomain);

                ServiceProvider updatedSP = removeUnsupportedTemplateConfigs(serviceProvider);
                applicationMgtValidator.validateSPConfigurations(updatedSP, tenantDomain,
                        CarbonContext.getThreadLocalCarbonContext().getUsername());
                Collection<ApplicationMgtListener> listeners =
                        ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable()) {
                        listener.doPreCreateApplicationTemplate(serviceProvider, tenantDomain);
                    }
                }

                String serviceProviderTemplateXml = marshalSPTemplate(updatedSP, tenantDomain);
                spTemplate.setContent(serviceProviderTemplateXml);
                doAddApplicationTemplate(spTemplate, tenantDomain);
            } catch (IdentityApplicationManagementValidationException e) {
                log.error("Validation error when creating the application template:" + spTemplate.getName() +
                        "from service provider: " + serviceProvider.getApplicationName() + " in:" + tenantDomain);
                for (String msg : e.getValidationMsg()) {
                    log.error(msg);
                }
                throw new IdentityApplicationManagementClientException(e.getValidationMsg());
            } catch (IdentityApplicationManagementException e) {
                String errorMsg = String.format("Error when creating the application template: %s from " +
                                "service provider: %s in: ", spTemplate.getName(), serviceProvider.getApplicationName(),
                        tenantDomain);
                throw new IdentityApplicationManagementException(errorMsg, e);
            }
        } else {
            createApplicationTemplate(spTemplate, tenantDomain);
        }
    }

    @Override
    public SpTemplate getApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        String retrievedTemplateName = templateName;
        if (StringUtils.isBlank(retrievedTemplateName)) {
            retrievedTemplateName = ApplicationConstants.TENANT_DEFAULT_SP_TEMPLATE_NAME;
        }

        SpTemplate spTemplate = doGetApplicationTemplate(retrievedTemplateName, tenantDomain);

        if (spTemplate == null) {
            if (StringUtils.isBlank(templateName)) {
                return null;
            } else {
                throw new IdentityApplicationManagementClientException(new String[]{
                        String.format("Template with name: %s is not " + "registered for tenant: %s.",
                                templateName, tenantDomain)});
            }
        }
        return spTemplate;
    }

    @Override
    public void deleteApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        doDeleteApplicationTemplate(templateName, tenantDomain);
    }

    @Override
    public void updateApplicationTemplate(String oldTemplateName, SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            validateSPTemplateExists(oldTemplateName, spTemplate, tenantDomain);

            ServiceProvider serviceProvider = unmarshalSPTemplate(spTemplate.getContent());
            validateUnsupportedTemplateConfigs(serviceProvider);

            applicationMgtValidator.validateSPConfigurations(serviceProvider, tenantDomain,
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            Collection<ApplicationMgtListener> listeners =
                    ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.doPreUpdateApplicationTemplate(serviceProvider, tenantDomain);
                }
            }
            doUpdateApplicationTemplate(oldTemplateName, spTemplate, tenantDomain);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error("Validation error when updating the application template: " + oldTemplateName + " in:" +
                    tenantDomain);
            for (String msg : e.getValidationMsg()) {
                log.error(msg);
            }
            throw new IdentityApplicationManagementClientException(e.getValidationMsg());
        } catch (IdentityApplicationManagementException e) {
            String errorMsg = String.format("Error in updating the application template: %s in tenant: %s",
                    oldTemplateName, tenantDomain);
            throw new IdentityApplicationManagementException(errorMsg, e);
        }
    }

    @Override
    public boolean isExistingApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        return doCheckApplicationTemplateExistence(templateName, tenantDomain);
    }

    @Override
    public List<SpTemplate> getAllApplicationTemplateInfo(String tenantDomain)
            throws IdentityApplicationManagementException {

        return doGetAllApplicationTemplateInfo(tenantDomain);
    }

    /**
     * Add SP template to database and cache.
     *
     * @param spTemplate   SP template info
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    private void doAddApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Add application template to database
        ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                .getApplicationTemplateDAO();
        applicationTemplateDAO.createApplicationTemplate(spTemplate, tenantDomain);

        // Add application template to cache
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(spTemplate.getName(),
                tenantDomain);
        ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate);
    }

    /**
     * Get SP template from cache or database.
     *
     * @param templateName template name
     * @param tenantDomain tenant domain
     * @return template info
     * @throws IdentityApplicationManagementException
     */
    private SpTemplate doGetApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Get SP template from cache
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName,
                tenantDomain);
        SpTemplate spTemplate = getSpTemplateFromCache(templateCacheKey);

        if (spTemplate == null) {
            // Get SP template from database
            spTemplate = getSpTemplateFromDB(templateName, tenantDomain, templateCacheKey);
        }
        return spTemplate;
    }

    /**
     * Delete SP template from database and cache.
     *
     * @param templateName template name
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    private void doDeleteApplicationTemplate(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Delete SP template from database
        ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                .getApplicationTemplateDAO();
        applicationTemplateDAO.deleteApplicationTemplate(templateName, tenantDomain);

        // Delete SP template from cache
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName,
                tenantDomain);
        ServiceProviderTemplateCache.getInstance().clearCacheEntry(templateCacheKey);
    }

    /**
     * Update SP template from database and cache.
     *
     * @param templateName template name
     * @param spTemplate   template info
     * @param tenantDomain tenant domain
     * @throws IdentityApplicationManagementException
     */
    private void doUpdateApplicationTemplate(String templateName, SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Update SP template in database
        ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                .getApplicationTemplateDAO();
        applicationTemplateDAO.updateApplicationTemplate(templateName, spTemplate, tenantDomain);

        // Update the template in cache
        if (!templateName.equals(spTemplate.getName())) {
            ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName,
                    tenantDomain);
            ServiceProviderTemplateCache.getInstance().clearCacheEntry(templateCacheKey);
        }
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(spTemplate.getName(),
                tenantDomain);
        ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate);
    }

    /**
     * Check existence of a SP template.
     *
     * @param templateName template name
     * @param tenantDomain tenant domain
     * @return true if SP template exists
     * @throws IdentityApplicationManagementException
     */
    private boolean doCheckApplicationTemplateExistence(String templateName, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Check existence in cache
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName,
                tenantDomain);
        SpTemplate spTemplate = getSpTemplateFromCache(templateCacheKey);

        if (spTemplate == null) {
            // Check existence in database
            ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                    .getApplicationTemplateDAO();
            return applicationTemplateDAO.isExistingTemplate(templateName, tenantDomain);
        }
        return true;
    }

    /**
     * Get basic info of all the SP templates.
     *
     * @param tenantDomain tenant domain
     * @return list of all template info
     * @throws IdentityApplicationManagementException
     */
    private List<SpTemplate> doGetAllApplicationTemplateInfo(String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                .getApplicationTemplateDAO();
        return applicationTemplateDAO.getAllApplicationTemplateInfo(tenantDomain);
    }

    /**
     * Validate unsupported application template configurations.
     *
     * @param serviceProvider SP template
     * @throws IdentityApplicationManagementException
     */
    private void validateUnsupportedTemplateConfigs(ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();

        if (serviceProvider.getInboundAuthenticationConfig() != null) {
            validationMsg.add("Inbound configurations are not supported.");
        }
        if (serviceProvider.getApplicationID() != 0) {
            validationMsg.add("Application ID is not supported.");
        }
        if (serviceProvider.getApplicationName() != null) {
            validationMsg.add("Application name is not supported.");
        }
        if (serviceProvider.getDescription() != null) {
            validationMsg.add("Application description is not supported.");
        }
        if (serviceProvider.getCertificateContent() != null) {
            validationMsg.add("Application certificate is not supported.");
        }

        if (!validationMsg.isEmpty()) {
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    private void validateSPTemplateExists(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        List<String> validationMsg = new ArrayList<>();
        if (StringUtils.isNotBlank(spTemplate.getName()) &&
                isExistingApplicationTemplate(spTemplate.getName(), tenantDomain)) {
            validationMsg.add(String.format("Template with name: %s is already configured for tenant: %s.",
                    spTemplate.getName(), tenantDomain));
            throw new IdentityApplicationManagementValidationException(validationMsg.toArray(new String[0]));
        }
    }

    private void validateSPTemplateExists(String oldTemplateName, SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (!oldTemplateName.equals(spTemplate.getName())) {
            validateSPTemplateExists(spTemplate, tenantDomain);
        }
    }

    private SpTemplate getSpTemplateFromDB(String templateName, String tenantDomain,
                                           ServiceProviderTemplateCacheKey templateCacheKey)
            throws IdentityApplicationManagementException {

        ApplicationTemplateDAO applicationTemplateDAO = ApplicationMgtSystemConfig.getInstance()
                .getApplicationTemplateDAO();
        SpTemplate spTemplate = applicationTemplateDAO.getApplicationTemplate(templateName, tenantDomain);
        if (spTemplate != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Template with name: %s is taken from database for tenant: %s ",
                        templateName, tenantDomain));
            }
            ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate);
            return spTemplate;
        }
        return null;
    }

    private SpTemplate getSpTemplateFromCache(ServiceProviderTemplateCacheKey templateCacheKey) {

        SpTemplate spTemplate = ServiceProviderTemplateCache.getInstance().getValueFromCache(templateCacheKey);
        if (spTemplate != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Template with name: %s is taken from cache of tenant: %s ",
                        templateCacheKey.getTemplateName(), templateCacheKey.getTenantDomain()));
            }
            return spTemplate;
        }
        return null;
    }

    private ServiceProvider unmarshalSPTemplate(String spTemplateXml)
            throws IdentityApplicationManagementValidationException {

        if (StringUtils.isEmpty(spTemplateXml)) {
            throw new IdentityApplicationManagementValidationException(new String[]{"Empty SP template configuration" +
                    " is provided."});
        }
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setXIncludeAware(false);
            try {
                spf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
                spf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
                spf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            } catch (SAXException | ParserConfigurationException e) {
                log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE +
                        " or " + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " +
                        Constants.LOAD_EXTERNAL_DTD_FEATURE + " or secure-processing.");
            }

            JAXBContext jc = JAXBContext.newInstance(ServiceProvider.class);
            UnmarshallerHandler unmarshallerHandler = jc.createUnmarshaller().getUnmarshallerHandler();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(unmarshallerHandler);

            ByteArrayInputStream inputStream = new ByteArrayInputStream(spTemplateXml.getBytes(StandardCharsets.UTF_8));
            InputSource inputSource = new InputSource(inputStream);
            xr.parse(inputSource);
            inputStream.close();
            return (ServiceProvider) unmarshallerHandler.getResult();
        } catch (JAXBException | SAXException | ParserConfigurationException | IOException e) {
            String msg = "Error in reading Service Provider template configuration.";
            log.error(msg, e);
            throw new IdentityApplicationManagementValidationException(new String[]{msg});
        }
    }

    private String marshalSPTemplate(ServiceProvider serviceProvider, String tenantDomain)
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
            throw new IdentityApplicationManagementException(String.format("Error in exporting Service Provider " +
                    "template from SP %s@%s", serviceProvider.getApplicationName(), tenantDomain), e);
        }
    }

    private ServiceProvider removeUnsupportedTemplateConfigs(ServiceProvider serviceProvider) {

        ServiceProvider updatedSp = serviceProvider;
        if (updatedSp != null) {
            updatedSp.setApplicationName(null);
            updatedSp.setDescription(null);
            updatedSp.setApplicationID(0);
            updatedSp.setCertificateContent(null);
            updatedSp.setInboundAuthenticationConfig(null);
        }
        return updatedSp;
    }

    private void updateSPFromTemplate(ServiceProvider serviceProvider, String tenantDomain,
                                      SpTemplate spTemplate) throws IdentityApplicationManagementException {

        if (spTemplate != null && spTemplate.getContent() != null) {
            ServiceProvider spConfigFromTemplate = unmarshalSP(spTemplate.getContent(), tenantDomain);
            Field[] fieldsSpTemplate = spConfigFromTemplate.getClass().getDeclaredFields();
            for (Field field : fieldsSpTemplate) {
                try {
                    Field fieldSpTemplate = spConfigFromTemplate.getClass().getDeclaredField(field.getName());
                    fieldSpTemplate.setAccessible(true);
                    Object value = fieldSpTemplate.get(spConfigFromTemplate);
                    if (value != null && fieldSpTemplate.getAnnotation(XmlElement.class) != null) {
                        Field fieldActualSp = serviceProvider.getClass().getDeclaredField(field.getName());
                        fieldActualSp.setAccessible(true);
                        fieldActualSp.set(serviceProvider, value);
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new IdentityApplicationManagementException("Error when updating SP template configurations" +
                            "into the actual service provider");
                }
            }
        }
    }

    private ServiceProvider unmarshalSP(String spTemplateXml, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (StringUtils.isEmpty(spTemplateXml)) {
            throw new IdentityApplicationManagementException("Empty SP template configuration is provided to " +
                    "unmarshal");
        }
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceProvider.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ServiceProvider) unmarshaller.unmarshal(new ByteArrayInputStream(
                    spTemplateXml.getBytes(StandardCharsets.UTF_8)));
        } catch (JAXBException e) {
            throw new IdentityApplicationManagementException("Error in reading Service Provider template " +
                    "configuration ", e);
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

        if (StringUtils.isBlank(serviceProvider.getApplicationName())) {
            // check for required attributes.
            throw new IdentityApplicationManagementException("Application Name is required");
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        if (appDAO.isApplicationExists(serviceProvider.getApplicationName(), tenantDomain)) {
            String errorMsg = "Application registration failed. An application with name \'" + serviceProvider.
                    getApplicationName() + "\' already exists.";
            log.error(errorMsg);
            throw new IdentityApplicationRegistrationFailureException(errorMsg);
        }

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
                    "to the regex " + ApplicationMgtUtil.getSPValidatorRegex());
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
                if (((AbstractUserStoreManager) realm.getUserStoreManager()).isUserInRole(username, roleName)) {
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
     * @param tenantDomain  tenant domain name
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

    private void setDefaultAuthenticationSeq(String sequenceName, String tenantDomain, ServiceProvider serviceProvider)
            throws IdentityApplicationManagementException {

        // if "Authentication Type" is "Default", get the tenant wise default authentication sequence if
        // available, otherwise the authentication sequence and adaptive script configuration in default SP
        DefaultAuthSeqMgtService seqMgtService = DefaultAuthSeqMgtServiceImpl.getInstance();
        DefaultAuthenticationSequence sequence;
        try {
            sequence = seqMgtService.getDefaultAuthenticationSeq(sequenceName, tenantDomain);
        } catch (DefaultAuthSeqMgtException e) {
            throw new IdentityApplicationManagementException("Error when retrieving default " +
                    "authentication sequence in tenant: " + tenantDomain, e);
        }

        if (sequence != null && sequence.getContent() != null) {
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    sequence.getContent().getAuthenticationSteps());
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(
                    sequence.getContent().getAuthenticationScriptConfig());
        } else {
            ServiceProvider defaultSP = ApplicationManagementServiceComponent
                    .getFileBasedSPs().get(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationSteps(
                    defaultSP.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps());
            serviceProvider.getLocalAndOutBoundAuthenticationConfig().setAuthenticationScriptConfig(
                    defaultSP.getLocalAndOutBoundAuthenticationConfig().getAuthenticationScriptConfig());
        }
    }
}

