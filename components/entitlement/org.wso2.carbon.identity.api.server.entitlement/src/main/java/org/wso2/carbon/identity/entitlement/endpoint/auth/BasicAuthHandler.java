/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.entitlement.endpoint.util.EntitlementEndpointConstants;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.List;
import java.util.Map;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * This is the default BASIC-Auth authentication handler for Entitlement REST Endpoints.
 */
public class BasicAuthHandler implements EntitlementAuthenticationHandler {

    private static Log log = LogFactory.getLog(BasicAuthHandler.class);
    /* constants specific to this authenticator */
    private final String BASIC_AUTH_HEADER = "Basic";
    private final int DEFAULT_PRIORITY = 5;
    /* property map */
    private Map<String, String> properties;
    /* properties specific to this authenticator */
    private int priority;

    public void setDefaultPriority() {
        priority = DEFAULT_PRIORITY;
    }

    /**
     * Ideally this should be configurable. For the moment, hard code the priority.
     *
     * @return
     */
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean canHandle(ContainerRequestContext message) {
        // check the "Authorization" header and if "Basic" is there, can be handled.

        // get the value for Authorization Header
        List authzHeaders = message.getHeaders().get(EntitlementEndpointConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            // get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);
            return authzHeader != null && authzHeader.contains(BASIC_AUTH_HEADER);
        }
        return false;
    }

    public boolean isAuthenticated(ContainerRequestContext message) {
        // extract authorization header and authenticate.

        // get the value for Authorization Header
        List authzHeaders = message.getHeaders().get(EntitlementEndpointConstants.AUTHORIZATION_HEADER);
        if (authzHeaders != null) {
            // get the authorization header value, if provided
            String authzHeader = (String) authzHeaders.get(0);

            // decode it and extract username and password
            byte[] decodedAuthHeader = Base64.decode(authzHeader.split(" ")[1]);
            String authHeader = new String(decodedAuthHeader);
            String userName = authHeader.split(":")[0];
            String password = authHeader.split(":")[1];
            if (userName != null && password != null) {
                String tenantDomain = MultitenantUtils.getTenantDomain(userName);
                String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);

                try {
                    // get super tenant context and get realm service which is an osgi service
                    RealmService realmService = (RealmService) PrivilegedCarbonContext
                            .getThreadLocalCarbonContext().getOSGiService(RealmService.class);
                    if (realmService != null) {
                        int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                        if (tenantId == -1) {
                            log.error("Invalid tenant domain " + tenantDomain);
                            return false;
                        }
                        // get tenant's user realm
                        UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                        boolean authenticated = userRealm.getUserStoreManager().authenticate(
                                tenantAwareUsername, password);
                        if (authenticated) {
                            // authentication success. set the username for authorization header and
                            // proceed the REST call
                            authzHeaders.set(0, userName);
                            return true;
                        } else {
                            if (log.isDebugEnabled()) {
                                log.debug("Authentication failed for the user: " + tenantAwareUsername
                                        + "@" + tenantDomain);
                            }
                            return false;
                        }
                    } else {
                        log.error("Error in getting Realm Service for user: " + userName);
                        return false;
                    }
                } catch (UserStoreException e) {
                    log.error("Internal server error while authenticating the user.");
                    return false;
                }
            } else {
                log.error("Authentication required for this resource. " +
                                "Username or password not provided.");
                return false;
            }
        } else {
            log.error("Authentication required for this resource. " +
                          "Authorization header not present in the request.");
            return false;
        }

    }

    /**
     * To set the properties specific to each authenticator
     *
     * @param authenticatorProperties
     */
    public void setProperties(Map<String, String> authenticatorProperties) {
        // set the priority read from config
        this.properties = authenticatorProperties;
        String priorityString = properties.get(EntitlementEndpointConstants.PROPERTY_NAME_PRIORITY);
        if (priorityString != null) {
            priority = Integer.parseInt(properties
                    .get(EntitlementEndpointConstants.PROPERTY_NAME_PRIORITY));
        } else {
            priority = DEFAULT_PRIORITY;
        }
    }
}
