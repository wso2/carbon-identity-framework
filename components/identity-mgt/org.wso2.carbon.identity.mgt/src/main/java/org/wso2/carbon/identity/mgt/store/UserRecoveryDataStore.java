/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.store;

import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.dto.UserRecoveryDataDO;

/**
 * TODO add java comments
 * @deprecated use org.wso2.carbon.identity.recovery.store.UserRecoveryDataStore instead.
 */
@Deprecated
public interface UserRecoveryDataStore {

    public static final String EXPIRE_TIME = "expireTime";
    public static final String SECRET_KEY = "secretKey";
    public static final String USER_ID = "userId";
    public static final String TENANT_ID = "tenantId";

    public void store(UserRecoveryDataDO recoveryDataDO) throws IdentityException;

    public void store(UserRecoveryDataDO[] recoveryDataDOs) throws IdentityException;

    public UserRecoveryDataDO load(String code) throws IdentityException;

    public UserRecoveryDataDO[] load(String userName, int tenantId)
            throws IdentityException;

    public void invalidate(UserRecoveryDataDO recoveryDataDO) throws IdentityException;

    public void invalidate(String userId, int tenantId) throws IdentityException;

    public void invalidate(String code) throws IdentityException;
}
