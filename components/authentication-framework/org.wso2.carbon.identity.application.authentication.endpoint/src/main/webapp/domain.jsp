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
<%@include file="localize.jsp" %>
<%@include file="init-url.jsp" %>

<%
    String domainUnknown = AuthenticationEndpointUtil.i18n(resourceBundle, "domain.unknown");
    String errorMessage = AuthenticationEndpointUtil.i18n(resourceBundle, "authentication.failed");
    boolean loginFailed = false;
    if (Boolean.parseBoolean(request.getParameter("authFailure"))) {
        loginFailed = true;
        if (request.getParameter("authFailureMsg") != null) {
            errorMessage = request.getParameter("authFailureMsg");

            if (domainUnknown.equalsIgnoreCase(errorMessage)) {
                errorMessage = AuthenticationEndpointUtil.i18n(resourceBundle, "domain.cannot.be.identified");
            }
        }
    }
%>
<script type="text/javascript">
	function doLogin() {
		var loginForm = document.getElementById('loginForm');
		loginForm.submit();
	}
</script>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%></title>

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
                <img src="images/logo-inverse.svg" alt="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>" title="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.name")%>" class="logo">
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%> </em></h1>
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
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "federated.login")%>
                    </h2>


                </div>
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <form action="<%=commonauthURL%>" method="post" id="loginForm" class="form-horizontal" >
                        <div class="padding-double login-form">
                            <% if (loginFailed) { %>
                            <div class="alert alert-erro" id="error-msg" ><%=Encode.forHtml(errorMessage)%></div>
                            <% } %>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <input id="fidp" name="fidp" type="text" class="form-control" tabindex="0"
                                       placeholder="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "domain")%>">
                            </div>
                            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute(request.getParameter("sessionDataKey"))%>'/>

                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                                <button class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                        type="submit">
                                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "submit")%>
                                </button>
                            </div>
                            <div class="clearfix"></div>
                        </div>
                    </form>

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
        <p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "wso2.identity.server")%> | &copy;
            <script>document.write(new Date().getFullYear());</script>
            <a href="<%=AuthenticationEndpointUtil.i18n(resourceBundle, "business.homepage")%>" target="_blank"><i class="icon fw fw-wso2"></i>
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "inc")%>
            </a>. <%=AuthenticationEndpointUtil.i18n(resourceBundle, "all.rights.reserved")%>
        </p>
    </div>
</footer>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
</body>
</html>
