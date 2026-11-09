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

package org.wso2.carbon.identity.flow.execution.engine.dao;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.database.utils.jdbc.JdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.QueryFilter;
import org.wso2.carbon.identity.core.util.JdbcUtils;
import org.wso2.carbon.identity.flow.execution.engine.poll.FlowPollingEntry;

import java.sql.PreparedStatement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for FlowPollingDAOImpl.
 */
public class FlowPollingDAOImplTest {

    private static final String ENTRY_ID = "poll_entry";
    private static final int TENANT_ID = -1234;
    private static final String POLL_ENTRY_TYPE = "FLOW_POLL";

    private final FlowPollingDAOImpl flowPollingDAO = new FlowPollingDAOImpl();
    private JdbcTemplate jdbcTemplate;
    private MockedStatic<JdbcUtils> jdbcUtils;

    @BeforeMethod
    public void setUp() {

        jdbcTemplate = mock(JdbcTemplate.class);
        jdbcUtils = mockStatic(JdbcUtils.class);
        jdbcUtils.when(JdbcUtils::getNewTemplate).thenReturn(jdbcTemplate);
    }

    @AfterMethod
    public void tearDown() {

        jdbcUtils.close();
    }

    @Test
    public void testAddPollingEntryUsesThePollType() throws Exception {

        flowPollingDAO.addPollingEntry(ENTRY_ID, TENANT_ID, new FlowPollingEntry("flow-id", "WAITING", 1000L));

        ArgumentCaptor<QueryFilter> filter = ArgumentCaptor.forClass(QueryFilter.class);
        verify(jdbcTemplate).executeUpdate(contains("INSERT"), filter.capture());
        PreparedStatement statement = mock(PreparedStatement.class);
        filter.getValue().filter(statement);
        verify(statement).setString(1, ENTRY_ID);
        verify(statement).setInt(2, TENANT_ID);
        verify(statement).setString(3, POLL_ENTRY_TYPE);
    }

    @Test
    public void testGetPollingEntryOnlyReadsPollEntries() throws Exception {

        doReturn("{\"flowId\":\"flow-id\",\"status\":\"SUCCESS\",\"expiresAt\":1000}").when(jdbcTemplate)
                .fetchSingleRecord(contains("FLOW_TYPE = ?"), any(), any());

        FlowPollingEntry entry = flowPollingDAO.getPollingEntry(ENTRY_ID, TENANT_ID);

        assertEquals(entry, new FlowPollingEntry("flow-id", "SUCCESS", 1000L));
        ArgumentCaptor<QueryFilter> filter = ArgumentCaptor.forClass(QueryFilter.class);
        verify(jdbcTemplate).fetchSingleRecord(anyString(), any(), filter.capture());
        PreparedStatement statement = mock(PreparedStatement.class);
        filter.getValue().filter(statement);
        verify(statement).setString(3, POLL_ENTRY_TYPE);
    }

    @Test
    public void testGetMissingPollingEntry() throws Exception {

        assertNull(flowPollingDAO.getPollingEntry(ENTRY_ID, TENANT_ID));
    }

    @Test
    public void testUpdatePollingEntryNeverInserts() throws Exception {

        when(jdbcTemplate.executeUpdateWithAffectedRows(contains("FLOW_TYPE = ?"), any())).thenReturn(0, 1);

        FlowPollingEntry entry = new FlowPollingEntry("flow-id", "SUCCESS", 1000L);
        assertFalse(flowPollingDAO.updatePollingEntry(ENTRY_ID, TENANT_ID, entry));
        assertTrue(flowPollingDAO.updatePollingEntry(ENTRY_ID, TENANT_ID, entry));
        verify(jdbcTemplate, never()).executeUpdate(contains("INSERT"), any());
    }

    @Test
    public void testDeletePollingEntryIsScopedToTenantAndType() throws Exception {

        flowPollingDAO.deletePollingEntry(ENTRY_ID, TENANT_ID);

        ArgumentCaptor<QueryFilter> filter = ArgumentCaptor.forClass(QueryFilter.class);
        verify(jdbcTemplate).executeUpdate(contains("FLOW_TYPE = ?"), filter.capture());
        PreparedStatement statement = mock(PreparedStatement.class);
        filter.getValue().filter(statement);
        verify(statement).setInt(2, TENANT_ID);
        verify(statement).setString(3, POLL_ENTRY_TYPE);
    }
}
