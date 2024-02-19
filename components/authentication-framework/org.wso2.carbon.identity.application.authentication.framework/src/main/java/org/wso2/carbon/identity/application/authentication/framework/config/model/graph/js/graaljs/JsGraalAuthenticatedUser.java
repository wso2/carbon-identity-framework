/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs;

import org.apache.commons.lang.StringUtils;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

/**
 * Javascript wrapper for Java level AuthenticatedUser.
 * This wrapper uses GraalJS polyglot context.
 * This provides controlled access to AuthenticatedUser object via provided javascript native syntax.
 * e.g
 * var userName = context.lastAuthenticatedUser.username
 * <p>
 * instead of
 * var userName = context.getLastAuthenticatedUser().getUserName()
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticatedUser.
 *
 * @see AuthenticatedUser
 */
public class JsGraalAuthenticatedUser extends JsAuthenticatedUser implements ProxyObject {

    public JsGraalAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser, int step,
                                    String idp) {

        super(context, wrappedUser, step, idp);
    }

    public JsGraalAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public JsGraalAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

        super(context, wrappedUser);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT_IDENTIFIER:
                return getWrapped().getAuthenticatedSubjectIdentifier();
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName();
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain();
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain();
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new JsGraalClaims(getContext(), step, idp, false);
                } else {
                    // Represent step independent user
                    return new JsGraalClaims(getContext(), getWrapped(), false);
                }
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new JsGraalClaims(getContext(), step, idp, true);
                } else {
                    // Represent step independent user
                    return new JsGraalClaims(getContext(), getWrapped(), true);
                }
            case FrameworkConstants.JSAttributes.JS_LOCAL_ROLES:
                return getLocalRoles();
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new JsGraalRuntimeClaims(getContext(), step, idp);
                } else {
                    // Represent step independent user
                    return new JsGraalRuntimeClaims(getContext(), getWrapped());
                }
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return ProxyArray.fromArray(
                FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT_IDENTIFIER,
                FrameworkConstants.JSAttributes.JS_USERNAME,
                FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN,
                FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN,
                FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS,
                FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS,
                FrameworkConstants.JSAttributes.JS_LOCAL_ROLES,
                FrameworkConstants.JSAttributes.JS_CLAIMS);
    }

    public void putMember(String name, Value value) {

        setMember(name, value.asString());
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                return true;
            default:
                return super.hasMember(name);
        }
    }

}
