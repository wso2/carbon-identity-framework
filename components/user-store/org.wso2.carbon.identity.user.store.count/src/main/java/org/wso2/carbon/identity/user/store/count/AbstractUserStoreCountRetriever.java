/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.count;

import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.Map;

public abstract class AbstractUserStoreCountRetriever implements UserStoreCountRetriever {

    public int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

    @Override
    public void init(RealmConfiguration realmConfiguration) throws UserStoreCounterException {
        return;
    }

    @Override
    public Long countUsers(String filter) throws UserStoreCounterException {
        return Long.valueOf(-1);
    }

    @Override
    public Long countRoles(String filter) throws UserStoreCounterException {
        return Long.valueOf(-1);
    }

    @Override
    public Long countClaim(String claimURI, String valueFilter) throws UserStoreCounterException {
        return Long.valueOf(-1);
    }

}
