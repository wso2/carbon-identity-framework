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

<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.apache.commons.collections.map.HashedMap" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.SecurityQuestionApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.InitiateAllQuestionResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.RetryError" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.User" %>
<%@ page import="java.util.Map" %>
<jsp:directive.include file="localize.jsp"/>

<%
    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
    RetryError errorResponse = (RetryError) request.getAttribute("errorResponse");

    if (errorResponse != null) {
        username = (String) session.getAttribute("username");
    }
    if (StringUtils.isNotBlank(username)) {
        if (Boolean.parseBoolean(application.getInitParameter(
                IdentityManagementEndpointConstants.ConfigConstants.PROCESS_ALL_SECURITY_QUESTIONS))) {
            User user = IdentityManagementServiceUtil.getInstance().getUser(username);

            try {
                Map<String, String> requestHeaders = new HashedMap();
                if (request.getParameter("g-recaptcha-response") != null) {
                    requestHeaders.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
                }
                
                SecurityQuestionApi securityQuestionApi = new SecurityQuestionApi();
                InitiateAllQuestionResponse initiateAllQuestionResponse = securityQuestionApi.securityQuestionsGet(
                        user.getUsername(), user.getRealm(), user.getTenantDomain(), requestHeaders);
                IdentityManagementEndpointUtil.addReCaptchaHeaders(request, securityQuestionApi.getApiClient().getResponseHeaders());
                session.setAttribute("initiateAllQuestionResponse", initiateAllQuestionResponse);
                request.getRequestDispatcher("challenge-questions-view-all.jsp").forward(request, response);
            } catch (ApiException e) {
                if (e.getCode() == 204) {

                    //No questions found
                    request.setAttribute("error", true);
                    request.setAttribute("errorMsg",
                            IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                    "No.security.questions.found.to.recover.password.contact.system.administrator"));
                    request.setAttribute("errorCode", "18017");
                    request.getRequestDispatcher("error.jsp").forward(request, response);
                    return;

                }
                IdentityManagementEndpointUtil.addReCaptchaHeaders(request, e.getResponseHeaders());
                Error error = new Gson().fromJson(e.getMessage(), Error.class);
                request.setAttribute("error", true);
                if (error != null) {
                    request.setAttribute("errorMsg", error.getDescription());
                    request.setAttribute("errorCode", error.getCode());
                }
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }

        } else {
            request.getRequestDispatcher("challenge-question-process.jsp?username=" + username).forward(request,
                    response);
        }
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Username.missing"));
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>

