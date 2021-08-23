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

import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.dao.CacheBackedIdPMgtDAO;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore({"javax.net.*", "javax.security.*", "javax.crypto.*", "javax.xml.*", "org.xml.*", "org.w3c.*",
        "javax.naming.*", "javax.sql.*"})
@PrepareForTest({AbstractUserStoreManager.class, IdentityTenantUtil.class})
public class GraphBasedSequenceHandlerAbstractTest extends AbstractFrameworkTest {

    protected static final String APPLICATION_AUTHENTICATION_FILE_NAME
            = "application-authentication-GraphStepHandlerTest.xml";
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

        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantDomain(anyInt())).thenReturn("foo.com");

        RealmService mockRealmService = mock(RealmService.class);
        FrameworkServiceDataHolder.getInstance().setRealmService(mockRealmService);

        CacheBackedIdPMgtDAO cacheBackedIdPMgtDAO = mock(CacheBackedIdPMgtDAO.class);
        Field daoField = IdentityProviderManager.class.getDeclaredField("dao");
        daoField.setAccessible(true);
        daoField.set(IdentityProviderManager.getInstance(), cacheBackedIdPMgtDAO);

        TenantManager tenantManager = mock(TenantManager.class);
        when(tenantManager.getTenantId(anyString())).thenReturn(1);
        when(mockRealmService.getTenantManager()).thenReturn(tenantManager);
        IdentityTenantUtil.setRealmService(mockRealmService);
        UserRealm mockUserRealm = mock(UserRealm.class);
        AbstractUserStoreManager mockUserStoreManager = mock(AbstractUserStoreManager.class);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getUserIDFromUserName(anyString()))
                .thenReturn("59d2c583-eafc-412a-a32e-bc409f3bd4e6");

        UserStoreManager mockSecUserStoreManager = mock(UserStoreManager.class);
        RealmConfiguration mockRealmConfiguration = mock(RealmConfiguration.class);
        when(mockSecUserStoreManager.getRealmConfiguration()).thenReturn(mockRealmConfiguration);
        when(mockUserStoreManager.getSecondaryUserStoreManager(anyString())).thenReturn(mockSecUserStoreManager);
        when(mockRealmConfiguration.getUserStoreProperty(IdentityCoreConstants.CASE_INSENSITIVE_USERNAME))
                .thenReturn("false");

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

    protected static class MockSubjectCallback implements SubjectCallback, Serializable {

        private static final long serialVersionUID = 597048141496121100L;

        @Override
        public AuthenticatedUser getAuthenticatedUser(AuthenticationContext context) {

            AuthenticatedUser result = AuthenticatedUser.createLocalAuthenticatedUserFromSubjectIdentifier("test_user");
            result.setUserId("4b4414e1-916b-4475-aaee-6b0751c29ff6");
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

    protected HttpServletRequest createMockHttpServletRequest() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        Map<String, Object> attributes = new HashMap<>();
        doAnswer(m -> attributes.put(m.getArgumentAt(0, String.class), m.getArgumentAt(1, Object.class))).when(req)
                .setAttribute(anyString(), anyObject());

        doAnswer(m -> attributes.get(m.getArgumentAt(0, String.class))).when(req).getAttribute(anyString());

        return req;
    }
}
