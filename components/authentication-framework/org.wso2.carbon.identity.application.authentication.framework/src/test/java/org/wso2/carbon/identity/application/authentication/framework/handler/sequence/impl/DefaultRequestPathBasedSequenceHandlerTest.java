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

import edu.emory.mathcs.backport.java.util.Arrays;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.reset;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest(FrameworkUtils.class)
public class DefaultRequestPathBasedSequenceHandlerTest {


    DefaultRequestPathBasedSequenceHandler sequenceHandler;

    @BeforeMethod
    public void setUp() throws Exception {
        sequenceHandler = new DefaultRequestPathBasedSequenceHandler();
    }

    @AfterMethod
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetInstance() throws Exception {

        DefaultRequestPathBasedSequenceHandler sequenceHandler = DefaultRequestPathBasedSequenceHandler.getInstance();
        Assert.assertNotNull(sequenceHandler);

        DefaultRequestPathBasedSequenceHandler anotherSequenceHandler = DefaultRequestPathBasedSequenceHandler
                .getInstance();
        Assert.assertNotNull(anotherSequenceHandler);
        Assert.assertEquals(sequenceHandler, anotherSequenceHandler);
    }

    @Test
    public void testHandle() throws Exception {


    }

    @Test
    public void testHandlePostAuthentication() throws Exception {
    }


    @DataProvider(name = "spRoleMappingDataProvider")
    public Object[][] provideSpRoleMappingData() {

        Map<String, String> spRoleMappings = new HashMap<>();
        spRoleMappings.put("LOCAL_ROLE1", "SP_ROLE1");
        spRoleMappings.put("LOCAL_ROLE2", "SP_ROLE2");

        List<String> localUserRoles = Arrays.asList(new String[]{"LOCAL_ROLE1", "ADMIN", "LOCAL_ROLE2"});
        String localRoles = "LOCAL_ROLE1,ADMIN,LOCAL_ROLE2";

        return new Object[][]{
                {spRoleMappings, localUserRoles, "SP_ROLE1,ADMIN,SP_ROLE2"},
                {null, localUserRoles, localRoles},
                {new HashMap<>(), localUserRoles, localRoles},
                {spRoleMappings, new ArrayList<>(), null},
                {spRoleMappings, null, null}
        };
    }


    @Test(dataProvider = "spRoleMappingDataProvider")
    public void testGetServiceProviderMappedUserRoles(Map<String, String> spRoleMappings,
                                                      List<String> localUserRoles,
                                                      String expectedRoles) throws Exception {

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.getApplicationName()).thenReturn("APP");
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(applicationConfig.getRoleMappings()).thenReturn(spRoleMappings);

        String mappedRoles = sequenceHandler.getServiceProviderMappedUserRoles(sequenceConfig, localUserRoles);
        Assert.assertEquals(mappedRoles, expectedRoles);
    }

    @Test
    public void testGetSpRoleClaimUri() throws Exception {

        String roleClaim;
        ApplicationConfig appConfig = mock(ApplicationConfig.class);

        // IDP mapped Role claim
        when(appConfig.getRoleClaim()).thenReturn("IDP_ROLE_CLAIM");
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, "IDP_ROLE_CLAIM");

        // No IDP mapped Role claim or SP mapped Role claim available
        reset(appConfig);
        when(appConfig.getRoleClaim()).thenReturn(null);
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
    }

    /*
        Get role claim from SP mapped roles
     */
    @Test
    public void testGetSpRoleClaimUriSpMappedClaim() throws Exception {

        String roleClaim;
        ApplicationConfig appConfig = mock(ApplicationConfig.class);

        // SP mapped role claim
        reset(appConfig);
        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put("SP_ROLE_CLAIM", FrameworkConstants.LOCAL_ROLE_CLAIM_URI);
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, "SP_ROLE_CLAIM");

        // Role not among SP mapped claims
        reset(appConfig);
        claimMappings = new HashMap<>();
        claimMappings.put("SP_MAPPED_CLAIM", "DUMMY");
        when(appConfig.getClaimMappings()).thenReturn(claimMappings);
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);

        // No IDP mapped Role claim or SP mapped Role claim available
        reset(appConfig);
        when(appConfig.getClaimMappings()).thenReturn(new HashMap<String, String>());
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);

        reset(appConfig);
        when(appConfig.getClaimMappings()).thenReturn(null);
        // Get the default role claim URI
        roleClaim = sequenceHandler.getSpRoleClaimUri(appConfig);
        Assert.assertEquals(roleClaim, FrameworkConstants.LOCAL_ROLE_CLAIM_URI);

    }


    @Test
    public void testHandleClaimMappingsErrorFlow() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);
        doThrow(new FrameworkException("ERROR")).when(claimHandler).handleClaimMappings(any(StepConfig.class),
                any(AuthenticationContext.class), any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        Map<String, String> claims = sequenceHandler.handleClaimMappings(new AuthenticationContext());
        Assert.assertNull(claims);
    }

    @Test
    public void testHandleClaimMappings() throws Exception {

        ClaimHandler claimHandler = mock(ClaimHandler.class);

        Map<String, String> claims = new HashMap<>();
        claims.put("claim1", "value1");

        doReturn(claims).when(claimHandler).handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                any(Map.class), anyBoolean());

        mockStatic(FrameworkUtils.class);
        when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);

        claims = sequenceHandler.handleClaimMappings(new AuthenticationContext());
        Assert.assertNotNull(claims);
    }

}
