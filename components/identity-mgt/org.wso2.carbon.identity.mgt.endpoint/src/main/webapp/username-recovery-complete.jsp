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

<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserIdentityClaimDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInfoRecoveryWithNotificationClient" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.UserInformationRecoveryClient" %>
<%@ page import="java.util.List" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.serviceclient.beans.Claim" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="javax.ws.rs.core.Response" %>

<%
    UserInformationRecoveryClient userInformationRecoveryClient = new UserInformationRecoveryClient();
    UserInfoRecoveryWithNotificationClient userInfoRecoveryWithNotificationClient = new UserInfoRecoveryWithNotificationClient();
    UserIdentityClaimDTO[] claimDTOs = userInformationRecoveryClient.getUserIdentitySupportedClaims(
            IdentityManagementEndpointConstants.WSO2_DIALECT);

    List<Claim> claimList = new ArrayList<Claim>();

    for (UserIdentityClaimDTO claimDTO : claimDTOs) {
        if (claimDTO.getRequired()) {
            Claim claim = new Claim();
            claim.setClaimURI(claimDTO.getClaimUri());
            claim.setClaimValue(request.getParameter(claimDTO.getClaimUri()));
            claimList.add(claim);
        } else if (StringUtils.equals(claimDTO.getClaimUri(),
                IdentityManagementEndpointConstants.ClaimURIs.FIRST_NAME_CLAIM) ||
                StringUtils.equals(claimDTO.getClaimUri(),
                        IdentityManagementEndpointConstants.ClaimURIs.LAST_NAME_CLAIM) ||
                StringUtils.equals(claimDTO.getClaimUri(),
                        IdentityManagementEndpointConstants.ClaimURIs.EMAIL_CLAIM)) {
            Claim claim = new Claim();
            claim.setClaimURI(claimDTO.getClaimUri());
            claim.setClaimValue(request.getParameter(claimDTO.getClaimUri()));
            claimList.add(claim);
        }
    }
    Claim[] claimArray = new Claim[claimList.size()];
    Response usernameRecoveryResponse = userInfoRecoveryWithNotificationClient.sendUserNameRecoveryNotification(claimList.toArray(claimArray));

    if ((usernameRecoveryResponse == null) || (StringUtils.isBlank(Integer.toString(usernameRecoveryResponse.getStatus()))) ||
            !(Response.Status.OK.equals(usernameRecoveryResponse.getStatus()))) {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                IdentityManagementEndpointUtil.getPrintableError("Failed to send email notification for username recovery.",
                        "Cannot verify the user with given username or confirmation key.",
                        usernameRecoveryResponse.getStatusInfo()));
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>

<html>
<head>
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
                    <h4 class="modal-title">Information</h4>
                </div>
                <div class="modal-body">
                    <p>Username recovery information has been sent to your email.</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
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
            location.href = "<%=Encode.forJavaScript(IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL)))%>";
        })
    });
</script>
</body>
</html>
