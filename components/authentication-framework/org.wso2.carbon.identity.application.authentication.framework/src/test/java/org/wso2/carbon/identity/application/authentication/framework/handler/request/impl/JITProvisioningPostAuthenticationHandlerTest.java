/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.AbstractFrameworkTest;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.loader.UIBasedConfigurationLoader;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.handler.event.account.lock.constants.AccountConstants;
import org.wso2.carbon.identity.handler.event.account.lock.exception.AccountLockServiceException;
import org.wso2.carbon.identity.handler.event.account.lock.service.AccountLockService;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManagerImpl;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.exception.FederatedAssociationManagerException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * This is a test class for {@link JITProvisioningPostAuthenticationHandler}.
 */
@PrepareForTest({FrameworkUtils.class, ConfigurationFacade.class, AccountLockService.class,
        FrameworkServiceDataHolder.class, IdentityTenantUtil.class})
@PowerMockIgnore({"javax.xml.*", "org.mockito.*"})
public class JITProvisioningPostAuthenticationHandlerTest extends AbstractFrameworkTest {

    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private JITProvisioningPostAuthenticationHandler postJITProvisioningHandler;
    private ServiceProvider sp;

    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;

    @Mock
    private AccountLockService accountLockService;

    @BeforeClass
    protected void setupSuite() throws XMLStreamException, IdentityProviderManagementException {

        configurationLoader = new UIBasedConfigurationLoader();
        mockStatic(FrameworkUtils.class);
        mockStatic(ConfigurationFacade.class);
        ConfigurationFacade configurationFacade = mock(ConfigurationFacade.class);

        PowerMockito.when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);
        IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
        ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
        Mockito.doReturn(externalIdPConfig).when(configurationFacade).getIdPConfigByName(Mockito.anyString(), Mockito
                .anyString());
        when(FrameworkUtils.isStepBasedSequenceHandlerExecuted(Mockito.any(AuthenticationContext.class)))
                .thenCallRealMethod();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postJITProvisioningHandler = JITProvisioningPostAuthenticationHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow without an authenticated user")
    public void testHandleWithoutAuthenticatedUser() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, false, false);
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler.handle(request, response,
                context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed without having a authenticated user");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithoutFederatedIdp() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, false);
        // Need to mock ConfigurationFacade class to mock get instance method.
        mockStatic(ConfigurationFacade.class);
        ConfigurationFacade configurationFacade = mock(ConfigurationFacade.class);
        PowerMockito.when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                .handle(request, response, context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed while having a authenticated user without federated "
                        + "authenticator");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithFederatedIdp() throws FrameworkException,
            FederatedAssociationManagerException, AccountLockServiceException, UserStoreException, XMLStreamException,
            IdentityProviderManagementException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
        FederatedAssociationManager federatedAssociationManager = mock(FederatedAssociationManagerImpl.class);
        when(FrameworkUtils.getFederatedAssociationManager()).thenReturn(federatedAssociationManager);
        doReturn("test").when(federatedAssociationManager).getUserForFederatedAssociation
                (Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(Mockito.mock(StepBasedSequenceHandler.class));


        mockStatic(FrameworkServiceDataHolder.class);
        PowerMockito.when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);

        mockStatic(AccountLockService.class);
        when(frameworkServiceDataHolder.getAccountLockService()).thenReturn(accountLockService);
        when(accountLockService.isAccountLocked(anyString(), anyString())).thenReturn(false);

        RealmService mockRealmService = mock(RealmService.class);
        PowerMockito.when(FrameworkServiceDataHolder.getInstance().getRealmService()).thenReturn(mockRealmService);
        UserRealm mockUserRealm = mock(UserRealm.class);
        UserStoreManager mockUserStoreManager = mock(UserStoreManager.class);
        Map<String, String> mockClaimValues = mock(HashMap.class);
        mockStatic(IdentityTenantUtil.class);
        when(IdentityTenantUtil.getTenantId(anyString())).thenReturn(1);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUserStoreManager);
        when(mockUserStoreManager.getUserClaimValues(anyString(),
                        eq(new String[]{AccountConstants.ACCOUNT_DISABLED_CLAIM}),
                        eq(UserCoreConstants.DEFAULT_PROFILE))).thenReturn(mockClaimValues);
        when(mockClaimValues.get(AccountConstants.ACCOUNT_DISABLED_CLAIM)).thenReturn("false");

        // Need to mock getIdPConfigByName with a null parameter.
        ConfigurationFacade configurationFacade = mock(ConfigurationFacade.class);
        PowerMockito.when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);
        IdentityProvider identityProvider = getTestIdentityProvider("default-tp-1.xml");
        ExternalIdPConfig externalIdPConfig = new ExternalIdPConfig(identityProvider);
        Mockito.doReturn(externalIdPConfig).when(configurationFacade).getIdPConfigByName(eq(null), anyString());

        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                .handle(request, response, context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed while having a authenticated user without federated "
                        + "authenticator");
    }

    /**
     * To get the authentication context and to call the handle method of the PostJitProvisioningHandler.
     *
     * @param sp1 Service Provider
     * @return relevant authentication context.
     * @throws FrameworkException Framwork Exception.
     */
    private AuthenticationContext processAndGetAuthenticationContext(ServiceProvider sp1, boolean
            withAuthenticatedUser, boolean isFederated) throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        context.setSequenceConfig(sequenceConfig);
        context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);

        if (isFederated) {
            applicationAuthenticator = mock(FederatedApplicationAuthenticator.class);
        }
        when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        if (withAuthenticatedUser) {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserName("test");
            authenticatedUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            authenticatedUser.setAuthenticatedSubjectIdentifier("test");
            sequenceConfig.setAuthenticatedUser(authenticatedUser);

            AuthenticatorConfig authenticatorConfig = new AuthenticatorConfig();
            authenticatorConfig.setApplicationAuthenticator(applicationAuthenticator);
            for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
                StepConfig stepConfig = entry.getValue();
                stepConfig.setAuthenticatedAutenticator(authenticatorConfig);
                stepConfig.setAuthenticatedUser(authenticatedUser);
            }
            context.setSequenceConfig(sequenceConfig);
        }

        UserCoreUtil.setDomainInThreadLocal("test_domain");
        return context;
    }

}
