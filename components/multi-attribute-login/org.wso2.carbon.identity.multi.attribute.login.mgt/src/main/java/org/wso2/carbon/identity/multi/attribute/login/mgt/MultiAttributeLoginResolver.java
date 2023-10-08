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
 * This interface is used to implement a user name resolver class.
 */
public interface MultiAttributeLoginResolver {

    /**
     * Resolves user from given login identifier then returns resolved claim URI and user details if found a
     * matching user.
     *
     * @param loginIdentifierValue User input when user try to login using multi attribute login.
     * @param allowedAttributes    List of claim URIs which are allowed by admin for multi attribute login.
     * @param tenantDomain         User tenant domain.
     * @return ResolvedUserResult object with user details and resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifierValue, List<String> allowedAttributes, String tenantDomain);

    /**
     * Resolves user from given login identifier and login attribute hint, then returns resolved claim URI and user
     * details if found a matching user.
     *
     * @param loginIdentifierValue User input when user try to login using multi attribute login.
     * @param allowedAttributes    List of claim URIs which are allowed by admin for multi attribute login.
     * @param tenantDomain         User tenant domain.
     * @param hint                 The login attribute claim hint.
     * @return ResolvedUserResult object with user details and resolved claim details.
     */
    ResolvedUserResult resolveUser(String loginIdentifierValue, List<String> allowedAttributes, String tenantDomain,
                                   String hint);

    /**
     * Authenticates with multi attribute login identifier. User resolves by matching multi attribute login
     * identifier and user credential.
     *
     * @param loginIdentifierValue User entered multi attribute login identifier value.
     * @param allowedAttributes    List of claim URIs which are allowed by admin for multi attribute login.
     * @param credential           User credential.
     * @param tenantDomain         User tenant domain.
     * @return AuthenticationResult object.
     */
    AuthenticationResult authenticateWithIdentifier(String loginIdentifierValue, List<String> allowedAttributes,
                                                    Object credential, String tenantDomain);

    /**
     * Resolves possible users from given login identifier then returns resolved claim URI and user details if found a
     * matching users.
     *
     * @param loginAttribute    User input when user try to login using multi attribute login.
     * @param allowedAttributes List of claim URIs which are allowed by admin for multi attribute login.
     * @param tenantDomain      User tenant domain.
     * @return List<ResolvedUserResult> object with possible user details and resolved claim details.
     */
    List<ResolvedUserResult> resolvePossibleUsers(String loginAttribute, List<String> allowedAttributes,
                                                  String tenantDomain);

}
