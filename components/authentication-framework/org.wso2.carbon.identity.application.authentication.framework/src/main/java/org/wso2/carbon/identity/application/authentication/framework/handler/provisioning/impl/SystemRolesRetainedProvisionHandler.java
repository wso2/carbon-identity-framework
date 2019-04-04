/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.WORKFLOW_DOMAIN;


/**
 * Provisioning handler implementation which will keep all system (Application/*, Workflow/*)
 * roles without being deleted during the provisioning process.
 */
public class SystemRolesRetainedProvisionHandler extends DefaultProvisioningHandler {

    @Override
    protected List<String> retrieveRolesToBeDeleted(UserRealm realm, List<String> currentRolesList,
                                                    List<String> rolesToAdd) throws UserStoreException {

        List<String> deletingRoles = super.retrieveRolesToBeDeleted(realm, currentRolesList, rolesToAdd);

        // Remove all internal roles from deleting list
        deletingRoles.removeAll(extractInternalRoles(currentRolesList));

        return deletingRoles;
    }

    /**
     * Extract all internal roles from a list of provided roles.
     *
     * @param allRoles list of roles to filter from
     * @return internal role list
     */
    private List<String> extractInternalRoles(List<String> allRoles) {

        List<String> internalRoles = new ArrayList<>();

        for (String role : allRoles) {
            if (StringUtils.contains(role, APPLICATION_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)
                    || StringUtils.contains(role, WORKFLOW_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
                internalRoles.add(role);
            }
        }

        return internalRoles;
    }
}
