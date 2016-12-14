/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.entitlement.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.common.dto.PolicyEditorDataHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class PolicyEditorEngine {

    private static final Object lock = new Object();
    private static ConcurrentHashMap<String, PolicyEditorEngine> policyEditorEngine =
            new ConcurrentHashMap<String, PolicyEditorEngine>();
    private static Log log = LogFactory.getLog(PolicyEditorEngine.class);
    private int tenantId;
    private Map<String, PolicyEditorDataHolder> dataHolder = new HashMap<String, PolicyEditorDataHolder>();
    private DataPersistenceManager manager;

    public PolicyEditorEngine(int tenantId) {
        this.tenantId = tenantId;
        this.manager = new RegistryPersistenceManager();
        try {
            this.dataHolder = this.manager.buildDataHolder();
        } catch (PolicyEditorException e) {
            log.error("Error while building policy editor config", e);
        }
    }

    /**
     * Get a PolicyEditorEngine instance for that tenant. This method will return an
     * PolicyEditorEngine instance if exists, or creates a new one
     *
     * @return EntitlementEngine instance for that tenant
     */
    public static PolicyEditorEngine getInstance() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (!policyEditorEngine.containsKey(Integer.toString(tenantId))) {
            synchronized (lock) {
                if (!policyEditorEngine.containsKey(Integer.toString(tenantId))) {
                    policyEditorEngine.put(Integer.toString(tenantId), new PolicyEditorEngine(tenantId));
                }
            }
        }
        return policyEditorEngine.get(Integer.toString(tenantId));
    }

    public PolicyEditorDataHolder getPolicyEditorData(String policyEditorType) {

        if (dataHolder != null) {
            return dataHolder.get(policyEditorType);
        }
        return null;
    }

    public void persistConfig(String policyEditorType, String xmlConfig) throws PolicyEditorException {

        manager.persistConfig(policyEditorType, xmlConfig);
        dataHolder = manager.buildDataHolder();
    }

    public String getConfig(String policyEditorType) {

        Map<String, String> configs = manager.getConfig();
        if (configs != null) {
            return configs.get(policyEditorType);
        }
        return null;
    }
}
