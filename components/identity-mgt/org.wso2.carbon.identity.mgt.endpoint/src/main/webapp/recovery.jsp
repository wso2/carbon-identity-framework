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

<%
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

    if (isUserRegistrationEmailConfirmation) {
        // Self Registration Account Confirmation Scenario
    } else if (isUsernameRecovery) {
        // Username Recovery Scenario
/*        UserIdentityClaimDTO[] claimDTOs = userInformationRecoveryClient.getUserIdentitySupportedClaims(
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

        UserIdentityClaimDTO[] claimDTOArray = new UserIdentityClaimDTO[claimDTOList.size()];*/
    } else {
        /*verificationBean = userInformationRecoveryClient.verifyUser(username, captchaInfoBean);*/
    }

    // if (verificationBean != null && verificationBean.getVerified()) {
    if (isUserRegistrationEmailConfirmation) {
        request.getRequestDispatcher("challenge-question-add.jsp").forward(request, response);
    } else if (isUsernameRecovery) {
        request.getRequestDispatcher("username-recovery-complete.jsp").forward(request, response);
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
                request.getRequestDispatcher("challenge-question-request.jsp").forward(request, response);
            } else {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", "Unknown Password Recovery Option");
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }
        }
    }

    /*else {
        if (isUserRegistrationEmailConfirmation || isPasswordRecoveryEmailConfirmation) {
            request.setAttribute("username", username);
            request.setAttribute("confirmationKey", confirmationKey);
        }

        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                             IdentityManagementEndpointUtil.getPrintableError("Invalid information provided.",
                                                                              "Either the user not found or captcha answer is incorrect.",
                                                                              verificationBean));

        if (isUserRegistrationEmailConfirmation) {
            request.getRequestDispatcher("confirmregistration.do").forward(request, response);
        } else if (isUsernameRecovery) {
            request.getRequestDispatcher("recoverusername.do").forward(request, response);
        } else {
            request.getRequestDispatcher("recoverpassword.do").forward(request, response);
        }
    }*/
%>
<html>
<head>
    <title></title>
</head>
<body>

</body>
</html>
