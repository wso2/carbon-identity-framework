/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;

/**
 * ImpersonatedUser is the class that represents the impersonated subject.
 */
public class ImpersonatedUser extends AuthenticatedUser {

    public ImpersonatedUser() {

        super();
    }

    public ImpersonatedUser(AuthenticatedUser authenticatedUser) throws UserIdNotFoundException {

        super(authenticatedUser);
    }

    /**
     * As the ImpersonatedUser cannot have an impersonated user this method should not be implemented.
     *
     * @param impersonatedUser the impersonated user to set.
     */
    public void setImpersonatedUser(ImpersonatedUser impersonatedUser) {

        throw new UnsupportedOperationException("ImpersonatedUser cannot have another impersonated user.");
    }

    /**
     * As the ImpersonatedUser cannot have an impersonated user this method will always return null.
     *
     * @return the impersonated user, which always be null for ImpersonatedUser class.
     */
    public ImpersonatedUser getImpersonatedUser() {

        return null;
    }
}
