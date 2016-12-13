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

public class IdentityMessageContext<T1 extends Serializable, T2 extends Serializable> extends MessageContext<T1,T2>
        implements Serializable {

    private static final long serialVersionUID = 104614801932285909L;

	protected IdentityRequest request;
    protected String relyingPartyID;

    public IdentityMessageContext(IdentityRequest request, Map<T1,T2> parameters) {
        super(parameters);
        this.request = request;
    }

    public IdentityMessageContext(IdentityRequest request) {
        this.request = request;
    }

   /**
    * This constructor is deprecated because any processor using {@link IdentityMessageContext} must create a
    * {@link IdentityRequest} object.
    */
    @Deprecated
    public IdentityMessageContext() {

    }

	public IdentityRequest getRequest() {
		return request;
	}

    public void setRelyingPartyID(String relyingPartyID) {
        this.relyingPartyID = relyingPartyID;
    }

    public String getRelyingPartyId() {
        return relyingPartyID;
    }
}
