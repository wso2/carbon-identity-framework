/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import com.google.gson.JsonObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This class handles the redirect URL retrieval for invalid session index.
 */
public class LoginContextManagementUtil {

    private static final String SP_REDIRECT_URL_RESOURCE_PATH = "/identity/config/relyingPartyRedirectUrls";
    private static final Log log = LogFactory.getLog(LoginContextManagementUtil.class);

    public static void handleLoginContext(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String sessionDataKey = request.getParameter("sessionDataKey");
        String relyingParty = request.getParameter("relyingParty");
        String tenantDomain = request.getParameter("tenantDomain");

        JsonObject result = new JsonObject();
        response.setContentType("application/json");
        if (StringUtils.isBlank(relyingParty) || StringUtils.isBlank(sessionDataKey)) {
            if (log.isDebugEnabled()) {
                log.debug("Required data to proceed is not available in the request.");
            }
            //cannot handle the request without sessionDataKey
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
            return;
        }

        // Valid Request
        if (FrameworkUtils.getAuthenticationContextFromCache(sessionDataKey) != null) {
            result.addProperty("status", "success");
            response.getWriter().write(result.toString());
        } else {
            String redirectUrl = getRelyingPartyRedirectUrl(relyingParty, tenantDomain);
            if (StringUtils.isBlank(redirectUrl)) {
                if (log.isDebugEnabled()) {
                    log.debug("Redirect URL is not available for the relaying party - " + relyingParty);
                }
                // Can't handle
                result.addProperty("status", "success");
                response.getWriter().write(result.toString());
            }

            result.addProperty("status", "redirect");
            result.addProperty("redirectUrl", redirectUrl);
            response.getWriter().write(result.toString());
        }
    }

    /**
     * Returns the redirect url configured in the registry against relying party.
     *
     * @param relyingParty Name of the relying party
     * @param tenantDomain Tenant Domain.
     * @return Redirect URL.
     */
    public static String getRelyingPartyRedirectUrl(String relyingParty, String tenantDomain) {
        if (log.isDebugEnabled()) {
            log.debug("retrieving configured url against relying party : " + relyingParty + "for tenant domain : " +
                    tenantDomain);
        }

        int tenantId;
        if (StringUtils.isEmpty(tenantDomain)) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant domain is not available. Hence using super tenant domain");
            }
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            tenantId = MultitenantConstants.SUPER_TENANT_ID;
        } else {
            tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
        }
        try {
            IdentityTenantUtil.initializeRegistry(tenantId, tenantDomain);
            Registry registry = IdentityTenantUtil.getConfigRegistry(tenantId);
            if (registry.resourceExists(SP_REDIRECT_URL_RESOURCE_PATH)) {
                Resource resource = registry.get(SP_REDIRECT_URL_RESOURCE_PATH);
                if (resource != null) {
                    String redirectUrl = resource.getProperty(relyingParty);
                    if (StringUtils.isNotEmpty(redirectUrl)) {
                        return redirectUrl;
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error while getting data from the registry.", e);
        } catch (IdentityException e) {
            log.error("Error while getting the tenant domain from tenant id : " + tenantId, e);
        }
        return null;
    }
}
