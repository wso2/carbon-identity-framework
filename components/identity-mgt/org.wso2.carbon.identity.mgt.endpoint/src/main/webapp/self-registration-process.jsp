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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.SelfRegisterApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.UsernameRecoveryApi" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.*" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.apache.commons.collections.map.HashedMap" %>
<%@ page import="org.wso2.carbon.base.MultitenantConstants" %>
<jsp:directive.include file="localize.jsp"/>


    <html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Wso2.identity.server")%></title>

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
                    <img src="images/logo-inverse.svg" alt=<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                 "Wso2")%> title=<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                 "Wso2")%> class="logo">

                    <h1><em><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Identity.server")%></em></h1>
                </a>
            </div>
        </div>
    </header>

    <!-- page content -->
    <div class="container-fluid body-wrapper">

        <%
            boolean isSelfRegistrationWithVerification =
                    Boolean.parseBoolean(request.getParameter("isSelfRegistrationWithVerification"));
            
            String userLocale = request.getHeader("Accept-Language");
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String callback = request.getParameter("callback");
            String tenantDomain = request.getParameter("tenantDomain");
            String consent = request.getParameter("consent");
            String policyURL = IdentityManagementServiceUtil.getInstance().getServiceContextURL().replace("/services",
                    "/authenticationendpoint/privacy_policy.do");
            if (StringUtils.isNotEmpty(consent)) {
                consent = IdentityManagementEndpointUtil.buildConsentForResidentIDP
                        (username, consent, "USA",
                                IdentityManagementEndpointConstants.Consent.COLLECTION_METHOD_SELF_REGISTRATION,
                                IdentityManagementEndpointConstants.Consent.LANGUAGE_ENGLISH, policyURL,
                                IdentityManagementEndpointConstants.Consent.EXPLICIT_CONSENT_TYPE,
                                true, false, IdentityManagementEndpointConstants.Consent.INFINITE_TERMINATION);
            }
            if (StringUtils.isBlank(callback)) {
                callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                        application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
            }
            if (StringUtils.isBlank(username)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Username.cannot.be.empty"));
                if (isSelfRegistrationWithVerification) {
                    request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
                }
            }

            if (StringUtils.isBlank(password)) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                        "Password.cannot.be.empty"));
                if (isSelfRegistrationWithVerification) {
                    request.getRequestDispatcher("self-registration-with-verification.jsp").forward(request, response);
                } else {
                    request.getRequestDispatcher("self-registration-without-verification.jsp").forward(request, response);
                }
            }

            session.setAttribute("username", username);


            User user = IdentityManagementServiceUtil.getInstance().getUser(username);


            Claim[] claims = new Claim[0];

            List<Claim> claimsList;
            UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
            try {
                claimsList = usernameRecoveryApi.claimsGet(null);
                if (claimsList != null) {
                    claims = claimsList.toArray(new Claim[claimsList.size()]);
                }
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


            List<Claim> userClaimList = new ArrayList<Claim>();
            try {

                for (Claim claim : claims) {
                    if (StringUtils.isNotBlank(request.getParameter(claim.getUri()))) {
                        Claim userClaim = new Claim();
                        userClaim.setUri(claim.getUri());
                        userClaim.setValue(request.getParameter(claim.getUri()));
                        userClaimList.add(userClaim);

                    } else if (claim.getUri().trim().equals("http://wso2.org/claims/locality")
                            && StringUtils.isNotBlank(userLocale)) {

                        Claim localeClaim = new Claim();
                        localeClaim.setUri(claim.getUri());
                        localeClaim.setValue(userLocale.split(",")[0].replace('-','_'));
                        userClaimList.add(localeClaim);

                    }
                }

                SelfRegistrationUser selfRegistrationUser = new SelfRegistrationUser();
                selfRegistrationUser.setUsername(user.getUsername());
                selfRegistrationUser.setTenantDomain(user.getTenantDomain());
                selfRegistrationUser.setRealm(user.getRealm());
                selfRegistrationUser.setPassword(password);
                selfRegistrationUser.setClaims(userClaimList);

                List<Property> properties = new ArrayList<Property>();
                Property sessionKey = new Property();
                sessionKey.setKey("callback");
                sessionKey.setValue(URLEncoder.encode(callback, "UTF-8"));
                
                Property consentProperty = new Property();
                consentProperty.setKey("consent");
                consentProperty.setValue(consent);
                properties.add(sessionKey);
                properties.add(consentProperty);


                SelfUserRegistrationRequest selfUserRegistrationRequest = new SelfUserRegistrationRequest();
                selfUserRegistrationRequest.setUser(selfRegistrationUser);
                selfUserRegistrationRequest.setProperties(properties);

                Map<String, String> requestHeaders = new HashedMap();
                if(request.getParameter("g-recaptcha-response") != null) {
                    requestHeaders.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
                }

                SelfRegisterApi selfRegisterApi = new SelfRegisterApi();
                selfRegisterApi.mePostCall(selfUserRegistrationRequest, requestHeaders);
                request.setAttribute("callback", callback);
                request.getRequestDispatcher("self-registration-complete.jsp").forward(request, response);

            } catch (Exception e) {
                Error error = new Gson().fromJson(e.getMessage(), Error.class);
                request.setAttribute("error", true);
                if (error != null) {
                    request.setAttribute("errorMsg", error.getDescription());
                    request.setAttribute("errorCode", error.getCode());
                }
                request.getRequestDispatcher("error.jsp").forward(request, response);
            }


        %>
    </div>


    <!-- footer -->
    <footer class="footer">
        <div class="container-fluid">
            <p><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Wso2.identity.server")%> | &copy;
                <script>document.write(new Date().getFullYear());</script>
                <a href="<%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "business.homepage")%>" target="_blank"><i class="icon fw fw-wso2"></i> <%=
                IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Inc")%></a>.
                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "All.rights.reserved")%>
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>


    </body>
    </html>
