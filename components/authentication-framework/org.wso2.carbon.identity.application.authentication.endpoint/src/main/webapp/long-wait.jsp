<%--
  ~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.AdaptiveAuthUtil" %>
<%@ page import="java.io.File" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:directive.include file="init-url.jsp"/>

<%
    String sessionDataKey = request.getParameter("sessionDataKey");
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
    <link href="css/longwait-loader.css" rel="stylesheet">
    
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <script src="js/respond.min.js"></script>
    <![endif]-->
    <script language="JavaScript" type="text/javascript" src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
    <script language="JavaScript" type="text/javascript" src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>

</head>

<body>

<div id="loader-wrapper">
    <div id="loader"></div>
    <form id="toCommonAuth" action="<%=commonauthURL%>" method="POST" style="display:none;">
        <input id="sessionDataKey" type="hidden" name="sessionDataKey" value="<%=Encode.forHtmlAttribute(sessionDataKey)%>">
    </form>
</div>

<script type="text/javascript">
    var sessionDataKey = '<%=Encode.forJavaScriptBlock(sessionDataKey)%>';
    var refreshInterval = '<%=AdaptiveAuthUtil.getRefreshInterval()%>';
    var timeout = '<%=AdaptiveAuthUtil.getRequestTimeout()%>';
    $(document).ready(function () {
        var intervalListener = window.setInterval(function () {
            checkLongWaitStatus();
        }, refreshInterval);

        var timeoutListenerListener = window.setTimeout(function () {
            window.clearInterval(intervalListener);
            window.location.replace("retry.do");
        }, timeout);

        function checkLongWaitStatus() {
            $.ajax("/longwaitstatus", {
                async: false,
                data: {waitingId: sessionDataKey},
                success: function (res) {
                    handleStatusResponse(res);
                },
                error: function (res) {
                    window.clearInterval(intervalListener);
                    window.location.replace("retry.do");
                },
                failure: function (res) {
                    window.clearInterval(intervalListener);
                    window.location.replace("retry.do");
                }
            });
        }

        function handleStatusResponse(res) {
            if (res.status === 'COMPLETED') {
                continueAuthentication();
            }
        }

        function continueAuthentication() {
            //Redirect to common auth
            window.clearInterval(intervalListener);
            document.getElementById("toCommonAuth").submit();
        }
    });
</script>
</body>
</html>
