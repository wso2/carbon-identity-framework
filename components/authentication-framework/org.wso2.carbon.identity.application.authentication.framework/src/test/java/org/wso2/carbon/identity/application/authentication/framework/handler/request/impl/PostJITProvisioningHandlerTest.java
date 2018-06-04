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
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileAdmin;
import org.wso2.carbon.identity.user.profile.mgt.UserProfileException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

/**
 * This is a test class for {@link PostJITProvisioningHandler}.
 */
@PrepareForTest({FrameworkUtils.class, ConfigurationFacade.class, UserProfileAdmin.class})
@PowerMockIgnore({"javax.xml.*"})
public class PostJITProvisioningHandlerTest extends AbstractFrameworkTest {

    private UIBasedConfigurationLoader configurationLoader;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PostJITProvisioningHandler postJITProvisioningHandler;
    private ServiceProvider sp;

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

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        postJITProvisioningHandler = PostJITProvisioningHandler.getInstance();
        sp = getTestServiceProvider("default-sp-1.xml");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow without an authenticated user")
    public void testHandleWithoutAuthenticatedUser() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, false, false);
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler.handle(request, response, context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED,
                "Post JIT provisioning handler executed without having a authenticated user");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithoutFederatedIdp() throws FrameworkException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, false);
        PostAuthnHandlerFlowStatus postAuthnHandlerFlowStatus = postJITProvisioningHandler
                .handle(request, response, context);
        Assert.assertEquals(postAuthnHandlerFlowStatus, PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED,
                "Post JIT provisioning handler executed while having a authenticated user without federated "
                        + "authenticator");
    }

    @Test(description = "This test case tests the Post JIT provisioning handling flow with an authenticated user")
    public void testHandleWithAuthenticatedUserWithFederatedIdp()
            throws FrameworkException, UserProfileException {

        AuthenticationContext context = processAndGetAuthenticationContext(sp, true, true);
        mockStatic(UserProfileAdmin.class);
        UserProfileAdmin userProfileAdmin = Mockito.mock(UserProfileAdmin.class);
        when(UserProfileAdmin.getInstance()).thenReturn(userProfileAdmin);
        doReturn("test").when(userProfileAdmin).getNameAssociatedWith(Mockito.anyString(), Mockito.anyString());
        when(FrameworkUtils.getStepBasedSequenceHandler()).thenReturn(Mockito.mock(StepBasedSequenceHandler.class));
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
