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
 */

package org.wso2.carbon.identity.gateway.api.context;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;

import java.io.Serializable;
import java.util.Map;

public class GatewayMessageContext<T1 extends Serializable, T2 extends Serializable, T3 extends GatewayRequest>
        extends
        MessageContext<T1, T2>
        implements Serializable {

    private static final long serialVersionUID = 104614801932285909L;

    protected T3 identityRequest;

    public GatewayMessageContext(T3 identityRequest, Map<T1, T2> parameters) {
        super(parameters);
        this.identityRequest = identityRequest;
    }

    public GatewayMessageContext(T3 identityRequest) {
        this.identityRequest = identityRequest;
    }

    public T3 getIdentityRequest() {
        return identityRequest;
    }

    public void setIdentityRequest(T3 identityRequest) {
        this.identityRequest = identityRequest;
    }
}
