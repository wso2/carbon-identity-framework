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

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserRegistrationAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO" %>
<%@ page import="org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO" %>
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    boolean isSelfRegistrationWithVerification =
            Boolean.parseBoolean(request.getParameter("isSelfRegistrationWithVerification"));

    String username = request.getParameter("username");
    String password = request.getParameter("password");

    if (StringUtils.isBlank(username)) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                             "Username cannot be empty.");
        if (isSelfRegistrationWithVerification) {
            request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
        }
    }

    if (StringUtils.isBlank(password)) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                             "Password cannot be empty.");
        if (isSelfRegistrationWithVerification) {
            request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
        }
    }

    session.setAttribute("username", username);

    if (isSelfRegistrationWithVerification) {
        UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();

        UserIdentityClaimDTO[] claimDTOs = userInformationRecoveryClient.getUserIdentitySupportedClaims(
                IdentityManagementEndpointConstants.WSO2_DIALECT);

        List<UserIdentityClaimDTO> claimDTOList = new ArrayList<UserIdentityClaimDTO>();

        for (UserIdentityClaimDTO claimDTO : claimDTOs) {
            if (StringUtils.isNotBlank(request.getParameter(claimDTO.getClaimUri()))) {
                claimDTO.setClaimValue(request.getParameter(claimDTO.getClaimUri()));
                claimDTOList.add(claimDTO);
            }
        }

        UserIdentityClaimDTO[] claimDTOArray = new UserIdentityClaimDTO[claimDTOList.size()];
        VerificationBean verificationBean =
                userInformationRecoveryClient.registerUser(MultitenantUtils.getTenantAwareUsername(username),
                                                           password,
                                                           claimDTOList.toArray(claimDTOArray), "default",
                                                           MultitenantUtils.getTenantDomain(username));

        if (verificationBean == null || !verificationBean.getVerified()) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg",
                                 IdentityManagementEndpointUtil.getPrintableError("Failed to register user " +
                                                                                  username + ".", null,
                                                                                  verificationBean));
            request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
            return;
        }

        request.getRequestDispatcher("self-registration-with-verification-notify.jsp").forward(request, response);
    } else {
        UserRegistrationAdminServiceClient registrationClient = new UserRegistrationAdminServiceClient();

        if (registrationClient.isUserExist(username)) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", "User already exists.");
            request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
            return;
        }

        UserFieldDTO[] userFieldDTOs = registrationClient.readUserFieldsForUserRegistration(
                IdentityManagementEndpointConstants.WSO2_DIALECT);

        List<UserFieldDTO> userFieldDTOList = new ArrayList<UserFieldDTO>();

        for (UserFieldDTO userFieldDTO : userFieldDTOs) {
            if (StringUtils.isNotBlank(request.getParameter(userFieldDTO.getClaimUri()))) {
                userFieldDTO.setFieldValue(request.getParameter(userFieldDTO.getClaimUri()));
                userFieldDTOList.add(userFieldDTO);
            }
        }

        char[] passwordCharArray = password.toCharArray();
        registrationClient.addUser(username, passwordCharArray, userFieldDTOList);
        request.getRequestDispatcher("challenge-question-add.jsp").forward(request, response);
    }
%>

