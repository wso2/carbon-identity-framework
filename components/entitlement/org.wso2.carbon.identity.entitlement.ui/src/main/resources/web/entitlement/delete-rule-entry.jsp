<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~    http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied.  See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<jsp:useBean id="entitlementPolicyBean" type="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean"
             class="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyBean" scope="session"/>
<jsp:setProperty name="entitlementPolicyBean" property="*" />
<%
    String forwardTo = null;
    String ruleId = request.getParameter("ruleId");
    String returnPage = request.getParameter("returnPage");
    if(returnPage != null && returnPage.trim().length() > 0){
        if(ruleId != null && ruleId.trim().length() > 0){
            entitlementPolicyBean.removeBasicRuleElement(ruleId);
        }
        forwardTo = "basic-policy-editor.jsp";
    } else {
        if(ruleId != null && ruleId.trim().length() > 0){
            entitlementPolicyBean.removeRuleDTO(ruleId);
        }
        forwardTo = "policy-editor.jsp";
    }
%>

<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>