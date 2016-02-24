<!--
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
<%@ page import="org.wso2.carbon.identity.mgt.stub.beans.VerificationBean" %>
<%@ page import="org.wso2.carbon.identity.mgt.ui.IdentityManagementClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    String forward = "index.jsp";
    boolean  isAccountUnlocked = false;
    try {
        String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(),
                session);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        IdentityManagementClient client =
                new IdentityManagementClient(backendServerURL, configContext);
        VerificationBean bean = (VerificationBean) session.getAttribute("confirmationBean");
        isAccountUnlocked = client.unlockUserAccount(bean.getUserId(), bean.getKey());
    } catch (Exception e) {
        //ignore
    }

    if (isAccountUnlocked) {
        forward = "success_unlock.jsp";
    } else {
        forward = "fail_unlock.jsp";
    }
%>

    <script type="text/javascript">
        function forward(){
            location.href = "<%=forward%>";
        }
    </script>

    <script type="text/javascript">
        forward();
    </script>
