/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.core.internal;

import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.Initiator;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Stack;

/**
 * This class is used to store the identity context information of the current thread.
 */
public class IdentityContextDataHolder {

    private Flow flow;
    private Initiator initiator;

    private static final ThreadLocal<IdentityContextDataHolder> currentContextHolder =
            new ThreadLocal<IdentityContextDataHolder>() {
        protected IdentityContextDataHolder initialValue() {
            return new IdentityContextDataHolder();
        }
    };

    private static final ThreadLocal<Stack<IdentityContextDataHolder>> parentContextHolderStack =
            new ThreadLocal<Stack<IdentityContextDataHolder>>();

    /**
     * Default constructor to disallow creation of the CarbonContext.
     */
    private IdentityContextDataHolder() {

    }

    /**
     * This method will always attempt to obtain an instance of the current IdentityContext from the
     * thread-local copy.
     *
     * @return the IdentityContext holder.
     */
    public static IdentityContextDataHolder getThreadLocalIdentityContextHolder() {

        return currentContextHolder.get();
    }

    /**
     * Set the flow of the request.
     *
     * @param flow flow of the request.
     */
    public void setFlow(Flow flow) {

        CarbonUtils.checkSecurity();
        this.flow = flow;
    }

    /**
     * Get the flow id of the request.
     *
     * @return Flow of the request.
     */
    public Flow getFlow() {

        return flow;
    }

    /**
     * Set the initiator of the request.
     *
     * @param initiator initiator of the request.
     */
    public void setInitiator(Initiator initiator) {

        CarbonUtils.checkSecurity();
        this.initiator = initiator;
    }

    /**
     * Get the initiator of the request.
     *
     * @return Initiator of the request.
     */
    public Initiator getInitiator() {

        return initiator;
    }

    /**
     * Starts a tenant flow. This will stack the current IdentityContext and begin
     * a new nested flow which can have an entirely different context. This is
     * ideal for scenarios where multiple super-tenant and sub-tenant phases are
     * required within as a single block of execution.
     */
    public void startTenantFlow() {

        Stack<IdentityContextDataHolder> identityContextDataHolders = parentContextHolderStack.get();
        if (identityContextDataHolders == null) {
            identityContextDataHolders = new Stack<IdentityContextDataHolder>();
            parentContextHolderStack.set(identityContextDataHolders);
        }
        identityContextDataHolders.push(currentContextHolder.get());
        currentContextHolder.remove();
    }

    /**
     * This will end the tenant flow and restore the previous IdentityContext.
     */
    public void endTenantFlow() {

        Stack<IdentityContextDataHolder> carbonContextDataHolders = parentContextHolderStack.get();
        if (carbonContextDataHolders != null) {
            currentContextHolder.set(carbonContextDataHolders.pop());
        }
    }

    /**
     * This method will destroy the current CarbonContext holder.
     */
    public static void destroyCurrentCarbonContextHolder() {

        currentContextHolder.remove();
        parentContextHolderStack.remove();
    }
}
