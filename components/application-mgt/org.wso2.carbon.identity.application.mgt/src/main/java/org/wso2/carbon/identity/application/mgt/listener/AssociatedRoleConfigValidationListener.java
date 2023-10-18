/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.listener;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementClientException;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.AssociatedRolesConfig;
import org.wso2.carbon.identity.application.common.model.RoleV2;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.IdentityRoleManagementException;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleBasicInfo;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants;
import org.wso2.carbon.identity.role.v2.mgt.core.RoleManagementService;

import java.util.List;

import static org.wso2.carbon.identity.role.v2.mgt.core.RoleConstants.Error.ROLE_NOT_FOUND;

/**
 * Internal implementation of {@link AbstractApplicationMgtListener} to listen to associated role updates
 * and validate the operation.
 */
public class AssociatedRoleConfigValidationListener extends AbstractApplicationMgtListener {

    private static Log LOG = LogFactory.getLog(AssociatedRoleConfigValidationListener.class);

    @Override
    public int getDefaultOrderId() {

        return 5;
    }

    public boolean doPreCreateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        boolean isValid = isAssociatedRolesConfigValid(serviceProvider, tenantDomain);
        if (!isValid) {
            throw new IdentityApplicationManagementClientException("One or more associating roles does not exist or not in the allowed " +
                    "audience for the application.");
        }
        return true;
    }

    public boolean doPreUpdateApplication(ServiceProvider serviceProvider, String tenantDomain, String userName)
            throws IdentityApplicationManagementException {

        boolean isValid = isAssociatedRolesConfigValid(serviceProvider, tenantDomain);
        if (!isValid) {
            throw new IdentityApplicationManagementClientException("One or more associating roles does not exist or not in the allowed " +
                    "audience for the application.");
        }
        return true;
    }

    private boolean isAssociatedRolesConfigValid(ServiceProvider serviceProvider, String tenantDomain)
            throws IdentityApplicationManagementException {

        AssociatedRolesConfig associatedRolesConfig = serviceProvider.getAssociatedRolesConfig();
        if (associatedRolesConfig == null) {
            return true;
        }
        List<RoleV2> roles = associatedRolesConfig.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            return true;
        }
        String allowedAudienceType =
                StringUtils.isNotBlank(associatedRolesConfig.getAllowedAudience()) ? RoleConstants.ORGANIZATION :
                        associatedRolesConfig.getAllowedAudience();
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
            LOG.debug("One or more role does not exist or not in correct audience.");
        }
        return allRolesInCorrectAudience;
    }

    private boolean isRoleInCorrectAudience(RoleV2 role, String tenantDomain, String allowedAudienceType,
                                            String allowedAudienceId) {

        try {
            RoleBasicInfo retrievedRole = getRoleManagementServiceV2().getRoleBasicInfoById(role.getId(), tenantDomain);
            if (retrievedRole != null) {
                return allowedAudienceType.equals(retrievedRole.getAudience()) &&
                        allowedAudienceId.equals(retrievedRole.getAudienceId());
            }
        } catch (IdentityRoleManagementException e) {
            // TODO: use constant for error code prefix.
            if (("RMA-" + ROLE_NOT_FOUND).equals(e.getErrorCode())) {
                LOG.error(String.format("Role: %s does not exist.", role.getId()));
                return false;
            }
            LOG.error(String.format("Error while retrieving the role: %s", role.getId()), e);
            return false;
        }
        return false;
    }

    private static RoleManagementService getRoleManagementServiceV2() {

        return ApplicationManagementServiceComponentHolder.getInstance().getRoleManagementServiceV2();
    }

    private static OrganizationManager getOrganizationManager() {

        return ApplicationManagementServiceComponentHolder.getInstance().getOrganizationManager();
    }
}
