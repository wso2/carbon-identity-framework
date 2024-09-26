/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.entitlement.persistence;

import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.WithRegistry;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.internal.EntitlementConfigHolder;

/**
 * This class tests the behavior of the JDBC Simple PAP Status Data Handler class.
 */
@WithCarbonHome
@WithRegistry
@WithRealmService(injectToSingletons = {EntitlementConfigHolder.class}, initUserStoreManager = true)
@WithH2Database(files = {"dbscripts/h2.sql"})
public class JDBCSimplePAPStatusDataHandlerTest extends PAPStatusDataHandlerTest {

    public PAPStatusDataHandler createPAPStatusDataHandler() {

        return new JDBCSimplePAPStatusDataHandler();
    }

    public SubscriberPersistenceManager createSubscriberPersistenceManager() {

        return new JDBCSubscriberPersistenceManager();
    }
}
