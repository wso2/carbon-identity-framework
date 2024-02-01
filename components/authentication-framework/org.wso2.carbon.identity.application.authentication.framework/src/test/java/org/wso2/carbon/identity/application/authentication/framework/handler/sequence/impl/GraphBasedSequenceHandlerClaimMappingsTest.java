/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.common.testng.realm.UserStoreModel;
import org.wso2.carbon.identity.core.IdentityClaimManager;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.user.core.claim.Claim;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests the claim handling in the Javascript.
 */
@Test
@PowerMockIgnore({"javax.xml.*", "org.mockito.*", "org.apache.*", "org.w3c.*", })
@PrepareForTest({IdentityClaimManager.class})
@WithH2Database(jndiName = "jdbc/WSO2IdentityDB", files = {"dbScripts/h2.sql"})
@WithCarbonHome
@WithRealmService(injectToSingletons =
        {IdentityCoreServiceDataHolder.class, FrameworkServiceDataHolder.class})
public class GraphBasedSequenceHandlerClaimMappingsTest extends GraphBasedSequenceHandlerAbstractTest {

    public void testHandleClaimHandling() throws Exception {

        LoggerUtils.isLogMaskingEnable = false;
        PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);

        ServiceProvider sp1 = getTestServiceProvider("js-sp-4-claim.xml");

        AuthenticationContext context = getAuthenticationContext(sp1);

        FrameworkServiceDataHolder.getInstance().setAdaptiveAuthenticationAvailable(true);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);

        HttpServletRequest req = createMockHttpServletRequest();

        HttpServletResponse resp = mock(HttpServletResponse.class);

        Claim[] supportedClaims = new Claim[3];
        Claim firstNameClaim = new Claim();
        firstNameClaim.setClaimUri("http://wso2.org/claims/givenname");
        supportedClaims[0] = firstNameClaim;
        Claim lastNameClaim = new Claim();
        lastNameClaim.setClaimUri("http://wso2.org/claims/lastname");
        supportedClaims[1] = lastNameClaim;
        Claim displayNameClaim = new Claim();
        displayNameClaim.setClaimUri("http://wso2.org/claims/displayName");
        supportedClaims[2] = displayNameClaim;

        mockStatic(IdentityClaimManager.class);
        IdentityClaimManager identityClaimManager = mock(IdentityClaimManager.class);
        when(IdentityClaimManager.getInstance()).thenReturn(identityClaimManager);
        when(identityClaimManager.getAllSupportedClaims(any())).thenReturn(supportedClaims);

        RealmService currentRealmService = FrameworkServiceDataHolder.getInstance().getRealmService();

        UserStoreModel userStoreModel = createUserStoreModel();
        userStoreModel.bindToRealm();

        //Set the AUTHENTICATING_USER, so that the Mock authenticators can create an authenticated user based on
        //this user_id, from the supplies user model.
        context.setProperty(AUTHENTICATING_USER, TEST_USER_1_ID);

        graphBasedSequenceHandler.handle(req, resp, context);
        userStoreModel.unBindFromRealm();
        FrameworkServiceDataHolder.getInstance().setRealmService(currentRealmService);

        String displayNameCreatedByScript =
                userStoreModel.getClaimValues("4b4414e1-916b-4475-aaee-6b0751c29ff6").
                        get("http://wso2.org/claims/displayName");
        assertEquals(displayNameCreatedByScript, "FName Lname by Javascript");

    }

    private UserStoreModel createUserStoreModel() {

        UserStoreModel userStoreModel = new UserStoreModel();
        userStoreModel.newUserBuilder()
                .withUserId(TEST_USER_1_ID)
                .withClaim("http://wso2.org/claims/givenname", "FName")
                .withClaim("http://wso2.org/claims/lastname", "Lname")
                .build();
        return userStoreModel;
    }
}
