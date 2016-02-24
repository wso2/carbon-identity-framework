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

<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    String[] profiles = request.getParameterValues("profile");
    String[] claimTags = request.getParameterValues("claimTag");
    String[] claimValues = request.getParameterValues("claimValue");
    String openidreturnto = request.getParameter("openid.return_to");
    String openididentity = request.getParameter("openid.identity");
    if (openidreturnto != null && openidreturnto.indexOf("?") > 0) {
        openidreturnto = openidreturnto.substring(0, openidreturnto.indexOf("?"));
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.application.authentication.endpoint.i18n.Resources">

    <html>
    <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>OpenID2.0 Profile</title>

        <link rel="icon" href="images/favicon.png" type="image/x-icon"/>
        <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
        <link href="css/Roboto.css" rel="stylesheet">
        <link href="css/custom-common.css" rel="stylesheet">

        <!--[if lt IE 9]>
        <script src="js/html5shiv.min.js"></script>
        <script src="js/respond.min.js"></script>
        <![endif]-->

        <script type="text/javascript">
            function submitProfileSelection() {
                document.profileSelection.submit();
            }

            function approved() {
                document.getElementById("hasApprovedAlways").value = "false";
                document.profile.submit();
            }

            function approvedAlways() {
                document.getElementById("hasApprovedAlways").value = "true";
                document.profile.submit();
            }
        </script>

    </head>

    <body>

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

    <div class="container-fluid body-wrapper">

        <div class="row">
            <div class="col-md-12">

                <!-- content -->
                <div
                        class="container col-xs-12 col-sm-10 col-md-7 col-lg-5 ol-centered wr-content wr-login col-centered">
                    <div>
                        <h2
                                class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                            Open ID User Claims
                        </h2>


                    </div>
                    <div class="boarder-all ">
                        <div class="clearfix"></div>

                        <div class="padding-double login-form">
                            <div>
                                <form action="../openidserver" id="profile" name="profile" class="form-horizontal">
                                    <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12 control-group">
                                        <div class="controls" style="margin-left: 0px !important;">

                                            <%
                                                if (claimTags != null && claimTags.length > 0) { %>
                                            <table class="table table-striped table-bordered">
                                                <tr>
                                                    <th>Claim URI</th>
                                                    <th>Claim Value</th>
                                                </tr>
                                                <%
                                                    for (int i = 0; i < claimTags.length; i++) {
                                                        String claimTag = claimTags[i];
                                                        if ("MultiAttributeSeparator" .equals(claimTag)) {
                                                            continue;
                                                        }
                                                %>
                                                <tr>
                                                    <td><%=Encode.forHtmlContent(claimTag)%>
                                                    </td>
                                                    <td><%=Encode.forHtmlContent(claimValues[i])%>
                                                    </td>
                                                </tr>
                                                <%
                                                    } %>
                                            </table>
                                            <%
                                                }
                                            %>
                                        </div>
                                    </div>

                                    <div style="text-align:left;">
                                        <input type="button" class="btn  btn-primary" id="approve" name="approve"
                                               onclick="javascript: approved(); return false;"
                                               value="<fmt:message key='approve'/>"/>
                                        <input type="button" class="btn" id="chkApprovedAlways"
                                               onclick="javascript: approvedAlways();"
                                               value="<fmt:message key='approve.always'/>"/>
                                        <input type="hidden" id="hasApprovedAlways" name="hasApprovedAlways"
                                               value="false"/>
                                        <input class="btn" type="reset" value="<fmt:message key='cancel'/>"
                                               onclick="javascript:document.location.href='<%=openidreturnto%>'"/>
                                    </div>
                                </form>

                            </div>
                            <div class="clearfix"></div>
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
            <p>WSO2 Identity Server | &copy;
                <script>document.write(new Date().getFullYear());</script>
                <a href="http://wso2.com/" target="_blank"><i class="icon fw fw-wso2"></i> Inc</a>. All Rights Reserved.
            </p>
        </div>
    </footer>

    <script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
    <script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
    </body>
    </html>

</fmt:bundle>
