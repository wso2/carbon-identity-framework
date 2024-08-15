/*
 * Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

/**
 * Javascript wrapper for Java level AuthenticationContext.
 * This wrapper uses jdk.nashorn engine.
 * This provides controlled access to AuthenticationContext object via provided javascript native syntax.
 * e.g
 * var requestedAcr = context.requestedAcr
 * <p>
 * instead of
 * var requestedAcr = context.getRequestedAcr()
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticationContext.
 *
 * @see AuthenticationContext
 */
public class JsNashornAuthenticationContext extends JsAuthenticationContext implements AbstractJsObject {

    public JsNashornAuthenticationContext(AuthenticationContext wrapped) {

        super(wrapped);
        initializeContext(wrapped);
    }

    public void setMember(String name, Object value) {

        super.setMemberObject(name, value);
    }

    @Override
    public void removeMember(String name) {

        if (!super.removeMemberObject(name)) {
            AbstractJsObject.super.removeMember(name);
        }
    }
}
