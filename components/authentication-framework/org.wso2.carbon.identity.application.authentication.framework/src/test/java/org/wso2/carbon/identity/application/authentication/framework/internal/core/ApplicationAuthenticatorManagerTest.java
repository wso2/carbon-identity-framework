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

package org.wso2.carbon.identity.application.authentication.framework.internal.core;

import org.mockito.MockedStatic;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.LocalApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.UserDefinedAuthenticatorService;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.UserDefinedFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.UserDefinedLocalAuthenticatorConfig;
import org.wso2.carbon.identity.base.AuthenticatorPropertyConstants;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@Listeners(MockitoTestNGListener.class)
public class ApplicationAuthenticatorManagerTest extends AbstractFrameworkTest {

    private final ApplicationAuthenticatorManager applicationAuthenticatorService =
            ApplicationAuthenticatorManager.getInstance();

    private static final String SYSTEM_DEFINED_AUTHENTICATOR_NAME = "BasicAuthenticator";
    private static final String USER_DEFINED_LOCAL_AUTHENTICATOR_NAME = "UserDefinedLocalMockAuthenticator";
    private static final String USER_DEFINED_FEDERATED_AUTHENTICATOR_NAME = "UserDefinedFederatedMockAuthenticator";
    private static final String TENANT_DOMAIN = "carbon.super";

    private static final ApplicationAuthenticator systemDefinedAuthenticator =
            new MockAuthenticator(SYSTEM_DEFINED_AUTHENTICATOR_NAME);
    private static final LocalApplicationAuthenticator userDefinedLocalAuthenticator =
            new MockAuthenticator.MockLocalAuthenticator(USER_DEFINED_LOCAL_AUTHENTICATOR_NAME);
    private static final FederatedApplicationAuthenticator userDefinedFederatedAuthenticator =
            new MockAuthenticator.MockFederatedAuthenticator(USER_DEFINED_FEDERATED_AUTHENTICATOR_NAME);

    private static UserDefinedAuthenticatorService userDefinedAuthenticatorService =
            mock(UserDefinedAuthenticatorService.class);

    private IdentityConfigParser mockIdentityConfigParser;
    private MockedStatic<IdentityConfigParser> identityConfigParser;

    private final MockedStatic<ApplicationAuthenticatorService> mockedAuthenticationService =
            mockStatic(ApplicationAuthenticatorService.class);
    private final ApplicationAuthenticatorService authenticatorService = mock(ApplicationAuthenticatorService.class);

    private final MockedStatic<IdentityProviderManager> mockedIdentityProviderManager =
            mockStatic(IdentityProviderManager.class);
    private final IdentityProviderManager identityProviderManager = mock(IdentityProviderManager.class);
    private FederatedAuthenticatorConfig[] fedAuthConfig;
    private IdentityProvider idp;

    @BeforeClass
    public void setUp() {

        removeAllSystemDefinedAuthenticators();
        mockIdentityConfigParser = mock(IdentityConfigParser.class);
        identityConfigParser = mockStatic(IdentityConfigParser.class);
        identityConfigParser.when(IdentityConfigParser::getInstance).thenReturn(mockIdentityConfigParser);
        applicationAuthenticatorService.addSystemDefinedAuthenticator(systemDefinedAuthenticator);
    }

    @BeforeTest
    public void setUpTest() throws Exception {

        when(userDefinedAuthenticatorService.getUserDefinedLocalAuthenticator(any())).thenReturn(
                userDefinedLocalAuthenticator);
        when(userDefinedAuthenticatorService.getUserDefinedFederatedAuthenticator(any())).thenReturn(
                userDefinedFederatedAuthenticator);

        mockedAuthenticationService.when(ApplicationAuthenticatorService::getInstance).thenReturn(authenticatorService);
        when(authenticatorService.getAllUserDefinedLocalAuthenticators(TENANT_DOMAIN)).thenReturn(
                List.of(new UserDefinedLocalAuthenticatorConfig(
                        AuthenticatorPropertyConstants.AuthenticationType.IDENTIFICATION)));

        fedAuthConfig = new FederatedAuthenticatorConfig[]{new UserDefinedFederatedAuthenticatorConfig()};
        idp = new IdentityProvider();
        idp.setIdentityProviderName("testIdp");
        idp.setFederatedAuthenticatorConfigs(fedAuthConfig);

        when(identityProviderManager.getAllUserDefinedFederatedAuthenticators(TENANT_DOMAIN))
                .thenReturn(Arrays.asList(fedAuthConfig));
        when(identityProviderManager.getIdPByResourceId(anyString(), anyString(), anyBoolean())).thenReturn(idp);
        mockedIdentityProviderManager.when(IdentityProviderManager::getInstance).thenReturn(identityProviderManager);
        FrameworkServiceDataHolder.getInstance().setIdentityProviderManager(identityProviderManager);
    }

    @AfterClass
    public void tearDown() {

        identityConfigParser.close();
        mockedAuthenticationService.close();
        mockedIdentityProviderManager.close();
    }

    @Test
    public void testGetAllAuthenticatorsWithAuthActionTypeEnabledAndNotNullUserDefinedAuthenticatorService()
            throws Exception {

        setAuthenticatorActionEnableStatus(true);
        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(userDefinedAuthenticatorService);
        List<ApplicationAuthenticator> result = applicationAuthenticatorService.getAllAuthenticators(TENANT_DOMAIN);
        assertEquals(3, result.size());
    }

    @Test
    public void testGetAllAuthenticatorsWithAuthenticationActionTypeDisabled() throws Exception {

        setAuthenticatorActionEnableStatus(false);
        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(userDefinedAuthenticatorService);
        List<ApplicationAuthenticator> result = applicationAuthenticatorService.getAllAuthenticators(TENANT_DOMAIN);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAuthenticatorByNameWithAuthenticationActionTypeDisabled() throws Exception {

        setAuthenticatorActionEnableStatus(false);
        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(userDefinedAuthenticatorService);
        Assert.assertNotNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                SYSTEM_DEFINED_AUTHENTICATOR_NAME, TENANT_DOMAIN));
        Assert.assertNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                USER_DEFINED_FEDERATED_AUTHENTICATOR_NAME, TENANT_DOMAIN));
        Assert.assertNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                USER_DEFINED_LOCAL_AUTHENTICATOR_NAME, TENANT_DOMAIN));
    }

    @Test
    public void testGetAllAuthenticatorsWithAuthActionTypeEnabledAndNullUserDefinedAuthenticatorService()
            throws Exception {

        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(null);
        Assert.assertNotNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                SYSTEM_DEFINED_AUTHENTICATOR_NAME, TENANT_DOMAIN));
        Assert.assertNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                USER_DEFINED_FEDERATED_AUTHENTICATOR_NAME, TENANT_DOMAIN));
        Assert.assertNull(applicationAuthenticatorService.getApplicationAuthenticatorByName(
                USER_DEFINED_LOCAL_AUTHENTICATOR_NAME, TENANT_DOMAIN));
    }

    @Test
    public void testGetAuthenticatorByNameWithAuthActionTypeEnabledAndNullUserDefinedAuthenticatorService()
            throws Exception {

        FrameworkServiceDataHolder.getInstance().setUserDefinedAuthenticatorService(null);
        setAuthenticatorActionEnableStatus(true);
        List<ApplicationAuthenticator> result = applicationAuthenticatorService.getAllAuthenticators(TENANT_DOMAIN);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetSerializableIdPByResourceId() throws Exception {

        IdentityProvider serializableIdp = applicationAuthenticatorService.getSerializableIdPByResourceId(
                "dummyResourceId", TENANT_DOMAIN);
        Assert.assertFalse(serializableIdp.getFederatedAuthenticatorConfigs()[0]
                instanceof UserDefinedFederatedAuthenticatorConfig);
    }

    private void setAuthenticatorActionEnableStatus(boolean isEnabled) {

        Map<String, Object> configMap = new HashMap<>();
        configMap.put("Actions.Types.Authentication.Enable", Boolean.toString(isEnabled));
        when(mockIdentityConfigParser.getConfiguration()).thenReturn(configMap);
    }
}
