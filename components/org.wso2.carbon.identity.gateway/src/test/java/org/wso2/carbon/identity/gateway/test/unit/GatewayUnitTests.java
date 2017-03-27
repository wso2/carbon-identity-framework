/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.gateway.test.unit;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.gateway.api.exception.GatewayClientException;
import org.wso2.carbon.identity.gateway.api.exception.GatewayRuntimeException;
import org.wso2.carbon.identity.gateway.authentication.executer.MultiOptionExecutionHandler;
import org.wso2.carbon.identity.gateway.local.LocalAuthenticationRequest;
import org.wso2.carbon.identity.gateway.local.LocalAuthenticationRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.handler.GatewayHandlerManager;
import org.wso2.carbon.identity.gateway.handler.session.DefaultSessionHandler;
import org.wso2.carbon.identity.gateway.internal.GatewayServiceHolder;
import org.wso2.carbon.identity.gateway.model.FederatedUser;
import org.wso2.carbon.identity.gateway.model.LocalUser;
import org.wso2.carbon.identity.gateway.model.User;
import org.wso2.carbon.identity.gateway.model.UserClaim;
import org.wso2.carbon.identity.gateway.request.AuthenticationRequest;
import org.wso2.carbon.identity.gateway.request.ClientAuthenticationRequest;
import org.wso2.carbon.identity.gateway.test.unit.sample.SampleAuthenticationHandler;
import org.wso2.carbon.identity.gateway.test.unit.sample.SampleSessionHandler;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.claim.Claim;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.exception.IdentityStoreException;
import org.wso2.carbon.identity.mgt.exception.UserNotFoundException;
import org.wso2.carbon.identity.mgt.impl.Domain;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;

import javax.ws.rs.core.Response;

/**
 * Unit tests for gateway.
 */
@PrepareForTest()
public class GatewayUnitTests {

    @Mock
    private RealmService realmService;

    //    @Mock
    //    private AuthorizationStore authorizationStore;

    @Mock
    private IdentityMgtDataHolder identityMgtDataHolder;

    @Mock
    private Domain domain;

    private IdentityStore identityStore;

    @BeforeClass
    public void initClass() {

        MockitoAnnotations.initMocks(this);
    }

    @BeforeMethod
    public void initMethod() throws DomainException {

    }

    @AfterMethod
    public void resetMocks() {

        Mockito.reset(realmService);
        //      Mockito.reset(authorizationStore);
        Mockito.reset(identityMgtDataHolder);
    }

    @Test
    public void testFederatedUser() {
        User user = new FederatedUser("FederatedUser");
        try {
            user.getClaims().add(new Claim("dialect", "claimUri", "value"));
            Assert.assertNotNull(user.getClaims());
            Assert.assertTrue(user.getClaims().size() > 0);
        } catch (IdentityStoreException | UserNotFoundException e) {
            Assert.fail("Error while getting user claims");
        }
    }

    @Test
    public void testGetAuthenticationHandlerError() {
        GatewayHandlerManager gatewayHandlerManager = GatewayHandlerManager.getInstance();

        try {
            gatewayHandlerManager.getAuthenticationHandler(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find AuthenticationHandler"));
        }

        GatewayServiceHolder gatewayServiceHolder = GatewayServiceHolder.getInstance();
        gatewayServiceHolder.getAuthenticationHandlers().add(new SampleAuthenticationHandler());

        try {
            gatewayHandlerManager.getAuthenticationHandler(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find AuthenticationHandler"));
        }
    }

    @Test
    public void testGetRequestValidatorError() {
        GatewayHandlerManager gatewayHandlerManager = GatewayHandlerManager.getInstance();

        try {
            gatewayHandlerManager.getRequestValidator(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find AbstractRequestValidator to handle this request."));
        }
    }

    @Test
    public void testGetResponseHandlerError() {
        GatewayHandlerManager gatewayHandlerManager = GatewayHandlerManager.getInstance();

        try {
            gatewayHandlerManager.getResponseHandler(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find ResponseHandler to handle this request."));
        }
    }

    @Test
    public void testGetSessionHandlers() {
        GatewayHandlerManager gatewayHandlerManager = GatewayHandlerManager.getInstance();

        try {
            gatewayHandlerManager.getSessionHandler(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find AbstractSessionHandler to handle this request."));
        }
        GatewayServiceHolder gatewayServiceHolder = GatewayServiceHolder.getInstance();
        DefaultSessionHandler defaultSessionHandler = new DefaultSessionHandler();
        gatewayServiceHolder.getSessionHandlers().add(defaultSessionHandler);

        Assert.assertNotNull(gatewayHandlerManager.getSessionHandler(null));
        gatewayServiceHolder.getSessionHandlers().remove(defaultSessionHandler);
        gatewayServiceHolder.getSessionHandlers().add(new SampleSessionHandler());
        try {
            gatewayHandlerManager.getSessionHandler(null);
        } catch (GatewayRuntimeException e) {
            Assert.assertTrue(e.getMessage().contains("Cannot find AbstractSessionHandler to handle this request."));
        }
    }


    @Test
    public void testLocalAuthentication() {
        LocalAuthenticationRequest.LocalAuthenticationRequestBuilder localAuthenticationRequestBuilder = new
                LocalAuthenticationRequest.LocalAuthenticationRequestBuilder();
        localAuthenticationRequestBuilder.setAuthenticatorName("testAuthenticator");
        localAuthenticationRequestBuilder.setIdentityProviderName("testIdentityProvider");
        LocalAuthenticationRequest localAuthenticationRequest = localAuthenticationRequestBuilder.build();
        Assert.assertEquals("testAuthenticator", localAuthenticationRequest.getAuthenticatorName());
        Assert.assertEquals("testIdentityProvider", localAuthenticationRequest.getIdentityProviderName());
    }

    @Test
    public void testLocalAuthenticationBuilderFactor() throws GatewayClientException {
        LocalAuthenticationRequestBuilderFactory factory = new LocalAuthenticationRequestBuilderFactory();
        Assert.assertNotNull(factory.getName());
        Response.ResponseBuilder builder = factory.handleException(new GatewayClientException("Error while validating" +
                " request"));
        Assert.assertNotNull(builder.build());
        Assert.assertNotNull(factory.getPriority());
    }

    @Test
    public void testLocalUser() {
        LocalUser localUser = new LocalUser(null);
    }

    @Test
    public void testUserClaim() {
        UserClaim userClaim = new UserClaim("http://org.wso2/claim/username", "testuser");
        userClaim.setUri("http://org.wso2/claim/username");
        Assert.assertEquals("http://org.wso2/claim/username", userClaim.getUri());
    }

    @Test
    public void testUtility() throws GatewayClientException {
        Assert.assertNull(
                GatewayServiceHolder.getInstance().getFederatedApplicationAuthenticator("federatedAuthenticator"));
        Assert.assertNull(GatewayServiceHolder.getInstance().getLocalApplicationAuthenticator("localAuthenticator"));
        Assert.assertNull(
                GatewayServiceHolder.getInstance().getRequestPathApplicationAuthenticator("requestPathAuthenticators"));
    }



}

