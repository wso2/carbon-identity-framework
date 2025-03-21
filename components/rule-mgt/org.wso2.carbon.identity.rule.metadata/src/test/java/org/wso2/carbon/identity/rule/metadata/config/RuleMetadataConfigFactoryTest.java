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

package org.wso2.carbon.identity.rule.metadata.config;

import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.rule.metadata.api.exception.RuleMetadataConfigException;
import org.wso2.carbon.identity.rule.metadata.internal.config.RuleMetadataConfigFactory;
import org.wso2.carbon.utils.CarbonUtils;

import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertNotNull;

public class RuleMetadataConfigFactoryTest {

    @Test
    public void testLoadConfigurations() throws RuleMetadataConfigException {

        try (MockedStatic<CarbonUtils> carbonUtilsMockedStatic = mockStatic(CarbonUtils.class)) {

            carbonUtilsMockedStatic.when(CarbonUtils::getCarbonHome)
                    .thenReturn(getClass().getClassLoader().getResource("configs").getFile());

            RuleMetadataConfigFactory.load();
            assertNotNull(RuleMetadataConfigFactory.getOperatorConfig());
            assertNotNull(RuleMetadataConfigFactory.getFieldDefinitionConfig());
            assertNotNull(RuleMetadataConfigFactory.getFlowConfig());
        }
    }

    @Test(expectedExceptions = RuleMetadataConfigException.class,
            expectedExceptionsMessageRegExp = "File not found at: .*")
    public void testLoadConfigurationsFileNotFound() throws RuleMetadataConfigException {

        try (MockedStatic<CarbonUtils> carbonUtilsMockedStatic = mockStatic(CarbonUtils.class)) {

            carbonUtilsMockedStatic.when(CarbonUtils::getCarbonHome).thenReturn("invalid/path");
            RuleMetadataConfigFactory.load();
        }
    }
}
