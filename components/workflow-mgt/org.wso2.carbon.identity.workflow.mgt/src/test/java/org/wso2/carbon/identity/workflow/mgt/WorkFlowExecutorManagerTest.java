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

package org.wso2.carbon.identity.workflow.mgt;

import org.apache.commons.lang.SerializationUtils;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.rule.evaluation.api.exception.RuleEvaluationException;
import org.wso2.carbon.identity.rule.evaluation.api.model.RuleEvaluationResult;
import org.wso2.carbon.identity.rule.evaluation.api.service.RuleEvaluationService;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowExecutorManagerListener;
import org.wso2.carbon.identity.workflow.mgt.util.ExecutorResultState;
import org.wso2.carbon.identity.workflow.mgt.util.SQLConstants;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Unit tests for {@link WorkFlowExecutorManager}.
 */
public class WorkFlowExecutorManagerTest {

    private static final String TEST_UUID = "test-uuid-123";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String TEST_EVENT_TYPE = "ADD_USER";
    private static final String TEST_TENANT_DOMAIN = "carbon.super";
    private static final String TEST_WORKFLOW_ID = "wf-id-1";
    private static final String TEST_ASSOCIATION_NAME = "Test Association";
    // A valid UUID string used as a rule-based condition identifier.
    private static final String TEST_RULE_UUID = "550e8400-e29b-41d4-a716-446655440000";

    private MockedStatic<IdentityDatabaseUtil> identityDatabaseUtilMockedStatic;
    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContextMockedStatic;

    @Mock
    private WorkflowServiceDataHolder mockWorkflowServiceDataHolder;

    @BeforeMethod
    public void setUp() {

        openMocks(this);
        identityDatabaseUtilMockedStatic = mockStatic(IdentityDatabaseUtil.class);
        privilegedCarbonContextMockedStatic = mockStatic(PrivilegedCarbonContext.class);

        // Set up an empty listener list on the real data holder singleton.
        WorkflowServiceDataHolder.getInstance().setExecutorListenerList(new ArrayList<>());
    }

    @AfterMethod
    public void tearDown() {

        if (identityDatabaseUtilMockedStatic != null) {
            identityDatabaseUtilMockedStatic.close();
        }
        if (privilegedCarbonContextMockedStatic != null) {
            privilegedCarbonContextMockedStatic.close();
        }
        // Clear the rule evaluation service to avoid state leaking between tests.
        WorkflowServiceDataHolder.getInstance().setRuleEvaluationService(null);
        // Clear workflow implementations to avoid state leaking between tests.
        WorkflowServiceDataHolder.getInstance().getWorkflowImpls().clear();
    }

    /**
     * Test public handleCallback method that delegates to private handleCallback.
     */
    @Test
    public void testHandleCallbackWhenWorkflowRequestAbortedOrDeleted() throws Exception {

        Map<String, Object> additionalParams = new HashMap<>();

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(SQLConstants.REQUEST_ID_COLUMN)).thenReturn("REQUEST_ID");

        WorkflowRequest workflowRequest = new WorkflowRequest();

        byte[] workflowRequestBytes = SerializationUtils.serialize(workflowRequest);
        when(mockResultSet.getBytes(SQLConstants.REQUEST_COLUMN)).thenReturn(workflowRequestBytes);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection())
                .thenReturn(mockConnection);

        when(mockResultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN))
                .thenReturn(WorkflowRequestStatus.ABORTED.toString());
        WorkFlowExecutorManager.getInstance().handleCallback(TEST_UUID, STATUS_APPROVED, additionalParams);

        when(mockResultSet.getString(SQLConstants.REQUEST_STATUS_COLUMN))
                .thenReturn(WorkflowRequestStatus.DELETED.toString());
        WorkFlowExecutorManager.getInstance().handleCallback(TEST_UUID, STATUS_APPROVED, additionalParams);
    }

    /**
     * Test that an enabled pre-executor listener has its doPreExecuteWorkflow method called.
     */
    @Test
    public void testExecuteWorkflow_withEnabledPreListener_callsDoPreExecuteWorkflow() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();

        WorkflowExecutorManagerListener mockListener = mock(WorkflowExecutorManagerListener.class);
        when(mockListener.isEnable()).thenReturn(true);
        WorkflowServiceDataHolder.getInstance().setExecutorListenerList(
                Collections.singletonList(mockListener));

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockAssocResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockAssocResultSet);
        when(mockAssocResultSet.next()).thenReturn(false);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        verify(mockListener).doPreExecuteWorkflow(workflowRequest);
    }

    /**
     * Test that a disabled pre-executor listener does NOT have its doPreExecuteWorkflow method called.
     */
    @Test
    public void testExecuteWorkflow_withDisabledPreListener_skipsDoPreExecuteWorkflow() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();

        WorkflowExecutorManagerListener mockListener = mock(WorkflowExecutorManagerListener.class);
        when(mockListener.isEnable()).thenReturn(false);
        WorkflowServiceDataHolder.getInstance().setExecutorListenerList(
                Collections.singletonList(mockListener));

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockAssocResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockAssocResultSet);
        when(mockAssocResultSet.next()).thenReturn(false);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        // Verify listener.isEnable() was checked but doPreExecuteWorkflow was NOT invoked.
        verify(mockListener).isEnable();
        verify(mockListener, org.mockito.Mockito.never()).doPreExecuteWorkflow(any());
    }

    /**
     * Test executeWorkflow with a blank condition that satisfies, engaging the full workflow and
     * returning STARTED_ASSOCIATION.
     */
    @Test
    public void testExecuteWorkflow_blankCondition_fullEngagement_returnsStartedAssociation() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();
        mockCarbonContext();

        // Association connection: one row with blank condition.
        Connection assocConn = buildMockConnectionWithAssociation("");

        // Insert connection 1: addWorkflowEntry.
        Connection insertConn1 = buildMockInsertConnection();

        // Workflow connection: getWorkflow returns a workflow with template 't1', impl 'impl1'.
        Connection workflowConn = buildMockWorkflowConnection();

        // Params connection: getWorkflowParams returns empty list.
        Connection paramsConn = buildMockEmptyParamsConnection();

        // Insert connection 2: addNewRelationship.
        Connection insertConn2 = buildMockInsertConnection();

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(assocConn, workflowConn, paramsConn);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection())
                .thenReturn(insertConn1, insertConn2);

        // Register a mock AbstractWorkflow implementation for the template.
        AbstractWorkflow mockAbstractWorkflow = mock(AbstractWorkflow.class);
        when(mockAbstractWorkflow.getTemplateId()).thenReturn("t1");
        when(mockAbstractWorkflow.getWorkflowImplId()).thenReturn("impl1");
        doNothing().when(mockAbstractWorkflow).execute(any(), any());
        WorkflowServiceDataHolder.getInstance().addWorkflowImplementation(mockAbstractWorkflow);

        WorkflowExecutorResult result = WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        assertNotNull(result);
        assertEquals(result.getExecutorResultState(), ExecutorResultState.STARTED_ASSOCIATION);
    }

    /**
     * Test executeWorkflow returns NO_ASSOCIATION when no workflow associations are found for the event.
     */
    @Test
    public void testExecuteWorkflow_noAssociations_returnsNoAssociation() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockAssocResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockAssocResultSet);
        // No rows returned — empty association list.
        when(mockAssocResultSet.next()).thenReturn(false);

        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        WorkflowExecutorResult result = WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        assertNotNull(result);
        assertEquals(result.getExecutorResultState(), ExecutorResultState.NO_ASSOCIATION);
    }

    /**
     * Test executeWorkflow returns CONDITION_FAILED when a UUID-based rule condition evaluates to false.
     */
    @Test
    public void testExecuteWorkflow_uuidCondition_ruleNotSatisfied_returnsConditionFailed() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();
        mockCarbonContext();

        Connection mockConnection = buildMockConnectionWithAssociation(TEST_RULE_UUID);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        RuleEvaluationService mockRuleEvalService = mock(RuleEvaluationService.class);
        RuleEvaluationResult mockRuleResult = mock(RuleEvaluationResult.class);
        when(mockRuleEvalService.evaluate(anyString(), any(), anyString())).thenReturn(mockRuleResult);
        when(mockRuleResult.isRuleSatisfied()).thenReturn(false);
        WorkflowServiceDataHolder.getInstance().setRuleEvaluationService(mockRuleEvalService);

        WorkflowExecutorResult result = WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        assertNotNull(result);
        assertEquals(result.getExecutorResultState(), ExecutorResultState.CONDITION_FAILED);
    }

    /**
     * Test executeWorkflow throws WorkflowException when rule evaluation throws a RuleEvaluationException.
     */
    @Test(expectedExceptions = WorkflowException.class)
    public void testExecuteWorkflow_uuidCondition_ruleEvaluationException_throwsWorkflowException() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();
        mockCarbonContext();

        Connection mockConnection = buildMockConnectionWithAssociation(TEST_RULE_UUID);
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        RuleEvaluationService mockRuleEvalService = mock(RuleEvaluationService.class);
        when(mockRuleEvalService.evaluate(anyString(), any(), anyString()))
                .thenThrow(new RuleEvaluationException("Rule evaluation failed."));
        WorkflowServiceDataHolder.getInstance().setRuleEvaluationService(mockRuleEvalService);

        WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);
    }

    /**
     * Test executeWorkflow returns CONDITION_FAILED when the XPath condition evaluates to false.
     */
    @Test
    public void testExecuteWorkflow_xpathCondition_notSatisfied_returnsConditionFailed() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();
        mockCarbonContext();

        // XPath expression "false()" always evaluates to false.
        Connection mockConnection = buildMockConnectionWithAssociation("false()");
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        WorkflowExecutorResult result = WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        assertNotNull(result);
        assertEquals(result.getExecutorResultState(), ExecutorResultState.CONDITION_FAILED);
    }

    /**
     * Test executeWorkflow returns FAILED when the XPath expression is syntactically invalid (JaxenException).
     */
    @Test
    public void testExecuteWorkflow_invalidXpathCondition_jaxenException_returnsFailed() throws Exception {

        WorkflowRequest workflowRequest = buildMinimalWorkflowRequest();
        mockCarbonContext();

        // This is not a UUID and not a valid XPath expression, so AXIOMXPath constructor throws JaxenException.
        Connection mockConnection = buildMockConnectionWithAssociation("!@#$invalid_xpath");
        identityDatabaseUtilMockedStatic.when(() -> IdentityDatabaseUtil.getDBConnection(false))
                .thenReturn(mockConnection);

        WorkflowExecutorResult result = WorkFlowExecutorManager.getInstance().executeWorkflow(workflowRequest);

        assertNotNull(result);
        assertEquals(result.getExecutorResultState(), ExecutorResultState.FAILED);
    }

    // ---- Private helpers ----

    /**
     * Builds a mock Connection for a write (INSERT) operation, allowing prepareStatement and executeUpdate.
     *
     * @return A mock Connection configured to accept a prepared statement and update.
     */
    private Connection buildMockInsertConnection() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeUpdate()).thenReturn(1);
        return conn;
    }

    /**
     * Builds a mock Connection whose PreparedStatement returns a single Workflow row for getWorkflow.
     *
     * @return A mock Connection configured for workflowDAO.getWorkflow().
     */
    private Connection buildMockWorkflowConnection() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);
        when(rs.getString(SQLConstants.WF_NAME_COLUMN)).thenReturn("TestWorkflow");
        when(rs.getString(SQLConstants.DESCRIPTION_COLUMN)).thenReturn("Test workflow description.");
        when(rs.getString(SQLConstants.TEMPLATE_ID_COLUMN)).thenReturn("t1");
        when(rs.getString(SQLConstants.TEMPLATE_IMPL_ID_COLUMN)).thenReturn("impl1");
        return conn;
    }

    /**
     * Builds a mock Connection whose PreparedStatement returns no rows, simulating empty workflow params.
     *
     * @return A mock Connection configured for workflowDAO.getWorkflowParams().
     */
    private Connection buildMockEmptyParamsConnection() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);
        return conn;
    }

    /**
     * Builds a minimal WorkflowRequest suitable for unit tests.
     *
     * @return A WorkflowRequest with event type set and empty request parameters.
     */
    private WorkflowRequest buildMinimalWorkflowRequest() {

        WorkflowRequest request = new WorkflowRequest();
        request.setEventType(TEST_EVENT_TYPE);
        request.setTenantId(-1234);
        request.setRequestParameters(new ArrayList<>());
        return request;
    }

    /**
     * Sets up the PrivilegedCarbonContext static mock to return a predictable tenant domain.
     */
    private void mockCarbonContext() {

        PrivilegedCarbonContext mockCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContextMockedStatic.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockCarbonContext);
        when(mockCarbonContext.getTenantDomain()).thenReturn(TEST_TENANT_DOMAIN);
        when(mockCarbonContext.getTenantId()).thenReturn(-1234);
        when(mockCarbonContext.getUsername()).thenReturn("admin");
    }

    /**
     * Builds a mock Connection whose PreparedStatement returns a single WorkflowAssociation row.
     *
     * @param condition The association condition string (blank, UUID, or XPath).
     * @return A mock Connection pre-configured for {@code getWorkflowAssociationsForRequest}.
     */
    private Connection buildMockConnectionWithAssociation(String condition) throws Exception {

        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockAssocResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockAssocResultSet);
        // Return exactly one row, then stop.
        when(mockAssocResultSet.next()).thenReturn(true, false);
        when(mockAssocResultSet.getInt(SQLConstants.ID_COLUMN)).thenReturn(1);
        when(mockAssocResultSet.getString(SQLConstants.CONDITION_COLUMN)).thenReturn(condition);
        when(mockAssocResultSet.getString(SQLConstants.WORKFLOW_ID_COLUMN)).thenReturn(TEST_WORKFLOW_ID);
        when(mockAssocResultSet.getString(SQLConstants.ASSOCIATION_NAME_COLUMN)).thenReturn(TEST_ASSOCIATION_NAME);

        return mockConnection;
    }
}
