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

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataManagementService;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.workflow.mgt.bean.Entity;
import org.wso2.carbon.identity.workflow.mgt.bean.Parameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Property;
import org.wso2.carbon.identity.workflow.mgt.bean.RequestParameter;
import org.wso2.carbon.identity.workflow.mgt.bean.Workflow;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowAssociation;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequest;
import org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestAssociation;
import org.wso2.carbon.identity.workflow.mgt.bean.metadata.ParametersMetaData;
import org.wso2.carbon.identity.workflow.mgt.dao.AssociationDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.RequestEntityRelationshipDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestAssociationDAO;
import org.wso2.carbon.identity.workflow.mgt.dao.WorkflowRequestDAO;
import org.wso2.carbon.identity.workflow.mgt.dto.Association;
import org.wso2.carbon.identity.workflow.mgt.dto.Template;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowEvent;
import org.wso2.carbon.identity.workflow.mgt.dto.WorkflowImpl;
import org.wso2.carbon.identity.workflow.mgt.exception.InternalWorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowClientException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowException;
import org.wso2.carbon.identity.workflow.mgt.exception.WorkflowRuntimeException;
import org.wso2.carbon.identity.workflow.mgt.extension.WorkflowRequestHandler;
import org.wso2.carbon.identity.workflow.mgt.internal.WorkflowServiceDataHolder;
import org.wso2.carbon.identity.workflow.mgt.listener.WorkflowListener;
import org.wso2.carbon.identity.workflow.mgt.template.AbstractTemplate;
import org.wso2.carbon.identity.workflow.mgt.util.WFConstant;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowDataType;
import org.wso2.carbon.identity.workflow.mgt.util.WorkflowRequestStatus;
import org.wso2.carbon.identity.workflow.mgt.workflow.AbstractWorkflow;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link WorkflowManagementServiceImpl}.
 */
public class WorkflowManagementServiceImplTest {

    private static final String TEST_REQUEST_ID_1 = "test_request_id_1";
    private static final String TEST_REQUEST_ID_2 = "test_request_id_2";
    private static final String TEST_REQUEST_ID_3 = "test_request_id_3";
    private static final String INVALID_REQUEST_ID = "non_existent_id";

    private static final String OPERATION_UPDATE_USER = "UPDATE_USER";
    private static final String OPERATION_ADD_USER = "ADD_USER";
    private static final String OPERATION_DELETE_USER = "DELETE_USER";

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_REJECTED = "REJECTED";

    private static final String CREATED_AT_1 = "2023-10-01 10:00:00";
    private static final String UPDATED_AT_1 = "2023-10-01 10:05:00";
    private static final String CREATED_AT_2 = "2023-10-01 11:00:00";
    private static final String UPDATED_AT_2 = "2023-10-01 11:05:00";
    private static final String CREATED_AT_3 = "2023-10-01 12:00:00";
    private static final String UPDATED_AT_3 = "2023-10-01 12:05:00";

    private static final String CREATED_BY = "admin";

    @Mock
    private WorkflowRequestDAO mockDAO;

    @Mock
    private WorkflowListener mockListener;

    private MockedStatic<WorkflowServiceDataHolder> mockedDataHolder;
    private WorkflowManagementServiceImpl service;

    private static final String TENANT_DOMAIN = "carbon.super";
    private static final int TENANT_ID = -1234;
    private static final String WORKFLOW_ID = "test-workflow-1";
    private static final String WORKFLOW_NAME = "Test Workflow";
    private static final String WORKFLOW_DESCRIPTION = "Test workflow description";
    private static final String TEMPLATE_ID = "test-template-1";
    private static final String DEFAULT_WORKFLOW_IMPL_ID = "WorkflowEngine";
    private static final String CUSTOM_WORKFLOW_IMPL_ID = "ApprovalWorkflow";

    private static final String EVENT_ID = "test-event-1";
    private static final String ASSOCIATION_NAME = "test-association";
    private static final String ASSOCIATION_ID = "1";
    private static final String CONDITION = "//condition[@name='test']";
    private static final String REQUEST_ID = "test-request-1";
    private static final String USER_NAME = "testuser";
    private static final String ENTITY_ID = "test-entity-id";
    private static final String ENTITY_TYPE = "USER";

    private WorkflowManagementServiceImpl workflowManagementService;

    @Mock
    private WorkflowDAO mockWorkflowDAO;
    @Mock
    private AssociationDAO mockAssociationDAO;
    @Mock
    private RequestEntityRelationshipDAO mockRequestEntityRelationshipDAO;
    @Mock
    private WorkflowRequestDAO mockWorkflowRequestDAO;
    @Mock
    private WorkflowRequestAssociationDAO mockWorkflowRequestAssociationDAO;
    @Mock
    private WorkflowServiceDataHolder mockWorkflowServiceDataHolder;
    @Mock
    private WorkflowListener mockWorkflowListener;
    @Mock
    private WorkflowRequestHandler mockWorkflowRequestHandler;
    @Mock
    private AbstractTemplate mockAbstractTemplate;
    @Mock
    private AbstractWorkflow mockAbstractWorkflow;

    private MockedStatic<IdentityTenantUtil> mockedIdentityTenantUtil;

    @BeforeMethod
    public void setUp() throws Exception {

        openMocks(this);

        when(mockListener.isEnable()).thenReturn(true);

        List<WorkflowListener> listeners = new ArrayList<>();
        listeners.add(mockListener);

        WorkflowServiceDataHolder mockHolder = mock(WorkflowServiceDataHolder.class);
        when(mockHolder.getWorkflowListenerList()).thenReturn(listeners);

        // Set up required system properties to avoid CarbonContext initialization issues
        System.setProperty("carbon.home", System.getProperty("java.io.tmpdir"));
        System.setProperty("java.naming.factory.initial", "org.wso2.carbon.tomcat.jndi.CarbonJavaURLContextFactory");

        workflowManagementService = new WorkflowManagementServiceImpl();

        // Use reflection to inject mocked DAOs
        injectMockDAO("workflowDAO", mockWorkflowDAO);
        injectMockDAO("associationDAO", mockAssociationDAO);
        injectMockDAO("requestEntityRelationshipDAO", mockRequestEntityRelationshipDAO);
        injectMockDAO("workflowRequestDAO", mockWorkflowRequestDAO);
        injectMockDAO("workflowRequestAssociationDAO", mockWorkflowRequestAssociationDAO);

        // Mock static classes
        mockedDataHolder = mockStatic(WorkflowServiceDataHolder.class);
        mockedIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);

        // Set up common mock behaviors
        mockedDataHolder.when(WorkflowServiceDataHolder::getInstance)
                .thenReturn(mockWorkflowServiceDataHolder);
        mockedIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(TENANT_DOMAIN))
                .thenReturn(TENANT_ID);

        // Set up workflow listener
        List<WorkflowListener> workflowListeners = Arrays.asList(mockWorkflowListener);
        when(mockWorkflowServiceDataHolder.getWorkflowListenerList()).thenReturn(workflowListeners);
        when(mockWorkflowListener.isEnable()).thenReturn(true);

        service = new WorkflowManagementServiceImpl();

        Field daoField = WorkflowManagementServiceImpl.class.getDeclaredField("workflowRequestDAO");
        daoField.setAccessible(true);
        daoField.set(service, mockDAO);

    }

    @AfterMethod
    public void tearDown() {

        mockedDataHolder.close();
        if (mockedIdentityTenantUtil != null) {
            mockedIdentityTenantUtil.close();
        }
    }

    @DataProvider(name = "validRequestData")
    public Object[][] provideValidRequestData() {

        return new Object[][]{
                {TEST_REQUEST_ID_1, CREATED_BY, OPERATION_UPDATE_USER, CREATED_AT_1, UPDATED_AT_1, STATUS_PENDING},
                {TEST_REQUEST_ID_2, CREATED_BY, OPERATION_ADD_USER, CREATED_AT_2, UPDATED_AT_2, STATUS_APPROVED},
                {TEST_REQUEST_ID_3, CREATED_BY, OPERATION_DELETE_USER, CREATED_AT_3, UPDATED_AT_3, STATUS_REJECTED}
        };
    }

    @Test(dataProvider = "validRequestData")
    public void testGetWorkflowRequestByValidRequestId(String requestId, String createdBy, String operation,
                                                       String createdAt, String updatedAt, String status)
            throws WorkflowException {

        WorkflowRequest expectedRequest = new WorkflowRequest();
        expectedRequest.setRequestId(requestId);
        expectedRequest.setCreatedBy(createdBy);
        expectedRequest.setOperationType(operation);
        expectedRequest.setCreatedAt(createdAt);
        expectedRequest.setUpdatedAt(updatedAt);
        expectedRequest.setStatus(status);

        when(mockDAO.getWorkflowRequest(requestId)).thenReturn(expectedRequest);

        WorkflowRequest result = service.getWorkflowRequestBean(requestId);

        assertNotNull(result, "Returned workflow request should not be null");
        assertEquals(result.getRequestId(), requestId, "Request ID should match");
        assertEquals(result.getOperationType(), operation, "Operation type should match");
        assertEquals(result.getStatus(), status, "Status should match");
        assertEquals(result.getCreatedBy(), createdBy, "Created by should match");

        verify(mockDAO).getWorkflowRequest(requestId);
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithInvalidId() throws Exception {

        when(mockDAO.getWorkflowRequest(INVALID_REQUEST_ID))
                .thenThrow(new WorkflowClientException("Invalid request ID"));
        service.getWorkflowRequestBean(INVALID_REQUEST_ID);
    }

    @Test(expectedExceptions = WorkflowClientException.class)
    public void testGetWorkflowRequestWithNullId() throws Exception {

        service.getWorkflowRequestBean(null);
    }

    private void injectMockDAO(String fieldName, Object mockObject) throws Exception {

        Field field = WorkflowManagementServiceImpl.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(workflowManagementService, mockObject);
    }

    @Test
    public void testGetWorkflow() throws WorkflowException {

        Workflow expectedWorkflow = createTestWorkflow();
        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(expectedWorkflow);

        Workflow result = workflowManagementService.getWorkflow(WORKFLOW_ID);

        assertNotNull(result);
        assertEquals(result.getWorkflowId(), WORKFLOW_ID);
        assertEquals(result.getWorkflowName(), WORKFLOW_NAME);
        verify(mockWorkflowListener).doPreGetWorkflow(WORKFLOW_ID);
        verify(mockWorkflowListener).doPostGetWorkflow(WORKFLOW_ID, expectedWorkflow);
    }

    @Test
    public void testGetWorkflowNotFound() throws WorkflowException {

        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(null);
        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.getWorkflow(WORKFLOW_ID));
    }

    @Test
    public void testIsWorkflowExistByName() throws WorkflowException {

        Workflow existingWorkflow = createTestWorkflow();
        when(mockWorkflowDAO.getWorkflowByName(WORKFLOW_NAME, TENANT_ID)).thenReturn(existingWorkflow);

        boolean result = workflowManagementService.isWorkflowExistByName(WORKFLOW_NAME, TENANT_DOMAIN);

        assertTrue(result);
        verify(mockWorkflowDAO).getWorkflowByName(WORKFLOW_NAME, TENANT_ID);
    }

    @Test
    public void testIsWorkflowExistByNameNotFound() throws WorkflowException {

        when(mockWorkflowDAO.getWorkflowByName(WORKFLOW_NAME, TENANT_ID)).thenReturn(null);

        boolean result = workflowManagementService.isWorkflowExistByName(WORKFLOW_NAME, TENANT_DOMAIN);

        assertFalse(result);
    }

    @Test
    public void testGetWorkflowParameters() throws WorkflowException {

        List<Parameter> expectedParameters = createTestParameters();
        when(mockWorkflowDAO.getWorkflowParams(WORKFLOW_ID)).thenReturn(expectedParameters);

        List<Parameter> result = workflowManagementService.getWorkflowParameters(WORKFLOW_ID);

        assertNotNull(result);
        assertEquals(result.size(), 2);
        verify(mockWorkflowListener).doPreGetWorkflowParameters(WORKFLOW_ID);
        verify(mockWorkflowListener).doPostGetWorkflowParameters(WORKFLOW_ID, expectedParameters);
    }

    @Test
    public void testListWorkflowEvents() {

        List<WorkflowRequestHandler> requestHandlers = Arrays.asList(mockWorkflowRequestHandler);
        when(mockWorkflowServiceDataHolder.listRequestHandlers()).thenReturn(requestHandlers);
        when(mockWorkflowRequestHandler.getEventId()).thenReturn(EVENT_ID);
        when(mockWorkflowRequestHandler.getFriendlyName()).thenReturn("Friendly Name");
        when(mockWorkflowRequestHandler.getDescription()).thenReturn("Description");
        when(mockWorkflowRequestHandler.getCategory()).thenReturn("Category");
        when(mockWorkflowRequestHandler.getParamDefinitions()).thenReturn(Collections.emptyMap());

        List<WorkflowEvent> result = workflowManagementService.listWorkflowEvents();

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getEventId(), EVENT_ID);
        verify(mockWorkflowListener).doPreListWorkflowEvents();
        verify(mockWorkflowListener).doPostListWorkflowEvents(result);
    }

    @Test
    public void testGetEvent() {

        when(mockWorkflowServiceDataHolder.getRequestHandler(EVENT_ID)).thenReturn(mockWorkflowRequestHandler);
        when(mockWorkflowRequestHandler.getEventId()).thenReturn(EVENT_ID);
        when(mockWorkflowRequestHandler.getFriendlyName()).thenReturn("Friendly Name");
        when(mockWorkflowRequestHandler.getDescription()).thenReturn("Description");
        when(mockWorkflowRequestHandler.getCategory()).thenReturn("Category");
        when(mockWorkflowRequestHandler.getParamDefinitions()).thenReturn(Collections.emptyMap());

        WorkflowEvent result = workflowManagementService.getEvent(EVENT_ID);

        assertNotNull(result);
        assertEquals(result.getEventId(), EVENT_ID);
        verify(mockWorkflowListener).doPreGetEvent(EVENT_ID);
        verify(mockWorkflowListener).doPostGetEvent(EVENT_ID, result);
    }

    @Test
    public void testGetEventNotFound() {

        when(mockWorkflowServiceDataHolder.getRequestHandler(EVENT_ID)).thenReturn(null);

        WorkflowEvent result = workflowManagementService.getEvent(EVENT_ID);

        assertEquals(result, null);
    }

    @Test
    public void testListTemplates() throws WorkflowException {

        Map<String, AbstractTemplate> templateMap = new HashMap<>();
        templateMap.put(TEMPLATE_ID, mockAbstractTemplate);
        when(mockWorkflowServiceDataHolder.getTemplates()).thenReturn(templateMap);
        when(mockAbstractTemplate.getTemplateId()).thenReturn(TEMPLATE_ID);
        when(mockAbstractTemplate.getName()).thenReturn("Template Name");
        when(mockAbstractTemplate.getDescription()).thenReturn("Template Description");
        when(mockAbstractTemplate.getParametersMetaData()).thenReturn(new ParametersMetaData());

        List<Template> result = workflowManagementService.listTemplates();

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getTemplateId(), TEMPLATE_ID);
        verify(mockWorkflowListener).doPreListTemplates();
        verify(mockWorkflowListener).doPostListTemplates(result);
    }

    @Test
    public void testGetTemplate() throws WorkflowException {

        Map<String, AbstractTemplate> templateMap = new HashMap<>();
        templateMap.put(TEMPLATE_ID, mockAbstractTemplate);
        when(mockWorkflowServiceDataHolder.getTemplates()).thenReturn(templateMap);
        when(mockAbstractTemplate.getTemplateId()).thenReturn(TEMPLATE_ID);
        when(mockAbstractTemplate.getName()).thenReturn("Template Name");
        when(mockAbstractTemplate.getDescription()).thenReturn("Template Description");
        when(mockAbstractTemplate.getParametersMetaData()).thenReturn(new ParametersMetaData());

        Template result = workflowManagementService.getTemplate(TEMPLATE_ID);

        assertNotNull(result);
        assertEquals(result.getTemplateId(), TEMPLATE_ID);
        verify(mockWorkflowListener).doPreGetTemplate(TEMPLATE_ID);
        verify(mockWorkflowListener).doPostGetTemplate(TEMPLATE_ID, result);
    }

    @Test
    public void testGetTemplateNotFound() throws WorkflowException {

        when(mockWorkflowServiceDataHolder.getTemplates()).thenReturn(Collections.emptyMap());

        Template result = workflowManagementService.getTemplate(TEMPLATE_ID);

        assertEquals(result, null);
    }

    @Test
    public void testListWorkflowImpls() throws WorkflowException {

        Map<String, AbstractWorkflow> workflowImplMap = new HashMap<>();
        workflowImplMap.put(DEFAULT_WORKFLOW_IMPL_ID, mockAbstractWorkflow);
        Map<String, Map<String, AbstractWorkflow>> workflowImpls = new HashMap<>();
        workflowImpls.put(TEMPLATE_ID, workflowImplMap);
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(workflowImpls);
        when(mockAbstractWorkflow.getWorkflowImplId()).thenReturn(DEFAULT_WORKFLOW_IMPL_ID);
        when(mockAbstractWorkflow.getWorkflowImplName()).thenReturn("Workflow Impl Name");
        when(mockAbstractWorkflow.getParametersMetaData()).thenReturn(new ParametersMetaData());
        when(mockAbstractWorkflow.getTemplateId()).thenReturn(TEMPLATE_ID);

        List<WorkflowImpl> result = workflowManagementService.listWorkflowImpls(TEMPLATE_ID);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getWorkflowImplId(), DEFAULT_WORKFLOW_IMPL_ID);
        verify(mockWorkflowListener).doPreListWorkflowImpls(TEMPLATE_ID);
        verify(mockWorkflowListener).doPostListWorkflowImpls(TEMPLATE_ID, result);
    }

    @Test
    public void testGetWorkflowImpl() throws WorkflowException {

        Map<String, AbstractWorkflow> workflowImplMap = new HashMap<>();
        workflowImplMap.put(DEFAULT_WORKFLOW_IMPL_ID, mockAbstractWorkflow);
        Map<String, Map<String, AbstractWorkflow>> workflowImpls = new HashMap<>();
        workflowImpls.put(TEMPLATE_ID, workflowImplMap);
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(workflowImpls);
        when(mockAbstractWorkflow.getWorkflowImplId()).thenReturn(DEFAULT_WORKFLOW_IMPL_ID);
        when(mockAbstractWorkflow.getWorkflowImplName()).thenReturn("Workflow Impl Name");
        when(mockAbstractWorkflow.getParametersMetaData()).thenReturn(new ParametersMetaData());
        when(mockAbstractWorkflow.getTemplateId()).thenReturn(TEMPLATE_ID);

        WorkflowImpl result = workflowManagementService.getWorkflowImpl(TEMPLATE_ID, DEFAULT_WORKFLOW_IMPL_ID);

        assertNotNull(result);
        assertEquals(result.getWorkflowImplId(), DEFAULT_WORKFLOW_IMPL_ID);
        verify(mockWorkflowListener).doPreGetWorkflowImpl(TEMPLATE_ID, DEFAULT_WORKFLOW_IMPL_ID);
        verify(mockWorkflowListener).doPostGetWorkflowImpl(TEMPLATE_ID, DEFAULT_WORKFLOW_IMPL_ID, result);
    }

    @DataProvider
    public Object[][] workflowImplementations() {

        return new Object[][]{
                {DEFAULT_WORKFLOW_IMPL_ID},
                {CUSTOM_WORKFLOW_IMPL_ID}
        };
    }

    @Test(dataProvider = "workflowImplementations")
    public void testAddWorkflow(String workflowImplementation) throws WorkflowException, UserStoreException {

        Workflow workflow = createTestWorkflow();
        workflow.setWorkflowImplId(workflowImplementation);

        List<Parameter> parameters = createTestParameters();

        Map<String, AbstractWorkflow> workflowImplMap = new HashMap<>();
        workflowImplMap.put(workflowImplementation, mockAbstractWorkflow);
        Map<String, Map<String, AbstractWorkflow>> workflowImpls = new HashMap<>();
        workflowImpls.put(TEMPLATE_ID, workflowImplMap);
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(workflowImpls);

        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(null); // First time creation

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            UserRealm mockUserRealm = mock(UserRealm.class);
            UserStoreManager mockUserStoreManager = mock(UserStoreManager.class);

            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
            when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

            workflowManagementService.addWorkflow(workflow, parameters, TENANT_ID);
        }

        verify(mockAbstractWorkflow).deploy(any());
        verify(mockWorkflowDAO).addWorkflow(eq(workflow), eq(TENANT_ID));
        verify(mockWorkflowDAO).addWorkflowParams(any(), eq(WORKFLOW_ID), eq(TENANT_ID));
        verify(mockWorkflowListener).doPreAddWorkflow(eq(workflow), any(), eq(TENANT_ID));
        verify(mockWorkflowListener).doPostAddWorkflow(eq(workflow), any(), eq(TENANT_ID));
    }

    @Test(dataProvider = "workflowImplementations")
    public void testAddWorkflowUpdate(String workflowImplementation) throws WorkflowException, UserStoreException {

        Workflow workflow = createTestWorkflow();
        workflow.setWorkflowImplId(workflowImplementation);

        Workflow existingWorkflow = createTestWorkflow();
        existingWorkflow.setWorkflowName("Old Name");
        List<Parameter> parameters = createTestParameters();

        Map<String, AbstractWorkflow> workflowImplMap = new HashMap<>();
        workflowImplMap.put(workflowImplementation, mockAbstractWorkflow);
        Map<String, Map<String, AbstractWorkflow>> workflowImpls = new HashMap<>();
        workflowImpls.put(TEMPLATE_ID, workflowImplMap);
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(workflowImpls);

        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(existingWorkflow);

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            UserRealm mockUserRealm = mock(UserRealm.class);
            UserStoreManager mockUserStoreManager = mock(UserStoreManager.class);

            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
            when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

            workflowManagementService.addWorkflow(workflow, parameters, TENANT_ID);
        }

        verify(mockAbstractWorkflow).deploy(any());
        verify(mockWorkflowDAO).removeWorkflowParams(WORKFLOW_ID);
        verify(mockWorkflowDAO).updateWorkflow(workflow);
        verify(mockWorkflowDAO).addWorkflowParams(any(), eq(WORKFLOW_ID), eq(TENANT_ID));
    }

    @Test
    public void testAddWorkflowInvalidTemplate() {

        Workflow workflow = createTestWorkflow();
        List<Parameter> parameters = createTestParameters();
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(Collections.emptyMap());

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.addWorkflow(workflow, parameters, TENANT_ID));
    }

    @Test
    public void testAddWorkflowInvalidImpl() {

        Workflow workflow = createTestWorkflow();
        List<Parameter> parameters = createTestParameters();

        Map<String, Map<String, AbstractWorkflow>> workflowImpls = new HashMap<>();
        workflowImpls.put(TEMPLATE_ID, Collections.emptyMap());
        when(mockWorkflowServiceDataHolder.getWorkflowImpls()).thenReturn(workflowImpls);

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.addWorkflow(workflow, parameters, TENANT_ID));
    }

    @Test
    public void testAddAssociation() throws WorkflowException {

        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
        verify(mockWorkflowListener).doPreAddAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
        verify(mockWorkflowListener).doPostAddAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
    }

    @Test
    public void testAddAssociationWithNullCondition() throws WorkflowException {

        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, null);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID,
                WFConstant.DEFAULT_ASSOCIATION_CONDITION);
    }

    @Test
    public void testAddAssociationBlankWorkflowId() {

        assertThrows(InternalWorkflowException.class, () ->
                workflowManagementService.addAssociation(ASSOCIATION_NAME, "", EVENT_ID, CONDITION));
    }

    @Test
    public void testAddAssociationBlankEventId() {

        assertThrows(InternalWorkflowException.class, () ->
                workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, "", CONDITION));
    }

    @Test
    public void testAddAssociationBlankCondition() {

        assertThrows(InternalWorkflowException.class, () ->
                workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, ""));
    }

    /**
     * Test addAssociation with duplicate association detection - should throw WorkflowClientException.
     */
    @Test
    public void testAddAssociationDuplicateDetection() throws WorkflowException {

        // Create existing association with same event and condition
        Association existingAssociation = new Association();
        existingAssociation.setEventId(EVENT_ID);
        existingAssociation.setCondition(CONDITION);
        existingAssociation.setWorkflowId(WORKFLOW_ID);

        List<Association> existingAssociations = Arrays.asList(existingAssociation);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(existingAssociations);

        // Attempt to add duplicate association should throw exception
        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION));

        // Verify that the duplicate was detected before any DAO operations
        verify(mockAssociationDAO).listAssociationsForWorkflow(WORKFLOW_ID);
        verify(mockAssociationDAO, times(0)).addAssociation(any(), any(), any(), any());
    }

    /**
     * Test addAssociation with null existing associations list.
     */
    @Test
    public void testAddAssociationWithNullExistingAssociations() throws WorkflowException {

        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(null);

        // Should succeed when no existing associations
        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
    }

    /**
     * Test addAssociation with empty existing associations list.
     */
    @Test
    public void testAddAssociationWithEmptyExistingAssociations() throws WorkflowException {

        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(Collections.emptyList());

        // Should succeed when no existing associations
        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
    }

    /**
     * Test addAssociation with different event ID should succeed.
     */
    @Test
    public void testAddAssociationDifferentEventId() throws WorkflowException {

        // Create existing association with different event ID
        Association existingAssociation = new Association();
        existingAssociation.setEventId("different-event-id");
        existingAssociation.setCondition(CONDITION);
        existingAssociation.setWorkflowId(WORKFLOW_ID);

        List<Association> existingAssociations = Arrays.asList(existingAssociation);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(existingAssociations);

        // Should succeed with different event ID
        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
    }

    /**
     * Test addAssociation with different condition should succeed.
     */
    @Test
    public void testAddAssociationDifferentCondition() throws WorkflowException {

        // Create existing association with different condition
        Association existingAssociation = new Association();
        existingAssociation.setEventId(EVENT_ID);
        existingAssociation.setCondition("//different[@condition='test']");
        existingAssociation.setWorkflowId(WORKFLOW_ID);

        List<Association> existingAssociations = Arrays.asList(existingAssociation);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(existingAssociations);

        // Should succeed with different condition
        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, CONDITION);
    }

    /**
     * Test addAssociation with null association in existing list.
     */
    @Test
    public void testAddAssociationWithNullAssociationInList() throws WorkflowException {

        // Create list with null association
        List<Association> existingAssociations = new ArrayList<>();
        existingAssociations.add(null);
        existingAssociations.add(createTestAssociation());

        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(existingAssociations);

        // Should succeed as null associations are filtered out
        workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, "different-event", CONDITION);

        verify(mockAssociationDAO).addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, "different-event", CONDITION);
    }

    /**
     * Test updateAssociation with duplicate detection for new values.
     */
    @Test
    public void testUpdateAssociationDuplicateDetection() throws WorkflowException {

        Association currentAssociation = createTestAssociation();
        currentAssociation.setAssociationId("1");

        // Create another existing association that would conflict with update.
        Association conflictingAssociation = new Association();
        conflictingAssociation.setAssociationId("2");
        conflictingAssociation.setEventId("ADD_USER");
        conflictingAssociation.setCondition(WFConstant.DEFAULT_ASSOCIATION_CONDITION);
        conflictingAssociation.setWorkflowId(WORKFLOW_ID);

        List<Association> existingAssociations = Arrays.asList(currentAssociation, conflictingAssociation);

        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(currentAssociation);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(existingAssociations);

        // Attempt to update to conflicting values should throw exception.
        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.updateAssociation(ASSOCIATION_ID, "New Name", WORKFLOW_ID,
                        "ADD_USER", WFConstant.DEFAULT_ASSOCIATION_CONDITION, true));

        verify(mockAssociationDAO).getAssociation(ASSOCIATION_ID);
        verify(mockAssociationDAO).listAssociationsForWorkflow(WORKFLOW_ID);
        verify(mockAssociationDAO, times(0)).updateAssociation(any());
    }

    /**
     * Test updateAssociation succeeds when no duplicates exist.
     */
    @Test
    public void testUpdateAssociationNoDuplicates() throws WorkflowException {

        Association association = createTestAssociation();

        // Empty existing associations list - no conflicts.
        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(association);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(Collections.emptyList());

        workflowManagementService.updateAssociation(ASSOCIATION_ID, "New Name", WORKFLOW_ID,
                "new-event-id", WFConstant.DEFAULT_ASSOCIATION_CONDITION, false);

        verify(mockAssociationDAO).updateAssociation(association);
        assertEquals(association.getAssociationName(), "New Name");
        assertEquals(association.getEventId(), "new-event-id");
        assertFalse(association.isEnabled());
    }

    /**
     * Test addAssociation with invalid XPath expression should throw WorkflowRuntimeException.
     */
    @Test
    public void testAddAssociationInvalidXPath() throws WorkflowException {

        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(Collections.emptyList());

        String invalidXPath = "//invalid[xpath expression";

        assertThrows(WorkflowRuntimeException.class, () ->
                workflowManagementService.addAssociation(ASSOCIATION_NAME, WORKFLOW_ID, EVENT_ID, invalidXPath));

        verify(mockAssociationDAO, times(0)).addAssociation(any(), any(), any(), any());
    }

    @Test
    public void testListPaginatedWorkflows() throws WorkflowException {

        List<Workflow> expectedWorkflows = Arrays.asList(createTestWorkflow());
        when(mockWorkflowDAO.listPaginatedWorkflows(TENANT_ID, WFConstant.DEFAULT_FILTER, 0, 10))
                .thenReturn(expectedWorkflows);

        List<Workflow> result = workflowManagementService.listPaginatedWorkflows(TENANT_ID, 10, 0, null);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        verify(mockWorkflowListener).doPreListPaginatedWorkflows(TENANT_ID, 10, 0, null);
        verify(mockWorkflowListener).doPostListPaginatedWorkflows(TENANT_ID, 10, 0, WFConstant.DEFAULT_FILTER,
                expectedWorkflows);
    }

    @Test
    public void testListPaginatedWorkflowsInvalidLimit() {

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.listPaginatedWorkflows(TENANT_ID, -1, 0, null));
    }

    @Test
    public void testListPaginatedWorkflowsInvalidOffset() {

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.listPaginatedWorkflows(TENANT_ID, 10, -1, null));
    }

    @Test
    public void testListWorkflows() throws WorkflowException {

        List<Workflow> expectedWorkflows = Arrays.asList(createTestWorkflow());
        when(mockWorkflowDAO.listWorkflows(TENANT_ID)).thenReturn(expectedWorkflows);

        List<Workflow> result = workflowManagementService.listWorkflows(TENANT_ID);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        verify(mockWorkflowListener).doPreListWorkflows(TENANT_ID);
        verify(mockWorkflowListener).doPostListWorkflows(TENANT_ID, expectedWorkflows);
    }

    @Test
    public void testGetWorkflowsCount() throws WorkflowException {

        when(mockWorkflowDAO.getWorkflowsCount(TENANT_ID, WFConstant.DEFAULT_FILTER)).thenReturn(5);

        int result = workflowManagementService.getWorkflowsCount(TENANT_ID, null);

        assertEquals(result, 5);
        verify(mockWorkflowDAO).getWorkflowsCount(TENANT_ID, WFConstant.DEFAULT_FILTER);
    }

    @Test(dataProvider = "workflowImplementations")
    public void testRemoveWorkflow(String workflowImplementation) throws WorkflowException, UserStoreException {

        Workflow workflow = createTestWorkflow();
        workflow.setWorkflowImplId(workflowImplementation);

        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(workflow);

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            UserRealm mockUserRealm = mock(UserRealm.class);
            UserStoreManager mockUserStoreManager = mock(UserStoreManager.class);

            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUserRealm()).thenReturn(mockUserRealm);
            when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);

            workflowManagementService.removeWorkflow(WORKFLOW_ID);
        }
        verify(mockWorkflowDAO).removeWorkflowParams(WORKFLOW_ID);
        verify(mockWorkflowDAO).removeWorkflow(WORKFLOW_ID);
        verify(mockWorkflowListener).doPreDeleteWorkflow(workflow);
        verify(mockWorkflowListener).doPostDeleteWorkflow(workflow);
    }

    @Test
    public void testRemoveWorkflowNotFound() throws WorkflowException {

        when(mockWorkflowDAO.getWorkflow(WORKFLOW_ID)).thenReturn(null);

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.removeWorkflow(WORKFLOW_ID));
    }

    @Test
    public void testRemoveWorkflows() throws WorkflowException {

        workflowManagementService.removeWorkflows(TENANT_ID);

        verify(mockWorkflowDAO).removeWorkflowParams(TENANT_ID);
        verify(mockWorkflowDAO).removeWorkflows(TENANT_ID);
        verify(mockWorkflowListener).doPreDeleteWorkflows(TENANT_ID);
        verify(mockWorkflowListener).doPostDeleteWorkflows(TENANT_ID);
    }

    @Test
    public void testRemoveAssociation() throws WorkflowException {

        workflowManagementService.removeAssociation(Integer.parseInt(ASSOCIATION_ID));

        verify(mockAssociationDAO).removeAssociation(Integer.parseInt(ASSOCIATION_ID));
        verify(mockWorkflowListener).doPreRemoveAssociation(Integer.parseInt(ASSOCIATION_ID));
        verify(mockWorkflowListener).doPostRemoveAssociation(Integer.parseInt(ASSOCIATION_ID));
    }

    @Test
    public void testGetAssociationsForWorkflow() throws WorkflowException {

        Association association = createTestAssociation();
        List<Association> associations = Arrays.asList(association);
        when(mockAssociationDAO.listAssociationsForWorkflow(WORKFLOW_ID)).thenReturn(associations);
        when(mockWorkflowServiceDataHolder.getRequestHandler(EVENT_ID)).thenReturn(mockWorkflowRequestHandler);
        when(mockWorkflowRequestHandler.getFriendlyName()).thenReturn("Friendly Name");

        List<Association> result = workflowManagementService.getAssociationsForWorkflow(WORKFLOW_ID);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getEventName(), "Friendly Name");
        verify(mockWorkflowListener).doPreGetAssociationsForWorkflow(WORKFLOW_ID);
        verify(mockWorkflowListener).doPostGetAssociationsForWorkflow(WORKFLOW_ID, result);
    }

    @Test
    public void testListPaginatedAssociations() throws WorkflowException {

        Association association = createTestAssociation();
        List<Association> associations = Arrays.asList(association);
        when(mockAssociationDAO.listPaginatedAssociations(TENANT_ID, WFConstant.DEFAULT_FILTER, 0, 10))
                .thenReturn(associations);
        when(mockWorkflowServiceDataHolder.getRequestHandler(EVENT_ID)).thenReturn(mockWorkflowRequestHandler);
        when(mockWorkflowRequestHandler.getFriendlyName()).thenReturn("Friendly Name");

        List<Association> result = workflowManagementService.listPaginatedAssociations(TENANT_ID, 10, 0, null);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        verify(mockWorkflowListener).doPreListPaginatedAssociations(TENANT_ID, 10, 0, null);
        verify(mockWorkflowListener).doPostListPaginatedAssociations(TENANT_ID, 10, 0, WFConstant.DEFAULT_FILTER,
                result);
    }

    @Test
    public void testListPaginatedAssociationsInvalidLimit() {

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.listPaginatedAssociations(TENANT_ID, -1, 0, null));
    }

    @Test
    public void testListPaginatedAssociationsInvalidOffset() {

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.listPaginatedAssociations(TENANT_ID, 10, -1, null));
    }

    @Test
    public void testListAllAssociations() throws WorkflowException {

        Association association = createTestAssociation();
        List<Association> associations = Arrays.asList(association);
        when(mockAssociationDAO.listAssociations(TENANT_ID)).thenReturn(associations);
        when(mockWorkflowServiceDataHolder.getRequestHandler(EVENT_ID)).thenReturn(mockWorkflowRequestHandler);
        when(mockWorkflowRequestHandler.getFriendlyName()).thenReturn("Friendly Name");

        List<Association> result = workflowManagementService.listAllAssociations(TENANT_ID);

        assertNotNull(result);
        assertEquals(result.size(), 1);
        verify(mockWorkflowListener).doPreListAllAssociations(TENANT_ID);
        verify(mockWorkflowListener).doPostListAllAssociations(TENANT_ID, result);
    }

    @Test
    public void testGetAssociation() throws WorkflowException {

        Association association = createTestAssociation();
        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(association);

        Association result = workflowManagementService.getAssociation(ASSOCIATION_ID);

        assertNotNull(result);
        assertEquals(result.getAssociationId(), ASSOCIATION_ID);
        verify(mockWorkflowListener).doPreGetAssociation(ASSOCIATION_ID);
        verify(mockWorkflowListener).doPostGetAssociation(ASSOCIATION_ID);
    }

    @Test
    public void testGetAssociationNotFound() throws WorkflowException {

        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(null);

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.getAssociation(ASSOCIATION_ID));
    }

    @Test
    public void testGetAssociationsCount() throws WorkflowException {

        when(mockAssociationDAO.getAssociationsCount(TENANT_ID, WFConstant.DEFAULT_FILTER)).thenReturn(3);

        int result = workflowManagementService.getAssociationsCount(TENANT_ID, null);

        assertEquals(result, 3);
        verify(mockAssociationDAO).getAssociationsCount(TENANT_ID, WFConstant.DEFAULT_FILTER);
    }

    @Test
    public void testChangeAssociationState() throws WorkflowException {

        Association association = createTestAssociation();
        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(association);

        workflowManagementService.changeAssociationState(ASSOCIATION_ID, true);

        verify(mockAssociationDAO).updateAssociation(association);
        verify(mockWorkflowListener).doPreChangeAssociationState(ASSOCIATION_ID, true);
        verify(mockWorkflowListener).doPostChangeAssociationState(ASSOCIATION_ID, true);
        assertTrue(association.isEnabled());
    }

    @Test
    public void testUpdateAssociation() throws WorkflowException {

        Association association = createTestAssociation();
        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(association);

        workflowManagementService.updateAssociation(ASSOCIATION_ID, "New Name", "new-workflow-id",
                "new-event-id", WFConstant.DEFAULT_ASSOCIATION_CONDITION, false);

        verify(mockAssociationDAO).updateAssociation(association);
        assertEquals(association.getAssociationName(), "New Name");
        assertEquals(association.getWorkflowId(), "new-workflow-id");
        assertEquals(association.getEventId(), "new-event-id");
        assertFalse(association.isEnabled());
    }

    @Test
    public void testUpdateAssociationNotFound() throws WorkflowException {

        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(null);

        assertThrows(WorkflowClientException.class, () ->
                workflowManagementService.updateAssociation(ASSOCIATION_ID, "New Name", null, null, null, true));
    }

    @Test
    public void testUpdateAssociationInvalidCondition() throws WorkflowException {

        Association association = createTestAssociation();
        when(mockAssociationDAO.getAssociation(ASSOCIATION_ID)).thenReturn(association);

        assertThrows(WorkflowRuntimeException.class, () ->
                workflowManagementService.updateAssociation(ASSOCIATION_ID, null, null, null, "invalid condition",
                        true));
    }

    @Test
    public void testAddRequestEntityRelationships() throws WorkflowException {

        Entity[] entities = {createTestEntity()};

        workflowManagementService.addRequestEntityRelationships(REQUEST_ID, entities);

        verify(mockRequestEntityRelationshipDAO).addRelationship(entities[0], REQUEST_ID);
        verify(mockWorkflowListener).doPreAddRequestEntityRelationships(REQUEST_ID, entities);
        verify(mockWorkflowListener).doPostAddRequestEntityRelationships(REQUEST_ID, entities);
    }

    @Test
    public void testEntityHasPendingWorkflows() throws WorkflowException {

        Entity entity = createTestEntity();
        when(mockRequestEntityRelationshipDAO.entityHasPendingWorkflows(entity)).thenReturn(true);

        boolean result = workflowManagementService.entityHasPendingWorkflows(entity);

        assertTrue(result);
        verify(mockWorkflowListener).doPreEntityHasPendingWorkflows(entity);
        verify(mockWorkflowListener).doPostEntityHasPendingWorkflows(entity);
    }

    @Test
    public void testEntityHasPendingWorkflowsOfType() throws WorkflowException {

        Entity entity = createTestEntity();
        String requestType = "ADD_USER";
        when(mockRequestEntityRelationshipDAO.entityHasPendingWorkflowsOfType(entity, requestType)).thenReturn(true);

        boolean result = workflowManagementService.entityHasPendingWorkflowsOfType(entity, requestType);

        assertTrue(result);
        verify(mockWorkflowListener).doPreEntityHasPendingWorkflowsOfType(entity, requestType);
        verify(mockWorkflowListener).doPostEntityHasPendingWorkflowsOfType(entity, requestType);
    }

    @Test
    public void testAreTwoEntitiesRelated() throws WorkflowException {

        Entity entity1 = createTestEntity();
        Entity entity2 = createTestEntity();
        when(mockRequestEntityRelationshipDAO.twoEntitiesAreRelated(entity1, entity2)).thenReturn(true);
        boolean result = workflowManagementService.areTwoEntitiesRelated(entity1, entity2);
        assertTrue(result);
        verify(mockWorkflowListener).doPreAreTwoEntitiesRelated(entity1, entity2);
        verify(mockWorkflowListener).doPostAreTwoEntitiesRelated(entity1, entity2);
    }

    @Test
    public void testIsEventAssociated() throws WorkflowException {

        List<WorkflowAssociation> associations = Arrays.asList(new WorkflowAssociation());
        when(mockWorkflowRequestAssociationDAO.getWorkflowAssociationsForRequest(EVENT_ID, TENANT_ID))
                .thenReturn(associations);

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantId()).thenReturn(TENANT_ID);

            boolean result = workflowManagementService.isEventAssociated(EVENT_ID);
            assertTrue(result);
            verify(mockWorkflowListener, times(2)).doPreIsEventAssociated(EVENT_ID);
        }
    }

    @Test
    public void testIsEventAssociatedEmpty() throws WorkflowException {

        when(mockWorkflowRequestAssociationDAO.getWorkflowAssociationsForRequest(EVENT_ID, TENANT_ID))
                .thenReturn(Collections.emptyList());

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantId()).thenReturn(TENANT_ID);

            boolean result = workflowManagementService.isEventAssociated(EVENT_ID);
            assertFalse(result);
        }
    }

    @Test
    public void testGetRequestsCreatedByUser() throws WorkflowException {

        WorkflowRequest[] expectedRequests = {createTestWorkflowRequest()};
        when(mockWorkflowRequestDAO.getRequestsOfUser(USER_NAME, TENANT_ID)).thenReturn(expectedRequests);

        WorkflowRequest[] result = workflowManagementService.getRequestsCreatedByUser(USER_NAME, TENANT_ID);

        assertNotNull(result);
        assertEquals(result.length, 1);
        verify(mockWorkflowListener).doPreGetRequestsCreatedByUser(USER_NAME, TENANT_ID);
        verify(mockWorkflowListener).doPostGetRequestsCreatedByUser(USER_NAME, TENANT_ID, expectedRequests);
    }

    @Test
    public void testGetWorkflowsOfRequest() throws WorkflowException {

        WorkflowRequestAssociation[] expectedAssociations = {new WorkflowRequestAssociation()};
        when(mockWorkflowRequestAssociationDAO.getWorkflowsOfRequest(REQUEST_ID)).thenReturn(expectedAssociations);

        WorkflowRequestAssociation[] result = workflowManagementService.getWorkflowsOfRequest(REQUEST_ID);

        assertNotNull(result);
        assertEquals(result.length, 1);
        verify(mockWorkflowListener).doPreGetWorkflowsOfRequest(REQUEST_ID);
        verify(mockWorkflowListener).doPostGetWorkflowsOfRequest(REQUEST_ID, expectedAssociations);
    }

    @Test
    public void testDeleteWorkflowRequest() throws WorkflowException {

        when(mockWorkflowRequestDAO.retrieveCreatedUserOfRequest(REQUEST_ID)).thenReturn(USER_NAME);

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUsername()).thenReturn(USER_NAME);

            workflowManagementService.deleteWorkflowRequest(REQUEST_ID);

            verify(mockWorkflowRequestDAO).updateStatusOfRequest(REQUEST_ID, WorkflowRequestStatus.DELETED.toString());
            verify(mockWorkflowRequestAssociationDAO).updateStatusOfRelationshipsOfPendingRequest(REQUEST_ID,
                    WFConstant.HT_STATE_SKIPPED);
            verify(mockRequestEntityRelationshipDAO).deleteRelationshipsOfRequest(REQUEST_ID);
            verify(mockWorkflowListener).doPreDeleteWorkflowRequest(any(WorkflowRequest.class));
            verify(mockWorkflowListener).doPostDeleteWorkflowRequest(any(WorkflowRequest.class));
        }
    }

    @Test
    public void testDeleteWorkflowRequestUnauthorized() throws WorkflowException {

        when(mockWorkflowRequestDAO.retrieveCreatedUserOfRequest(REQUEST_ID)).thenReturn("different-user");

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getUsername()).thenReturn(USER_NAME);

            assertThrows(WorkflowException.class, () ->
                    workflowManagementService.deleteWorkflowRequest(REQUEST_ID));
        }
    }

    @Test
    public void testDeleteWorkflowRequestCreatedByAnyUser() throws WorkflowException {

        when(mockWorkflowRequestDAO.retrieveCreatedUserOfRequest(REQUEST_ID)).thenReturn("any-user");

        workflowManagementService.deleteWorkflowRequestCreatedByAnyUser(REQUEST_ID);

        verify(mockWorkflowRequestDAO).updateStatusOfRequest(REQUEST_ID, WorkflowRequestStatus.DELETED.toString());
        verify(mockWorkflowRequestAssociationDAO).updateStatusOfRelationshipsOfPendingRequest(REQUEST_ID,
                WFConstant.HT_STATE_SKIPPED);
        verify(mockRequestEntityRelationshipDAO).deleteRelationshipsOfRequest(REQUEST_ID);

        workflowManagementService.permanentlyDeleteWorkflowRequestByAnyUser(REQUEST_ID);

    }

    @Test
    public void testGetRequestsFromFilter() throws WorkflowException {

        String beginDate = "2025-01-01:00:00:00.000";
        String endDate = "2025-01-31:23:59:59.999";
        String dateCategory = "created";
        String status = "PENDING";
        WorkflowRequest[] expectedRequests = {createTestWorkflowRequest()};
        org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestFilterResponse expectedResponse =
                new org.wso2.carbon.identity.workflow.mgt.bean.WorkflowRequestFilterResponse(expectedRequests, 1);
        when(mockWorkflowRequestDAO.getFilteredRequests(eq(USER_NAME), eq((String) null), any(String.class),
                any(String.class), eq(dateCategory), eq(TENANT_ID), eq(status), eq(1000), eq(0)))
                .thenReturn(expectedResponse);

        WorkflowRequest[] result = workflowManagementService.getRequestsFromFilter(USER_NAME, beginDate, endDate,
                dateCategory, TENANT_ID, status);

        assertNotNull(result);
        assertEquals(result.length, 1);
        verify(mockWorkflowListener).doPreGetRequestsFromFilter(USER_NAME, null, beginDate, endDate, dateCategory,
                TENANT_ID, status, 1000, 0);
        verify(mockWorkflowListener).doPostGetRequestsFromFilter(USER_NAME, null, beginDate, endDate, dateCategory,
                TENANT_ID, status, 1000, 0, expectedResponse);
    }

    @Test
    public void testListEntityNames() throws WorkflowException {

        String operationType = "ADD_USER";
        String status = "PENDING";
        String entityType = "USER";
        String idFilter = "test";
        List<String> expectedEntityNames = Arrays.asList("entity1", "entity2");
        when(mockRequestEntityRelationshipDAO.getEntityNamesOfRequest(operationType, status, entityType, idFilter,
                TENANT_ID))
                .thenReturn(expectedEntityNames);

        List<String> result =
                workflowManagementService.listEntityNames(operationType, status, entityType, TENANT_ID, idFilter);

        assertNotNull(result);
        assertEquals(result.size(), 2);
        verify(mockWorkflowListener).doPreListEntityNames(operationType, status, entityType, TENANT_ID, idFilter);
        verify(mockWorkflowListener).doPostListEntityNames(operationType, status, entityType, TENANT_ID, idFilter,
                result);
    }

    @Test
    public void testGetWorkflowRequest() throws WorkflowException {

        org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest expectedRequest =
                new org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest();
        when(mockWorkflowRequestDAO.retrieveWorkflow(REQUEST_ID)).thenReturn(expectedRequest);

        org.wso2.carbon.identity.workflow.mgt.dto.WorkflowRequest result =
                workflowManagementService.getWorkflowRequest(REQUEST_ID);

        assertNotNull(result);
        assertEquals(result, expectedRequest);
    }

    /**
     * Test abortWorkflowRequest - happy path.
     * Verifies that workflow request is properly aborted with correct status updates and relationship deletions.
     */
    @Test
    public void testAbortWorkflowRequest() throws WorkflowException {

        String testRequestId = "test-request-abort-1";

        // Mock the workflow listener.
        List<WorkflowListener> workflowListeners = Arrays.asList(mockWorkflowListener);
        when(mockWorkflowServiceDataHolder.getWorkflowListenerList()).thenReturn(workflowListeners);
        when(mockWorkflowListener.isEnable()).thenReturn(true);

        // Execute the method.
        workflowManagementService.abortWorkflowRequest(testRequestId);

        // Verify pre-update listener is called.
        verify(mockWorkflowListener).doPreUpdateWorkflowRequest(any(WorkflowRequest.class));

        // Verify DAO methods are called with correct parameters.
        verify(mockWorkflowRequestDAO).updateStatusOfRequest(
                eq(testRequestId),
                eq(WorkflowRequestStatus.ABORTED.toString())
        );
        verify(mockWorkflowRequestAssociationDAO).updateStatusOfRelationshipsOfPendingRequest(
                eq(testRequestId),
                eq(WFConstant.HT_STATE_SKIPPED)
        );
        verify(mockRequestEntityRelationshipDAO).deleteRelationshipsOfRequest(eq(testRequestId));

        // Verify post-update listener is called.
        verify(mockWorkflowListener).doPostUpdateWorkflowRequest(any(WorkflowRequest.class));
    }

    // Helper methods for creating test objects
    private Workflow createTestWorkflow() {

        Workflow workflow = new Workflow();
        workflow.setWorkflowId(WORKFLOW_ID);
        workflow.setWorkflowName(WORKFLOW_NAME);
        workflow.setWorkflowDescription(WORKFLOW_DESCRIPTION);
        workflow.setTemplateId(TEMPLATE_ID);
        workflow.setWorkflowImplId(DEFAULT_WORKFLOW_IMPL_ID);
        return workflow;
    }

    private List<Parameter> createTestParameters() {

        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new Parameter(WORKFLOW_ID, "param1", "value1", "qname1", "holder1"));
        parameters.add(new Parameter(WORKFLOW_ID, "param2", "value2", "qname2", "holder2"));
        return parameters;
    }

    private Association createTestAssociation() {

        Association association = new Association();
        association.setAssociationId(ASSOCIATION_ID);
        association.setAssociationName(ASSOCIATION_NAME);
        association.setWorkflowId(WORKFLOW_ID);
        association.setEventId(EVENT_ID);
        association.setCondition(CONDITION);
        association.setEnabled(true);
        return association;
    }

    private Entity createTestEntity() {

        Entity entity = new Entity(ENTITY_ID, ENTITY_TYPE, TENANT_ID);
        return entity;
    }

    private WorkflowRequest createTestWorkflowRequest() {

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestId(REQUEST_ID);
        request.setCreatedBy(USER_NAME);
        return request;
    }

    @Test
    public void testGetWorkflowRequestBeanWithClaimsProcessing() throws Exception {

        WorkflowRequest mockWorkflowRequest = createWorkflowRequestWithClaims();
        when(mockDAO.getWorkflowRequest(REQUEST_ID)).thenReturn(mockWorkflowRequest);

        ClaimMetadataManagementService mockClaimService = mock(ClaimMetadataManagementService.class);
        List<LocalClaim> mockLocalClaims = createMockLocalClaims();
        when(mockClaimService.getLocalClaims(anyString())).thenReturn(mockLocalClaims);

        WorkflowServiceDataHolder mockHolder = mock(WorkflowServiceDataHolder.class);
        when(mockHolder.getClaimMetadataManagementService()).thenReturn(mockClaimService);
        List<WorkflowListener> listeners = Arrays.asList(mockWorkflowListener);
        when(mockHolder.getWorkflowListenerList()).thenReturn(listeners);

        try (MockedStatic<CarbonContext> mockedCarbonContext = mockStatic(CarbonContext.class)) {
            mockedDataHolder.when(WorkflowServiceDataHolder::getInstance).thenReturn(mockHolder);
            CarbonContext mockCarbonContext = mock(CarbonContext.class);
            mockedCarbonContext.when(CarbonContext::getThreadLocalCarbonContext).thenReturn(mockCarbonContext);
            when(mockCarbonContext.getTenantDomain()).thenReturn("carbon.super");

            WorkflowRequest result = service.getWorkflowRequestBean(REQUEST_ID);

            assertNotNull(result, "Result should not be null");
            assertEquals(result.getRequestId(), REQUEST_ID);
            assertNotNull(result.getProperties(), "Properties should not be null after processing");
            assertTrue(result.getProperties().size() > 0, "Should have processed properties");

            // Verify that claims were processed correctly
            boolean foundEmailProperty = false;
            boolean foundFirstNameProperty = false;

            for (Property property : result.getProperties()) {
                if ("Email".equals(property.getKey())) {
                    foundEmailProperty = true;
                    assertEquals(property.getValue(), "test@example.com");
                } else if ("First Name".equals(property.getKey())) {
                    foundFirstNameProperty = true;
                    assertEquals(property.getValue(), "John");
                }
            }

            assertTrue(foundEmailProperty, "Should find processed email property");
            assertTrue(foundFirstNameProperty, "Should find processed first name property");
            verify(mockClaimService).getLocalClaims("carbon.super");
        }

        verify(mockDAO).getWorkflowRequest(REQUEST_ID);
    }

    @Test
    public void testGetWorkflowRequestBeanWithCredentialFiltering() throws Exception {

        WorkflowRequest mockWorkflowRequest = createWorkflowRequestWithCredentials();
        when(mockDAO.getWorkflowRequest(REQUEST_ID)).thenReturn(mockWorkflowRequest);

        // Use existing mock infrastructure
        WorkflowServiceDataHolder mockHolder = mock(WorkflowServiceDataHolder.class);
        List<WorkflowListener> listeners = Arrays.asList(mockWorkflowListener);
        when(mockHolder.getWorkflowListenerList()).thenReturn(listeners);

        mockedDataHolder.when(WorkflowServiceDataHolder::getInstance).thenReturn(mockHolder);

        WorkflowRequest result = service.getWorkflowRequestBean(REQUEST_ID);

        assertNotNull(result);
        assertNotNull(result.getProperties());
        // Should have only 1 property (username), credential should be filtered out
        assertEquals(result.getProperties().size(), 1);
        assertEquals(result.getProperties().get(0).getKey(), "username");
        assertEquals(result.getProperties().get(0).getValue(), "testuser");

        verify(mockDAO).getWorkflowRequest(REQUEST_ID);
    }

    @Test
    public void testGetWorkflowRequestBeanWithEmptyProperties() throws Exception {

        WorkflowRequest mockWorkflowRequest = createWorkflowRequestWithEmptyProperties();
        when(mockDAO.getWorkflowRequest(REQUEST_ID)).thenReturn(mockWorkflowRequest);

        // Use existing mock infrastructure
        WorkflowServiceDataHolder mockHolder = mock(WorkflowServiceDataHolder.class);
        List<WorkflowListener> listeners = Arrays.asList(mockWorkflowListener);
        when(mockHolder.getWorkflowListenerList()).thenReturn(listeners);

        mockedDataHolder.when(WorkflowServiceDataHolder::getInstance).thenReturn(mockHolder);

        WorkflowRequest result = service.getWorkflowRequestBean(REQUEST_ID);

        assertNotNull(result);
        assertEquals(result.getRequestId(), REQUEST_ID);
        assertNotNull(result.getProperties());
        assertEquals(result.getProperties().size(), 0);

        verify(mockDAO).getWorkflowRequest(REQUEST_ID);
    }

    // Helper methods for creating test data

    private WorkflowRequest createWorkflowRequestWithClaims() {

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestId(REQUEST_ID);
        request.setCreatedBy("testuser");

        List<RequestParameter> parameters = new ArrayList<>();

        // Add claims parameter
        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put("http://wso2.org/claims/emailaddress", "test@example.com");
        claimsMap.put("http://wso2.org/claims/givenname", "John");

        RequestParameter claimsParam = new RequestParameter();
        claimsParam.setName(WFConstant.CLAIMS_PROPERTY_NAME);
        claimsParam.setValue(claimsMap);
        claimsParam.setValueType(WorkflowDataType.STRING_STRING_MAP_TYPE);
        parameters.add(claimsParam);

        // Add regular parameter
        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithCredentials() {

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestId(REQUEST_ID);
        request.setCreatedBy("testuser");

        List<RequestParameter> parameters = new ArrayList<>();

        RequestParameter credentialParam = new RequestParameter();
        credentialParam.setName(WFConstant.CREDENTIAL);
        credentialParam.setValue("secret");
        credentialParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(credentialParam);

        RequestParameter regularParam = new RequestParameter();
        regularParam.setName("username");
        regularParam.setValue("testuser");
        regularParam.setValueType(WorkflowDataType.STRING_TYPE);
        parameters.add(regularParam);

        request.setRequestParameters(parameters);
        return request;
    }

    private WorkflowRequest createWorkflowRequestWithEmptyProperties() {

        WorkflowRequest request = new WorkflowRequest();
        request.setRequestId(REQUEST_ID);
        request.setCreatedBy("testuser");
        request.setRequestParameters(new ArrayList<>());
        return request;
    }

    private List<LocalClaim> createMockLocalClaims() {

        List<LocalClaim> localClaims = new ArrayList<>();

        LocalClaim emailClaim = mock(LocalClaim.class);
        when(emailClaim.getClaimURI()).thenReturn("http://wso2.org/claims/emailaddress");
        when(emailClaim.getClaimProperty("DisplayName")).thenReturn("Email");
        localClaims.add(emailClaim);

        LocalClaim firstNameClaim = mock(LocalClaim.class);
        when(firstNameClaim.getClaimURI()).thenReturn("http://wso2.org/claims/givenname");
        when(firstNameClaim.getClaimProperty("DisplayName")).thenReturn("First Name");
        localClaims.add(firstNameClaim);

        return localClaims;
    }
}
