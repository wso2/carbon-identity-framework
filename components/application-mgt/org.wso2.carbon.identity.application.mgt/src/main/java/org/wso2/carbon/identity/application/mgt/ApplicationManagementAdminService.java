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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Application management admin service
 */
public class ApplicationManagementAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(ApplicationManagementAdminService.class);
    private static final String APPLICATION_ROLE_PREFIX = "Application/";
    private ApplicationManagementService applicationMgtService;
    private List<InboundAuthenticationRequestConfig> customInboundAuthenticatorConfigs;

    /**
     * Creates a service provider with basic information.First we need to create
     * a role with the
     * application name. Only the users in this role will be able to edit/update
     * the application.The
     * user will assigned to the created role.Internal roles used.
     *
     * @param serviceProvider Service provider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public void createApplication(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        try {
            createApplicationWithTemplate(serviceProvider, null);
        } catch (IdentityApplicationManagementException ex) {
            String message = "Error while creating application: " + serviceProvider.getApplicationName() + " for " +
                    "tenant: " + getTenantDomain();
            throw handleException(message, ex);
        }
    }

    private IdentityApplicationManagementException handleException(String msg,
                                                                   IdentityApplicationManagementException ex) {

        if (ex instanceof IdentityApplicationManagementClientException) {
            if (log.isDebugEnabled()) {
                log.debug(msg, ex);
            }
        } else {
            log.error(msg, ex);
        }

        return ex;
    }

    /**
     * Creates a service provider with the provided service provider template.
     *
     * @param serviceProvider Service provider
     * @param templateName    SP template name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public ServiceProvider createApplicationWithTemplate(ServiceProvider serviceProvider, String templateName)
            throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.createApplicationWithTemplate(serviceProvider, getTenantDomain(),
                    getUsername(),
                    templateName);
        } catch (IdentityApplicationManagementException ex) {
            String message = "Error while creating application: " + serviceProvider.getApplicationName() + " for " +
                    "tenant: " + getTenantDomain();
            throw handleException(message, ex);
        }
    }

    /**
     * Get Service provider information for given application name
     *
     * @param applicationName Application name
     * @return service provider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public ServiceProvider getApplication(String applicationName) throws IdentityApplicationManagementException {

        try {
            if (!ApplicationConstants.LOCAL_SP.equals(applicationName) &&
                    !ApplicationMgtUtil.isUserAuthorized(applicationName, getUsername())) {
                log.warn("Illegal Access! User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have access to the application " + applicationName);
                throw new IdentityApplicationManagementException("User not authorized");
            }
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getApplicationExcludingFileBasedSPs(applicationName, getTenantDomain());
        } catch (IdentityApplicationManagementException ex) {
            String msg = "Error while retrieving application: " + applicationName + " for tenant: " + getTenantDomain();
            throw handleException(msg, ex);
        }
    }

    /**
     * Get all basic application information
     *
     * @return Application Basic information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public ApplicationBasicInfo[] getAllApplicationBasicInfo() throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            ApplicationBasicInfo[] applicationBasicInfos =
                    applicationMgtService.getAllApplicationBasicInfo(getTenantDomain(), getUsername());
            List<ApplicationBasicInfo> appInfo = getAuthorizedApplicationBasicInfo(applicationBasicInfos,
                    getUsername());
            return appInfo.toArray(new ApplicationBasicInfo[appInfo.size()]);
        } catch (IdentityApplicationManagementException ex) {
            String message = "Error while retrieving all application basic info for tenant: " + getTenantDomain();
            throw handleException(message, ex);
        }
    }

    /**
     * Get all basic application information for a matching filter.
     *
     * @param filter Application name filter
     * @return Application Basic Information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public ApplicationBasicInfo[] getApplicationBasicInfo(String filter)
            throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            ApplicationBasicInfo[] applicationBasicInfos =
                    applicationMgtService.getApplicationBasicInfo(getTenantDomain(), getUsername(), filter);
            List<ApplicationBasicInfo> appInfo = getAuthorizedApplicationBasicInfo(applicationBasicInfos,
                    getUsername());
            return appInfo.toArray(new ApplicationBasicInfo[appInfo.size()]);
        } catch (IdentityApplicationManagementException ex) {
            String message = "Error while retrieving all application basic info for tenant: " + getTenantDomain() +
                    " with filter: " + filter;
            throw handleException(message, ex);
        }
    }

    /**
     * Get all basic application information with paginated manner
     *
     * @return Application Basic information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public ApplicationBasicInfo[] getAllPaginatedApplicationBasicInfo(int pageNumber)
            throws IdentityApplicationManagementException {

        return getPaginatedApplicationBasicInfo(pageNumber, "*");
    }

    /**
     * Get all basic application information for a matching filter.
     *
     * @param filter Application name filter
     * @return Application Basic Information array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public ApplicationBasicInfo[] getPaginatedApplicationBasicInfo(int pageNumber, String filter)
            throws IdentityApplicationManagementException {

        validateRequestedPageNumber(pageNumber);

        String authorizedUserTenantDomain = getTenantDomain();
        String authorizedUser = getUsername();
        int totalFilteredUserAuthorizedAppCount = getCountOfApplications(filter);

        if (totalFilteredUserAuthorizedAppCount == 0) {
            if (log.isDebugEnabled()) {
                log.debug("The user: " + authorizedUser + " in tenant domain: " + authorizedUserTenantDomain +
                        ", doesn't have any authorized applications that matches the given filter: " + filter);
            }
            return new ApplicationBasicInfo[0];
        }

        int itemsPerPage = ApplicationMgtUtil.getItemsPerPage();
        // Validate whether the start index of the requested page is less than the user's filtered authorized app count.
        if (isMaxPageNumberToRequestExceeded(authorizedUser, pageNumber, totalFilteredUserAuthorizedAppCount,
                itemsPerPage)) {
            return new ApplicationBasicInfo[0];
        }

        // Initial offset value is set to zero.
        int offset = 0;
        // Total expected number of app info results needed to decide the apps to be displayed for a specific page.
        int expectedNumberOfResults = itemsPerPage * pageNumber;

        int totalFilteredSystemAppCount =
                ApplicationManagementService.getInstance().getCountOfApplications(getTenantDomain(), getUsername(),
                        filter);
        // This is the chunk size configured for DB fetching.
        int chunkSize = getFetchChunkSizeForPagination();
        List<ApplicationBasicInfo> expectedFilteredAuthorizedAppInfoList = new ArrayList<>();
        expectedFilteredAuthorizedAppInfoList =
                getFilteredAuthorizedAppBasicInfo(expectedFilteredAuthorizedAppInfoList, authorizedUserTenantDomain,
                        authorizedUser, filter, offset, chunkSize, expectedNumberOfResults,
                        totalFilteredSystemAppCount);
        int startIndexOfRequestedPage = (itemsPerPage * (pageNumber - 1));
        int endIndexOfRequestedPage = expectedFilteredAuthorizedAppInfoList.size();
        if (startIndexOfRequestedPage > endIndexOfRequestedPage) {
            return new ApplicationBasicInfo[0];
        } else {
            return expectedFilteredAuthorizedAppInfoList.subList(startIndexOfRequestedPage, endIndexOfRequestedPage)
                    .toArray(new ApplicationBasicInfo[0]);
        }
    }

    /**
     * Method to get the remaining basic application information based on a filter for a requested page.
     *
     * @param expectedFilteredAuthorizedAppInfoList List of filtered authorized {@link ApplicationBasicInfo} instances.
     * @param authorizedUserTenantDomain            Tenant domain.
     * @param authorizedUser                        Authorized user.
     * @param filter                                Application name filter.
     * @param offset                                Starting index of the count.
     * @param chunkSize                             Chunk size configured for DB fetching.
     * @param expectedAuthorizedAppInfoCount        Total filtered authorized app info count needed to decide apps to be
     *                                              displayed for a requested page.
     * @param totalFilteredSystemAppCount           Count of all applications in the system for a matching filter.
     * @return List of remaining authorized {@link ApplicationBasicInfo} instances for pagination.
     * @throws IdentityApplicationManagementException Error in getting remaining basic application information.
     */
    private List<ApplicationBasicInfo> getFilteredAuthorizedAppBasicInfo(
            List<ApplicationBasicInfo> expectedFilteredAuthorizedAppInfoList, String authorizedUserTenantDomain,
            String authorizedUser, String filter, int offset, int chunkSize, int expectedAuthorizedAppInfoCount,
            int totalFilteredSystemAppCount) throws IdentityApplicationManagementException {

        ApplicationBasicInfo[] applicationBasicInfos;
        if (expectedAuthorizedAppInfoCount > chunkSize) {
            applicationBasicInfos =
                    ApplicationManagementService.getInstance().getApplicationBasicInfo(authorizedUserTenantDomain,
                            authorizedUser, filter, offset, chunkSize);
            offset += chunkSize;
        } else {
            applicationBasicInfos =
                    ApplicationManagementService.getInstance().getApplicationBasicInfo(authorizedUserTenantDomain,
                            authorizedUser, filter, offset, expectedAuthorizedAppInfoCount);
            offset += expectedAuthorizedAppInfoCount;
        }

        List<ApplicationBasicInfo> authorizedAppBasicInfo = getAuthorizedApplicationBasicInfo(applicationBasicInfos,
                authorizedUser);

        if (authorizedAppBasicInfo.size() == expectedAuthorizedAppInfoCount) {
            expectedFilteredAuthorizedAppInfoList.addAll(authorizedAppBasicInfo);
            return expectedFilteredAuthorizedAppInfoList;
        } else if (authorizedAppBasicInfo.size() > expectedAuthorizedAppInfoCount) {
            expectedFilteredAuthorizedAppInfoList.addAll(authorizedAppBasicInfo.subList(0,
                    expectedAuthorizedAppInfoCount));
            return expectedFilteredAuthorizedAppInfoList;
        } else {
            expectedFilteredAuthorizedAppInfoList.addAll(authorizedAppBasicInfo);
            if (log.isDebugEnabled()) {
                log.debug("No. of authorized app information found for user " + authorizedUser + " in tenant domain "
                        + authorizedUserTenantDomain + ": " + expectedFilteredAuthorizedAppInfoList.size());
            }

            if (offset >= totalFilteredSystemAppCount) {
                return expectedFilteredAuthorizedAppInfoList;
            }

            expectedAuthorizedAppInfoCount = expectedAuthorizedAppInfoCount - authorizedAppBasicInfo.size();
            if (log.isDebugEnabled()) {
                log.debug("No. of remaining authorized app information needed to be fetched from the DB for user "
                        + authorizedUser + " in tenant domain " + authorizedUserTenantDomain + ": "
                        + expectedAuthorizedAppInfoCount);
            }
            return getFilteredAuthorizedAppBasicInfo(expectedFilteredAuthorizedAppInfoList,
                    authorizedUserTenantDomain, authorizedUser, filter, offset, chunkSize,
                    expectedAuthorizedAppInfoCount, totalFilteredSystemAppCount);
        }
    }

    /**
     * Get count of all the applications authorized for the user.
     *
     * @return Number of applications
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public int getCountOfAllApplications() throws IdentityApplicationManagementException {

        int applicationCount;
        boolean validateRoles = ApplicationMgtUtil.validateRoles();
        if (!validateRoles) {
            if (log.isDebugEnabled()) {
                log.debug("Allowing the application access based on the role is disabled. " +
                        "Therefore sending count of all applications.");
            }
            applicationCount = ApplicationManagementService.getInstance().getCountOfAllApplications(getTenantDomain(),
                    getUsername());
        } else {
            /* Application role validation is enabled. Checking the
             number of applications the user has access to, based
             on the application role. */
            List<String> applicationRoles = getApplicationRolesOfUser(getUsername());
            applicationCount = getSynchronizedApplicationCount(applicationRoles, null);
        }

        return applicationCount;
    }

    /**
     * Get count of all the applications authorized for the user for a matching filter.
     *
     * @return Number of applications
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public int getCountOfApplications(String filter) throws IdentityApplicationManagementException {

        int applicationCount;
        boolean validateRoles = ApplicationMgtUtil.validateRoles();
        if (!validateRoles) {
            if (log.isDebugEnabled()) {
                log.debug("Allowing the application access based on the role is disabled. " +
                        "Therefore sending count of all matching applications.");
            }
            applicationCount = ApplicationManagementService.getInstance().getCountOfApplications(getTenantDomain(),
                    getUsername(), filter);
        } else {
            /* Application role validation is enabled. Checking the
             number of matching applications the user has access to,
             based on the application role. */
            String sanitizedFilter = getSanitizedFilter(filter);
            Pattern pattern = Pattern.compile(sanitizedFilter, Pattern.CASE_INSENSITIVE);
            List<String> applicationRoles = getApplicationRolesOfUser(getUsername());
            List<String> filteredApplicationRoles = new ArrayList<>();
            for (String applicationRole : applicationRoles) {
                Matcher matcher = pattern.matcher(applicationRole);
                if (matcher.matches()) {
                    filteredApplicationRoles.add(applicationRole);
                }
            }
            applicationCount = getSynchronizedApplicationCount(filteredApplicationRoles, filter);
        }

        return applicationCount;
    }

    /**
     * Retrieves the proper application count by considering both the application roles and the service providers/
     * applications currently available.
     *
     * @param applicationRoles Filtered/unfiltered application roles assigned for the user.
     * @param filter Filter provided to isolate service providers.
     * @return Synchronized application count according to the user roles and the available service providers.
     * @throws IdentityApplicationManagementException if there is an error while retrieving the basic info of all the
     * applications/service providers.
     */
    private int getSynchronizedApplicationCount(List<String> applicationRoles, String filter)
            throws IdentityApplicationManagementException {

        int synchronizedApplicationCount = 0;
        ApplicationBasicInfo[] applications;
        if (filter == null) {
            applications = getAllApplicationBasicInfo();
        } else {
            applications = getApplicationBasicInfo(filter);
        }
        for (ApplicationBasicInfo applicationBasicInfo : applications) {
            if (applicationRoles.contains(APPLICATION_ROLE_PREFIX + applicationBasicInfo.getApplicationName())) {
                synchronizedApplicationCount += 1;
            }
        }

        return synchronizedApplicationCount;
    }

    /**
     * Update application
     *
     * @param serviceProvider Service provider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public void updateApplication(ServiceProvider serviceProvider) throws IdentityApplicationManagementException {

        // check whether use is authorized to update the application.
        try {
            if (!ApplicationConstants.LOCAL_SP.equals(serviceProvider.getApplicationName()) &&
                    !ApplicationMgtUtil.isUserAuthorized(serviceProvider.getApplicationName(), getUsername(),
                            serviceProvider.getApplicationID())) {
                log.warn("Illegal Access! User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have access to the application " + serviceProvider.getApplicationName());
                throw new IdentityApplicationManagementException("User not authorized");
            }
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.updateApplication(serviceProvider, getTenantDomain(), getUsername());
        } catch (IdentityApplicationManagementException ex) {
            String msg = "Error while updating application: " + serviceProvider.getApplicationName() + " for " +
                    "tenant: " + getTenantDomain();
            throw handleException(msg, ex);
        }
    }

    /**
     * Delete Application
     *
     * @param applicationName Application name
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public void deleteApplication(String applicationName) throws IdentityApplicationManagementException {

        try {
            if (!ApplicationMgtUtil.isUserAuthorized(applicationName, getUsername())) {
                log.warn("Illegal Access! User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have access to the application " + applicationName);
                throw new IdentityApplicationManagementException("User not authorized");
            }
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.deleteApplication(applicationName, getTenantDomain(), getUsername());
        } catch (IdentityApplicationManagementException ex) {
            String msg = "Error while deleting application: " + applicationName + " for tenant: " + getTenantDomain();
            throw handleException(msg, ex);
        }
    }

    /**
     * Get identity provider by identity provider name
     *
     * @param federatedIdPName Federated identity provider name
     * @return Identity provider
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    public IdentityProvider getIdentityProvider(String federatedIdPName) throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getIdentityProvider(federatedIdPName, getTenantDomain());
        } catch (IdentityApplicationManagementException idpException) {
            log.error("Error while retrieving identity provider: " + federatedIdPName + " for tenant: " +
                    getTenantDomain(), idpException);
            throw idpException;

        }
    }

    /**
     * Get all identity providers
     *
     * @return Identity providers array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public IdentityProvider[] getAllIdentityProviders() throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getAllIdentityProviders(getTenantDomain());
        } catch (IdentityApplicationManagementException idpException) {
            log.error("Error while retrieving all identity providers for tenant: " + getTenantDomain(), idpException);
            throw idpException;

        }
    }

    /**
     * Get all local authenticators
     *
     * @return local authenticators array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public LocalAuthenticatorConfig[] getAllLocalAuthenticators() throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getAllLocalAuthenticators(getTenantDomain());
        } catch (IdentityApplicationManagementException idpException) {
            log.error("Error while retrieving all local authenticators for tenant: " + getTenantDomain(),
                    idpException);
            throw idpException;

        }
    }

    /**
     * Get all request path authenticator config
     *
     * @return Request path authenticator config array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public RequestPathAuthenticatorConfig[] getAllRequestPathAuthenticators()
            throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getAllRequestPathAuthenticators(getTenantDomain());
        } catch (IdentityApplicationManagementException idpException) {
            log.error("Error while retrieving all request path authenticators for tenant: " + getTenantDomain(),
                    idpException);
            throw idpException;

        }
    }

    /**
     * Get all local claim uris
     *
     * @return claim uri array
     * @throws org.wso2.carbon.identity.application.common.IdentityApplicationManagementException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public String[] getAllLocalClaimUris() throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getAllLocalClaimUris(getTenantDomain());
        } catch (IdentityApplicationManagementException idpException) {
            log.error("Error while retrieving all local claim URIs for tenant: " + getTenantDomain(), idpException);
            throw idpException;

        }
    }

    /**
     * Retrieve the set of authentication templates configured from file system in JSON format
     *
     * @return Authentication templates.
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public String getAuthenticationTemplatesJSON() {

        return applicationMgtService.getAuthenticationTemplatesJSON();
    }

    /**
     * Import application from XML file from UI.
     *
     * @param spFileContent xml string of the SP and file name
     * @return Created application name
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public ImportResponse importApplication(SpFileContent spFileContent) throws IdentityApplicationManagementException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.importSPApplication(spFileContent, getTenantDomain(), getUsername(), false);
        } catch (IdentityApplicationManagementException ex) {
            String message = "Error while importing application for tenant: " + getTenantDomain();
            throw handleException(message, ex);
        }
    }

    /**
     * Export service provider as XML.
     *
     * @param applicationName Name of the application to be exported
     * @return XML content of the service provider
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public String exportApplication(String applicationName, boolean exportSecrets) throws
            IdentityApplicationManagementException {

        try {
            if (ApplicationConstants.LOCAL_SP.equals(applicationName)) {
                log.warn("Illegal access! Local service provider can't be exported");
                throw new IdentityApplicationManagementException("Local service provider can't be exported");
            }
            if (!ApplicationMgtUtil.isUserAuthorized(applicationName, getUsername())) {
                log.warn("Illegal Access! User " + CarbonContext.getThreadLocalCarbonContext().getUsername() +
                        " does not have export the application " + applicationName);
                throw new IdentityApplicationManagementException("User not authorized");
            }
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.exportSPApplication(applicationName, exportSecrets, getTenantDomain());
        } catch (IdentityApplicationManagementException ex) {
            String msg = "Error while exporting application: " + applicationName + " for tenant: " + getTenantDomain();
            throw handleException(msg, ex);
        }
    }

    /**
     * Create Service provider template.
     *
     * @param spTemplate service provider template info
     * @throws IdentityApplicationManagementClientException
     */
    public void createApplicationTemplate(SpTemplate spTemplate) throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.createApplicationTemplate(spTemplate, getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while creating application template: %s for tenant: %s.",
                    spTemplate.getName(), getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Add configured service provider as a template.
     *
     * @param serviceProvider Service provider to be configured as a template
     * @param spTemplate      service provider template basic info
     * @throws IdentityApplicationManagementClientException
     */
    public void createApplicationTemplateFromSP(ServiceProvider serviceProvider, SpTemplate spTemplate)
            throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.createApplicationTemplateFromSP(serviceProvider, spTemplate,
                    getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while creating service provider template for the configured SP: %s for " +
                    "tenant: %s.", serviceProvider.getApplicationName(), getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Get Service provider template.
     *
     * @param templateName template name
     * @return service provider template info
     * @throws IdentityApplicationManagementClientException
     */
    public SpTemplate getApplicationTemplate(String templateName) throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getApplicationTemplate(templateName, getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while retrieving application template: %s for tenant: %s.",
                    templateName, getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Delete an application template.
     *
     * @param templateName name of the template
     * @throws IdentityApplicationManagementClientException
     */
    public void deleteApplicationTemplate(String templateName) throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.deleteApplicationTemplate(templateName, getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while deleting application template: %s in tenant: %s.",
                    templateName, getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Update an application template.
     *
     * @param templateName name of the template
     * @param spTemplate   SP template info to be updated
     * @throws IdentityApplicationManagementClientException
     */
    public void updateApplicationTemplate(String templateName, SpTemplate spTemplate)
            throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            applicationMgtService.updateApplicationTemplate(templateName, spTemplate, getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while updating application template: %s in tenant: %s.",
                    spTemplate.getName(), getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Check existence of a application template.
     *
     * @param templateName template name
     * @return true if a template with the specified name exists
     * @throws IdentityApplicationManagementClientException
     */
    public boolean isExistingApplicationTemplate(String templateName)
            throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.isExistingApplicationTemplate(templateName, getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while checking existence of application template: %s in tenant: %s.",
                    templateName, getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Get template info of all the service provider templates.
     *
     * @return list of all application template info
     * @throws IdentityApplicationManagementClientException
     */
    @SuppressWarnings("ValidExternallyBoundObject")
    public List<SpTemplate> getAllApplicationTemplateInfo() throws IdentityApplicationManagementClientException {

        try {
            applicationMgtService = ApplicationManagementService.getInstance();
            return applicationMgtService.getAllApplicationTemplateInfo(getTenantDomain());
        } catch (IdentityApplicationManagementClientException e) {
            throw e;
        } catch (IdentityApplicationManagementException e) {
            log.error(String.format("Error while getting all the application template basic info for tenant: %s.",
                    getTenantDomain()), e);
            throw new IdentityApplicationManagementClientException(new String[]{"Server error occurred."});
        }
    }

    /**
     * Return a list of custom inbound authenticators.
     *
     * @return Map<String, InboundAuthenticationRequestConfig>
     */
    public List<InboundAuthenticationRequestConfig> getCustomInboundAuthenticatorConfigs() {

        if (customInboundAuthenticatorConfigs != null) {
            return customInboundAuthenticatorConfigs;
        }
        generateCustomInboundAuthenticatorConfigs();
        return customInboundAuthenticatorConfigs;
    }

    private void generateCustomInboundAuthenticatorConfigs() {

        List<InboundAuthenticationRequestConfig> customAuthenticatorConfigs = new ArrayList<>();
        Map<String, AbstractInboundAuthenticatorConfig> customInboundAuthenticators =
                ApplicationManagementServiceComponentHolder.getAllInboundAuthenticatorConfig();
        if (customInboundAuthenticators != null && customInboundAuthenticators.size() > 0) {
            for (Map.Entry<String, AbstractInboundAuthenticatorConfig> entry :
                    customInboundAuthenticators.entrySet()) {
                AbstractInboundAuthenticatorConfig inboundAuthenticatorConfig = entry.getValue();
                InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig =
                        new InboundAuthenticationRequestConfig();
                inboundAuthenticationRequestConfig.setInboundAuthType(inboundAuthenticatorConfig.getName());
                inboundAuthenticationRequestConfig.setInboundConfigType(inboundAuthenticatorConfig.getConfigName());
                inboundAuthenticationRequestConfig.setFriendlyName(inboundAuthenticatorConfig.getFriendlyName());
                inboundAuthenticationRequestConfig.setProperties(inboundAuthenticatorConfig
                        .getConfigurationProperties());

                customAuthenticatorConfigs.add(inboundAuthenticationRequestConfig);
            }
        }
        this.customInboundAuthenticatorConfigs = customAuthenticatorConfigs;
    }

    private ArrayList<ApplicationBasicInfo> getAuthorizedApplicationBasicInfo(
            ApplicationBasicInfo[] applicationBasicInfos, String userName)
            throws IdentityApplicationManagementException {

        ArrayList<ApplicationBasicInfo> appInfo = new ArrayList<>();
        for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
            if (ApplicationMgtUtil.isUserAuthorized(applicationBasicInfo.getApplicationName(), userName)) {
                appInfo.add(applicationBasicInfo);
                if (log.isDebugEnabled()) {
                    log.debug("Retrieving basic information of application: " +
                            applicationBasicInfo.getApplicationName() + "username: " + userName);
                }
            }
        }
        return appInfo;
    }

    /**
     * Method to get the FetchChunkSize property configured in the identity.xml file.
     *
     * @return FetchChunkSize property.
     */
    private int getFetchChunkSizeForPagination() {

        String fetchChunkSizeForPagination =
                IdentityUtil.getProperty(ApplicationConstants.SERVICE_PROVIDERS + "." +
                        ApplicationConstants.FETCH_CHUNK_SIZE);

        try {
            if (StringUtils.isNotBlank(fetchChunkSizeForPagination)) {
                int fetchChunkSize = Math.abs(Integer.parseInt(fetchChunkSizeForPagination));
                if (log.isDebugEnabled()) {
                    log.debug("Fetch chunk size property is set to : " + fetchChunkSize);
                }
                return fetchChunkSize;
            }
        } catch (NumberFormatException e) {
            // No need to handle exception since default value is already set.
            log.warn("Error occurred while parsing the 'FetchChunkSize' property value in identity.xml."
                    + " Defaulting to: " + ApplicationConstants.DEFAULT_FETCH_CHUNK_SIZE);
        }

        return ApplicationConstants.DEFAULT_FETCH_CHUNK_SIZE;
    }

    /**
     * Method to retrieve all the application roles of a user.
     *
     * @param username User name.
     * @return Application role list.
     * @throws IdentityApplicationManagementException Error in retrieving roles of a user.
     */
    private List<String> getApplicationRolesOfUser(String username) throws IdentityApplicationManagementException {

        try {
            String[] userRoles = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                    getUserStoreManager().getRoleListOfUser(username);
            List<String> applicationRoles = new ArrayList<>();
            if (userRoles != null) {
                String applicationRoleDomain =
                        ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR;
                for (String role : userRoles) {
                    if (role.startsWith(applicationRoleDomain)) {
                        applicationRoles.add(role);
                    }
                }
            }
            return applicationRoles;
        } catch (UserStoreException e) {
            throw new IdentityApplicationManagementException("Error while retrieving application roles for user: " +
                    username, e);
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
            throw new IdentityApplicationManagementException("Invalid page number requested. The page number should " +
                    "be a value greater than 0.");
        }
    }

    /**
     * Checks whether the start index of the requested page is less than the user's total authorized app count.
     *
     * @param authorizedUser              Authorized user.
     * @param pageNumber                  Page number.
     * @param totalUserAuthorizedAppCount Total number of apps authorized for a user.
     * @param itemsPerPage                Apps to be displayed per page.
     * @return Whether the requested page has exceeded the maximum page value.
     * @throws IdentityApplicationManagementException
     */
    private boolean isMaxPageNumberToRequestExceeded(String authorizedUser, int pageNumber,
                                                     int totalUserAuthorizedAppCount, int itemsPerPage) {

        int numberOfPages = (int) Math.ceil((double) totalUserAuthorizedAppCount / itemsPerPage);
        // Validate whether the start index of the requested page is less than the user's total authorized app count.
        int startIndexOfRequestedPage = (itemsPerPage * (pageNumber - 1)) + 1;
        if (totalUserAuthorizedAppCount < startIndexOfRequestedPage) {
            if (log.isDebugEnabled()) {
                log.debug("The requested page number exceeds the total number of applications authorized for the " +
                        "user: " + authorizedUser + ". Pages can be requested only upto page " + numberOfPages + ".");
            }
            return true;
        }

        return false;
    }

    /**
     * Sanitize the filter to fetch application roles.
     *
     * @param filter Application name filter.
     * @return Sanitized filter string.
     */
    private String getSanitizedFilter(String filter) {

        if (StringUtils.isNotBlank(filter)) {
            filter = filter.replace("*", ".*");
            filter = ApplicationConstants.APPLICATION_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR + filter;
        } else {
            filter = ".*";
        }

        return filter;
    }
}
