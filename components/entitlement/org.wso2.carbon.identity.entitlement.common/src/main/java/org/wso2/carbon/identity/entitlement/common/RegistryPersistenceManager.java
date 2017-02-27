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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class RegistryPersistenceManager extends InMemoryPersistenceManager {

    @Override
    public void persistConfig(String policyEditorType, String xmlConfig) throws PolicyEditorException {

        super.persistConfig(policyEditorType, xmlConfig);

        Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
        try {
            Resource resource = registry.newResource();
            resource.setContent(xmlConfig);
            String path = null;
            if (EntitlementConstants.PolicyEditor.BASIC.equals(policyEditorType)) {
                path = EntitlementConstants.ENTITLEMENT_POLICY_BASIC_EDITOR_CONFIG_FILE_REGISTRY_PATH;
            } else if (EntitlementConstants.PolicyEditor.STANDARD.equals(policyEditorType)) {
                path = EntitlementConstants.ENTITLEMENT_POLICY_STANDARD_EDITOR_CONFIG_FILE_REGISTRY_PATH;
            } else if (EntitlementConstants.PolicyEditor.RBAC.equals(policyEditorType)) {
                path = EntitlementConstants.ENTITLEMENT_POLICY_RBAC_EDITOR_CONFIG_FILE_REGISTRY_PATH;
            } else if (EntitlementConstants.PolicyEditor.SET.equals(policyEditorType)) {
                path = EntitlementConstants.ENTITLEMENT_POLICY_SET_EDITOR_CONFIG_FILE_REGISTRY_PATH;
            } else {
                //default
                path = EntitlementConstants.ENTITLEMENT_POLICY_BASIC_EDITOR_CONFIG_FILE_REGISTRY_PATH;
            }
            registry.put(path, resource);
        } catch (RegistryException e) {
            throw new PolicyEditorException("Error while persisting policy editor config");
        }
    }

    @Override
    public Map<String, String> getConfig() {
        Map<String, String> config = super.getConfig();
        if (config == null || config.size() == 0) {
            config = new HashMap<String, String>();
            Registry registry = CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            String configString = null;
            try {
                Resource resource = registry.
                        get(EntitlementConstants.ENTITLEMENT_POLICY_BASIC_EDITOR_CONFIG_FILE_REGISTRY_PATH);
                if (resource != null && resource.getContent() != null) {
                    configString = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));

                }
            } catch (Exception e) {
                //ignore
            }

            if (configString == null) {
                configString = getDefaultBasicConfig();
            }
            config.put(EntitlementConstants.PolicyEditor.BASIC, configString);

            configString = null;
            try {
                Resource resource = registry.
                        get(EntitlementConstants.ENTITLEMENT_POLICY_STANDARD_EDITOR_CONFIG_FILE_REGISTRY_PATH);
                if (resource != null && resource.getContent() != null) {
                    configString = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
                    config.put(EntitlementConstants.PolicyEditor.STANDARD, configString);
                }
            } catch (Exception e) {
                //ignore
            }
            if (configString == null) {
                configString = getDefaultConfig();
            }
            config.put(EntitlementConstants.PolicyEditor.STANDARD, configString);

            configString = null;
            try {
                Resource resource = registry.
                        get(EntitlementConstants.ENTITLEMENT_POLICY_RBAC_EDITOR_CONFIG_FILE_REGISTRY_PATH);
                if (resource != null && resource.getContent() != null) {
                    configString = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
                    config.put(EntitlementConstants.PolicyEditor.RBAC, configString);
                }
            } catch (Exception e) {
                //ignore
            }
            if (configString == null) {
                configString = getSimpleConfig();
            }
            config.put(EntitlementConstants.PolicyEditor.RBAC, configString);

            configString = null;
            try {
                Resource resource = registry.
                        get(EntitlementConstants.ENTITLEMENT_POLICY_SET_EDITOR_CONFIG_FILE_REGISTRY_PATH);
                if (resource != null && resource.getContent() != null) {
                    configString = new String((byte[]) resource.getContent(), Charset.forName("UTF-8"));
                    config.put(EntitlementConstants.PolicyEditor.SET, configString);
                }
            } catch (Exception e) {
                //ignore
            }
            if (configString == null) {
                configString = getDefaultSetConfig();
            }
            config.put(EntitlementConstants.PolicyEditor.SET, configString);
        }
        return config;
    }
}
