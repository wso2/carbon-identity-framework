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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.entitlement.PAPStatusDataHandler;
import org.wso2.carbon.identity.entitlement.SimplePAPStatusDataHandler;

import static org.wso2.carbon.identity.entitlement.PDPConstants.POLICY_STORAGE_CONFIG;

public class PersistenceManagerFactory {

    private static final Log LOG = LogFactory.getLog(PersistenceManagerFactory.class);

    private static final String POLICY_STORAGE_TYPE = IdentityUtil.getProperty(POLICY_STORAGE_CONFIG);
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";

    private PersistenceManagerFactory() {

    }

    public static PolicyPersistenceManager getPolicyPersistenceManager() {

        PolicyPersistenceManager defaultPolicyPersistenceManager = new JDBCPolicyPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    LOG.info("Hybrid XACML policy persistent manager initialized.");
                    return new HybridPolicyPersistenceManager();
                case REGISTRY:
                    LOG.warn("Registry based XACML policy persistent manager initialized.");
                    return new RegistryPolicyPersistenceManager();
                default:
                    return defaultPolicyPersistenceManager;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("XACML policy persistent manager initialized with the type: " +
                    defaultPolicyPersistenceManager.getClass());
        }
        return defaultPolicyPersistenceManager;
    }

    public static ConfigPersistenceManager getConfigPersistenceManager() {

        ConfigPersistenceManager defaultConfigPersistenceManager = new JDBCConfigPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    LOG.info("Hybrid XACML config persistent manager initialized.");
                    return new HybridConfigPersistenceManager();
                case REGISTRY:
                    LOG.warn("Registry based XACML config persistent manager initialized.");
                    return new RegistryConfigPersistenceManager();
                default:
                    return defaultConfigPersistenceManager;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("XACML config persistent manager initialized with the type: " +
                    defaultConfigPersistenceManager.getClass());
        }
        return defaultConfigPersistenceManager;
    }

    public static SubscriberPersistenceManager getSubscriberPersistenceManager() {

        SubscriberPersistenceManager defaultSubscriberPersistenceManager = new JDBCSubscriberPersistenceManager();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    LOG.info("Hybrid XACML subscriber persistent manager initialized.");
                    return new HybridSubscriberPersistenceManager();
                case REGISTRY:
                    LOG.warn("Registry based XACML subscriber persistent manager initialized.");
                    return new RegistrySubscriberPersistenceManager();
                default:
                    return defaultSubscriberPersistenceManager;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("XACML subscriber persistent manager initialized with the type: " +
                    defaultSubscriberPersistenceManager.getClass());
        }
        return defaultSubscriberPersistenceManager;
    }

    public static PAPStatusDataHandler getPAPStatusDataHandler() {

        PAPStatusDataHandler defaultPAPStatusDataHandler = new JDBCSimplePAPStatusDataHandler();
        if (StringUtils.isNotBlank(POLICY_STORAGE_TYPE)) {
            switch (POLICY_STORAGE_TYPE) {
                case HYBRID:
                    LOG.info("Hybrid XACML PAP status persistent manager initialized.");
                    return new HybridPAPStatusDataHandler();
                case REGISTRY:
                    LOG.warn("Registry based XACML PAP status persistent manager initialized.");
                    return new SimplePAPStatusDataHandler();
                default:
                    return defaultPAPStatusDataHandler;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("XACML PAP status persistent manager initialized with the type: " +
                    defaultPAPStatusDataHandler.getClass());
        }
        return defaultPAPStatusDataHandler;
    }
}

