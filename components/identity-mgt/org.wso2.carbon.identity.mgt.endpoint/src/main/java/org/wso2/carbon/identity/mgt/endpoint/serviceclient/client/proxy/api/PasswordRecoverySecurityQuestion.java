package org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api;

import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserPassword;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAllAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/questions")
@Consumes({ MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_JSON })
public interface PasswordRecoverySecurityQuestion {

    @PUT @Path("/initiate") public Response initiateUserChallengeQuestion(User user);

    @PUT @Path("/verify") public Response verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest);

    @PUT @Path("/reset-password") public Response updatePassword(UserPassword userPassword);

    @PUT @Path("/initiate-all") public Response initiateUserChallengeQuestionAtOnce(User user);

    @PUT @Path("/verify-all") public Response verifyUserChallengeAnswerAtOnce(
            VerifyAllAnswerRequest verifyAllAnswerRequest);

}
