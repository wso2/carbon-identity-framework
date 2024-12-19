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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Javascript wrapper for Java level AuthenticatedUser.
 * This wrapper uses jdk.nashorn engine.
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
public class JsNashornAuthenticatedUser extends JsAuthenticatedUser implements AbstractJsObject {

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param context Authentication context
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsNashornAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser,
                                      int step, String idp) {

        super(context, wrappedUser, step, idp);
    }

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsNashornAuthenticatedUser(AuthenticatedUser wrappedUser, int step, String idp) {

        super(wrappedUser, step, idp);
    }

    /**
     * Constructor to be used when required to access step independent user.
     *
     * @param wrappedUser Authenticated user
     */
    public JsNashornAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public JsNashornAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

        super(context, wrappedUser);
    }
}
