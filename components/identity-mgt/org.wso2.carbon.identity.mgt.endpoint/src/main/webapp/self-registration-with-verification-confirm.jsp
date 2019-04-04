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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.SelfRegisterApi" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.*" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.base.MultitenantConstants" %>
<jsp:directive.include file="localize.jsp"/>
<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));


    String username = request.getParameter("username");
    String tenantdomain = request.getParameter("tenantdomain");
    String confirmationKey = request.getParameter("confirmation");
    String callback = request.getParameter("callback");

    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }


    if (StringUtils.isBlank(username) || StringUtils.isBlank(confirmationKey)) {
        confirmationKey = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("confirmationKey"));
    }
    String message = "" ;

    try {
        SelfRegisterApi selfRegisterApi = new SelfRegisterApi();
        CodeValidationRequest validationRequest = new CodeValidationRequest();
        List<Property> properties = new ArrayList<>();
        Property tenantDomainProperty = new Property();
        tenantDomainProperty.setKey(MultitenantConstants.TENANT_DOMAIN);
        tenantDomainProperty.setValue(tenantdomain);
        properties.add(tenantDomainProperty);

        validationRequest.setCode(confirmationKey);
        validationRequest.setProperties(properties);

        selfRegisterApi.validateCodePostCall(validationRequest);

        request.setAttribute("callback", callback);
        request.setAttribute("confirm", "true");
        request.getRequestDispatcher("self-registration-complete.jsp").forward(request,response);
    } catch (Exception e) {

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

    <html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>
            <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Wso2.identity.server")%>
        </title>

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

                    <h1><em>
                        <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Identity.server")%></em>
                    </h1>
                </a>
            </div>
        </div>
    </header>

    <!-- page content -->
    <div class="container-fluid body-wrapper">

        <div class="row">
            <!-- content -->
            <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">

                <div class="boarder-all ">

                    <% if (error) { %>
                    <div class="alert alert-danger" id="server-error-msg">
                        <%= IdentityManagementEndpointUtil.i18nBase64(recoveryResourceBundle, errorMsg) %>
                    </div>
                    <% }else{
                        %>
                    <div class="alert alert-info"><%=message%></div>
                    <%
                    } %>
                    <div class="alert alert-danger" id="error-msg" hidden="hidden"></div>
                </div>
                <div class="clearfix"></div>
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
