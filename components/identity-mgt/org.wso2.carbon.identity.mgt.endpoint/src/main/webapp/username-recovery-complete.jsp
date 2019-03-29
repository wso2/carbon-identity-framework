<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~    http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.CallBackValidator" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.IdentityRecoveryException" %>
<%@ page import="java.net.URISyntaxException" %>
<jsp:directive.include file="localize.jsp"/>

<%
    String callback = (String) request.getAttribute("callback");
    String tenantDomain = (String) request.getAttribute("tenantDomain");
    boolean isUserPortalURL = (boolean) request.getAttribute("isUserPortalURL");

    CallBackValidator callBackValidator = new CallBackValidator();
    try {
        if (!callBackValidator.isValidCallbackURL(callback, tenantDomain, isUserPortalURL)) {
            request.setAttribute("error", true);
            request.setAttribute("errorMsg", "Configured callback URL does not match with the provided callback " +
                    "URL in the request.");
            request.getRequestDispatcher("error.jsp").forward(request, response);
            return;
        }
    } catch (IdentityRecoveryException e) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg", "Callback URL validation failed. " + e);
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>

<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <link href="libs/bootstrap_3.3.5/css/bootstrap.min.css" rel="stylesheet">
    <link href="css/Roboto.css" rel="stylesheet">
    <link href="css/custom-common.css" rel="stylesheet">
</head>
<body>
<div class="container">
    <div id="infoModel" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&times;</button>
                    <h4 class="modal-title"><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                            "Information")%></h4>
                </div>
                <div class="modal-body">
                    <p><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                            "Username.recovery.information.sent.to.your.email")%></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Close")%>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<script type="application/javascript">
    $(document).ready(function () {
        var infoModel = $("#infoModel");
        infoModel.modal("show");
        infoModel.on('hidden.bs.modal', function () {
            <%
            try {
            %>
                location.href = "<%= IdentityManagementEndpointUtil.getURLEncodedCallback(callback)%>";
            <%
            } catch (URISyntaxException e) {
                request.setAttribute("error", true);
                request.setAttribute("errorMsg", "Invalid callback URL found in the request.");
                request.getRequestDispatcher("error.jsp").forward(request, response);
                return;
            }
            %>
        })
    });
</script>
</body>
</html>
