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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserRegistrationClient" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.Claim" %>
<%@ page import="org.apache.cxf.jaxrs.impl.ResponseImpl" %>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));


    boolean isFirstNameInClaims = true;
    boolean isFirstNameRequired = true;
    boolean isLastNameInClaims = true;
    boolean isLastNameRequired = true;
    boolean isEmailInClaims = true;
    boolean isEmailRequired = true;

    Claim[] claims = new Claim[0];

    UserRegistrationClient userRegistrationClient = new UserRegistrationClient();
    Response responseForAllClaims = userRegistrationClient.getAllClaims(null);
    if(responseForAllClaims != null && Response.Status.OK.getStatusCode() == responseForAllClaims.getStatus()) {
        String claimsContent = responseForAllClaims.readEntity(String.class);
        Gson gson = new Gson();
        claims = gson.fromJson(claimsContent, Claim[].class);
    }
    if(((ResponseImpl)responseForAllClaims).getHeaders().containsKey("reCaptcha") &&
            Boolean.parseBoolean((String) ((ResponseImpl)responseForAllClaims).getHeaders().get("reCaptcha").get(0))) {
        request.setAttribute("reCaptcha", "true");
        request.setAttribute("reCaptchaKey", ((ResponseImpl)responseForAllClaims).getHeaders().get("reCaptchaKey").get(0));
        request.setAttribute("reCaptchaAPI", ((ResponseImpl)responseForAllClaims).getHeaders().get("reCaptchaAPI").get(0));
    }

%>
<%
    boolean reCaptchaEnabled = false;
    if (request.getAttribute("reCaptcha") != null && "TRUE".equalsIgnoreCase((String) request.getAttribute("reCaptcha"))) {
        reCaptchaEnabled = true;
    }
%>
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

        <%
            if (reCaptchaEnabled) {
        %>
        <script src='<%=(request.getAttribute("reCaptchaAPI"))%>'></script>
        <%
            }
        %>

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

        <div class="row">
            <!-- content -->
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
                <form action="processregistration.do" method="post" id="register">
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Create
                        An Account</h2>

                    <div class="clearfix"></div>
                    <div class="boarder-all ">

                        <% if (error) { %>
                        <div class="alert alert-danger" id="server-error-msg">
                            <%= Encode.forHtmlContent(errorMsg) %>
                        </div>
                        <% } %>

                        <div class="alert alert-danger" id="error-msg" hidden="hidden">
                        </div>

                        <div class="padding-double font-large">Enter required fields to complete registration</div>
                        <!-- validation -->
                        <div class="padding-double">
                            <div id="regFormError" class="alert alert-danger" style="display:none"></div>
                            <div id="regFormSuc" class="alert alert-success" style="display:none"></div>

                            <% if (isFirstNameInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">First Name</label>
                                <input type="text" name="http://wso2.org/claims/givenname" class="form-control"
                                    <% if (isFirstNameRequired) {%> required <%}%>>
                            </div>
                            <%}%>

                            <% if (isLastNameInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">Last Name</label>
                                <input type="text" name="http://wso2.org/claims/lastname" class="form-control"
                                    <% if (isLastNameRequired) {%> required <%}%>>
                            </div>
                            <%}%>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Username</label>
                                <input id="username" name="username" type="text"
                                       class="form-control required usrName usrNameLength" required>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">Password</label>
                                <input id="password" name="password" type="password"
                                       class="form-control" required>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">Confirm password</label>
                                <input id="password2" name="password2" type="password" class="form-control"
                                       data-match="reg-password" required>
                            </div>

                            <% if (isEmailInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Email</label>
                                <input type="email" name="http://wso2.org/claims/emailaddress" data-claim-uri="http://wso2.org/claims/emailaddress"
                                       class="form-control" data-validate="email"
                                    <% if (isEmailRequired) {%> required <%}%>>
                            </div>
                            <%}%>

                            <% for (Claim claim : claims) {
                                if (!StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) &&
                                    !StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) &&
                                    !StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM) &&
                                    !StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_URI_CLAIM) &&
                                    !StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_1_CLAIM) &&
                                    !StringUtils.equals(claim.getClaimUri(), IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_2_CLAIM)) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label <% if (claim.isRequired()) {%> class="control-label" <%}%>><%= Encode
                                        .forHtmlContent(claim.getDisplayTag()) %>
                                </label>
                                <input name="<%= Encode.forHtmlAttribute(claim.getClaimUri()) %>"
                                       data-claim-uri="<%= Encode.forHtmlAttribute(claim.getClaimUri()) %>"
                                       class="form-control"
                                    <% if (claim.isRequired()) {%> required <%}%>>
                            </div>
                            <%
                                    }
                                }
                            %>
                            <%
                                if (reCaptchaEnabled) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <div class="g-recaptcha"
                                     data-sitekey="<%=Encode.forHtmlContent((String)request.getAttribute("reCaptchaKey"))%>">
                                </div>
                            </div>
                            <%
                                }
                            %>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input id="isSelfRegistrationWithVerification" type="hidden"
                                       name="isSelfRegistrationWithVerification"
                                       value="false"/>
                            </div>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <br/>
                                <button id="registrationSubmit"
                                        class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="submit">Next
                                </button>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <span class="margin-top padding-top-double font-large">Already have an account? </span>
                                <a href="<%=Encode.forHtmlAttribute(IdentityManagementEndpointUtil.getUserPortalUrl(
                                    application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>"
                                   id="signInLink" class="font-large">Sign in</a>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <!-- /content/body -->

    </div>

    <!-- footer -->
    <footer class="footer" style="position: relative">
        <div class="container-fluid">
            <p>WSO2 Identity Server | &copy;
                <script>document.write(new Date().getFullYear());</script>
                <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
    <script type="text/javascript">

        $(document).ready(function () {

            $("#register").submit(function (e) {

                var password = $("#password").val();
                var password2 = $("#password2").val();
                var error_msg = $("#error-msg");

                if (password != password2) {
                    error_msg.text("Passwords did not match. Please try again.");
                    error_msg.show();
                    $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                    return false;
                }

                <%
                if(reCaptchaEnabled) {
                %>
                var resp = $("[name='g-recaptcha-response']")[0].value;
                if (resp.trim() == '') {
                    error_msg.text("Please select reCaptcha.");
                    error_msg.show();
                    $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                    return false;
                }
                <%
                }
                %>

                return true;
            });
        });
    </script>
    </body>
    </html>
</fmt:bundle>

