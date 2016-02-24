<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="java.util.List" %>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    List<IdentityProvider> identityProvidersList = (List<IdentityProvider>)session.getAttribute("identityProviderList");
    String identityProvider = "identityProvider";
    session.removeAttribute(identityProvider);
    if (identityProvidersList == null) {
%>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp?callback=idp-mgt-list.jsp";
        </script>
<%
    }
%>

<script>

    function editIdPName(obj){
        location.href = "idp-mgt-edit-load.jsp?idPName=" + encodeURIComponent(jQuery(obj).parent().prev().prev().text());
    }
    function deleteIdPName(obj){
        CARBON.showConfirmationDialog('Are you sure you want to delete "'  + jQuery(obj).parent().prev().prev().text() + '" IdP information?',
                function (){
                    location.href = "idp-mgt-delete-finish.jsp?idPName=" + encodeURIComponent(jQuery(obj).parent().prev().prev().text());
                },
                null);
    }
    

    function enable(idpName) {
        location.href = "idp-mgt-edit-finish.jsp?idPName="+idpName+"&enable=1";
    }

    function disable(idpName) {
        location.href = "idp-mgt-edit-finish.jsp?idPName="+idpName+"&enable=0";
    }


</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='identity.providers'/>
        </h2>
        <div id="workArea">
            <div style="height:30px;">
                <a href="idp-mgt-edit-load-local.jsp" class="icon-link" style="background-image:url(images/resident-idp.png);"><fmt:message key='resident.idp'/></a>
            </div>
            <div class="sectionSub">
            <table class="styledLeft" id="idPsListTable">
                <thead><tr><th class="leftCol-med"><fmt:message key='registered.idps'/></th><th class="leftCol-big"><fmt:message key='description'/></th><th style="width: 30% ;" ><fmt:message key='actions'/></th></tr></thead>
                <tbody>
                    <% if(identityProvidersList != null && identityProvidersList.size() > 0){ %>
                        <% for(int i = 0; i < identityProvidersList.size(); i++){
                         String description = identityProvidersList.get(i).getIdentityProviderDescription();
                         boolean enable = identityProvidersList.get(i).getEnable();
                         if(description == null){
                            description = "";
                         }
                         %>
                            <tr>
                                <td><%=Encode.forHtmlContent(identityProvidersList.get(i).getIdentityProviderName())%></td>
                                <td><%=Encode.forHtmlContent(description)%></td>
                                <td>
                                 	<% if (enable) { %>
                    						<a title="<fmt:message key='disable.policy'/>"
                       						onclick="disable('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>');return false;"
                       						href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                        					<fmt:message key='disable.policy'/></a>
                    				<% } else { %>
                    						<a title="<fmt:message key='enable.policy'/>"
                       						onclick="enable('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>');return false;"
                       						href="#" style="background-image: url(images/enable2.gif);" class="icon-link">
                       						 <fmt:message key='enable.policy'/></a>
                   				   <% } %>
                                    <a title="<fmt:message key='edit.idp.info'/>"
                                       onclick="editIdPName(this);return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/edit.gif)">
                                       <fmt:message key='edit'/>
                                    </a>
                                    <a title="<fmt:message key='delete'/>"
                                       onclick="deleteIdPName(this);return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/delete.gif)">
                                       <fmt:message key='delete'/>
                                    </a>
                                </td>
                            </tr>
                        <% } %>
                    <% } else { %>
                        <tr>
                            <td colspan="3"><i><fmt:message key='no.idp'/></i></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
            </div>
        </div>
    </div>

</fmt:bundle>
