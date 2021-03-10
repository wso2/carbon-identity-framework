/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

/**
 * Javascript wrapper for Java level AuthenticatedUser for Nashorn.
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
public class NashornJsAuthenticatedUser extends JsBaseAuthenticatedUser implements AbstractJsObject {

    public NashornJsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser,
                                      int step, String idp) {

        super(context, wrappedUser, step, idp);
    }

    public NashornJsAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public NashornJsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

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
                    return new NashornJsClaims(getContext(), step, idp, false);
                } else {
                    // Represent step independent user
                    return new NashornJsClaims(getContext(), getWrapped(), false);
                }
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new NashornJsClaims(getContext(), step, idp, true);
                } else {
                    // Represent step independent user
                    return new NashornJsClaims(getContext(), getWrapped(), true);
                }
            case FrameworkConstants.JSAttributes.JS_LOCAL_ROLES:
                return getLocalRoles();
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new NashornJsRuntimeClaims(getContext(), step, idp);
                } else {
                    // Represent step independent user
                    return new NashornJsRuntimeClaims(getContext(), getWrapped());
                }
            default:
                return super.getMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        super.setMember(name, (String) value);
    }

}

