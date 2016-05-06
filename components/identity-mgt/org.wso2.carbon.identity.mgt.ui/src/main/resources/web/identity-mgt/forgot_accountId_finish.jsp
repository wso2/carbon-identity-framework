<!--
  ~
  ~ Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
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
  ~
  -->

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.UserMgtBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.stub.dto.UserEvidenceDTO" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%
        
    boolean success = false;

    String email = request.getParameter("emailAddress");
    String lastName = request.getParameter("lastName");


    UserEvidenceDTO dto1 = new UserEvidenceDTO();
    UserEvidenceDTO dto2 = new UserEvidenceDTO();
    dto1.setClaimUri("http://wso2.org/claims/emailaddress");
    dto1.setClaimValue(email);
    dto2.setClaimUri("http://wso2.org/claims/lastname");
    dto2.setClaimValue(lastName);
    bean.setUserEvidenceDTOs(new UserEvidenceDTO[]{dto1, dto2});

    try{        
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityManagementClient client =
                new IdentityManagementClient(cookie, backendServerURL, configContext);
        success = client.processAccountRecovery(bean);

    } catch (Exception ignore) {

    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.mgt.ui.i18n.Resources">

    <link href="css/forgot-password.css" rel="stylesheet" type="text/css" media="all"/>


<%
    if(success){
%>

    <div id="middle">
        <h2><fmt:message key="account.id.found"/></h2>
    </div>
    <div>
        <p><fmt:message key="account.id.found.message"/></p>
    </div>
<%
    } else {
%>

    <div id="middle">
        <h2><fmt:message key="account.id.found"/></h2>
    </div>
    <div>
        <p><fmt:message key="account.id.not.found.message"/></p>
        <p>
        <fmt:message key="try.again"/><a href="forgot_root.jsp"><fmt:message key="try.again.here"/></a> .
        </p>
    </div>
<%
    }
%>
</fmt:bundle>

