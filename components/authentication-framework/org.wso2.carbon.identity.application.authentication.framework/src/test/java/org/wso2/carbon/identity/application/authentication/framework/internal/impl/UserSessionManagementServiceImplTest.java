/*
 * Copyright (c) 2025-2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.UserSessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementClientException;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for UserSessionManagementServiceImpl.
 */
@WithCarbonHome
public class UserSessionManagementServiceImplTest {

    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;

    @Mock
    private RealmService realmService;

    @Mock
    private TenantManager tenantManager;

    @Mock
    private UserSessionStore userSessionStore;

    @Mock
    private FederatedAssociationManager federatedAssociationManager;

    @Mock
    private SessionContext sessionContext;

    private UserSessionManagementServiceImpl userSessionManagementService;
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;
    private MockedStatic<UserSessionStore> userSessionStoreMockedStatic;
    private MockedStatic<IdentityUtil> identityUtilMockedStatic;
    private Map<SessionMgtConstants.AuthSessionUserKeys, String> authSessionUserMap = new HashMap<>();

    private static final String TEST_TENANT_DOMAIN = "test-tenant.com";
    private static final int TEST_TENANT_ID = 1;
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_FED_USER_ID = "test-fed-user-id";
    private static final String TEST_SESSION_ID_1 = "test-session-id-1";
    private static final String TEST_SESSION_ID_2 = "test-session-id-2";
    private static final String TEST_IDP_ID = "1";
    private static final String TEST_IDP_NAME = "LOCAL";

    @BeforeClass
    public void setUp() throws Exception {

        openMocks(this);

        frameworkServiceDataHolderMockedStatic = mockStatic(FrameworkServiceDataHolder.class);
        userSessionStoreMockedStatic = mockStatic(UserSessionStore.class);
        identityUtilMockedStatic = mockStatic(IdentityUtil.class);

        frameworkServiceDataHolderMockedStatic.when(FrameworkServiceDataHolder::getInstance)
                .thenReturn(frameworkServiceDataHolder);
        when(frameworkServiceDataHolder.getRealmService()).thenReturn(realmService);
        when(frameworkServiceDataHolder.getFederatedAssociationManager()).thenReturn(federatedAssociationManager);
        when(realmService.getTenantManager()).thenReturn(tenantManager);
        when(tenantManager.getTenantId(TEST_TENANT_DOMAIN)).thenReturn(TEST_TENANT_ID);

        userSessionManagementService = new UserSessionManagementServiceImpl();
        userSessionStoreMockedStatic.when(UserSessionStore::getInstance).thenReturn(userSessionStore);
        authSessionUserMap.put(SessionMgtConstants.AuthSessionUserKeys.USER_ID, TEST_FED_USER_ID);
        authSessionUserMap.put(SessionMgtConstants.AuthSessionUserKeys.IDP_ID, TEST_IDP_ID);
        authSessionUserMap.put(SessionMgtConstants.AuthSessionUserKeys.IDP_NAME, TEST_IDP_NAME);
    }

    @AfterClass
    public void tearDown() {

        frameworkServiceDataHolderMockedStatic.close();
        userSessionStoreMockedStatic.close();
        identityUtilMockedStatic.close();
    }

    @Test
    public void testAddAssociatedAssociatedLocalUserIdSessionsWithFilterDisabled() throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("false");
        List<UserSession> userSessions = new ArrayList<>();
        userSessions.add(createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));
        setupFederatedUserSessionMocks(TEST_SESSION_ID_2, TEST_FED_USER_ID);

        Method method = getAddAssociatedAssociatedLocalUserIdSessionsMethod();
        executeWithCommonMocks(method, userSessions, TEST_FED_USER_ID, TEST_SESSION_ID_2);

        assertEquals(userSessions.size(), 2);
        assertTrue(userSessions.stream().anyMatch(session -> TEST_SESSION_ID_2.equals(session.getSessionId())));
    }

    @Test
    public void testAddAssociatedAssociatedLocalUserIdSessionsWithFilterEnabled() throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("true");
        List<UserSession> userSessions = new ArrayList<>();
        userSessions.add(createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));
        setupFederatedUserSessionMocks(TEST_SESSION_ID_2, TEST_FED_USER_ID);

        Method method = getAddAssociatedAssociatedLocalUserIdSessionsMethod();
        executeWithCommonMocks(method, userSessions, TEST_FED_USER_ID, TEST_SESSION_ID_2);

        assertEquals(userSessions.size(), 2);
        UserSession actualSession = userSessions.stream()
                .filter(session -> TEST_SESSION_ID_2.equals(session.getSessionId()))
                .findFirst()
                .orElse(null);
        assertNotNull(actualSession);
    }

    @Test
    public void testAddAssociatedAssociatedLocalUserIdSessionsWithDuplicateSessionIdsWithFilterDisabled()
            throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("false");
        List<UserSession> userSessions = new ArrayList<>();
        userSessions.add(createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));
        setupFederatedUserSessionMocks(TEST_SESSION_ID_1, TEST_FED_USER_ID);

        Method method = getAddAssociatedAssociatedLocalUserIdSessionsMethod();
        executeWithCommonMocks(method, userSessions, TEST_FED_USER_ID, TEST_SESSION_ID_1);

        assertEquals(userSessions.size(), 2);
    }

    @Test
    public void testAddAssociatedAssociatedLocalUserIdSessionsWithDuplicateSessionIdsWithFilterEnabled()
            throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("true");
        List<UserSession> userSessions = new ArrayList<>();
        userSessions.add(createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));
        setupFederatedUserSessionMocks(TEST_SESSION_ID_1, TEST_FED_USER_ID);

        Method method = getAddAssociatedAssociatedLocalUserIdSessionsMethod();
        executeWithCommonMocks(method, userSessions, TEST_FED_USER_ID, TEST_SESSION_ID_1);

        assertEquals(userSessions.size(), 1);
        assertEquals(userSessions.get(0).getSessionId(), TEST_SESSION_ID_1);
    }

    @Test
    public void testAddAssociatedAssociatedLocalUserIdSessionsWithEmptyAuthSessionUserMap() throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("false");
        List<UserSession> userSessions = new ArrayList<>();
        userSessions.add(createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));
        setupFederatedUserSessionMocks(TEST_SESSION_ID_2, TEST_FED_USER_ID);
        authSessionUserMap = new HashMap<>();

        Method method = getAddAssociatedAssociatedLocalUserIdSessionsMethod();
        executeWithCommonMocks(method, userSessions, TEST_FED_USER_ID, TEST_SESSION_ID_2);

        assertEquals(userSessions.size(), 2);
        assertTrue(userSessions.stream().anyMatch(session -> TEST_SESSION_ID_2.equals(session.getSessionId())));
    }

    /**
     * A bounded session lookup must read only as many session IDs as it needs, and must never fall back to the
     * unbounded lookup while it can establish the answer from the bounded read. The unbounded lookup performs one
     * seek into the session store per session ever mapped to the user, so issuing it on the login path makes every
     * login of an account that accumulates sessions progressively more expensive.
     */
    @Test
    public void testGetBoundedActiveSessionListReadsOnlyWhatIsNeeded() throws Exception {

        int limit = 2;
        String userId = "bounded-lookup-user";
        // Fewer candidate session IDs than the candidate window, so every active session of the user was read.
        List<String> candidateSessionIds = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            candidateSessionIds.add("bounded-session-" + i);
        }
        when(userSessionStore.getActiveSessionIds(userId, 10)).thenReturn(candidateSessionIds);

        List<UserSession> userSessions = invokeGetBoundedActiveSessionList(userId, candidateSessionIds, limit);

        assertEquals(userSessions.size(), limit, "No more sessions than the given limit should be resolved.");
        verify(userSessionStore, never()).getActiveSessionIds(userId);
    }

    /**
     * When the candidate window is exhausted without resolving the requested number of sessions, the bounded lookup
     * must fall back to the unbounded one so that the returned set stays complete.
     */
    @Test
    public void testGetBoundedActiveSessionListFallsBackWhenCandidateWindowExhausted() throws Exception {

        int limit = 2;
        String userId = "exhausted-window-user";
        // A full candidate window of session IDs, none of which resolves to a session.
        List<String> candidateSessionIds = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            candidateSessionIds.add("unresolvable-session-" + i);
        }
        when(userSessionStore.getActiveSessionIds(userId, 10)).thenReturn(candidateSessionIds);
        when(userSessionStore.getActiveSessionIds(userId)).thenReturn(candidateSessionIds);

        List<UserSession> userSessions = invokeGetBoundedActiveSessionList(userId, new ArrayList<>(), limit);

        assertTrue(userSessions.isEmpty(), "No session should be resolved when none of the candidates hydrates.");
        verify(userSessionStore).getActiveSessionIds(userId);
    }

    /**
     * The bounded lookup is a default method, so an implementation that does not override it must still honour the
     * "at most limit" contract rather than returning the unbounded result unchanged.
     */
    @Test
    public void testDefaultBoundedLookupTruncatesToLimit() throws Exception {

        List<UserSession> allSessions = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            allSessions.add(createTestUserSession("default-session-" + i, TEST_USER_ID));
        }
        UserSessionManagementService serviceWithoutOverride = new UserSessionManagementService() {

            @Override
            public void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) {

            }

            @Override
            public List<UserSession> getSessionsByUserId(String userId, String tenantDomain) {

                return allSessions;
            }
        };

        List<UserSession> bounded =
                serviceWithoutOverride.getSessionsByUserId(TEST_USER_ID, TEST_TENANT_DOMAIN, 2);

        assertEquals(bounded.size(), 2, "The default implementation should truncate to the given limit.");
        assertEquals(allSessions.size(), 5, "The default implementation should not modify the resolved list.");
        assertEquals(serviceWithoutOverride.getSessionsByUserId(TEST_USER_ID, TEST_TENANT_DOMAIN, 10).size(), 5,
                "A limit above the number of sessions should return all of them.");
    }

    /**
     * The bounded lookup cannot bound anything for a limit of zero or less, so it must be rejected rather than
     * silently treated as unbounded.
     */
    @Test(expectedExceptions = SessionManagementClientException.class)
    public void testBoundedLookupRejectsInvalidLimit() throws Exception {

        new UserSessionManagementService() {

            @Override
            public void terminateSessionsOfUser(String username, String userStoreDomain, String tenantDomain) {

            }

            @Override
            public List<UserSession> getSessionsByUserId(String userId, String tenantDomain) {

                return new ArrayList<>();
            }
        }.getSessionsByUserId(TEST_USER_ID, TEST_TENANT_DOMAIN, 0);
    }

    /**
     * Invokes the private bounded session lookup with the session contexts and session records of
     * {@code resolvableSessionIds} mocked as available.
     */
    private List<UserSession> invokeGetBoundedActiveSessionList(String userId, List<String> resolvableSessionIds,
                                                                int limit) throws Exception {

        Method method = UserSessionManagementServiceImpl.class.getDeclaredMethod("getBoundedActiveSessionList",
                String.class, String.class, String.class, int.class);
        method.setAccessible(true);

        SessionContext mockedSessionContext = mock(SessionContext.class);
        when(mockedSessionContext.getProperties()).thenReturn(new HashMap<>());

        try (MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class);
             MockedConstruction<UserSessionDAOImpl> userSessionDAOConstruction =
                     mockConstruction(UserSessionDAOImpl.class, (mock, context) -> {
                         for (String sessionId : resolvableSessionIds) {
                             when(mock.getSession(sessionId))
                                     .thenReturn(createTestUserSession(sessionId, userId));
                         }
                     })) {

            frameworkUtilsMockedStatic.when(FrameworkUtils::getLoginTenantDomainFromContext).thenReturn("carbon.super");
            for (String sessionId : resolvableSessionIds) {
                frameworkUtilsMockedStatic.when(() -> FrameworkUtils.getSessionContextFromCache(sessionId,
                        "carbon.super")).thenReturn(mockedSessionContext);
            }
            return (List<UserSession>) method.invoke(userSessionManagementService, userId, null, null, limit);
        }
    }

    private UserSession createTestUserSession(String sessionId, String userId) {

        UserSession userSession = new UserSession();
        userSession.setUserId(userId);
        userSession.setSessionId(sessionId);
        
        List<Application> applications = new ArrayList<>();
        Application app = new Application("subject", "Test App", "1");
        applications.add(app);
        userSession.setApplications(applications);
        
        return userSession;
    }

    private void setupFederatedUserSessionMocks(String sessionId, String userId) throws Exception {

        List<String> fedUserSessionIds = new ArrayList<>();
        fedUserSessionIds.add(sessionId);
        when(userSessionStore.getActiveSessionIds(userId)).thenReturn(fedUserSessionIds);
    }

    private Method getAddAssociatedAssociatedLocalUserIdSessionsMethod() throws Exception {

        Method method = UserSessionManagementServiceImpl.class.getDeclaredMethod(
                "addAssociatedAssociatedLocalUserIdSessions", List.class, String.class);
        method.setAccessible(true);
        return method;
    }

    private void executeWithCommonMocks(Method method, List<UserSession> userSessions, String fedUserId,
                                        String sessionId) throws Exception {

        UserSession fedUserSession = createTestUserSession(sessionId, fedUserId);

        // Mock session context
        SessionContext mockedSessionContext = mock(SessionContext.class);
        when(mockedSessionContext.getProperties()).thenReturn(new HashMap<>());

        try (MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class);
             MockedConstruction<UserSessionDAOImpl> userSessionDAOConstruction =
                     mockConstruction(UserSessionDAOImpl.class,
                             (mock, context) -> when(mock.getSession(sessionId)).thenReturn(fedUserSession))) {

            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.getSessionContextFromCache(sessionId,
                    "carbon.super")).thenReturn(mockedSessionContext);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getLoginTenantDomainFromContext).thenReturn("carbon.super");
            method.invoke(userSessionManagementService, userSessions, fedUserId);
        }
    }
}
