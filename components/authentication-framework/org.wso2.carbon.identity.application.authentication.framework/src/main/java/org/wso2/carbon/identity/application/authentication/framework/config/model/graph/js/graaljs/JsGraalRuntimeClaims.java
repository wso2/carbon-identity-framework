/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

/**
 * Represent the user's runtime claims for GraalJs Execution.
 */
public class JsGraalRuntimeClaims extends JsGraalClaims implements ProxyObject {

    public JsGraalRuntimeClaims(AuthenticationContext context, int step, String idp) {

        super(context, step, idp, false);
    }

    public JsGraalRuntimeClaims(AuthenticationContext context, AuthenticatedUser user) {

        super(context, user, false);
    }

    @Override
    public Object getMember(String claimUri) {

        if (authenticatedUser != null) {
            return getRuntimeClaim(claimUri);
        }
        return null;
    }

    @Override
    public boolean hasMember(String claimUri) {

        if (authenticatedUser != null) {
            return hasRuntimeClaim(claimUri);
        }
        return false;
    }

    @Override
    public void putMember(String claimUri, Value claimValue) {

        if (authenticatedUser != null) {
            setRuntimeClaim(claimUri, String.valueOf(claimValue));
        }
    }

    private Object getRuntimeClaim(String claimUri) {

        String runtimeClaimValue = getContext().getRuntimeClaim(claimUri);
        if (runtimeClaimValue != null) {
            return runtimeClaimValue;
        }
        if (isFederatedIdP()) {
            return getFederatedClaim(claimUri);
        }
        return getLocalClaim(claimUri);
    }

    private boolean hasRuntimeClaim(String claimUri) {

        String claim = getContext().getRuntimeClaim(claimUri);
        if (claim != null) {
            return true;
        }
        if (isFederatedIdP()) {
            return hasFederatedClaim(claimUri);
        }
        return hasLocalClaim(claimUri);
    }

    private void setRuntimeClaim(String claimUri, String claimValue) {

        String claimValueAsString = String.valueOf(claimValue);
        if (claimValueAsString == null) {
            claimValueAsString = StringUtils.EMPTY;
        }
        getContext().addRuntimeClaim(claimUri, claimValueAsString);
    }
}
