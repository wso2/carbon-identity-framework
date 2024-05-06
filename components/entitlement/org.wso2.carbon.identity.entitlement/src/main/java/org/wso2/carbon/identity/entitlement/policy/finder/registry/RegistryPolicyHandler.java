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

package org.wso2.carbon.identity.entitlement.policy.finder.registry;

import org.wso2.carbon.identity.entitlement.PDPConstants;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Properties;

/**
 *  Registry policy handler
 */
public class RegistryPolicyHandler extends Handler {

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        super.put(requestContext);
        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
        boolean enableRegistryCacheClear = true ;
        if(properties.getProperty(PDPConstants.PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR)!=null){
            enableRegistryCacheClear = Boolean.parseBoolean(properties.getProperty(PDPConstants.PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR));
        }
        if(enableRegistryCacheClear) {
            RegistryPolicyDAOImpl.invalidateCache();
        }


    }

    @Override
    public void delete(RequestContext requestContext) throws RegistryException {

        super.delete(requestContext);
        Properties properties = EntitlementServiceComponent.getEntitlementConfig().getEngineProperties();
        boolean enableRegistryCacheClear = true ;
        if(properties.getProperty(PDPConstants.PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR)!=null){
            enableRegistryCacheClear = Boolean.parseBoolean(properties.getProperty(PDPConstants.PDP_REGISTRY_LEVEL_POLICY_CACHE_CLEAR));
        }
        if(enableRegistryCacheClear) {
            RegistryPolicyDAOImpl.invalidateCache();
        }

    }



}
