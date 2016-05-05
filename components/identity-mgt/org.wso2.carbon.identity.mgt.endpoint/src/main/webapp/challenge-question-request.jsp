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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.ChallengeQuestionIdsDTO" %>

<%
    UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();

    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    String confirmationKey = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("confirmationKey"));

    if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(confirmationKey)) {
        ChallengeQuestionIdsDTO challengeQuestionIds =
                userInformationRecoveryClient.getUserChallengeQuestionIds(username,
                                                                          confirmationKey);

        if (challengeQuestionIds != null) {
            String[] questionIds = challengeQuestionIds.getIds();

            if (ArrayUtils.isEmpty(questionIds)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg",
                                     "Could not find Security Questions. Seems you have not configured them.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

            request.getSession().setAttribute("username", username);
            request.getSession().setAttribute("confirmationKey", challengeQuestionIds.getKey());
            request.getSession().setAttribute("questionIdentifiers", questionIds);
            request.getRequestDispatcher("processsecurityquestions.do").forward(request, response);
        } else {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", "Could not find Security Questions. Seems you have not configured them.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    }
%>

