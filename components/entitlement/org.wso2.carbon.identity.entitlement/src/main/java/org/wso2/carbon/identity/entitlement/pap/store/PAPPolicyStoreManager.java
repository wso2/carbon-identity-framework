/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.entitlement.pap.store;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.dao.PolicyDAO;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.registry.core.Resource;

public class PAPPolicyStoreManager {

    private final PolicyDAO store;

    private final PAPPolicyStoreReader storeReader;


    public PAPPolicyStoreManager() {
        store = new RegistryPolicyDAOImpl();
        storeReader = new PAPPolicyStoreReader(store);
    }


    public void addOrUpdatePolicy(PolicyDTO policy) throws EntitlementException {
        store.addOrUpdatePolicy(policy);
    }


    public void removePolicy(String policyId) throws EntitlementException {
        store.removePolicy(policyId);
    }


    public String[] getPolicyIds() throws EntitlementException {
        return store.listPolicyIds().toArray(new String[0]);
    }


    public PolicyDTO getPolicy(String policyId) throws EntitlementException {
        return storeReader.readPolicyDTO(policyId);
    }


    public boolean isExistPolicy(String policyId) {
        return storeReader.isExistPolicy(policyId);
    }


    public PolicyDTO getLightPolicy(String policyId) throws EntitlementException {
        return storeReader.readLightPolicyDTO(policyId);
    }


    public PolicyDTO getMetaDataPolicy(String policyId) throws EntitlementException {
        return storeReader.readMetaDataPolicyDTO(policyId);
    }


    public PolicyDTO getPolicy(Resource resource) throws EntitlementException {
        return storeReader.readPolicyDTO(resource);
    }


    public PolicyDTO[] getAllLightPolicyDTOs() throws EntitlementException {
        return storeReader.readAllLightPolicyDTOs();
    }
}
