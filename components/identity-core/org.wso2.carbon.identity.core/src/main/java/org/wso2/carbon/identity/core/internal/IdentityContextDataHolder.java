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

import org.wso2.carbon.identity.core.context.model.Actor;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * This class is used to store the identity context information of the current thread.
 */
public class IdentityContextDataHolder {

    private Flow flow;
    private Actor actor;
    private String accessTokenIssuedOrganization;

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

    /**
     * This method will destroy the current IdentityContextDataHolder.
     */
    public static void destroyCurrentIdentityContextDataHolder() {

        currentContextHolder.remove();
    }
}
