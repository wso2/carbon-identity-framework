/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.handler.step.impl;

import edu.emory.mathcs.backport.java.util.Arrays;
import junit.framework.TestCase;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.DefaultAuthStepsSelector;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AcrRule;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthHistory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.AuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.LogoutFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultStepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for AdaptiveStepHandler.
 */
public class AdaptiveStepHandlerTest extends TestCase {

    private DefaultStepBasedSequenceHandler defaultStepBasedSequenceHandler = new DefaultStepBasedSequenceHandler();
    private AdaptiveStepHandler adaptiveStepHandler = new AdaptiveStepHandler(new DefaultAuthStepsSelector());

    protected void setUp() throws UserStoreException, NoSuchFieldException, IllegalAccessException {
        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.toString());
        FrameworkServiceDataHolder.getInstance().getAuthenticators()
                .add(new MockAuthenticator("BasicMockAuthenticator"));
        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(new MockAuthenticator("HwkMockAuthenticator"));

        CacheBackedIdPMgtDAO cacheBackedIdPMgtDAO = mock(CacheBackedIdPMgtDAO.class);
        Field daoField = IdentityProviderManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(IdentityProviderManager.getInstance(), cacheBackedIdPMgtDAO);

        RealmService mockRealmService = mock(RealmService.class);
        TenantManager tenantManager = mock(TenantManager.class);
        when(tenantManager.getTenantId(anyString())).thenReturn(1);
        when(mockRealmService.getTenantManager()).thenReturn(tenantManager);
        IdentityTenantUtil.setRealmService(mockRealmService);

    }

    public void testHandle() throws Exception {
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setTenantDomain("test_domain");
        authenticationContext.setCurrentStep(1);
        authenticationContext.setSequenceConfig(getSequenceConfigs().get(0));

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        defaultStepBasedSequenceHandler.handle(req, resp, authenticationContext);
    }

    public void testHandle_WithAmr_Pwd() throws Exception {
        AuthenticationContext authenticationContext = getAuthenticationContext("test-app-basic-hwk");
        authenticationContext.setAcrRequested(Arrays.asList(new String[] { "Password" }));
        authenticationContext.setAcrRule(AcrRule.MINIMUM);

        HttpServletRequest req = mock(HttpServletRequest.class);

        HttpServletResponse resp = mock(HttpServletResponse.class);

        defaultStepBasedSequenceHandler.handle(req, resp, authenticationContext);

        assertNotNull(authenticationContext.getAuthenticationStepHistory());
        assertTrue("Password authentication should be triggered as it was requested",
                contains(authenticationContext.getAuthenticationStepHistory(),
                        new AuthHistory("BasicMockAuthenticator", null)));
//        assertFalse("Hardware Key authentication should not be triggered as it was not requested",
//                contains(authenticationContext.getAuthenticationStepHistory(),
//                        new AuthHistory("HwkMockAuthenticator", null)));
    }

    public void testHandle_WithAmr_Pwd_Hwk() throws Exception {
        final AuthenticationContext authenticationContext = getAuthenticationContext("test-app-basic-hwk");
        authenticationContext
                .setAcrRequested(Arrays.asList(new String[] { "Password", "HardwareKey" }));

        final HttpServletRequest req = mock(HttpServletRequest.class);

        final HttpServletResponse resp = mock(HttpServletResponse.class);

        defaultStepBasedSequenceHandler.handle(req, resp, authenticationContext);
        assertNotNull(authenticationContext.getAuthenticationStepHistory());
        assertTrue(contains(authenticationContext.getAuthenticationStepHistory(),
                new AuthHistory("BasicMockAuthenticator", null)));
        assertTrue(contains(authenticationContext.getAuthenticationStepHistory(),
                new AuthHistory("HwkMockAuthenticator", null)));

    }

    private AuthenticationContext getAuthenticationContext(String appName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setTenantDomain("test_domain");
        authenticationContext.setCurrentStep(1);
        authenticationContext.setSequenceConfig(findSequenceConfig(appName));
        return authenticationContext;
    }

    private boolean contains(List<AuthHistory> authenticationStepHistory, AuthHistory testAuthHistory) {
        if (authenticationStepHistory == null | authenticationStepHistory.isEmpty()) {
            return false;
        }
        for (AuthHistory authHistory : authenticationStepHistory) {
            if (authHistory.getAuthenticatorName().equals(testAuthHistory.getAuthenticatorName())) {
                if (testAuthHistory.getIdpName() == null) {
                    return true;
                } else if (authHistory.getIdpName().equals(testAuthHistory.getIdpName())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<SequenceConfig> getSequenceConfigs()
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        FileBasedConfigurationBuilder configurationBuilder = FileBasedConfigurationBuilder.getInstance();
        Method buildConfigurationMethod = FileBasedConfigurationBuilder.class
                .getDeclaredMethod("buildConfiguration", InputStream.class);
        buildConfigurationMethod.setAccessible(true);
        InputStream file = this.getClass()
                .getResourceAsStream("application-authentication-AdaptiveStepHandlerTest.xml");
        buildConfigurationMethod.invoke(configurationBuilder, file);

        //Make the Adaptive Step Handler our default handler for the tests.
        FileBasedConfigurationBuilder.getInstance().getExtensions()
                .put(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER, adaptiveStepHandler);
        
        return FileBasedConfigurationBuilder.getInstance().getSequenceList();
    }

    private SequenceConfig findSequenceConfig(String appName)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<SequenceConfig> sequenceConfigs = getSequenceConfigs();
        if (sequenceConfigs == null) {
            fail("No sequence configs found");
        }

        for (SequenceConfig sequenceConfig : sequenceConfigs) {
            if (sequenceConfig.getApplicationId().equals(appName)) {
                return sequenceConfig;
            }
        }
        fail("Could not find sequence with app name: " + appName);
        return null;
    }

    public static class MockAuthenticator implements ApplicationAuthenticator {

        private String name;

        public MockAuthenticator(String name) {
            this.name = name;
        }

        @Override
        public boolean canHandle(HttpServletRequest request) {
            return false;
        }

        @Override
        public AuthenticatorFlowStatus process(HttpServletRequest request, HttpServletResponse response,
                AuthenticationContext context) throws AuthenticationFailedException, LogoutFailedException {
            return AuthenticatorFlowStatus.SUCCESS_COMPLETED;
        }

        @Override
        public String getContextIdentifier(HttpServletRequest request) {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getFriendlyName() {
            return null;
        }

        @Override
        public String getClaimDialectURI() {
            return null;
        }

        @Override
        public List<Property> getConfigurationProperties() {
            return null;
        }
    }
}