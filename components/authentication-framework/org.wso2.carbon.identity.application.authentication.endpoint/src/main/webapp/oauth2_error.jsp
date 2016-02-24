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

<%
    String errorCode = request.getParameter("oauthErrorCode");
    String errorMsg = request.getParameter("oauthErrorMsg");
%>

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

<script type="text/javascript">
    function approved() {
        document.getElementById('consent').value = "approve";
        document.getElementById("oauth2_authz").submit();
    }
    function approvedAlways() {
        document.getElementById('consent').value = "approveAlways";
        document.getElementById("oauth2_authz").submit();
    }
    function deny() {
        document.getElementById('consent').value = "deny";
        document.getElementById("oauth2_authz").submit();
    }
</script>

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
        <div class="col-md-12">

            <!-- content -->
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2
                            class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Invalid Request
                    </h2>
                </div>

                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <form action="../commonauth" method="post" id="oauth2_authz" name="oauth2_authz" class="form-horizontal" >
                        <div class="padding-double login-form">
                            <div id="workArea">
                                <table>
                                    <%
                                        if (errorCode != null && errorMsg != null) {
                                    %>
                                    <tr>
                                        <td><b><%=Encode.forHtmlContent(errorCode)%>
                                        </b></td>
                                    </tr>
                                    <tr>
                                        <td><%=Encode.forHtmlContent(errorMsg)%>
                                        </td>
                                    </tr>
                                    <%
                                    } else {
                                    %>
                                    <tr>
                                        <td><fmt:message key='oauth.processing.error.msg'/></td>
                                    </tr>
                                    <%
                                        }
                                    %>
                                </table>
                            </div>
                        </div>
                    </form>

                </div>
            </div>


        </div>
        <!-- /content -->

    </div>
</div>
<!-- /content/body -->

</div>

<!-- footer -->
<footer class="footer">
    <div class="container-fluid">
        <p>WSO2 Identity Server | &copy; <script>document.write(new Date().getFullYear());</script> <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.</p>
    </div>
</footer>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
</body>
</html>


