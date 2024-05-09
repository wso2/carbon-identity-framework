/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.endpoint.util;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * This class tests the methods of IdentityManagementServiceUtil class.
 */
@Listeners(MockitoTestNGListener.class)
public class IdentityManagementServiceUtilTest {

    @Mock
    ServiceURL serviceURL;
    @Mock
    private ServiceURLBuilder serviceURLBuilder;
    private final IdentityManagementServiceUtil util = IdentityManagementServiceUtil.getInstance();
    private static final String SERVICE_URL = "https://wso2.org:9443";

    /**
     * This test tests IdentityManagementServiceUtil init method return a valid service URL
     * when the service URL is not configured from the property file.
     *
     * @throws Exception Exception.
     */
    @Test
    public void testServiceURLFormat() throws Exception {

        try (MockedStatic<ServiceURLBuilder> serviceURLBuilder = mockStatic(ServiceURLBuilder.class)) {
            prepareServiceURLBuilder(serviceURLBuilder);
            prepareServiceURL();
            prepareIdentityManagementServiceUtil();

            String actualURL = util.getServiceContextURL();
            Assert.assertEquals(actualURL, SERVICE_URL);
        }
    }

    /**
     * Prepare service URL.
     */
    private void prepareServiceURL() {

        when(serviceURL.getAbsoluteInternalURL()).thenReturn(IdentityManagementServiceUtilTest.SERVICE_URL);
    }

    /**
     * Prepare IdentityManagementServiceUtil by initializing properties.
     */
    private void prepareIdentityManagementServiceUtil() {

        util.init();
    }

    /**
     * Prepare service URL builder.
     */
    private void prepareServiceURLBuilder(MockedStatic<ServiceURLBuilder> serviceURLBuilder) throws URLBuilderException {

        serviceURLBuilder.when(ServiceURLBuilder::create).thenReturn(this.serviceURLBuilder);
        when(this.serviceURLBuilder.build()).thenReturn(serviceURL);
    }
}
