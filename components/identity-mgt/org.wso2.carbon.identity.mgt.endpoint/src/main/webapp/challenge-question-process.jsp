<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.ChallengeQuestion" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.UserChallengeAnswer" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.UserChallengeQuestion" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.PasswordRecoverySecurityQuestionClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>

<%
    String userName = request.getParameter("username");
    String securityQuestionAnswer = request.getParameter("securityQuestionAnswer");

    if(userName != null) {

        User user = IdentityManagementServiceUtil.getInstance().getUser(userName);
        session.setAttribute("user", user);
        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
        ChallengeQuestionResponse challengeQuestionResponse = pwRecoverySecurityQuestionClient.initiateUserChallengeQuestion(user);
        session.setAttribute("challengeQuestionResponse", challengeQuestionResponse);
        request.getRequestDispatcher("challenge-question-view.jsp").forward(request, response);
    } else if(securityQuestionAnswer != null) {

        ChallengeQuestionResponse challengeQuestionResponse = (ChallengeQuestionResponse)session.getAttribute("challengeQuestionResponse");

        String code =  challengeQuestionResponse.getCode();
        ChallengeQuestion challengeQuestion = challengeQuestionResponse.getQuestion();
        String question = challengeQuestion.getQuestion();
        String getQuestionSetId = challengeQuestion.getQuestionSetId();

        User user = (User)session.getAttribute("user");

        VerifyAnswerRequest verifyAnswerRequest = new VerifyAnswerRequest();

        verifyAnswerRequest.setUser(user);
        verifyAnswerRequest.setCode(code);

        UserChallengeQuestion userChallengeQuestion = new UserChallengeQuestion();
        userChallengeQuestion.setQuestion(question);
        userChallengeQuestion.setQuestionSetId(getQuestionSetId);

        UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();
        userChallengeAnswer.setQuestion(userChallengeQuestion);
        userChallengeAnswer.setAnswer(securityQuestionAnswer);

        verifyAnswerRequest.setAnswer(userChallengeAnswer);
        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
        ChallengeQuestionResponse challengeQuestionResponse1 = pwRecoverySecurityQuestionClient.verifyUserChallengeAnswer(verifyAnswerRequest);
        String status = challengeQuestionResponse1.getStatus();
        session.setAttribute("challengeQuestionResponse", challengeQuestionResponse1);
        
        if("INCOMPLETE".equals(status)) {
            request.getRequestDispatcher("challenge-question-view.jsp").forward(request, response);
        }else if("COMPLETE".equals(status)) {
                request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        }
    }

%>
