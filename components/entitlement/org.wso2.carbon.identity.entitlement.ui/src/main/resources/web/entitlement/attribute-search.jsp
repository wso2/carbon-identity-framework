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
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.wso2.carbon.CarbonConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.AttributeDTO"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants"%>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.utils.ServerConstants"%>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String policyId = "";
    String attributeType = "";
    String attributeId = "";
    String attributeDataType = "";
    String [] results = null;
    String[] policyIds = null;
    String[] attributeTypes = new String[] {EntitlementPolicyConstants.RESOURCE_ELEMENT,
                                            EntitlementPolicyConstants.SUBJECT_ELEMENT,
                                            EntitlementPolicyConstants.ACTION_ELEMENT,
                                            EntitlementPolicyConstants.ENVIRONMENT_ELEMENT};
    String forwardTo;

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    policyId = (String)request.getParameter("policyId");
    attributeType = (String)request.getParameter("attributeType");
    attributeId = (String)request.getParameter("attributeId");
    attributeDataType = (String)request.getParameter("attributeDataType");
    
    List<AttributeDTO> attributeValueDTOs = new ArrayList<AttributeDTO>();

    if(policyId != null && !"".equals(policyId)){
        AttributeDTO attributeValueDTO = new AttributeDTO();
        attributeValueDTO.setPolicyId(policyId);
        if(!EntitlementPolicyConstants.COMBO_BOX_ANY_VALUE.equals(attributeType)){
            attributeValueDTO.setAttributeType(attributeType);            
        }
        attributeValueDTO.setAttributeDataType(attributeDataType);
        attributeValueDTO.setAttributeId(attributeId);
        attributeValueDTOs.add(attributeValueDTO);
    }

    try {
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                    serverURL, configContext);
        policyIds = client.getAllPolicyIds();
        if(attributeValueDTOs.size() > 0){
            results = client.getAdvanceSearchResult(attributeValueDTOs.toArray(new AttributeDTO[attributeValueDTOs.size()]));
        }

    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.loading.policy.resource");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        forwardTo = "../admin/error.jsp";
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }
</script>

<script type="text/javascript">
    forward();
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
		label="create.evaluation.request"
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

    function submitForm(){
        document.requestForm.action = "attribute-search.jsp";
        document.requestForm.submit();
    }

    function doCancel(){
        location.href = 'index.jsp?';
    }
    
</script>

<div id="middle">
    <h2><fmt:message key="attribute.search"/></h2>
    <div id="workArea">
        <form id="requestForm" name="requestForm" method="post" action="advance-search.jsp">
        <table class="styledLeft noBorders">
        <tr>
            <td><fmt:message key='policy.id'/></td>
            <td colspan="2">
                <select id="policyId" name="policyId">
                    <option value="<%=EntitlementPolicyConstants.COMBO_BOX_ANY_VALUE%>" selected="selected">
                        <%=EntitlementPolicyConstants.COMBO_BOX_ANY_VALUE%>
                    </option>
                    <%
                        if(policyIds != null){
                            for (String id : policyIds) {
                                if (id != null && id.equals(policyId)) {
                    %>
                    <option value="<%=Encode.forHtmlAttribute(policyId)%>"
                            selected="selected"><%=Encode.forHtmlContent(policyId)%>
                    </option>
                    <%
                        } else {
                    %>
                    <option value="<%=id%>"><%=id%></option>
                    <%
                                }
                            }
                        }
                    %>
                </select>
            </td>
        </tr>

        <tr>
            <td><fmt:message key="attribute.type"/></td>
            <td colspan="2">
                <select id="attributeType" name="attributeType">
                    <option value="<%=EntitlementPolicyConstants.COMBO_BOX_ANY_VALUE%>" selected="selected">
                        <%=EntitlementPolicyConstants.COMBO_BOX_ANY_VALUE%>
                    </option>
                    <%
                        for (String type : attributeTypes) {
                            if (type != null && type.equals(attributeType)) {
                    %>
                    <option value="<%=attributeType%>"
                            selected="selected"><%=attributeType%>
                    </option>
                    <%
                        } else {
                    %>
                    <option value="<%=type%>"><%=type%></option>
                    <%
                            }
                        }
                    %>
                </select>
            </td>
        </tr>

        <tr>
            <td><fmt:message key='attribute.id'/></td>
            <td colspan="2">
            <%
                if (attributeId != null && !attributeId.equals("")) {
            %>
            <input type="text" name="attributeId" id="attributeId"
                       value="<%=attributeId%>" class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="attributeId" id="attributeId" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>

        <tr>
            <td><fmt:message key='attribute.dataType'/></td>
            <td colspan="2">
            <%
                if (attributeDataType != null && !attributeDataType.equals("")) {
            %>
            <input type="text" name="attributeDataType" id="attributeDataType" value="<%=attributeDataType%>"
                       class="text-box-big"/>
            <%
                } else {
            %>
            <input type="text" name="attributeDataType" id="attributeDataType" class="text-box-big"/>
            <%
                }
            %>
            </td>
        </tr>

        <tr>
            <td>
                <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                   onclick="submitForm(); return false;"><fmt:message key="search"/></a>
            </td>
        </tr>
        </table>
        </form>
        <h3><fmt:message key="search.results"/></h3>
        <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <thead>
                <tr>
                    <th><fmt:message key='attribute.value'/></th>
                    <th><fmt:message key='attribute.id'/></th>
                    <th><fmt:message key='attribute.dataType'/></th>
                </tr>
            </thead>
            <tbody>
            <%
                if(results != null && results.length > 0) {
                    for(String result : results){
            %>
                    <tr>
            <%
                        String[] resultData = result.split(",");
                        for(String data : resultData){
            %>
                            <td><%=data%></td>
            <%
                        }
            %>
                    </tr>
            <%
                    }
                } else {
            %>
                <tr>
                    <td colspan="3"><fmt:message key="no.result.found"/></td>
                </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
</div>
</fmt:bundle>
