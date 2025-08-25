/*
 * Copyright (c) 2023-2024, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementServerException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.AuthorizationDetailsType;
import org.wso2.carbon.identity.application.common.model.AuthorizedAPI;
import org.wso2.carbon.identity.application.common.model.AuthorizedScopes;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.mgt.dao.AuthorizedAPIDAO;
import org.wso2.carbon.identity.application.mgt.dao.impl.AuthorizedAPIDAOImpl;
import org.wso2.carbon.identity.application.mgt.dao.impl.CacheBackedAuthorizedAPIDAOImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationMgtListenerServiceComponent;
import org.wso2.carbon.identity.application.mgt.listener.AuthorizedAPIManagementListener;
import org.wso2.carbon.identity.application.mgt.publisher.ApplicationAuthorizedAPIManagementEventPublisherProxy;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.INVALID_REQUEST;
import static org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants.Error.UNEXPECTED_SERVER_ERROR;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.AUTHORIZE_ALL_SCOPES;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.AUTHORIZE_INTERNAL_SCOPES;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.RBAC;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.APPLICATION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.INTERNAL_SCOPE_PREFIX;

/**
 * Authorized API management service implementation.
 */
public class AuthorizedAPIManagementServiceImpl implements AuthorizedAPIManagementService {

    private final AuthorizedAPIDAO authorizedAPIDAO = new CacheBackedAuthorizedAPIDAOImpl(new AuthorizedAPIDAOImpl());

    @Override
    public void addAuthorizedAPI(String applicationId, AuthorizedAPI authorizedAPI, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationAuthorizedAPIManagementEventPublisherProxy publisherProxy =
                ApplicationAuthorizedAPIManagementEventPublisherProxy.getInstance();
        publisherProxy.publishPreAddAuthorizedAPIForApplication(applicationId, authorizedAPI, tenantDomain);
        Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                .getAuthorizedAPIManagementListeners();
        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.preAddAuthorizedAPI(applicationId, authorizedAPI, tenantDomain);
        }
        // Check if the application is a main application. If not, throw a client error.
        ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        String mainAppId = applicationManagementService.getMainAppId(applicationId);
        if (StringUtils.isNotBlank(mainAppId)) {
            throw buildClientException(INVALID_REQUEST, "Cannot add authorized APIs to a shared application.");
        }
        authorizedAPIDAO.addAuthorizedAPI(applicationId, authorizedAPI, IdentityTenantUtil.getTenantId(tenantDomain));

        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.postAddAuthorizedAPI(applicationId, authorizedAPI, tenantDomain);
        }
        publisherProxy.publishPostAddAuthorizedAPIForApplication(applicationId, authorizedAPI, tenantDomain);
    }

    @Override
    public void deleteAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationAuthorizedAPIManagementEventPublisherProxy publisherProxy =
                ApplicationAuthorizedAPIManagementEventPublisherProxy.getInstance();
        publisherProxy.publishPreDeleteAuthorizedAPIForApplication(appId, apiId, tenantDomain);
        Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                .getAuthorizedAPIManagementListeners();
        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.preDeleteAuthorizedAPI(appId, apiId, tenantDomain);
        }
        authorizedAPIDAO.deleteAuthorizedAPI(appId, apiId, IdentityTenantUtil.getTenantId(tenantDomain));
        try {
            List<String> apiScopes = ApplicationManagementServiceComponentHolder.getInstance().getAPIResourceManager()
                    .getAPIResourceById(apiId, tenantDomain).getScopes().stream().map(Scope::getName)
                    .collect(Collectors.toList());
            updateRolesWithRemovedScopes(appId, apiScopes, tenantDomain);
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving API resource.", e);
        }
        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.postDeleteAuthorizedAPI(appId, apiId, tenantDomain);
        }
        publisherProxy.publishPostDeleteAuthorizedAPIForApplication(appId, apiId, tenantDomain);
    }

    @Override
    public List<AuthorizedAPI> getAuthorizedAPIs(String applicationId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                    .getAuthorizedAPIManagementListeners();
            for (AuthorizedAPIManagementListener listener : listeners) {
                listener.preGetAuthorizedAPIs(applicationId, tenantDomain);
            }
            // Check if the application is a main application else get the main application id and main tenant id.
            ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
            String mainAppId = applicationManagementService.getMainAppId(applicationId);
            if (StringUtils.isNotBlank(mainAppId)) {
                applicationId = mainAppId;
                int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
                tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            }

            List<AuthorizedAPI> authorizedAPIs = authorizedAPIDAO.getAuthorizedAPIs(applicationId,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            for (AuthorizedAPI authorizedAPI : authorizedAPIs) {
                // Get API resource data from OSGi service.
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(authorizedAPI.getAPIId(), tenantDomain);
                authorizedAPI.setAPIIdentifier(apiResource.getIdentifier());
                authorizedAPI.setAPIName(apiResource.getName());
                authorizedAPI.setType(apiResource.getType());
                // Get Scope data from OSGi service.
                List<Scope> scopeList = new ArrayList<>();
                if (authorizedAPI.getScopes() != null) {
                    for (Scope scope : authorizedAPI.getScopes()) {
                        Scope scopeWithMetadata = ApplicationManagementServiceComponentHolder.getInstance()
                                .getAPIResourceManager().getScopeByName(scope.getName(), tenantDomain);
                        scopeList.add(scopeWithMetadata);
                    }
                }
                authorizedAPI.setScopes(scopeList);
            }
            for (AuthorizedAPIManagementListener listener : listeners) {
                listener.postGetAuthorizedAPIs(authorizedAPIs, applicationId, tenantDomain);
            }
            return authorizedAPIs;
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving authorized APIs.", e);
        }
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException {

        this.patchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, Collections.emptyList(),
                Collections.emptyList(), tenantDomain);
    }

    @Override
    public List<AuthorizedScopes> getAuthorizedScopes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                    .getAuthorizedAPIManagementListeners();
            for (AuthorizedAPIManagementListener listener : listeners) {
                listener.preGetAuthorizedScopes(appId, tenantDomain);
            }
            // Check if the application is a main application else get the main application id and main tenant id.
            ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
            String mainAppId = applicationManagementService.getMainAppId(appId);
            if (mainAppId != null) {
                appId = mainAppId;
                int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
                tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            }

            List<AuthorizedScopes> authorizedScopes;
            // If authorizeAllScopes is enabled, all scopes will be considered as authorized.
            if (Boolean.parseBoolean(IdentityUtil.getProperty(AUTHORIZE_ALL_SCOPES))) {
                List<Scope> scopes = ApplicationManagementServiceComponentHolder.getInstance().getAPIResourceManager()
                        .getScopesByTenantDomain(tenantDomain, null);
                Map<String, AuthorizedScopes> authorizedScopesMap = new HashMap<>();
                AuthorizedScopes.AuthorizedScopesBuilder authorizedScopesBuilder =
                        new AuthorizedScopes.AuthorizedScopesBuilder()
                                .policyId(RBAC)
                                .scopes(scopes.stream()
                                        .map(Scope::getName)
                                        .collect(Collectors.toCollection(ArrayList::new)));
                authorizedScopesMap.put(RBAC, authorizedScopesBuilder.build());

                // Exclude internal scopes that start with "internal_", If this configuration is not enabled,
                // IS will not authorise internal scopes.
                boolean authoriseInternalScopes = Boolean.parseBoolean(IdentityUtil.getProperty(
                        AUTHORIZE_INTERNAL_SCOPES));
                if (authoriseInternalScopes) {
                    authorizedScopes = new ArrayList<>(authorizedScopesMap.values());
                } else {
                    authorizedScopes = new ArrayList<>(getScopesExcludingInternalScopes(authorizedScopesMap));
                    List<AuthorizedScopes> appAuthorisedScopes = authorizedAPIDAO.getAuthorizedScopes(appId,
                            IdentityTenantUtil.getTenantId(tenantDomain));
                    // Get the scopes authorised in the application and add them too.
                    authorizedScopes.addAll(appAuthorisedScopes);
                }
            } else {
                authorizedScopes = authorizedAPIDAO.getAuthorizedScopes(appId,
                        IdentityTenantUtil.getTenantId(tenantDomain));
            }

            for (AuthorizedAPIManagementListener listener : listeners) {
                listener.postGetAuthorizedScopes(authorizedScopes, appId, tenantDomain);
            }
            return authorizedScopes;
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving scopes for tenant domain : " + tenantDomain, e);
        }
    }

    private List<AuthorizedScopes> getScopesExcludingInternalScopes(Map<String, AuthorizedScopes> authorizedScopesMap) {

        // Iterate and filter scopes that do not start with "internal_" and "console" scopes.
        return authorizedScopesMap.values().stream()
                .map(authorizedScopes -> {
                    // Filter scopes that do not start with "internal_" and "console".
                    List<String> filteredScopes = authorizedScopes.getScopes().stream()
                            .filter(scope -> !scope.startsWith(INTERNAL_SCOPE_PREFIX))
                            .filter(scope -> !scope.startsWith(CONSOLE_SCOPE_PREFIX))
                            .collect(Collectors.toList());
                    return new AuthorizedScopes(authorizedScopes.getPolicyId(), filteredScopes);
                })
                .collect(Collectors.toList());
    }

    @Override
    public AuthorizedAPI getAuthorizedAPI(String appId, String apiId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                    .getAuthorizedAPIManagementListeners();
            for (AuthorizedAPIManagementListener listener : listeners) {
                listener.preGetAuthorizedAPI(appId, apiId, tenantDomain);
            }
            // Check if the application is a main application else get the main application id and main tenant id.
            ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
            String mainAppId = applicationManagementService.getMainAppId(appId);
            if (mainAppId != null) {
                apiId = mainAppId;
                int tenantId = applicationManagementService.getTenantIdByApp(mainAppId);
                tenantDomain = IdentityTenantUtil.getTenantDomain(tenantId);
            }

            AuthorizedAPI authorizedAPI = authorizedAPIDAO.getAuthorizedAPI(appId, apiId,
                    IdentityTenantUtil.getTenantId(tenantDomain));
            if (authorizedAPI != null) {
                APIResource apiResource = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIResourceById(authorizedAPI.getAPIId(), tenantDomain);
                authorizedAPI.setAPIIdentifier(apiResource.getIdentifier());
                authorizedAPI.setAPIName(apiResource.getName());
                if (authorizedAPI.getScopes() != null) {
                    // Get Scope data from OSGi service.
                    List<Scope> scopeList = new ArrayList<>();
                    for (Scope scope : authorizedAPI.getScopes()) {
                        Scope scopeWithMetadata = ApplicationManagementServiceComponentHolder.getInstance()
                                .getAPIResourceManager().getScopeByName(scope.getName(), tenantDomain);
                        scopeList.add(scopeWithMetadata);
                    }
                    authorizedAPI.setScopes(scopeList);
                }
            }
            for (AuthorizedAPIManagementListener listener : listeners) {
                authorizedAPI = listener.postGetAuthorizedAPI(authorizedAPI, appId, apiId, tenantDomain);
            }
            return authorizedAPI;
        } catch (APIResourceMgtException e) {
            throw buildServerException("Error while retrieving authorized API.", e);
        }
    }

    private IdentityApplicationManagementClientException buildClientException(
            IdentityApplicationConstants.Error errorMessage, String message) {

        return new IdentityApplicationManagementClientException(errorMessage.getCode(), message);
    }

    private IdentityApplicationManagementServerException buildServerException(String message, Throwable ex) {

        return new IdentityApplicationManagementServerException(UNEXPECTED_SERVER_ERROR.getCode(), message, ex);
    }

    private void updateRolesWithRemovedScopes(String appId, List<String> removedScopes, String tenantDomain)
            throws IdentityApplicationManagementException {

        if (CollectionUtils.isEmpty(removedScopes) || !isApplicationAudience(appId, tenantDomain)) {
            return;
        }

        List<Permission> removedPermissions = removedScopes.stream().map(Permission::new).collect(Collectors.toList());
        List<RoleV2> roles = ApplicationManagementService.getInstance().getAssociatedRolesOfApplication(appId,
                tenantDomain);
        try {
            for (RoleV2 role : roles) {
                getRoleManagementServiceV2().updatePermissionListOfRole(role.getId(), new ArrayList<>(),
                        removedPermissions, tenantDomain);
            }
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error while updating permission list of roles " +
                    "associated with the application ID: " + appId, e);
        }
    }

    private boolean isApplicationAudience(String appId, String tenantDomain) throws
            IdentityApplicationManagementException {

        String audience = ApplicationManagementService.getInstance().getAllowedAudienceForRoleAssociation(appId,
                tenantDomain);
        return APPLICATION.equalsIgnoreCase(audience);
    }

    private static RoleManagementService getRoleManagementServiceV2() {

        return ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2();
    }

    @Override
    public void patchAuthorizedAPI(String appId, String apiId, List<String> addedScopes,
                                   List<String> removedScopes, List<String> addedAuthorizationDetailsTypes,
                                   List<String> removedAuthorizationDetailsTypes, String tenantDomain)
            throws IdentityApplicationManagementException {

        ApplicationAuthorizedAPIManagementEventPublisherProxy publisherProxy =
                ApplicationAuthorizedAPIManagementEventPublisherProxy.getInstance();
        publisherProxy.publishPreUpdateAuthorizedAPIForApplication(appId, apiId, addedScopes, removedScopes,
                addedAuthorizationDetailsTypes, removedAuthorizationDetailsTypes, tenantDomain);
        Collection<AuthorizedAPIManagementListener> listeners = ApplicationMgtListenerServiceComponent
                .getAuthorizedAPIManagementListeners();
        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.prePatchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, tenantDomain);
        }
        authorizedAPIDAO.patchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, addedAuthorizationDetailsTypes,
                removedAuthorizationDetailsTypes, IdentityTenantUtil.getTenantId(tenantDomain));
        updateRolesWithRemovedScopes(appId, removedScopes, tenantDomain);
        for (AuthorizedAPIManagementListener listener : listeners) {
            listener.postPatchAuthorizedAPI(appId, apiId, addedScopes, removedScopes, tenantDomain);
        }
        publisherProxy.publishPostUpdateAuthorizedAPIForApplication(appId, apiId, addedScopes, removedScopes,
                addedAuthorizationDetailsTypes, removedAuthorizationDetailsTypes, tenantDomain);
    }

    @Override
    public List<AuthorizationDetailsType> getAuthorizedAuthorizationDetailsTypes(String appId, String tenantDomain)
            throws IdentityApplicationManagementException {

        // Check if the application is a main application else get the main application id and main tenant id.
        ApplicationManagementService applicationManagementService = ApplicationManagementServiceImpl.getInstance();
        String mainAppId = applicationManagementService.getMainAppId(appId);
        if (mainAppId != null) {
            appId = mainAppId;
            tenantDomain = IdentityTenantUtil.getTenantDomain(applicationManagementService.getTenantIdByApp(mainAppId));
        }

        return this.authorizedAPIDAO
                .getAuthorizedAuthorizationDetailsTypes(appId, IdentityTenantUtil.getTenantId(tenantDomain));
    }
}
