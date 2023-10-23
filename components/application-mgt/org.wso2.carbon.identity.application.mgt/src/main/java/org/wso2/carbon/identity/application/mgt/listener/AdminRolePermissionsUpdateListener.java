package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.api.resource.mgt.APIResourceMgtException;
import org.wso2.carbon.identity.api.resource.mgt.constant.APIResourceManagementConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.APIResource;
import org.wso2.carbon.identity.application.common.model.Scope;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.util.OrganizationManagementUtil;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.model.Permission;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.ORGANIZATION;

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
            updateAdminRolePermission(tenantDomain);
        } catch (OrganizationManagementException e) {
            LOG.error("Error while registering system API resources in tenant: " + tenantDomain);
        }
        return true;
    }

    private void updateAdminRolePermission(String tenantDomain) throws IdentityApplicationManagementException {

        String adminRoleId = getAdminRoleId(tenantDomain);

        try {
            // Fetch the system API resources count.
            int systemAPICount = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getAPIResources(null, null, 1,
                            APIResourceManagementConstants.SYSTEM_API_FILTER, "ASC", tenantDomain).getTotalCount();
            // Fetch all system APIs.
            List<APIResource> apiResources = ApplicationManagementServiceComponentHolder.getInstance()
                    .getAPIResourceManager().getAPIResources(null, null, systemAPICount,
                            APIResourceManagementConstants.SYSTEM_API_FILTER, "ASC", tenantDomain).getAPIResources();
            if (apiResources.isEmpty()) {
                LOG.error("Error while authorizing system API console. System APIs not found in tenant: "
                        + tenantDomain);
            }
            List<Permission> systemPermissions =  new ArrayList<>();
            for (APIResource apiResource : apiResources) {
                List<Scope> scopes = ApplicationManagementServiceComponentHolder.getInstance()
                        .getAPIResourceManager().getAPIScopesById(apiResource.getId(), tenantDomain);
                for(Scope scope: scopes) {
                    systemPermissions.add(new Permission(scope.getName()));
                }
            }
            RoleManagementService roleManagementService = ApplicationManagementServiceComponentHolder.getInstance()
                    .getRoleManagementServiceV2();
            roleManagementService.updatePermissionListOfRole(adminRoleId, systemPermissions, new ArrayList<>() ,tenantDomain);
        } catch (APIResourceMgtException | IdentityRoleManagementException e) {
            throw new IdentityApplicationManagementException("Error while update admin role permissions", e);
        }
    }

    private String getAdminRoleId(String tenantDomain) throws IdentityApplicationManagementException {

        String orgId;
        try {
            orgId = ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager().resolveOrganizationId(tenantDomain);
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

    @Override
    public int getDefaultOrderId() {

        return 212;
    }

    @Override
    public boolean isEnable() {

        return true;
    }
}
