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
<%@page import="java.util.ArrayList" %>
<%@page import="java.util.Arrays" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.TenantDataManager" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.identity.core.util.IdentityCoreConstants" %>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

    <%
        String BUNDLE = "org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

        String[] missingClaimList = null;
        if (request.getParameter("missingClaims") != null) {
            missingClaimList = request.getParameter("missingClaims").split(",");
        }

    %>

    <script>
    </script>

    <html>
    <head>
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

    <!-- page content -->
    <div class="container-fluid body-wrapper">
<form action="../commonauth" method="post" id="claimForm">
        <div class="row">
            <div class="col-md-12">

                <!-- content -->
                <div class="container col-centered">
                    <div>
                        <h2 class="wr-title uppercase padding-double boarder-bottom-blue margin-none">Missing Details </h2>
                    </div>
                    <div>
                        <div class="padding-double login-form">
<table style="width: 100%" class="styledLeft">
                            <% for (String claim : missingClaimList) { %>

                                <tr>
                                    <td class="leftCol-small"><%=claim%></td>
                                    <td style="padding: 1%"><input id="<%=claim%>" value="" name="<%=claim%>" class="text-box-big" type="text" /></td>
                                </tr>
                            <%}%>
</table>
                        </div>
                    </div>
                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 form-group">
                        <input type="hidden" name="sessionDataKey" value='<%=Encode.forHtmlAttribute
                            (request.getParameter("sessionDataKey"))%>'/>
                    </div>
			<div class="form-actions">
            <button
                    class="wr-btn grey-bg col-xs-12 col-md-12 col-lg-12 uppercase font-extra-large"
                    type="submit">Submit
            </button>
        </div>
        </form>
                    <!-- /content -->

                </div>
            </div>
            <!-- /content/body -->

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


</fmt:bundle>