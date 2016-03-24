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

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.wso2.carbon.identity.core.bean.context.MessageContext;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class InboundMessageContext<T1 extends Serializable, T2 extends Serializable> extends MessageContext
        implements Serializable {

    private static final long serialVersionUID = 1146964596245780217L;

	protected InboundRequest request;
    protected Map<T1,T2> parameters = new HashMap<>();

    public InboundMessageContext(InboundRequest request, Map<T1, T2> parameters){
        super(parameters);
        this.request = request;
    }

	public InboundRequest getRequest() {
		return request;
	}
}
