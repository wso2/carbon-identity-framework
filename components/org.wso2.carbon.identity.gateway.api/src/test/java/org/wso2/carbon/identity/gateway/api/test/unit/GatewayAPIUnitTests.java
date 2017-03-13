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

package org.wso2.carbon.identity.gateway.api.test.unit;

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
import org.wso2.carbon.identity.gateway.api.request.GatewayRequest;
import org.wso2.carbon.identity.gateway.api.request.GatewayRequestBuilderFactory;
import org.wso2.carbon.identity.gateway.api.response.GatewayResponseBuilderFactory;
import org.wso2.carbon.identity.mgt.IdentityStore;
import org.wso2.carbon.identity.mgt.RealmService;
import org.wso2.carbon.identity.mgt.exception.DomainException;
import org.wso2.carbon.identity.mgt.impl.Domain;
import org.wso2.carbon.identity.mgt.impl.internal.IdentityMgtDataHolder;

import javax.ws.rs.core.Response;

/**
 * Identity Store Tests.
 */
@PrepareForTest()
public class GatewayAPIUnitTests {

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
    public void testGatewayRequestBuilder() {
        GatewayRequest.GatewayRequestBuilder builder = new GatewayRequest.GatewayRequestBuilder();
        builder.addAttribute("testAttribute", "testAttributeValue");
        builder.addHeader("testHeader", "testHeaderValue");
        builder.addParameter("testParameter", "testParameterValue");
        builder.setContentType("application/json");
        builder.setHttpMethod("GET");
        builder.setQueryString("param1=param1value&param2=param2value");
        builder.setRequestURI("gateway/someContext");
        GatewayRequest gatewayRequest = builder.build();
//      /*  Assert.assertEquals(gatewayRequest.getAttribute("testAttribute"),"testAttributeValue");
        Assert.assertEquals(gatewayRequest.getHeader("testHeader"), "testHeaderValue");
        Assert.assertEquals(gatewayRequest.getContentType(), "application/json");
        Assert.assertEquals(gatewayRequest.getRequestURI(), "gateway/someContext");
        Assert.assertEquals(gatewayRequest.getQueryString(), "param1=param1value&param2=param2value");
    }

    @Test
    public void testRequestBuilderFactoryErrorHandling() {
        GatewayRequestBuilderFactory gatewayRequestBuilderFactory = new GatewayRequestBuilderFactory();
        Response.ResponseBuilder responseBuilder = gatewayRequestBuilderFactory.handleException(new
                GatewayClientException(
                "This is a gateway client exception"));
        Response response = responseBuilder.build();
        Assert.assertEquals(400, response.getStatus());
        Assert.assertEquals("This is a gateway client exception", response.getEntity());

        responseBuilder = gatewayRequestBuilderFactory.handleException(new
                GatewayRuntimeException(
                "This is a gateway runtime exception"));
        response = responseBuilder.build();
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals("something went wrong", response.getEntity());
    }

    @Test
    public void testResponseStatusInErrors() {
        GatewayResponseBuilderFactory gatewayResponseBuilderFactory = new GatewayResponseBuilderFactory();

        Response.ResponseBuilder responseBuilder = gatewayResponseBuilderFactory
                .handleException(new GatewayRuntimeException("This is a " +
                        "gateway runtime exception"));
        Response response = responseBuilder.build();
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals("This is a gateway runtime exception", response.getEntity());


        responseBuilder = gatewayResponseBuilderFactory
                .handleException(new GatewayRuntimeException("This is a run time " +
                        "exception"));
        response = responseBuilder.build();
        Assert.assertEquals(500, response.getStatus());
        Assert.assertEquals("This is a run time exception", response.getEntity());
    }
}
