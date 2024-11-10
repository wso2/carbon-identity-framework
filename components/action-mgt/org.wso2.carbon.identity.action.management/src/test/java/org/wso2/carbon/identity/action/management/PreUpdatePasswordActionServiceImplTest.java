/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.action.management;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.action.management.constant.ActionMgtConstants;
import org.wso2.carbon.identity.action.management.exception.ActionMgtClientException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtException;
import org.wso2.carbon.identity.action.management.exception.ActionMgtServerException;
import org.wso2.carbon.identity.action.management.internal.ActionMgtServiceComponentHolder;
import org.wso2.carbon.identity.action.management.model.Action;
import org.wso2.carbon.identity.action.management.model.PreUpdatePasswordAction;
import org.wso2.carbon.identity.action.management.util.TestUtil;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtClientException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtException;
import org.wso2.carbon.identity.certificate.management.exception.CertificateMgtServerException;
import org.wso2.carbon.identity.certificate.management.model.Certificate;
import org.wso2.carbon.identity.certificate.management.service.CertificateManagementService;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.common.testng.WithH2Database;
import org.wso2.carbon.identity.common.testng.WithRealmService;
import org.wso2.carbon.identity.core.internal.IdentityCoreServiceDataHolder;
import org.wso2.carbon.identity.secret.mgt.core.SecretManagerImpl;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_ID;
import static org.wso2.carbon.identity.action.management.util.TestUtil.CERTIFICATE_NAME;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_PATH;
import static org.wso2.carbon.identity.action.management.util.TestUtil.PRE_UPDATE_PASSWORD_TYPE;
import static org.wso2.carbon.identity.action.management.util.TestUtil.TENANT_DOMAIN;
import static org.wso2.carbon.identity.action.management.util.TestUtil.UPDATED_CERTIFICATE;
import static org.wso2.carbon.identity.certificate.management.constant.CertificateMgtErrors.ERROR_INVALID_CERTIFICATE_CONTENT;

/**
 * This class is a test suite for the ActionManagementDAOImpl class.
 * It contains unit tests to verify the functionality of the methods in the ActionManagementDAOImpl class
 * for PRE_UPDATE_PASSWORD action type.
 */
@WithCarbonHome
@WithH2Database(files = {"dbscripts/h2.sql"})
@WithRealmService(injectToSingletons = {IdentityCoreServiceDataHolder.class})
public class PreUpdatePasswordActionServiceImplTest {

    private ActionManagementService actionManagementService;
    private CertificateManagementService certificateManagementService;

    private PreUpdatePasswordAction preUpdatePasswordAction;
    private Certificate certificate;
    private CertificateMgtServerException serverException;
    private CertificateMgtClientException clientException;

    @BeforeClass
    public void setUpClass() {

        actionManagementService = ActionManagementServiceImpl.getInstance();
    }

    @BeforeMethod
    public void setUp() throws SecretManagementException {

        SecretManagerImpl secretManager = mock(SecretManagerImpl.class);
        SecretType secretType = mock(SecretType.class);
        ActionMgtServiceComponentHolder.getInstance().setSecretManager(secretManager);
        when(secretType.getId()).thenReturn("secretId");
        when(secretManager.getSecretType(any())).thenReturn(secretType);

        certificateManagementService = mock(CertificateManagementService.class);
        ActionMgtServiceComponentHolder.getInstance()
                .setCertificateManagementService(certificateManagementService);

        serverException = new CertificateMgtServerException("server_error_message", "server_error_description", "65030",
                new Throwable());
        clientException = new CertificateMgtClientException(ERROR_INVALID_CERTIFICATE_CONTENT.getMessage(),
                ERROR_INVALID_CERTIFICATE_CONTENT.getDescription(), ERROR_INVALID_CERTIFICATE_CONTENT.getCode());
    }

    @Test(priority = 1)
    public void testAddPreUpdatePasswordAction() throws ActionMgtException, CertificateMgtException {

        PreUpdatePasswordAction actionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "PreUpdatePassword",
                "To configure PreUpdatePassword",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"),
                PreUpdatePasswordAction.PasswordFormat.PLAIN_TEXT,
                CERTIFICATE);
        certificate = new Certificate.Builder()
                .id(String.valueOf(CERTIFICATE_ID))
                .name(CERTIFICATE_NAME)
                .certificateContent(CERTIFICATE)
                .build();

        doReturn(CERTIFICATE_ID).when(certificateManagementService).addCertificate(any(), any());
        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        preUpdatePasswordAction = (PreUpdatePasswordAction) actionManagementService.addAction(PRE_UPDATE_PASSWORD_PATH,
                actionModel, TENANT_DOMAIN);

        Assert.assertNotNull(preUpdatePasswordAction.getId());
        Assert.assertEquals(actionModel.getName(), preUpdatePasswordAction.getName());
        Assert.assertEquals(actionModel.getDescription(), preUpdatePasswordAction.getDescription());
        Assert.assertEquals(PRE_UPDATE_PASSWORD_TYPE, preUpdatePasswordAction.getType().getActionType());
        Assert.assertEquals(Action.Status.ACTIVE, preUpdatePasswordAction.getStatus());
        Assert.assertEquals(actionModel.getEndpoint().getUri(), preUpdatePasswordAction.getEndpoint().getUri());
        Assert.assertEquals(actionModel.getEndpoint().getAuthentication().getType(),
                preUpdatePasswordAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(actionModel.getPasswordSharingFormat(), preUpdatePasswordAction.getPasswordSharingFormat());
        Assert.assertNotNull(preUpdatePasswordAction.getCertificate());
        Assert.assertEquals(CERTIFICATE_ID, preUpdatePasswordAction.getCertificate().getId());
        Assert.assertEquals(CERTIFICATE_NAME, preUpdatePasswordAction.getCertificate().getName());
    }

    @Test(priority = 2, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testGetPreUpdatePasswordActionByActionId() throws ActionMgtException, CertificateMgtException {

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        PreUpdatePasswordAction fetchedAction = (PreUpdatePasswordAction) actionManagementService
                .getActionByActionId(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(), TENANT_DOMAIN);

        Assert.assertEquals(preUpdatePasswordAction.getId(), fetchedAction.getId());
        Assert.assertEquals(preUpdatePasswordAction.getName(), fetchedAction.getName());
        Assert.assertEquals(preUpdatePasswordAction.getDescription(), fetchedAction.getDescription());
        Assert.assertEquals(preUpdatePasswordAction.getType(), fetchedAction.getType());
        Assert.assertEquals(preUpdatePasswordAction.getStatus(), fetchedAction.getStatus());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), fetchedAction.getEndpoint().getUri());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(),
                fetchedAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(
                preUpdatePasswordAction.getPasswordSharingFormat(), fetchedAction.getPasswordSharingFormat());
        Assert.assertNotNull(fetchedAction.getCertificate());
        Assert.assertEquals(preUpdatePasswordAction.getCertificate().getId(), fetchedAction.getCertificate().getId());
        Assert.assertEquals(
                preUpdatePasswordAction.getCertificate().getName(), fetchedAction.getCertificate().getName());
    }

    @Test(priority = 3, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testGetPreUpdatePasswordActionsByActionType() throws ActionMgtException, CertificateMgtException {

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        List<Action> preUpdatePasswordActionList =
                actionManagementService.getActionsByActionType(PRE_UPDATE_PASSWORD_PATH, TENANT_DOMAIN);

        Assert.assertEquals(1, preUpdatePasswordActionList.size());
        PreUpdatePasswordAction fetchedAction = (PreUpdatePasswordAction) preUpdatePasswordActionList.get(0);
        Assert.assertEquals(preUpdatePasswordAction.getId(), fetchedAction.getId());
        Assert.assertEquals(preUpdatePasswordAction.getName(), fetchedAction.getName());
        Assert.assertEquals(preUpdatePasswordAction.getDescription(), fetchedAction.getDescription());
        Assert.assertEquals(preUpdatePasswordAction.getType(), fetchedAction.getType());
        Assert.assertEquals(preUpdatePasswordAction.getStatus(), fetchedAction.getStatus());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), fetchedAction.getEndpoint().getUri());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(),
                fetchedAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(
                preUpdatePasswordAction.getPasswordSharingFormat(), fetchedAction.getPasswordSharingFormat());
        Assert.assertNotNull(fetchedAction.getCertificate());
        Assert.assertEquals(preUpdatePasswordAction.getCertificate().getId(), fetchedAction.getCertificate().getId());
        Assert.assertEquals(
                preUpdatePasswordAction.getCertificate().getName(), fetchedAction.getCertificate().getName());

    }

    @Test(priority = 4, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testUpdatePreUpdatePasswordAction() throws ActionMgtException, CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "Updated PreUpdatePassword Action",
                "To configure PreUpdatePassword of wso2.com organization",
                "https://my-extension.com/pre-update-password",
                TestUtil.buildMockNoneAuthentication(),
                PreUpdatePasswordAction.PasswordFormat.SHA256_HASHED,
                UPDATED_CERTIFICATE);

        certificate = new Certificate.Builder()
                .id(String.valueOf(CERTIFICATE_ID))
                .name(CERTIFICATE_NAME)
                .certificateContent(UPDATED_CERTIFICATE)
                .build();

        doNothing().when(certificateManagementService).updateCertificateContent(anyString(), anyString(), anyString());
        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());

        PreUpdatePasswordAction updatedAction = (PreUpdatePasswordAction) actionManagementService.updateAction(
                PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(), updateActionModel, TENANT_DOMAIN);

        Assert.assertEquals(preUpdatePasswordAction.getId(), updatedAction.getId());
        Assert.assertEquals(updateActionModel.getName(), updatedAction.getName());
        Assert.assertEquals(updateActionModel.getDescription(), updatedAction.getDescription());
        Assert.assertEquals(preUpdatePasswordAction.getType(), updatedAction.getType());
        Assert.assertEquals(preUpdatePasswordAction.getStatus(), updatedAction.getStatus());
        Assert.assertEquals(updateActionModel.getEndpoint().getUri(), updatedAction.getEndpoint().getUri());
        Assert.assertEquals(updateActionModel.getEndpoint().getAuthentication().getType(),
                updatedAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(updateActionModel.getPasswordSharingFormat(), updatedAction.getPasswordSharingFormat());
        Assert.assertNotNull(updatedAction.getCertificate());
        Assert.assertEquals(certificate.getId(), updatedAction.getCertificate().getId());
        Assert.assertEquals(certificate.getName(), updatedAction.getCertificate().getName());
        Assert.assertEquals(certificate.getCertificateContent(),
                updatedAction.getCertificate().getCertificateContent());

        preUpdatePasswordAction = updatedAction;
    }

    @Test(priority = 5, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testGetPreUpdatePasswordActionWithServerErrorFromCertificate() throws CertificateMgtException {

        doThrow(serverException).when(certificateManagementService).getCertificate(anyString(), anyString());
        try {
            actionManagementService.getActionByActionId(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(),
                    TENANT_DOMAIN);
            Assert.fail("Successful retrieval of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(e.getClass(), ActionMgtServerException.class);
            Assert.assertEquals(e.getMessage(),
                    ActionMgtConstants.ErrorMessages.ERROR_WHILE_RETRIEVING_ACTION_BY_ID.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtServerException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }

    @Test(priority = 6, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testUpdatePreUpdatePasswordActionWithServerErrorFromCertificate() throws CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "Updated PreUpdatePassword Action",
                "To configure PreUpdatePassword of wso2.com organization",
                "https://my-extension.com/pre-update-password",
                TestUtil.buildMockNoneAuthentication(),
                PreUpdatePasswordAction.PasswordFormat.SHA256_HASHED,
                CERTIFICATE);

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        doThrow(serverException).when(certificateManagementService).updateCertificateContent(any(), any(), any());
        try {
            actionManagementService.updateAction(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(),
                    updateActionModel, TENANT_DOMAIN);
            Assert.fail("Successful update of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtServerException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtServerException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }

    @Test(priority = 7, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testUpdatePreUpdatePasswordActionWithClientErrorFromCertificate() throws CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "Updated PreUpdatePassword Action",
                "To configure PreUpdatePassword of wso2.com organization",
                "https://my-extension.com/pre-update-password",
                TestUtil.buildMockNoneAuthentication(),
                PreUpdatePasswordAction.PasswordFormat.SHA256_HASHED,
                CERTIFICATE);

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        doThrow(clientException).when(certificateManagementService).updateCertificateContent(any(), any(), any());
        try {
            actionManagementService.updateAction(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(),
                    updateActionModel, TENANT_DOMAIN);
            Assert.fail("Successful update of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtClientException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_CERTIFICATE.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtClientException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtClientException was not found in the exception chain");
        }
    }

    @Test(priority = 8, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testDeleteCertificateOfPreUpdatePasswordActionWithServerError() throws CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(null, null,
                null, null, null, StringUtils.EMPTY);

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        doThrow(serverException).when(certificateManagementService).deleteCertificate(any(), any());
        try {
            actionManagementService.updateAction(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(),
                    updateActionModel, TENANT_DOMAIN);
            Assert.fail("Successful update of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtServerException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_WHILE_UPDATING_ACTION.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtServerException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }

    @Test(priority = 9, dependsOnMethods = "testUpdatePreUpdatePasswordAction")
    public void testDeleteCertificateOfPreUpdatePasswordAction() throws ActionMgtException, CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(null, null,
                null, null, null, StringUtils.EMPTY);

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        doNothing().when(certificateManagementService).deleteCertificate(anyString(), anyString());

        PreUpdatePasswordAction updatedAction = (PreUpdatePasswordAction) actionManagementService.updateAction(
                PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(), updateActionModel, TENANT_DOMAIN);

        Assert.assertEquals(preUpdatePasswordAction.getId(), updatedAction.getId());
        Assert.assertEquals(preUpdatePasswordAction.getName(), updatedAction.getName());
        Assert.assertEquals(preUpdatePasswordAction.getDescription(), updatedAction.getDescription());
        Assert.assertEquals(preUpdatePasswordAction.getType(), updatedAction.getType());
        Assert.assertEquals(preUpdatePasswordAction.getStatus(), updatedAction.getStatus());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), updatedAction.getEndpoint().getUri());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(),
                updatedAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(preUpdatePasswordAction.getPasswordSharingFormat(),
                updatedAction.getPasswordSharingFormat());
        Assert.assertNull(updatedAction.getCertificate());

        preUpdatePasswordAction = updatedAction;
    }

    @Test(priority = 10, dependsOnMethods = "testDeleteCertificateOfPreUpdatePasswordAction")
    public void testAddCertificateOfPreUpdatePasswordAction() throws ActionMgtException, CertificateMgtException {

        PreUpdatePasswordAction updateActionModel = TestUtil.buildMockPreUpdatePasswordAction(null, null,
                null, null, null, CERTIFICATE);

        doReturn(CERTIFICATE_ID).when(certificateManagementService).addCertificate(any(), anyString());
        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());

        PreUpdatePasswordAction updatedAction = (PreUpdatePasswordAction) actionManagementService.updateAction(
                PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(), updateActionModel, TENANT_DOMAIN);

        Assert.assertEquals(preUpdatePasswordAction.getId(), updatedAction.getId());
        Assert.assertEquals(preUpdatePasswordAction.getName(), updatedAction.getName());
        Assert.assertEquals(preUpdatePasswordAction.getDescription(), updatedAction.getDescription());
        Assert.assertEquals(preUpdatePasswordAction.getType(), updatedAction.getType());
        Assert.assertEquals(preUpdatePasswordAction.getStatus(), updatedAction.getStatus());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getUri(), updatedAction.getEndpoint().getUri());
        Assert.assertEquals(preUpdatePasswordAction.getEndpoint().getAuthentication().getType(),
                updatedAction.getEndpoint().getAuthentication().getType());
        Assert.assertEquals(preUpdatePasswordAction.getPasswordSharingFormat(),
                updatedAction.getPasswordSharingFormat());

        Assert.assertNotNull(updatedAction.getCertificate());
        Assert.assertEquals(certificate.getId(), updatedAction.getCertificate().getId());
        Assert.assertEquals(certificate.getName(), updatedAction.getCertificate().getName());
        Assert.assertEquals(certificate.getCertificateContent(),
                updatedAction.getCertificate().getCertificateContent());

        preUpdatePasswordAction = updatedAction;
    }

    @Test(priority = 8, dependsOnMethods = "testAddPreUpdatePasswordAction")
    public void testDeletePreUpdatePasswordActionWithServerErrorFromCertificate() throws CertificateMgtException {

        doReturn(certificate).when(certificateManagementService).getCertificate(anyString(), anyString());
        doThrow(serverException).when(certificateManagementService).deleteCertificate(any(), any());
        try {
            actionManagementService.deleteAction(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(),
                    TENANT_DOMAIN);
            Assert.fail("Successful deletion of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtServerException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_WHILE_DELETING_ACTION.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtServerException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }

    @Test(priority = 11)
    public void testDeletePreUpdatePasswordAction() throws ActionMgtException, CertificateMgtException {

        doNothing().when(certificateManagementService).deleteCertificate(anyString(), anyString());

        actionManagementService.deleteAction(PRE_UPDATE_PASSWORD_PATH, preUpdatePasswordAction.getId(), TENANT_DOMAIN);

        Assert.assertNull(actionManagementService.getActionByActionId(PRE_UPDATE_PASSWORD_PATH,
                preUpdatePasswordAction.getId(), TENANT_DOMAIN));
    }

    @Test(priority = 12)
    public void testAddPreUpdatePasswordActionWithServerErrorFromCertificate() throws CertificateMgtException {

        PreUpdatePasswordAction actionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "PreUpdatePassword",
                "To configure PreUpdatePassword",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"),
                PreUpdatePasswordAction.PasswordFormat.PLAIN_TEXT,
                CERTIFICATE);

        doThrow(serverException).when(certificateManagementService).addCertificate(any(), any());
        try {
            actionManagementService.addAction(PRE_UPDATE_PASSWORD_PATH, actionModel, TENANT_DOMAIN);
            Assert.fail("Successful addition of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtServerException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_WHILE_ADDING_ACTION.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtServerException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }

    @Test(priority = 13)
    public void testAddPreUpdatePasswordActionWithClientErrorFromCertificate() throws CertificateMgtException {

        PreUpdatePasswordAction actionModel = TestUtil.buildMockPreUpdatePasswordAction(
                "PreUpdatePassword",
                "To configure PreUpdatePassword",
                "https://example.com",
                TestUtil.buildMockBasicAuthentication("admin", "admin"),
                PreUpdatePasswordAction.PasswordFormat.PLAIN_TEXT,
                CERTIFICATE);

        doThrow(clientException).when(certificateManagementService).addCertificate(any(), any());
        try {
            actionManagementService.addAction(PRE_UPDATE_PASSWORD_PATH, actionModel, TENANT_DOMAIN);
            Assert.fail("Successful addition of the action without an exception is considered as a failure");
        } catch (ActionMgtException e) {
            Assert.assertEquals(ActionMgtClientException.class, e.getClass());
            Assert.assertEquals(ActionMgtConstants.ErrorMessages.ERROR_INVALID_ACTION_CERTIFICATE.getMessage(),
                    e.getMessage());
            for (Throwable cause = e.getCause(); cause != null; cause = cause.getCause()) {
                if (cause instanceof CertificateMgtClientException) {
                    return;
                }
            }
            Assert.fail("Expected cause of type CertificateMgtServerException was not found in the exception chain");
        }
    }
}
