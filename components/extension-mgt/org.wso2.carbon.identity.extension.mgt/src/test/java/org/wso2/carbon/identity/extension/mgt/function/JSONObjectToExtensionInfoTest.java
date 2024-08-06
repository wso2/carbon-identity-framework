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

package org.wso2.carbon.identity.extension.mgt.function;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.extension.mgt.TestUtils;
import org.wso2.carbon.identity.extension.mgt.model.ExtensionInfo;

/**
 * Unit tests for JSONObjectToExtensionInfo.
 */
@Test
public class JSONObjectToExtensionInfoTest {

    private final static String EXTENSION_RESOURCE_VERSION = "v1.0.0";
    private final static String APPLICATION_EXTENSIONS = "applications";
    private final static String APPLICATION_TEMPLATE_ID_1 = "template-1";
    private final static String APPLICATION_TEMPLATE_ID_2 = "template-2";

    @Test
    public void testGeneratedExtensionInfoObjectIncludesEmptyTemplateVersion() throws Exception {

        String info = TestUtils.readExtensionResourceInfo(APPLICATION_EXTENSIONS, APPLICATION_TEMPLATE_ID_1);
        ExtensionInfo extensionInfo = new JSONObjectToExtensionInfo().apply(new JSONObject(info));
        Assert.assertEquals(extensionInfo.getVersion(), StringUtils.EMPTY);
    }

    @Test
    public void testGeneratedExtensionInfoObjectIncludesTemplateVersion() throws Exception {

        String info = TestUtils.readExtensionResourceInfo(APPLICATION_EXTENSIONS, APPLICATION_TEMPLATE_ID_2);
        ExtensionInfo extensionInfo = new JSONObjectToExtensionInfo().apply(new JSONObject(info));
        Assert.assertEquals(extensionInfo.getVersion(), EXTENSION_RESOURCE_VERSION);
    }
}
