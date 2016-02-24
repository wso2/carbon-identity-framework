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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PIPFinderDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PolicyFinderDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>

<%

    PIPFinderDataHolder pipFinderDataHolder = null;
    PolicyFinderDataHolder policyFinderDataHolder = null;
    String policyString = "";
    String attributeString = "";
    String forwardTo = null;

    String  finderId = request.getParameter("finderId");
    String type = request.getParameter("type");

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    try {
        EntitlementAdminServiceClient client = new EntitlementAdminServiceClient(cookie,
                serverURL, configContext);
        if(finderId != null){
            if("attribute".equals(type)) {
                pipFinderDataHolder = client.getPIPAttributeFinderData(finderId);
            } else if("resource".equals(type)){
                pipFinderDataHolder = client.getPIPResourceFinderData(finderId);
            } else if("policy".equals(type)){
                policyFinderDataHolder = client.getPolicyFinderData(finderId);
                String[] policies = policyFinderDataHolder.getPolicyIdentifiers();
                if(policies != null){
                    for(String policy : policies){
                        if(policy == null){
                            continue;
                        }
                        if("".equals(policyString)){
                            policyString = policy;
                        } else {
                            policyString = policyString + " , " + policy;
                        }
                    }
                }
            }

            if(pipFinderDataHolder != null){
                String[] attributeIds = pipFinderDataHolder.getSupportedAttributeIds();
                if(attributeIds != null){
                    for(String attribute : attributeIds){
                        if(attribute == null){
                            continue;
                        }
                        if("".equals(attributeString)){
                            attributeString = attribute;
                        } else {
                            attributeString = attributeString + " , " + attribute;
                        }
                    }
                }
            }
        }

    } catch (Exception e) {
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(e.getMessage()))%>', function () {
        location.href = "index.jsp";
    });
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="view.finder"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="false"
		request="<%=request%>" />

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="resources/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function doCancel(){
        location.href = 'pdp-manage.jsp';
    }

</script>

<div id="middle">
    <h2><fmt:message key="view.finder"/></h2>
    <div id="workArea">
            <div class="sectionSeperator">
                 <%
                    if(policyFinderDataHolder != null){
                %>
                    <%=Encode.forHtml(policyFinderDataHolder.getModuleName())%>
                <%
                    }
                %>
                 <%
                    if(pipFinderDataHolder != null){
                %>
                    <%=Encode.forHtml(pipFinderDataHolder.getModuleName())%>
                <%
                    }
                %>
            </div>
            <div class="sectionSub">
            <%
                if(policyFinderDataHolder != null){
            %>

                    <table  class="styledLeft"  style="width: 100%">
                        <tr>
                            <td>Name</td><td><%=Encode.forHtml(policyFinderDataHolder.getModuleName())%></td>
                        </tr>
                        <tr>
                            <td>Class Name</td><td><%=Encode.forHtml(policyFinderDataHolder.getClassName())%></td>
                        </tr>
                        <tr>
                            <td>Policy Ids </td>
                            <td><%=Encode.forHtml(policyString)%></td>
                        </tr>
                    </table>

            <%
                }
            %>

            <%
                if(pipFinderDataHolder != null){
            %>

                    <table  class="styledLeft"  style="width: 100%">
                        <tr>
                            <td>Name</td><td><%=Encode.forHtml(pipFinderDataHolder.getModuleName())%></td>
                        </tr>
                        <tr>
                            <td>Class Name</td><td><%=Encode.forHtml(pipFinderDataHolder.getClassName())%></td>
                        </tr>
                        <tr>
                            <td>Support Attribute Ids </td>
                            <td><%=Encode.forHtml(attributeString)%></td>
                        </tr>
                    </table>

            <%
                }
            %>
        </div>
        <div class="buttonRow">
            <a onclick="doCancel()" class="icon-link" style="background-image:none;">
                <fmt:message key="back.to.pdp.config"/></a><div style="clear:both"></div>
        </div>
    </div>
</div>
</fmt:bundle>
