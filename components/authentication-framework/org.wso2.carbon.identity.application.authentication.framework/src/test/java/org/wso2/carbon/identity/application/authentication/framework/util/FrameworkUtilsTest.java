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
package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.PostAuthnMissingClaimHandler;

import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@PrepareForTest({ConfigurationFacade.class})
public class FrameworkUtilsTest {

    @Mock
    ConfigurationFacade mockedConfigurationFacade;

    private PostAuthnMissingClaimHandler testPostAuthenticationHandler;

    @BeforeTest
    public void setUp() {
        testPostAuthenticationHandler = new PostAuthnMissingClaimHandler();
    }

    private void setMockedConfigurationFacade() {
        mockStatic(ConfigurationFacade.class);
        when(ConfigurationFacade.getInstance()).thenReturn(mockedConfigurationFacade);
    }

    @DataProvider(name = "providePostAuthenticationData")
    public Object[][] provideInvalidData() {

        Map<String, Object> map1 = new HashMap<>();
        map1.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, testPostAuthenticationHandler);

        Map<String, Object> map2 = new HashMap<>();
        map2.put(FrameworkConstants.Config.QNAME_EXT_POST_AUTHENTICATION_HANDLER, new Object());

        return new Object[][]{
                {map1, true},
                {map2, false}
        };
    }
}

