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

package org.wso2.carbon.identity.multi.attribute.login.mgt;

import org.wso2.carbon.user.core.common.AuthenticationResult;

import java.util.List;

/**
 * This interface is used to implement a MultiAttributeLoginService component, which provides multi attribute login
 * feature
 */
public interface MultiAttributeLoginService {

    /**
     * Checks whether multi attribute login enable or disable.
     *
     * @param tenantDomain User tenant domain.
     * @return True when enable multi attribute login in carbon console and false otherwise.
     */
    boolean isEnabled(String tenantDomain);

    /**
     * Authenticates with multi attribute login identifier.
     *
     * @param loginAttributeValue Multi attribute login identifier.
     * @param credential          User credential.
     * @param tenantDomain        User tenant domain.
     * @return AuthenticationResult with SUCCESS authentication status if user credentials match otherwise
     * returns AuthenticationResult with FAIL authentication status.
     */
    AuthenticationResult authenticateWithIdentifier(String loginAttributeValue, Object credential, String tenantDomain);

    /**
     * Resolves user from given login identifier and returns resolved user details.
     *
     * @param loginIdentifier Multi attribute login identifier.
     * @param tenantDomain    User tenant domain.
     * @return ResolvedUserResult object, which gives whether user input resolved or not and if resolve, gives user
     * object.
     */
    ResolvedUserResult resolveUser(String loginIdentifier, String tenantDomain);

    /**
     * Resolves user from given login identifier and returns resolved user details. The hint will reduce the searching
     * time of user name by providing first priority to provided hint claim.
     *
     * @param loginIdentifier Multi attribute login identifier.
     * @param tenantDomain    User tenant domain.
     * @param hint            Guess login identifier.
     * @return ResolvedUserResult object, which gives whether user input resolved or not and if resolve, gives user
     * object.
     */
    ResolvedUserResult resolveUser(String loginIdentifier, String tenantDomain, String hint);

    /**
     * Resolves possible users from given login identifier and returns resolved user details.
     *
     * @param loginIdentifierValue Multi attribute login identifier.
     * @param tenantDomain         User tenant domain.
     * @return List<ResolvedUserResult> object, which gives whether user input resolved or not and if resolve, gives
     * the list of user objects.
     */
    List<ResolvedUserResult> resolvePossibleUsers(String loginIdentifierValue, String tenantDomain);

}
