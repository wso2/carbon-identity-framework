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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;

/**
 * Unit tests for FlowContextStoreDAOImpl.
 */
public class FlowContextStoreDAOImplTest {

    private static final String CONTEXT_ID = "test-context-id";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final long TTL_SECONDS = 900L;
    private static final int CLEANUP_LIMIT = 1000;

    private FlowContextStoreDAOImpl flowContextStoreDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private MockedStatic<JdbcUtils> jdbcUtils;
    private MockedStatic<FlowExecutionEngineUtils> flowEngineUtils;

    @BeforeMethod
    public void setup() throws Exception {

        MockitoAnnotations.openMocks(this);
        flowContextStoreDAO = new FlowContextStoreDAOImpl();

        jdbcUtils = mockStatic(JdbcUtils.class);
        flowEngineUtils = mockStatic(FlowExecutionEngineUtils.class);

        jdbcUtils.when(JdbcUtils::getNewTemplate).thenReturn(jdbcTemplate);
    }

    @AfterMethod
    public void tearDown() {

        if (jdbcUtils != null) {
            jdbcUtils.close();
        }
        if (flowEngineUtils != null) {
            flowEngineUtils.close();
        }
    }

    @Test
    public void testStoreContextSuccessfulInsert() throws Exception {

        FlowExecutionContext context = createTestContext();
        when(jdbcTemplate.executeUpdateWithAffectedRows(contains("UPDATE"), any())).thenReturn(0);

        flowContextStoreDAO.storeContext(context, TTL_SECONDS);

        verify(jdbcTemplate).executeUpdateWithAffectedRows(contains("UPDATE"), any());
        verify(jdbcTemplate).executeUpdate(contains("INSERT"), any());
    }

    @Test
    public void testStoreContextSuccessfulUpdate() throws Exception {

        FlowExecutionContext context = createTestContext();
        when(jdbcTemplate.executeUpdateWithAffectedRows(contains("UPDATE"), any())).thenReturn(1);

        flowContextStoreDAO.storeContext(context, TTL_SECONDS);

        verify(jdbcTemplate).executeUpdateWithAffectedRows(contains("UPDATE"), any());
        verify(jdbcTemplate, times(0)).executeUpdate(contains("INSERT"), any());
    }

    @Test
    public void testStoreContextWithDataAccessException() throws Exception {

        FlowExecutionContext context = createTestContext();
        DataAccessException dataAccessException = new DataAccessException("Database error");
        doThrow(dataAccessException).when(jdbcTemplate).executeUpdateWithAffectedRows(contains("UPDATE"), any());

        FlowEngineServerException expectedException = new FlowEngineServerException("Store failure");
        flowEngineUtils.when(() -> FlowExecutionEngineUtils.handleServerException(
                        any(Constants.ErrorMessages.class), any(Exception.class), any(String.class)))
                .thenReturn(expectedException);

        assertThrows(FlowEngineException.class, () -> flowContextStoreDAO.storeContext(context, TTL_SECONDS));
    }

    @Test
    public void testGetContextSuccess() throws Exception {

        FlowExecutionContext expectedContext = createTestContext();
        doReturn(expectedContext).when(jdbcTemplate).fetchSingleRecord(contains("SELECT"), any(), any());

        FlowExecutionContext actualContext = flowContextStoreDAO.getContext(CONTEXT_ID);

        assertNotNull(actualContext);
        assertEquals(actualContext.getContextIdentifier(), CONTEXT_ID);
        assertEquals(actualContext.getTenantDomain(), TENANT_DOMAIN);
        assertEquals(actualContext.getFlowType(), FLOW_TYPE);
    }

    @Test
    public void testGetContextWithDataAccessException() throws Exception {

        DataAccessException dataAccessException = new DataAccessException("Database error");
        doThrow(dataAccessException).when(jdbcTemplate).fetchSingleRecord(contains("SELECT"), any(), any());

        FlowEngineServerException expectedException = new FlowEngineServerException("Retrieval failure");
        flowEngineUtils.when(() -> FlowExecutionEngineUtils.handleServerException(
                        any(Constants.ErrorMessages.class), any(Exception.class), any(String.class)))
                .thenReturn(expectedException);

        assertThrows(FlowEngineException.class, () -> flowContextStoreDAO.getContext(CONTEXT_ID));
    }

    @Test
    public void testDeleteContextSuccess() throws Exception {

        flowContextStoreDAO.deleteContext(CONTEXT_ID);

        verify(jdbcTemplate).executeUpdate(contains("DELETE"), any());
    }

    @Test
    public void testDeleteContextWithDataAccessException() throws Exception {

        DataAccessException dataAccessException = new DataAccessException("Database error");
        doThrow(dataAccessException).when(jdbcTemplate).executeUpdate(contains("DELETE"), any());

        FlowEngineServerException expectedException = new FlowEngineServerException("Deletion failure");
        flowEngineUtils.when(() -> FlowExecutionEngineUtils.handleServerException(
                        any(Constants.ErrorMessages.class), any(Exception.class), any(String.class)))
                .thenReturn(expectedException);

        assertThrows(FlowEngineException.class, () -> flowContextStoreDAO.deleteContext(CONTEXT_ID));
    }

    @Test
    public void testCleanupExpiredContextsSuccess() throws Exception {

        when(jdbcTemplate.getDriverName()).thenReturn("MySQL");

        flowContextStoreDAO.cleanupExpiredContexts(CLEANUP_LIMIT);

        verify(jdbcTemplate).executeUpdate(anyString(), any());
        verify(jdbcTemplate).getDriverName();
    }

    @Test
    public void testCleanupExpiredContextsWithDataAccessExceptionDuringExecution() throws Exception {

        when(jdbcTemplate.getDriverName()).thenReturn("MySQL");
        DataAccessException dataAccessException = new DataAccessException("Database error");
        doThrow(dataAccessException).when(jdbcTemplate).executeUpdate(anyString(), any());

        FlowEngineServerException expectedException = new FlowEngineServerException("Cleanup failure");
        flowEngineUtils.when(() -> FlowExecutionEngineUtils.handleServerException(
                        any(Constants.ErrorMessages.class), any(Exception.class)))
                .thenReturn(expectedException);

        assertThrows(FlowEngineException.class, () -> flowContextStoreDAO.cleanupExpiredContexts(CLEANUP_LIMIT));
    }

    private FlowExecutionContext createTestContext() {

        FlowExecutionContext context = new FlowExecutionContext();
        context.setContextIdentifier(CONTEXT_ID);
        context.setTenantDomain(TENANT_DOMAIN);
        context.setFlowType(FLOW_TYPE);
        context.setGraphConfig(new GraphConfig());
        return context;
    }
}
