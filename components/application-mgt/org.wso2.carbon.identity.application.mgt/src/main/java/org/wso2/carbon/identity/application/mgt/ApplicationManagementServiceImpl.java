/*
 * Copyright (c) 2014-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.w3c.dom.Document;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementValidationException;
import org.wso2.carbon.identity.application.common.IdentityApplicationRegistrationFailureException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.common.model.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.AuthenticationStep;
import org.wso2.carbon.identity.application.common.model.DefaultAuthenticationSequence;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ImportResponse;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.PermissionsAndRoleConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProviderProperty;
import org.wso2.carbon.identity.application.common.model.SpFileContent;
import org.wso2.carbon.identity.application.common.model.SpTemplate;
import org.wso2.carbon.identity.application.common.model.TrustedApp;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCache;
import org.wso2.carbon.identity.application.mgt.cache.IdentityServiceProviderCacheKey;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderTemplateCache;
import org.wso2.carbon.identity.application.mgt.cache.ServiceProviderTemplateCacheKey;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.ApplicationTemplateDAO;
import org.wso2.carbon.identity.application.mgt.dao.IdentityProviderDAO;
import org.wso2.carbon.identity.application.mgt.dao.PaginatableFilterableApplicationDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.AbstractApplicationDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.FileBasedApplicationDAO;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtException;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtService;
import org.wso2.carbon.identity.application.mgt.defaultsequence.DefaultAuthSeqMgtServiceImpl;
import org.wso2.carbon.identity.application.mgt.inbound.dto.ApplicationDTO;
import org.wso2.carbon.identity.application.mgt.inbound.dto.InboundProtocolConfigurationDTO;
import org.wso2.carbon.identity.application.mgt.inbound.dto.InboundProtocolsDTO;
import org.wso2.carbon.identity.application.mgt.inbound.protocol.ApplicationInboundAuthConfigHandler;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponent;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.application.mgt.listener.AbstractApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationMgtListener;
import org.wso2.carbon.identity.application.mgt.listener.ApplicationResourceManagementListener;
import org.wso2.carbon.identity.application.mgt.validator.ApplicationValidatorManager;
import org.wso2.carbon.identity.central.log.mgt.utils.LogConstants;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;
import org.wso2.carbon.idp.mgt.model.ConnectedAppsResult;
import org.wso2.carbon.idp.mgt.util.IdPManagementConstants;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.utils.AuditLog;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.APPLICATION_ALREADY_EXISTS;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.APPLICATION_NOT_FOUND;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_TENANT_DOMAIN;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.OPERATION_FORBIDDEN;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.PlatformType;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.APPLICATION_NAME_CONFIG_ELEMENT;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.DEFAULT_APPLICATIONS_CONFIG_ELEMENT;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.SYSTEM_APPLICATIONS_CONFIG_ELEMENT;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.buildSPData;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.endTenantFlow;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.getAppId;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.getUser;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.isRegexValidated;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.startTenantFlow;
import static org.wso2.carbon.identity.application.mgt.ApplicationMgtUtil.validateTenant;
import static org.wso2.carbon.identity.application.mgt.inbound.InboundFunctions.doRollback;
import static org.wso2.carbon.identity.application.mgt.inbound.InboundFunctions.rollbackInbounds;
import static org.wso2.carbon.identity.application.mgt.inbound.InboundFunctions.updateOrInsertInbound;
import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.triggerAuditLogEvent;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getInitiatorId;
import static org.wso2.carbon.identity.core.util.IdentityUtil.isValidPEMCertificate;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_MANAGEMENT_ERROR_CODE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

/**
 * Application management service implementation.
 */
public class ApplicationManagementServiceImpl extends ApplicationManagementService {

    private static final Log log = LogFactory.getLog(ApplicationManagementServiceImpl.class);
    private static volatile ApplicationManagementServiceImpl appMgtService;
    private ApplicationValidatorManager applicationValidatorManager = new ApplicationValidatorManager();
    private String message;

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
    public ServiceProvider addApplication(ServiceProvider serviceProvider, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        return createApplicationWithTemplate(serviceProvider, tenantDomain, username, null);
    }

    @Override
    public ServiceProvider createApplicationWithTemplate(ServiceProvider serviceProvider, String tenantDomain,
                                                         String username, String templateName)
            throws IdentityApplicationManagementException {

        // Call pre listeners.
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreCreateApplication(serviceProvider, tenantDomain, username)) {
                throw buildServerException("Pre create application operation of listener: "
                        + getName(listener) + " failed for application: " + serviceProvider.getApplicationName() +
                        " of tenantDomain: " + tenantDomain);
            }
        }

        // Set default application version.
        serviceProvider.setApplicationVersion(ApplicationConstants.ApplicationVersion.LATEST_APP_VERSION);

        doPreAddApplicationChecks(serviceProvider, tenantDomain, username);
        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        serviceProvider.setOwner(getUser(tenantDomain, username).orElseThrow(() ->
                new IdentityApplicationManagementException("Error resolving service provider owner.")));

        int appId = doAddApplication(serviceProvider, tenantDomain, username, appDAO::createApplication);
        serviceProvider.setApplicationID(appId);
        setDisplayNamesOfLocalAuthenticators(serviceProvider, tenantDomain);
        SpTemplate spTemplate = this.getApplicationTemplate(templateName, tenantDomain);
        if (spTemplate != null) {
            updateSpFromTemplate(serviceProvider, tenantDomain, spTemplate);
            appDAO.updateApplication(serviceProvider, tenantDomain);
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostCreateApplication(serviceProvider, tenantDomain, username)) {
                log.error("Post create application operation of listener:" + getName(listener) + " failed for " +
                        "application: " + serviceProvider.getApplicationName() + " of tenantDomain: " + tenantDomain);
                break;
            }
        }
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain),  LoggerUtils.Target.User.name(),
                    getAppId(serviceProvider), LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.CREATE_APPLICATION_ACTION)
                    .data(buildSPData(serviceProvider));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return serviceProvider;
    }

    @Override
    public ServiceProvider getApplicationExcludingFileBasedSPs(String applicationName, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider;
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetApplicationExcludingFileBasedSPs(applicationName,
                    tenantDomain)) {
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
            if (listener.isEnable() && !listener.doPostGetApplicationExcludingFileBasedSPs(serviceProvider,
                    applicationName, tenantDomain)) {
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
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
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

    @Override
    public ApplicationBasicInfo[] getApplicationBasicInfoBySPProperty(String tenantDomain, String username,
                                                                      String key, String value)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO;
        ApplicationBasicInfo[] applicationBasicInfos;
        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.getApplicationBasicInfoBySPProperty(tenantDomain, username, key,
                    value)) {
                return new ApplicationBasicInfo[0];
            }
        }

        try {
            startTenantFlow(tenantDomain, username);
            appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();

            if (!(appDAO instanceof AbstractApplicationDAOImpl)) {
                log.error("Get application basic info service is not supported.");
                throw new IdentityApplicationManagementException("This service is not supported.");
            }

            applicationBasicInfos = ((AbstractApplicationDAOImpl) appDAO)
                    .getApplicationBasicInfoBySPProperty(key, value);
        } finally {
            endTenantFlow();
        }

        return applicationBasicInfos;
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
                Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
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
                        getApplicationMgtListeners();
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
                Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
                for (ApplicationMgtListener listener : listeners) {
                    if (listener.isEnable() && listener instanceof AbstractApplicationMgtListener &&
                            !((AbstractApplicationMgtListener) listener).doPreGetPaginatedApplicationBasicInfo(
                                    tenantDomain, username,
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
                        getApplicationMgtListeners();
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

    @Override
    public ApplicationBasicInfo getApplicationBasicInfoByName(String name, String tenantDomain)
            throws IdentityApplicationManagementException {

        Collection<ApplicationResourceManagementListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationResourceMgtListeners();

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPreGetApplicationBasicInfoByName(name, tenantDomain)) {
                throw buildServerException("Error executing doPreGetApplicationBasicInfoByName operation of " +
                        "listener: " + getName(listener) + " for application name: " + name);
            }
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ApplicationBasicInfo basicAppInfo = appDAO.getApplicationBasicInfoByName(name, tenantDomain);

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPostGetApplicationBasicInfoByName(basicAppInfo, name, tenantDomain)) {
                throw buildServerException("Error executing doPostGetApplicationBasicInfoByName operation of " +
                        "listener: " + getName(listener) + " for application name: " + name);
            }
        }
        return basicAppInfo;
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
    public int getCountOfAllApplications(String tenantDomain, String username)
            throws IdentityApplicationManagementException {

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

        validateApplicationConfigurations(serviceProvider, tenantDomain, username);

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreUpdateApplication(serviceProvider, tenantDomain, username)) {
                throw buildServerException("Pre Update application failed");
            }
        }

        String applicationName = serviceProvider.getApplicationName();
        try {
            // check whether user is authorized to update the application.
            startTenantFlow(tenantDomain, username);

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            String storedAppName = appDAO.getApplicationName(serviceProvider.getApplicationID());
            if (StringUtils.isBlank(storedAppName)) {
                // This means the application is not a valid one.
                String msg = "Cannot find application with id: " + serviceProvider.getApplicationID() + " in " +
                        "tenantDomain: " + tenantDomain;
                throw buildClientException(APPLICATION_NOT_FOUND, msg);
            }

            // Updating the isManagement flag of application is blocked. So updating it to stored value
            boolean isManagementApp = appDAO.getApplication(serviceProvider.getApplicationID())
                    .isManagementApp();
            serviceProvider.setManagementApp(isManagementApp);

            // Updating the isB2BSelfService flag of application is blocked, thus set it to stored value
            serviceProvider.setB2BSelfServiceApp(appDAO.getApplication(serviceProvider.getApplicationID())
                    .isB2BSelfServiceApp());

            doPreUpdateChecks(storedAppName, serviceProvider, tenantDomain, username);
            appDAO.updateApplication(serviceProvider, tenantDomain);
            if (isOwnerUpdatedInRequest(serviceProvider)) {
                //It is not required to validate the user here, as the user is validating inside the updateApplication
                // method above. Hence assign application role to the app owner.
                assignApplicationRole(serviceProvider.getApplicationName(),
                        MultitenantUtils.getTenantAwareUsername(serviceProvider.getOwner().toFullQualifiedUsername()));
            }

            updateApplicationPermissions(serviceProvider, applicationName, storedAppName);
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
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain), LoggerUtils.Target.User.name(),
                    getAppId(serviceProvider), LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.UPDATE_APPLICATION_ACTION)
                    .data(buildSPData(serviceProvider));
            triggerAuditLogEvent(auditLogBuilder, true);
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
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreDeleteApplication(applicationName, tenantDomain, username)) {
                throw buildServerException("Pre Delete application operation of listener: " + getName(listener) +
                        " failed for application: " + applicationName + " of tenantDomain: " + tenantDomain);
            }
        }

        try {
            startTenantFlow(tenantDomain, username);
            doPreDeleteChecks(applicationName, tenantDomain, username);

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            serviceProvider = appDAO.getApplication(applicationName, tenantDomain);

            if (serviceProvider != null) {

                ApplicationMgtUtil.deleteAppRole(applicationName);
                ApplicationMgtUtil.deletePermissions(applicationName);

                appDAO.deleteApplication(applicationName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Application cannot be found for name: " + applicationName +
                            " in tenantDomain: " + tenantDomain);
                }
                return;
            }

        } catch (Exception e) {
            String error = "Error occurred while deleting the application: " + applicationName + ". " + e.getMessage();
            throw buildServerException(error, e);
        } finally {
            endTenantFlow();
        }

        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostDeleteApplication(serviceProvider, tenantDomain, username)) {
                log.error("Post Delete application operation of listener: " + getName(listener) + " failed for " +
                        "application with name: " + applicationName + " of tenantDomain: " + tenantDomain);
                return;
            }
        }
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain), LoggerUtils.Target.User.name(), getAppId(serviceProvider),
                    LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.DELETE_APPLICATION_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    /**
     * Delete Applications by tenant id.
     *
     * @param tenantId The id of the tenant.
     * @throws IdentityApplicationManagementException throws when an error occurs in deleting applications.
     */
    @Override
    public void deleteApplications(int tenantId) throws IdentityApplicationManagementException {

        String tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
        ApplicationBasicInfo[] applicationBasicInfos = getAllApplicationBasicInfo(
                tenantDomain, CarbonContext.getThreadLocalCarbonContext().getUsername());

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        appDAO.deleteApplications(tenantId);

        // Clear cache entries of each deleted SP.
        if (log.isDebugEnabled()) {
            log.debug("Clearing the cache entries of all SP applications of the tenant: " + tenantDomain);
        }

        for (ApplicationBasicInfo applicationBasicInfo : applicationBasicInfos) {
            IdentityServiceProviderCache.getInstance().clearCacheEntry(
                    new IdentityServiceProviderCacheKey(applicationBasicInfo.getApplicationName()), tenantDomain);
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
    public ConnectedAppsResult getConnectedAppsForLocalAuthenticator(String authenticatorId, String tenantDomain,
                                                                     Integer limit, Integer offset)
            throws IdentityApplicationManagementException {

        int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new IdentityApplicationManagementClientException(INVALID_TENANT_DOMAIN.getCode(),
                   "Invalid tenant domain: " + tenantDomain);
        }
        validateResourceId(authenticatorId, tenantDomain);
        IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
        ConnectedAppsResult connectedAppsOfLocalAuthenticator = idpdao
                .getConnectedAppsOfLocalAuthenticator(authenticatorId, tenantId, limit, offset);
        return connectedAppsOfLocalAuthenticator;
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

            List<String> claimUris = new ArrayList<>();
            if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equalsIgnoreCase(claimDialect)) {
                // Local claims are retrieved via ClaimMetadataManagement service for consistency.
                List<LocalClaim> localClaims = ApplicationManagementServiceComponentHolder.getInstance()
                        .getClaimMetadataManagementService().getLocalClaims(tenantDomain);
                claimUris = getLocalClaimURIs(localClaims);
            } else {
                ClaimMapping[] claimMappings = CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                        .getClaimManager().getAllClaimMappings(claimDialect);
                for (ClaimMapping claimMap : claimMappings) {
                    claimUris.add(claimMap.getClaim().getClaimUri());
                }
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
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderNameByClientIdExcludingFileBasedSPs(name,
                    clientId, type, tenantDomain)) {
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
            if (listener.isEnable() && !listener.doPostGetServiceProviderNameByClientIdExcludingFileBasedSPs(name,
                    clientId, type, tenantDomain)) {
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

        validateTenant(tenantDomain);
        String name = null;

        // invoking the listeners
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderNameByClientId(clientId, clientType,
                    tenantDomain)) {
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
            if (listener.isEnable() && !listener.doPostGetServiceProviderNameByClientId(name, clientId, clientType,
                    tenantDomain)) {
                return null;
            }
        }

        return name;

    }

    /**
     * Retrieve application resource id using the inboundKey and inboundType.
     *
     * @param inboundKey   inboundKey
     * @param inboundType  inboundType
     * @param tenantDomain tenantDomain
     * @return application resourceId
     * @throws IdentityApplicationManagementException IdentityApplicationManagementException
     */
    @Override
    public String getApplicationResourceIDByInboundKey(String inboundKey, String inboundType,
                                                       String tenantDomain)
            throws IdentityApplicationManagementException {

        if (StringUtils.isEmpty(inboundKey) || StringUtils.isEmpty(inboundType) || StringUtils.isEmpty(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Error while retrieving resource id. One of inboundKey, inboundType or tenantDomain " +
                        "parameters were found to be empty.");
            }
            return null;
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        String resourceId =  appDAO.getApplicationResourceIDByInboundKey(inboundKey, inboundType, tenantDomain);

        if (resourceId == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find an application resourceId for inboundKey: " + inboundKey +
                        " inboundType: " + inboundType + " in tenantDomain: " + tenantDomain);
            }
        }
        return resourceId;
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
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
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

    @Override
    public ServiceProvider getApplicationWithRequiredAttributes(int applicationId, List<String> requiredAttributes)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getApplicationWithRequiredAttributes(applicationId, requiredAttributes);
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
        String tenantDomain = serviceProvider.getTenantDomain();

        // TODO: Since we didn't add post listener methods to the ApplicationMgtListener API to avoid API changes, we
        // TODO: are invoking doPostGetServiceProvider(serviceProvider, serviceProviderName, tenantDomain) listener
        // TODO: method here as well.
        // invoking the post listeners
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPostGetServiceProvider(serviceProvider, serviceProviderName,
                    tenantDomain)) {
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
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable() && !listener.doPreGetServiceProviderByClientId(clientId, clientType,
                    tenantDomain)) {
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
            if (listener.isEnable() && !listener.doPostGetServiceProviderByClientId(serviceProvider, clientId,
                    clientType, tenantDomain)) {
                return null;
            }
        }

        return serviceProvider;
    }

    /**
     * Import Service Provider application from file.
     *
     * @param spFileContent XML string of the Service Provider and file name.
     * @param tenantDomain  Tenant Domain name.
     * @param username      User performing the operation.
     * @param isUpdate      Whether to update an existing Service Provider or create a new one.
     * @return ImportResponse
     * @throws IdentityApplicationManagementException Identity Application Management Exception.
     */
    public ImportResponse importSPApplication(SpFileContent spFileContent, String tenantDomain, String username,
                                              boolean isUpdate) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Importing service provider from file " + spFileContent.getFileName());
        }

        ServiceProvider serviceProvider = unmarshalSP(spFileContent, tenantDomain);
        ImportResponse importResponse = importSPApplication(serviceProvider, tenantDomain, username, isUpdate);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Service provider %s@%s created successfully from file %s",
                    serviceProvider.getApplicationName(), tenantDomain, spFileContent.getFileName()));
        }

        return importResponse;
    }

    /**
     * Import Service Provider application from object.
     *
     * @param serviceProvider Service Provider object.
     * @param tenantDomain    Tenant Domain name.
     * @param username        User performing the operation.
     * @param isUpdate        Whether to update an existing Service Provider or create a new one.
     * @return ImportResponse
     * @throws IdentityApplicationManagementException
     */
    public ImportResponse importSPApplication(ServiceProvider serviceProvider, String tenantDomain, String username,
                                              boolean isUpdate) throws IdentityApplicationManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Importing service provider from object " + serviceProvider.getApplicationName());
        }

        ImportResponse importResponse = importApplication(serviceProvider, tenantDomain, username, isUpdate);

        if (log.isDebugEnabled()) {
            log.debug(String.format("Service provider %s@%s created successfully from object",
                    serviceProvider.getApplicationName(), tenantDomain));
        }

        return importResponse;
    }

    private void validateResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (StringUtils.isEmpty(resourceId)) {
            String message = "Invalid argument: Authenticator resource ID value is empty";
            throw new IdentityApplicationManagementClientException(message);
        }
        String authenticatorName = new String(Base64.getUrlDecoder().decode(resourceId), StandardCharsets.UTF_8);
        int filteredCount = 0;
        try {
            startTenantFlow(tenantDomain);
            IdentityProviderDAO idpdao = ApplicationMgtSystemConfig.getInstance().getIdentityProviderDAO();
            List<LocalAuthenticatorConfig> localAuthenticators = idpdao.getAllLocalAuthenticators();
            if (localAuthenticators != null) {
                filteredCount = (int) localAuthenticators.stream()
                        .filter(authenticatorConfig ->
                                authenticatorConfig.getName()
                                        .equals(authenticatorName)).count();
            }
        } catch (IdentityApplicationManagementException e) {
            throw new IdentityApplicationManagementException(
                    String.format(IdPManagementConstants.ErrorMessage
                            .ERROR_CODE_GET_CONNECTED_APPS_REQUEST_INVALID.getMessage(), resourceId));
        } finally {
            endTenantFlow();
        }

        if (filteredCount == 0) {
            throw new IdentityApplicationManagementClientException(Error.AUTHENTICATOR_NOT_FOUND.getCode(),
                    String.format(IdPManagementConstants.ErrorMessage
                            .ERROR_CODE_IDP_DOES_NOT_EXIST.getMessage(), resourceId));
        }
    }

    private ImportResponse importApplication(ServiceProvider serviceProvider, String tenantDomain, String username,
                                             boolean isUpdate) throws IdentityApplicationManagementException {

        Collection<ApplicationMgtListener> listeners =
                getApplicationMgtListeners();

        ServiceProvider savedSP = null;
        String appName = serviceProvider.getApplicationName();
        try {
            if (isUpdate) {
                savedSP = getApplicationExcludingFileBasedSPs(appName, tenantDomain);
                if (savedSP == null) {
                    String errorMsg = String.format("Service provider %s@%s is not found", appName, tenantDomain);
                    throw new IdentityApplicationManagementClientException(APPLICATION_NOT_FOUND.getCode(), errorMsg);
                }
            }

            if (!isUpdate) {
                ServiceProvider basicApplication = new ServiceProvider();
                basicApplication.setApplicationName(serviceProvider.getApplicationName());
                basicApplication.setDescription(serviceProvider.getDescription());

                if (serviceProvider.isManagementApp()) {
                    basicApplication.setManagementApp(true);
                }

                String resourceId = createApplication(basicApplication, tenantDomain, username);
                savedSP = getApplicationByResourceId(resourceId, tenantDomain);
            }

            serviceProvider.setApplicationResourceId(savedSP.getApplicationResourceId());
            serviceProvider.setApplicationID(savedSP.getApplicationID());
            serviceProvider.setOwner(getUser(tenantDomain, username).orElseThrow(() ->
                            new IdentityApplicationManagementException("Error resolving service provider owner.")));
            serviceProvider.setSpProperties(savedSP.getSpProperties());
            serviceProvider.setApplicationVersion(savedSP.getApplicationVersion());

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

            ImportResponse importResponse = new ImportResponse();
            if (isUpdate) {
                importResponse.setResponseCode(ImportResponse.UPDATED);
            } else {
                importResponse.setResponseCode(ImportResponse.CREATED);
            }

            importResponse.setApplicationName(appName);
            importResponse.setApplicationResourceId(serviceProvider.getApplicationResourceId());
            importResponse.setErrors(new String[0]);
            return importResponse;
        } catch (IdentityApplicationManagementClientException e) {
            deleteCreatedSP(savedSP, tenantDomain, username, isUpdate);
            return buildImportErrorResponse(e);
        } catch (IdentityApplicationManagementException e) {
            deleteCreatedSP(savedSP, tenantDomain, username, isUpdate);
            String errorMsg = String.format("Error in importing provided service provider %s@%s from file ",
                    appName, tenantDomain);
            throw new IdentityApplicationManagementException(errorMsg, e);
        }
    }

    private ImportResponse buildImportErrorResponse(IdentityApplicationManagementClientException e) {

        ImportResponse importResponse = new ImportResponse();
        importResponse.setResponseCode(ImportResponse.FAILED);
        importResponse.setApplicationName(null);

        String errorCode = e.getErrorCode() != null ? e.getErrorCode() : INVALID_REQUEST.getCode();
        importResponse.setErrorCode(errorCode);

        if (e instanceof IdentityApplicationManagementValidationException) {
            importResponse.setErrors(((IdentityApplicationManagementValidationException) e).getValidationMsg());
        } else {
            String message = e.getMessage();
            if (StringUtils.isNotBlank(message)) {
                importResponse.setErrors(new String[]{e.getMessage()});
            }
        }

        return importResponse;
    }

    /**
     * Export Service Provider application using application ID.
     *
     * @param applicationId ID of the Service Provider.
     * @param exportSecrets Whether to export the secrets or not.
     * @param tenantDomain  Tenant domain name.
     * @return XML string of the Service Provider.
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    @Override
    public String exportSPApplicationFromAppID(String applicationId, boolean exportSecrets,
                                               String tenantDomain) throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = exportSPFromAppID(applicationId, exportSecrets, tenantDomain);
        return marshalSP(serviceProvider, tenantDomain);
    }

    /**
     * Export Service Provider application using application ID.
     *
     * @param applicationId ID of the Service Provider.
     * @param exportSecrets Whether to export the secrets or not.
     * @param tenantDomain  Tenant domain name.
     * @return Service Provider.
     * @throws IdentityApplicationManagementException Identity Application Management Exception.
     */
    @Override
    public ServiceProvider exportSPFromAppID(String applicationId, boolean exportSecrets,
                                               String tenantDomain) throws IdentityApplicationManagementException {

        ApplicationBasicInfo application = getApplicationBasicInfoByResourceId(applicationId, tenantDomain);

        if (application == null) {
            throw buildClientException(APPLICATION_NOT_FOUND, "Application could not be found " +
                    "for the provided resourceId: " + applicationId);
        }

        String appName = application.getApplicationName();

        try {
            startTenantFlow(tenantDomain);
            return exportSP(appName, exportSecrets, tenantDomain);
        } finally {
            endTenantFlow();
        }

    }

    /**
     * Export Service Provider application.
     *
     * @param applicationName Name of the Service Provider.
     * @param exportSecrets   Whether to export the secrets or not.
     * @param tenantDomain    Tenant domain name.
     * @return XML string of the Service Provider.
     * @throws IdentityApplicationManagementException Identity Application Management Exception
     */
    public String exportSPApplication(String applicationName, boolean exportSecrets, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = exportSP(applicationName, exportSecrets, tenantDomain);
        return marshalSP(serviceProvider, tenantDomain);
    }

    /**
     * Export Service Provider application.
     *
     * @param applicationName Name of the Service Provider.
     * @param exportSecrets   Whether to export the secrets or not.
     * @param tenantDomain    Tenant domain name.
     * @return Service Provider.
     * @throws IdentityApplicationManagementException Identity Application Management Exception.
     */
    public ServiceProvider exportSP(String applicationName, boolean exportSecrets, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider serviceProvider = getApplicationExcludingFileBasedSPs(applicationName, tenantDomain);
        ServiceProvider serviceProviderCopy = SerializationUtils.clone(serviceProvider);

        // Invoking the listeners.
        Collection<ApplicationMgtListener> listeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : listeners) {
            if (listener.isEnable()) {
                listener.doExportServiceProvider(serviceProviderCopy, exportSecrets);
            }
        }

        return serviceProviderCopy;
    }

    @Override
    public void createApplicationTemplate(SpTemplate spTemplate, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            ServiceProvider serviceProvider = unmarshalSPTemplate(spTemplate.getContent());
            // Set default application version.
            if (StringUtils.isBlank(serviceProvider.getApplicationVersion())) {
                serviceProvider.setApplicationVersion(ApplicationConstants.ApplicationVersion.LATEST_APP_VERSION);
            }
            validateSPTemplateExists(spTemplate, tenantDomain);
            validateUnsupportedTemplateConfigs(serviceProvider);
            applicationValidatorManager.validateSPConfigurations(serviceProvider, tenantDomain,
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            Collection<ApplicationMgtListener> listeners =
                    getApplicationMgtListeners();
            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.doPreCreateApplicationTemplate(serviceProvider, tenantDomain);
                }
            }
            doAddApplicationTemplate(spTemplate, tenantDomain);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error("Validation error when creating the application template: " + spTemplate.getName() + " in:" +
                    tenantDomain);
            logValidationErrorMessages(e);
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
                applicationValidatorManager.validateSPConfigurations(updatedSP, tenantDomain,
                        CarbonContext.getThreadLocalCarbonContext().getUsername());
                Collection<ApplicationMgtListener> listeners =
                        getApplicationMgtListeners();
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
                logValidationErrorMessages(e);
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
            // Set default application version.
            if (StringUtils.isBlank(serviceProvider.getApplicationVersion())) {
                serviceProvider.setApplicationVersion(ApplicationConstants.ApplicationVersion.LATEST_APP_VERSION);
            }
            validateUnsupportedTemplateConfigs(serviceProvider);

            applicationValidatorManager.validateSPConfigurations(serviceProvider, tenantDomain,
                    CarbonContext.getThreadLocalCarbonContext().getUsername());

            Collection<ApplicationMgtListener> listeners =
                    getApplicationMgtListeners();
            for (ApplicationMgtListener listener : listeners) {
                if (listener.isEnable()) {
                    listener.doPreUpdateApplicationTemplate(serviceProvider, tenantDomain);
                }
            }
            doUpdateApplicationTemplate(oldTemplateName, spTemplate, tenantDomain);
        } catch (IdentityApplicationManagementValidationException e) {
            log.error("Validation error when updating the application template: " + oldTemplateName + " in:" +
                    tenantDomain);
            logValidationErrorMessages(e);
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

    @Override
    public AuthenticationStep[] getConfiguredAuthenticators(String applicationID, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        LocalAndOutboundAuthenticationConfig localAndOutboundAuthenticationConfig = appDAO
                .getConfiguredAuthenticators(applicationID, tenantDomain);

        if (localAndOutboundAuthenticationConfig == null) {
            return null;
        }
        // If "Authentication Type" is "Default" we must get the steps from the default SP.
        AuthenticationStep[] authenticationSteps = localAndOutboundAuthenticationConfig.getAuthenticationSteps();
        if (authenticationSteps == null || authenticationSteps.length == 0) {
            ServiceProvider defaultSP = ApplicationManagementServiceComponent
                    .getFileBasedSPs().get(IdentityApplicationConstants.DEFAULT_SP_CONFIG);
            authenticationSteps = defaultSP.getLocalAndOutBoundAuthenticationConfig()
                    .getAuthenticationSteps();
        }
        return authenticationSteps;
    }

    @Override
    public AuthenticationStep[] getConfiguredAuthenticators(String applicationID)
            throws IdentityApplicationManagementException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        return getConfiguredAuthenticators(applicationID, tenantDomain);
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
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(spTemplate.getName());
        ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate, tenantDomain);
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
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName);
        SpTemplate spTemplate = getSpTemplateFromCache(templateCacheKey, tenantDomain);

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
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName);
        ServiceProviderTemplateCache.getInstance().clearCacheEntry(templateCacheKey, tenantDomain);
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
            ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName);
            ServiceProviderTemplateCache.getInstance().clearCacheEntry(templateCacheKey, tenantDomain);
        }
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(spTemplate.getName());
        ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate, tenantDomain);
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
        ServiceProviderTemplateCacheKey templateCacheKey = new ServiceProviderTemplateCacheKey(templateName);
        SpTemplate spTemplate = getSpTemplateFromCache(templateCacheKey, tenantDomain);

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
            ServiceProviderTemplateCache.getInstance().addToCache(templateCacheKey, spTemplate, tenantDomain);
            return spTemplate;
        }
        return null;
    }

    private SpTemplate getSpTemplateFromCache(ServiceProviderTemplateCacheKey templateCacheKey, String tenantDomain) {

        SpTemplate spTemplate = ServiceProviderTemplateCache.getInstance()
                .getValueFromCache(templateCacheKey, tenantDomain);
        if (spTemplate != null) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Template with name: %s is taken from cache of tenant: %s ",
                        templateCacheKey.getTemplateName(), tenantDomain));
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

    private void updateSpFromTemplate(ServiceProvider serviceProvider, String tenantDomain,
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

    private void doPreAddApplicationChecks(ServiceProvider serviceProvider, String tenantDomain,
                                           String username) throws IdentityApplicationManagementException {

        String appName = serviceProvider.getApplicationName();
        if (StringUtils.isBlank(appName)) {
            // check for required attributes.
            throw buildClientException(INVALID_REQUEST, "Application name cannot be empty.");
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        if (appDAO.isApplicationExists(appName, tenantDomain)) {
            String msg = "An application with name: '" + appName + "' already exists in tenantDomain: " + tenantDomain;
            throw new IdentityApplicationRegistrationFailureException(APPLICATION_ALREADY_EXISTS.getCode(), msg);
        }

        if (ApplicationManagementServiceComponent.getFileBasedSPs().containsKey(appName)) {
            String msg = "Application with name: '" + appName + "' already loaded from the file system.";
            throw buildClientException(APPLICATION_ALREADY_EXISTS, msg);
        }

        if (!isRegexValidated(appName)) {
            String message = "The Application name: '" + appName + "' is not valid! It is not adhering to the regex: "
                    + ApplicationMgtUtil.getSPValidatorRegex();
            throw buildClientException(INVALID_REQUEST, message);
        }

        boolean isValid = isAssociatedRolesConfigValid(serviceProvider, tenantDomain);
        if (!isValid) {
            throw new IdentityApplicationManagementClientException(
                    "One or more associating roles does not exist or not in the allowed " +
                            "audience for the application.");
        }

        addUserIdAsDefaultSubject(serviceProvider);

        validateApplicationConfigurations(serviceProvider, tenantDomain, username);
    }

    private void addUserIdAsDefaultSubject(ServiceProvider serviceProvider) {
        boolean containsUseUserIdForSubjectProp = false;
        ArrayList<ServiceProviderProperty> serviceProviderProperties
                = new ArrayList<>(Arrays.asList(serviceProvider.getSpProperties()));
        for (ServiceProviderProperty prop: serviceProviderProperties) {
            if (IdentityApplicationConstants.USE_USER_ID_FOR_DEFAULT_SUBJECT.equals(prop.getName())) {
                containsUseUserIdForSubjectProp = true;
                break;
            }
        }
        if (!containsUseUserIdForSubjectProp) {
            ServiceProviderProperty useUserIdForSubject = new ServiceProviderProperty();
            useUserIdForSubject.setName(IdentityApplicationConstants.USE_USER_ID_FOR_DEFAULT_SUBJECT);
            useUserIdForSubject.setValue("true");
            serviceProviderProperties.add(useUserIdForSubject);
            serviceProvider.setSpProperties(serviceProviderProperties.toArray(new ServiceProviderProperty[0]));
        }
    }

    private <T> T doAddApplication(ServiceProvider serviceProvider, String tenantDomain, String username,
                                   ApplicationPersistFunction<ServiceProvider, T> applicationPersistFunction)
            throws IdentityApplicationManagementException {

        try {
            startTenantFlow(tenantDomain, username);

            String applicationName = serviceProvider.getApplicationName();
            // First we need to create a role with the application name. Only the users in this role will be able to
            // edit/update the application.
            ApplicationMgtUtil.createAppRole(applicationName, username);
            if (SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain) || !isOrganization(tenantDomain)) {
                try {
                    PermissionsAndRoleConfig permissionAndRoleConfig = serviceProvider.getPermissionAndRoleConfig();
                    ApplicationMgtUtil.storePermissions(applicationName, username, permissionAndRoleConfig);
                } catch (IdentityApplicationManagementException ex) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating application: " + applicationName + " in tenantDomain: " + tenantDomain +
                                " failed. Rolling back by cleaning up partially created data.");
                    }
                    deleteApplicationRole(applicationName);
                    throw ex;
                }
            }

            try {
                return applicationPersistFunction.persistApplication(serviceProvider, tenantDomain);
            } catch (IdentityApplicationManagementException ex) {
                if (isRollbackRequired(ex)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Creating application: " + applicationName + " in tenantDomain: " + tenantDomain +
                                " failed. Rolling back by cleaning up partially created data.");
                    }
                    deleteApplicationRole(applicationName);
                    deleteApplicationPermission(applicationName);
                }
                throw ex;
            }
        } finally {
            endTenantFlow();
        }
    }

    private boolean isRollbackRequired(IdentityApplicationManagementException ex) {
        // If the error code indicates an application conflict we don't need to rollback since it will affect the
        // already existing app.
        return !StringUtils.equals(ex.getErrorCode(), APPLICATION_ALREADY_EXISTS.getCode());
    }

    private boolean isOwnerUpdatedInRequest(ServiceProvider serviceProvider) {

        return serviceProvider.getOwner() != null && StringUtils.isNotEmpty(serviceProvider.getOwner().getUserName())
                && !CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(serviceProvider.getOwner().getUserName());
    }

    private void assignApplicationRole(String applicationName, String username)
            throws IdentityApplicationManagementException {

        boolean validateRoles = ApplicationMgtUtil.validateRoles();
        if (!validateRoles) {
            if (log.isDebugEnabled()) {
                log.debug("Validating user with application roles is disabled. Therefore, the application " +
                        "role will not be assigned to user: " + username);
            }
            return;
        }
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
            // Creating secure parser by disabling XXE.
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setNamespaceAware(true);
            spf.setXIncludeAware(false);
            try {
                spf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
                spf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
                spf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (SAXException | ParserConfigurationException e) {
                log.error("Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or "
                        + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE
                        + " or secure-processing.");
            }
            // Creating source object using the secure parser.
            Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(),
                    new InputSource(new StringReader(spFileContent.getContent())));
            // Performing unmarshall operation by passing the generated source object to the unmarshaller.
            JAXBContext jaxbContext = JAXBContext.newInstance(ServiceProvider.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (ServiceProvider) unmarshaller.unmarshal(xmlSource);
        } catch (JAXBException | SAXException | ParserConfigurationException e) {
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
            marshaller.setListener(new Marshaller.Listener() {
                @Override
                public void beforeMarshal(Object source) {
                    if (source instanceof InboundAuthenticationConfig) {
                        InboundAuthenticationConfig config = (InboundAuthenticationConfig) source;
                        for (InboundAuthenticationRequestConfig requestConfig
                                : config.getInboundAuthenticationRequestConfigs()) {
                            requestConfig.setInboundConfigurationProtocol(null);
                        }
                    }
                }
            });
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

    private boolean isOrganization(String tenantDomain) {

        int tenantID = IdentityTenantUtil.getTenantId(tenantDomain);
        Tenant tenant = IdentityTenantUtil.getTenant(tenantID);
        return tenant != null && StringUtils.isNotBlank(tenant.getAssociatedOrganizationUUID());
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

    @Override
    public ApplicationBasicInfo getApplicationBasicInfoByResourceId(String resourceId,
                                                                    String tenantDomain)
            throws IdentityApplicationManagementException {

        Collection<ApplicationResourceManagementListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationResourceMgtListeners();

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPreGetApplicationBasicInfoByResourceId(resourceId, tenantDomain)) {
                throw buildServerException("Error executing doPreGetApplicationBasicInfoByResourceId operation of " +
                        "listener: " + getName(listener) + " for application resourceId: " + resourceId);
            }
        }

        ApplicationBasicInfo basicAppInfo = getApplicationBasicInfo(resourceId, tenantDomain);

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPostGetApplicationBasicInfoByResourceId(basicAppInfo, resourceId, tenantDomain)) {
                throw buildServerException("Error executing doPostGetApplicationBasicInfoByResourceId operation of " +
                        "listener: " + getName(listener) + " for application resourceId: " + resourceId);
            }
        }
        return basicAppInfo;
    }

    private ApplicationBasicInfo getApplicationBasicInfo(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getApplicationBasicInfoByResourceId(resourceId, tenantDomain);
    }
    
    /**
     * Create the application. This method creates the service provider without creating any protocol configurations.
     *
     * @param application  Service provider.
     * @param tenantDomain Tenant domain.
     * @param username     Username.
     */
    @Override
    public String createApplication(ServiceProvider application, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        // Invoking the listeners.
        Collection<ApplicationResourceManagementListener> listeners = ApplicationMgtListenerServiceComponent
                .getApplicationResourceMgtListeners();

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() && !listener.doPreCreateApplication(application, tenantDomain, username)) {
                throw buildServerException("Pre create application operation of listener: "
                        + getName(listener) + " failed for application: " + application.getApplicationName() +
                        " of tenantDomain: " + tenantDomain);
            }
        }

        // Set default application version.
        application.setApplicationVersion(ApplicationConstants.ApplicationVersion.LATEST_APP_VERSION);

        doPreAddApplicationChecks(application, tenantDomain, username);
        ApplicationDAO applicationDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        String resourceId = doAddApplication(application, tenantDomain, username, applicationDAO::addApplication);

        for (ApplicationResourceManagementListener listener : listeners) {
            try {
                if (listener.isEnabled() && !listener.doPostCreateApplication(resourceId, application, tenantDomain,
                        username)) {
                    log.error("Post create application operation of listener:" + getName(listener) + " failed for " +
                            "application: " + application.getApplicationName() + " of tenantDomain: " + tenantDomain);
                    break;
                }
            } catch (Throwable e) {
                /*
                 * For more information read https://github.com/wso2/product-is/issues/12579. This is to overcome the
                 * above issue.
                 */
                log.error("Post create application operation of listener:" + getName(listener) + " failed for " +
                        "application: " + application.getApplicationName() + " of tenantDomain: " + tenantDomain +
                        " due to: " + e);
                deleteApplicationByResourceId(resourceId, tenantDomain, username);
                if (log.isDebugEnabled()) {
                    log.debug("Removed the application created with id: " + resourceId + " of tenantDomain: "
                            + tenantDomain);
                }
                throw buildServerException("Server encountered an unexpected error when creating the application.");
            }
        }
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain), LoggerUtils.Target.User.name(), resourceId,
                    LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.CREATE_APPLICATION_ACTION)
                    .data(buildSPData(application));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
        return resourceId;
    }
    
    /**
     * Create the application. This method creates the service provider with protocol configurations.
     *
     * @param applicationModel ApplicationModelDTO containing the app information.
     * @param tenantDomain     Tenant domain.
     * @param username         Username.
     * @return Application ID.
     * @throws IdentityApplicationManagementException Identity Application Management Exception.
     */
    public String createApplication(ApplicationDTO applicationModel, String tenantDomain, String username)
            throws IdentityApplicationManagementException {

        List<InboundAuthenticationRequestConfig> addedInbounds = new ArrayList<>();
        try {
            ServiceProvider application = applicationModel.getServiceProvider();
             addedInbounds = addInboundAuthenticationProtocolsToApplication(
                    application, applicationModel.getInboundProtocolConfigurationDto());
            
            return createApplication(application, tenantDomain, username);
        } catch (IdentityApplicationManagementException identityApplicationManagementException) {
            /*
             * The current implementation of the application creation process is not atomic. Therefore, if an Exception
             * occurs, there is a chance that the database gets updated partially. Hence, we need to rollback the
             * created inbounds if available.
             * For more information read https://github.com/wso2/product-is/issues/12579.
             */
            rollbackInbounds(addedInbounds);
            throw identityApplicationManagementException;
        }
    }

    private List<InboundAuthenticationRequestConfig> addInboundAuthenticationProtocolsToApplication(
            ServiceProvider application, InboundProtocolsDTO inboundProtocolsModel)
            throws IdentityApplicationManagementException {

        if (inboundProtocolsModel == null) {
            return Collections.emptyList();
        }
        List<InboundAuthenticationRequestConfig> addedInbounds = new ArrayList<>();
        try {
            // This handles the inbound protocol creation with the help of protocol handlers.
            List<ApplicationInboundAuthConfigHandler> applicationInboundAuthConfigHandlerList =
                    ApplicationManagementServiceComponentHolder.getInstance().getApplicationInboundAuthConfigHandler();
            for (ApplicationInboundAuthConfigHandler applicationInboundAuthConfigHandler :
                    applicationInboundAuthConfigHandlerList) {
                if (applicationInboundAuthConfigHandler.canHandle(inboundProtocolsModel)) {
                    addedInbounds.add(applicationInboundAuthConfigHandler.handleConfigCreation(application,
                            inboundProtocolsModel));
                }
            }
        } catch (IdentityApplicationManagementClientException e) {
            /*
             * If there is a client exception, we don't need to do rollback since it will not affect the already
             * existing app.
             */
            throw e;
        } catch (IdentityApplicationManagementException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while adding inbound protocols for application id: "
                        + application.getApplicationResourceId() + ". Cleaning up possible partially created inbound " +
                        "configurations.");
            }
            rollbackInbounds(addedInbounds);
            throw e;
        }
        InboundAuthenticationConfig alreadyAddedInboundConfigs = application.getInboundAuthenticationConfig();
        InboundAuthenticationConfig inboundAuthConfig = new InboundAuthenticationConfig();
        if (alreadyAddedInboundConfigs != null) {
            List<InboundAuthenticationRequestConfig> alreadyAddedInbounds =
                    Arrays.asList(alreadyAddedInboundConfigs.getInboundAuthenticationRequestConfigs());
            addedInbounds.addAll(alreadyAddedInbounds);
        }
        inboundAuthConfig.setInboundAuthenticationRequestConfigs(
                addedInbounds.toArray(new InboundAuthenticationRequestConfig[0])
        );
        application.setInboundAuthenticationConfig(inboundAuthConfig);
        return addedInbounds;
    }

    private <T> String getName(T listener) {

        return listener.getClass().getName();
    }

    @Override
    public ServiceProvider getApplicationByResourceId(String resourceId, String tenantDomain)
            throws IdentityApplicationManagementException {

        Collection<ApplicationResourceManagementListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationResourceMgtListeners();

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() && !listener.doPreGetApplicationByResourceId(resourceId, tenantDomain)) {
                throw buildServerException("Pre Get application operation of " +
                        "listener: " + getName(listener) + " failed for application with resourceId: " + resourceId);
            }
        }

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        ServiceProvider application = appDAO.getApplicationByResourceId(resourceId, tenantDomain);
        if (application == null) {
            application = new FileBasedApplicationDAO().getApplicationByResourceId(resourceId, tenantDomain);
            if (application == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find an application for resourceId: " + resourceId + " in tenantDomain: "
                            + tenantDomain);
                }
                return null;
            }
        }
        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPostGetApplicationByResourceId(application, resourceId, tenantDomain)) {
                log.error("Post Get application operation of " +
                        "listener: " + getName(listener) + " failed for application with resourceId: " + resourceId);
                break;
            }
        }

        return application;
    }
    
    @Override
    public void updateApplicationByResourceId(String resourceId, ServiceProvider updatedApp, String tenantDomain,
                                              String username) throws IdentityApplicationManagementException {
        
        validateApplicationConfigurations(updatedApp, tenantDomain, username);
        
        updatedApp.setApplicationResourceId(resourceId);
        setDisplayNamesOfLocalAuthenticators(updatedApp, tenantDomain);
        Collection<ApplicationResourceManagementListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationResourceMgtListeners();
        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPreUpdateApplicationByResourceId(updatedApp, resourceId, tenantDomain, username)) {
                
                throw buildServerException("Pre Update application operation of listener: " + getName(listener) +
                        " failed for application with resourceId: " + resourceId);
            }
        }
        
        try {
            startTenantFlow(tenantDomain, username);
            
            ApplicationBasicInfo storedAppInfo = getApplicationBasicInfo(resourceId, tenantDomain);
            if (storedAppInfo == null) {
                String msg = "Cannot find an application for " + "resourceId: " + resourceId + " in tenantDomain: "
                        + tenantDomain;
                throw buildClientException(APPLICATION_NOT_FOUND, msg);
            }
            
            String updatedAppName = updatedApp.getApplicationName();
            String storedAppName = storedAppInfo.getApplicationName();
            
            doPreUpdateChecks(storedAppName, updatedApp, tenantDomain, username);
            
            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            ServiceProvider storedApp = getApplicationByResourceId(resourceId, tenantDomain);
            appDAO.updateApplicationByResourceId(resourceId, tenantDomain, updatedApp);
            postApplicationUserAttributeUpdate(updatedApp, storedApp, tenantDomain);
            
            if (isOwnerUpdateRequest(storedAppInfo.getAppOwner(), updatedApp.getOwner())) {
                // User existence check is already done in appDAO.updateApplicationByResourceId() method.
                assignApplicationRole(updatedApp.getApplicationName(), updatedApp.getOwner().getUserName());
            }
            
            updateApplicationPermissions(updatedApp, updatedAppName, storedAppName);
        } finally {
            endTenantFlow();
        }
        
        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled()
                    && !listener.doPostUpdateApplicationByResourceId(updatedApp, resourceId, tenantDomain, username)) {
                log.error("Post Update application operation of listener: " + getName(listener) + " failed for " +
                        "application with resourceId: " + resourceId);
                return;
            }
        }
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain), LoggerUtils.Target.User.name(),
                    resourceId, LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.UPDATE_APPLICATION_ACTION)
                    .data(buildSPData(updatedApp));
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }
    
    /**
     * Update the application by resource id. This method update the inbound protocol configurations of the application
     * by calling the relevant protocol handlers if available.
     *
     * @param resourceId        Unique resource identifier of the application.
     * @param serviceProvider   Service provider. This can contain updated application information.
     * @param tenantDomain      Tenant domain of the application.
     * @param username          Tenant aware username of the user performing the operation.
     * @throws IdentityApplicationManagementException Identity Application Management Exception.
     */
    public void updateApplicationByResourceId(String resourceId, ServiceProvider serviceProvider,
                                              InboundProtocolConfigurationDTO inboundProtocolConfigurationDTO,
                                              String tenantDomain, String username)
            throws IdentityApplicationManagementException {
        
        if (inboundProtocolConfigurationDTO == null) {
            if (log.isDebugEnabled()) {
                log.debug("Inbound protocol configuration is not provided for the application: " + resourceId +
                        ". Hence updating the application just using the service provider details in the" +
                        " serviceProvider object.");
            }
            updateApplicationByResourceId(resourceId, serviceProvider, tenantDomain, username);
            return;
        }
        InboundAuthenticationRequestConfig addedInbound = null;
        List<ApplicationInboundAuthConfigHandler> applicationInboundAuthConfigHandlers =
                ApplicationManagementServiceComponentHolder.getInstance().getApplicationInboundAuthConfigHandler();
        for (ApplicationInboundAuthConfigHandler handler : applicationInboundAuthConfigHandlers) {
            if (handler.canHandle(inboundProtocolConfigurationDTO.fetchProtocolName())) {
                addedInbound = handler.handleConfigUpdate(serviceProvider,
                        inboundProtocolConfigurationDTO);
                break;
            }
        }
        if (addedInbound != null) {
            updateOrInsertInbound(serviceProvider, addedInbound);
        }
        try {
            /*
             * At this point the serviceProvided object is updated with the inbound auth config details. So we can call
             * the updateApplicationByResourceId(String, ServiceProvider, String, String) method to update the
             * service provider details in the database.
             */
            updateApplicationByResourceId(resourceId, serviceProvider, tenantDomain, username);
        } catch (IdentityApplicationManagementException e) {
            if (addedInbound != null) {
                doRollback(resourceId, addedInbound, tenantDomain);
            }
            throw e;
        }
    }

    @Override
    public Set<String> getSystemApplications() {

        return getApplications(SYSTEM_APPLICATIONS_CONFIG_ELEMENT);
    }

    @Override
    public Set<String> getDefaultApplications() {

        return getApplications(DEFAULT_APPLICATIONS_CONFIG_ELEMENT);
    }

    @Override
    public String getMainAppId(String sharedAppId) throws IdentityApplicationManagementServerException {

        return ApplicationMgtSystemConfig.getInstance().getApplicationDAO().getMainAppId(sharedAppId);
    }

    @Override
    public int getTenantIdByApp(String appId) throws IdentityApplicationManagementServerException {

        return ApplicationMgtSystemConfig.getInstance().getApplicationDAO().getTenantIdByApp(appId);
    }

    @Override
    public String getAllowedAudienceForRoleAssociation(String applicationUUID, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Invoking the pre listeners.
        Collection<ApplicationMgtListener> preListeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : preListeners) {
            if (listener.isEnable() &&
                    !listener.doPreGetAllowedAudienceForRoleAssociation(applicationUUID, tenantDomain)) {
                throw buildServerException("Error executing doPreGetAllowedAudienceForRoleAssociation operation of " +
                        "listener: " + getName(listener) + " for application with id: " + applicationUUID);
            }
        }

        String allowedAudience = ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                .getSPPropertyValueByPropertyKey(applicationUUID,
                        IdentityApplicationConstants.ALLOWED_ROLE_AUDIENCE_PROPERTY_NAME, tenantDomain);
        AssociatedRolesConfig associatedRolesConfigExcludingRoles = new AssociatedRolesConfig();
        associatedRolesConfigExcludingRoles.setAllowedAudience(allowedAudience);

        // Invoking the post listeners.
        Collection<ApplicationMgtListener> postListeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : postListeners) {
            if (listener.isEnable() &&
                    !listener.doPostGetAllowedAudienceForRoleAssociation(associatedRolesConfigExcludingRoles,
                            applicationUUID, tenantDomain)) {
                throw buildServerException(
                        "Error executing doPostGetAllowedAudienceForRoleAssociation operation of listener: " +
                                getName(listener) + " for application with id: " + applicationUUID);
            }
        }
        return associatedRolesConfigExcludingRoles.getAllowedAudience();
    }

    @Override
    public List<RoleV2> getAssociatedRolesOfApplication(String applicationUUID, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Invoking the pre listeners.
        Collection<ApplicationMgtListener> preListeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : preListeners) {
            if (listener.isEnable() &&
                    !listener.doPreGetAssociatedRolesOfApplication(applicationUUID, tenantDomain)) {
                throw buildServerException("Error executing doPreGetAssociatedRolesOfApplication operation of " +
                        "listener: " + getName(listener) + " for application with id: " + applicationUUID);
            }
        }

        List<RoleV2> associatedRolesOfApplication = ApplicationMgtSystemConfig.getInstance().getApplicationDAO()
                .getAssociatedRolesOfApplication(applicationUUID, tenantDomain);

        // Invoking the post listeners.
        Collection<ApplicationMgtListener> postListeners = getApplicationMgtListeners();
        for (ApplicationMgtListener listener : postListeners) {
            if (listener.isEnable() &&
                    !listener.doPostGetAssociatedRolesOfApplication(associatedRolesOfApplication, applicationUUID,
                            tenantDomain)) {
                throw buildServerException("Error executing doPostGetAssociatedRolesOfApplication operation of " +
                        "listener: " + getName(listener) + " for application with id: " + applicationUUID);
            }
        }
        return associatedRolesOfApplication;
    }

    @Override
    public void addAssociatedRoleToApplication(ServiceProvider serviceProvider, String roleId, String tenantDomain)
            throws IdentityApplicationManagementException {

        AssociatedRolesConfig associatedRolesConfig = serviceProvider.getAssociatedRolesConfig();
        if (associatedRolesConfig == null) {
            return;
        }
        String allowedAudienceType =
                StringUtils.isBlank(associatedRolesConfig.getAllowedAudience()) ? RoleConstants.ORGANIZATION :
                        associatedRolesConfig.getAllowedAudience().toLowerCase();
        String allowedAudienceId;
        switch (allowedAudienceType) {
            case RoleConstants.APPLICATION:
                allowedAudienceId = serviceProvider.getApplicationResourceId();
                break;
            default:
                try {
                    allowedAudienceId = getOrganizationManager().resolveOrganizationId(tenantDomain);
                } catch (OrganizationManagementException e) {
                    throw new IdentityApplicationManagementException(
                            String.format("Error while resolving the organization id for the tenant domain: %s",
                                    tenantDomain), e);
                }
                break;
        }
        boolean isValid = isRoleInCorrectAudience(new RoleV2(roleId), tenantDomain, allowedAudienceType,
                allowedAudienceId);
        if (!isValid) {
            throw new IdentityApplicationManagementClientException(
                    "Invalid role audience provided.");
        }
        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        appDAO.addAssociatedRoleToApplication(serviceProvider.getApplicationResourceId(), roleId);
    }

    /**
     * Get the list of trusted applications based on the requested platform type.
     *
     * @param platformType Platform type of the trusted apps.
     * @return List of trusted apps of all tenants.
     * @throws IdentityApplicationManagementException If an error occurs while retrieving the trusted apps.
     */
    @Override
    public List<TrustedApp> getTrustedApps(PlatformType platformType) throws IdentityApplicationManagementException {

        ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
        return appDAO.getTrustedApps(platformType);
    }

    private void doPreUpdateChecks(String storedAppName, ServiceProvider updatedApp, String tenantDomain,
                                   String username) throws IdentityApplicationManagementException {

        String updatedAppName = updatedApp.getApplicationName();

        validateAuthorization(updatedAppName, storedAppName, username, tenantDomain);
        validateAppName(storedAppName, updatedApp, tenantDomain);
        validateApplicationCertificate(updatedApp, tenantDomain);
        boolean isValid = isAssociatedRolesConfigValid(updatedApp, tenantDomain);
        if (!isValid) {
            throw new IdentityApplicationManagementClientException(
                    "One or more associating roles does not exist or not in the allowed " +
                            "audience for the application.");
        }
        // Will be supported with 'Advance Consent Management Feature'.
        // validateConsentPurposes(serviceProvider);
    }

    private void updateApplicationPermissions(ServiceProvider updatedApp, String updatedAppName, String storedAppName)
            throws IdentityApplicationManagementException {

        ApplicationMgtUtil.renameAppPermissionPathNode(storedAppName, updatedAppName);

        if (updatedApp.getPermissionAndRoleConfig() != null) {
            ApplicationMgtUtil.updatePermissions(updatedAppName,
                    updatedApp.getPermissionAndRoleConfig().getPermissions());
        }
    }

    private void validateApplicationCertificate(ServiceProvider updatedApp,
                                                String tenantDomain) throws IdentityApplicationManagementException {

        if (!isValidPEMCertificate(updatedApp.getCertificateContent())) {
            String error = "Provided application certificate for application with name: %s in tenantDomain: %s " +
                    "is malformed.";
            throw buildClientException(INVALID_REQUEST,
                    String.format(error, updatedApp.getApplicationName(), tenantDomain));
        }
    }

    private void validateApplicationConfigurations(ServiceProvider application,
                                                   String tenantDomain,
                                                   String username) throws IdentityApplicationManagementException {

        try {
            applicationValidatorManager.validateSPConfigurations(application, tenantDomain, username);
        } catch (IdentityApplicationManagementValidationException e) {
            String message = "Invalid application configuration for application: '" +
                    application.getApplicationName() + "' of tenantDomain: " + tenantDomain + ".";
            String errorCode = INVALID_REQUEST.getCode();
            throw new IdentityApplicationManagementValidationException(errorCode, message, e.getValidationMsg());
        }
    }

    private void validateAuthorization(String updatedAppName,
                                       String storedAppName,
                                       String username,
                                       String tenantDomain) throws IdentityApplicationManagementException {

        if (!ApplicationConstants.LOCAL_SP.equals(storedAppName) &&
                !ApplicationMgtUtil.isUserAuthorized(storedAppName, username)) {
            String message = "Illegal Access! User: " + username + " does not have access to update the " +
                    "application: '" + updatedAppName + "' in tenantDomain: " + tenantDomain;
            throw buildClientException(OPERATION_FORBIDDEN, message);
        }
    }

    private boolean isOwnerUpdateRequest(User storedAppOwner, User updatedAppOwner) {

        if (updatedAppOwner != null) {
            boolean isValidAppOwnerInUpdateRequest = StringUtils.isNotEmpty(updatedAppOwner.getUserName())
                    && !CarbonConstants.REGISTRY_SYSTEM_USERNAME.equals(updatedAppOwner.getUserName());
            boolean isOwnerChanged = !storedAppOwner.equals(updatedAppOwner);

            return isValidAppOwnerInUpdateRequest && isOwnerChanged;
        } else {
            // There is no app owner defined in the update request. Nothing to do there.
            return false;
        }
    }

    private void validateAppName(String currentAppName, ServiceProvider updatedApp, String tenantDomain)
            throws IdentityApplicationManagementException {

        String updatedAppName = updatedApp.getApplicationName();
        if (StringUtils.isBlank(updatedAppName)) {
            // check for required attributes.
            throw buildClientException(INVALID_REQUEST, "Application name cannot be empty.");
        }

        if (!isRegexValidated(updatedAppName)) {
            String message = "The Application name '" + updatedAppName + "' is not valid. " +
                    "Application name does not adhere to the regex " + ApplicationMgtUtil.getSPValidatorRegex();
            throw buildClientException(INVALID_REQUEST, message);
        }

        if (isAppRenamed(currentAppName, updatedAppName)
                && ApplicationConstants.LOCAL_SP.equalsIgnoreCase(updatedAppName)) {
            String msg = "Cannot update an application's name to tenant resident service provider's name '%s'";
            throw buildClientException(OPERATION_FORBIDDEN, String.format(msg, ApplicationConstants.LOCAL_SP));
        }

        if (isAppRenamed(currentAppName, updatedAppName)
                && isAnotherAppExistsWithUpdatedName(updatedApp, tenantDomain)) {
            String msg = "Updated application name '%s' already exists.";
            throw buildClientException(APPLICATION_ALREADY_EXISTS, String.format(msg, updatedAppName));
        }
    }

    private boolean isAnotherAppExistsWithUpdatedName(ServiceProvider updatedApp, String tenantDomain)
            throws IdentityApplicationManagementException {

        ServiceProvider appWithUpdatedName = getServiceProvider(updatedApp.getApplicationName(), tenantDomain);
        return appWithUpdatedName != null && appWithUpdatedName.getApplicationID() != updatedApp.getApplicationID();
    }

    private boolean isAppRenamed(String currentAppName, String updatedAppName) {

        return !StringUtils.equals(currentAppName, updatedAppName);
    }

    private void logValidationErrorMessages(IdentityApplicationManagementValidationException e) {

        if (e.getValidationMsg() != null) {
            log.error(StringUtils.join(e.getValidationMsg(), "\n"));
        }
    }

    @Override
    public void deleteApplicationByResourceId(String resourceId,
                                              String tenantDomain,
                                              String username) throws IdentityApplicationManagementException {

        // Invoking listeners.
        Collection<ApplicationResourceManagementListener> listeners =
                ApplicationMgtListenerServiceComponent.getApplicationResourceMgtListeners();
        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPreDeleteApplicationByResourceId(resourceId, tenantDomain, username)) {

                throw buildServerException("Pre Delete application operation of listener: " + getName(listener) +
                        " failed for application with resourceId: " + resourceId);
            }
        }

        ServiceProvider application;
        try {
            startTenantFlow(tenantDomain, username);

            ApplicationDAO appDAO = ApplicationMgtSystemConfig.getInstance().getApplicationDAO();
            application = appDAO.getApplicationByResourceId(resourceId, tenantDomain);

            if (application != null) {

                String applicationName = application.getApplicationName();
                doPreDeleteChecks(applicationName, tenantDomain, username);

                ApplicationMgtUtil.deleteAppRole(applicationName);
                ApplicationMgtUtil.deletePermissions(applicationName);
                // Delete the app information from SP_APP table.
                appDAO.deleteApplicationByResourceId(resourceId, tenantDomain);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Application cannot be found for resourceId: " + resourceId +
                            " in tenantDomain: " + tenantDomain);
                }
                return;
            }
        } catch (Exception e) {
            log.error(String.format("Application: %s in tenant: %s might have partially deleted",
                    resourceId, tenantDomain));
            throw e;
        } finally {
            endTenantFlow();
        }

        for (ApplicationResourceManagementListener listener : listeners) {
            if (listener.isEnabled() &&
                    !listener.doPostDeleteApplicationByResourceId(application, resourceId, tenantDomain, username)) {
                log.error("Post Delete application operation of listener: " + getName(listener) + " failed for " +
                        "application with resourceId: " + resourceId);
                return;
            }
        }
        if (ApplicationMgtUtil.isEnableV2AuditLogs()) {
            AuditLog.AuditLogBuilder auditLogBuilder = new AuditLog.AuditLogBuilder(
                    getInitiatorId(username, tenantDomain), LoggerUtils.Target.User.name(),
                    resourceId, LoggerUtils.Target.Application.name(),
                    LogConstants.ApplicationManagement.DELETE_APPLICATION_ACTION);
            triggerAuditLogEvent(auditLogBuilder, true);
        }
    }

    private void doPreDeleteChecks(String applicationName, String tenantDomain,
                                   String username) throws IdentityApplicationManagementException {

        if (!ApplicationMgtUtil.isUserAuthorized(applicationName, username)) {
            String message = "Illegal Access! User " + username + " does not have access to delete the application: '"
                    + applicationName + "' of tenantDomain: " + tenantDomain;
            log.warn(message);
            throw buildClientException(OPERATION_FORBIDDEN, message);
        }

        if (StringUtils.equals(applicationName, ApplicationConstants.LOCAL_SP)) {
            String msg = "Cannot delete tenant resident service provider: " + ApplicationConstants.LOCAL_SP;
            throw buildClientException(OPERATION_FORBIDDEN, msg);
        }
    }

    private IdentityApplicationManagementClientException buildClientException(Error errorMessage, String message) {

        return new IdentityApplicationManagementClientException(errorMessage.getCode(), message);
    }

    private IdentityApplicationManagementServerException buildServerException(String message,
                                                                              Throwable ex) {

        return new IdentityApplicationManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), message, ex);
    }

    private IdentityApplicationManagementServerException buildServerException(String message) {

        return new IdentityApplicationManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), message);
    }

    @FunctionalInterface
    private interface ApplicationPersistFunction<S extends ServiceProvider, T> {

        T persistApplication(S application, String tenantDomain) throws IdentityApplicationManagementException;
    }

    /**
     * Returns the Collection of ApplicationMgtListener, discovered via the component.
     * @return
     */
    private Collection<ApplicationMgtListener> getApplicationMgtListeners() {

        return ApplicationMgtListenerServiceComponent.getApplicationMgtListeners();
    }

    /**
     * Set displayName of configured localAuthenticators in the service provider, if displayName is null.
     *
     * @param serviceProvider Service provider.
     * @param tenantDomain    Tenant domain.
     * @throws IdentityApplicationManagementException If an error occur while retrieving local authenticator configs.
     */
    private void setDisplayNamesOfLocalAuthenticators(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Set displayName of local authenticators if displayNames are null.
        LocalAuthenticatorConfig[] localAuthenticatorConfigs = getAllLocalAuthenticators(tenantDomain);
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() == null || localAuthenticatorConfigs == null) {
            return;
        }
        AuthenticationStep[] authSteps =
                serviceProvider.getLocalAndOutBoundAuthenticationConfig().getAuthenticationSteps();
        if (CollectionUtils.isEmpty(Arrays.asList(authSteps))) {
            return;
        }
        for (AuthenticationStep authStep : authSteps) {
            if (CollectionUtils.isEmpty(Arrays.asList(authStep.getLocalAuthenticatorConfigs()))) {
                return;
            }
            for (LocalAuthenticatorConfig localAuthenticator : authStep.getLocalAuthenticatorConfigs()) {
                if (localAuthenticator.getDisplayName() == null) {
                    Arrays.stream(localAuthenticatorConfigs).forEach(config -> {
                        if (StringUtils.equals(localAuthenticator.getName(), config.getName())) {
                            localAuthenticator.setDisplayName(config.getDisplayName());
                        }
                    });
                }
            }
        }
    }

    private ArrayList<String> getLocalClaimURIs(List<LocalClaim> localClaims) {

        // Using Java 8 streams to do the mapping will result in breaking at the axis level thus using the following
        // approach.
        ArrayList<String> localClaimsArray = new ArrayList<String>();
        for (LocalClaim localClaim : localClaims) {
            localClaimsArray.add(localClaim.getClaimURI());
        }
        return localClaimsArray;
    }

    private void postApplicationUserAttributeUpdate(ServiceProvider updatedApp, ServiceProvider storedApp,
                                                    String tenantDomain) {

        if (updatedApp.getClaimConfig().getClaimMappings().length > 0) {
            List<String> storedAppClaimUrls = Arrays.stream(storedApp.getClaimConfig()
                            .getClaimMappings()).map(claimMapping -> claimMapping.getLocalClaim().getClaimUri())
                    .collect(Collectors.toList());
            List<org.wso2.carbon.identity.application.common.model.ClaimMapping> updatedClaimMappings =
                    Arrays.stream(updatedApp.getClaimConfig().getClaimMappings())
                            .filter(claimMapping ->
                                    !storedAppClaimUrls.contains(claimMapping.getLocalClaim().getClaimUri()))
                            .collect(Collectors.toList());

            if (!updatedClaimMappings.isEmpty()) {
                Map<String, Object> eventProperties = new HashMap<>();
                eventProperties.put(IdentityEventConstants.EventProperty.UPDATED_CLAIM_MAPPINGS,
                        updatedClaimMappings);
                eventProperties.put(IdentityEventConstants.EventProperty.APPLICATION_ID,
                        updatedApp.getApplicationResourceId());
                eventProperties.put(IdentityEventConstants.EventProperty.TENANT_DOMAIN, tenantDomain);
                Event event = new Event(IdentityEventConstants.Event.POST_APP_USER_ATTRIBUTE_UPDATE, eventProperties);
                fireEvent(event);
            }

        }
    }

    private void fireEvent(Event event) {

        try {
            if (log.isDebugEnabled()) {
                log.debug("Event: " + event.getEventName() + " is published for the application user attribute " +
                        "update operation in the tenant with the tenantId: " + event.getEventProperties().
                        get(IdentityEventConstants.EventProperty.TENANT_ID));
            }
            IdentityEventService eventService =
                    ApplicationManagementServiceComponentHolder.getInstance().getIdentityEventService();
            eventService.handleEvent(event);
        } catch (IdentityEventException e) {
            log.error("Error while publishing the event: " + event.getEventName() + ".", e);
        }
    }

    private boolean isAssociatedRolesConfigValid(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        AssociatedRolesConfig associatedRolesConfig = serviceProvider.getAssociatedRolesConfig();
        if (associatedRolesConfig == null) {
            return true;
        }
        List<RoleV2> roles = new ArrayList<>();
        if (associatedRolesConfig.getRoles() != null) {
            roles = Arrays.asList(associatedRolesConfig.getRoles());
        }
        if (CollectionUtils.isEmpty(roles)) {
            return true;
        }
        String allowedAudienceType =
                StringUtils.isBlank(associatedRolesConfig.getAllowedAudience()) ? RoleConstants.ORGANIZATION :
                        associatedRolesConfig.getAllowedAudience().toLowerCase();
        String allowedAudienceId;
        switch (allowedAudienceType) {
            case RoleConstants.APPLICATION:
                allowedAudienceId = serviceProvider.getApplicationResourceId();
                break;
            default:
                try {
                    allowedAudienceId = getOrganizationManager().resolveOrganizationId(tenantDomain);
                } catch (OrganizationManagementException e) {
                    throw new IdentityApplicationManagementException(
                            String.format("Error while resolving the organization id for the tenant domain: %s",
                                    tenantDomain), e);
                }
                break;
        }
        // Stream the roles and check whether the role exits in the correct audience.
        boolean allRolesInCorrectAudience = roles.stream()
                .allMatch(role -> isRoleInCorrectAudience(role, tenantDomain, allowedAudienceType, allowedAudienceId));
        if (!allRolesInCorrectAudience) {
            log.debug("One or more role does not exist or not in correct audience.");
        }
        return allRolesInCorrectAudience;
    }

    private boolean isRoleInCorrectAudience(RoleV2 role, String tenantDomain, String allowedAudienceType,
                                            String allowedAudienceId) {

        try {
            RoleBasicInfo retrievedRole = getRoleManagementServiceV2().getRoleBasicInfoById(role.getId(), tenantDomain);
            if (retrievedRole != null) {
                return allowedAudienceType.equals(retrievedRole.getAudience()) &&
                        StringUtils.equals(allowedAudienceId, retrievedRole.getAudienceId());
            }
        } catch (IdentityRoleManagementException e) {
            if ((ROLE_MANAGEMENT_ERROR_CODE_PREFIX + ROLE_NOT_FOUND).equals(e.getErrorCode())) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Role: %s does not exist.", role.getId()));
                }
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug(String.format("Error while retrieving the role: %s", role.getId()));
            }
            return false;
        }
        return false;
    }

    private static Set<String> getApplications(String parentElement) {

        IdentityConfigParser configParser = IdentityConfigParser.getInstance();
        OMElement systemApplicationsConfig = configParser.getConfigElement(parentElement);
        if (systemApplicationsConfig == null) {
            if (log.isDebugEnabled()) {
                log.debug("'" + parentElement + "' config not found.");
            }
            return Collections.emptySet();
        }

        Iterator applicationIdentifierIterator = systemApplicationsConfig
                .getChildrenWithLocalName(APPLICATION_NAME_CONFIG_ELEMENT);
        if (applicationIdentifierIterator == null) {
            if (log.isDebugEnabled()) {
                log.debug("'" + APPLICATION_NAME_CONFIG_ELEMENT + "' config not found.");
            }
            return Collections.emptySet();
        }

        Set<String> applications = new HashSet<>();
        while (applicationIdentifierIterator.hasNext()) {
            OMElement applicationIdentifierConfig = (OMElement) applicationIdentifierIterator.next();
            String applicationName = applicationIdentifierConfig.getText();
            if (StringUtils.isNotBlank(applicationName)) {
                applications.add(applicationName.trim());
            }
        }

        return applications;
    }

    private static RoleManagementService getRoleManagementServiceV2() {

        return ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2();
    }

    private static OrganizationManager getOrganizationManager() {

        return ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager();
    }
}
