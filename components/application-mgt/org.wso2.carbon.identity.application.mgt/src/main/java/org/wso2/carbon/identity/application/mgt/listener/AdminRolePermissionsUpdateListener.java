package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementServiceImpl;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

/**
 * Admin role permissions update listener to update admin role permissions.
 */
public class AdminRolePermissionsUpdateListener extends AbstractApplicationMgtListener  {

    private static final Log LOG = LogFactory.getLog(AdminRolePermissionsUpdateListener.class);

    @Override
    public boolean doPostCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        if (!isEnable()) {
            LOG.debug("AdminRolePermissionUpdateListener is not enabled.");
            return false;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("AdminRolePermissionUpdateListener fired for tenant " +
                    "creation for Tenant: " + tenantDomain);
        }

        if (!ApplicationConstants.CONSOLE_APPLICATION_NAME.equals(serviceProvider.getApplicationName())) {
            return false;
        }

        try {
            if (OrganizationManagementUtil.isOrganization(tenantDomain)) {
                return false;
            }
            if (CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME) {
                return false;
            }
            String adminRoleId = getAdminRoleId(tenantDomain);
            addAdminRoleToConsoleAppAsAssociatedRole(adminRoleId, serviceProvider, tenantDomain);
            updateAdminRolePermissions(adminRoleId, tenantDomain);
        } catch (OrganizationManagementException e) {
            LOG.error("Error while registering system API resources in tenant: " + tenantDomain);
        }
        return true;
    }

    /**
     * Update admin role system permissions.
     *
     * @param adminRoleId Admin role id.
     * @param tenantDomain Tenant domain.
     * @throws IdentityApplicationManagementException if an error occurs while updating admin role permissions.
     */
    private void updateAdminRolePermissions(String adminRoleId, String tenantDomain)
            throws IdentityApplicationManagementException {

        try {
            List<String> internalScopes = getInternalScopes(tenantDomain);
            List<Permission> systemPermissions =  new ArrayList<>();
            for (String scope : internalScopes) {
                systemPermissions.add(new Permission(scope));
            }
            RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                    .getRoleManagementServiceV2();
            roleManagementService.updatePermissionListOfRole(adminRoleId, systemPermissions, new ArrayList<>(),
                    tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error while update admin role permissions", e);
        }
    }

    /**
     * Add admin role to console app as associated role.
     *
     * @param adminRoleId Admin role id.
     * @param serviceProvider Service provider.
     * @param tenantDomain Tenant domain.
     * @throws IdentityApplicationManagementException if an error occurs while adding admin role to console app.
     */
    private void addAdminRoleToConsoleAppAsAssociatedRole(String adminRoleId, ServiceProvider serviceProvider,
                                                     String tenantDomain)
            throws IdentityApplicationManagementException {

        AssociatedRolesConfig associatedRolesConfig = new AssociatedRolesConfig();
        associatedRolesConfig.setAllowedAudience(ORGANIZATION);
        serviceProvider.setAssociatedRolesConfig(associatedRolesConfig);
        ApplicationManagementServiceImpl.getInstance().addAssociatedRoleToApplication(serviceProvider, adminRoleId,
                tenantDomain);
    }

    /**
     * Get Admin role id.
     *
     * @param tenantDomain Tenant domain.
     * @return Admin role id.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving admin role id.
     */
    private String getAdminRoleId(String tenantDomain) throws IdentityApplicationManagementException {

        String orgId;
        try {
            orgId = ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager()
                    .resolveOrganizationId(tenantDomain);
        } catch (OrganizationManagementException e) {
            throw new IdentityApplicationManagementException("Error while retrieving organization id from tenant " +
                    "domain : " + tenantDomain, e);
        }
        if (orgId == null) {
            throw new IdentityApplicationManagementException("Error while retrieving organization id from tenant " +
                    "domain : " + tenantDomain);
        }
        RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                .getRoleManagementServiceV2();
        try {
            return roleManagementService.getRoleIdByName("admin", ORGANIZATION, orgId, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error while retrieving role id for admin role in " +
                    "tenant domain : " + tenantDomain, e);
        }
    }

    /**
     * Get the internal scopes.
     *
     * @param tenantDomain Tenant domain.
     * @return Internal scopes.
     * @throws IdentityApplicationManagementException if an error occurs while retrieving internal scopes for
     *                                                  tenant domain.
     */
    private List<String> getInternalScopes(String tenantDomain) throws IdentityApplicationManagementException {

        try {
            List<Scope> scopes = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getScopesByTenantDomain(tenantDomain, "name sw internal_");
            return scopes.stream().map(Scope::getName).collect(Collectors.toCollection(ArrayList::new));
        } catch (APIResourceMgtException e) {
            throw new IdentityApplicationManagementException("Error while retrieving internal scopes for tenant " +
                    "domain : " + tenantDomain, e);
        }
    }

    @Override
    public int getDefaultOrderId() {

        return 212;
    }

    @Override
    public boolean isEnable() {

        return true;
    }
}
