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
import javax.ws.rs.container.ContainerRequestContext;

/**
 * Interface to be implemented by any type of authentication handlers that will be registered
 * to authenticate Entitlement requests coming to Entitlement REST endpoints.
 */
public interface EntitlementAuthenticationHandler {

    /**
     * Returns the priority of the particular authentication handler implementation.
     *
     * @return
     */
    int getPriority();

    /**
     * Sets the priority of the particular authentication handler implementation.
     */
    void setPriority(int priority);

    /**
     * To check whether the given handler can authenticate the request by looking at the message,
     * including headers.
     *
     * @param message
     * @return
     */
    boolean canHandle(ContainerRequestContext message);

    /**
     * If the authenticator can handle the request, decide whether the request is authenticated.
     *
     * @param message
     * @return
     */
    boolean isAuthenticated(ContainerRequestContext message);

    /**
     * To set the properties specific to each authenticator
     *
     * @param authenticatorProperties
     */
    void setProperties(Map<String, String> authenticatorProperties);

}
