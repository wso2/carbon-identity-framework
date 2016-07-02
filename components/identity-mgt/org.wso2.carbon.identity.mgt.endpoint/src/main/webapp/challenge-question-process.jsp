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

<%@ page import="org.apache.cxf.jaxrs.impl.ResponseImpl" %>
<%@ page import="org.wso2.carbon.identity.mgt.beans.User" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.PasswordRecoverySecurityQuestionClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionsResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ErrorResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAllAnswerRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.VerifyAnswerRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.ChallengeQuestion" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.model.UserChallengeAnswer" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>

<%
    String userName = request.getParameter("username");
    String securityQuestionAnswer = request.getParameter("securityQuestionAnswer");

    if(userName != null) {

        User user = IdentityManagementServiceUtil.getInstance().getUser(userName);
        session.setAttribute("user", user);
        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
        Response responseJAXRS = pwRecoverySecurityQuestionClient.initiateUserChallengeQuestion(user);
        int status = responseJAXRS.getStatus();
        if(Response.Status.OK.getStatusCode() == status) {
            ChallengeQuestionResponse challengeQuestionResponse = responseJAXRS.readEntity(ChallengeQuestionResponse.class);
            session.setAttribute("challengeQuestionResponse", challengeQuestionResponse);
            if(((ResponseImpl)responseJAXRS).getHeaders().containsKey("reCaptcha") &&
                    Boolean.parseBoolean((String) ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptcha").get(0))) {
                request.setAttribute("reCaptcha", "true");
                request.setAttribute("reCaptchaKey", ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptchaKey").get(0));
                request.setAttribute("reCaptchaAPI", ((ResponseImpl)responseJAXRS).getHeaders().get("reCaptchaAPI").get(0));
            }
            request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
        } else if (Response.Status.BAD_REQUEST.getStatusCode() == status || Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == status) {
            ErrorResponse errorResponse = responseJAXRS.readEntity(ErrorResponse.class);
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", errorResponse.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

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

        UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();
        userChallengeAnswer.setQuestion(challengeQuestion);
        userChallengeAnswer.setAnswer(securityQuestionAnswer);

        verifyAnswerRequest.setAnswer(userChallengeAnswer);
        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();

        Map<String, String> headers = new HashMap<String, String>();
        if (request.getParameter("g-recaptcha-response") != null) {
            headers.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
        }
        Response responseJAXRS = pwRecoverySecurityQuestionClient.verifyUserChallengeAnswer(verifyAnswerRequest, headers);
        int statusCode = responseJAXRS.getStatus();
        if(Response.Status.OK.getStatusCode() == statusCode) {
            ChallengeQuestionResponse challengeQuestionResponse1 = responseJAXRS.readEntity(ChallengeQuestionResponse.class);
            String status = challengeQuestionResponse1.getStatus();
            session.setAttribute("challengeQuestionResponse", challengeQuestionResponse1);
            if("INCOMPLETE".equals(status)) {
                request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
            }else if("COMPLETE".equals(status)) {
                request.getRequestDispatcher("password-reset.jsp").forward(request, response);
            }
        } else if (Response.Status.BAD_REQUEST.getStatusCode() == statusCode || Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == statusCode) {
            ErrorResponse errorResponse = responseJAXRS.readEntity(ErrorResponse.class);
            if ("20008".equals(errorResponse.getCode()) &&
                    ((ResponseImpl) responseJAXRS).getHeaders().containsKey("reCaptcha") &&
                    "conditional".equalsIgnoreCase((String) ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptcha").get(0))) {
                request.setAttribute("reCaptcha", "true");
                request.setAttribute("reCaptchaKey", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaKey").get(0));
                request.setAttribute("reCaptchaAPI", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaAPI").get(0));
            }
            request.setAttribute("errorResponse", errorResponse);
            request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
            return;
        }
    } else if(Boolean.parseBoolean(application.getInitParameter(
                              IdentityManagementEndpointConstants.ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {

        ChallengeQuestionsResponse challengeQuestionsResponse = (ChallengeQuestionsResponse)session.getAttribute("challengeQuestionsResponse");
        ChallengeQuestion[] challengeQuestions = challengeQuestionsResponse.getQuestion();

        UserChallengeAnswer[] userChallengeAnswers = new UserChallengeAnswer[challengeQuestions.length];
        for(int i=0;i<challengeQuestions.length;i++){

            String answer = request.getParameter(challengeQuestions[i].getQuestionSetId());
            UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();
            userChallengeAnswer.setQuestion(challengeQuestions[i]);
            userChallengeAnswer.setAnswer(answer);
            userChallengeAnswers[i] = userChallengeAnswer;

        }
        String code = challengeQuestionsResponse.getCode();
        User user = (User)session.getAttribute("user");
        VerifyAllAnswerRequest verifyAllAnswerRequest = new VerifyAllAnswerRequest();
        verifyAllAnswerRequest.setCode(code);
        verifyAllAnswerRequest.setUser(user);
        verifyAllAnswerRequest.setAnswers(userChallengeAnswers);
        Map<String, String> headers = new HashMap<String, String>();
        if (request.getParameter("g-recaptcha-response") != null) {
            headers.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
        }
        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
        Response responseJAXRS =
                pwRecoverySecurityQuestionClient.verifyUserChallengeAnswerAtOnce(verifyAllAnswerRequest, headers);
        int statusCode = responseJAXRS.getStatus();
        if(Response.Status.OK.getStatusCode() == statusCode) {
            ChallengeQuestionResponse challengeQuestionResponse1 = responseJAXRS.readEntity(ChallengeQuestionResponse.class);
            session.setAttribute("challengeQuestionResponse", challengeQuestionResponse1);
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        } else if (Response.Status.BAD_REQUEST.getStatusCode() == statusCode || Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == statusCode) {
            ErrorResponse errorResponse = responseJAXRS.readEntity(ErrorResponse.class);
            if ("20008".equals(errorResponse.getCode()) &&
                    ((ResponseImpl) responseJAXRS).getHeaders().containsKey("reCaptcha") &&
                    "conditional".equalsIgnoreCase((String) ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptcha").get(0))) {
                request.setAttribute("reCaptcha", "true");
                request.setAttribute("reCaptchaKey", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaKey").get(0));
                request.setAttribute("reCaptchaAPI", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaAPI").get(0));
            }
            request.setAttribute("errorResponse", errorResponse);
            request.getRequestDispatcher("challenge-question-request.jsp").forward(request, response);
            return;
        }
    }

%>
