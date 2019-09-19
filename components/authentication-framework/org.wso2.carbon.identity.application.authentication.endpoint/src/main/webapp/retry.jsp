<%--
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.io.File"%>
<%@include file="localize.jsp" %>

    <%
        String stat = request.getParameter("status");
        String statusMessage = request.getParameter("statusMsg");
        if (stat == null || statusMessage == null) {
            stat = AuthenticationEndpointUtil.i18n(resourceBundle, "authentication.error");
            statusMessage =  AuthenticationEndpointUtil.i18n(resourceBundle,
                    "something.went.wrong.during.authentication");
        } else {
            stat = AuthenticationEndpointUtil.customi18n(resourceBundle, stat);
            statusMessage = AuthenticationEndpointUtil.customi18n(resourceBundle, statusMessage);
        }
        session.invalidate();
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
        <%} else {%>
                <jsp:directive.include file="includes/title.jsp"/>
        <%}%>

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
    <%} else {%>
            <jsp:directive.include file="includes/header.jsp"/>
    <%}%>

    <!-- page content -->
    <div class="row">
        <div class="col-md-12">
            <!-- content -->
            <div class="container col-xs-7 col-sm-5 col-md-4 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none"><%=Encode.forHtmlContent(stat)%> </h2>
                </div>

                <div class="boarder-all col-lg-12 padding-top-double padding-bottom-double error-alert  ">
                    <div class="font-medium">
                        <strong>
                            <%=AuthenticationEndpointUtil.i18n(resourceBundle, "attention")%> :
                        </strong>
                    </div>
                    <div class="padding-bottom-double">
                        <%=Encode.forHtmlContent(statusMessage)%>
                    </div>
                </div>
            </div>
            <!-- /content -->

        </div>
    </div>

    <!-- footer -->
    <%
        File footerFile = new File(getServletContext().getRealPath("extensions/footer.jsp"));
        if (footerFile.exists()) {
    %>
            <jsp:include page="extensions/footer.jsp"/>
    <%} else {%>
            <jsp:directive.include file="includes/footer.jsp"/>
    <%}%>

    <script src="libs/jquery_3.4.1/jquery-3.4.1.js"></script>
    <script src="libs/bootstrap_3.4.1/js/bootstrap.min.js"></script>

    <script>


        $('#popover').popover({
            html: true,
            title: function () {
                return $("#popover-head").html();
            },
            content: function () {
                return $("#popover-content").html();
            }
        });

    </script>

    <script type="text/javascript" src="js/u2f-api.js"></script>

    </body>
    </html>
