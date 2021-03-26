/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseClaims;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;


/**
 * Represent the user's claim in Nashorn execution. Can be either remote or local.
 */
public class NashornJsClaims extends JsBaseClaims implements AbstractJsObject {

    public NashornJsClaims(AuthenticationContext context, int step, String idp, boolean isRemoteClaimRequest) {

        super(context, step, idp, isRemoteClaimRequest);
    }

    public NashornJsClaims(int step, String idp, boolean isRemoteClaimRequest) {

        super(step, idp, isRemoteClaimRequest);
    }

    public NashornJsClaims(AuthenticatedUser authenticatedUser, boolean isRemoteClaimRequest) {

        super(authenticatedUser, isRemoteClaimRequest);
    }

    public NashornJsClaims(AuthenticationContext context, AuthenticatedUser authenticatedUser,
                           boolean isRemoteClaimRequest) {

        super(context, authenticatedUser, isRemoteClaimRequest);
    }

    @Override
    public void setMember(String claimUri, Object claimValue) {

        if (authenticatedUser != null) {
            if (isRemoteClaimRequest) {
                setFederatedClaim(claimUri, String.valueOf(claimValue));
                return;
            } else {
                setLocalClaim(claimUri, String.valueOf(claimValue));
                return;
            }
        }
        AbstractJsObject.super.setMember(claimUri, claimValue);
    }
}
