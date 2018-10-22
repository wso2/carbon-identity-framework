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

<%@ page import="org.apache.commons.collections.CollectionUtils" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.authentication.endpoint.util.Constants" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.stream.Collectors" %>
<%@ page import="java.util.stream.Stream" %>
<%@include file="localize.jsp" %>
<%@include file="init-url.jsp" %>

<%
    String app = request.getParameter("application");
    String scopeString = request.getParameter("scope");
    boolean displayScopes = Boolean.parseBoolean(getServletContext().getInitParameter("displayScopes"));
%>

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

<script type="text/javascript">
    function approved() {
        var scopeApproval = $("input[name='scope-approval']");

        // If scope approval radio button is rendered then we need to validate that it's checked
        if (scopeApproval.length > 0) {
            if (scopeApproval.is(":checked")) {
                var checkScopeConsent = $("input[name='scope-approval']:checked");
                $('#consent').val(checkScopeConsent.val());
            } else {
                $("#modal_scope_validation").modal();
                return;
            }
        }

        document.getElementById("profile").submit();
    }
    
    function deny() {
        document.getElementById('consent').value = "deny";
        document.getElementById("profile").submit();
    }
</script>

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
            <div class="container col-xs-10 col-sm-6 col-md-6 col-lg-5 col-centered wr-content wr-login col-centered">
                <div>
                    <h2 class="wr-title uppercase blue-bg padding-double white boarder-bottom-blue margin-none">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "authorize")%>
                    </h2>
                </div>
                
                <div class="boarder-all ">
                    <div class="clearfix"></div>
                    <div class="padding-double login-form">
                        <form action="<%=oauth2AuthorizeURL%>" method="post" id="profile" name="oauth2_authz"
                              class="form-horizontal" >
                            
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <div class="alert alert-warning" role="alert">
                                    <p class="margin-bottom-double">
                                        <strong><%=Encode.forHtml(request.getParameter("application"))%></strong>
                                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "request.access.profile")%>
                                    </p>
                                </div>
                            </div>
                            
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                
                                    <%
                                        if (displayScopes && StringUtils.isNotBlank(scopeString)) {
                                            // Remove "openid" from the scope list to display.
                                            List<String> openIdScopes = Stream.of(scopeString.split(" "))
                                                    .filter(x -> !StringUtils.equalsIgnoreCase(x, "openid"))
                                                    .collect(Collectors.toList());
            
                                            if (CollectionUtils.isNotEmpty(openIdScopes)) {
                                    %>
                                <h5 class="section-heading-5"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "requested.scopes")%>
                                </h5>
                                <div class="border-gray" style="border-bottom: none;">
        
                                    <ul class="scopes-list padding">
                                        <%
                                            for (String scopeID : openIdScopes) {
                                        %>
                                        <li><%=Encode.forHtml(scopeID)%>
                                        </li>
                                        <%
                                            }
                                        %>
                                    </ul>
                                </div>
                                <%
                                            }
                                        }
                                %>
                                <div class="border-gray margin-bottom-double">
                                <div class="padding">
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="scope-approval" id="approveCb" value="approve">
                                            Approve Once
                                        </label>
                                    </div>
                                    <div class="radio">
                                        <label>
                                            <input type="radio" name="scope-approval" id="approveAlwaysCb" value="approveAlways">
                                            Approve Always
                                        </label>
                                    </div>
                                </div>
                                </div>
                            </div>
                            <%
                            %>
                            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-12">
                                <table width="100%" class="styledLeft margin-top-double">
                                    <tbody>
                                    <tr>
                                        <td class="buttonRow" colspan="2">
                                            <input type="hidden" name="<%=Constants.SESSION_DATA_KEY_CONSENT%>"
                                                   value="<%=Encode.forHtmlAttribute(request.getParameter(Constants.SESSION_DATA_KEY_CONSENT))%>"/>
                                            <input type="hidden" name="consent" id="consent" value="deny"/>
                                            <div style="text-align:left;">
                                                <input type="button" class="btn  btn-primary" id="approve" name="approve"
                                                       onclick="approved(); return false;"
                                                       value="<%=AuthenticationEndpointUtil.i18n(resourceBundle,"continue")%>"/>
                                                <input class="btn" type="reset"
                                                       onclick="deny(); return false;"
                                                       value="<%=AuthenticationEndpointUtil.i18n(resourceBundle,"deny")%>"/>
                                            </div>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </div>
                        </form>
                        <div class="clearfix"></div>
                    </div>
                </div>
            </div>
        
        
        </div>
        <!-- /content -->
    
    </div>
</div>

<div id="modal_scope_validation" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel">
    <div class="modal-dialog modal-md" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
                <h4 class="modal-title"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "requested.scopes")%></h4>
            </div>
            <div class="modal-body">
                <%=AuthenticationEndpointUtil.i18n(resourceBundle, "please.select.approve.always")%>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal"><%=AuthenticationEndpointUtil.i18n(resourceBundle, "ok")%></button>
            </div>
        </div>
    </div>
</div>

<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>

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
</body>
</html>
