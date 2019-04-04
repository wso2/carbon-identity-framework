<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.constants.SelfRegistrationStatusCodes" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.User" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="java.util.Map" %><%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<jsp:directive.include file="localize.jsp"/>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String username = request.getParameter("username");
    User user = IdentityManagementServiceUtil.getInstance().getUser(username);
    Object errorCodeObj = request.getAttribute("errorCode");
    Object errorMsgObj = request.getAttribute("errorMsg");
    String callback =  Encode.forHtmlAttribute(request.getParameter("callback"));
    String errorCode = null;
    String errorMsg = null;
    
    if (errorCodeObj != null) {
        errorCode = errorCodeObj.toString();
    }
    if (SelfRegistrationStatusCodes.ERROR_CODE_INVALID_TENANT.equalsIgnoreCase(errorCode)) {
        errorMsg = "Invalid tenant domain - " + user.getTenantDomain();
    } else if (SelfRegistrationStatusCodes.ERROR_CODE_USER_ALREADY_EXISTS.equalsIgnoreCase(errorCode)) {
        errorMsg = "Username '" + username + "' is already taken. Please pick a different username";
    } else if (SelfRegistrationStatusCodes.ERROR_CODE_SELF_REGISTRATION_DISABLED.equalsIgnoreCase(errorCode)) {
        errorMsg = "Self registration is disabled for tenant - " + user.getTenantDomain();
    } else if (SelfRegistrationStatusCodes.CODE_USER_NAME_INVALID.equalsIgnoreCase(errorCode)) {
        errorMsg = user.getUsername() + " is an invalid user name. Please pick a valid username.";
    } else if (errorMsgObj != null) {
        errorMsg = errorMsgObj.toString();
    }
    boolean skipSignUpEnableCheck = Boolean.parseBoolean(request.getParameter("skipsignupenablecheck"));
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
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-6 col-centered wr-login">
                <form action="signup.do" method="post" id="register">
                    <h2
                            class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                            <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Start.signing.up")%>
                    </h2>
                
                    <div class="clearfix"></div>
                    <div class="boarder-all ">
                        <div class="alert alert-danger margin-left-double margin-right-double margin-top-double" id="error-msg" hidden="hidden">
                        </div>
                        <% if (error) { %>
                        <div class="alert alert-danger margin-left-double margin-right-double margin-top-double" id="server-error-msg">
                            <%=IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle, errorMsg)%>
                        </div>
                        <% } %>
                        <!-- validation -->
                        <div class="padding-double">
                        
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <div class="margin-bottom-double">
                                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Enter.your.username.here")%>
                                </div>
                                <label class="control-label">
                                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Username")%></label>
                                
                                <input id="username" name="username" type="text"
                                       class="form-control required usrName usrNameLength" required
                                    <% if(skipSignUpEnableCheck) {%> value="<%=Encode.forHtmlAttribute(username)%>" <%}%>>
                                <div class="font-small help-block">
                                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                            "If.you.specify.tenant.domain.you.registered.under.super.tenant")%></div>
                                <input id="callback" name="callback" type="hidden" value="<%=callback%>"
                                       class="form-control required usrName usrNameLength" required>
                            </div>
                            <%  Map<String, String[]> requestMap = request.getParameterMap();
                                for (Map.Entry<String, String[]> entry : requestMap.entrySet()) {
                                    String key = Encode.forHtmlAttribute(entry.getKey());
                                    String value = Encode.forHtmlAttribute(entry.getValue()[0]); %>
                                <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group ">
                                    <input id="<%= key%>" name="<%= key%>" type="hidden"
                                           value="<%=value%>" class="form-control">
                                </div>
                            <% } %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group username-proceed">
                                <button id="registrationSubmit"
                                        class="wr-btn grey-bg uppercase font-large full-width-xs"
                                        type="submit"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "Proceed.to.self.register")%>
                                </button>
                                <a href="<%=Encode.forHtmlAttribute(IdentityManagementEndpointUtil.getUserPortalUrl(
                                    application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>"
                                   class="light-btn uppercase font-large full-width-xs">
                                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Cancel")%>
                                </a>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                    </div>
                </form>
            </div>
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


    </body>
    </html>
