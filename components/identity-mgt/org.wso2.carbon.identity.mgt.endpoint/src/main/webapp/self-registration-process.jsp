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
<%@ page import="org.wso2.carbon.utils.multitenancy.MultitenantUtils" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserRegistrationClient" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.SelfRegistrationRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.Claim" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>


<fmt:bundle basename="org.wso2.carbon.identity.mgt.endpoint.i18n.Resources">
    <html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>WSO2 Identity Server</title>

        <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
        <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link href="css/Roboto.css" rel="stylesheet">
        <link href="css/custom-common.css" rel="stylesheet">

        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->
    </head>

    <body>

    <!-- header -->
    <header class="header header-default">
        <div class="container-fluid"><br></div>
        <div class="container-fluid">
            <div class="pull-left brand float-remove-xs text-center-xs">
                <a href="#">
                    <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">

                    <h1><em>Identity Server</em></h1>
                </a>
            </div>
        </div>
    </header>

    <!-- page content -->
    <div class="container-fluid body-wrapper">

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

    String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
    String tenantDomain = MultitenantUtils.getTenantDomain(username);


    UserRegistrationClient userRegistrationClient = new UserRegistrationClient();
    Response responseForAllClaims = userRegistrationClient.getAllClaims(tenantDomain);

    User user = new User();
    user.setUserName(tenantAwareUsername);
    user.setTenantDomain(tenantDomain);

    List<Claim> userClaimList = new ArrayList<Claim>();
    if(responseForAllClaims != null && Response.Status.OK.getStatusCode() == responseForAllClaims.getStatus()){
        try {
            String claimsContent = responseForAllClaims.readEntity(String.class);
            Gson gson = new Gson();
            Claim[] claims = gson.fromJson(claimsContent, Claim[].class);
            for (Claim claim : claims){
                if (StringUtils.isNotBlank(request.getParameter(claim.getClaimUri()))) {
                    Claim userClaim = new Claim();
                    userClaim.setClaimUri(claim.getClaimUri());
                    userClaim.setValue(request.getParameter(claim.getClaimUri()));
                    userClaimList.add(userClaim);
                }
            }
            SelfRegistrationRequest selfRegistrationRequest =  new SelfRegistrationRequest();
            selfRegistrationRequest.setClaims(userClaimList.toArray(new Claim[userClaimList.size()]));
            selfRegistrationRequest.setUser(user);
            selfRegistrationRequest.setPassword(password);
            Map<String, String> headers = new HashMap<String, String>();
            if (request.getParameter("g-recaptcha-response") != null) {
                headers.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
            }
            userRegistrationClient.registerUser(selfRegistrationRequest, headers);
        } catch (Exception e) {
            request.getRequestDispatcher("error.jsp?errorMsg=Error occured while registering the user.").forward(request, response);
        }

    }else{
        request.getRequestDispatcher("error.jsp?errorMsg=Error occured while registering the user.").forward(request, response);
    }

%>
        <div class="alert alert-info">Registration Done.</div>
    </div>


    <!-- footer -->
    <footer class="footer">
        <div class="container-fluid">
            <p>WSO2 Identity Server | &copy;
                <script>document.write(new Date().getFullYear());</script>
                <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>


    </body>
    </html>
</fmt:bundle>

