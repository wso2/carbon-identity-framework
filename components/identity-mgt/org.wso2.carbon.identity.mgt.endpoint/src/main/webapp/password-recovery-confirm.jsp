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

<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.wso2.carbon.identity.mgt.endpoint.IdentityManagementEndpointUtil" %>

<%
    String username = request.getParameter("username");
    String userStoreDomain = request.getParameter("userstoredomain");
    String tenantDomain = request.getParameter("tenantdomain");
    String confirmationKey = request.getParameter("confirmation");

    String fulQualifiedUsername = IdentityManagementEndpointUtil.getFullQualifiedUsername(username, tenantDomain,
                                                                                          userStoreDomain);
    if (StringUtils.isNotBlank(fulQualifiedUsername) && StringUtils.isNotBlank(confirmationKey)) {
        request.setAttribute("username", fulQualifiedUsername);
        request.getSession().setAttribute("confirmationKey", confirmationKey);
        request.getRequestDispatcher("passwordreset.do").forward(request, response);
    } else {
        request.setAttribute("error", true);
        request.setAttribute("errorMsg",
                             "Cannot process the email notification confirmation. Either the username or confirmation code is missing.");
        request.getRequestDispatcher("error.jsp").forward(request, response);
        return;
    }
%>
