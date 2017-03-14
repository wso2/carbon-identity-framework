/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.carbon.identity.gateway.authentication.sequence;


import org.wso2.carbon.identity.gateway.api.handler.AbstractGatewayHandler;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.exception.AuthenticationHandlerException;

/**
 * AbstractSequenceBuildFactory is an extension point to put a custom sequence implementation to the flow. We can
 * implement Sequence interface and extends this class to create custom builder to return custom sequence.
 */
public abstract class AbstractSequenceBuildFactory extends AbstractGatewayHandler {

    /**
     * To return custom sequence, this method should implement and return new sequence type instance.
     *
     * @param authenticationContext
     * @return
     * @throws AuthenticationHandlerException
     */
    public abstract Sequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;
}
