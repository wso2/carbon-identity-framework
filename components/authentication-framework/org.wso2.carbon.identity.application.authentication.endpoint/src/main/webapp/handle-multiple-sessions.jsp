<%--
  ~ Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~ Version 2.0 (the "License"); you may not use this file except
  ~ in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AuthContextAPIClient" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.UserSessionMgtAPIClient" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<jsp:directive.include file="init-url.jsp"/>
<jsp:directive.include file="template-mapper.jsp"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    Boolean isActionCompleted = false;
    String completedAction = null;
    String promptId = request.getParameter("promptId");
    String authAPIURL = application.getInitParameter(Constants.AUTHENTICATION_REST_ENDPOINT_URL);
    
    if (StringUtils.isBlank(authAPIURL)) {
        authAPIURL = IdentityUtil.getServerURL("/api/identity/auth/v1.2/", true, true);
    }
    if (!authAPIURL.endsWith("/")) {
        authAPIURL += "/";
    }
    authAPIURL += "context/" + request.getParameter("promptId");
    String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
    
    Gson gson = new Gson();
    Map data = gson.fromJson(contextProperties, Map.class);
    
    if (request.getParameter("terminateActiveSessionsAction") != null && request.getParameter("sessionsToTerminate") != null) {
        String[] sessionIds = request.getParameter("sessionsToTerminate").split(",");
        String deleteSessionAPIUrl;
        for (String sessionId : sessionIds) {
            deleteSessionAPIUrl = IdentityUtil.getServerURL("/api/users/v1/" + data.get("encodedUsername") + "/sessions/" + sessionId, true, true);
            UserSessionMgtAPIClient.terminateUserSession(deleteSessionAPIUrl);
        }
        isActionCompleted = true;
        completedAction = "action.success";
    } else if (request.getParameter("denyLoginAction") != null) {
        isActionCompleted = true;
        completedAction = "action.fail";
    }
%>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%>
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

<script>
    function toggleSessionCheckboxes() {
        var isMasterCheckboxChecked = document.getElementById("masterCheckbox").checked;
        var checkboxes = document.sessionsForm.sessionsToTerminate;

        if (checkboxes instanceof RadioNodeList) {
            for (i = 0; i < checkboxes.length; i++) {
                checkboxes[i].checked = isMasterCheckboxChecked;
            }
        } else {
            checkboxes.checked = isMasterCheckboxChecked;
        }
    }

    function toggleMasterCheckbox() {
        var masterCheckbox = document.getElementById("masterCheckbox");
        var checkboxes = document.sessionsForm.sessionsToTerminate;

        if (checkboxes instanceof RadioNodeList) {
            for (i = 0; i < checkboxes.length; i++) {
                if (!checkboxes[i].checked) {
                    masterCheckbox.checked = false;
                    return;
                }
            }
            masterCheckbox.checked = true;
        } else {
            masterCheckbox.checked = checkboxes.checked;
        }
    }

    function validateForm(submittedAction) {
        if (submittedAction === "terminateAction") {
            var checkboxes = document.sessionsForm.sessionsToTerminate;

            if (checkboxes instanceof RadioNodeList) {
                for (i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        return true;
                    }
                }
            } else if (checkboxes.checked) {
                return true;
            }
        } else if (submittedAction === "denyAction") {
            return true;
        }
        return false;
    }

    function submitActionCompletedForm() {
        document.getElementById("actionCompletedFormSubmit").name = "<%=completedAction%>";
        document.actionCompletedForm.submit();
    }
</script>


<header class="header header-default">
    <div class="container-fluid"><br></div>
    <div class="container-fluid">
        <div class="pull-left brand float-remove-xs text-center-xs">
            <a href="#">
                <img src="images/logo-inverse.svg" alt="wso2" title="wso2" class="logo">
                
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%>
                </em></h1>
            </a>
        </div>
    </div>
</header>
<!-- page content -->
<div class="container-fluid body-wrapper">
    
    <div class="row">
        <div class="col-md-12">
            
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-4 col-centered wr-content wr-login col-centered">
                <c:set var="data" value="<%=data%>" scope="request"/>
                <c:set var="promptId" value="<%=URLEncoder.encode(promptId, StandardCharsets.UTF_8.name())%>"
                       scope="request"/>
                
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        Multiple Active Session(s) Found
                    </h2>
                </div>
                
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">
                        
                        <form name="sessionsForm" action="handle-multiple-sessions.do" method="POST"
                              onsubmit="return validateForm(this.submitted)">
                            <h4 class="text-center padding-double">
                                You currently have <fmt:formatNumber value='${requestScope.data["maxSessionCount"]}'/>
                                active sessions.
                                You are not allowed to have more than <fmt:formatNumber
                                    value='${requestScope.data["maxSessionCount"]}'/> active sessions.
                            </h4>
                            <table class="table table-striped table-bordered">
                                <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Browser</th>
                                    <th>Platform</th>
                                    <th>Last Accessed</th>
                                    <th><input type="checkbox" onchange="toggleSessionCheckboxes()" id="masterCheckbox"
                                               checked></th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach items='${requestScope.data["sessions"]}' var="session" varStatus="loop">
                                    <tr>
                                        <td>${loop.index + 1}</td>
                                        <td>${session[2]}</td>
                                        <td>${session[3]}</td>
                                        <jsp:useBean id="dateValue" class="java.util.Date"/>
                                        <jsp:setProperty name="dateValue" property="time" value="${session[1]}"/>
                                        <td><fmt:formatDate value="${dateValue}"
                                                            pattern="MM-dd-yyyy 'at' hh:mm a"/></td>
                                        <td><input type="checkbox" onchange="toggleMasterCheckbox()"
                                                   value="${session[0]}"
                                                   name="sessionsToTerminate" checked></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                            <h4 class="text-center padding-double">
                                You need to either terminate unwanted active sessions & proceed, or deny the login.
                                <br>
                                Please select your option.
                            </h4>
                            
                            <input type="hidden" name="promptResp" value="true">
                            <input type="hidden" name="promptId" value="${requestScope.promptId}">
                            
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input name="terminateActiveSessionsAction" type="submit"
                                       onclick="this.form.submitted='terminateAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Terminate Selected Active Sessions & Proceed">
                            </div>
                            
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input name="denyLoginAction" type="submit"
                                       onclick="this.form.submitted='denyAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Deny Login">
                            </div>
                        </form>
                        
                        <form name="actionCompletedForm" action="<%=commonauthURL%>" method="POST">
                            <input type="hidden" id="promptResp" name="promptResp" value="true">
                            <input type="hidden" id="promptId" name="promptId" value="${requestScope.promptId}">
                            <input type="hidden" id="actionCompletedFormSubmit">
                        </form>
                        
                        <div class="clearfix"></div>
                    </div>
                </div>
            
            
            </div>
        
        </div>
    </div>
</div>

<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> | &copy;
            <script>document.write(new Date().getFullYear());</script>
            <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "inc")%>
            </a>. <%=AuthenticationEndpointUtil.i18n(resourceBundle, "all.rights.reserved")%>
        </p>
    </div>
</footer>

<% if (isActionCompleted) { %>
<script>
    submitActionCompletedForm();
</script>
<% } %>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

</body>
</html>
