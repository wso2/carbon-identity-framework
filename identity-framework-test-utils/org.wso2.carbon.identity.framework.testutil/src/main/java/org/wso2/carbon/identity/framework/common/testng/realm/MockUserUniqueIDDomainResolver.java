/*
 * Copyright (c) 2022. WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.framework.common.testng.realm;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.UserUniqueIDDomainResolver;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple mocked unique id domain resolver for testing.
 */
public class MockUserUniqueIDDomainResolver extends UserUniqueIDDomainResolver {

    private final Map<String, String> mapping = new HashMap<>();

    public MockUserUniqueIDDomainResolver(DataSource dataSource) {

        super(dataSource);
    }

    public String getDomainForUserId(String userId, int tenantId) throws UserStoreException {
        return mapping.get(userId);
    }

    public void setDomainForUserId(String userId, String userDomain, int tenantId) throws UserStoreException {
        mapping.put(userId, userDomain);
    }
}
