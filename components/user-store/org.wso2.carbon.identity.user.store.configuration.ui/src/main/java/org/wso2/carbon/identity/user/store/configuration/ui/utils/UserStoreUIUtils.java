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
package org.wso2.carbon.identity.user.store.configuration.ui.utils;


import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Properties;
import org.wso2.carbon.identity.user.store.configuration.stub.api.Property;

import java.util.Map;


public class UserStoreUIUtils extends AbstractAdmin {
    /**
     * Merge the defined property values of an available user store with the required set of properties
     *
     * @param properties
     * @param tempProperties
     * @return
     */
    public static Properties mergePropertyValues(Properties properties, Map<String, String> tempProperties) {
        Property[] mandatories = properties.getMandatoryProperties();
        Property[] optionals = properties.getOptionalProperties();
        Property[] advancedProperties = properties.getAdvancedProperties();

        if (mandatories[0] != null) {
            for (int i = 0; i < mandatories.length; i++) {
                Property property = mandatories[i];
                if (tempProperties.get(property.getName()) != null) {
                    mandatories[i].setValue(tempProperties.get(property.getName()));
                }
            }
        }

        if (optionals[0] != null) {
            for (int i = 0; i < optionals.length; i++) {
                Property property = optionals[i];
                if (tempProperties.get(property.getName()) != null) {
                    optionals[i].setValue(tempProperties.get(property.getName()));
                }
            }
        }

        if (advancedProperties[0] != null) {
            for (int i = 0; i < advancedProperties.length; i++) {
                Property property = advancedProperties[i];
                if (tempProperties.get(property.getName()) != null) {
                    advancedProperties[i].setValue(tempProperties.get(property.getName()));
                }
            }
        }

        properties.setMandatoryProperties(mandatories);
        properties.setOptionalProperties(optionals);
        properties.setAdvancedProperties(advancedProperties);
        return properties;
    }

}
