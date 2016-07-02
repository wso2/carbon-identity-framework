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
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserRegistrationClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ConfirmSelfRegistrationRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.User" %>
<%@ page import="javax.ws.rs.core.Response" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.ErrorResponse" %>

<%
    boolean error = IdentityManagementEndpointUtil.getBooleanValue(request.getAttribute("error"));
    String errorMsg = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("errorMsg"));


    String username = request.getParameter("username");
    String confirmationKey = request.getParameter("confirmation");

    if (StringUtils.isBlank(username) || StringUtils.isBlank(confirmationKey)) {
        username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));
        confirmationKey = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("confirmationKey"));
    }

    UserRegistrationClient userRegistrationClient = new UserRegistrationClient();
    ConfirmSelfRegistrationRequest confirmSelfRegistrationRequest = new ConfirmSelfRegistrationRequest();
    confirmSelfRegistrationRequest.setCode(confirmationKey);
    User user = new User();
    user.setUserName(username);
    confirmSelfRegistrationRequest.setUser(user);

    Response response1 = userRegistrationClient.confirmUser(confirmSelfRegistrationRequest);
    String message = "" ;
    if(Response.Status.OK.getStatusCode() == response1.getStatus()){
        message = "Successfully Confirmed." ;
    }else{
        error = true ;
        ErrorResponse errorResponse = response1.readEntity(ErrorResponse.class);
        errorMsg = errorResponse.getMessage() ;
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

                <div class="boarder-all ">

                    <% if (error) { %>
                    <div class="alert alert-danger" id="server-error-msg">
                        <%= Encode.forHtmlContent(errorMsg) %>
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

