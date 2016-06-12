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
<%@ page import="org.wso2.carbon.identity.mgt.util.Utils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="org.wso2.carbon.captcha.mgt.beans.xsd.CaptchaInfoBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInfoRecoveryWithNotificationClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.UserClaim" %>

<%
    UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();

    boolean isUserRegistrationEmailConfirmation =
            Boolean.parseBoolean(request.getParameter("isUserRegistrationEmailConfirmation"));
    boolean isPasswordRecoveryEmailConfirmation =
            Boolean.parseBoolean(request.getParameter("isPasswordRecoveryEmailConfirmation"));
    boolean isUsernameRecovery = Boolean.parseBoolean(request.getParameter("isUsernameRecovery"));

    // Common parameters for password recovery with email and self registration with email
    String username = request.getParameter("username");
    String confirmationKey = request.getParameter("confirmationKey");

    // Password recovery parameters
    String recoveryOption = request.getParameter("recoveryOption");

    // Captcha Related Request Parameters
    String captchaImagePath = request.getParameter("captchaImagePath");
    String captchaKey = request.getParameter("captchaKey");
    String captchaAnswer = request.getParameter("captchaAnswer");
    CaptchaInfoBean captchaInfoBean = new CaptchaInfoBean();
    captchaInfoBean.setImagePath(captchaImagePath);
    captchaInfoBean.setSecretKey(captchaKey);
    captchaInfoBean.setUserAnswer(captchaAnswer);

    VerificationBean verificationBean = null;

    if (isUserRegistrationEmailConfirmation) {
        // Self Registration Account Confirmation Scenario
        verificationBean = userInformationRecoveryClient.confirmUserSelfRegistration(username, confirmationKey,
                captchaInfoBean, MultitenantUtils.getTenantDomain(username));

        if (verificationBean != null && verificationBean.getVerified()) {
            request.getRequestDispatcher("challenge-question-add.jsp").forward(request, response);
        } else {
            request.setAttribute("username", username);
            request.setAttribute("confirmationKey", confirmationKey);
            request.setAttribute("error", true);
            request.setAttribute("errorMsg",
                    IdentityManagementEndpointUtil.getPrintableError("Invalid information provided.",
                            "Either the user not found or captcha answer is incorrect.",
                            verificationBean));
            request.getRequestDispatcher("confirmregistration.do").forward(request, response);
        }
    } else if (isUsernameRecovery) {
        // Username Recovery Scenario
        UserIdentityClaimDTO[] claimDTOs = userInformationRecoveryClient.getUserIdentitySupportedClaims(
                IdentityManagementEndpointConstants.WSO2_DIALECT);

        List<UserIdentityClaimDTO> claimDTOList = new ArrayList<UserIdentityClaimDTO>();

        for (UserIdentityClaimDTO claimDTO : claimDTOs) {
            if (claimDTO.getRequired()) {
                UserIdentityClaimDTO userIdentityClaimDTO = new UserIdentityClaimDTO();
                userIdentityClaimDTO.setClaimUri(claimDTO.getClaimUri());
                userIdentityClaimDTO.setClaimValue(request.getParameter(claimDTO.getClaimUri()));
                claimDTOList.add(userIdentityClaimDTO);
            } else if (StringUtils.equals(claimDTO.getClaimUri(),
                    IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getClaimUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) ||
                    StringUtils.equals(claimDTO.getClaimUri(),
                            IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
                UserIdentityClaimDTO userIdentityClaimDTO = new UserIdentityClaimDTO();
                userIdentityClaimDTO.setClaimUri(claimDTO.getClaimUri());
                userIdentityClaimDTO.setClaimValue(request.getParameter(claimDTO.getClaimUri()));
                claimDTOList.add(userIdentityClaimDTO);
            }
        }

        UserIdentityClaimDTO[] claimDTOArray = new UserIdentityClaimDTO[claimDTOList.size()];
        verificationBean =
                userInformationRecoveryClient.verifyAccount(claimDTOList.toArray(claimDTOArray), captchaInfoBean, null);
        if (verificationBean != null && verificationBean.getVerified()) {
            request.getRequestDispatcher("username-recovery-complete.jsp").forward(request, response);
        } else {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg",
                    IdentityManagementEndpointUtil.getPrintableError("Invalid information provided.",
                            "Either the user not found or captcha answer is incorrect.",
                            verificationBean));
            request.getRequestDispatcher("recoverusername.do").forward(request, response);
        }
    } else {
        if (isPasswordRecoveryEmailConfirmation) {
            session.setAttribute("username", username);
            session.setAttribute("confirmationKey", confirmationKey);
            request.getRequestDispatcher("password-reset.jsp").forward(request, response);
        } else {
            request.setAttribute("username", username);

            if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.EMAIL.equals(recoveryOption)) {
                request.getRequestDispatcher("password-recovery-notify.jsp").forward(request, response);
            } else if (IdentityManagementEndpointConstants.PasswordRecoveryOptions.SECURITY_QUESTIONS
                    .equals(recoveryOption)) {
                request.getRequestDispatcher("challenge-question-process.jsp?username=" + username).forward(request,
                response);
            } else {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", "Unknown Password Recovery Option");
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
