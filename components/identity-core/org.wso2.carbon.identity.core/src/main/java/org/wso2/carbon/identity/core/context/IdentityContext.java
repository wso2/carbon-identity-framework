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

package org.wso2.carbon.identity.core.context;

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.Initiator;
import org.wso2.carbon.identity.core.internal.IdentityContextDataHolder;

/**
 * This class is used to store the identity context information of the current thread.
 */
public class IdentityContext extends CarbonContext {

    private final IdentityContextDataHolder identityContextDataHolder;

    /**
     * Creates a IdentityContext using the given IdentityContext data holder as its backing instance.
     *
     * @param identityContextDataHolder the IdentityContext data holder that backs this CarbonContext object.
     */
    protected IdentityContext(IdentityContextDataHolder identityContextDataHolder) {

        super();
        this.identityContextDataHolder = identityContextDataHolder;
    }

    public static IdentityContext getThreadLocalIdentityContext() {

        return new IdentityContext(IdentityContextDataHolder.getThreadLocalIdentityContextHolder());
    }

    /**
     * Set the flow of the request.
     *
     * @param flow flow of the request.
     */
    public void setFlow(Flow flow) {

        identityContextDataHolder.setFlow(flow);
    }

    /**
     * Get the flow id of the request.
     *
     * @return Flow of the request.
     */
    public Flow getFlow() {

        return identityContextDataHolder.getFlow();
    }

    /**
     * Set the initiator of the request.
     *
     * @param initiator initiator of the request.
     */
    public void setInitiator(Initiator initiator) {

        identityContextDataHolder.setInitiator(initiator);
    }

    /**
     * Get the initiator of the request.
     *
     * @return Initiator of the request.
     */
    public Initiator getInitiator() {

        return identityContextDataHolder.getInitiator();
    }
}
