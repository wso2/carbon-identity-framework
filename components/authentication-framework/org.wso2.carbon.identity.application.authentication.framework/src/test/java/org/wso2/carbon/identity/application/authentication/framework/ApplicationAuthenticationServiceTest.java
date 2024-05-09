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

package org.wso2.carbon.identity.application.authentication.framework;

import org.mockito.Mock;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.exception.ApplicationAuthenticationException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class ApplicationAuthenticationServiceTest {

    private static final String REQUESTPATH_AUTHENTICATOR_NAME = "RequestPathAuthenticator";
    private static final String LOCAL_AUTHENTICATOR_NAME = "LocalAuthenticator";
    private static final String FEDERATED_AUTHENTICATOR_NAME = "FederatedAuthenticator";
    private static final String NON_EXISTING_AUTHENTICATOR_NAME = "NonExistingAuthenticator";


    @Mock
    private RequestPathApplicationAuthenticator requestPathApplicationAuthenticator;
    @Mock
    private FederatedApplicationAuthenticator federatedApplicationAuthenticator;
    @Mock
    private LocalApplicationAuthenticator localApplicationAuthenticator;

    private ApplicationAuthenticationService applicationAuthenticationService;


    @BeforeTest
    public void setUp() throws Exception {
        initMocks(this);
        initAuthenticators();
        applicationAuthenticationService = new ApplicationAuthenticationService();
    }

    private void initAuthenticators() {

        // Mock the authenticators
        when(requestPathApplicationAuthenticator.getName()).thenReturn(REQUESTPATH_AUTHENTICATOR_NAME);
        when(federatedApplicationAuthenticator.getName()).thenReturn(FEDERATED_AUTHENTICATOR_NAME);
        when(localApplicationAuthenticator.getName()).thenReturn(LOCAL_AUTHENTICATOR_NAME);

        FrameworkServiceComponent.getAuthenticators().add(requestPathApplicationAuthenticator);
        FrameworkServiceComponent.getAuthenticators().add(federatedApplicationAuthenticator);
        FrameworkServiceComponent.getAuthenticators().add(localApplicationAuthenticator);
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test(expectedExceptions = ApplicationAuthenticationException.class)
    public void testGetAuthenticatorWithNullName() throws Exception {

        applicationAuthenticationService.getAuthenticator(null);
    }


    @Test
    public void testGetAuthenticatorWithNonExistingName() throws Exception {

        assertNull(applicationAuthenticationService.getAuthenticator(NON_EXISTING_AUTHENTICATOR_NAME));
    }


    @Test
    public void testGetAuthenticator() throws Exception {

        ApplicationAuthenticator applicationAuthenticator = applicationAuthenticationService.getAuthenticator
                (REQUESTPATH_AUTHENTICATOR_NAME);
        assertNotNull(applicationAuthenticator);
        assertEquals(applicationAuthenticator.getName(), REQUESTPATH_AUTHENTICATOR_NAME);
    }

    @Test
    public void testGetAllAuthenticators() throws Exception {

        List<ApplicationAuthenticator> allAuthenticators = applicationAuthenticationService.getAllAuthenticators();
        assertEquals(allAuthenticators.size(), 3);
    }

    @Test
    public void testGetLocalAuthenticators() throws Exception {

        List<ApplicationAuthenticator> localAuthenticators = applicationAuthenticationService.getLocalAuthenticators();
        assertEquals(localAuthenticators.size(), 1);
        assertEquals(localAuthenticators.get(0).getName(), LOCAL_AUTHENTICATOR_NAME);
    }

    @Test
    public void testGetFederatedAuthenticators() throws Exception {

        List<ApplicationAuthenticator> federatedAuthenticators =
                applicationAuthenticationService.getFederatedAuthenticators();
        assertEquals(federatedAuthenticators.size(), 1);
        assertEquals(federatedAuthenticators.get(0).getName(), FEDERATED_AUTHENTICATOR_NAME);
    }

    @Test
    public void testGetRequestPathAuthenticators() throws Exception {

        List<ApplicationAuthenticator> requestPathAuthenticators =
                applicationAuthenticationService.getRequestPathAuthenticators();
        assertEquals(requestPathAuthenticators.size(), 1);
        assertEquals(requestPathAuthenticators.get(0).getName(), REQUESTPATH_AUTHENTICATOR_NAME);
    }

}
