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

<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.apache.commons.collections.map.HashedMap" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementServiceUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.NotificationApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Property" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.RecoveryInitiatingRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.User" %>
<%@ page import="java.io.UnsupportedEncodingException" %>
<%@ page import="java.net.URISyntaxException" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<jsp:directive.include file="localize.jsp"/>

<%


    String username = IdentityManagementEndpointUtil.getStringValue(request.getAttribute("username"));

    User user = IdentityManagementServiceUtil.getInstance().getUser(username);

    NotificationApi notificationApi = new NotificationApi();

    RecoveryInitiatingRequest recoveryInitiatingRequest = new RecoveryInitiatingRequest();
    recoveryInitiatingRequest.setUser(user);
    String callback = (String) request.getAttribute("callback");
    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }
    List<Property> properties = new ArrayList<Property>();
    Property property = new Property();
    property.setKey("callback");
    property.setValue(URLEncoder.encode(callback, "UTF-8"));
    properties.add(property);
    recoveryInitiatingRequest.setProperties(properties);

    try {
        Map<String, String> requestHeaders = new HashedMap();
        if (request.getParameter("g-recaptcha-response") != null) {
            requestHeaders.put("g-recaptcha-response", request.getParameter("g-recaptcha-response"));
        }
        notificationApi.recoverPasswordPost(recoveryInitiatingRequest, null, null, requestHeaders);
    } catch (ApiException e) {
        Error error = new Gson().fromJson(e.getMessage(), Error.class);
        request.setAttribute("error", true);
        if (error != null) {
            request.setAttribute("errorMsg", error.getDescription());
            request.setAttribute("errorCode", error.getCode());
        }

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
                    <h4 class="modal-title">
                        <%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Information")%>
                    </h4>
                </div>
                <div class="modal-body">
                    <p><%=IdentityManagementEndpointUtil.i18n(recoveryResourceBundle,
                            "Password.recovery.information.sent.to.the.email.registered.with.account")%>
                        &nbsp; <%=Encode.forHtml(username)%>
                    </p>
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
