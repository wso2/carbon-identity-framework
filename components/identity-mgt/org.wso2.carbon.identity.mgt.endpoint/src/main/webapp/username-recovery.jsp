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
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.base.IdentityRuntimeException" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityTenantUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.ReCaptchaApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.UsernameRecoveryApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Claim" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.ReCaptchaProperties" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<jsp:directive.include file="localize.jsp"/>

<%
    if (!Boolean.parseBoolean(application.getInitParameter(
            IdentityManagementEndpointConstants.ConfigConstants.ENABLE_EMAIL_NOTIFICATION))) {
        response.sendError(HttpServletResponse.SC_FOUND);
        return;
    }
    
    ReCaptchaApi reCaptchaApi = new ReCaptchaApi();
    String tenantDomain = request.getParameter("tenantDomain");
    
    if (StringUtils.isNotEmpty(tenantDomain)) {
        try {
            IdentityTenantUtil.getTenantId(tenantDomain);
        } catch (IdentityRuntimeException e) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", e.getMessage());
            request.getRequestDispatcher("username-recovery-tenant-request.jsp").forward(request, response);
            return;
        }
    }
    
    try {
        ReCaptchaProperties reCaptchaProperties = reCaptchaApi.getReCaptcha(tenantDomain, true, "ReCaptcha",
                "username-recovery");
        
        if (reCaptchaProperties != null && reCaptchaProperties.getReCaptchaEnabled()) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("reCaptcha", Arrays.asList(String.valueOf(true)));
            headers.put("reCaptchaAPI", Arrays.asList(reCaptchaProperties.getReCaptchaAPI()));
            headers.put("reCaptchaKey", Arrays.asList(reCaptchaProperties.getReCaptchaKey()));
            IdentityManagementEndpointUtil.addReCaptchaHeaders(request, headers);
        }
    } catch (ApiException e) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", e.getMessage());
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
    
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));

    boolean isFirstNameInClaims = false;
    boolean isLastNameInClaims = false;
    boolean isEmailInClaims = false;
    List<Claim> claims;
    UsernameRecoveryApi usernameRecoveryApi = new UsernameRecoveryApi();
    try {
        claims = usernameRecoveryApi.getClaimsForUsernameRecovery(null, true);
    } catch (ApiException e) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", e.getMessage());
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    if (claims == null || claims.size() == 0) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                "No.recovery.supported.claims.found"));
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }

    for (Claim claim : claims) {
        if (StringUtils.equals(claim.getUri(),
                IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM)) {
            isFirstNameInClaims = true;
        }
        if (StringUtils.equals(claim.getUri(), IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM)) {
            isLastNameInClaims = true;
        }
        if (StringUtils.equals(claim.getUri(),
                IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
            isEmailInClaims = true;
        }
    }

%>
<%
    boolean reCaptchaEnabled = false;
    if (request.getAttribute("reCaptcha") != null &&
            "TRUE".equalsIgnoreCase((String) request.getAttribute("reCaptcha"))) {
        reCaptchaEnabled = true;
    }
%>

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

        <div class="row">
            <!-- content -->
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
                <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Recover.username")%>
                </h2>

                <div class="clearfix"></div>
                <div class="boarder-all ">

                    <% if (error) { %>
                    <div class="alert alert-danger" id="server-error-msg">
                        <%= IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle, errorMsg) %>
                    </div>
                    <% } %>
                    <div class="alert alert-danger" id="error-msg" hidden="hidden"></div>

                    <div class="padding-double font-large">
                        <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                "Enter.detail.to.recover.uname")%></div>
                    <div class="padding-double">
                        <form method="post" action="verify.do" id="recoverDetailsForm">
                            <% if (isFirstNameInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 ">
                                <label class="control-label"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "First.name")%></label>
                                <input id="first-name" type="text" name="http://wso2.org/claims/givenname"
                                       class="form-control">
                            </div>
                            <%}%>

                            <% if (isLastNameInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 ">
                                <label class="control-label"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "Last.name")%></label>
                                <input id="last-name" type="text" name="http://wso2.org/claims/lastname"
                                       class="form-control ">
                            </div>
                            <%}%>

                            <%
                                String callback = request.getParameter("callback");

                                if (StringUtils.isBlank(callback)) {
                                    callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                                            application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
                                }

                                if (callback != null) {
                            %>
                            <div>
                                <input type="hidden" name="callback" value="<%=Encode.forHtmlAttribute(callback) %>"/>
                            </div>
                            <%
                                }

                             if (isEmailInClaims) { %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 ">
                                <label class="control-label"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "Email")%></label>
                                <input id="email" type="email" name="http://wso2.org/claims/emailaddress"
                                       class="form-control"
                                       data-validate="email">
                            </div>
                            <%}%>
    
                            <%
                                if (StringUtils.isNotEmpty(tenantDomain) && !error) {
                            %>
                            <div>
                                <input type="hidden" name="tenantDomain" value="<%=Encode.forHtmlAttribute(tenantDomain)%>"/>
                            </div>
                            <%
                            } else {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 ">
                                <label class="control-label"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "Tenant.domain")%>
                                </label>
                                <input id="tenant-domain" type="text" name="tenantDomain"
                                       class="form-control ">
                            </div>
                            <%
                                }
                            %>
                            
                            <td>&nbsp;&nbsp;</td>
                            <input type="hidden" id="isUsernameRecovery" name="isUsernameRecovery" value="true">

                            <% for (Claim claim : claims) {
                                if (claim.getRequired() &&
                                        !StringUtils.equals(claim.getUri(),
                                                IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) &&
                                        !StringUtils.equals(claim.getUri(),
                                                IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) &&
                                        !StringUtils.equals(claim.getUri(),
                                                IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <label class="control-label"><%=IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle,
                                        claim.getDisplayName())%>
                                </label>
                                <input type="text" name="<%= Encode.forHtmlAttribute(claim.getUri()) %>"
                                       class="form-control"/>
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
                                     data-sitekey=
                                             "<%=Encode.forHtmlContent((String)request.getAttribute("reCaptchaKey"))%>">
                                </div>
                            </div>
                            <%
                                }
                            %>

                            <div class="form-actions">
                                <table width="100%" class="styledLeft">
                                    <tbody>
                                    <tr class="buttonRow">
                                        <td>
                                            <button id="recoverySubmit"
                                                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                                    type="submit"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Submit")%>
                                            </button>
                                        </td>
                                        <td>&nbsp;&nbsp;</td>
                                        <td>
                                            <button id="recoveryCancel"
                                                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                                    type="button"
                                                    onclick="location.href='<%=Encode.forJavaScript(IdentityManagementEndpointUtil.getUserPortalUrl(
                                                        application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>';">
                                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Cancel")%>
                                            </button>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="clearfix"></div>
            </div>
            <!-- /content/body -->

        </div>
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
    <script type="text/javascript">

        $(document).ready(function () {

            $("#recoverDetailsForm").submit(function (e) {
                var errorMessage = $("#error-msg");
                errorMessage.hide();

                <%
                if (isFirstNameInClaims){
                %>
                var firstName = $("#first-name").val();
                if (firstName == '') {
                    errorMessage.text("Please fill the first name.");
                    errorMessage.show();
                    $("html, body").animate({scrollTop: errorMessage.offset().top}, 'slow');
                    return false;
                }
                <%
                }
                %>

                <%
                if (reCaptchaEnabled) {
                %>
                var reCaptchaResponse = $("[name='g-recaptcha-response']")[0].value;
                if (reCaptchaResponse.trim() == '') {
                    errorMessage.text("Please select reCaptcha.");
                    errorMessage.show();
                    $("html, body").animate({scrollTop: errorMessage.offset().top}, 'slow');
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
