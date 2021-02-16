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

package org.wso2.carbon.identity.application.authentication.framework.store;


import org.testng.annotations.DataProvider;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import redis.clients.jedis.exceptions.JedisException;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

public class RedisSessionDataStoreTest {

    @DataProvider
    public Object[][] getValidSessionContextDO(){
        return new Object[][]{
                {"11111","type","entry","nanotime"},
        };
    }

    @Test
    public void testRemoveSessionData(String key, String type, long nanoTime) {
        SessionDataStore.getInstance().removeSessionData(key, type, nanoTime);
    }

    @Test(dataProvider = "getValidSessionContextDO", dependsOnMethods = {"testPersistSessionData"}, expectedExceptions =
            JedisException.class)
    public SessionContextDO testGetSessionContextData(String key, String type){
        return SessionDataStore.getInstance().getSessionContextData(key,type);

    }

    @Test
    public void testPersistSessionData(String key, String type, Object entry, long nanoTime, int tenantId) {
        SessionDataStore.getInstance().persistSessionData(key, type, entry, nanoTime, tenantId);
    }

}
