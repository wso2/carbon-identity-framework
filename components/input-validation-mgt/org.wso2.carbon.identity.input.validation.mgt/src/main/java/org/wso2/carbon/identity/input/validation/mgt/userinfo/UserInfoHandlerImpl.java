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

package org.wso2.carbon.identity.input.validation.mgt.userinfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.multitenancy.userinfo.UserInfoHandler;

/**
 * This class implemented to use username input validation.
 * TenantQualifiedURl option should be enabled along with this implementation.
 */
public class UserInfoHandlerImpl implements UserInfoHandler {

    private static final Log LOG = LogFactory.getLog(UserInfoHandlerImpl.class);
    private static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";

    /**
     * Since thread local properties has tenant domain context, we can retrieve the tenant from the context.
     * If the username is tenant qualified, tenant domain will be filtered out from username.
     *
     * @param username Username.
     * @return TenantAwareUsername.
     */
    @Override
    public String getTenantAwareUsername(String username) {

        String tenantDomain = getTenantDomainFromContext();
        if (tenantDomain.equalsIgnoreCase(username.substring(username.lastIndexOf('@') + 1))) {
                username = username.substring(0, username.lastIndexOf('@'));
        }
        return username;
    }

    /**
     * Since thread local properties has tenant domain context, we can retrieve the tenant from the context.
     *
     * @param username Username.
     * @return TenantAwareUsername.
     */
    @Override
    public String getTenantDomain(String username) {

        return getTenantDomainFromContext().toLowerCase();
    }

    /**
     * Retrieves loaded tenant domain from carbon context.
     *
     * @return tenant domain of the request is being served.
     */
    private String getTenantDomainFromContext() {

        String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        if (IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT) != null) {
            tenantDomain = (String) IdentityUtil.threadLocalProperties.get().get(TENANT_NAME_FROM_CONTEXT);
        }
        return tenantDomain;
    }
}
