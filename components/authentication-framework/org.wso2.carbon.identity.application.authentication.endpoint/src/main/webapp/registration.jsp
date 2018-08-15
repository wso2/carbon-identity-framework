<% /**
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/ %>
<%@ page
        import="org.wso2.carbon.identity.application.authentication.endpoint.util.UserRegistrationAdminServiceClient" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.user.registration.stub.dto.UserFieldDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="java.net.URLEncoder" %>
<%@include file="localize.jsp" %>

<%
    String forwardTo;
    String username = request.getParameter("reg_username");

    try {
        UserRegistrationAdminServiceClient registrationClient = new UserRegistrationAdminServiceClient();
        boolean isExistingUser = registrationClient.isUserExist(username);

        if (StringUtils.equals(request.getParameter("is_validation"), "true")) {
            if (isExistingUser) {
                out.write("User Exist");
            } else {
                out.write("Ok");
            }
            return;
        }

        if (isExistingUser) {
            throw new Exception("User exist");
        }
        List<UserFieldDTO> fields = (List<UserFieldDTO>) session.getAttribute("fields");

        for(UserFieldDTO userFieldDTO : fields) {
            userFieldDTO.setFieldValue(request.getParameter(userFieldDTO.getFieldName()));
        }
        char [] password = request.getParameter("reg_password").toCharArray();
        registrationClient.addUser(username, password, fields);
        forwardTo = "../dashboard/index.jag";
    } catch (Exception e) {
        String error = URLEncoder.encode(AuthenticationEndpointUtil.i18n(resourceBundle, "internal.error.occurred"),"utf-8");
        response.sendRedirect("create-account.jsp?sessionDataKey=" + request.getParameter("sessionDataKey") +
                "&failedPrevious=true&errorCode=" + error);
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
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "information")%>
                    </h4>
                </div>
                <div class="modal-body">
                    <p><%=AuthenticationEndpointUtil.i18n(resourceBundle, "user.details.submitted")%></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">
                        <%=AuthenticationEndpointUtil.i18n(resourceBundle, "close")%>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="libs/jquery_1.11.3/jquery-1.11.3.js"></script>
<script src="libs/bootstrap_3.3.5/js/bootstrap.min.js"></script>
<script type="application/javascript" >
    $(document).ready(function () {
        var infoModel = $("#infoModel");
        infoModel.modal("show");
        infoModel.on('hidden.bs.modal', function() {
            location.href = "<%= Encode.forJavaScriptBlock(forwardTo) %>";
        })
    });
</script>
</body>
</html>
