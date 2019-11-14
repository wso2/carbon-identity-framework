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
<%@ page import="org.apache.commons.collections.CollectionUtils"%>
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil"%>
<%@ page import="org.apache.commons.lang.StringUtils"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"%>

<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>" />
<jsp:include page="../dialog/display_messages.jsp"/>

<script type="text/javascript" src="../admin/js/main.js"></script>

<%
    String DEFAULT_FILTER = "*";
    List<IdentityProvider> identityProvidersList = (List<IdentityProvider>)session.getAttribute(IdPManagementUIUtil.IDP_LIST);
    String identityProvider = "identityProvider";
    session.removeAttribute(identityProvider);
    String filter = (String) session.getAttribute(IdPManagementUIUtil.IDP_FILTER);
    String searchFilter = request.getParameter(IdPManagementUIUtil.FILTER_STRING);
    if (searchFilter != null) {
        searchFilter = Encode.forHtml(searchFilter);
%>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp?callback=idp-mgt-list.jsp&<%=IdPManagementUIUtil.FILTER_STRING%>=<%=searchFilter%>";
        </script>
<%
    }
    else if (filter == null) {
%>
        <script type="text/javascript">
            location.href = "idp-mgt-list-load.jsp?callback=idp-mgt-list.jsp";
        </script>
<%
    }
%>

<script>

    function editIdPName(idpName){
        location.href = "idp-mgt-edit-load.jsp?idPName=" + encodeURIComponent(idpName);
    }
    function deleteIdPName(idpName){
        function doDelete() {
            $.ajax({
                type: 'POST',
                url: 'idp-mgt-delete-finish-ajaxprocessor.jsp',
                headers: {
                    Accept: "text/html"
                },
                data: 'idPName=' + encodeURIComponent(idpName),
                async: false,
                success: function (responseText, status) {
                    if (status == "success") {
                        location.assign("idp-mgt-list-load.jsp");
                    }
                }
            });
        }

        CARBON.showConfirmationDialog('Are you sure you want to delete "'  +
                idpName + '" IdP information?', doDelete, null);
    }
    

    function enableOrDisableIdP(idpName, indicator) {
        $.ajax({
            type: 'POST',
            url: 'idp-mgt-edit-finish-ajaxprocessor.jsp',
            headers: {
                Accept: "text/html"
            },
            data: 'idPName=' + encodeURIComponent(idpName) + '&enable=' + indicator,
            async: false,
            success: function (responseText, status) {
                if (status == "success") {
                    location.assign("idp-mgt-list-load.jsp");
                }
            }
        });
    }
</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">
    <div id="middle">
        <h2>
            <fmt:message key='identity.providers'/>
        </h2>
        <div id="workArea">
            <%--<div style="height:30px;">--%>
                <%--<a href="idp-mgt-edit-load-local.jsp" class="icon-link" style="background-image:url(images/resident-idp.png);"><fmt:message key='resident.idp'/></a>--%>
            <%--</div>--%>
            <form name="filterForm" method="post" action="idp-mgt-list.jsp">
              <table style="width: 100%" class="styledLeft">
                <tbody>
                  <tr>
                    <td style="border: none !important">
                      <table class="styledLeft" width="100%" id="IDProviders">
                        <thead>
                          <tr>
                            <th colspan="2"><fmt:message key="search.idp" /></th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td class="leftCol-big" style="padding-right: 0 !important;"><fmt:message
										key="idp.search.pattern" /></td>
                            <td><input type="text" name="<%=IdPManagementUIUtil.FILTER_STRING%>"
									value="<%=StringUtils.isBlank(filter) ? DEFAULT_FILTER : Encode.forHtmlAttribute(filter)%>"
									black-list-patterns="xml-meta-exists" /> <input class="button"
									type="submit" value="Search" /></td>
                          </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                </tbody>
              </table>
          </form>
          <div class="sectionSub">
            <table class="styledLeft" id="idPsListTable">
                <thead><tr><th class="leftCol-med"><fmt:message key='registered.idps'/></th><th class="leftCol-big"><fmt:message key='description'/></th><th style="width: 30% ;" ><fmt:message key='actions'/></th></tr></thead>
                <tbody>
                    <% if (identityProvidersList != null && identityProvidersList.size() > 0) { %>
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
                       						onclick="enableOrDisableIdP('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>', 0);return false;"
                       						href="#" style="background-image: url(images/disable.gif);" class="icon-link">
                        					<fmt:message key='disable.policy'/></a>
                    				<% } else { %>
                    						<a title="<fmt:message key='enable.policy'/>"
                       						onclick="enableOrDisableIdP('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>', 1);return false;"
                       						href="#" style="background-image: url(images/enable2.gif);" class="icon-link">
                       						 <fmt:message key='enable.policy'/></a>
                   				   <% } %>
                                    <a title="<fmt:message key='edit.idp.info'/>"
                                       onclick="editIdPName('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>');return false;"
                                       href="#"
                                       class="icon-link"
                                       style="background-image: url(images/edit.gif)">
                                       <fmt:message key='edit'/>
                                    </a>
                                    <a title="<fmt:message key='delete'/>"
                                       onclick="deleteIdPName('<%=Encode.forJavaScriptAttribute(identityProvidersList.get(i).getIdentityProviderName())%>');return false;"
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
