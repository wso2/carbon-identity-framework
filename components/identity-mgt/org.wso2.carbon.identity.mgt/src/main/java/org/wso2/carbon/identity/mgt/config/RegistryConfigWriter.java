/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.mgt.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.IdentityMgtConfigException;
import org.wso2.carbon.identity.mgt.internal.IdentityMgtServiceComponent;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class RegistryConfigWriter implements ConfigWriter {

    private static final Log log = LogFactory.getLog(RegistryConfigWriter.class);
            
    @Override
    public void write(int tenantId, Properties props, String resourcePath)
            throws IdentityMgtConfigException {

        if(log.isDebugEnabled()) {
            log.debug("Writing data to registry path : " + resourcePath);
        }
        
        RegistryService registry = IdentityMgtServiceComponent.getRegistryService();
        try {
            UserRegistry userReg = registry.getConfigSystemRegistry(tenantId);
            Resource resource = userReg.newResource();
            Set<String> names = props.stringPropertyNames();
            // Only key value pairs exists and no multiple values exists a key.
            for (String keyName : names) {
                List<String> value = new ArrayList<>();
                String valueStr = props.getProperty(keyName);
                
                if(log.isDebugEnabled()) {
                    log.debug("Write key : " + keyName + " value : " + value);
                }
                
                // This is done due to casting to List in JDBCRegistryDao
                value.add(valueStr);
                resource.setProperty(keyName, value);
            }
            userReg.put(resourcePath, resource);

        } catch (RegistryException e) {
            throw new IdentityMgtConfigException(
                    "Error occurred while writing data to registry path : " + resourcePath, e);
        }

    }

}
