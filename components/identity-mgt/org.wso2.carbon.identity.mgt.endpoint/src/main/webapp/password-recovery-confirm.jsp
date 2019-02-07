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
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.base.MultitenantConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointConstants" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.ApiException" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.api.NotificationApi" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.CodeValidationRequest" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Error" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.client.model.Property" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<jsp:directive.include file="localize.jsp"/>

<%
    String confirmationKey = request.getParameter("confirmation");
    String callback = request.getParameter("callback");
    if (StringUtils.isBlank(callback)) {
        callback = request.getParameter("redirect_uri");
    }
    String tenantDomain = request.getParameter(IdentityManagementEndpointConstants.TENANT_DOMAIN);
    NotificationApi notificationApi = new NotificationApi();
    try {
        List<Property> properties = new ArrayList<>();
        Property tenantDomainProperty = new Property();
        tenantDomainProperty.setKey(MultitenantConstants.TENANT_DOMAIN);
        tenantDomainProperty.setValue(tenantDomain);
        properties.add(tenantDomainProperty);
        
        CodeValidationRequest validationRequest = new CodeValidationRequest();
        validationRequest.setCode(confirmationKey);
        validationRequest.setProperties(properties);
        notificationApi.validateCodePostCall(validationRequest);
        
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
    
    if (StringUtils.isBlank(tenantDomain)) {
        tenantDomain = IdentityManagementEndpointConstants.SUPER_TENANT;
    }
    if (StringUtils.isBlank(callback)) {
        callback = IdentityManagementEndpointUtil.getUserPortalUrl(
                application.getInitParameter(IdentityManagementEndpointConstants.ConfigConstants.USER_PORTAL_URL));
    }
    
    if (StringUtils.isNotBlank(confirmationKey)) {
        request.getSession().setAttribute("confirmationKey", confirmationKey);
        request.setAttribute("callback", callback);
        request.setAttribute(IdentityManagementEndpointConstants.TENANT_DOMAIN, tenantDomain);
        request.getRequestDispatcher("passwordreset.do").forward(request, response);
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                IdentityManagementEndpointUtil.i18n(recoveryResourceBundle, "Cannot.process.email.confirmation.code.is.missing"));
        request.setAttribute("errorCode", "18001");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>

