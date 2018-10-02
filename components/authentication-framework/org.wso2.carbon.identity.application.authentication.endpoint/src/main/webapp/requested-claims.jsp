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

<%@page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@include file="localize.jsp" %>
<%@include file="init-url.jsp" %>

<%
    String[] missingClaimList = null;
    String appName = null;
    Boolean isFederated = false;
    if (request.getParameter(Constants.MISSING_CLAIMS) != null) {
        missingClaimList = request.getParameter(Constants.MISSING_CLAIMS).split(",");
    }
    if (request.getParameter(Constants.REQUEST_PARAM_SP) != null) {
        appName = request.getParameter(Constants.REQUEST_PARAM_SP);
    }

%>

<script>
</script>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">


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
                <h1><em><%=AuthenticationEndpointUtil.i18n(resourceBundle, "identity.server")%>
                </em></h1>
            </a>
        </div>
    </div>
</header>
<div class="container-fluid body-wrapper">

    <div class="row">
        <!-- content -->
        <div class="col-xs-12 col-sm-10 col-md-8 col-lg-5 col-centered wr-login">
            <form action="<%=commonauthURL%>" method="post" id="claimForm">
                <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                    <%=AuthenticationEndpointUtil.i18n(resourceBundle, "provide.mandatory.details")%>
                </h2>

                <div class="clearfix"></div>
                <div class="boarder-all ">

                    <div class="padding-double font-large">
                        <%=Encode.forHtmlContent(appName)%> <%=AuthenticationEndpointUtil.i18n(resourceBundle,
                            "requested.claims.recommendation")%>
                    </div>

                    <!-- validation -->
                    <div class="padding-double">
                        <% for (String claim : missingClaimList) { %>
                        <div class="col-xs-12 col-sm-12 col-md-6 col-lg-6 form-group required">
                            <label class="control-label"><%=Encode.forHtmlContent(claim)%>
                            </label>
                            <input type="text" name="claim_mand_<%=Encode.forHtmlAttribute(claim)%>" id="claim_mand_<%=Encode.forHtmlAttribute(claim)%>"
                                   class="form-control" required="required">
                        </div>
                        <%}%>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                            <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
                                (request.getParameter("sessionDataKey"))%>'/>
                        </div>
                        <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                            <br/>
                            <button class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                                    type="submit">
                                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "login")%>
                            </button>
                        </div>

                        <div class="clearfix"></div>
                    </div>
                </div>
            </form>
        </div>
    </div>
    <!-- /content/body -->

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

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

<script>
    $(document).ready(function () {
        $('.main-link').click(function () {
            $('.main-link').next().hide();
            $(this).next().toggle('fast');
            var w = $(document).width();
            var h = $(document).height();
            $('.overlay').css("width", w + "px").css("height", h + "px").show();
        });
        $('[data-toggle="popover"]').popover();
        $('.overlay').click(function () {
            $(this).hide();
            $('.main-link').next().hide();
        });


    });

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

</body>
</html>
