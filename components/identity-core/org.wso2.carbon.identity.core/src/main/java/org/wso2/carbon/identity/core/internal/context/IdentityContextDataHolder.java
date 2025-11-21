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

package org.wso2.carbon.identity.core.internal.context;

import org.wso2.carbon.identity.core.context.model.Actor;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.Organization;
import org.wso2.carbon.identity.core.context.model.Request;
import org.wso2.carbon.identity.core.context.model.RootOrganization;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * This class is used to store the identity context information of the current thread.
 */
public class IdentityContextDataHolder {

    private Request request;
    private Flow flow;
    private Actor actor;
    private String accessTokenIssuedOrganization;
    private RootOrganization rootOrganization;
    private Organization organization;

    // Stack (FILO) to manage nested flows in the current thread context.
    private Deque<Flow> flowSequence = new ArrayDeque<>();

    private static final ThreadLocal<IdentityContextDataHolder> currentContextHolder =
            new ThreadLocal<IdentityContextDataHolder>() {
                @Override
                protected IdentityContextDataHolder initialValue() {

                    return new IdentityContextDataHolder();
                }
            };

    /**
     * Default constructor to disallow creation of the IdentityContextDataHolder.
     */
    private IdentityContextDataHolder() {

    }

    /**
     * This method will always attempt to obtain an instance of the current IdentityContext from the
     * thread-local copy.
     *
     * @return the IdentityContextDataHolder.
     */
    public static IdentityContextDataHolder getThreadLocalIdentityContextHolder() {

        return currentContextHolder.get();
    }

    /**
     * Set the request.
     *
     * @param request Request.
     */
    public void setRequest(Request request) {

        CarbonUtils.checkSecurity();
        this.request = request;
    }

    /**
     * Get the request.
     *
     * @return Request.
     */
    public Request getRequest() {

        return request;
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
     * Set the actor who is authenticating in the request.
     *
     * @param actor actor of the request.
     */
    public void setActor(Actor actor) {

        CarbonUtils.checkSecurity();
        this.actor = actor;
    }

    /**
     * Get the initiator of the request.
     *
     * @return Initiator of the request.
     */
    public Actor getActor() {

        return actor;
    }

    public String getAccessTokenIssuedOrganization() {

        return accessTokenIssuedOrganization;
    }

    public void setAccessTokenIssuedOrganization(String accessTokenIssuedOrganization) {

        CarbonUtils.checkSecurity();
        this.accessTokenIssuedOrganization = accessTokenIssuedOrganization;
    }

    public RootOrganization getRootOrganization() {

        return rootOrganization;
    }

    public void setRootOrganization(RootOrganization rootOrganization) {

        CarbonUtils.checkSecurity();
        this.rootOrganization = rootOrganization;
    }

    public Organization getOrganization() {

        return organization;
    }

    public void setOrganization(Organization organization) {

        CarbonUtils.checkSecurity();
        this.organization = organization;
    }

    /**
     * This method will destroy the current IdentityContextDataHolder.
     */
    public static void destroyCurrentIdentityContextDataHolder() {

        currentContextHolder.remove();
    }

    /**
     * Enter a new flow. Pushes the given flow onto the flow sequence.
     *
     * @param flow The new flow to be started.
     */
    public void enterFlow(Flow flow) {

        CarbonUtils.checkSecurity();
        if (flow != null) {
            flowSequence.push(flow);
        }
    }

    /**
     * Exit the current flow. Pops the top flow from the flow sequence.
     *
     * @return The flow that was removed, or null if none.
     */
    public Flow exitFlow() {

        CarbonUtils.checkSecurity();
        if (!flowSequence.isEmpty()) {
            return flowSequence.pop();
        }
        return null;
    }

    /**
     * Peek at the current flow without removing it from the flow sequence.
     *
     * @return The current active flow, or null if no flow is active.
     */
    public Flow getCurrentFlow() {

        if (!flowSequence.isEmpty()) {
            return flowSequence.peek();
        }
        return null;
    }
}
