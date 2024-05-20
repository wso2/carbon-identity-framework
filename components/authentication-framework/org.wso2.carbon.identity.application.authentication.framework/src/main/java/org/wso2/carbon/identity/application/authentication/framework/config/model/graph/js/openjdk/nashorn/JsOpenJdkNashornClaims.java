/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Represent the user's claim. Can be either remote or local.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing OpenJDK Nashorn engine.
 *
 */
public class JsOpenJdkNashornClaims extends JsClaims implements AbstractOpenJdkNashornJsObject {

    /**
     * Constructor to get the user authenticated in step 'n'
     *
     * @param step                 The authentication step
     * @param idp                  The authenticated IdP
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsOpenJdkNashornClaims(AuthenticationContext context, int step, String idp, boolean isRemoteClaimRequest) {

        super(context, step, idp, isRemoteClaimRequest);
    }

    public JsOpenJdkNashornClaims(int step, String idp, boolean isRemoteClaimRequest) {

        super(step, idp, isRemoteClaimRequest);
    }

    /**
     * Constructor to get user who is not directly from a authentication step. Eg. Associated user of authenticated
     * federated user in a authentication step.
     *
     * @param authenticatedUser    Authenticated user
     * @param isRemoteClaimRequest Whether the request is for remote claim (false for local claim request)
     */
    public JsOpenJdkNashornClaims(AuthenticatedUser authenticatedUser, boolean isRemoteClaimRequest) {

        super(authenticatedUser, isRemoteClaimRequest);
    }

    public JsOpenJdkNashornClaims(AuthenticationContext context, AuthenticatedUser authenticatedUser,
                                  boolean isRemoteClaimRequest) {

        super(context, authenticatedUser, isRemoteClaimRequest);
    }

    @Override
    public void setMember(String claimUri, Object claimValue) {

        boolean isClaimSet = setMemberObject(claimUri, claimValue);
        if (!isClaimSet) {
            AbstractOpenJdkNashornJsObject.super.setMember(claimUri, claimValue);
        }
    }
}
