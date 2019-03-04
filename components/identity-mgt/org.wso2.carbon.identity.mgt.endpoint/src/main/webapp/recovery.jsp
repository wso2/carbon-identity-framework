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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.UsernameRecoveryApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Claim" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.UserClaim" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<jsp:directive.include file="localize.jsp"/>

<%


    boolean isPasswordRecoveryEmailConfirmation =
            Boolean.parseBoolean(request.getParameter("isPasswordRecoveryEmailConfirmation"));
    boolean isUsernameRecovery = Boolean.parseBoolean(request.getParameter("isUsernameRecovery"));

    // Common parameters for password recovery with email and self registration with email
    String username = request.getParameter("username");
    String confirmationKey = request.getParameter("confirmationKey");
    String callback = request.getParameter("callback");
    String tenantDomain = request.getParameter("tenantDomain");
    boolean isUserPortalURL = false;

    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }

    if (callback.equals(IdentityManagementEndpointUtil.getUserPortalUrl(application
            .getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))) {
        isUserPortalURL = true;
    }

    // Password recovery parameters
    String recoveryOption = request.getParameter("recoveryOption");


    if (isUsernameRecovery) {
        // Username Recovery Scenario
        List<Claim> claims;
        UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
        try {
            claims = usernameRecoveryApi.getClaimsForUsernameRecovery(null, true);
        } catch (ApiException e) {

            Error error = new Gson().fromJson(e.getMessage(), Error.class);
            request.setAttribute("error", true);
            if (error != null) {
                request.setAttribute("errorMsg", error.getDescription());
                request.setAttribute("errorCode", error.getCode());
            }
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }

        List<UserClaim> claimDTOList = new ArrayList<UserClaim>();

        for (Claim claimDTO : claims) {
            if (StringUtils.equals(claimDTO.getUri(),
                    IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
                if (StringUtils.isNotBlank(request.getParameter(claimDTO.getUri()))) {
                    UserClaim userClaim = new UserClaim();
                    userClaim.setUri(claimDTO.getUri());
                    userClaim.setValue(request.getParameter(claimDTO.getUri()));
                    claimDTOList.add(userClaim);
                }
            }
        }

        try {
            Map<String, String> requestHeaders = new HashedMap();
            if (request.getParameter("g-recaptcha-response") != null) {
                requestHeaders.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
            }
    
            usernameRecoveryApi.recoverUsernamePost(claimDTOList, tenantDomain, null, requestHeaders);
            request.setAttribute("callback", callback);
            request.setAttribute("tenantDomain", tenantDomain);
            request.setAttribute("isUserPortalURL", isUserPortalURL);
            request.getRequestDispatcher("username-recovery-complete.jsp").forward(request, response);
        } catch (ApiException e) {
            if (e.getCode() == 204) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "No.valid.user.found"));
                request.getRequestDispatcher("recoveraccountrouter.do").forward(request, response);
                return;
            }

            Error error = new Gson().fromJson(e.getMessage(), Error.class);
            request.setAttribute("error", true);
            if (error != null) {
                request.setAttribute("errorMsg", error.getDescription());
                request.setAttribute("errorCode", error.getCode());
            }
            request.getRequestDispatcher("recoveraccountrouter.do").forward(request, response);
            return;
        }

    } else {
        if (isPasswordRecoveryEmailConfirmation) {
            session.setAttribute("username", username);
            session.setAttribute("confirmationKey", confirmationKey);
            request.setAttribute("callback", callback);
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        } else {
            request.setAttribute("username", username);
            session.setAttribute("username", username);

            if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.EMAIL.equals(recoveryOption)) {
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("password-recovery-notify.jsp").forward(request, response);
            } else if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.SECURITY_QUESTIONS
                    .equals(recoveryOption)) {
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("challenge-question-request.jsp?username=" + username).forward(request,
                        response);
            } else {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Unknown.password.recovery.option"));
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }
%>
<html>
<head>
    <title></title>
</head>
<body>

</body>
</html>
