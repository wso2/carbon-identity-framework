/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.IObjectFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.ObjectFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framwork.test.utils.CommonTestUtils;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.testng.Assert.assertEquals;

/**
 * This class will test the Post Authentication Handler for Missing Challenge Questions
 **/
@PrepareForTest({IdentityProviderManager.class, MultitenantUtils.class, Utils.class, FrameworkServiceDataHolder
        .class, ChallengeQuestionManager.class, HttpServletResponse.class, ConfigurationFacade.class})
public class PostAuthnMissingChallengeQuestionsHandlerTest {


    private static final String CHALLENGE_QUESTIONS_REQUESTED = "challengeQuestionsRequested";

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpServletResponse httpServletResponse;

    @Mock
    private IdentityProviderManager identityProviderManager;

    @Mock
    private FrameworkServiceDataHolder frameworkServiceDataHolder;

    @Mock
    private ChallengeQuestionManager challengeQuestionManager;

    @Mock
    private ConfigurationFacade configurationFacade;

    @ObjectFactory
    public IObjectFactory getObjectFactory() {
        return new org.powermock.modules.testng.PowerMockObjectFactory();
    }

    @BeforeMethod
    public void setup() {
        // initialize all the @Mock objects
        MockitoAnnotations.initMocks(this);
        // mock all the statics
        mockStatic(HttpServletResponse.class);
        mockStatic(IdentityProviderManager.class);
        mockStatic(MultitenantUtils.class);
        mockStatic(Utils.class);
        mockStatic(FrameworkServiceDataHolder.class);
        mockStatic(ChallengeQuestionManager.class);
        mockStatic(ConfigurationFacade.class);
    }

    @Test(description = "Test get instance method")
    public void testGetInstance() {
        CommonTestUtils.testSingleton(
                PostAuthnMissingChallengeQuestionsHandler.getInstance(),
                PostAuthnMissingChallengeQuestionsHandler.getInstance()
        );
    }


    @DataProvider(name = "forceChallengeQuestionSettings")
    public Object[][] forceChallengeQuestionSettings() {
        return new Object[][]{
                {"false"},
                {"random_text"},
                {null},

        };
    }

    @Test(dataProvider = "forceChallengeQuestionSettings", description = "Test the functionality of the setting to " +
            "force challenge questions")
    public void testSettingTheOptionToForceChallengeQuestions(String setting) throws Exception {
        AuthenticationContext context = spy(new AuthenticationContext());
        when(context.getTenantDomain()).thenReturn("carbon.super");

        IdentityProvider residentIdp = spy(new IdentityProvider());
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[1];
        IdentityProviderProperty idpProp = new IdentityProviderProperty();
        if (setting != null) {
            idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);
            idpProp.setValue(setting);
        } else {
            idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.ACCOUNT_LOCK_ON_CREATION);
            idpProp.setValue("true");
        }
        idpProperties[0] = idpProp;

        residentIdp.setIdpProperties(idpProperties);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP("carbon.super")).thenReturn(residentIdp);

        PostAuthnHandlerFlowStatus flowStatus = PostAuthnMissingChallengeQuestionsHandler.getInstance().handle
                (httpServletRequest, httpServletResponse, context);

        String expectedResult;
        if (setting == null) {
            setting = "null";
        }
        switch (setting) {
            case "false":
                expectedResult = PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.name();
                break;
            case "true":
                expectedResult = PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.name();
                break;
            case "null":
                expectedResult = PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED.name();
                break;
            default:
                expectedResult = PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.name();
                break;
        }
        assertEquals(flowStatus.name(), expectedResult);

    }

    @Test(description = "Test the behaviour of the handler if the user is null")
    public void testForNullUser() throws Exception {
        AuthenticationContext context = spy(new AuthenticationContext());
        when(context.getTenantDomain()).thenReturn("carbon.super");

        IdentityProvider residentIdp = spy(new IdentityProvider());
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[1];
        IdentityProviderProperty idpProp = new IdentityProviderProperty();
        idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);
        idpProp.setValue("true");
        idpProperties[0] = idpProp;

        residentIdp.setIdpProperties(idpProperties);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP("carbon.super")).thenReturn(residentIdp);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(null);

        context.setSequenceConfig(sequenceConfig);

        PostAuthnHandlerFlowStatus flowStatus = PostAuthnMissingChallengeQuestionsHandler.getInstance().handle
                (httpServletRequest, httpServletResponse, context);

        String expectedResult = PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED.name();
        assertEquals(flowStatus.name(), expectedResult);
    }

    @Test(description = "Test the flow for the user who has already given the challenge questions")
    public void testAlreadyChallengeQuestionProvidedUserFlow() throws Exception {
        AuthenticationContext context = spy(new AuthenticationContext());
        when(context.getTenantDomain()).thenReturn("carbon.super");

        IdentityProvider residentIdp = spy(new IdentityProvider());
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[1];
        IdentityProviderProperty idpProp = new IdentityProviderProperty();
        idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);
        idpProp.setValue("true");
        idpProperties[0] = idpProp;

        residentIdp.setIdpProperties(idpProperties);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP("carbon.super")).thenReturn(residentIdp);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        AuthenticatedUser user = spy(new AuthenticatedUser());
        user.setUserName("admin");
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(user);

        context.setSequenceConfig(sequenceConfig);


        when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        when(Utils.getTenantId("carbon.super")).thenReturn(-1234);


        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        RealmService realmService = mock(RealmService.class);

        UserStoreManager userStoreManager = mock(UserStoreManager.class);

        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(frameworkServiceDataHolder.getRealmService()).thenReturn(realmService);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        Map<String, String> claimsMap = new HashMap<>();
        claimsMap.put(IdentityRecoveryConstants.CHALLENGE_QUESTION_URI, "dummy_data");
        when(userStoreManager.getUserClaimValues("admin", new String[]{IdentityRecoveryConstants
                .CHALLENGE_QUESTION_URI}, UserCoreConstants.DEFAULT_PROFILE)).thenReturn(claimsMap);
        PostAuthnHandlerFlowStatus flowStatus = PostAuthnMissingChallengeQuestionsHandler.getInstance().handle
                (httpServletRequest, httpServletResponse, context);


        String expectedResult = PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.name();
        assertEquals(flowStatus.name(), expectedResult);
    }

    @Test(description = "Test the flow of challenge question post authentication handler before requesting challenge " +
            "questions from the user")
    public void testBeforeRequestingChallengeQuestionFlow() throws Exception {
        AuthenticationContext context = spy(new AuthenticationContext());
        when(context.getTenantDomain()).thenReturn("carbon.super");

        IdentityProvider residentIdp = spy(new IdentityProvider());
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[1];
        IdentityProviderProperty idpProp = new IdentityProviderProperty();
        idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);
        idpProp.setValue("true");
        idpProperties[0] = idpProp;

        residentIdp.setIdpProperties(idpProperties);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP("carbon.super")).thenReturn(residentIdp);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        AuthenticatedUser user = spy(new AuthenticatedUser());
        user.setUserName("admin");
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(user);

        context.setSequenceConfig(sequenceConfig);


        when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        when(Utils.getTenantId("carbon.super")).thenReturn(-1234);


        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        RealmService realmService = mock(RealmService.class);

        UserStoreManager userStoreManager = mock(UserStoreManager.class);

        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(frameworkServiceDataHolder.getRealmService()).thenReturn(realmService);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        Map<String, String> claimsMap = new HashMap<>();
        when(userStoreManager.getUserClaimValues("admin", new String[]{IdentityRecoveryConstants
                .CHALLENGE_QUESTION_URI}, UserCoreConstants.DEFAULT_PROFILE)).thenReturn(claimsMap);

        List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
        ChallengeQuestion challengeQuestion = spy(new ChallengeQuestion());
        challengeQuestion.setQuestionSetId("dummy_set");
        challengeQuestion.setQuestionId("dummy_id");
        challengeQuestion.setQuestion("dummy_question");
        challengeQuestions.add(challengeQuestion);
        when(challengeQuestionManager.getAllChallengeQuestions("carbon.super")).thenReturn(challengeQuestions);
        when(ChallengeQuestionManager.getInstance()).thenReturn(challengeQuestionManager);

        doNothing().doThrow(Exception.class).when(httpServletResponse).sendRedirect(any());

        when(configurationFacade.getAuthenticationEndpointURL()).thenReturn("");
        when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);

        PostAuthnHandlerFlowStatus flowStatus = PostAuthnMissingChallengeQuestionsHandler.getInstance().handle
                (httpServletRequest, httpServletResponse, context);

        String expectedResult = PostAuthnHandlerFlowStatus.INCOMPLETE.name();
        assertEquals(flowStatus.name(), expectedResult);
    }

    @Test(description = "Test the flow of challenge question post authentication handler after requesting challenge " +
            "questions from the user")
    public void testAfterRequestingChallengeQuestionFlow() throws Exception {

        AuthenticationContext context = spy(new AuthenticationContext());
        when(context.getTenantDomain()).thenReturn("carbon.super");

        IdentityProvider residentIdp = spy(new IdentityProvider());
        IdentityProviderProperty[] idpProperties = new IdentityProviderProperty[1];
        IdentityProviderProperty idpProp = new IdentityProviderProperty();
        idpProp.setName(IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);
        idpProp.setValue("true");
        idpProperties[0] = idpProp;

        residentIdp.setIdpProperties(idpProperties);
        when(IdentityProviderManager.getInstance()).thenReturn(identityProviderManager);
        when(identityProviderManager.getResidentIdP("carbon.super")).thenReturn(residentIdp);

        SequenceConfig sequenceConfig = spy(new SequenceConfig());
        AuthenticatedUser user = spy(new AuthenticatedUser());
        user.setUserName("admin");
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(user);

        context.setSequenceConfig(sequenceConfig);


        when(MultitenantUtils.getTenantDomain("admin")).thenReturn("carbon.super");
        when(Utils.getTenantId("carbon.super")).thenReturn(-1234);


        when(FrameworkServiceDataHolder.getInstance()).thenReturn(frameworkServiceDataHolder);
        RealmService realmService = mock(RealmService.class);

        UserStoreManager userStoreManager = mock(UserStoreManager.class);

        UserRealm userRealm = mock(UserRealm.class);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        when(realmService.getTenantUserRealm(-1234)).thenReturn(userRealm);
        when(frameworkServiceDataHolder.getRealmService()).thenReturn(realmService);
        when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);

        Map<String, String> claimsMap = new HashMap<>();
        when(userStoreManager.getUserClaimValues("admin", new String[]{IdentityRecoveryConstants
                .CHALLENGE_QUESTION_URI}, UserCoreConstants.DEFAULT_PROFILE)).thenReturn(claimsMap);

        List<ChallengeQuestion> challengeQuestions = new ArrayList<>();
        ChallengeQuestion challengeQuestion = spy(new ChallengeQuestion());
        challengeQuestion.setQuestionSetId("dummy_set");
        challengeQuestion.setQuestionId("dummy_id");
        challengeQuestion.setQuestion("dummy_question");
        challengeQuestions.add(challengeQuestion);
        when(challengeQuestionManager.getAllChallengeQuestions("carbon.super")).thenReturn(challengeQuestions);
        when(ChallengeQuestionManager.getInstance()).thenReturn(challengeQuestionManager);

        doNothing().doThrow(Exception.class).when(httpServletResponse).sendRedirect(any());

        when(configurationFacade.getAuthenticationEndpointURL()).thenReturn("");
        when(ConfigurationFacade.getInstance()).thenReturn(configurationFacade);

        when(context.getParameter(CHALLENGE_QUESTIONS_REQUESTED)).thenReturn(true);
        Vector<String> set = new Vector<>();
        set.add("Q-dummy_question");
        set.add("A-dummy_answer");
        Enumeration<String> paramNames = new Vector(set).elements();
        when(httpServletRequest.getParameterNames()).thenReturn(paramNames);
        when(httpServletRequest.getParameter(anyString())).thenReturn("dummy_question");


        PostAuthnHandlerFlowStatus flowStatus = PostAuthnMissingChallengeQuestionsHandler.getInstance().handle
                (httpServletRequest, httpServletResponse, context);

        String expectedResult = PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED.name();
        assertEquals(flowStatus.name(), expectedResult);
    }

}
