<%--
  ~ Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@page import="org.wso2.carbon.directory.common.stub.types.ServerPrinciple" %>
<%@page import="org.wso2.carbon.directory.server.manager.ui.DirectoryServerManagerClient" %>

<%@page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.owasp.encoder.Encode" %>

<script type="text/javascript" src="../admin/js/main.js"></script>

<jsp:include page="../dialog/display_messages.jsp"/>

<fmt:bundle basename="org.wso2.carbon.directory.server.manager.ui.i18n.Resources">

<carbon:breadcrumb label="service.principles"
		resourceBundle="org.wso2.carbon.directory.server.manager.ui.i18n.Resources"
		topPage="true" request="<%=request%>" />


    <script type="text/javascript">

        function addServicePricniple() {
            location.href = 'add-step1.jsp';
        }

        function deleteServicePrinciple(spn) {

            function doDelete(){
                var spnName = spn;
                location.href = 'delete-finish.jsp?spnName=' + spnName;
            }
            CARBON.showConfirmationDialog("<fmt:message key="confirm.delete.spn"/> \'"+ spn + "\'?", doDelete, null);
        }

        function changeSpnPassword(spn) {
            location.href = 'change-passwd.jsp?spnName=' + spn;
        }

    </script>
    
    
     <%
        DirectoryServerManagerClient serverManager = null;
        try{
            
            serverManager = (DirectoryServerManagerClient)session.getAttribute(DirectoryServerManagerClient.
                    SERVER_MANAGER_CLIENT);

            if(serverManager == null){
                String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
                String backEndServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
                ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().
                            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

                serverManager = new DirectoryServerManagerClient(cookie, backEndServerURL, configContext);
                session.setAttribute(DirectoryServerManagerClient.SERVER_MANAGER_CLIENT, serverManager);
            }
        }catch(Exception e){
            CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
   %>
            <jsp:include page="../admin/error.jsp"/>
   <%
            return;
        }
   %>
    


    <div id="middle">
        <h2><fmt:message key="service.principle.management"/></h2>

        <div id="workArea">


            <% if (CarbonUIUtil.isUserAuthorized(request, "/permission/admin/configure/security")) {%>
            <table width="100%">
                <tr>
                    <%
                        if (serverManager.isKDCEnabled()) {
                    %>
                    <td>
                        <a href="#" onclick="addServicePricniple()" class="icon-link" style="background-image:url(images/add.gif);"><fmt:message key="add"/></a>
                    </td>
                    <%
                        } else {
                    %>
                    <td>
                        <a href="#" onclick="return;" class="icon-link" style="background-image:url(images/add.gif);"><font color="grey"><fmt:message key="add"/></font></a>
                    </td>
                    <%
                        }
                    %>
                </tr>
                </table>


                        <table class="styledLeft" id="internal" width="100%">
			                <thead>
                            <tr>
                               <th>
                                  &nbsp;<fmt:message key="service.principle.name"/>
                               </th>
                               <th>
                                  &nbsp;<fmt:message key="service.principle.description"/>
                               </th>
                               <th>
                                  &nbsp;<fmt:message key="edit.principle"/>
                               </th>
                            </tr>
                            </thead>
                            <tbody>
                            <%
                                try {
                                    if (serverManager.isKDCEnabled()) {
                                        ServerPrinciple[] servicePrinciples =
                                                serverManager.listServicePrinciples("*");
                                        int i = 0;
                                        if(servicePrinciples.length == 0){
                                        %>
                            <tr>
                                <td colspan="3" width="100%"><i><fmt:message key="no.service.providers.registered"/></i></td>
                            </tr>

                            <%
                                        }else {
                                        for (ServerPrinciple principle :
                                                servicePrinciples) {
                            %>

                            <tr>
                                <td><%=Encode.forHtml(principle.getServerName())%></td>
                                <td><%=Encode.forHtml(principle.getServerDescription())%></td>
                                <td>
                                    <a href="#" onclick="changeSpnPassword('<%=Encode.forJavascriptAttribute(principle.getServerName())%>')" class="icon-link" style="background-image:url(images/edit.gif);"><fmt:message key="edit"/></a>
                                     &nbsp;
                                    <a href="#" onclick="deleteServicePrinciple('<%=Encode.forJavascriptAttribute(principle.getServerName())%>')" class="icon-link" style="background-image:url(images/delete.gif);"><fmt:message key="delete"/></a>
                                </td>
                            </tr>

                                <%          ++i;

                                        }
                                      }
                                    }
                                } catch (org.wso2.carbon.directory.server.manager.ui.ServerManagerClientException e) {


                                    CarbonUIMessage uiMsg = new CarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, e);
                                    session.setAttribute(CarbonUIMessage.ID, uiMsg);
                            %>
                                    <jsp:include page="../admin/error.jsp"/>
                            <%
                                    return;

                                }
                            %>
                            </tbody>
                        </table>





                <% } %>

        </div>
    </div>
    <script type="text/javascript">
        alternateTableRows('internal', 'tableEvenRow', 'tableOddRow');
        alternateTableRows('external', 'tableEvenRow', 'tableOddRow');
    </script>
</fmt:bundle>