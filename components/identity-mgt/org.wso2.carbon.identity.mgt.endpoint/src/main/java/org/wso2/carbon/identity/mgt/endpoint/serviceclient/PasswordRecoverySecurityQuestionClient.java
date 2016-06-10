/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.mgt.endpoint.serviceclient;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionResponse;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionsResponse;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserPassword;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAllAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.PasswordRecoverySecurityQuestion;

import javax.ws.rs.core.Response;

/**
 * REST client for password reset with security question
 */
public class PasswordRecoverySecurityQuestionClient {

    StringBuilder builder = new StringBuilder();
    String url = IdentityManagementServiceUtil.getInstance().getServiceContextURL()
            .replace("services", "account-recovery");

    public ChallengeQuestionResponse initiateUserChallengeQuestion(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestion(user);
        ChallengeQuestionResponse challengeQuestionResponse = response.readEntity(ChallengeQuestionResponse.class);
        return challengeQuestionResponse;
    }

    public ChallengeQuestionResponse verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswer(verifyAnswerRequest);
        ChallengeQuestionResponse challengeQuestionResponse = response.readEntity(ChallengeQuestionResponse.class);
        return challengeQuestionResponse;
    }

    public Response updatePassword(UserPassword userPassword) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.updatePassword(userPassword);
        return response;
    }

    public ChallengeQuestionsResponse initiateUserChallengeQuestionAtOnce(User user) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestionAtOnce(user);
        ChallengeQuestionsResponse challengeQuestionsResponse = response.readEntity(ChallengeQuestionsResponse.class);
        return challengeQuestionsResponse;
    }

    public ChallengeQuestionsResponse verifyUserChallengeAnswerAtOnce(VerifyAllAnswerRequest verifyAllAnswerRequest) {
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory
                .create(url, PasswordRecoverySecurityQuestion.class,
                        IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswerAtOnce(verifyAllAnswerRequest);
        ChallengeQuestionsResponse challengeQuestionsResponse = response.readEntity(ChallengeQuestionsResponse.class);
        return challengeQuestionsResponse;
    }

}
