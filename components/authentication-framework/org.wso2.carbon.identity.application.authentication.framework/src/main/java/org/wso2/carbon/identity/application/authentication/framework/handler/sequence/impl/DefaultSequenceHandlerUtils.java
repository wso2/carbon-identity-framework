/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Common utility used by Default Sequence Handlers.
 */
public class DefaultSequenceHandlerUtils {

    private static Log log = LogFactory.getLog(DefaultSequenceHandlerUtils.class);
    private static final String SEND_ONLY_SP_MAPPED_ROLES = "SPRoleManagement.ReturnOnlyMappedLocalRoles";
    private DefaultSequenceHandlerUtils() {
    }

    public static String getServiceProviderMappedUserRoles(SequenceConfig sequenceConfig,
                                                           List<String> locallyMappedUserRoles) {
        if (log.isDebugEnabled()) {
            AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
            String serviceProvider = sequenceConfig.getApplicationConfig().getApplicationName();
            log.debug("Getting Service Provider mapped roles of application: " + serviceProvider +
                    " of user: " + authenticatedUser);
        }

        // SP role mapped role values joined by Multi Attribute Separator.
        boolean returnOnlyMappedLocalRoles = Boolean.parseBoolean(IdentityUtil.getProperty(SEND_ONLY_SP_MAPPED_ROLES));

        String spMappedRoles = null;
        if (CollectionUtils.isNotEmpty(locallyMappedUserRoles)) {
            // Get SP Role mappings
            Map<String, String> localToSpRoleMapping = sequenceConfig.getApplicationConfig().getRoleMappings();
            List<String> spMappedRoleList = new ArrayList<>();
            List<String> domainRemovedRoleList = new ArrayList<>();
            // Check whether there are any SpRoleMappings
            if (localToSpRoleMapping != null && !localToSpRoleMapping.isEmpty()) {
                for (String locallyMappedRole : locallyMappedUserRoles) {
                    if (localToSpRoleMapping.containsKey(locallyMappedRole)) {
                        // add the SP mapped role
                        String spMappedRole = localToSpRoleMapping.get(locallyMappedRole);
                        spMappedRoleList.add(spMappedRole);
                        if (log.isDebugEnabled()) {
                            log.debug("Mapping local role: " + locallyMappedRole + " to service provider role: "
                                    + spMappedRole);
                        }
                    } else {
                        //  If ReturnOnlyMappedLocalRoles is false, add local role to the list.
                        if (!returnOnlyMappedLocalRoles) {
                            if (isRemoveUserDomainInRole(sequenceConfig)) {
                                //if 'Use user store domain in roles' is false add the list to remove domain name.
                                domainRemovedRoleList.add(locallyMappedRole);
                            } else {
                                spMappedRoleList.add(locallyMappedRole);
                            }
                        }
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No local roles to map to Service Provider role mappings. Sending back all local roles " +
                            "as service provider mapped roles.");
                }
                // We don't have any sp role mappings
                if (isRemoveUserDomainInRole(sequenceConfig)) {
                    domainRemovedRoleList = locallyMappedUserRoles;
                } else {
                    spMappedRoleList = locallyMappedUserRoles;
                }
            }

            //if 'Use user store domain in roles' is false remove the domain from roles.
            if (isRemoveUserDomainInRole(sequenceConfig)) {
                String[] domainRemovedRoles = UserCoreUtil.removeDomainFromNames(domainRemovedRoleList.toArray
                        (new String[domainRemovedRoleList.size()]));
                if (!ArrayUtils.isEmpty(domainRemovedRoles)) {
                    spMappedRoleList.addAll(Arrays.asList(domainRemovedRoles));
                }
            }
            spMappedRoles = StringUtils.join(spMappedRoleList.toArray(), FrameworkUtils.getMultiAttributeSeparator());
        }

        if (log.isDebugEnabled()) {
            log.debug("Service Provider Mapped Roles: " + spMappedRoles);
        }
        return spMappedRoles;
    }

    // Execute only if it has allowed removing userstore domain from the sp level configurations.
    private static boolean isRemoveUserDomainInRole(SequenceConfig sequenceConfig) {

        return !sequenceConfig.getApplicationConfig().getServiceProvider().getLocalAndOutBoundAuthenticationConfig().
                isUseUserstoreDomainInRoles();
    }
}
