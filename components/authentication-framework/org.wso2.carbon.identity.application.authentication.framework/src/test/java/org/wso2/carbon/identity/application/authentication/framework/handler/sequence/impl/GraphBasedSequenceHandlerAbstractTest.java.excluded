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

package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.MockAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsFunctionRegistryImpl;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.SubjectCallback;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;

public class GraphBasedSequenceHandlerAbstractTest extends AbstractFrameworkTest {

    private static final Log logger = LogFactory.getLog(GraphBasedSequenceHandlerAbstractTest.class);

    protected static final String APPLICATION_AUTHENTICATION_FILE_NAME
            = "application-authentication-GraphStepHandlerTest.xml";
    protected static final String AUTHENTICATING_TENANT_ID = "AUTHENTICATING_TENANT_ID";
    protected static final String AUTHENTICATING_USER = "AUTHENTICATING_USER";
    protected static final String TEST_USER_1_ID = "4b4414e1-916b-4475-aaee-6b0751c29ff6";
    protected GraphBasedSequenceHandler graphBasedSequenceHandler = new GraphBasedSequenceHandler();
    protected UIBasedConfigurationLoader configurationLoader;
    protected JsGraphBuilderFactory graphBuilderFactory;

    @BeforeClass
    protected void setupSuite() {

        configurationLoader = new UIBasedConfigurationLoader();
        graphBuilderFactory = new JsGraphBuilderFactory();

        JsFunctionRegistryImpl jsFunctionRegistry = new JsFunctionRegistryImpl();
        FrameworkServiceDataHolder.getInstance().setJsFunctionRegistry(jsFunctionRegistry);

        graphBuilderFactory.init();
        FrameworkServiceDataHolder.getInstance().setJsGraphBuilderFactory(graphBuilderFactory);

        AsyncSequenceExecutor asyncSequenceExecutor = new AsyncSequenceExecutor();
        asyncSequenceExecutor.init();
        FrameworkServiceDataHolder.getInstance().setAsyncSequenceExecutor(asyncSequenceExecutor);
    }

    @BeforeMethod
    protected void setUp() throws UserStoreException, NoSuchFieldException, IllegalAccessException,
            NoSuchMethodException, URISyntaxException {

        URL root = this.getClass().getClassLoader().getResource(".");
        File file = new File(root.getPath());
        System.setProperty("carbon.home", file.toString());
        resetAuthenticators();

        CacheBackedIdPMgtDAO cacheBackedIdPMgtDAO = mock(CacheBackedIdPMgtDAO.class);
        Field daoField = IdentityProviderManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(IdentityProviderManager.getInstance(), cacheBackedIdPMgtDAO);

        Field configFilePathField = FileBasedConfigurationBuilder.class.getDeclaredField("configFilePath");
        configFilePathField.setAccessible(true);
        URL url = this.getClass().getResource(APPLICATION_AUTHENTICATION_FILE_NAME);
        configFilePathField.set(null, Paths.get(url.toURI()).toString());
    }

    protected void resetAuthenticators() {

        FrameworkServiceDataHolder.getInstance().getAuthenticators().clear();
        FrameworkServiceDataHolder.getInstance().getAuthenticators()
                .add(new MockAuthenticator("BasicMockAuthenticator", new MockSubjectCallback()));
        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(new MockAuthenticator("HwkMockAuthenticator"));
        FrameworkServiceDataHolder.getInstance().getAuthenticators().add(new MockAuthenticator("FptMockAuthenticator"));
    }

    protected HttpServletRequest createMockHttpServletRequest() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(m -> attributes.put(m.getArgument(0), m.getArgument(1))).when(req)
                .setAttribute(anyString(), anyObject());

        doAnswer(m -> attributes.get(m.getArgument(0))).when(req).getAttribute(anyString());

        return req;
    }

    protected static class MockSubjectCallback implements SubjectCallback, Serializable {

        private static final long serialVersionUID = 597048141496121100L;

        @Override
        public AuthenticatedUser getAuthenticatedUser(AuthenticationContext context) {

            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            if (realmService == null) {
                return createDefaultUser();
            }

            Integer tenantId = (Integer) context.getProperties().get(AUTHENTICATING_TENANT_ID);
            String userId = (String) context.getProperties().get(AUTHENTICATING_USER);
            if (userId == null) {
                return createDefaultUser();
            }
            if (tenantId == null) {
                tenantId = MultitenantConstants.SUPER_TENANT_ID;
            }
            try {
                UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
                Map<String, String> claimValues = userStoreManager.getUserClaimValuesWithID(userId, null, null);
                AuthenticatedUser result = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier(userId);
                result.setUserId(userId);
                claimValues.entrySet().stream().forEach(
                        (k) -> result.getUserAttributes().put(
                                ClaimMapping.build(k.getKey(), k.getKey(), "", false), k.getValue()));
                return result;
            } catch (UserStoreException e) {
                logger.error("There has been an issue creating the test(Mock) user from Mock user store." +
                        " The default User will be injected to the Authenticator instead.", e);
                return createDefaultUser();
            }
        }

        private AuthenticatedUser createDefaultUser() {

            AuthenticatedUser result = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("test_user");
            result.setUserId(TEST_USER_1_ID);
            result.getUserAttributes().put(ClaimMapping
                            .build("http://wso2.org/claims/givenname", "http://wso2.org/claims/givenname", "Test",
                                    false),
                    "Test");
            result.getUserAttributes().put(ClaimMapping
                            .build("http://wso2.org/claims/lastname", "http://wso2.org/claims/lastname", "Test", false),
                    "User");
            return result;
        }
    }
}
