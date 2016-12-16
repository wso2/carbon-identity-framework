/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.gateway.element.authentication.context;


import org.wso2.carbon.identity.framework.FrameworkRuntimeException;
import org.wso2.carbon.identity.framework.model.UserClaim;
import org.wso2.carbon.identity.gateway.element.authentication.Authenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Context to capture the current authentication status. This provides information about previously authenticated
 * IDPs, their status and other data such as claims gathered.
 */
public class AuthenticationContext {

    // List authenticated IDPs
    private List<Authenticator> authenticatedIDPs = new ArrayList<>();

    public List<Authenticator> getAuthenticatedIDPs() {

        return authenticatedIDPs;
    }

    public String getAuthenticateSubjectIdentifier() {

        return Optional.ofNullable(authenticatedIDPs.parallelStream()
                .filter(x -> x.isAuthenticated() && x.isSubjectStep())
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("No IDP found with Subject step set."))
                .getAuthenticatedUser())
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find the authenticated user info in authenticated IDP"))
                .getUserIdentifier();
    }


    public Map<String, UserClaim> getAuthenticatedUserClaims() {

        return Optional.ofNullable(authenticatedIDPs.parallelStream()
                .filter(x -> x.isAuthenticated() && x.isAttributeStep())
                .findFirst()
                .orElseThrow(() -> new FrameworkRuntimeException("No IDP found with attribute step set."))
                .getAuthenticatedUser())
                .orElseThrow(() -> new FrameworkRuntimeException("Cannot find the authenticated user info in " +
                        "authenticated IDP"))
                .getUserClaims();
    }

}



