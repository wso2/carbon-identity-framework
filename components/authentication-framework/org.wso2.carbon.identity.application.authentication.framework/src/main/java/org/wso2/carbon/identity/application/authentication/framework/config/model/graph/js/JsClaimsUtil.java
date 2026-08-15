/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;

/**
 * Utility methods for validating the tenant domain of the authenticated user when claims are accessed
 * from adaptive authentication scripts. Shared across the Nashorn and OpenJDK Nashorn claim implementations
 * to avoid duplicating the tenant validation logic in each engine specific class.
 */
public class JsClaimsUtil {

    private static final Log LOG = LogFactory.getLog(JsClaimsUtil.class);
    private static final String SAAS_ENABLE_CROSS_TENANT_OPERATIONS = "SaaS.EnableCrossTenantOperations";

    private JsClaimsUtil() {

    }

    /**
     * Validate whether the authenticated user belongs to the tenant domain of the current authentication flow.
     * Cross-tenant claim access is only permitted for SaaS applications when it is explicitly enabled via the
     * {@code SaaS.EnableCrossTenantOperations} configuration.
     *
     * @param authenticatedUser Authenticated user whose claims are being accessed.
     * @param context           Authentication context of the current flow.
     * @return {@code true} if the authenticated user belongs to the current tenant (or cross-tenant access is
     * explicitly permitted for the SaaS application); {@code false} otherwise.
     */
    public static boolean isAuthenticatedUserInCurrentTenant(AuthenticatedUser authenticatedUser,
                                                             AuthenticationContext context) {

        if (authenticatedUser == null) {
            return false;
        }

        if (isSaasApp(context) && isSaaSCrossTenantOperationsEnabled()) {
            return true;
        }

        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
            return StringUtils.equals(authenticatedUser.getTenantDomain(),
                    PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        }

        if (context == null || StringUtils.isBlank(context.getTenantDomain())) {
            LOG.warn("Unable to determine the tenant domain from the authentication context. " +
                    "Hence user tenant domain validation is considered as failed.");
            return false;
        }
        return StringUtils.equals(authenticatedUser.getTenantDomain(), context.getTenantDomain());
    }

    private static boolean isSaaSCrossTenantOperationsEnabled() {

        String value = IdentityUtil.getProperty(SAAS_ENABLE_CROSS_TENANT_OPERATIONS);
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    private static boolean isSaasApp(AuthenticationContext context) {

        if (context == null || context.getSequenceConfig() == null
                || context.getSequenceConfig().getApplicationConfig() == null
                || context.getSequenceConfig().getApplicationConfig().getServiceProvider() == null) {
            LOG.debug("Unable to determine if the application is a SaaS app. Treating as non-SaaS app.");
            return false;
        }
        return context.getSequenceConfig().getApplicationConfig().getServiceProvider().isSaasApp();
    }
}
