package org.wso2.carbon.identity.api.resource.collection.mgt.listener;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.api.resource.collection.mgt.APIResourceCollectionManagerImpl;
import org.wso2.carbon.identity.api.resource.collection.mgt.exception.APIResourceCollectionMgtException;
import org.wso2.carbon.identity.api.resource.collection.mgt.internal.APIResourceCollectionMgtServiceDataHolder;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollection;
import org.wso2.carbon.identity.api.resource.collection.mgt.model.APIResourceCollectionSearchResult;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.listener.AbstractRoleManagementListener;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Role;
import org.wso2.carbon.identity.role.v2.mgt.core.model.RoleBasicInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants.APIResourceCollectionConfigBuilderConstants.EDIT_ACTION;
import static org.wso2.carbon.identity.api.resource.collection.mgt.constant.APIResourceCollectionManagementConstants.APIResourceCollectionConfigBuilderConstants.VIEW_ACTION;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_APP_AUDIENCE_NAME;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_ORG_SCOPE_PREFIX;
import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.CONSOLE_SCOPE_PREFIX;

/**
 * Console role listener to populate organization console application roles permissions.
 */
public class ConsoleRoleListener extends AbstractRoleManagementListener {

    @Override
    public int getDefaultOrderId() {

        return 4;
    }

    @Override
    public boolean isEnable() {

        return true;
    }

    @Override
    public void postGetRole(Role role, String roleId, String tenantDomain) throws IdentityRoleManagementException {

        // Add new read and write scopes from API resource collection to the console role's permission list.
        if (isConsoleRole(roleId, tenantDomain)) {
            // Fetch all system scopes to resolve permission details from permission name.
            List<Scope> systemScopes;
            try {
                systemScopes = APIResourceCollectionMgtServiceDataHolder.getInstance()
                        .getAPIResourceManagementService().getSystemAPIScopes(tenantDomain);
            } catch (APIResourceMgtException e) {
                throw new IdentityRoleManagementException("Error while retrieving internal scopes for tenant " +
                        "domain : " + tenantDomain, e);
            }
            List<Permission> systemPermissions = systemScopes.stream().map(scope -> new Permission(scope.getName(),
                    scope.getDisplayName(), scope.getApiID())).collect(Collectors.toList());
            // Get all the permissions of the role.
            List<Permission> rolePermissions = role.getPermissions();
            List<APIResourceCollection> apiResourceCollections = getAPIResourceCollections(tenantDomain);
            List<Permission> consoleFeaturePermissions = getConsoleFeaturePermissions(rolePermissions);
            if (!consoleFeaturePermissions.isEmpty()) {
                // This is where we handle the new console roles (console roles created after 7.0.0) permissions.
                // We check whether the role has the view feature scope and edit feature scope. If the role has the
                // view feature scope, then we add all the read scopes. If the role has the edit feature scope, then we
                // add all the write scopes.
                consoleFeaturePermissions.forEach(permission -> {
                    apiResourceCollections.forEach(apiResourceCollection -> {
                        if (apiResourceCollection.getViewFeatureScope() != null &&
                                apiResourceCollection.getViewFeatureScope().equals(permission.getName())) {
                            apiResourceCollection.getReadScopes().forEach(newReadScope -> {
                                Optional<Permission> newPermission = systemPermissions.stream()
                                        .filter(permission1 -> permission1.getName().equals(newReadScope))
                                        .findFirst();
                                newPermission.ifPresent(rolePermissions::add);
                            });
                        }
                        // If the role has the edit feature scope, then we add all the write scopes.
                        if (apiResourceCollection.getEditFeatureScope() != null &&
                                apiResourceCollection.getEditFeatureScope().equals(permission.getName())) {
                            apiResourceCollection.getWriteScopes().forEach(newReadScope -> {
                                Optional<Permission> newPermission = systemPermissions.stream()
                                        .filter(permission1 -> permission1.getName().equals(newReadScope))
                                        .findFirst();
                                newPermission.ifPresent(rolePermissions::add);
                            });
                        }
                    });
                });
            } else {
                // This is where we handle the initial console roles (console roles created in 7.0.0) permissions.
                List<Permission> consolePermissions = getConsolePermissions(rolePermissions);
                consolePermissions.forEach(permission -> {
                    apiResourceCollections.forEach(apiResourceCollection -> {
                        // Match the permission with the collection.
                        if (apiResourceCollection.getReadScopes().contains(permission.getName())) {
                            List<String> newReadScopes = apiResourceCollection.getNewReadScopes().get("v1");
                            if (ArrayUtils.isNotEmpty(newReadScopes.toArray())) {
                                // Add new read scopes since we have the feature scope.
                                newReadScopes.forEach(newReadScope -> {
                                    Optional<Permission> newPermission = systemPermissions.stream()
                                            .filter(permission1 -> permission1.getName().equals(newReadScope))
                                            .findFirst();
                                    newPermission.ifPresent(rolePermissions::add);
                                });

                            }
                            List<String> writeScopes = apiResourceCollection.getWriteScopes();
                            List<String> newWriteScopes = apiResourceCollection.getNewWriteScopes().get("v1");
                            if (ArrayUtils.isNotEmpty(newWriteScopes.toArray())) {
                                // Create a copy of the original writeScopes list
                                List<String> writeScopesCopy = new ArrayList<>(writeScopes);
                                // Remove the new write scopes from the write scopes since we need to cross-check with
                                // the role's permission list.
                                writeScopesCopy.removeAll(newWriteScopes);
                                // if all the writeScopes are in the role's permission list, then add new write scopes.
                                if (rolePermissions.stream().anyMatch(rolePermission ->
                                        writeScopesCopy.contains(rolePermission.getName()))) {
                                    newWriteScopes.forEach(newWriteScope -> {
                                        Optional<Permission> newPermission = systemPermissions.stream()
                                                .filter(permission1 -> permission1.getName().equals(newWriteScope))
                                                .findFirst();
                                        newPermission.ifPresent(rolePermissions::add);
                                    });
                                }
                            }
                        }
                    });
                });
            }
            role.setPermissions(rolePermissions);
        }
    }

    private boolean isConsoleRole(String roleId, String tenantDomain) throws IdentityRoleManagementException {

        RoleManagementService roleManagementService = APIResourceCollectionMgtServiceDataHolder.getInstance()
                .getRoleManagementServiceV2();
        RoleBasicInfo role = roleManagementService.getRoleBasicInfoById(roleId, tenantDomain);
        return !RoleConstants.ADMINISTRATOR.equals(role.getName()) &&
                role.getAudienceName().equals(CONSOLE_APP_AUDIENCE_NAME);
    }

    private List<APIResourceCollection> getAPIResourceCollections(String tenantDomain)
            throws IdentityRoleManagementException {

        try {
            List<String> requiredAttributes = new ArrayList<>();
            requiredAttributes.add("apiResources");
            APIResourceCollectionSearchResult apiResourceCollectionSearchResult = APIResourceCollectionManagerImpl
                    .getInstance().getAPIResourceCollections("", requiredAttributes, tenantDomain);
            return apiResourceCollectionSearchResult.getAPIResourceCollections();

        } catch (APIResourceCollectionMgtException e) {
            throw new IdentityRoleManagementException("Error while retrieving api collection for tenant : " +
                    tenantDomain, e);
        }
    }

    private List<Permission> getConsoleFeaturePermissions(List<Permission> rolePermissions) {

        return rolePermissions.stream().filter(permission -> permission != null &&
                        permission.getName() != null && (permission.getName().startsWith(CONSOLE_SCOPE_PREFIX)
                        || permission.getName().startsWith(CONSOLE_ORG_SCOPE_PREFIX)) &&
                        (permission.getName().endsWith(VIEW_ACTION) || permission.getName().endsWith(EDIT_ACTION)))
                .collect(Collectors.toList());
    }

    private List<Permission> getConsolePermissions(List<Permission> rolePermissions) {

        return rolePermissions.stream().filter(permission -> permission != null &&
                        permission.getName() != null && (permission.getName().startsWith(CONSOLE_SCOPE_PREFIX)
                        || permission.getName().startsWith(CONSOLE_ORG_SCOPE_PREFIX)) &&
                        !(permission.getName().endsWith(VIEW_ACTION) || permission.getName().endsWith(EDIT_ACTION)))
                .collect(Collectors.toList());
    }
}
