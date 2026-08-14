/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.dao;

import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.SessionStorageSelector;
import org.wso2.carbon.identity.base.IdentityRuntimeException;

/**
 * Provides the {@link UserSessionDAO} of the configured session store.
 */
public final class UserSessionDAOFactory {

    private UserSessionDAOFactory() {

    }

    /**
     * Returns the user session DAO of the configured session store.
     * <p>
     * The relational {@link UserSessionDAOImpl} is used when no store is configured. A configured non-default
     * store must register its own DAO as an OSGi {@code UserSessionDAO} service.
     *
     * @return the user session DAO to use.
     */
    public static UserSessionDAO getUserSessionDAO() {

        String storeName = SessionStorageSelector.getConfiguredStoreName();
        if (SessionStorageSelector.isDefaultStoreConfigured(storeName)) {
            return new UserSessionDAOImpl();
        }

        UserSessionDAO userSessionDAO = FrameworkServiceDataHolder.getInstance().getUserSessionDAO(storeName);
        if (userSessionDAO == null) {
            throw IdentityRuntimeException.error("Configured user session DAO is not available: " + storeName
                    + ". Its bundle may not be installed or its service may not have been registered yet.");
        }
        return userSessionDAO;
    }
}
