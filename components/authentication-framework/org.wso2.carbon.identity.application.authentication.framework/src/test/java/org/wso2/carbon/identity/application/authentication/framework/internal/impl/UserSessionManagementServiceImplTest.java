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

package org.wso2.carbon.identity.application.authentication.framework.internal.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.UserSessionDAOImpl;
import org.wso2.carbon.identity.application.authentication.framework.exception.session.mgt.SessionManagementClientException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.internal.util.SessionEventPublishingUtil;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.authentication.framework.model.UserSession;
import org.wso2.carbon.identity.application.authentication.framework.services.SessionManagementService;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.authentication.framework.util.SessionMgtConstants;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.profile.mgt.AssociatedAccountDTO;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CURRENT_SESSION_IDENTIFIER;

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

    private UserSessionManagementServiceImpl userSessionManagementService;
    private MockedStatic<FrameworkServiceDataHolder> frameworkServiceDataHolderMockedStatic;
    private MockedStatic<UserSessionStore> userSessionStoreMockedStatic;
    private MockedStatic<IdentityUtil> identityUtilMockedStatic;

    private static final String TEST_TENANT_DOMAIN = "test-tenant.com";
    private static final int TEST_TENANT_ID = 1;
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_FED_USER_ID = "test-fed-user-id";
    private static final String TEST_SESSION_ID_1 = "test-session-id-1";
    private static final String TEST_SESSION_ID_2 = "test-session-id-2";
    private static final String TEST_IDP_ID = "1";
    private static final String TEST_IDP_NAME = "LOCAL";
    private static final String TEST_USERNAME = "PRIMARY/testuser";
    private static final String TEST_FED_SESSION_ID = "test-fed-session-id";
    private static final String TEST_LOCAL_SESSION_ID = "test-local-session-id";
    private static final String TEST_SHARED_SESSION_ID = "test-shared-session-id";

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
    }

    @BeforeMethod
    public void resetSharedMocks() {

        /* UserSessionStore is a class level mock shared by all the tests. It is reset before each test, so that the
           stubbing and the invocation counts of one test do not leak into another. */
        reset(userSessionStore);
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
    public void testTerminateSessionsByUserIdTerminatesBothFederatedAndLocalSessions() throws Exception {

        /* The user has a federated association. One session was created through federated authentication and is
           mapped to the federated user id, while another session was created through local authentication and is
           mapped to the local user id. Both of them must be terminated. */
        TerminationResult result = invokeTerminateSessionsByUserId(
                Collections.singletonList(TEST_FED_SESSION_ID),
                Collections.singletonList(TEST_LOCAL_SESSION_ID), false, null);

        /* The sessions must be resolved for the local user id which was passed in, so that getSessionsByUserId
           looks up the resolved federated user id first and then appends the sessions of the associated local
           user id. */
        assertEquals(result.resolvedUserIds, Arrays.asList(TEST_FED_USER_ID, TEST_USER_ID));

        assertEquals(result.terminatedSessionIds.size(), 2);
        assertTrue(result.terminatedSessionIds.contains(TEST_FED_SESSION_ID));
        assertTrue(result.terminatedSessionIds.contains(TEST_LOCAL_SESSION_ID));
        assertEquals(result.removedRecordSessionIds, result.terminatedSessionIds);
        assertEquals(result.publishedSessionIds, result.terminatedSessionIds);
    }

    @Test
    public void testTerminateSessionsByUserIdTerminatesSharedSessionOnlyOnce() throws Exception {

        /* A single session can be mapped to both the local user entry and the federated user entry. Such a session
           is resolved twice by getSessionsByUserId, but it must be terminated only once. */
        TerminationResult result = invokeTerminateSessionsByUserId(
                Collections.singletonList(TEST_SHARED_SESSION_ID),
                Collections.singletonList(TEST_SHARED_SESSION_ID), false, null);

        assertEquals(result.terminatedSessionIds, Collections.singletonList(TEST_SHARED_SESSION_ID));
        assertEquals(result.removedRecordSessionIds, Collections.singletonList(TEST_SHARED_SESSION_ID));
        assertEquals(result.publishedSessionIds, Collections.singletonList(TEST_SHARED_SESSION_ID));
    }

    @Test
    public void testTerminateSessionsByUserIdPreservesCurrentSessionResolvedTwice() throws Exception {

        /* The current session is mapped to both the local user entry and the federated user entry. It must be
           preserved even though it is resolved twice, while the other session is still terminated. */
        TerminationResult result = invokeTerminateSessionsByUserId(
                Arrays.asList(TEST_SHARED_SESSION_ID, TEST_FED_SESSION_ID),
                Collections.singletonList(TEST_SHARED_SESSION_ID), true, TEST_SHARED_SESSION_ID);

        assertFalse(result.terminatedSessionIds.contains(TEST_SHARED_SESSION_ID));
        assertEquals(result.terminatedSessionIds, Collections.singletonList(TEST_FED_SESSION_ID));
        assertEquals(result.removedRecordSessionIds, Collections.singletonList(TEST_FED_SESSION_ID));
        assertEquals(result.publishedSessionIds, Collections.singletonList(TEST_FED_SESSION_ID));
    }

    @Test
    public void testTerminateSessionsByUserIdTerminatesSessionsOfUserWithoutFederatedAssociation() throws Exception {

        /* A user without any federated association must still have the sessions resolved and terminated, looking
           the user id up exactly once. */
        TerminationResult result = invokeTerminateSessionsByUserId(Collections.emptyList(),
                Arrays.asList(TEST_SESSION_ID_1, TEST_SESSION_ID_2), false, null, false);

        assertEquals(result.resolvedUserIds, Collections.singletonList(TEST_USER_ID));
        assertEquals(result.terminatedSessionIds, Arrays.asList(TEST_SESSION_ID_1, TEST_SESSION_ID_2));
        assertEquals(result.removedRecordSessionIds, Arrays.asList(TEST_SESSION_ID_1, TEST_SESSION_ID_2));
        assertEquals(result.publishedSessionIds, Arrays.asList(TEST_SESSION_ID_1, TEST_SESSION_ID_2));
    }

    @Test
    public void testTerminateSessionsByUserIdWithNoActiveSessions() throws Exception {

        /* When the user has no active session, nothing is terminated and the terminated session records are not
           cleaned up, since the resolved session id list is empty. */
        TerminationResult result = invokeTerminateSessionsByUserId(Collections.emptyList(),
                Collections.emptyList(), false, null, true);

        assertTrue(result.terminatedSessionIds.isEmpty());
        assertFalse(result.isRemoveTerminatedSessionRecordsInvoked);
        assertTrue(result.publishedSessionIds.isEmpty());
    }

    @Test
    public void testTerminateSessionsByUserIdPreservesCurrentSessionWhenNoCurrentSessionIsSet() throws Exception {

        /* Preserving is enabled but no current session is set in the thread local, so every resolved session is
           terminated. */
        TerminationResult result = invokeTerminateSessionsByUserId(
                Collections.singletonList(TEST_FED_SESSION_ID),
                Collections.singletonList(TEST_LOCAL_SESSION_ID), true, null, true);

        assertEquals(result.terminatedSessionIds.size(), 2);
        assertTrue(result.terminatedSessionIds.contains(TEST_FED_SESSION_ID));
        assertTrue(result.terminatedSessionIds.contains(TEST_LOCAL_SESSION_ID));
    }

    @Test
    public void testGetSessionBySessionIdReturnsSessionMappedToFederatedUserId() throws Exception {

        /* The session is mapped to the resolved federated user id, so it is returned from that lookup itself and
           the local user id is never looked up. */
        Map<String, UserSession> sessionByUserId = new HashMap<>();
        sessionByUserId.put(TEST_FED_USER_ID, createTestUserSession(TEST_SESSION_ID_1, TEST_FED_USER_ID));

        SessionLookupResult result = invokeGetSessionBySessionId(sessionByUserId, true);

        assertTrue(result.userSession.isPresent());
        assertEquals(result.userSession.get().getSessionId(), TEST_SESSION_ID_1);
        assertEquals(result.userSession.get().getIdpName(), TEST_IDP_NAME);
        assertEquals(result.queriedUserIds, Collections.singletonList(TEST_FED_USER_ID));
    }

    @Test
    public void testGetSessionBySessionIdFallsBackToLocalUserIdWhenNotMappedToFederatedUserId() throws Exception {

        /* The user has a federated association, but the session was created through local authentication and is
           mapped to the local user id. It must still be resolved, by falling back to the local user id. */
        Map<String, UserSession> sessionByUserId = new HashMap<>();
        sessionByUserId.put(TEST_USER_ID, createTestUserSession(TEST_SESSION_ID_1, TEST_USER_ID));

        SessionLookupResult result = invokeGetSessionBySessionId(sessionByUserId, true);

        assertTrue(result.userSession.isPresent());
        assertEquals(result.userSession.get().getSessionId(), TEST_SESSION_ID_1);
        assertEquals(result.userSession.get().getUserId(), TEST_USER_ID);
        /* The federated user id is looked up first and the local user id only afterwards, as the fallback. */
        assertEquals(result.queriedUserIds, Arrays.asList(TEST_FED_USER_ID, TEST_USER_ID));
    }

    @Test
    public void testGetSessionBySessionIdReturnsEmptyWhenMappedToNeitherUserId() throws Exception {

        /* The session belongs to neither the federated user id nor the local user id, so both lookups are made and
           nothing is returned. */
        SessionLookupResult result = invokeGetSessionBySessionId(new HashMap<>(), true);

        assertFalse(result.userSession.isPresent());
        assertEquals(result.queriedUserIds, Arrays.asList(TEST_FED_USER_ID, TEST_USER_ID));
    }

    @Test
    public void testGetSessionBySessionIdWithoutFederatedAssociationLooksUpLocalUserIdOnce() throws Exception {

        /* A user without a federated association must not trigger the fallback lookup, so the user id is queried
           exactly once even when the session is not found. */
        SessionLookupResult result = invokeGetSessionBySessionId(new HashMap<>(), false);

        assertFalse(result.userSession.isPresent());
        assertEquals(result.queriedUserIds, Collections.singletonList(TEST_USER_ID));
    }

    @Test
    public void testTerminateSessionBySessionIdTerminatesSessionMappedToFederatedUserId() throws Exception {

        /* The session is mapped to the resolved federated user id, so the ownership is established by the first
           check itself and the local user id is never checked. */
        SessionTerminationResult result = invokeTerminateSessionBySessionId(
                new HashSet<>(Collections.singletonList(TEST_FED_USER_ID)), true);

        assertTrue(result.isTerminated);
        assertEquals(result.mappingCheckedUserIds, Collections.singletonList(TEST_FED_USER_ID));
    }

    @Test
    public void testTerminateSessionBySessionIdTerminatesSessionMappedToLocalUserId() throws Exception {

        /* The user has a federated association, but the session is mapped to the local user id. The ownership must
           still be established, by evaluating the local user id as well. */
        SessionTerminationResult result = invokeTerminateSessionBySessionId(
                new HashSet<>(Collections.singletonList(TEST_USER_ID)), true);

        assertTrue(result.isTerminated);
        assertEquals(result.mappingCheckedUserIds, Arrays.asList(TEST_FED_USER_ID, TEST_USER_ID));
    }

    @Test
    public void testTerminateSessionBySessionIdThrowsForbiddenWhenSessionIsNotOwnedByUser() throws Exception {

        /* The session is mapped to neither user id, so both are checked and the termination is rejected. */
        SessionTerminationResult result = invokeTerminateSessionBySessionId(Collections.emptySet(), true);

        assertFalse(result.isTerminated);
        assertEquals(result.errorCode, SessionMgtConstants.ErrorMessages.ERROR_CODE_FORBIDDEN_ACTION.getCode());
        assertEquals(result.mappingCheckedUserIds, Arrays.asList(TEST_FED_USER_ID, TEST_USER_ID));
    }

    @Test
    public void testTerminateSessionBySessionIdWithoutFederatedAssociationChecksMappingOnce() throws Exception {

        /* For a user without a federated association the resolved user id is the user id itself, so the ownership
           must not be evaluated twice against the same user id. */
        SessionTerminationResult result = invokeTerminateSessionBySessionId(Collections.emptySet(), false);

        assertFalse(result.isTerminated);
        assertEquals(result.errorCode, SessionMgtConstants.ErrorMessages.ERROR_CODE_FORBIDDEN_ACTION.getCode());
        assertEquals(result.mappingCheckedUserIds, Collections.singletonList(TEST_USER_ID));
    }

    /**
     * Holder for what terminateSessionsByUserId did, captured while the static mocks are still open.
     */
    private static class TerminationResult {

        private List<String> resolvedUserIds = new ArrayList<>();
        private List<String> terminatedSessionIds = new ArrayList<>();
        private List<String> removedRecordSessionIds = new ArrayList<>();
        private List<String> publishedSessionIds = new ArrayList<>();
        private boolean isRemoveTerminatedSessionRecordsInvoked;
    }

    /**
     * Holder for what getSessionBySessionId resolved, captured while the static mocks are still open.
     */
    private static class SessionLookupResult {

        private Optional<UserSession> userSession = Optional.empty();
        private List<String> queriedUserIds = new ArrayList<>();
    }

    /**
     * Holder for what terminateSessionBySessionId did, captured while the static mocks are still open.
     */
    private static class SessionTerminationResult {

        private boolean isTerminated;
        private String errorCode;
        private List<String> mappingCheckedUserIds = new ArrayList<>();
    }

    /**
     * Runs terminateSessionsByUserId for a user who has a federated association, with the given sessions mapped to
     * the federated user id and to the local user id.
     *
     * @param fedUserSessionIds         Session ids mapped to the federated user id.
     * @param localUserSessionIds       Session ids mapped to the local user id.
     * @param isCurrentSessionPreserved Whether PreserveCurrentSessionAtPasswordUpdate is enabled.
     * @param currentSessionId          Session id to be set as the current session, when preserving is enabled.
     * @return What the termination actually did.
     */
    private TerminationResult invokeTerminateSessionsByUserId(List<String> fedUserSessionIds,
                                                              List<String> localUserSessionIds,
                                                              boolean isCurrentSessionPreserved,
                                                              String currentSessionId) throws Exception {

        return invokeTerminateSessionsByUserId(fedUserSessionIds, localUserSessionIds, isCurrentSessionPreserved,
                currentSessionId, true);
    }

    /**
     * Runs terminateSessionsByUserId with the given sessions mapped to the federated user id and to the local user
     * id.
     *
     * @param fedUserSessionIds         Session ids mapped to the federated user id.
     * @param localUserSessionIds       Session ids mapped to the local user id.
     * @param isCurrentSessionPreserved Whether PreserveCurrentSessionAtPasswordUpdate is enabled.
     * @param currentSessionId          Session id to be set as the current session, when preserving is enabled.
     * @param hasFederatedAssociation   Whether the user has a federated association.
     * @return What the termination actually did.
     */
    @SuppressWarnings("unchecked")
    private TerminationResult invokeTerminateSessionsByUserId(List<String> fedUserSessionIds,
                                                              List<String> localUserSessionIds,
                                                              boolean isCurrentSessionPreserved,
                                                              String currentSessionId,
                                                              boolean hasFederatedAssociation) throws Exception {

        identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(FrameworkConstants.FILER_BY_SESSION_ID_FOR_USER))
                .thenReturn("false");
        setupFederatedAssociationMocks(hasFederatedAssociation);
        when(userSessionStore.getActiveSessionIds(TEST_FED_USER_ID)).thenReturn(new ArrayList<>(fedUserSessionIds));
        when(userSessionStore.getActiveSessionIds(TEST_USER_ID)).thenReturn(new ArrayList<>(localUserSessionIds));

        SessionManagementService mockedSessionManagementService = mock(SessionManagementService.class);
        setSessionManagementService(mockedSessionManagementService);

        CarbonContext mockedCarbonContext = mock(CarbonContext.class);
        when(mockedCarbonContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);

        SessionContext mockedSessionContext = mock(SessionContext.class);
        when(mockedSessionContext.getProperties()).thenReturn(new HashMap<>());

        TerminationResult result = new TerminationResult();
        if (isCurrentSessionPreserved && currentSessionId != null) {
            IdentityUtil.threadLocalProperties.get().put(CURRENT_SESSION_IDENTIFIER, currentSessionId);
        }

        try (MockedStatic<CarbonContext> carbonContextMockedStatic = mockStatic(CarbonContext.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class);
             MockedStatic<IdPManagementUtil> idPManagementUtilMockedStatic = mockStatic(IdPManagementUtil.class);
             MockedStatic<SessionEventPublishingUtil> sessionEventPublishingUtilMockedStatic =
                     mockStatic(SessionEventPublishingUtil.class);
             MockedConstruction<UserSessionDAOImpl> userSessionDAOConstruction =
                     mockConstruction(UserSessionDAOImpl.class, (mock, context) ->
                             when(mock.getSessions(anyList())).thenAnswer(invocation -> {
                                 Map<String, UserSession> sessions = new HashMap<>();
                                 for (String sessionId : (List<String>) invocation.getArgument(0)) {
                                     sessions.put(sessionId, createTestUserSession(sessionId, TEST_USER_ID));
                                 }
                                 return sessions;
                             }))) {

            carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(mockedCarbonContext);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getLoginTenantDomainFromContext)
                    .thenReturn(TEST_TENANT_DOMAIN);
            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.getSessionContextFromCache(anyString(),
                    eq(TEST_TENANT_DOMAIN))).thenReturn(mockedSessionContext);
            idPManagementUtilMockedStatic.when(
                            () -> IdPManagementUtil.getPreserveCurrentSessionAtPasswordUpdate(TEST_TENANT_DOMAIN))
                    .thenReturn(isCurrentSessionPreserved);

            assertTrue(userSessionManagementService.terminateSessionsByUserId(TEST_USER_ID));

            ArgumentCaptor<String> resolvedUserIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(userSessionStore, atLeast(1)).getActiveSessionIds(resolvedUserIdCaptor.capture());
            result.resolvedUserIds = new ArrayList<>(resolvedUserIdCaptor.getAllValues());

            /* The mocked SessionManagementService is created per invocation, so atLeast(0) simply captures every
               session id that was actually terminated. */
            ArgumentCaptor<String> terminatedSessionIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockedSessionManagementService, atLeast(0)).removeSession(terminatedSessionIdCaptor.capture());
            result.terminatedSessionIds = new ArrayList<>(terminatedSessionIdCaptor.getAllValues());

            /* removeTerminatedSessionRecords is bypassed when nothing was resolved for termination, hence
               atLeast(0) is used to capture whether it was invoked at all. */
            ArgumentCaptor<List> removedRecordsCaptor = ArgumentCaptor.forClass(List.class);
            verify(userSessionStore, atLeast(0)).removeTerminatedSessionRecords(removedRecordsCaptor.capture());
            result.isRemoveTerminatedSessionRecordsInvoked = !removedRecordsCaptor.getAllValues().isEmpty();
            if (result.isRemoveTerminatedSessionRecordsInvoked) {
                result.removedRecordSessionIds = new ArrayList<>(removedRecordsCaptor.getValue());
            }

            ArgumentCaptor<List> publishedSessionIdsCaptor = ArgumentCaptor.forClass(List.class);
            sessionEventPublishingUtilMockedStatic.verify(() ->
                    SessionEventPublishingUtil.publishSessionTerminationEvent(eq(TEST_USER_ID),
                            publishedSessionIdsCaptor.capture()));
            result.publishedSessionIds = new ArrayList<>(publishedSessionIdsCaptor.getValue());
        } finally {
            IdentityUtil.threadLocalProperties.get().remove(CURRENT_SESSION_IDENTIFIER);
        }
        return result;
    }

    /**
     * Sets up the user store and the federated association lookups which the federated association resolution of
     * the service relies on.
     *
     * @param hasFederatedAssociation Whether the user has a federated association. When false, the resolution
     *                                falls through and the user id itself is used.
     */
    private void setupFederatedAssociationMocks(boolean hasFederatedAssociation) throws Exception {

        UserRealm mockedUserRealm = mock(UserRealm.class);
        AbstractUserStoreManager mockedUserStoreManager = mock(AbstractUserStoreManager.class);
        when(realmService.getTenantUserRealm(TEST_TENANT_ID)).thenReturn(mockedUserRealm);
        when(mockedUserRealm.getUserStoreManager()).thenReturn(mockedUserStoreManager);
        when(mockedUserStoreManager.getUserNameFromUserID(TEST_USER_ID)).thenReturn(TEST_USERNAME);

        if (!hasFederatedAssociation) {
            when(federatedAssociationManager.getFederatedAssociationsOfUser(anyInt(), anyString(), anyString()))
                    .thenReturn(Collections.emptyList());
            return;
        }

        AssociatedAccountDTO mockedAssociatedAccount = mock(AssociatedAccountDTO.class);
        when(mockedAssociatedAccount.getIdentityProviderId()).thenReturn(Integer.parseInt(TEST_IDP_ID));
        when(mockedAssociatedAccount.getIdentityProviderName()).thenReturn(TEST_IDP_NAME);
        when(federatedAssociationManager.getFederatedAssociationsOfUser(anyInt(), anyString(), anyString()))
                .thenReturn(Collections.singletonList(mockedAssociatedAccount));
        when(userSessionStore.getUserId(anyString(), anyInt(), isNull(), anyInt())).thenReturn(TEST_FED_USER_ID);
    }

    /**
     * Runs getSessionBySessionId for TEST_SESSION_ID_1, with the session mapped to the given user ids.
     *
     * @param sessionByUserId         Session to be returned by the DAO, keyed by the user id it is mapped to. A
     *                                user id which is absent from the map does not own the session.
     * @param hasFederatedAssociation Whether the user has a federated association.
     * @return The resolved session along with the user ids the DAO was queried with, in order.
     */
    private SessionLookupResult invokeGetSessionBySessionId(Map<String, UserSession> sessionByUserId,
                                                            boolean hasFederatedAssociation) throws Exception {

        setupFederatedAssociationMocks(hasFederatedAssociation);

        CarbonContext mockedCarbonContext = mock(CarbonContext.class);
        when(mockedCarbonContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
        SessionContext mockedSessionContext = mock(SessionContext.class);
        when(mockedSessionContext.getProperties()).thenReturn(new HashMap<>());

        SessionLookupResult result = new SessionLookupResult();
        try (MockedStatic<CarbonContext> carbonContextMockedStatic = mockStatic(CarbonContext.class);
             MockedStatic<FrameworkUtils> frameworkUtilsMockedStatic = mockStatic(FrameworkUtils.class);
             MockedConstruction<UserSessionDAOImpl> userSessionDAOConstruction =
                     mockConstruction(UserSessionDAOImpl.class, (mock, context) ->
                             when(mock.getSession(anyString(), eq(TEST_SESSION_ID_1))).thenAnswer(invocation -> {
                                 String queriedUserId = invocation.getArgument(0);
                                 result.queriedUserIds.add(queriedUserId);
                                 return Optional.ofNullable(sessionByUserId.get(queriedUserId));
                             }))) {

            carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(mockedCarbonContext);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getLoginTenantDomainFromContext)
                    .thenReturn(TEST_TENANT_DOMAIN);
            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.getSessionContextFromCache(eq(TEST_SESSION_ID_1),
                    eq(TEST_TENANT_DOMAIN))).thenReturn(mockedSessionContext);

            result.userSession = userSessionManagementService.getSessionBySessionId(TEST_USER_ID, TEST_SESSION_ID_1);
        }
        return result;
    }

    /**
     * Runs terminateSessionBySessionId for TEST_SESSION_ID_1, with the session owned by the given user ids.
     *
     * @param userIdsOwningSession    User ids which have a user session mapping for the session.
     * @param hasFederatedAssociation Whether the user has a federated association.
     * @return What the termination did, along with the user ids the ownership was evaluated against, in order.
     */
    private SessionTerminationResult invokeTerminateSessionBySessionId(Set<String> userIdsOwningSession,
                                                                       boolean hasFederatedAssociation)
            throws Exception {

        setupFederatedAssociationMocks(hasFederatedAssociation);

        SessionTerminationResult result = new SessionTerminationResult();
        when(userSessionStore.isExistingMapping(anyString(), eq(TEST_SESSION_ID_1))).thenAnswer(invocation -> {
            String checkedUserId = invocation.getArgument(0);
            result.mappingCheckedUserIds.add(checkedUserId);
            return userIdsOwningSession.contains(checkedUserId);
        });

        SessionManagementService mockedSessionManagementService = mock(SessionManagementService.class);
        setSessionManagementService(mockedSessionManagementService);

        CarbonContext mockedCarbonContext = mock(CarbonContext.class);
        when(mockedCarbonContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);

        try (MockedStatic<CarbonContext> carbonContextMockedStatic = mockStatic(CarbonContext.class);
             MockedStatic<SessionEventPublishingUtil> sessionEventPublishingUtilMockedStatic =
                     mockStatic(SessionEventPublishingUtil.class)) {

            carbonContextMockedStatic.when(CarbonContext::getThreadLocalCarbonContext)
                    .thenReturn(mockedCarbonContext);
            try {
                result.isTerminated = userSessionManagementService.terminateSessionBySessionId(TEST_USER_ID,
                        TEST_SESSION_ID_1);
            } catch (SessionManagementClientException e) {
                result.errorCode = e.getErrorCode();
            }

            if (result.isTerminated) {
                verify(mockedSessionManagementService).removeSession(TEST_SESSION_ID_1);
                verify(userSessionStore).removeTerminatedSessionRecords(
                        Collections.singletonList(TEST_SESSION_ID_1));
                sessionEventPublishingUtilMockedStatic.verify(() ->
                        SessionEventPublishingUtil.publishSessionTerminationEvent(TEST_USER_ID, TEST_SESSION_ID_1));
            } else {
                verify(mockedSessionManagementService, never()).removeSession(anyString());
                verify(userSessionStore, never()).removeTerminatedSessionRecords(anyList());
            }
        }
        return result;
    }

    private void setSessionManagementService(SessionManagementService sessionManagementService) throws Exception {

        Field field = UserSessionManagementServiceImpl.class.getDeclaredField("sessionManagementService");
        field.setAccessible(true);
        field.set(userSessionManagementService, sessionManagementService);
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
                             (mock, context) -> when(mock.getSessions(anyList()))
                                     .thenReturn(Collections.singletonMap(sessionId, fedUserSession)))) {

            frameworkUtilsMockedStatic.when(() -> FrameworkUtils.getSessionContextFromCache(sessionId,
                    "carbon.super")).thenReturn(mockedSessionContext);
            frameworkUtilsMockedStatic.when(FrameworkUtils::getLoginTenantDomainFromContext).thenReturn("carbon.super");
            method.invoke(userSessionManagementService, userSessions, fedUserId);
        }
    }
}
