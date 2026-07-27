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
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.flow.execution.engine.Constants;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineException;
import org.wso2.carbon.identity.flow.execution.engine.exception.FlowEngineServerException;
import org.wso2.carbon.identity.flow.execution.engine.model.FlowExecutionContext;
import org.wso2.carbon.identity.flow.execution.engine.util.FlowExecutionEngineUtils;
import org.wso2.carbon.identity.flow.mgt.model.GraphConfig;

import static org.mockito.ArgumentMatchers.any;
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
@WithCarbonHome
public class FlowContextStoreDAOImplTest {

    private static final String CONTEXT_ID = "test-context-id";
    private static final String DIFFERENT_CONTEXT_ID = "different-context-id";
    private static final String TENANT_DOMAIN = "carbon.super";
    private static final String FLOW_TYPE = "REGISTRATION";
    private static final long TTL_SECONDS = 900L;
    private static final String RESOLVED_TENANT_DOMAIN = "org1.com";
    private static final int RESOLVED_TENANT_ID = 5;

    private FlowContextStoreDAOImpl flowContextStoreDAO;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private AutoCloseable autoCloseable;

    private MockedStatic<JdbcUtils> jdbcUtils;
    private MockedStatic<FlowExecutionEngineUtils> flowEngineUtils;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    @BeforeMethod
    public void setup() throws Exception {

        autoCloseable = MockitoAnnotations.openMocks(this);
        flowContextStoreDAO = new FlowContextStoreDAOImpl();

        jdbcUtils = mockStatic(JdbcUtils.class);
        flowEngineUtils = mockStatic(FlowExecutionEngineUtils.class);
        identityTenantUtil = mockStatic(IdentityTenantUtil.class);

        jdbcUtils.when(JdbcUtils::getNewTemplate).thenReturn(jdbcTemplate);

        // Mock resolveTenantDomain to return the tenant domain for getTenantId() calls.
        flowEngineUtils.when(FlowExecutionEngineUtils::resolveTenantDomain).thenReturn(TENANT_DOMAIN);

        // Mock IdentityTenantUtil to return tenant ID for the default tenant domain.
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN)).thenReturn(-1234);
    }

    @AfterMethod
    public void tearDown() throws Exception {

        if (jdbcUtils != null) {
            jdbcUtils.close();
        }
        if (flowEngineUtils != null) {
            flowEngineUtils.close();
        }
        if (identityTenantUtil != null) {
            identityTenantUtil.close();
        }
        if (autoCloseable != null) {
            autoCloseable.close();
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
    public void testStoreContextSuccessfulInsertWithDifferentContextID() throws Exception {

        FlowExecutionContext context = createTestContext();
        when(jdbcTemplate.executeUpdateWithAffectedRows(contains("UPDATE"), any())).thenReturn(0);

        flowContextStoreDAO.storeContext(DIFFERENT_CONTEXT_ID, context, TTL_SECONDS);

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
    public void testStoreContextSuccessfulUpdateWithDifferentContextID() throws Exception {

        FlowExecutionContext context = createTestContext();
        when(jdbcTemplate.executeUpdateWithAffectedRows(contains("UPDATE"), any())).thenReturn(1);

        flowContextStoreDAO.storeContext(DIFFERENT_CONTEXT_ID, context, TTL_SECONDS);

        verify(jdbcTemplate).executeUpdateWithAffectedRows(contains("UPDATE"), any());
        verify(jdbcTemplate, times(0)).executeUpdate(contains("INSERT"), any());
    }

    @Test
    public void testStoreContextWithDataAccessException() throws Exception {

        FlowExecutionContext context = createTestContext();
        DataAccessException dataAccessException = new DataAccessException("Database error");
        doThrow(dataAccessException).when(jdbcTemplate).executeUpdateWithAffectedRows(contains("UPDATE"), any());

        FlowEngineServerException expectedException = new FlowEngineServerException("Store failure");
        flowEngineUtils.when(() -> FlowExecutionEngineUtils.handleServerException( any(String.class),
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
    public void testGetContextSuccessWithDifferentContextID() throws Exception {

        FlowExecutionContext expectedContext = createTestContext();
        doReturn(expectedContext).when(jdbcTemplate).fetchSingleRecord(contains("SELECT"), any(), any());

        FlowExecutionContext actualContext = flowContextStoreDAO.getContext(DIFFERENT_CONTEXT_ID);

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
    public void testGetContextWithAppResidentOrgId() throws Exception {

        // Mock scenario where resolveTenantDomain returns a different tenant domain (from org resolution).
        flowEngineUtils.when(FlowExecutionEngineUtils::resolveTenantDomain).thenReturn(RESOLVED_TENANT_DOMAIN);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(RESOLVED_TENANT_DOMAIN))
                .thenReturn(RESOLVED_TENANT_ID);

        FlowExecutionContext expectedContext = createTestContext();
        doReturn(expectedContext).when(jdbcTemplate).fetchSingleRecord(contains("SELECT"), any(), any());

        FlowExecutionContext actualContext = flowContextStoreDAO.getContext(CONTEXT_ID);

        assertNotNull(actualContext);
        assertEquals(actualContext.getContextIdentifier(), CONTEXT_ID);
        flowEngineUtils.verify(FlowExecutionEngineUtils::resolveTenantDomain);
        identityTenantUtil.verify(() -> IdentityTenantUtil.getTenantId(RESOLVED_TENANT_DOMAIN));
    }

    @Test(expectedExceptions = FlowEngineServerException.class)
    public void testGetContextWithResolveTenantDomainThrowsException() throws Exception {

        // Mock scenario where resolveTenantDomain throws FlowEngineServerException.
        FlowEngineServerException expectedException = new FlowEngineServerException(
                "Failed to resolve tenant domain from organization");
        flowEngineUtils.when(FlowExecutionEngineUtils::resolveTenantDomain).thenThrow(expectedException);

        flowContextStoreDAO.getContext(CONTEXT_ID);
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
