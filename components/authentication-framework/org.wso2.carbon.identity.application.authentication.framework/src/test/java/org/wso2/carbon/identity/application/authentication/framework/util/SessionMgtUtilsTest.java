/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.application.authentication.framework.dao.impl.AuthUserDAO;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.Application;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ApplicationBasicInfo;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Unit tests for the session store selection and the session reference data reads of
 * {@link SessionMgtUtils}.
 */
public class SessionMgtUtilsTest {

    private static final String SESSION_STORAGE_TYPE_PROPERTY = "SessionStorage.Type";
    private static final String APP_FILTER_TENANT_CONDITION = " AND (TENANT_ID = ? OR IS_SAAS_APP = 1)";

    @AfterMethod
    public void resetApplicationManagementService() {

        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(null);
    }

    @DataProvider(name = "configuredStoreNames")
    public Object[][] configuredStoreNames() {

        return new Object[][]{
                // Configured value, expected store name.
                {null, SessionMgtUtils.DEFAULT_SESSION_STORE_NAME},
                {"", SessionMgtUtils.DEFAULT_SESSION_STORE_NAME},
                {"   ", SessionMgtUtils.DEFAULT_SESSION_STORE_NAME},
                {"jdbc", SessionMgtUtils.DEFAULT_SESSION_STORE_NAME},
                {"redis", "redis"},
                // The configured name is matched case insensitively, and is padded in a hand written config.
                {"Redis", "redis"},
                {"REDIS", "redis"},
                {"  Redis  ", "redis"},
        };
    }

    /**
     * An unset or blank configuration must resolve to the relational store, so that a deployment that
     * does not configure the session storage keeps its previous behaviour. A configured name is
     * normalized to lower case, and surrounding whitespace is dropped.
     */
    @Test(dataProvider = "configuredStoreNames")
    public void testGetConfiguredSessionStoreName(String configuredValue, String expected) {

        try (MockedStatic<IdentityUtil> identityUtil = mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(SESSION_STORAGE_TYPE_PROPERTY))
                    .thenReturn(configuredValue);

            assertEquals(SessionMgtUtils.getConfiguredSessionStoreName(), expected,
                    "Unexpected store name resolved for the configured value: " + configuredValue);
        }
    }

    /**
     * The default store name is the relational one, since it is what the selection falls back to.
     */
    @Test
    public void testDefaultSessionStoreNameIsJdbc() {

        assertEquals(SessionMgtUtils.DEFAULT_SESSION_STORE_NAME, "jdbc");
    }

    /**
     * The session storage properties are the ones the config parser read, handed over as they are: a
     * store names its own properties, and none of them is interpreted by the framework.
     */
    @Test
    public void testGetSessionStoragePropertiesArePassedThrough() {

        Map<String, String> configured = new HashMap<>();
        configured.put("hosts", "redis-1:6379");
        configured.put("connection.timeout.millis", "2000");

        try (MockedStatic<IdentityConfigParser> configParser = mockStatic(IdentityConfigParser.class)) {
            configParser.when(IdentityConfigParser::getSessionStorageProperties).thenReturn(configured);

            Map<String, String> properties = SessionMgtUtils.getSessionStorageProperties();

            assertEquals(properties.size(), 2);
            assertEquals(properties.get("hosts"), "redis-1:6379");
            assertEquals(properties.get("connection.timeout.millis"), "2000",
                    "A property name carrying a dot should be kept as it is written");
        }
    }

    /**
     * Applications are read through the application management service and keyed by their string
     * encoded numeric identifier, which is how the session records refer to them.
     */
    @Test
    public void testGetApplicationsByIds() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfosByIds(any(int[].class)))
                .thenReturn(Arrays.asList(applicationInfo(1, "My Account", "resource-1"),
                        applicationInfo(2, "Console", "resource-2")));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        Map<String, Application> applications = SessionMgtUtils.getApplicationsByIds(
                new HashSet<>(Arrays.asList("1", "2")));

        assertEquals(applications.size(), 2);
        assertEquals(applications.get("1").getAppName(), "My Account");
        assertEquals(applications.get("1").getResourceId(), "resource-1");
        assertEquals(applications.get("1").getAppId(), "1");
        assertNull(applications.get("1").getSubject(), "The subject is not part of an application record");
        assertEquals(applications.get("2").getAppName(), "Console");

        ArgumentCaptor<int[]> idsCaptor = ArgumentCaptor.forClass(int[].class);
        verify(applicationManagementService).getApplicationBasicInfosByIds(idsCaptor.capture());
        int[] queriedIds = idsCaptor.getValue();
        Arrays.sort(queriedIds);
        assertEquals(queriedIds, new int[]{1, 2}, "The identifiers should be passed through as numbers");
    }

    /**
     * An identifier with no application record is left out rather than mapped to an empty application,
     * so that a caller can tell a deleted application from an existing one.
     */
    @Test
    public void testGetApplicationsByIdsLeavesOutTheIdsWithoutARecord() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfosByIds(any(int[].class)))
                .thenReturn(Collections.singletonList(applicationInfo(1, "My Account", "resource-1")));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        Map<String, Application> applications = SessionMgtUtils.getApplicationsByIds(
                new HashSet<>(Arrays.asList("1", "2")));

        assertEquals(applications.size(), 1);
        assertTrue(applications.containsKey("1"));
    }

    /**
     * No identifier means no query: the application management service should not be called at all.
     */
    @Test
    public void testGetApplicationsByIdsWithoutIdsDoesNotQuery() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        assertTrue(SessionMgtUtils.getApplicationsByIds(null).isEmpty());
        assertTrue(SessionMgtUtils.getApplicationsByIds(Collections.emptySet()).isEmpty());

        verify(applicationManagementService, never()).getApplicationBasicInfosByIds(any(int[].class));
    }

    /**
     * A session record holding a non numeric application identifier is corrupt rather than merely
     * unmatched, so the read fails instead of silently returning fewer applications.
     */
    @Test
    public void testGetApplicationsByIdsRejectsANonNumericId() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        Set<String> appIds = new HashSet<>(Collections.singletonList("not-a-number"));
        try {
            SessionMgtUtils.getApplicationsByIds(appIds);
            fail("Expected a DataAccessException for a non numeric application identifier");
        } catch (DataAccessException e) {
            assertTrue(e.getMessage().contains("Invalid application ID"), "Unexpected message: " + e.getMessage());
        }
        verify(applicationManagementService, never()).getApplicationBasicInfosByIds(any(int[].class));
    }

    /**
     * A failure of the application management service is reported as a data access failure, which is
     * what the session reads of the callers are declared to throw.
     */
    @Test
    public void testGetApplicationsByIdsWrapsAnApplicationManagementFailure() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfosByIds(any(int[].class)))
                .thenThrow(new IdentityApplicationManagementException("Read failed."));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        try {
            SessionMgtUtils.getApplicationsByIds(new HashSet<>(Collections.singletonList("1")));
            fail("Expected a DataAccessException when the application management service fails");
        } catch (DataAccessException e) {
            assertTrue(e.getCause() instanceof IdentityApplicationManagementException);
        }
    }

    /**
     * The name and the resource identifier are filled in from the application records, and an
     * application whose record is gone is dropped from the session rather than listed without a name.
     */
    @Test
    public void testSetApplicationDetails() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfosByIds(any(int[].class)))
                .thenReturn(Collections.singletonList(applicationInfo(1, "My Account", "resource-1")));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        List<Application> applications = new ArrayList<>(Arrays.asList(
                new Application("subject-1", null, "1"),
                new Application("subject-2", null, "2")));

        SessionMgtUtils.setApplicationDetails(applications);

        assertEquals(applications.size(), 1, "The application without a record should have been removed");
        assertEquals(applications.get(0).getAppId(), "1");
        assertEquals(applications.get(0).getAppName(), "My Account");
        assertEquals(applications.get(0).getResourceId(), "resource-1");
        assertEquals(applications.get(0).getSubject(), "subject-1", "The subject of the session should be kept");
    }

    /**
     * Nothing to complete means no query and no failure, including for a null list.
     */
    @Test
    public void testSetApplicationDetailsWithoutApplicationsDoesNotQuery() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        SessionMgtUtils.setApplicationDetails(null);
        List<Application> empty = new ArrayList<>();
        SessionMgtUtils.setApplicationDetails(empty);

        assertTrue(empty.isEmpty());
        verify(applicationManagementService, never()).getApplicationBasicInfosByIds(any(int[].class));
    }

    /**
     * The application filter of a session search is widened with the tenant condition, so that both the
     * applications of the tenant and the SaaS applications are matched, and the tenant identifier is
     * bound last, after the values of the filter itself.
     */
    @Test
    public void testGetApplicationsByFilterAppendsTheTenantCondition() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfos(anyString(), anyList()))
                .thenReturn(Collections.singletonList(applicationInfo(7, "My Account", "resource-7")));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        SessionFilterQueryBuilder filterBuilder = new SessionFilterQueryBuilder();
        filterBuilder.setFilterQuery(SessionMgtConstants.FilterType.APPLICATION, "WHERE APP_NAME LIKE ? ESCAPE '\\'");
        filterBuilder.addFilterParam(SessionMgtConstants.FilterType.APPLICATION, "my%");

        Map<String, Application> applications = SessionMgtUtils.getApplicationsByFilter(filterBuilder, 3);

        assertEquals(applications.size(), 1);
        assertEquals(applications.get("7").getAppName(), "My Account");
        assertEquals(applications.get("7").getResourceId(), "resource-7");

        ArgumentCaptor<String> clauseCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Object>> paramsCaptor = ArgumentCaptor.forClass(List.class);
        verify(applicationManagementService).getApplicationBasicInfos(clauseCaptor.capture(), paramsCaptor.capture());
        assertEquals(clauseCaptor.getValue(), "WHERE APP_NAME LIKE ? ESCAPE '\\'" + APP_FILTER_TENANT_CONDITION);
        assertEquals(paramsCaptor.getValue(), Arrays.asList("my%", 3),
                "The tenant identifier should be bound after the values of the filter");
    }

    /**
     * A search without an application filter still matches only the applications the tenant can see, so
     * the tenant condition is the whole clause.
     */
    @Test
    public void testGetApplicationsByFilterWithoutAFilterStillScopesToTheTenant() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfos(anyString(), anyList()))
                .thenReturn(Collections.emptyList());
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        SessionFilterQueryBuilder filterBuilder = new SessionFilterQueryBuilder();
        filterBuilder.setFilterQuery(SessionMgtConstants.FilterType.APPLICATION, "");

        assertTrue(SessionMgtUtils.getApplicationsByFilter(filterBuilder, 3).isEmpty());

        ArgumentCaptor<String> clauseCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List<Object>> paramsCaptor = ArgumentCaptor.forClass(List.class);
        verify(applicationManagementService).getApplicationBasicInfos(clauseCaptor.capture(), paramsCaptor.capture());
        assertEquals(clauseCaptor.getValue(), APP_FILTER_TENANT_CONDITION);
        assertEquals(paramsCaptor.getValue(), Collections.singletonList(3));
    }

    /**
     * A failure of the application filter read is reported as a data access failure too.
     */
    @Test
    public void testGetApplicationsByFilterWrapsAnApplicationManagementFailure() throws Exception {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getApplicationBasicInfos(anyString(), anyList()))
                .thenThrow(new IdentityApplicationManagementException("Read failed."));
        FrameworkServiceDataHolder.getInstance().setApplicationManagementService(applicationManagementService);

        SessionFilterQueryBuilder filterBuilder = new SessionFilterQueryBuilder();
        filterBuilder.setFilterQuery(SessionMgtConstants.FilterType.APPLICATION, "");

        try {
            SessionMgtUtils.getApplicationsByFilter(filterBuilder, 3);
            fail("Expected a DataAccessException when the application management service fails");
        } catch (DataAccessException e) {
            assertTrue(e.getCause() instanceof IdentityApplicationManagementException);
        }
    }

    /**
     * The identity providers of the session users are read through the authentication user DAO, which
     * stays relational whatever the configured session store is.
     */
    @Test
    public void testGetIdpIdsByUserIds() throws Exception {

        Map<String, String> idpIds = new HashMap<>();
        idpIds.put("user-1", "1");
        idpIds.put("user-2", "2");

        AuthUserDAO authUserDAO = mock(AuthUserDAO.class);
        when(authUserDAO.getIdpIdsByUserIds(anyList())).thenReturn(idpIds);

        try (MockedStatic<AuthUserDAO> authUserDAOStatic = mockStatic(AuthUserDAO.class)) {
            authUserDAOStatic.when(AuthUserDAO::getInstance).thenReturn(authUserDAO);

            Map<String, String> resolved = SessionMgtUtils.getIdpIdsByUserIds(
                    new HashSet<>(Arrays.asList("user-1", "user-2")));

            assertEquals(resolved, idpIds);
        }
    }

    /**
     * No user means no query, and an empty result rather than a failure.
     */
    @Test
    public void testGetIdpIdsByUserIdsWithoutUsersDoesNotQuery() throws Exception {

        AuthUserDAO authUserDAO = mock(AuthUserDAO.class);

        try (MockedStatic<AuthUserDAO> authUserDAOStatic = mockStatic(AuthUserDAO.class)) {
            authUserDAOStatic.when(AuthUserDAO::getInstance).thenReturn(authUserDAO);

            assertTrue(SessionMgtUtils.getIdpIdsByUserIds(null).isEmpty());
            assertTrue(SessionMgtUtils.getIdpIdsByUserIds(Collections.emptySet()).isEmpty());

            verify(authUserDAO, never()).getIdpIdsByUserIds(anyList());
        }
    }

    /**
     * A failure of the user DAO is reported as a data access failure, as the application reads are.
     */
    @Test
    public void testGetIdpIdsByUserIdsWrapsAUserSessionFailure() throws Exception {

        AuthUserDAO authUserDAO = mock(AuthUserDAO.class);
        when(authUserDAO.getIdpIdsByUserIds(anyList())).thenThrow(new UserSessionException("Read failed."));

        try (MockedStatic<AuthUserDAO> authUserDAOStatic = mockStatic(AuthUserDAO.class)) {
            authUserDAOStatic.when(AuthUserDAO::getInstance).thenReturn(authUserDAO);

            SessionMgtUtils.getIdpIdsByUserIds(new HashSet<>(Collections.singletonList("user-1")));
            fail("Expected a DataAccessException when the user DAO fails");
        } catch (DataAccessException e) {
            assertTrue(e.getCause() instanceof UserSessionException);
        }
    }

    private static ApplicationBasicInfo applicationInfo(int applicationId, String name, String resourceId) {

        ApplicationBasicInfo applicationBasicInfo = new ApplicationBasicInfo();
        applicationBasicInfo.setApplicationId(applicationId);
        applicationBasicInfo.setApplicationName(name);
        applicationBasicInfo.setApplicationResourceId(resourceId);
        return applicationBasicInfo;
    }
}
