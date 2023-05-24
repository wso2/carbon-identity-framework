/*
 * Copyright (c) 2023, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.ServiceURL;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * This class tests the methods of IdentityManagementServiceUtil class.
 */
@PrepareForTest({ServiceURLBuilder.class})
@PowerMockIgnore("org.mockito.*")
public class IdentityManagementServiceUtilTest extends PowerMockTestCase {

    @Mock
    ServiceURL serviceURL;
    @Mock
    private ServiceURLBuilder serviceURLBuilder;
    private IdentityManagementServiceUtil util = IdentityManagementServiceUtil.getInstance();
    private static final String SERVICE_URL = "https://wso2.org:9443";

    /**
     * This test tests IdentityManagementServiceUtil init method return a valid service URL.
     *
     * @throws Exception Exception.
     */
    @Test
    public void testServiceURLFormat() throws Exception {

        prepareServiceURLBuilder();
        prepareServiceURL();
        prepareIdentityManagementServiceUtil();

        String actualURL = util.getServiceContextURL();
        Assert.assertEquals(actualURL, SERVICE_URL);
    }

    /**
     * Prepare service URL.
     */
    private void prepareServiceURL() {

        when(serviceURL.getAbsoluteInternalURL()).thenReturn(IdentityManagementServiceUtilTest.SERVICE_URL);
    }

    /**
     * Prepare IdentityManagementServiceUtil.
     */
    private void prepareIdentityManagementServiceUtil() {

        util = IdentityManagementServiceUtil.getInstance();
        util.init();
    }

    /**
     * Prepare service URL builder.
     */
    private void prepareServiceURLBuilder() throws URLBuilderException {

        mockStatic(ServiceURLBuilder.class);
        when(ServiceURLBuilder.create()).thenReturn(serviceURLBuilder);
        when(serviceURLBuilder.addPath(any())).thenReturn(serviceURLBuilder);
        when(serviceURLBuilder.addFragmentParameter(any(), any())).thenReturn(serviceURLBuilder);
        when(serviceURLBuilder.addParameter(any(), any())).thenReturn(serviceURLBuilder);
        when(serviceURLBuilder.build()).thenReturn(serviceURL);
    }
}
