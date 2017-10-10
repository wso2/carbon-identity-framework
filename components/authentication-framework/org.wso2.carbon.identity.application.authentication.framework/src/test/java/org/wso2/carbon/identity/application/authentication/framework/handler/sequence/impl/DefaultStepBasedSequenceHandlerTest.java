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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.collections.CollectionUtils;
import org.mockito.Mock;
import org.mockito.Spy;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorStateInfo;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

@PrepareForTest(FrameworkUtils.class)
public class DefaultStepBasedSequenceHandlerTest {

    private DefaultStepBasedSequenceHandler stepBasedSequenceHandler;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Spy
    private AuthenticationContext context;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setUp() throws Exception {
        stepBasedSequenceHandler = new DefaultStepBasedSequenceHandler();
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {
        CommonTestUtils.testSingleton(
                DefaultStepBasedSequenceHandler.getInstance(),
                DefaultStepBasedSequenceHandler.getInstance()
        );
    }

    @DataProvider(name = "spRoleMappingDataProvider")
    public Object[][] provideSpRoleMappingData() {
        return Util.getSpRoleMappingData();
    }

    @Test(dataProvider = "spRoleMappingDataProvider")
    public void testGetServiceProviderMappedUserRoles(Map<String, String> spRoleMappings,
                                                      List<String> localUserRoles,
                                                      String multiAttributeSeparator,
                                                      String expectedRoles) throws Exception {
        Util.mockMultiAttributeSeparator(multiAttributeSeparator);
        SequenceConfig sequenceConfig = Util.mockSequenceConfig(spRoleMappings);
        String mappedRoles = stepBasedSequenceHandler.getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
        assertEquals(mappedRoles, expectedRoles, "Service Provider Mapped Role do not have the expect value.");
    }

    @DataProvider(name = "spRoleClaimUriProvider")
    private Object[][] getSpRoleClaimUriData() {
        return new Object[][]{
                {"SP_ROLE_CLAIM", "SP_ROLE_CLAIM"},
                {null, FrameworkConstants.LOCAL_ROLE_CLAIM_URI},
                {"", FrameworkConstants.LOCAL_ROLE_CLAIM_URI}
        };
    }

    /*
        Find SP mapped role claim URI among mapped claims
     */
    @Test(dataProvider = "spRoleClaimUriProvider")
    public void testGetSpRoleClaimUri(String spRoleClaimUri,
                                      String expectedRoleClaimUri) throws Exception {
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getRoleClaim()).thenReturn(spRoleClaimUri);
        assertEquals(stepBasedSequenceHandler.getSpRoleClaimUri(appConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "spClaimMappingProvider")
    public Object[][] getSpClaimMappingProvider() {
        return new Object[][]{
                {       // SP mapped role claim
                        new HashMap<String, String>() {{
                            put("SP_ROLE_CLAIM", FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
                        }},
                        "SP_ROLE_CLAIM"
                },
                {       // Role claim not among SP mapped claims
                        new HashMap<String, String>() {{
                            put("SP_CLAIM", "LOCAL_CLAIM");
                        }},
                        FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                },
                {      // No SP mapped claims
                        new HashMap<>(), FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                },
                {
                        null, FrameworkConstants.LOCAL_ROLE_CLAIM_URI
                }
        };
    }

    /*
        Get role claim URI from SP mapped claims
     */
    @Test(dataProvider = "spClaimMappingProvider")
    public void testGetSpRoleClaimUriSpMappedClaim(Map<String, String> claimMappings,
                                                   String expectedRoleClaim) throws Exception {
        ApplicationConfig appConfig = mock(ApplicationConfig.class);
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        String roleClaim = stepBasedSequenceHandler.getSpRoleClaimUri(appConfig);
        assertEquals(roleClaim, expectedRoleClaim);
    }

    @DataProvider(name = "idpRoleClaimUriProvider")
    public Object[][] getIdpRoleClaimUriData() {
        return new Object[][]{
                {"IDP_ROLE_CLAIM", "IDP_ROLE_CLAIM"},
                {"", ""},
                {null, null}
        };
    }

    /*
        Get User Role Claim URI from IDP Mapped Role Claim URI
     */
    @Test(dataProvider = "idpRoleClaimUriProvider")
    public void testGetIdpRoleClaimUri(String idpRoleClaimUri,
                                       String expectedRoleClaimUri) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
        when(externalIdPConfig.getRoleClaimUri()).thenReturn(idpRoleClaimUri);
        assertEquals(stepBasedSequenceHandler.getIdpRoleClaimUri(externalIdPConfig), expectedRoleClaimUri);
    }

    @DataProvider(name = "idpClaimMappingProvider")
    public Object[][] getIdpClaimMappingsProvider() {
        return new Object[][]{
                {       // SP mapped role claim
                        new ClaimMapping[]{
                                ClaimMapping.build(FrameworkConstants.LOCAL_ROLE_CLAIM_URI, "IDP_ROLE_CLAIM", "", true)
                        },
                        "IDP_ROLE_CLAIM"
                },
                {       // Role claim not among SP mapped claims
                        new ClaimMapping[]{
                                ClaimMapping.build("LOCAL_CLAIM", "IDP_CLAIM", "", true)
                        },
                        null
                },
                {       // Role claim among claim mappings but remote claim is null
                        new ClaimMapping[]{
                                ClaimMapping.build(FrameworkConstants.LOCAL_ROLE_CLAIM_URI, null, null, true)
                        },
                        null
                },
                {      // No IDP mapped claims
                        new ClaimMapping[0], null
                },
                {
                        null, null
                }
        };
    }

    @Test(dataProvider = "idpClaimMappingProvider")
    public void testGetIdpRoleClaimUriFromClaimMappings(Object claimMappings,
                                                        String expectedRoleClaimUri) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);
        when(externalIdPConfig.getClaimMappings()).thenReturn((ClaimMapping[]) claimMappings);

        String roleClaim = stepBasedSequenceHandler.getIdpRoleClaimUri(externalIdPConfig);
        assertEquals(roleClaim, expectedRoleClaimUri);

    }

    @Test
    public void testHandleClaimMappings() throws Exception {
        ClaimHandler claimHandler = Util.mockClaimHandler();
        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                null,
                new AuthenticationContext(),
                new HashMap<String, String>(),
                false);
        assertNotNull(claims);
    }

    @Test
    public void testHandleClaimMappingsFailed() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("Claim Handling failed"))
                .when(claimHandler)
                .handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class), any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = stepBasedSequenceHandler.handleClaimMappings(
                null,
                new AuthenticationContext(),
                new HashMap<String, String>(),
                false);

        assertNotNull(claims);
        assertEquals(claims.size(), 0);
    }

    @DataProvider(name = "idpMappedUserRoleDataProvider")
    public Object[][] getIdpMappedUserRolesData() {
        return new Object[][]{
                // IDP mapped user role is null
                {null, null, true, null}
        };
    }

    @Test(dataProvider = "idpMappedUserRoleDataProvider")
    public void testGetIdentityProviderMappedUserRoles(Map<String, String> attributeValueMap,
                                                       String idpRoleClaimUri,
                                                       boolean excludeUnmapped,
                                                       List<String> expected) throws Exception {

        ExternalIdPConfig externalIdPConfig = mock(ExternalIdPConfig.class);

        List<String> mappedUserRoles = stepBasedSequenceHandler.getIdentityProvideMappedUserRoles(externalIdPConfig,
                attributeValueMap, idpRoleClaimUri, excludeUnmapped);

        if (CollectionUtils.isEmpty(mappedUserRoles)) {
            mappedUserRoles = Collections.emptyList();
        }

        if (CollectionUtils.isEmpty(expected)) {
            expected = Collections.emptyList();
        }

        Collections.sort(mappedUserRoles);
        Collections.sort(expected);
        assertEquals(mappedUserRoles, expected);
    }

    @Test
    public void testResetAuthenticationContext() throws Exception {

        AuthenticationContext context = new AuthenticationContext();
        context.setSubject(new AuthenticatedUser());
        context.setStateInfo(mock(AuthenticatorStateInfo.class));
        context.setExternalIdP(mock(ExternalIdPConfig.class));

        Map<String, String> authenticatorProperties = new HashMap<>();
        authenticatorProperties.put("Prop1", "Value1");

        context.setAuthenticatorProperties(authenticatorProperties);
        context.setRetryCount(3);
        context.setRetrying(true);
        context.setCurrentAuthenticator("OIDCAuthenticator");

        stepBasedSequenceHandler.resetAuthenticationContext(context);

        assertNull(context.getSubject());
        assertNull(context.getStateInfo());
        assertNull(context.getExternalIdP());
        assertEquals(context.getAuthenticatorProperties().size(), 0);
        assertEquals(context.getRetryCount(), 0);
        assertFalse(context.isRetrying());
        assertNull(context.getCurrentAuthenticator());
    }

}
