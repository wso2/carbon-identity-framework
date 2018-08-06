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
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * This is a test class for {@link PostAuthAssociationHandler}.
 */
@PrepareForTest({FrameworkUtils.class, ConfigurationFacade.class, UserProfileAdmin.class})
@PowerMockIgnore({"javax.xml.*"})
public class PostAuthAssociationHandlerTest extends AbstractFrameworkTest {
    public static final String LOCAL_USER = "local-user";
    public static final String SECONDARY = "SECONDARY";
    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PostAuthAssociationHandler postAuthAssociationHandler;
    private ServiceProvider sp;

    @BeforeClass
    protected void setupSuite() throws XMLStreamException, IdentityProviderManagementException, FrameworkException {

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
        when(FrameworkUtils.prependUserStoreDomainToName(Mockito.anyString())).thenCallRealMethod();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postAuthAssociationHandler = PostAuthAssociationHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");

        ClaimHandler claimHandler = PowerMockito.mock(ClaimHandler.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("claim1", "value1");
        PowerMockito.doReturn(claims).when(claimHandler).handleClaimMappings(any(StepConfig.class), any(AuthenticationContext.class),
                any(Map.class), anyBoolean());
        PowerMockito.when(FrameworkUtils.getClaimHandler()).thenReturn(claimHandler);
    }

    @Test(description = "This test case tests the Post Authentication Association handling flow with an authenticated" +
            " user via federated IDP")
    public void testHandleWithAuthenticatedUserWithFederatedIdpAssociatedToSecondaryUserStore()
            throws FrameworkException, UserProfileException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
        mockStatic(UserProfileAdmin.class);
        UserProfileAdmin userProfileAdmin = Mockito.mock(UserProfileAdmin.class);
        when(UserProfileAdmin.getInstance()).thenReturn(userProfileAdmin);
        doReturn(SECONDARY + "/" + LOCAL_USER).when(userProfileAdmin).getNameAssociatedWith(Mockito.anyString(),
                Mockito.anyString());
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(Mockito.mock(StepBasedSequenceHandler.class));
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postAuthAssociationHandler.handle(request, response, context);
        AuthenticatedUser authUser = context.getSequenceConfig().getAuthenticatedUser();
        Assert.assertEquals(authUser.getUserName(), LOCAL_USER, "Post Association handler failed to set associated " +
                "username");
        Assert.assertEquals(authUser.getUserStoreDomain(), SECONDARY, "Post Association handler failed to set " +
                "associated user's domain");
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post Association handler failed to execute with an associated user in a secondary user store.");
    }

    /**
     * To get the authentication context and to call the handle method of the PostAuthAssociationHandler.
     *
     * @param sp1 Service Provider
     * @return relevant authentication context.
     * @throws FrameworkException Framework Exception.
     */
    private AuthenticationContext processAndGetAuthenticationContext(ServiceProvider sp1, boolean
            withAuthenticatedUser, boolean isFederated) throws FrameworkException {

        AuthenticationContext context = getAuthenticationContext(sp1);
        SequenceConfig sequenceConfig = configurationLoader
                .getSequenceConfig(context, Collections.emptyMap(), sp1);
        sequenceConfig.getApplicationConfig().setAlwaysSendMappedLocalSubjectId(true);
        context.setSequenceConfig(sequenceConfig);
        context.setProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED, true);

        ApplicationAuthenticator applicationAuthenticator = mock(ApplicationAuthenticator.class);

        if (isFederated) {
            applicationAuthenticator = mock(FederatedApplicationAuthenticator.class);
        }
        when(applicationAuthenticator.getName()).thenReturn("Authenticator1");

        if (withAuthenticatedUser) {
            AuthenticatedUser authenticatedUser = new AuthenticatedUser();
            authenticatedUser.setUserName("federated");
            authenticatedUser.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            authenticatedUser.setAuthenticatedSubjectIdentifier("federated");
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
        return context;
    }
}
