/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.model.test;

import org.junit.Test;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ProvisioningConnectorConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

/**
 * Testing the ProvisioningConnectorConfig class
 */
public class ProvisioningConnectorConfigTest {

    @Test
    public void shouldGenerateDifferentHashCodesForDifferentNames() {
        ProvisioningConnectorConfig config1 = new ProvisioningConnectorConfig();
        config1.setName("Name1");
        config1.setProvisioningProperties(new Property[0]);

        ProvisioningConnectorConfig config2 = new ProvisioningConnectorConfig();
        config2.setName("Name2");
        config2.setProvisioningProperties(new Property[0]);

        assertNotEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    public void shouldReturnFalseByEqualsForDifferentNames() {
        ProvisioningConnectorConfig config1 = new ProvisioningConnectorConfig();
        config1.setName("Name1");
        config1.setProvisioningProperties(new Property[0]);

        ProvisioningConnectorConfig config2 = new ProvisioningConnectorConfig();
        config2.setName("Name2");
        config2.setProvisioningProperties(new Property[0]);

        assertFalse(config1.equals(config2));
    }
}
