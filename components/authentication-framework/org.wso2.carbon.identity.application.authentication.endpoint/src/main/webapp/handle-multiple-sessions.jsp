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

<%@ page import="org.wso2.carbon.identity.core.util.IdentityUtil" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="com.sun.xml.bind.v2.TODO" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@taglib prefix="e" uri="https://www.owasp.org/index.php/OWASP_Java_Encoder_Project" %>
<jsp:directive.include file="init-url.jsp"/>
<jsp:directive.include file="template-mapper.jsp"/>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String promptId = request.getParameter("promptId");
    String authAPIURL = application.getInitParameter(Constants.AUTHENTICATION_REST_ENDPOINT_URL);
    
    if (StringUtils.isBlank(authAPIURL)) {
        authAPIURL = IdentityUtil.getServerURL("/api/identity/auth/v1.1/", true, true);
    }
    if (!authAPIURL.endsWith("/")) {
        authAPIURL += "/";
    }
    authAPIURL += "context/" + request.getParameter("promptId");
    String contextProperties = AuthContextAPIClient.getContextProperties(authAPIURL);
    
    Gson gson = new Gson();
    Map data = gson.fromJson(contextProperties, Map.class);
%>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%>
    </title>
    <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
    <link href="libs/bootstrap_3.4.1/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>
<body>
<script>
    function getDateFromTimestamp(timestamp) {
        var date = new Date(Number(timestamp));
        var options = {
            month: "2-digit",
            day: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            hour12: true,
        };
        document.getElementById(timestamp).innerText = date.toLocaleDateString(undefined, options);
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
                        
                        <form name="sessionsForm" action="<%=commonauthURL%>" method="POST"
                              onsubmit="return validateForm(this.submitted)">
                            <h4 class="text-center padding-double">
                                You currently have <fmt:formatNumber
                                    value='${fn:length(requestScope.data["sessions"])}'/>
                                active session(s).
                                You are not allowed to have more than <fmt:formatNumber
                                    value='${requestScope.data["MaxSessionCount"]}'/> active session(s).
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
                                        <td id="${session[1]}">
                                            <script>getDateFromTimestamp("${session[1]}");</script>
                                        </td>
                                        <td><input type="checkbox" onchange="toggleMasterCheckbox()"
                                                   value="${session[0]}"
                                                   name="sessionsToTerminate" checked></td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                            <h4 class="text-center padding-double">
                                You need to either terminate unwanted active session(s) & proceed, or deny the login.
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
                            
                            <div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 form-group required">
                                <input name="denyLoginAction" type="submit"
                                       onclick="this.form.submitted='denyAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Deny Login">
                            </div>
                            
                            <div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 form-group required">
                                <input name="refreshAction" type="submit"
                                       onclick="this.form.submitted='refreshAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Refresh Sessions">
                            </div>
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

<div id="selected_sessions_validation" class="modal fade" tabindex="-1" role="dialog"
     aria-labelledby="mySmallModalLabel">
    <div class="modal-dialog modal-md" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span>
                </button>
                <h4 class="modal-title">Not Enough Sessions Selected</h4>
            </div>
            <div class="modal-body">
                You need to select
                <span id="minimumSessionsElement" class="mandatory-msg"> at lest 1 session </span>
                in order to proceed.
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal">OK</button>
            </div>
        </div>
    </div>
</div>

<script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
<script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>

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
            for (var i = 0; i < checkboxes.length; i++) {
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
                for (var i = 0; i < checkboxes.length; i++) {
                    if (checkboxes[i].checked) {
                        return true;
                    }
                }
            } else if (checkboxes.checked) {
                return true;
            }

        } else if (submittedAction === "denyAction" || submittedAction === "refreshAction") {
            return true;
        }
        $('#selected_sessions_validation').modal();
        return false;
    }
</script>

</body>
</html>
