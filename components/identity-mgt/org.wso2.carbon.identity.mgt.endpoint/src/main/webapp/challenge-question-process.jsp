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

<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.SecurityQuestionApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.*" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
    String userName = request.getParameter("username");
    String securityQuestionAnswer = request.getParameter("securityQuestionAnswer");

    if (userName != null) {
        //Initiate Challenge Question flow with one by one questions

        User user = IdentityManagementServiceUtil.getInstance().getUser(userName);

        try {
            SecurityQuestionApi securityQuestionApi = new SecurityQuestionApi();
            InitiateQuestionResponse initiateQuestionResponse =
                    securityQuestionApi.securityQuestionGet(user.getUsername(), user.getRealm(), user.getTenantDomain());

            session.setAttribute("initiateChallengeQuestionResponse", initiateQuestionResponse);
            request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
        } catch (ApiException e) {
            if (e.getCode() == 204) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg",
                        "No Security Questions Found to recover password. Please contact your system Administrator");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

    } else if (securityQuestionAnswer != null) {

        InitiateQuestionResponse challengeQuestionResponse = (InitiateQuestionResponse)
                session.getAttribute("initiateChallengeQuestionResponse");


        List<SecurityAnswer> securityAnswers = new ArrayList<SecurityAnswer>();
        SecurityAnswer securityAnswer = new SecurityAnswer();
        securityAnswer.setQuestionSetId(challengeQuestionResponse.getQuestion().getQuestionSetId());
        securityAnswer.setAnswer(securityQuestionAnswer);

        securityAnswers.add(securityAnswer);

        AnswerVerificationRequest answerVerificationRequest = new AnswerVerificationRequest();
        answerVerificationRequest.setKey(challengeQuestionResponse.getKey());
        answerVerificationRequest.setAnswers(securityAnswers);


        try {
            SecurityQuestionApi securityQuestionApi = new SecurityQuestionApi();
            InitiateQuestionResponse initiateQuestionResponse =
                    securityQuestionApi.validateAnswerPost(answerVerificationRequest);


            if ("validate-answer".equalsIgnoreCase(initiateQuestionResponse.getLink().getRel())) {
                session.setAttribute("initiateChallengeQuestionResponse", initiateQuestionResponse);
                request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
            } else if ("set-password".equalsIgnoreCase(initiateQuestionResponse.getLink().getRel())) {
                session.setAttribute("confirmationKey", initiateQuestionResponse.getKey());
                request.getRequestDispatcher("password-reset.jsp").forward(request, response);
            }

        } catch (ApiException e) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

//        VerifyAnswerRequest verifyAnswerRequest = new VerifyAnswerRequest();
//
////        verifyAnswerRequest.setUser(user);
//        verifyAnswerRequest.setCode(code);
//
//        UserChallengeAnswer userChallengeAnswer = new UserChallengeAnswer();
//        userChallengeAnswer.setQuestion(challengeQuestion);
//        userChallengeAnswer.setAnswer(securityQuestionAnswer);
//
//        verifyAnswerRequest.setAnswer(userChallengeAnswer);
//        PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();
//
//        Map<String, String> headers = new HashMap<String, String>();
//        if (request.getParameter("g-recaptcha-response") != null) {
//            headers.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
//        }
//        Response responseJAXRS = pwRecoverySecurityQuestionClient.verifyUserChallengeAnswer(verifyAnswerRequest, headers);
//        int statusCode = responseJAXRS.getStatus();
//        if (Response.Status.OK.getStatusCode() == statusCode) {
//            ChallengeQuestionResponse challengeQuestionResponse1 = responseJAXRS.readEntity(ChallengeQuestionResponse.class);
//            String status = challengeQuestionResponse1.getStatus();
//            session.setAttribute("challengeQuestionResponse", challengeQuestionResponse1);
//            if ("INCOMPLETE".equals(status)) {
//                request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
//            } else if ("COMPLETE".equals(status)) {
//                request.getRequestDispatcher("password-reset.jsp").forward(request, response);
//            }
//        } else if (Response.Status.BAD_REQUEST.getStatusCode() == statusCode || Response.Status.INTERNAL_SERVER_ERROR.getStatusCode() == statusCode) {
//            ErrorResponse errorResponse = responseJAXRS.readEntity(ErrorResponse.class);
//            if ("20008".equals(errorResponse.getCode()) &&
//                    ((ResponseImpl) responseJAXRS).getHeaders().containsKey("reCaptcha") &&
//                    "conditional".equalsIgnoreCase((String) ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptcha").get(0))) {
//                request.setAttribute("reCaptcha", "true");
//                request.setAttribute("reCaptchaKey", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaKey").get(0));
//                request.setAttribute("reCaptchaAPI", ((ResponseImpl) responseJAXRS).getHeaders().get("reCaptchaAPI").get(0));
//            }
//            request.setAttribute("errorResponse", errorResponse);
//            request.getRequestDispatcher("/viewsecurityquestions.do").forward(request, response);
//            return;
//        }
    } else if (Boolean.parseBoolean(application.getInitParameter(IdentityManagementEndpointConstants
            .ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {

        //Process security questions at once

        InitiateAllQuestionResponse initiateAllQuestionResponse =
                (InitiateAllQuestionResponse) session.getAttribute("initiateAllQuestionResponse");
        List<Question> challengeQuestions = initiateAllQuestionResponse.getQuestions();

        List<SecurityAnswer> securityAnswers = new ArrayList<SecurityAnswer>();
        for (int i = 0; i < challengeQuestions.size(); i++) {

            SecurityAnswer userChallengeAnswer = new SecurityAnswer();
            userChallengeAnswer.setQuestionSetId(challengeQuestions.get(i).getQuestionSetId());
            userChallengeAnswer.setAnswer(request.getParameter(challengeQuestions.get(i).getQuestionSetId()));
            securityAnswers.add(userChallengeAnswer);

        }

        Map<String, String> headers = new HashMap<String, String>();
        if (request.getParameter("g-recaptcha-response") != null) {
            headers.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
        }


        AnswerVerificationRequest answerVerificationRequest = new AnswerVerificationRequest();
        answerVerificationRequest.setKey(initiateAllQuestionResponse.getKey());
        answerVerificationRequest.setAnswers(securityAnswers);


        try {
            SecurityQuestionApi securityQuestionApi = new SecurityQuestionApi();
            InitiateQuestionResponse initiateQuestionResponse =
                    securityQuestionApi.validateAnswerPost(answerVerificationRequest);

            session.setAttribute("confirmationKey", initiateQuestionResponse.getKey());
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);

        } catch (ApiException e) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }

%>
