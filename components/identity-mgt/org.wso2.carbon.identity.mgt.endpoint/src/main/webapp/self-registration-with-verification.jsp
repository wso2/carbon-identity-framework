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
<%@ page import="org.apache.cxf.jaxrs.impl.ResponseImpl" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.UsernameRecoveryApi" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.*" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.SelfRegistrationMgtClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.SelfRegistrationMgtClientException" %>
<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.lang.ArrayUtils" %>
<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="java.util.Map" %>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));
    SelfRegistrationMgtClient selfRegistrationMgtClient = new SelfRegistrationMgtClient();
    boolean isFirstNameInClaims = true;
    boolean isFirstNameRequired = true;
    boolean isLastNameInClaims = true;
    boolean isLastNameRequired = true;
    boolean isEmailInClaims = true;
    boolean isEmailRequired = true;
    Integer defaultPurposeCatId = null;
    Integer userNameValidityStatusCode = null;
    String username = request.getParameter("username");
    String[] missingClaimList = new String[0];
    String[] missingClaimDisplayName = new String[0];
    if (request.getParameter(Constants.MISSING_CLAIMS) != null) {
        missingClaimList = request.getParameter(Constants.MISSING_CLAIMS).split(",");
    }
    if (request.getParameter("missingClaimsDisplayName") != null) {
        missingClaimDisplayName = request.getParameter("missingClaimsDisplayName").split(",");
    }
    boolean skipSignUpEnableCheck = Boolean.parseBoolean(request.getParameter("skipsignupenablecheck"));
    boolean isPasswordProvisionEnabled = Boolean.parseBoolean(request.getParameter("passwordProvisionEnabled"));
    String callback = Encode.forHtmlAttribute(request.getParameter("callback"));
    User user = IdentityManagementServiceUtil.getInstance().getUser(username);
    
    if (StringUtils.isEmpty(username)) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Pick a username");
        request.getRequestDispatcher("register.do").forward(request, response);
        return;
    }
    
    try {
        userNameValidityStatusCode = selfRegistrationMgtClient.checkUsernameValidity(username, skipSignUpEnableCheck);
    } catch (SelfRegistrationMgtClientException e) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Something went wrong while registering user : " + Encode.forHtmlContent(username) +
                ". Please contact administrator");
        request.getRequestDispatcher("register.do").forward(request, response);
        return;
    }
    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }
    
    if (userNameValidityStatusCode != null && !SelfRegistrationStatusCodes.CODE_USER_NAME_AVAILABLE.
            equalsIgnoreCase(userNameValidityStatusCode.toString())) {
        
        request.setAttribute("error", true);
        request.setAttribute("errorCode", userNameValidityStatusCode);
        request.getRequestDispatcher("register.do").forward(request, response);
        return;
    }
    String purposes = selfRegistrationMgtClient.getPurposes(user.getTenantDomain());
    boolean hasPurposes = StringUtils.isNotEmpty(purposes);
    
    if (hasPurposes) {
        defaultPurposeCatId = selfRegistrationMgtClient.getDefaultPurposeId(user.getTenantDomain());
    }
    Claim[] claims = new Claim[0];

    List<Claim> claimsList;
    UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
    try {
        claimsList = usernameRecoveryApi.claimsGet(user.getTenantDomain(), false);
        if (claimsList != null) {
            claims = claimsList.toArray(new Claim[claimsList.size()]);
        }
        IdentityManagementEndpointUtil.addReCaptchaHeaders(request, usernameRecoveryApi.getApiClient().getResponseHeaders());

    } catch (ApiException e) {
        Error errorD = new Gson().fromJson(e.getMessage(), Error.class);
        request.setAttribute("error", true);
        if (errorD != null) {
            request.setAttribute("errorMsg", errorD.getDescription());
            request.setAttribute("errorCode", errorD.getCode());
        }

        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>
<%
    boolean reCaptchaEnabled = false;
    if (request.getAttribute("reCaptcha") != null && "TRUE".equalsIgnoreCase((String) request.getAttribute("reCaptcha"))) {
        reCaptchaEnabled = true;
    }
%>
    <html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>WSO2 Identity Server</title>

        <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
        <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link href="libs/font-awesome/css/font-awesome.css" rel="stylesheet">
        <link href="css/Roboto.css" rel="stylesheet">
        <link href="css/custom-common.css" rel="stylesheet">
        <link rel="stylesheet" type="text/css" href="libs/jstree/dist/themes/default/style.min.css" />

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

                <% if(skipSignUpEnableCheck) { %>
                    <form action="../commonauth" method="post" id="register">
                 <% } else { %>
                     <form action="processregistration.do" method="post" id="register">
                 <% } %>
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

                        <% if (isPasswordProvisionEnabled || !skipSignUpEnableCheck) {%>
                        <div class="padding-double font-large">Enter required fields to complete registration of
                            <b><%=Encode.forHtmlAttribute(username)%></b></div>
                        <!-- validation -->
                        <div class="padding-double">
                            <div id="regFormError" class="alert alert-danger" style="display:none"></div>
                            <div id="regFormSuc" class="alert alert-success" style="display:none"></div>

                            <% if (isFirstNameInClaims) {
                                String firstNameClaimURI = "http://wso2.org/claims/givenname";
                                String firstNameValue = request.getParameter(firstNameClaimURI);
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">First Name</label>
                                <input type="text" name="<%=
                                Encode.forHtmlAttribute(firstNameClaimURI)%>" class="form-control"
                                    <% if (isFirstNameRequired) {%> required <%}%>
                                    <% if (skipSignUpEnableCheck && StringUtils.isNotEmpty(firstNameValue)) { %>
                                    value="<%= Encode.forHtmlAttribute(firstNameValue)%>" disabled <% } %>>
                            </div>
                            <%}%>

                            <% if (isLastNameInClaims) {
                                String lastNameClaimURI = "http://wso2.org/claims/lastname";
                                String lastNameValue = request.getParameter(lastNameClaimURI);
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                                <label class="control-label">Last Name</label>
                                <input type="text" name="<%=
                                Encode.forHtmlAttribute(lastNameClaimURI)%>" class="form-control"
                                    <% if (isLastNameRequired) {%> required <%}%>
                                    <% if (skipSignUpEnableCheck && StringUtils.isNotEmpty(lastNameValue)) { %>
                                    value="<%= Encode.forHtmlAttribute(lastNameValue)%>" disabled <% } %>>
                            </div>
                            <%}%>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input id="username" name="username" type="hidden" value="<%=Encode.forHtmlAttribute(username)%>"
                                       class="form-control required usrName usrNameLength">
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

                            <% if (isEmailInClaims) {
                                String claimURI = "http://wso2.org/claims/emailaddress";
                                String emailValue = request.getParameter(claimURI);
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label">Email</label>
                                <input type="email" name="<%= Encode.forHtmlAttribute(claimURI)%>" class="form-control"
                                       data-validate="email"
                                    <% if (isEmailRequired) {%> required <%}%> <% if
                                    (skipSignUpEnableCheck && StringUtils.isNotEmpty(emailValue)) {%>
                                       value="<%= Encode.forHtmlAttribute(emailValue)%>"
                                        disabled<%}%>>
                            </div>
                            <%
                                }

                                if (callback != null) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input type="hidden" name="callback" value="<%=callback %>"/>
                            </div>
                            <% for (int index = 0; index < missingClaimList.length; index++) {
                                String claim = missingClaimList[index];
                                String claimDisplayName = missingClaimDisplayName[index];
                                if (!StringUtils
                                        .equals(claim, IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM)
                                        && !StringUtils
                                        .equals(claim, IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM)
                                        && !StringUtils
                                        .equals(claim, IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <label class="control-label"><%=Encode.forHtmlContent(claimDisplayName)%>
                                </label>
                                <input type="text" name="<%=Encode.forHtmlAttribute(claim)%>"
                                       id="<%=Encode.forHtmlAttribute(claim)%>" class="form-control" required="required">
                            </div>
                            <% }}%>
                            <%
                                }
                                List<String> missingClaims = null;
                                if (ArrayUtils.isNotEmpty(missingClaimList)) {
                                    missingClaims = Arrays.asList(missingClaimList);
                                }
                                for (Claim claim : claims) {

                                if ((CollectionUtils.isEmpty(missingClaims) || !missingClaims.contains(claim.getUri())) &&
                                            !StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) &&
                                    !StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) &&
                                    !StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM) &&
                                    !StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_URI_CLAIM) &&
                                    !StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_1_CLAIM) &&
                                    !StringUtils.equals(claim.getUri(),
                                            IdentityManagementEndpointConstants.ClaimURIs.CHALLENGE_QUESTION_2_CLAIM)) {
                                    String claimURI = claim.getUri();
                                    String claimValue = request.getParameter(claimURI);
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label <% if (claim.getRequired()) {%> class="control-label" <%}%>>
                                    <%= Encode.forHtmlContent(claim.getDisplayName()) %>
                                </label>
                                <input type="text" name="<%= Encode.forHtmlAttribute(claimURI) %>"
                                       class="form-control"
                                        <% if (claim.getValidationRegex() != null) { %>
                                                pattern="<%= Encode.forHtmlContent(claim.getValidationRegex()) %>"
                                        <% } %>
                                        <% if (claim.getRequired()) { %>
                                            required
                                        <% } %>
                                    <% if(skipSignUpEnableCheck && StringUtils.isNotEmpty(claimValue)) {%>
                                       value="<%= Encode.forHtmlAttribute(claimValue)%>" disabled<%}%>>
                            </div>
                            <%
                                    }
                                }
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group"></div>
                        </div>
                        <% } else { %>
                        <div class="padding-double">
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label">User Name
                                </label>
                                <input type="text" class="form-control"
                                       value="<%=Encode.forHtmlAttribute(username)%>" disabled>
                            </div>
                            <%
                                for (Claim claim : claims) {
                                    String claimUri = claim.getUri();
                                    String claimValue = request.getParameter(claimUri);

                                    if (StringUtils.isNotEmpty(claimValue)) { %>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label"><%=claim.getDisplayName()%>
                                </label>
                                <input type="text" class="form-control"
                                       value="<%= Encode.forHtmlAttribute(claimValue)%>" disabled>
                            </div>

                            <% } }%>
                        </div>
                        <% } %>
                        <% if (skipSignUpEnableCheck) { %>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
                                (request.getParameter("sessionDataKey"))%>'/>
                        </div>
                        <% } %>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 padding-double border-top">
                            <%
                                if (hasPurposes) {
                            %>
                                <!--User Consents-->
                                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 padding-top margin-bottom-half">
                                    <div class="alert alert-warning margin-none" role="alert">
                                        <div id="consent-mgt-container">
                                            <p>
                                                <strong>We need your consent on the attributes to use for the following purposes.</strong>
                                                <span>By selecting preferred attributes, I consent to use them for the given purposes.</span>
                                            </p>
                                            <div id="tree-table"></div>
                                        </div>
                                        <div class="text-left padding-top-double">
                                            <span class="required"><strong>Please note that all consents are mandatory</strong></span>
                                        </div>
                                    </div>
                                </div>
                                <!--End User Consents-->
                            <%
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
                                <!--Cookie Policy-->
                                <div class="alert alert-warning margin-bottom-double" role="alert">
                                    <div>
                                        After a successful sign in, we use a cookie in your browser to track your session. You can refer our
                                        <a href="/authenticationendpoint/cookie_policy.do" target="policy-pane">
                                            Cookie Policy
                                        </a>
                                        for more details.
                                    </div>
                                </div>
                                <!--End Cookie Policy-->
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <!--Terms/Privacy Policy-->
                                <div>
                                    <label class="agreement-checkbox">
                                        <input type="checkbox" /> I hereby confirm that I have read and understood the
                                        <a href="/authenticationendpoint/privacy_policy.do" target="policy-pane">
                                            Privacy Policy
                                        </a>
                                    </label>
                                </div>
                                <!--End Terms/Privacy Policy-->
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <button id="registrationSubmit"
                                        class="wr-btn col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="submit">Register
                                </button>
                            </div>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input id="isSelfRegistrationWithVerification" type="hidden"
                                       name="isSelfRegistrationWithVerification"
                                       value="true"/>
                                <input id="tenantDomain" name="tenantDomain" type="hidden" value="<%=user.getTenantDomain()%>"/>
                            </div>
                            <% if (!skipSignUpEnableCheck) { %>
                                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                    <span class="margin-top padding-top-double font-large">Already have an account? </span>
                                    <a href="<%=Encode.forHtmlAttribute(IdentityManagementEndpointUtil.getUserPortalUrl(
                                        application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>"
                                       id="signInLink" class="font-large">Sign in</a>
                                </div>
                            <% } %>
                            <div class="clearfix"></div>
                        </div>
                        <div class="clearfix"></div>
                    </div>
                </form>
                
                
                </div>
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
    <script type="text/javascript" src="libs/handlebars-v4.0.11.js"></script>
    <script type="text/javascript" src="libs/jstree/dist/jstree.min.js"></script>
    <script type="text/javascript" src="libs/jstree/src/jstree-actions.js"></script>
    <script type="text/javascript">

        var container;
        var allAttributes = [];
        $(document).ready(function () {

            var ALL_ATTRIBUTES_MANDATORY = true;

            var agreementChk = $(".agreement-checkbox input");
            var registrationBtn = $("#registrationSubmit");

            if (agreementChk.length > 0) {
                registrationBtn.prop("disabled", true).addClass("disabled");
            }
            agreementChk.click(function () {
                if ($(this).is(":checked")) {
                    registrationBtn.prop("disabled", false).removeClass("disabled");
                } else {
                    registrationBtn.prop("disabled", true).addClass("disabled");
                }
            });

            $("#register").submit(function (e) {

                var unsafeCharPattern = /[<>`\"]/;
                var elements = document.getElementsByTagName("input");
                var invalidInput = false;
                var error_msg = $("#error-msg");

                for (i = 0; i < elements.length; i++) {
                    if (elements[i].type === 'text' && elements[i].value != null
                        && elements[i].value.match(unsafeCharPattern) != null) {
                        error_msg.text('For security measures following characters are restricted < > ` "');
                        error_msg.show();
                        $("html, body").animate({scrollTop: error_msg.offset().top}, 'slow');
                        invalidInput = true;
                        return false;
                    }
                }

                if (ALL_ATTRIBUTES_MANDATORY) {
                    if (container) {
                        var selectedAttributes = container.jstree(true).get_selected();
                        var allSelected = compareArrays(allAttributes, selectedAttributes) ? true : false;

                        if (!allSelected) {
                            $("#attribute_selection_validation").modal();
                            return false;
                        }
                    }

                }

                if (invalidInput) {
                    return false;
                }

                var password = $("#password").val();
                var password2 = $("#password2").val();

                if (password !== password2) {
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

                <%
                if (hasPurposes) {
                %>
                var self = this;
                e.preventDefault();
                var recipt = addReciptInformation(container);
                $('<input />').attr('type', 'hidden')
                    .attr('name', "consent")
                    .attr('value', JSON.stringify(recipt))
                    .appendTo('#register');
                self.submit();
                <%
                }
                %>

                return true;
            });
        });

        function compareArrays(arr1, arr2) {
            return $(arr1).not(arr2).length == 0 && $(arr2).not(arr1).length == 0
        };

        <%
            if (hasPurposes) {
        %>
        renderReceiptDetails(<%=purposes%>);
        <%
            }
        %>

        function renderReceiptDetails(data) {

            var treeTemplate =
                '<div id="html1">' +
                '<ul><li class="jstree-open" data-jstree=\'{"icon":"icon-book"}\'>All' +
                '<ul>' +
                '{{#purposes}}' +
                '<li data-jstree=\'{"icon":"icon-book"}\' purposeid="{{purposeId}}">{{purpose}}{{#if description}} : {{description}}{{/if}}<ul>' +
                '{{#piiCategories}}' +
                '<li data-jstree=\'{"icon":"icon-user"}\' piicategoryid="{{piiCategoryId}}">{{#if displayName}}{{displayName}}{{else}}{{piiCategory}}{{/if}}</li>' +
                '</li>' +
                '{{/piiCategories}}' +
                '</ul>' +
                '{{/purposes}}' +
                '</ul></li>' +
                '</ul>' +
                '</div>';

            var tree = Handlebars.compile(treeTemplate);
            var treeRendered = tree(data);

            $("#tree-table").html(treeRendered);

            container = $("#html1").jstree({
                plugins: ["table", "sort", "checkbox", "actions", "wholerow"],
                checkbox: {"keep_selected_style": false}
            });

            container.on('ready.jstree', function (event, data) {
                var $tree = $(this);
                $($tree.jstree().get_json($tree, {
                    flat: true
                }))
                    .each(function (index, value) {
                        var node = container.jstree().get_node(this.id);
                        allAttributes.push(node.id);
                    });
            });

        }

        function addReciptInformation(container) {
            // var oldReceipt = receiptData.receipts;
            var newReceipt = {};
            var services = [];
            var service = {};

            var selectedNodes = container.jstree(true).get_selected('full', true);
            var undeterminedNodes = container.jstree(true).get_undetermined('full', true);

            if (!selectedNodes || selectedNodes.length < 1) {
                //revokeReceipt(oldReceipt.consentReceiptID);
                return;
            }
            selectedNodes = selectedNodes.concat(undeterminedNodes);
            var relationshipTree = unflatten(selectedNodes); //Build relationship tree
            var purposes = relationshipTree[0].children;
            var newPurposes = [];

            for (var i = 0; i < purposes.length; i++) {
                var purpose = purposes[i];
                var newPurpose = {};
                newPurpose["purposeId"] = purpose.li_attr.purposeid;
                //newPurpose = oldPurpose[0];
                newPurpose['piiCategory'] = [];
                newPurpose['purposeCategoryId'] = [<%=defaultPurposeCatId%>];

                var piiCategory = [];
                var categories = purpose.children;
                for (var j = 0; j < categories.length; j++) {
                    var category = categories[j];
                    var c = {};
                    c['piiCategoryId'] = category.li_attr.piicategoryid;
                    piiCategory.push(c);
                }
                newPurpose['piiCategory'] = piiCategory;
                newPurposes.push(newPurpose);
            }
            service['purposes'] = newPurposes;
            services.push(service);
            newReceipt['services'] = services;

            return newReceipt;
        }

        function unflatten(arr) {
            var tree = [],
                mappedArr = {},
                arrElem,
                mappedElem;

            // First map the nodes of the array to an object -> create a hash table.
            for (var i = 0, len = arr.length; i < len; i++) {
                arrElem = arr[i];
                mappedArr[arrElem.id] = arrElem;
                mappedArr[arrElem.id]['children'] = [];
            }

            for (var id in mappedArr) {
                if (mappedArr.hasOwnProperty(id)) {
                    mappedElem = mappedArr[id];
                    // If the element is not at the root level, add it to its parent array of children.
                    if (mappedElem.parent && mappedElem.parent != "#" && mappedArr[mappedElem['parent']]) {
                        mappedArr[mappedElem['parent']]['children'].push(mappedElem);
                    }
                    // If the element is at the root level, add it to first level elements array.
                    else {
                        tree.push(mappedElem);
                    }
                }
            }
            return tree;
        }

    </script>

    <div id="attribute_selection_validation" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
        <div class="modal-dialog modal-md" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                    <h4 class="modal-title">Consent Selection</h4>
                </div>
                <div class="modal-body">
                    You need to provide consent for all the claims in order to proceed..!
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-dismiss="modal">Ok</button>
                </div>
            </div>
        </div>
    </div>

    </body>
    </html>
