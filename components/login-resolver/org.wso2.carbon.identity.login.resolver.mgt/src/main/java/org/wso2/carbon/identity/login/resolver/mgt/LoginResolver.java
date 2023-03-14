/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.login.resolver.mgt;

import org.wso2.carbon.user.core.common.AuthenticationResult;

import java.util.List;

/**
 * This interface which is used as a blueprint to implement login resolver classes.
 */
public interface LoginResolver {

    /**
     * Resolves a user from the given login identifier and then returns the resolved claim URI and the user details if
     * a matching user exists.
     *
     * @param loginIdentifierValue The user login identifier input which is provided by the user during login.
     * @param allowedAttributes    The list of claim URIs which are allowed by admin for resolving the user.
     * @param tenantDomain         The tenant domain of the user.
     * @return A ResolvedUserResult object with the user details and the user resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifierValue, List<String> allowedAttributes, String tenantDomain);

    /**
     * Resolves a user from the given login identifier + login hint and then returns the resolved claim URI and the
     * user details if a matching user exists.
     *
     * @param loginIdentifierValue The user login identifier input which is provided by the user during login.
     * @param allowedAttributes    The list of claim URIs which are allowed by admin for resolving the user.
     * @param tenantDomain         The tenant domain of the user.
     * @param hint                 The login attribute claim hint.
     * @return ResolvedUserResult object with user details and resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifierValue, List<String> allowedAttributes, String tenantDomain,
                                   String hint);

    /**
     * Authenticates with the user login identifier. Resolves the user by matching the login identifier and the user
     * credentials.
     *
     * @param loginIdentifierValue The user login identifier input which is provided by the user during login.
     * @param allowedAttributes    The list of claim URIs which are allowed by admin for resolving the user.
     * @param credential           The credentials of the user.
     * @param tenantDomain         The tenant domain of the user.
     * @return An AuthenticationResult object with SUCCESS authentication status if the user credentials are correct
     * otherwise returns an AuthenticationResult with FAIL authentication status.
     */
    AuthenticationResult authenticateWithIdentifier(String loginIdentifierValue, List<String> allowedAttributes,
                                                    Object credential, String tenantDomain);
}
