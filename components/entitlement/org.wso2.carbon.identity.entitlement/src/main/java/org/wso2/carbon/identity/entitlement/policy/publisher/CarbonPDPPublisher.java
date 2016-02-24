/*
*  Copyright (c)  WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.entitlement.policy.publisher;

import org.wso2.carbon.identity.entitlement.EntitlementException;
import org.wso2.carbon.identity.entitlement.common.EntitlementConstants;
import org.wso2.carbon.identity.entitlement.dto.PolicyDTO;
import org.wso2.carbon.identity.entitlement.pap.EntitlementAdminEngine;
import org.wso2.carbon.identity.entitlement.policy.store.PolicyStoreManager;

import java.util.Properties;

/**
 *
 */
public class CarbonPDPPublisher implements PolicyPublisherModule {

    @Override
    public void init(Properties properties) {

    }

    @Override
    public Properties loadProperties() {
        return new Properties();
    }

    @Override
    public String getModuleName() {
        return "PDP Publisher";
    }

    @Override
    public void publish(PolicyDTO policyDTO, String action, boolean enabled, int order) throws EntitlementException {

        PolicyStoreManager manager = EntitlementAdminEngine.getInstance().getPolicyStoreManager();

        if (EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(action)) {
            policyDTO.setPolicyOrder(order);
            policyDTO.setActive(enabled);
            manager.addPolicy(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(action)) {
            manager.removePolicy(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(action)) {
            manager.updatePolicy(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(action)) {
            policyDTO.setActive(true);
            manager.enableDisablePolicy(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(action)) {
            policyDTO.setActive(false);
            manager.enableDisablePolicy(policyDTO);
        } else if (EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(action)) {
            policyDTO.setPolicyOrder(order);
            manager.orderPolicy(policyDTO);
        }
    }
}
