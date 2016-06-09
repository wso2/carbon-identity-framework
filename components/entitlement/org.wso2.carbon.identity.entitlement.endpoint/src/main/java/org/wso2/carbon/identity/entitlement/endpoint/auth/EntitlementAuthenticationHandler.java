/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;

import java.util.Map;

/**
 * Interface to be implemented by any type of authentication handlers that will be registered
 * to authenticate Entitlement requests coming to Entitlement REST endpoints.
 * Derived from SCIMAuthenticationHandler class
 */
public interface EntitlementAuthenticationHandler {

    /**
     * Returns the priority of the particular authentication handler implementation.
     *
     * @return
     */
    public int getPriority();

    /**
     * Sets the priority of the particular authentication handler implementation.
     */
    public void setPriority(int priority);

    /**
     * To check whether the given handler can authenticate the request by looking at the message,
     * including headers.
     *
     * @param message
     * @param classResourceInfo
     * @return
     */
    public boolean canHandle(Message message, ClassResourceInfo classResourceInfo);

    /**
     * If the authenticator can handle the request, decide whether the request is authenticated.
     *
     * @param message
     * @param classResourceInfo
     * @return
     */
    public boolean isAuthenticated(Message message, ClassResourceInfo classResourceInfo);

    /**
     * To set the properties specific to each authenticator
     *
     * @param authenticatorProperties
     */
    public void setProperties(Map<String, String> authenticatorProperties);

}
