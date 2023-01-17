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

/**
 * This interface is used as a blueprint to implement custom login resolver services. An example would be the
 * MultiAttributeLoginResolverService.
 */
public interface LoginResolverService {

    /**
     * Checks whether the login resolver service is enabled or disabled.
     *
     * @param tenantDomain The tenant domain of the user.
     * @return True when the login resolver service is enabled in the management console, false otherwise.
     */
    boolean isEnabled(String tenantDomain);

    /**
     * Authenticates with provided login identifier.
     *
     * @param loginAttributeValue The login identifier.
     * @param credential          The credentials of the user.
     * @param tenantDomain        The tenant domain of the user.
     * @return An AuthenticationResult object with SUCCESS authentication status if the user credentials are correct
     * otherwise returns an AuthenticationResult with FAIL authentication status.
     */
    AuthenticationResult authenticateWithIdentifier(String loginAttributeValue, Object credential, String tenantDomain);

    /**
     * Resolves the user from the given login identifier and returns the resolved user details.
     *
     * @param loginIdentifier The login identifier.
     * @param tenantDomain    The tenant domain of the user.
     * @return A ResolvedUserResult object with the user details and the user resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifier, String tenantDomain);

    /**
     * Resolves the user from the given login identifier and returns the resolved user details. The hint reduces the
     * searching time of the user by providing the first priority to the provided hint claim.
     *
     * @param loginIdentifier The login identifier.
     * @param tenantDomain    The tenant domain of the user.
     * @param hint            The hint for the login identifier.
     * @return A ResolvedUserResult object with the user details and the user resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifier, String tenantDomain, String hint);
}
