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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.PasswordRecoverySecurityQuestionClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInfoRecoveryWithNotificationClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ChallengeQuestionResponse" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User" %>

<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserPassword" %>
<%@ page import="org.wso2.carbon.identity.mgt.util.Utils" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ResetPasswordRequest" %>
<%

    UserInfoRecoveryWithNotificationClient userInfoRecoveryWithNotificationClient = new UserInfoRecoveryWithNotificationClient();
    PasswordRecoverySecurityQuestionClient pwRecoverySecurityQuestionClient = new PasswordRecoverySecurityQuestionClient();

    String username = IdentityManagementEndpointUtil.getStringValue(request.getSession().getAttribute("username"));
    String confirmationKey =
            IdentityManagementEndpointUtil.getStringValue(request.getSession().getAttribute("confirmationKey"));
    boolean isPasswordRecoveryEmailConfirmation =
            Boolean.parseBoolean(request.getParameter("isPasswordRecoveryEmailConfirmation"));

    Response resetPasswordResponse = null;

    String newPassword = request.getParameter("reset-password");

    String userStoreDomain = Utils.getUserStoreDomainName(username);
    String tenantDomain = MultitenantUtils.getTenantDomain(username);

    if (StringUtils.isNotBlank(newPassword)) {
        if (isPasswordRecoveryEmailConfirmation) {
            User user = new User();
            user.setUserName(username);
            user.setTenantDomain(tenantDomain);
            user.setUserStoreDomain(userStoreDomain);

            ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest();
            resetPasswordRequest.setUser(user);
            resetPasswordRequest.setPassword(newPassword);
            resetPasswordRequest.setCode(confirmationKey);

            resetPasswordResponse = userInfoRecoveryWithNotificationClient.resetPassword(resetPasswordRequest);

            if ((resetPasswordResponse == null) || (StringUtils.isBlank(Integer.toString(resetPasswordResponse.getStatus()))) ||
                    !(Integer.toString(Response.Status.OK.getStatusCode()).equals(Integer.toString(resetPasswordResponse.getStatus())))) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg",
                        IdentityManagementEndpointConstants.UserInfoRecoveryErrorDesc.NOTIFICATION_ERROR_3 + "\t" +
                                IdentityManagementEndpointConstants.UserInfoRecoveryErrorDesc.NOTIFICATION_ERROR_4);
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
        } else {
            ChallengeQuestionResponse challengeQuestionResponse = (ChallengeQuestionResponse) session.getAttribute("challengeQuestionResponse");
            User user = (User) session.getAttribute("user");

            UserPassword userPassword = new UserPassword();
            userPassword.setCode(challengeQuestionResponse.getCode());
            userPassword.setUser(user);
            userPassword.setPassword(newPassword);
            pwRecoverySecurityQuestionClient.updatePassword(userPassword);
        }

    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Password cannot be empty.");
        request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        return;
    }

    session.invalidate();
%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <div id="infoModel" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title">Information</h4>
                </div>
                <div class="modal-body">
                    <p>Updated the password successfully</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<script type="application/javascript">
    $(document).ready(function () {
        var infoModel = $("#infoModel");
        infoModel.modal("show");
        infoModel.on('hidden.bs.modal', function () {
            location.href = "<%=Encode.forJavaScript(IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>";
        })
    });
</script>
</body>
</html>
