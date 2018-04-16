/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.User;

/**
 * Javascript wrapper for {@link User}.
 * This provides controlled access to {@link User} object via provided javascript native syntax.
 * e.g
 * var userName = context.lastAttemptedUser.username
 *
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticatedUser.
 *
 * @see User
 */
public class JsUser extends AbstractJSObjectWrapper<User> {

    public JsUser(User wrapped) {
        super(wrapped);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName();
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain();
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain();
            default:
                return super.getMember(name);
        }
    }

    @Override
    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName() != null;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain() != null;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain() != null;
            default:
                return super.hasMember(name);
        }
    }
}
