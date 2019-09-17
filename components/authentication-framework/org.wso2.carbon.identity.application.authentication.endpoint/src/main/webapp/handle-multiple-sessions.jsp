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
    <!--title-->
    <jsp:directive.include file="title.jsp"/>

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

<!--header-->
<jsp:directive.include file="header.jsp"/>

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
                                You currently have <fmt:formatNumber>
                                <e:forHtmlContent value='${fn:length(requestScope.data["sessions"])}'/>
                            </fmt:formatNumber>
                                active session(s).
                                You are not allowed to have more than <fmt:formatNumber>
                                <e:forHtmlContent value='${requestScope.data["MaxSessionCount"]}'/>
                            </fmt:formatNumber> active session(s).
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
                                        <td><e:forHtmlContent value="${loop.index + 1}"/></td>
                                        <td><e:forHtmlContent value="${session[2]}"/></td>
                                        <td><e:forHtmlContent value="${session[3]}"/></td>
                                        <td id="<e:forHtmlAttribute value="${session[1]}"/>">
                                            <script>getDateFromTimestamp(<e:forJavaScript value="${session[1]}"/>);</script>
                                        </td>
                                        <td><input type="checkbox" onchange="toggleMasterCheckbox()"
                                                   value="<e:forHtmlAttribute value="${session[0]}"/>"
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
                            <input type="hidden" name="promptId"
                                   value="<e:forHtmlAttribute value="${requestScope.promptId}"/>">
                            
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group required">
                                <input name="terminateActiveSessionsAction" type="submit"
                                       onclick="this.form.submitted='terminateActiveSessionsAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Terminate Selected Active Sessions & Proceed">
                            </div>
                            
                            <div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 form-group required">
                                <input name="denyLimitActiveSessionsAction" type="submit"
                                       onclick="this.form.submitted='denyLimitActiveSessionsAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Deny Login">
                            </div>
                            
                            <div class="col-xs-6 col-sm-6 col-md-6 col-lg-6 form-group required">
                                <input name="refreshActiveSessionsAction" type="submit"
                                       onclick="this.form.submitted='refreshActiveSessionsAction';"
                                       class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-medium"
                                       value="Refresh Sessions">
                            </div>
                            <input id="ActiveSessionsLimitAction" type="hidden" name="ActiveSessionsLimitAction"/>
                        </form>
                        <div class="clearfix"></div>
                    </div>
                </div>
            
            
            </div>
        
        </div>
    </div>
</div>

<!--footer-->
<jsp:directive.include file="footer.jsp"/>

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
        document.getElementById("ActiveSessionsLimitAction").setAttribute("value", submittedAction);

        if (submittedAction === "terminateActiveSessionsAction") {
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

        } else if (submittedAction === "denyLimitActiveSessionsAction" || submittedAction === "refreshActiveSessionsAction") {
            return true;
        }
        $('#selected_sessions_validation').modal();
        return false;
    }
</script>

</body>
</html>
