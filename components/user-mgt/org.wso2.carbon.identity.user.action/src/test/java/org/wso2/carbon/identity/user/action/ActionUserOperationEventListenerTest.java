package org.wso2.carbon.identity.user.action;

import org.mockito.MockedStatic;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.execution.api.exception.ActionExecutionException;
import org.wso2.carbon.identity.action.execution.api.model.ActionExecutionStatus;
import org.wso2.carbon.identity.action.execution.api.model.ActionType;
import org.wso2.carbon.identity.action.execution.api.model.Error;
import org.wso2.carbon.identity.action.execution.api.model.ErrorStatus;
import org.wso2.carbon.identity.action.execution.api.model.FailedStatus;
import org.wso2.carbon.identity.action.execution.api.model.Failure;
import org.wso2.carbon.identity.action.execution.api.model.Success;
import org.wso2.carbon.identity.action.execution.api.model.SuccessStatus;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.context.IdentityContext;
import org.wso2.carbon.identity.core.context.model.Flow;
import org.wso2.carbon.identity.core.context.model.Organization;
import org.wso2.carbon.identity.core.model.IdentityEventListenerConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.organization.management.service.OrganizationManager;
import org.wso2.carbon.identity.organization.management.service.exception.OrganizationManagementException;
import org.wso2.carbon.identity.organization.management.service.model.BasicOrganization;
import org.wso2.carbon.identity.user.action.api.service.UserActionExecutor;
import org.wso2.carbon.identity.user.action.internal.component.UserActionServiceComponentHolder;
import org.wso2.carbon.identity.user.action.internal.factory.UserActionExecutorFactory;
import org.wso2.carbon.identity.user.action.internal.listener.ActionUserOperationEventListener;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertFalse;
import static org.wso2.carbon.identity.user.action.api.constant.UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR;
import static org.wso2.carbon.identity.user.action.api.constant.UserActionError.PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED;
import static org.wso2.carbon.identity.user.action.api.constant.UserActionError.PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR;
import static org.wso2.carbon.identity.user.action.api.constant.UserActionError.PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET;

/**
 * Unit tests for ActionUserOperationEventListener.
 */
@WithCarbonHome
public class ActionUserOperationEventListenerTest {

    private static final int DEFAULT_LISTENER_ORDER = 10000;
    public static final String USER_NAME = "USER_NAME";
    public static final String PASSWORD = "PASSWORD";
    public static final String TEST_RESIDENT_ORG_ID = "6a56eba9-23c4-4306-ae13-11259c2a40ae";
    public static final String TEST_RESIDENT_ORG_NAME = "mySubOrg1";
    public static final String TEST_RESIDENT_ORG_HANDLE = "mySubOrg1.com";
    public static final int TEST_RESIDENT_ORG_DEPTH = 20;
    public static final String TEST_MANAGED_BY_ORG_ID = "9a56eb19-23c4-4306-ae13-75299c2a40af";
    public static final String TEST_MANAGED_BY_ORG_NAME = "mySubOrg2";
    public static final String TEST_MANAGED_BY_ORG_HANDLE = "mySubOrg2.com";
    public static final int TEST_MANAGED_BY_ORG_DEPTH = 10;
    private static final String ENABLE_PRE_UPDATE_PASSWORD_REGISTRATION_FLOW =
            "Actions.Types.PreUpdatePassword.EnableInRegistrationFlows";

    private UniqueIDUserStoreManager userStoreManager;
    private UserActionExecutor mockExecutor;
    private OrganizationManager organizationManager;
    private MockedStatic<UserCoreUtil> userCoreUtil;

    private ActionUserOperationEventListener listener;

    @BeforeMethod
    public void setUp() {

        userStoreManager = mock(UniqueIDUserStoreManager.class);
        mockExecutor = mock(UserActionExecutor.class);
        userCoreUtil = mockStatic(UserCoreUtil.class);
        listener = new ActionUserOperationEventListener();
        userCoreUtil.when(() -> UserCoreUtil.getDomainName(any())).thenReturn("PRIMARY");
        IdentityContext.getThreadLocalIdentityContext().setFlow(new Flow.Builder()
                .name(Flow.Name.PASSWORD_RESET)
                .initiatingPersona(Flow.InitiatingPersona.USER)
                .build());

        organizationManager = mock(OrganizationManager.class);
        UserActionServiceComponentHolder.getInstance().setOrganizationManager(organizationManager);
    }

    @AfterMethod
    public void tearDown() {

        userCoreUtil.close();
        UserActionExecutorFactory.unregisterUserActionExecutor(mockExecutor);
        IdentityContext.destroyCurrentContext();
    }

    @Test
    public void testGetExecutionOrderId() {

        IdentityEventListenerConfig mockConfig = mock(IdentityEventListenerConfig.class);
        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.readEventListenerProperty(any(), any()))
                    .thenReturn(mockConfig);
            doReturn(5000).when(mockConfig).getOrder();
            Assert.assertEquals(listener.getExecutionOrderId(), 5000);

            doReturn(IdentityCoreConstants.EVENT_LISTENER_ORDER_ID).when(mockConfig).getOrder();
            Assert.assertEquals(listener.getExecutionOrderId(), DEFAULT_LISTENER_ORDER);
        }
    }

    @Test
    public void testPreUpdatePasswordActionExecutionWithDisabledListener()
            throws UserStoreException, UnsupportedSecretTypeException {

        setOrganizationToIdentityContext();
        IdentityEventListenerConfig mockConfig = mock(IdentityEventListenerConfig.class);
        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.readEventListenerProperty(any(), any()))
                    .thenReturn(mockConfig);
            doReturn("false").when(mockConfig).getEnable();
            // Update flow
            Assert.assertTrue(listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD),
                    userStoreManager));
            // Register flow
            Assert.assertTrue(listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD), null,
                    new HashMap<>(), null,
                    userStoreManager), "The method should return true when the listener is disabled.");
        }
    }

    private void setOrganizationToIdentityContext() {

        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(TEST_RESIDENT_ORG_ID)
                .name(TEST_RESIDENT_ORG_NAME)
                .organizationHandle(TEST_RESIDENT_ORG_HANDLE)
                .depth(TEST_RESIDENT_ORG_DEPTH)
                .build());
    }

    @Test
    public void testPreUpdatePasswordActionExecutionSuccess()
            throws UserStoreException, ActionExecutionException, UnsupportedSecretTypeException {

        setOrganizationToIdentityContext();
        ActionExecutionStatus<Success> successStatus =
                new SuccessStatus.Builder().setResponseContext(Collections.emptyMap()).build();
        doReturn(successStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        boolean result = listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD),
                userStoreManager);
        Assert.assertTrue(result, "The method should return true for successful execution.");

        // Register flow
        boolean newResult = listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                null, new HashMap<>(), null, userStoreManager);
        Assert.assertTrue(newResult, "The method should return true for successful execution.");
    }

    @Test
    public void testPreUpdatePasswordActionExecutionSuccessInSubOrgFlow()
            throws UserStoreException, ActionExecutionException, UnsupportedSecretTypeException {

        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(TEST_RESIDENT_ORG_ID)
                .name(TEST_RESIDENT_ORG_NAME)
                .organizationHandle(TEST_RESIDENT_ORG_HANDLE)
                .depth(TEST_RESIDENT_ORG_DEPTH)
                .build());
        doReturn(null).when(userStoreManager).getUserClaimValueWithID(any(), any(), any());

        ActionExecutionStatus<Success> successStatus =
                new SuccessStatus.Builder().setResponseContext(Collections.emptyMap()).build();
        doReturn(successStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        boolean result = listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD),
                userStoreManager);
        Assert.assertTrue(result, "The method should return true for successful execution.");

        // Register flow
        boolean newResult = listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                null, new HashMap<>(), null, userStoreManager);
        Assert.assertTrue(newResult, "The method should return true for successful execution.");
    }

    @Test
    public void testPreUpdatePasswordActionExecutionSuccessInSubOrgFlowForSharedUser()
            throws UserStoreException, ActionExecutionException, UnsupportedSecretTypeException,
            OrganizationManagementException {

        IdentityContext.getThreadLocalIdentityContext().setOrganization(new Organization.Builder()
                .id(TEST_RESIDENT_ORG_ID)
                .name(TEST_RESIDENT_ORG_NAME)
                .organizationHandle(TEST_RESIDENT_ORG_HANDLE)
                .depth(TEST_RESIDENT_ORG_DEPTH)
                .build());
        doReturn(TEST_MANAGED_BY_ORG_ID).when(userStoreManager).getUserClaimValueWithID(any(), any(), any());

        BasicOrganization managedByOrg = new BasicOrganization();
        managedByOrg.setId(TEST_MANAGED_BY_ORG_ID);
        managedByOrg.setName(TEST_MANAGED_BY_ORG_NAME);
        managedByOrg.setOrganizationHandle(TEST_MANAGED_BY_ORG_HANDLE);
        doReturn(Collections.singletonMap(TEST_MANAGED_BY_ORG_ID, managedByOrg)).when(organizationManager)
                .getBasicOrganizationDetailsByOrgIDs(any());
        doReturn(TEST_MANAGED_BY_ORG_DEPTH).when(organizationManager).getOrganizationDepthInHierarchy(any());

        ActionExecutionStatus<Success> successStatus =
                new SuccessStatus.Builder().setResponseContext(Collections.emptyMap()).build();
        doReturn(successStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        boolean result = listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD),
                userStoreManager);
        Assert.assertTrue(result, "The method should return true for successful execution.");
        verify(organizationManager, times(1)).getMinimalOrganization(any(), any());

        // Register flow
        boolean newResult = listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                null, new HashMap<>(), null, userStoreManager);
        Assert.assertTrue(newResult, "The method should return true for successful execution.");
    }

    @Test
    public void testPreUpdatePasswordActionExecutionFailure() throws ActionExecutionException {

        setOrganizationToIdentityContext();
        Failure failureResponse = new Failure("FailureReason", "FailureDescription");
        ActionExecutionStatus<Failure> failedStatus = new FailedStatus(failureResponse);
        doReturn(failedStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        try {
            listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD), userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreClientException);
            Assert.assertEquals(e.getMessage(), "FailureReason. FailureDescription");
            Assert.assertEquals(((UserStoreClientException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED);
        }

        // Register flow
        try {
            listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                    null, new HashMap<>(), null, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreClientException);
            Assert.assertEquals(e.getMessage(), "FailureReason. FailureDescription");
            Assert.assertEquals(((UserStoreClientException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED);
        }
    }

    @Test
    public void testPreUpdatePasswordActionExecutionFailureWithoutDescription() throws ActionExecutionException {

        setOrganizationToIdentityContext();
        Failure failureResponse = new Failure("FailureReason", null);
        ActionExecutionStatus<Failure> failedStatus = new FailedStatus(failureResponse);
        doReturn(failedStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        try {
            listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD), userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreClientException);
            Assert.assertEquals(e.getMessage(), "FailureReason");
            Assert.assertEquals(((UserStoreClientException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED);
        }

        // Register flow
        try {
            listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                    null, new HashMap<>(), null, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreClientException);
            Assert.assertEquals(e.getMessage(), "FailureReason");
            Assert.assertEquals(((UserStoreClientException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_EXECUTION_FAILED);
        }
    }

    @Test
    public void testPreUpdatePasswordActionExecutionError() throws ActionExecutionException {

        setOrganizationToIdentityContext();
        Error errorResponse = new Error("ErrorMessage", "ErrorDescription");
        ActionExecutionStatus<Error> errorStatus = new ErrorStatus(errorResponse);
        doReturn(errorStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        try {
            listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD), userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "ErrorMessage. ErrorDescription");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(), PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR);
        }

        // Register flow
        try {
            listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                    null, new HashMap<>(), null, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "ErrorMessage. ErrorDescription");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_EXECUTION_ERROR);
        }
    }

    @Test
    public void testPreUpdatePasswordActionExecutionWithUnsupportedSecret() throws ActionExecutionException {

        setOrganizationToIdentityContext();
        Error errorResponse = new Error("ErrorMessage", "ErrorDescription");
        ActionExecutionStatus<Error> errorStatus = new ErrorStatus(errorResponse);
        doReturn(errorStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        try {
            listener.doPreUpdateCredentialByAdminWithID(USER_NAME, 10, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "Credential is not in the expected format.");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(), PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET);
        }

        // Register flow
        try {
            listener.doPreAddUserWithID(USER_NAME, 10, null,
                    new HashMap<>(), null, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "Credential is not in the expected format.");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(),
                    PRE_UPDATE_PASSWORD_ACTION_UNSUPPORTED_SECRET);
        }
    }

    @Test
    public void testPreUpdatePasswordActionExecutionWithUnknownStatus()
            throws UserStoreException, ActionExecutionException, UnsupportedSecretTypeException {

        setOrganizationToIdentityContext();
        ActionExecutionStatus<?> unknownStatus = mock(ActionExecutionStatus.class);
        doReturn(null).when(unknownStatus).getStatus();
        doReturn(unknownStatus).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        assertFalse(listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD),
                userStoreManager));
        // Register flow
        assertFalse(listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                null, new HashMap<>(), null, userStoreManager),
                "The method should return false for unknown status.");
    }

    @Test
    public void testPreUpdatePasswordActionExecutionWithActionExecutionException() throws ActionExecutionException {

        setOrganizationToIdentityContext();
        doThrow(new ActionExecutionException("Execution error")).when(mockExecutor).execute(any(), any());
        doReturn(ActionType.PRE_UPDATE_PASSWORD).when(mockExecutor).getSupportedActionType();
        UserActionExecutorFactory.registerUserActionExecutor(mockExecutor);

        // Update flow
        try {
            listener.doPreUpdateCredentialByAdminWithID(USER_NAME, Secret.getSecret(PASSWORD), userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "Error while executing pre update password action.");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(), PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR);
        }

        // Register flow
        try {
            listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD),
                    null, new HashMap<>(), null, userStoreManager);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof UserStoreException);
            Assert.assertEquals(e.getMessage(), "Error while executing pre update password action.");
            Assert.assertEquals(((UserStoreException) e).getErrorCode(), PRE_UPDATE_PASSWORD_ACTION_SERVER_ERROR);
        }
    }

    @Test
    public void doPreAddUserWithIDReturnsTrueWhenPreUpdatePasswordFlowIsDisabled() throws
            UserStoreException, UnsupportedSecretTypeException {
        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(ENABLE_PRE_UPDATE_PASSWORD_REGISTRATION_FLOW))
                    .thenReturn("false");
            Assert.assertTrue(listener.doPreAddUserWithID(USER_NAME, Secret.getSecret(PASSWORD), null,
                    null, null, userStoreManager));
        }
    }

    @Test
    public void isEnabledInRegistrationFlowsReturnsTrueForValidTrueValue() {
        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(ENABLE_PRE_UPDATE_PASSWORD_REGISTRATION_FLOW))
                    .thenReturn("true");
        }
    }

    @Test
    public void isEnabledInRegistrationFlowsReturnsZeroForValidFalseValue() {
        try (MockedStatic<IdentityUtil> identityUtilMockedStatic = mockStatic(IdentityUtil.class)) {
            identityUtilMockedStatic.when(() -> IdentityUtil.getProperty(ENABLE_PRE_UPDATE_PASSWORD_REGISTRATION_FLOW))
                    .thenReturn("false");
        }
    }
}
