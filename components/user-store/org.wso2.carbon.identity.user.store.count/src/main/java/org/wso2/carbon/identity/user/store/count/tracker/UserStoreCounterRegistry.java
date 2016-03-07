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
package org.wso2.carbon.identity.user.store.count.tracker;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.tracker.UserStoreManagerRegistry;

import java.util.HashMap;
import java.util.Map;

public class UserStoreCounterRegistry extends UserStoreMgtDSComponent {
    private static Log log = LogFactory.getLog(UserStoreManagerRegistry.class);
    private static ServiceTracker userStoreMetricsTracker;
    private static Map<String, UserStoreCountRetriever> userStoreMetricServices = new HashMap<>();


    public static void init(BundleContext bc) throws Exception {
        try {
            userStoreMetricsTracker = new ServiceTracker(bc, UserStoreCountRetriever.class.getName(), null);
            userStoreMetricsTracker.open();
            if(log.isDebugEnabled()) {
            	log.debug(userStoreMetricsTracker.getServices().length + " User Store Metrics Services registered.");
            }
        } catch (Exception e) {
            log.error("Error" + e.getMessage(), e);
            throw e;
        }
    }


    /**
     * Get all the available user store metrics implementations
     *
     * @return Map<Class,<Map<Property,Value>>
     */
    private static Map<String, UserStoreCountRetriever> getUserStoreMetricsServices() {

        Object[] objects = userStoreMetricsTracker.getServices();
        for(Object object:objects){
            UserStoreCountRetriever userStoreCountRetriever = (UserStoreCountRetriever)object;
            userStoreMetricServices.put(userStoreCountRetriever.getClass().getName(), userStoreCountRetriever);
        }
        return userStoreMetricServices;
    }




}
