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

package org.wso2.carbon.identity.workflow.mgt.dao;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@WithCarbonHome
@WithH2Database(files = { "dbscripts/h2.sql" })
public class WorkflowDAOTest {
    
    private static final int TENANT_ID = -1234;
    private static final int SECOND_TENANT_ID = -5678;
    private static final String WORKFLOW_ID = "test-workflow-1";
    private static final String WORKFLOW_ID_2 = "test-workflow-2";
    private static final String WORKFLOW_NAME = "Test Workflow";
    private static final String WORKFLOW_NAME_2 = "Test Workflow 2";
    private static final String WORKFLOW_DESCRIPTION = "Test workflow description";
    private static final String UPDATED_WORKFLOW_DESCRIPTION = "Updated test workflow description";
    private static final String TEMPLATE_ID = "test-template-1";
    private static final String WORKFLOW_IMPL_ID = "test-impl-1";
    private static final String UPDATED_TEMPLATE_ID = "updated-template-1";
    private static final String UPDATED_WORKFLOW_IMPL_ID = "updated-impl-1";
    
    private Workflow testWorkflow;
    private Workflow testWorkflow2;
    private WorkflowDAO workflowDAO;

    private MockedStatic<PrivilegedCarbonContext> privilegedCarbonContext;

    @BeforeClass
    public void initTest() {
        workflowDAO = new WorkflowDAO();
        
        testWorkflow = new Workflow();
        testWorkflow.setWorkflowId(WORKFLOW_ID);
        testWorkflow.setWorkflowName(WORKFLOW_NAME);
        testWorkflow.setWorkflowDescription(WORKFLOW_DESCRIPTION);
        testWorkflow.setTemplateId(TEMPLATE_ID);
        testWorkflow.setWorkflowImplId(WORKFLOW_IMPL_ID);
        
        testWorkflow2 = new Workflow();
        testWorkflow2.setWorkflowId(WORKFLOW_ID_2);
        testWorkflow2.setWorkflowName(WORKFLOW_NAME_2);
        testWorkflow2.setWorkflowDescription(WORKFLOW_DESCRIPTION);
        testWorkflow2.setTemplateId(TEMPLATE_ID);
        testWorkflow2.setWorkflowImplId(WORKFLOW_IMPL_ID);
    }

    @BeforeMethod
    public void setupBeforeTest() {

        mockCarbonContextForTenant(TENANT_ID);
    }

    @AfterMethod
    public void cleanupAfterTest() throws InternalWorkflowException {

        // Clean up any test data to ensure tests don't interfere with each other
        try {
            workflowDAO.removeWorkflow(WORKFLOW_ID);
        } catch (InternalWorkflowException e) {
            // Ignore if workflow doesn't exist
        }
        try {
            workflowDAO.removeWorkflow(WORKFLOW_ID_2);
        } catch (InternalWorkflowException e) {
            // Ignore if workflow doesn't exist
        }
        try {
            workflowDAO.removeWorkflows(TENANT_ID);
        } catch (InternalWorkflowException e) {
            // Ignore if workflows don't exist
        }
        try {
            workflowDAO.removeWorkflows(SECOND_TENANT_ID);
        } catch (InternalWorkflowException e) {
            // Ignore if workflows don't exist
        }

        if (privilegedCarbonContext != null && !privilegedCarbonContext.isClosed()) {
            privilegedCarbonContext.close();
        }
    }
    
    @Test
    public void testAddWorkflow() throws InternalWorkflowException {
        // Test adding a workflow
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);

        // Test retrieving the workflow
        Workflow retrievedWorkflow = workflowDAO.getWorkflow(WORKFLOW_ID);
        
        // Verify the workflow was stored and retrieved correctly
        assertNotNull(retrievedWorkflow, "Failed to retrieve workflow from database");
        assertEquals(retrievedWorkflow.getWorkflowId(), WORKFLOW_ID, "Workflow ID mismatch");
        assertEquals(retrievedWorkflow.getWorkflowName(), WORKFLOW_NAME, "Workflow name mismatch");
        assertEquals(retrievedWorkflow.getWorkflowDescription(), WORKFLOW_DESCRIPTION, "Workflow description mismatch");
        assertEquals(retrievedWorkflow.getTemplateId(), TEMPLATE_ID, "Template ID mismatch");
        assertEquals(retrievedWorkflow.getWorkflowImplId(), WORKFLOW_IMPL_ID, "Workflow implementation ID mismatch");
    }
    
    @Test
    public void testGetWorkflow() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Test retrieving the workflow by ID
        Workflow retrievedWorkflow = workflowDAO.getWorkflow(WORKFLOW_ID);
        
        assertNotNull(retrievedWorkflow, "Retrieved workflow should not be null");
        assertEquals(retrievedWorkflow.getWorkflowId(), WORKFLOW_ID);
        assertEquals(retrievedWorkflow.getWorkflowName(), WORKFLOW_NAME);
        assertEquals(retrievedWorkflow.getWorkflowDescription(), WORKFLOW_DESCRIPTION);
        assertEquals(retrievedWorkflow.getTemplateId(), TEMPLATE_ID);
        assertEquals(retrievedWorkflow.getWorkflowImplId(), WORKFLOW_IMPL_ID);
    }
    
    @Test
    public void testGetWorkflowNonExistent() throws InternalWorkflowException {
        // Test retrieving a non-existent workflow
        Workflow retrievedWorkflow = workflowDAO.getWorkflow("non-existent-workflow");
        
        assertNull(retrievedWorkflow, "Retrieved workflow should be null for non-existent ID");
    }
    
    @Test
    public void testGetWorkflowByName() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Test retrieving the workflow by name
        Workflow retrievedWorkflow = workflowDAO.getWorkflowByName(WORKFLOW_NAME, TENANT_ID);
        
        assertNotNull(retrievedWorkflow, "Retrieved workflow should not be null");
        assertEquals(retrievedWorkflow.getWorkflowId(), WORKFLOW_ID);
        assertEquals(retrievedWorkflow.getWorkflowName(), WORKFLOW_NAME);
        assertEquals(retrievedWorkflow.getWorkflowDescription(), WORKFLOW_DESCRIPTION);
        assertEquals(retrievedWorkflow.getTemplateId(), TEMPLATE_ID);
        assertEquals(retrievedWorkflow.getWorkflowImplId(), WORKFLOW_IMPL_ID);
    }
    
    @Test
    public void testGetWorkflowByNameNonExistent() throws InternalWorkflowException {
        // Test retrieving a non-existent workflow by name
        Workflow retrievedWorkflow = workflowDAO.getWorkflowByName("non-existent-workflow", TENANT_ID);
        
        assertNull(retrievedWorkflow, "Retrieved workflow should be null for non-existent name");
    }
    
    @Test
    public void testGetWorkflowByNameDifferentTenant() throws InternalWorkflowException {
        // Add a workflow for tenant 1
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Try to retrieve from different tenant
        Workflow retrievedWorkflow = workflowDAO.getWorkflowByName(WORKFLOW_NAME, SECOND_TENANT_ID);
        
        assertNull(retrievedWorkflow, "Should not retrieve workflow from different tenant");
    }
    
    @Test
    public void testUpdateWorkflow() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Update the workflow
        Workflow updatedWorkflow = new Workflow();
        updatedWorkflow.setWorkflowId(WORKFLOW_ID);
        updatedWorkflow.setWorkflowName(WORKFLOW_NAME);
        updatedWorkflow.setWorkflowDescription(UPDATED_WORKFLOW_DESCRIPTION);
        updatedWorkflow.setTemplateId(UPDATED_TEMPLATE_ID);
        updatedWorkflow.setWorkflowImplId(UPDATED_WORKFLOW_IMPL_ID);

        workflowDAO.updateWorkflow(updatedWorkflow);
        
        // Verify the update
        Workflow retrievedWorkflow = workflowDAO.getWorkflow(WORKFLOW_ID);
        
        assertNotNull(retrievedWorkflow, "Retrieved workflow should not be null");
        assertEquals(retrievedWorkflow.getWorkflowId(), WORKFLOW_ID);
        assertEquals(retrievedWorkflow.getWorkflowName(), WORKFLOW_NAME);
        assertEquals(retrievedWorkflow.getWorkflowDescription(), UPDATED_WORKFLOW_DESCRIPTION);
        assertEquals(retrievedWorkflow.getTemplateId(), UPDATED_TEMPLATE_ID);
        assertEquals(retrievedWorkflow.getWorkflowImplId(), UPDATED_WORKFLOW_IMPL_ID);
    }
    
    @Test
    public void testRemoveWorkflow() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Verify it exists
        Workflow retrievedWorkflow = workflowDAO.getWorkflow(WORKFLOW_ID);
        assertNotNull(retrievedWorkflow, "Workflow should exist before removal");
        
        // Remove the workflow
        workflowDAO.removeWorkflow(WORKFLOW_ID);
        
        // Verify it's removed
        retrievedWorkflow = workflowDAO.getWorkflow(WORKFLOW_ID);
        assertNull(retrievedWorkflow, "Workflow should be null after removal");
    }
    
    @Test
    public void testRemoveWorkflows() throws InternalWorkflowException {

        // Add workflows for multiple tenants
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        workflowDAO.addWorkflow(testWorkflow2, TENANT_ID);
        
        Workflow workflow3 = new Workflow();
        workflow3.setWorkflowId("workflow-3");
        workflow3.setWorkflowName("Workflow 3");
        workflow3.setWorkflowDescription("Description 3");
        workflow3.setTemplateId(TEMPLATE_ID);
        workflow3.setWorkflowImplId(WORKFLOW_IMPL_ID);
        workflowDAO.addWorkflow(workflow3, SECOND_TENANT_ID);

        // Verify workflows exist
        assertNotNull(workflowDAO.getWorkflow(WORKFLOW_ID));
        assertNotNull(workflowDAO.getWorkflow(WORKFLOW_ID_2));

        // Workflows in other tenant can not be accessed.
        assertNull(workflowDAO.getWorkflow("workflow-3"));
        
        // Remove workflows for one tenant
        workflowDAO.removeWorkflows(TENANT_ID);
        
        // Verify workflows for the tenant are removed
        assertNull(workflowDAO.getWorkflow(WORKFLOW_ID));
        assertNull(workflowDAO.getWorkflow(WORKFLOW_ID_2));

        // Set tenant context as SECOND_TENANT_ID.
        mockCarbonContextForTenant(SECOND_TENANT_ID);

        // Verify workflow for other tenant still exists
        assertNotNull(workflowDAO.getWorkflow("workflow-3"));
        
        // Clean up
        workflowDAO.removeWorkflow("workflow-3");
    }
    
    @Test
    @SuppressWarnings("deprecation")
    public void testListWorkflows() throws InternalWorkflowException {
        // Add multiple workflows for the same tenant
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        workflowDAO.addWorkflow(testWorkflow2, TENANT_ID);
        
        // Add workflow for different tenant
        Workflow workflow3 = new Workflow();
        workflow3.setWorkflowId("workflow-3");
        workflow3.setWorkflowName("Workflow 3");
        workflow3.setWorkflowDescription("Description 3");
        workflow3.setTemplateId(TEMPLATE_ID);
        workflow3.setWorkflowImplId(WORKFLOW_IMPL_ID);
        workflowDAO.addWorkflow(workflow3, SECOND_TENANT_ID);
        
        // Test listing workflows for first tenant
        List<Workflow> workflows = workflowDAO.listWorkflows(TENANT_ID);
        
        assertNotNull(workflows, "Workflow list should not be null");
        assertEquals(workflows.size(), 2, "Should return 2 workflows for the tenant");
        
        // Verify workflow IDs are correct
        List<String> workflowIds = new ArrayList<>();
        for (Workflow workflow : workflows) {
            workflowIds.add(workflow.getWorkflowId());
        }
        assertTrue(workflowIds.contains(WORKFLOW_ID), "Should contain first workflow");
        assertTrue(workflowIds.contains(WORKFLOW_ID_2), "Should contain second workflow");
        
        // Test listing workflows for second tenant
        workflows = workflowDAO.listWorkflows(SECOND_TENANT_ID);
        
        assertNotNull(workflows, "Workflow list should not be null");
        assertEquals(workflows.size(), 1, "Should return 1 workflow for the second tenant");
        assertEquals(workflows.get(0).getWorkflowId(), "workflow-3");
        
        // Clean up
        workflowDAO.removeWorkflow("workflow-3");
    }
    
    @Test
    public void testListPaginatedWorkflows() throws InternalWorkflowException {
        // Add multiple workflows
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        workflowDAO.addWorkflow(testWorkflow2, TENANT_ID);
        
        // Test pagination
        List<Workflow> workflows = workflowDAO.listPaginatedWorkflows(TENANT_ID, "*", 0, 1);
        
        assertNotNull(workflows, "Workflow list should not be null");
        assertEquals(workflows.size(), 1, "Should return 1 workflow with limit 1");
        
        // Test with filter
        workflows = workflowDAO.listPaginatedWorkflows(TENANT_ID, "Test Workflow", 0, 10);
        
        assertNotNull(workflows, "Workflow list should not be null");
        assertEquals(workflows.size(), 1, "Should return 1 workflow matching filter");
        assertEquals(workflows.get(0).getWorkflowName(), WORKFLOW_NAME);
        
        // Test with filter that matches multiple
        workflows = workflowDAO.listPaginatedWorkflows(TENANT_ID, "*", 0, 10);
        
        assertNotNull(workflows, "Workflow list should not be null");
        assertEquals(workflows.size(), 2, "Should return 2 workflows with wildcard filter");
    }
    
    @Test
    public void testGetWorkflowsCount() throws InternalWorkflowException {
        // Test count with no workflows
        int count = workflowDAO.getWorkflowsCount(TENANT_ID, "*");
        assertEquals(count, 0, "Count should be 0 for empty tenant");
        
        // Add workflows
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        workflowDAO.addWorkflow(testWorkflow2, TENANT_ID);
        
        // Test count with workflows
        count = workflowDAO.getWorkflowsCount(TENANT_ID, "*");
        assertEquals(count, 2, "Count should be 2 after adding workflows");
        
        // Test count with filter
        count = workflowDAO.getWorkflowsCount(TENANT_ID, "Test Workflow");
        assertEquals(count, 1, "Count should be 1 with specific filter");
        
        // Test count for different tenant
        count = workflowDAO.getWorkflowsCount(SECOND_TENANT_ID, "*");
        assertEquals(count, 0, "Count should be 0 for different tenant");
    }
    
    @Test
    public void testWorkflowParams() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Create parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter param1 = new Parameter(WORKFLOW_ID, "param1", "value1", "qname1", "holder1");
        Parameter param2 = new Parameter(WORKFLOW_ID, "param2", "value2", "qname2", "holder2");
        parameters.add(param1);
        parameters.add(param2);
        
        // Add parameters
        workflowDAO.addWorkflowParams(parameters, WORKFLOW_ID, TENANT_ID);
        
        // Retrieve parameters
        List<Parameter> retrievedParams = workflowDAO.getWorkflowParams(WORKFLOW_ID);
        
        assertNotNull(retrievedParams, "Parameters list should not be null");
        assertEquals(retrievedParams.size(), 2, "Should return 2 parameters");
        
        // Verify parameter content
        Parameter retrievedParam1 = null;
        Parameter retrievedParam2 = null;
        
        for (Parameter param : retrievedParams) {
            if ("param1".equals(param.getParamName())) {
                retrievedParam1 = param;
            } else if ("param2".equals(param.getParamName())) {
                retrievedParam2 = param;
            }
        }
        
        assertNotNull(retrievedParam1, "First parameter should be found");
        assertNotNull(retrievedParam2, "Second parameter should be found");
        
        assertEquals(retrievedParam1.getParamValue(), "value1");
        assertEquals(retrievedParam1.getqName(), "qname1");
        assertEquals(retrievedParam1.getHolder(), "holder1");
        
        assertEquals(retrievedParam2.getParamValue(), "value2");
        assertEquals(retrievedParam2.getqName(), "qname2");
        assertEquals(retrievedParam2.getHolder(), "holder2");
    }
    
    @Test
    public void testRemoveWorkflowParams() throws InternalWorkflowException {
        // Add a workflow first
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        
        // Add parameters
        List<Parameter> parameters = new ArrayList<>();
        Parameter param1 = new Parameter(WORKFLOW_ID, "param1", "value1", "qname1", "holder1");
        parameters.add(param1);
        workflowDAO.addWorkflowParams(parameters, WORKFLOW_ID, TENANT_ID);
        
        // Verify parameters exist
        List<Parameter> retrievedParams = workflowDAO.getWorkflowParams(WORKFLOW_ID);
        assertEquals(retrievedParams.size(), 1, "Should have 1 parameter before removal");
        
        // Remove parameters
        workflowDAO.removeWorkflowParams(WORKFLOW_ID);
        
        // Verify parameters are removed
        retrievedParams = workflowDAO.getWorkflowParams(WORKFLOW_ID);
        assertEquals(retrievedParams.size(), 0, "Should have 0 parameters after removal");
    }
    
    @Test
    public void testRemoveWorkflowParamsByTenant() throws InternalWorkflowException {
        // Add workflows for multiple tenants
        workflowDAO.addWorkflow(testWorkflow, TENANT_ID);
        workflowDAO.addWorkflow(testWorkflow2, TENANT_ID);
        
        Workflow workflow3 = new Workflow();
        workflow3.setWorkflowId("workflow-3");
        workflow3.setWorkflowName("Workflow 3");
        workflow3.setWorkflowDescription("Description 3");
        workflow3.setTemplateId(TEMPLATE_ID);
        workflow3.setWorkflowImplId(WORKFLOW_IMPL_ID);
        workflowDAO.addWorkflow(workflow3, SECOND_TENANT_ID);
        
        // Add parameters for all workflows
        List<Parameter> params1 = new ArrayList<>();
        params1.add(new Parameter(WORKFLOW_ID, "param1", "value1", "qname1", "holder1"));
        workflowDAO.addWorkflowParams(params1, WORKFLOW_ID, TENANT_ID);
        
        List<Parameter> params2 = new ArrayList<>();
        params2.add(new Parameter(WORKFLOW_ID_2, "param2", "value2", "qname2", "holder2"));
        workflowDAO.addWorkflowParams(params2, WORKFLOW_ID_2, TENANT_ID);
        
        List<Parameter> params3 = new ArrayList<>();
        params3.add(new Parameter("workflow-3", "param3", "value3", "qname3", "holder3"));
        workflowDAO.addWorkflowParams(params3, "workflow-3", SECOND_TENANT_ID);
        
        // Verify parameters exist
        assertEquals(workflowDAO.getWorkflowParams(WORKFLOW_ID).size(), 1);
        assertEquals(workflowDAO.getWorkflowParams(WORKFLOW_ID_2).size(), 1);
        assertEquals(workflowDAO.getWorkflowParams("workflow-3").size(), 1);
        
        // Remove parameters for one tenant
        workflowDAO.removeWorkflowParams(TENANT_ID);
        
        // Verify parameters for the tenant are removed
        assertEquals(workflowDAO.getWorkflowParams(WORKFLOW_ID).size(), 0);
        assertEquals(workflowDAO.getWorkflowParams(WORKFLOW_ID_2).size(), 0);
        
        // Verify parameters for other tenant still exist
        assertEquals(workflowDAO.getWorkflowParams("workflow-3").size(), 1);
        
        // Clean up
        workflowDAO.removeWorkflow("workflow-3");
    }

    private void mockCarbonContextForTenant(int tenantId) {

        if (privilegedCarbonContext != null && !privilegedCarbonContext.isClosed()) {
            privilegedCarbonContext.close();
        }
        privilegedCarbonContext = mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext mockPrivilegedCarbonContext = mock(PrivilegedCarbonContext.class);
        privilegedCarbonContext.when(PrivilegedCarbonContext::getThreadLocalCarbonContext)
                .thenReturn(mockPrivilegedCarbonContext);
        when(mockPrivilegedCarbonContext.getTenantId()).thenReturn(tenantId);
    }
}
