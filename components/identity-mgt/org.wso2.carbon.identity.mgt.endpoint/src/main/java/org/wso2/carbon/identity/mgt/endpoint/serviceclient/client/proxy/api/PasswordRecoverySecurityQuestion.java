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
package org.wso2.carbon.identity.mgt.endpoint.serviceclient.client.proxy.api;

import org.wso2.carbon.identity.mgt.beans.User;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserPassword;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAllAnswerRequest;
import org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client Proxy API for security question password recovery
 */
@Path("/questions")
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
public interface PasswordRecoverySecurityQuestion {

    @PUT
    @Path("/initiate")
    public Response initiateUserChallengeQuestion(User user);

    @PUT
    @Path("/verify")
    public Response verifyUserChallengeAnswer(VerifyAnswerRequest verifyAnswerRequest);

    @PUT
    @Path("/reset-password")
    public Response updatePassword(UserPassword userPassword);

    @PUT
    @Path("/initiate-all")
    public Response initiateUserChallengeQuestionAtOnce(User user);

    @PUT
    @Path("/verify-all")
    public Response verifyUserChallengeAnswerAtOnce(
            VerifyAllAnswerRequest verifyAllAnswerRequest);

}
