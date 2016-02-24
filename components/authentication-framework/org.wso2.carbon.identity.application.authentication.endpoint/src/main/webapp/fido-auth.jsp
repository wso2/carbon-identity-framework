<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

<%
    String authRequest = request.getParameter("data");
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

<body onload='talkToDevice();'>

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
            <div class="container col-xs-7 col-sm-5 col-md-4 col-lg-3 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">Verification </h2>
                </div>

                <div class="boarder-all col-lg-12 padding-top-double padding-bottom-double">
                    <div class="padding-bottom-double font-large">Touch your U2F device to Proceed </div>
                    <div> <img class="img-responsive" src="images/U2F.png"> </div>
                </div>
            </div>
            <!-- /content -->

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

<script type="text/javascript">
    function talkToDevice(){
        var authRequest = '<%=Encode.forJavaScriptBlock(authRequest)%>';
        var jsonAuthRequest = JSON.parse(authRequest);
        setTimeout(function() {
            u2f.sign(jsonAuthRequest.authenticateRequests,
                    function(data) {
                        var form = document.getElementById('form');
                        var reg = document.getElementById('tokenResponse');
                        reg.value = JSON.stringify(data);
                        form.submit();
                    });
        }, 1000);
    }


</script>

<form method="POST" action="../commonauth" id="form" onsubmit="return false;">
    <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>
    <input type="hidden" name="tokenResponse" id="tokenResponse" value="tmp val"/>
</form>

</body>
</html>


</fmt:bundle>