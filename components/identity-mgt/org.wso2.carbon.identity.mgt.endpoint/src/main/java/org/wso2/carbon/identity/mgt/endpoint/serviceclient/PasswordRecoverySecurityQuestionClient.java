package org.wso2.carbon.identity.mgt.endpoint.serviceclient;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.*;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api.PasswordRecoverySecurityQuestion;

import javax.ws.rs.core.Response;

public class PasswordRecoverySecurityQuestionClient {

    StringBuilder builder = new StringBuilder();
    /*String url = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL()).append
            (IdentityManagementEndpointConstants.ServiceEndpoints.USER_INFORMATION_RECOVERY_SERVICE).toString()
            .replaceAll("(?<!(http:|https:))//", "/")+"/account-recovery";*/

    String url = "http://localhost:9763"+"/account-recovery";

    public ChallengeQuestionResponse initiateUserChallengeQuestion (User user){
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory.create(url, PasswordRecoverySecurityQuestion.class, IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestion(user);
        ChallengeQuestionResponse challengeQuestionResponse = response.readEntity(ChallengeQuestionResponse.class);
        return challengeQuestionResponse;
    }

    public ChallengeQuestionResponse verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest){
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory.create(url, PasswordRecoverySecurityQuestion.class, IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswer(verifyAnswerRequest);
        ChallengeQuestionResponse challengeQuestionResponse = response.readEntity(ChallengeQuestionResponse.class);
        return challengeQuestionResponse;
    }

    public Response updatePassword(UserPassword userPassword){
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory.create(url, PasswordRecoverySecurityQuestion.class, IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response =   passwordRecoverySecurityQuestion.updatePassword(userPassword);
        return response;
    }

    public ChallengeQuestionsResponse initiateUserChallengeQuestionAtOnce(User user){
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory.create(url, PasswordRecoverySecurityQuestion.class, IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.initiateUserChallengeQuestionAtOnce(user);
        ChallengeQuestionsResponse challengeQuestionsResponse = response.readEntity(ChallengeQuestionsResponse.class);
        return challengeQuestionsResponse;
    }

    public ChallengeQuestionsResponse verifyUserChallengeAnswerAtOnce (VerifyAllAnswerRequest verifyAllAnswerRequest){
        PasswordRecoverySecurityQuestion passwordRecoverySecurityQuestion = JAXRSClientFactory.create(url, PasswordRecoverySecurityQuestion.class,IdentityManagementServiceUtil.getInstance().getJSONProvider());
        Response response = passwordRecoverySecurityQuestion.verifyUserChallengeAnswerAtOnce(verifyAllAnswerRequest);
        ChallengeQuestionsResponse challengeQuestionsResponse = response.readEntity(ChallengeQuestionsResponse.class);
        return challengeQuestionsResponse;
    }

}
