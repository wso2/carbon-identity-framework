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
package org.wso2.carbon.identity.entitlement.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.api.Collection;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.ResourceNotFoundException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *
 */
public class RegistryPolicyVersionManager implements PolicyVersionManagerModule {


    private static final Log log = LogFactory.getLog(RegistryPolicyVersionManager.class);

    private int maxVersions;


    @Override
    public void init(Properties properties) {
        try {
            maxVersions = Integer.parseInt(properties.getProperty("maxVersions"));
        } catch (Exception e) {
            // ignore
        }
        if (maxVersions == 0) {
            maxVersions = PDPConstants.DEFAULT_MAX_POLICY_VERSION;
        }
    }


    @Override
    public PolicyDTO getPolicy(String policyId, String version) throws EntitlementException {

        // Zero means current version
        if (version == null || version.trim().isEmpty()) {
            Registry registry = EntitlementServiceComponent.getGovernanceRegistry(
                    CarbonContext.getThreadLocalCarbonContext().getTenantId());
            try {
                assert registry != null;
                Collection collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
                if (collection != null) {
                    version = collection.getProperty("version");
                }
            } catch (RegistryException e) {
                log.error(e);
                throw new EntitlementException("Invalid policy version");
            }
        }

        PAPPolicyStoreModule policyStore = new RegistryPAPPolicyStore();
        PolicyDTO dto = policyStore.getPolicy(version, policyId);

        if (dto == null) {
            throw new EntitlementException("Invalid policy version");
        }

        return dto;
    }


    @Override
    public String[] getVersions(String policyId) {

        List<String> versions = new ArrayList<>();
        Registry registry = EntitlementServiceComponent.getGovernanceRegistry(
                CarbonContext.getThreadLocalCarbonContext().getTenantId());
        Collection collection = null;
        try {
            try {
                assert registry != null;
                collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
            } catch (ResourceNotFoundException e) {
                // ignore
            }
            if (collection != null && collection.getChildren() != null) {
                String[] children = collection.getChildren();
                for (String child : children) {
                    versions.add(RegistryUtils.getResourceName(child));
                }
            }
        } catch (RegistryException e) {
            log.error("Error while creating new version of policy", e);
        }
        return versions.toArray(new String[0]);
    }


    @Override
    public String createVersion(PolicyDTO policyDTO) throws EntitlementException {

        PAPPolicyStoreModule policyStore = new RegistryPAPPolicyStore();
        Registry registry = EntitlementServiceComponent.getGovernanceRegistry(
                CarbonContext.getThreadLocalCarbonContext().getTenantId());

        String version = "0";

        try {

            Collection collection = null;
            try {
                assert registry != null;
                collection = (Collection) registry.get(PDPConstants.ENTITLEMENT_POLICY_VERSION +
                        policyDTO.getPolicyId());
            } catch (ResourceNotFoundException e) {
                // ignore
            }

            if (collection != null) {
                version = collection.getProperty("version");
            } else {
                collection = registry.newCollection();
                collection.setProperty("version", "1");
                registry.put(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId(), collection);
            }

            int versionInt = Integer.parseInt(version);
            String policyPath = PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId() +
                    RegistryConstants.PATH_SEPARATOR;

            // check whether this is larger than max version
            if (versionInt > maxVersions) {
                // delete the older version
                int olderVersion = versionInt - maxVersions;
                if (registry.resourceExists(policyPath + olderVersion)) {
                    registry.delete(policyPath + olderVersion);
                }
            }

            //new version
            version = Integer.toString(versionInt + 1);

            // set version properties
            policyDTO.setVersion(version);

            // persist new version
            policyStore.addOrUpdatePolicy(policyDTO, false);

            // set new version
            collection.setProperty("version", version);
            registry.put(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyDTO.getPolicyId(), collection);
        } catch (RegistryException e) {
            log.error("Error while creating new version of policy", e);
        }
        return version;
    }


    @Override
    public void deletePolicy(String policyId) throws EntitlementException {

        Registry registry = EntitlementServiceComponent.getGovernanceRegistry(
                CarbonContext.getThreadLocalCarbonContext().getTenantId());
        try {
            assert registry != null;
            if (registry.resourceExists(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId)) {
                registry.delete(PDPConstants.ENTITLEMENT_POLICY_VERSION + policyId);
            }
        } catch (RegistryException e) {
            log.error("Error while deleting all versions of policy", e);
        }
    }

}
