/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.extension.mgt.utils;

import java.io.IOException;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.extension.mgt.TestUtils;
import org.wso2.carbon.user.core.UserCoreConstants;

import static org.mockito.Mockito.mockStatic;

/**
 * Unit tests for ExtensionMgtUtils.
 */
@Test
public class ExtensionMgtUtilsTest {

    private final static String CONNECTION_EXTENSIONS = "connections";
    private final static String CONNECTION_TEMPLATE_ID_1 = "template-1";
    private final static String UPDATED_PRIMARY_DOMAIN_NAME = "WSO2.ORG";

    @Test(description = "Verify the `resolveConnectionJITPrimaryDomainName` method when the primary domain name " +
            "remains unchanged.")
    public void testResolveConnectionJITPrimaryDomainNameMethodForDefaultPrimaryDomain() throws IOException {

        JSONObject sampleConnectionTemplate =
                new JSONObject(
                        TestUtils.readExtensionResourceTemplate(CONNECTION_EXTENSIONS, CONNECTION_TEMPLATE_ID_1));

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(IdentityUtil::getPrimaryDomainName)
                    .thenReturn(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);

            ExtensionMgtUtils.resolveConnectionJITPrimaryDomainName(sampleConnectionTemplate);
            String primaryDomainName = sampleConnectionTemplate.getJSONObject(ExtensionMgtConstants.IDP_CONFIG_KEY)
                    .getJSONObject(ExtensionMgtConstants.IDP_PROVISIONING_CONFIG_KEY)
                    .getJSONObject(ExtensionMgtConstants.IDP_PROVISIONING_JIT_CONFIG_KEY)
                    .getString(ExtensionMgtConstants.IDP_PROVISIONING_JIT_DOMAIN_NAME_KEY);
            Assert.assertEquals(primaryDomainName, UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME);
        }
    }

    @Test(description = "Verify the `resolveConnectionJITPrimaryDomainName` method when the primary domain name " +
            "is changed.")
    public void testResolveConnectionJITPrimaryDomainNameMethodForChangedPrimaryDomain() throws IOException {

        JSONObject sampleConnectionTemplate =
                new JSONObject(
                        TestUtils.readExtensionResourceTemplate(CONNECTION_EXTENSIONS, CONNECTION_TEMPLATE_ID_1));

        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(IdentityUtil::getPrimaryDomainName).thenReturn(UPDATED_PRIMARY_DOMAIN_NAME);

            ExtensionMgtUtils.resolveConnectionJITPrimaryDomainName(sampleConnectionTemplate);
            String primaryDomainName = sampleConnectionTemplate.getJSONObject(ExtensionMgtConstants.IDP_CONFIG_KEY)
                    .getJSONObject(ExtensionMgtConstants.IDP_PROVISIONING_CONFIG_KEY)
                    .getJSONObject(ExtensionMgtConstants.IDP_PROVISIONING_JIT_CONFIG_KEY)
                    .getString(ExtensionMgtConstants.IDP_PROVISIONING_JIT_DOMAIN_NAME_KEY);
            Assert.assertEquals(primaryDomainName, UPDATED_PRIMARY_DOMAIN_NAME);
        }
    }
}
