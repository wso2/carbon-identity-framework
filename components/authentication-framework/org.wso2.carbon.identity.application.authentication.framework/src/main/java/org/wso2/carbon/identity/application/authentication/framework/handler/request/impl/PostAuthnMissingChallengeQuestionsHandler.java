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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AbstractPostAuthnHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthnHandlerFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.User;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.mgt.util.Utils;
import org.wso2.carbon.identity.recovery.ChallengeQuestionManager;
import org.wso2.carbon.identity.recovery.IdentityRecoveryConstants;
import org.wso2.carbon.identity.recovery.IdentityRecoveryException;
import org.wso2.carbon.identity.recovery.IdentityRecoveryServerException;
import org.wso2.carbon.identity.recovery.model.ChallengeQuestion;
import org.wso2.carbon.identity.recovery.model.UserChallengeAnswer;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class will check whether the challenge questions are set for the user.
 * Also, It will force users to add answers to challenge questions is challenge questions are not
 * already answered
 **/

public class PostAuthnMissingChallengeQuestionsHandler extends AbstractPostAuthnHandler {

    private static final String CHALLENGE_QUESTIONS_REQUESTED = "challengeQuestionsRequested";
    private static final String SELECTED_CHALLENGE_QUESTION_PREFIX = "Q-";
    private static final String CHALLENGE_QUESTION_ANSWER_PREFIX = "A-";

    private static final Log log = LogFactory.getLog(PostAuthnMissingChallengeQuestionsHandler.class);
    private static volatile PostAuthnMissingChallengeQuestionsHandler instance;

    public static PostAuthnMissingChallengeQuestionsHandler getInstance() {
        if (instance == null) {
            synchronized (PostAuthnMissingChallengeQuestionsHandler.class) {
                if (instance == null) {
                    instance = new PostAuthnMissingChallengeQuestionsHandler();
                }
            }
        }
        return instance;
    }

    @Override
    public PostAuthnHandlerFlowStatus handle(HttpServletRequest httpServletRequest,
                                             HttpServletResponse httpServletResponse,
                                             AuthenticationContext authenticationContext)
            throws PostAuthenticationFailedException {

        if (log.isDebugEnabled()) {
            log.debug("Post authentication handling for missing security questions has started");
        }
        String forceChallengeQuestionSetting = getResidentIdpProperty(authenticationContext.getTenantDomain(),
                IdentityRecoveryConstants.ConnectorConfig.FORCE_ADD_PW_RECOVERY_QUESTION);

        if (forceChallengeQuestionSetting == null) {
            // Exit post authentication handler if the value for the resident IDP setting not found
            if (log.isDebugEnabled()) {
                log.debug("Resident IdP value not found for " + IdentityRecoveryConstants.ConnectorConfig
                        .FORCE_ADD_PW_RECOVERY_QUESTION + " hence exiting from " +
                        "PostAuthnMissingChallengeQuestionsHandler");
            }
            return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;

        } else if (forceChallengeQuestionSetting.equals("true")) {
            // Execute the post authentication handler logic is the relevant setting is enabled at resident IDP
            AuthenticatedUser user = getAuthenticatedUser(authenticationContext);

            // Return from PostAuthnMissingChallengeQuestionsHandler if no authenticated user found
            if (user == null) {
                if (log.isDebugEnabled()) {
                    log.debug("No authenticated user found. Hence returning without handling missing security" +
                            " questions");
                }
                return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
            }

            // Return from the post authentication handler if the user is Federated user
            if (user.isFederatedUser()) {
                return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
            }

            // Check whether the user already added the security questions
            if (isChallengeQuestionsProvided(user)) {
                // Return from post authenticator with Success status
                return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
            }

            boolean challengeQuestionsRequested = isChallengeQuestionRequested(authenticationContext);

            if (!challengeQuestionsRequested) {

                return handlePostAuthenticationForMissingChallengeQuestionRequest(httpServletResponse,
                        authenticationContext, user);

            } else {
                return handlePostAuthenticationForMissingChallengeQuestionResponse(httpServletRequest, user);

            }
        }
        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext authenticationContext) {

        return authenticationContext.getSequenceConfig().getAuthenticatedUser();
    }

    @SuppressWarnings("unchecked")
    private void setChallengeQuestionRequestedState(AuthenticationContext authenticationContext) {

        authenticationContext.addParameter(CHALLENGE_QUESTIONS_REQUESTED, true);
    }

    @SuppressWarnings("unchecked")
    private boolean isChallengeQuestionRequested(AuthenticationContext authenticationContext) {

        return authenticationContext.getParameter(CHALLENGE_QUESTIONS_REQUESTED) != null;
    }

    @Override
    public String getName() {
        return "PostAuthnMissingChallengeQuestionsHandler";
    }

    private String getResidentIdpProperty(String tenantDomain, String key) {
        IdentityProvider residentIdp;

        try {
            residentIdp = IdentityProviderManager.getInstance().getResidentIdP(tenantDomain);
            IdentityProviderProperty[] idpProps = residentIdp.getIdpProperties();
            for (IdentityProviderProperty property : idpProps) {
                if (property.getName().equals(key)) {
                    return property.getValue();
                }
            }

            return null;
        } catch (IdentityProviderManagementException e) {
            log.error("Resident IdP value not found. Error while retrieving resident IdP property " +
                    "for force challenge password ", e);
            return null;
        }
    }

    private boolean isChallengeQuestionsProvided(AuthenticatedUser user) {

        try {
            String userName = user.getUserName();
            int tenantId = Utils.getTenantId(MultitenantUtils.getTenantDomain(userName));

            UserStoreManager userStoreManager =
                    FrameworkServiceDataHolder.getInstance().getRealmService()
                            .getTenantUserRealm(tenantId)
                            .getUserStoreManager();

            Map<String, String> claimsMap = userStoreManager
                    .getUserClaimValues(userName, new String[]{IdentityRecoveryConstants.CHALLENGE_QUESTION_URI},
                            UserCoreConstants.DEFAULT_PROFILE);

            String claimValue = claimsMap.get(IdentityRecoveryConstants.CHALLENGE_QUESTION_URI);
            return claimValue != null;

        } catch (IdentityException e) {
            log.error("Identity exception occurred for user :" + user.getUserName(), e);

        } catch (UserStoreException e) {
            log.error("User store exception occurred for user :" + user.getUserName(), e);
        }
        return false;
    }

    private List<ChallengeQuestion> getChallengeQuestions(AuthenticatedUser user) {
        String tenantDomain = MultitenantUtils.getTenantDomain(user.getUserName());

        try {
            return ChallengeQuestionManager.getInstance().getAllChallengeQuestions
                    (tenantDomain);
        } catch (IdentityRecoveryServerException e) {
            log.error("Identity recovery server error occurred for user:" + user.getUserName(), e);
            return null;
        }
    }

    private void setChallengeQuestionAnswers(User user, UserChallengeAnswer[] userChallengeAnswers) {

        try {
            ChallengeQuestionManager.getInstance().setChallengesOfUser(user, userChallengeAnswers);
        } catch (IdentityRecoveryException e) {
            log.error("Unable to save challenge question answers", e);
        }
    }

    private UserChallengeAnswer[] retrieveChallengeQuestionAnswers(HttpServletRequest servletRequest,
                                                                   List<ChallengeQuestion> list) {
        Map<String, String> questionsMap = new HashMap<>();
        Map<String, String> answersMap = new HashMap<>();
        List<UserChallengeAnswer> questionsAndAnswers = new ArrayList<>();
        List<String> paramNamesList = new ArrayList<>();

        Enumeration<String> paramNames = servletRequest.getParameterNames();
        while (paramNames.hasMoreElements()) {
            paramNamesList.add(paramNames.nextElement());
        }

        for (String requestParam : paramNamesList) {
            if (requestParam.contains("Q-")) {
                String question = servletRequest.getParameter(requestParam);
                String questionSetID = requestParam.replace(SELECTED_CHALLENGE_QUESTION_PREFIX, "");
                questionsMap.put(questionSetID, question);

            } else if (requestParam.contains("A-")) {
                String answer = servletRequest.getParameter(requestParam);
                String answerSetID = requestParam.replace(CHALLENGE_QUESTION_ANSWER_PREFIX, "");
                answersMap.put(answerSetID, answer);
            }
        }
        for (String s : questionsMap.keySet()) {
            String challengeQuestion = questionsMap.get(s);
            for (ChallengeQuestion question : list) {
                if (question.getQuestion().equals(challengeQuestion)) {
                    UserChallengeAnswer questionAndAnswer = new UserChallengeAnswer();
                    questionAndAnswer.setQuestion(question);
                    questionAndAnswer.setAnswer(answersMap.get(s));
                    questionsAndAnswers.add(questionAndAnswer);
                }
            }
        }
        return questionsAndAnswers.toArray(new UserChallengeAnswer[questionsAndAnswers.size()]);
    }

    private String getUrlEncodedChallengeQuestionsString(AuthenticatedUser user) {
        StringBuilder challengeQuestionData = new StringBuilder();
        List<ChallengeQuestion> challengeQuestionList = getChallengeQuestions(user);

        if (challengeQuestionList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Challenge Questions for found for tenant domain of the user: " + user.getUserName());
            }
            return null;
        } else {
            for (ChallengeQuestion question : challengeQuestionList) {
                String setId = question.getQuestionSetId();
                String questionId = question.getQuestionId();
                String questionString = question.getQuestion();
                String questionLocale = question.getLocale();
                challengeQuestionData.append(setId).append("|").append(questionId).append("|").append
                        (questionString).append("|").append(questionLocale).append("&");
            }
        }
        String encodedData = null;
        try {
            encodedData = java.net.URLEncoder.encode(challengeQuestionData.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred while encoding Challenge question data as URL", e);
        }
        return encodedData;
    }

    private PostAuthnHandlerFlowStatus handlePostAuthenticationForMissingChallengeQuestionRequest(
            HttpServletResponse httpServletResponse, AuthenticationContext authenticationContext,
            AuthenticatedUser user) {

        // If challenge questions are not requested redirect user to add challenge questions jsp page
        String encodedData = getUrlEncodedChallengeQuestionsString(user);

        if (encodedData == null || encodedData.equals("")) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to get challenge questions for user : " + user.getUserName() + " for " +
                        "tenant domain :" + authenticationContext.getTenantDomain());
            }
            return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
        }

        try {
            // Redirect the user to fill the answers for challenge questions
            httpServletResponse.sendRedirect
                    (ConfigurationFacade.getInstance().getAuthenticationEndpointURL().replace("/login.do", ""
                    ) + "/add-security-questions" + ".jsp?sessionDataKey=" +
                            authenticationContext.getContextIdentifier() + "&data=" + encodedData);
            setChallengeQuestionRequestedState(authenticationContext);
            return PostAuthnHandlerFlowStatus.INCOMPLETE;

        } catch (IOException e) {
            log.error("Cannot redirect Error while redirecting", e);
            return PostAuthnHandlerFlowStatus.UNSUCCESS_COMPLETED;
        }
    }

    private PostAuthnHandlerFlowStatus handlePostAuthenticationForMissingChallengeQuestionResponse(
            HttpServletRequest httpServletRequest, AuthenticatedUser user) {

        // If user already redirected to add challenge questions add answers for challenge questions
        List<ChallengeQuestion> challengeQuestionList = getChallengeQuestions(user);
        UserChallengeAnswer[] answersForChallengeQuestions = retrieveChallengeQuestionAnswers
                (httpServletRequest, challengeQuestionList);
        setChallengeQuestionAnswers(user, answersForChallengeQuestions);

        return PostAuthnHandlerFlowStatus.SUCCESS_COMPLETED;
    }

}
