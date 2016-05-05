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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserChallengesDTO" %>

<%
    org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient userInformationRecoveryClient =
            new org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient();

    String username = IdentityManagementEndpointUtil.getStringValue(request.getSession().getAttribute("username"));
    String[] questionIds = IdentityManagementEndpointUtil.getStringArray(request.getSession().getAttribute(
            "questionIdentifiers"));
    String confirmationKey =
            IdentityManagementEndpointUtil.getStringValue(request.getSession().getAttribute("confirmationKey"));

    int currentStep = request.getParameter("step") != null ? Integer.parseInt(request.getParameter("step")) : 0;
    String securityQuestionAnswer = request.getParameter("securityQuestionAnswer");

    if (currentStep != 0) {
        VerificationBean verificationBean =
                userInformationRecoveryClient.verifyUserChallengeAnswer(username, confirmationKey,
                                                                        questionIds[currentStep - 1],
                                                                        securityQuestionAnswer);
        if (!verificationBean.getVerified()) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", "The answer you provided is incorrect. You cannot proceed further.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        confirmationKey = verificationBean.getKey();
        request.getSession().setAttribute("confirmationKey", confirmationKey);
    }

    if (!ArrayUtils.isEmpty(questionIds)) {
        if (currentStep < questionIds.length) {
            UserChallengesDTO securityQuestion = userInformationRecoveryClient.getChallengeQuestion(username,
                                                                                                    confirmationKey,
                                                                                                    questionIds[currentStep]);
            request.getSession().setAttribute("confirmationKey", securityQuestion.getKey());
            request.setAttribute("question", securityQuestion.getQuestion());
            request.getRequestDispatcher("challenge-question-view.jsp?step=" + (++currentStep))
                   .forward(request, response);
        } else {
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        }
    }
%>
