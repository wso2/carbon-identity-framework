<%--
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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil" %>
<%@ page import="java.io.File" %>
<jsp:directive.include file="localize.jsp"/>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));
%>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <!-- title -->
    <%
        File titleFile = new File(getServletContext().getRealPath("extensions/title.jsp"));
        if (titleFile.exists()) {
    %>
            <jsp:include page="extensions/title.jsp"/>
    <% } else { %>
            <jsp:directive.include file="includes/title.jsp"/>
    <% } %>
    
    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.4.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
    
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->
</head>

<body>

<!-- header -->
<%
    File headerFile = new File(getServletContext().getRealPath("extensions/header.jsp"));
    if (headerFile.exists()) {
%>
        <jsp:include page="extensions/header.jsp"/>
<% } else { %>
        <jsp:directive.include file="includes/header.jsp"/>
<% } %>

<!-- page content -->
<div class="container-fluid body-wrapper">
    
    <div class="row">
        <!-- content -->
        <div class="col-xs-12 col-sm-10 col-md-8 col-lg-6 col-centered wr-login">
            <form action="recoverpassword.do" method="post" id="tenantBasedRecovery">
                <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                    <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Start.password.recovery")%>
                </h2>
                
                <div class="clearfix"></div>
                <div class="boarder-all ">
                    <div class="alert alert-danger margin-left-double margin-right-double margin-top-double"
                         id="error-msg" hidden="hidden">
                    </div>
                    <% if (error) { %>
                    <div class="alert alert-danger margin-left-double margin-right-double margin-top-double"
                         id="server-error-msg">
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
                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Username")%>
                            </label>
                            
                            <input id="username" name="username" type="text"
                                   class="form-control required usrName usrNameLength" required>
                            <div class="font-small help-block">
                                <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                        "If.you.do.not.specify.tenant.domain.consider.as.super.tenant")%>
                            </div>
                        </div>
                        
                        <%
                            String callback = Encode.forHtmlAttribute
                                    (request.getParameter("callback"));
                            if (callback != null) {
                        %>
                        <div>
                            <input type="hidden" name="callback" value="<%=callback %>"/>
                        </div>
                        <%
                            }
                        %>
                        
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group username-proceed">
                            <button id="registrationSubmit"
                                    class="wr-btn grey-bg uppercase font-large full-width-xs"
                                    type="submit"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                                    "Proceed.password.recovery")%>
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
<%
    File footerFile = new File(getServletContext().getRealPath("extensions/footer.jsp"));
    if (footerFile.exists()) {
%>
        <jsp:include page="extensions/footer.jsp"/>
<% } else { %>
        <jsp:directive.include file="includes/footer.jsp"/>
<% } %>

<script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
<script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>
<script type="text/javascript">

    $(document).ready(function () {
        $("#tenantBasedRecovery").submit(function (e) {
            var errorMessage = $("#error-msg");
            errorMessage.hide();
            var username = $("#username").val();

            if (username == '') {
                errorMessage.text("Please fill the username.");
                errorMessage.show();
                $("html, body").animate({scrollTop: errorMessage.offset().top}, 'slow');
                return false;
            }
            return true;
        });
    });
</script>
</body>
</html>
