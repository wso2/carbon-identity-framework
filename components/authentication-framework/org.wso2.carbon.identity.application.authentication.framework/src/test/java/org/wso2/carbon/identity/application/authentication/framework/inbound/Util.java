/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.inbound;

import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.Map;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

public class Util {

    private Util() {
    }

    static void mockReturnNullEventListenerConfig() {
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.readEventListenerProperty(anyString(), anyString())).thenReturn(null);
    }

    static void mockReturnEventListenerConfigWithProperties(Properties expectedProperties) {
        IdentityEventListenerConfig identityEventListenerConfig =
                new IdentityEventListenerConfig("true", 1, null, expectedProperties);
        mockStatic(IdentityUtil.class);
        when(IdentityUtil.readEventListenerProperty(anyString(), anyString())).thenReturn(identityEventListenerConfig);
    }

    static Object[][] getEventListenerPropertyData() {
        Properties eventListenerDuplicateProperties = new Properties();
        eventListenerDuplicateProperties.put("key1", "value1");
        eventListenerDuplicateProperties.put("key1", "value1");
        eventListenerDuplicateProperties.put("key2", "value2");

        Properties eventListenerUniqueProperties = new Properties();
        eventListenerUniqueProperties.put("key1", "value1");
        eventListenerUniqueProperties.put("key2", "value2");

        return new Object[][]{
                // Event Listener Properties
                // Expected Properties after removing duplicates
                {
                        null,
                        new Properties(),
                }
                ,
                {
                        // TODO : need to check whether we can actually duplicate keys in a Properties object
                        eventListenerDuplicateProperties,
                        eventListenerUniqueProperties
                }
                ,
                {
                        eventListenerUniqueProperties,
                        eventListenerUniqueProperties
                }
        };
    }

    static void assertPropertiesEqual(Properties actualProperties,
                                      Properties expectedProperties) {
        if (actualProperties == null) {
            // creating an empty Properties object to compare in the next step.
            actualProperties = new Properties();
        }

        if (expectedProperties == null) {
            // creating an empty Properties object to compare in the next step.
            expectedProperties = new Properties();
        }

        assertEquals(actualProperties.size(), expectedProperties.size());

        for (Map.Entry<Object, Object> expectedEntry : expectedProperties.entrySet()) {
            assertEquals(actualProperties.get(expectedEntry.getKey()), expectedEntry.getValue());
        }
    }
}
