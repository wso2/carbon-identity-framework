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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Common utility used by Default Sequence Handlers.
 */
public class DefaultSequenceHandlerUtils {

    private static Log log = LogFactory.getLog(DefaultSequenceHandlerUtils.class);

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

        // SP role mapped role values joined by Multi Attribute Separatorg
        String spMappedRoles = null;
        if (CollectionUtils.isNotEmpty(locallyMappedUserRoles)) {
            // Get SP Role mappings
            Map<String, String> localToSpRoleMapping = sequenceConfig.getApplicationConfig().getRoleMappings();
            List<String> spMappedRoleList = new ArrayList<>();
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
                        // Add local role to the list since there are no SP mapped roles for this one
                        spMappedRoleList.add(locallyMappedRole);
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No local roles to map to Service Provider role mappings. Sending back all local roles " +
                            "as service provider mapped roles.");
                }
                // We don't have any sp role mappings
                spMappedRoleList = locallyMappedUserRoles;
            }
            spMappedRoles = StringUtils.join(spMappedRoleList.toArray(), FrameworkUtils.getMultiAttributeSeparator());
        }

        if (log.isDebugEnabled()) {
            log.debug("Service Provider Mapped Roles: " + spMappedRoles);
        }
        return spMappedRoles;
    }
}
