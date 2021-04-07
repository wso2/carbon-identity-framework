/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.entitlement.endpoint.auth;

import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.container.ContainerRequestContext;

/**
 * This stores the authenticators registered for Entitlement REST endpoints and returns the appropriate
 * authenticator as requested by authentication filter associated with Entitlement REST endpoints.
 */
public class EntitlementAuthenticatorRegistry {

    private static EntitlementAuthenticatorRegistry EntitlementAuthRegistry;
    private static Map<Integer, EntitlementAuthenticationHandler> EntitlementAuthHandlers = new TreeMap<Integer,
            EntitlementAuthenticationHandler>();

    /**
     * Initialize the EntitlementAuthenticatorRegistry Singleton
     *
     * @return
     */
    public static EntitlementAuthenticatorRegistry getInstance() {
        if (EntitlementAuthRegistry == null) {
            synchronized (EntitlementAuthenticatorRegistry.class) {
                if (EntitlementAuthRegistry == null) {
                    EntitlementAuthRegistry = new EntitlementAuthenticatorRegistry();
                    return EntitlementAuthRegistry;
                } else {
                    return EntitlementAuthRegistry;
                }
            }
        }
        return EntitlementAuthRegistry;
    }

    /**
     * Given the RESTful message and other info, returns the authenticator which can handle the request.
     *
     * @param message
     * @return
     */
    public EntitlementAuthenticationHandler getAuthenticator(ContainerRequestContext message) {
        //since we use a tree map to store authenticators, they are ordered based on the priority.
        //therefore, we iterate over the authenticators and check the can handle method
        for (EntitlementAuthenticationHandler entitlementAuthenticationHandler : EntitlementAuthHandlers.values()) {
            if (entitlementAuthenticationHandler.canHandle(message)) {
                return entitlementAuthenticationHandler;
            }
        }
        return null;
    }

    public void setAuthenticator(EntitlementAuthenticationHandler EntitlementAuthHandler) {
        EntitlementAuthHandlers.put(EntitlementAuthHandler.getPriority(), EntitlementAuthHandler);
    }

    public void removeAuthenticator(EntitlementAuthenticationHandler entitlementAuthenticationHandler) {
        EntitlementAuthHandlers.remove(entitlementAuthenticationHandler.getPriority());
    }

}