/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

/**
 * Javascript wrapper for Java level AuthenticationContext.
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
public class JsAuthenticationContext extends AbstractJSObjectWrapper<AuthenticationContext> {

    public JsAuthenticationContext(AuthenticationContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Object getMember(String name) {
        if (wrapped == null) {
            return super.getMember(name);
        }
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_REQUESTED_ACR:
                return wrapped.getRequestedAcr();
            case FrameworkConstants.JSAttributes.JS_LAST_AUTHENTICATED_USER:
                return new JsAuthenticatedUser(wrapped.getLastAuthenticatedUser());
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return wrapped.getTenantDomain();
            case FrameworkConstants.JSAttributes.JS_INITIAL_REQUEST:
                return new JsServletRequest(wrapped.getInitialRequest());
            case FrameworkConstants.JSAttributes.JS_REQUEST:
                return new JsServletRequest(wrapped.getRequest());
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return new JsServletResponse(wrapped.getResponse());
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {
        if (wrapped == null) {
            return super.hasMember(name);
        }
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_REQUESTED_ACR:
                return wrapped.getRequestedAcr() != null;
            case FrameworkConstants.JSAttributes.JS_LAST_AUTHENTICATED_USER:
                return wrapped.getLastAuthenticatedUser() != null;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return wrapped.getTenantDomain() != null;
            case FrameworkConstants.JSAttributes.JS_INITIAL_REQUEST:
                return wrapped.getInitialRequest() != null;
            case FrameworkConstants.JSAttributes.JS_REQUEST:
                return wrapped.getRequest() != null;
            case FrameworkConstants.JSAttributes.JS_RESPONSE:
                return wrapped.getResponse() != null;
            default:
                return super.hasMember(name);
        }
    }

    @Override
    public void removeMember(String name) {
        if (wrapped == null) {
            super.removeMember(name);
            return;
        }
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                wrapped.setSelectedAcr(null);
                break;
            default:
                super.removeMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {
        if (wrapped == null) {
            super.setMember(name, value);
            return;
        }
        switch (name) {
            case FrameworkConstants.JSAttributes.JS_SELECTED_ACR:
                wrapped.setSelectedAcr(String.valueOf(value));
                break;
            default:
                super.setMember(name, value);
        }
    }
}
