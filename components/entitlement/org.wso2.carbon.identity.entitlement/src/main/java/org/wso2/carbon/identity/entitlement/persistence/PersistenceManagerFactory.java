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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;

import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_STORAGE_CONFIG;

public class PersistenceManagerFactory {

    private static String POLICY_STORAGE_TYPE = IdentityUtil.getProperty(POLICY_STORAGE_CONFIG);
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";

    private PersistenceManagerFactory() {

    }

    public static PolicyPersistenceManager getPolicyPersistenceManager() {

        PolicyPersistenceManager defaultPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    return new HybridPolicyPersistenceManager();
                case REGISTRY:
                    return new RegistryPolicyPersistenceManager();
                default:
                    return defaultPolicyPersistenceManager;
            }
        }
        return defaultPolicyPersistenceManager;
    }

    public static ConfigPersistenceManager getConfigPersistenceManager() {

        ConfigPersistenceManager defaultConfigPersistenceManager = new JDBCConfigPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    return new HybridConfigPersistenceManager();
                case REGISTRY:
                    return new RegistryConfigPersistenceManager();
                default:
                    return defaultConfigPersistenceManager;
            }
        }
        return defaultConfigPersistenceManager;
    }

    public static SubscriberPersistenceManager getSubscriberPersistenceManager() {

        SubscriberPersistenceManager defaultSubscriberPersistenceManager = new JDBCSubscriberPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    return new HybridSubscriberPersistenceManager();
                case REGISTRY:
                    return new RegistrySubscriberPersistenceManager();
                default:
                    return defaultSubscriberPersistenceManager;
            }
        }
        return defaultSubscriberPersistenceManager;
    }

    public static PAPStatusDataHandler getPAPStatusDataHandler() {

        PAPStatusDataHandler defaultPAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    return new HybridPAPStatusDataHandler();
                case REGISTRY:
                    return new SimplePAPStatusDataHandler();
                default:
                    return defaultPAPStatusDataHandler;
            }
        }
        return defaultPAPStatusDataHandler;
    }
}

