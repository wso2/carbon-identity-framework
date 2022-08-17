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
<%@page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherDataHolder" %>
<%@ page import="org.wso2.carbon.identity.entitlement.stub.dto.PublisherPropertyDTO" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.EntitlementPolicyConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.PropertyDTOComparator" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.owasp.encoder.Encode" %>

<%
    String subscriberId;
    PublisherDataHolder subscriber = null;
    PublisherDataHolder[] dataHolders;
    PublisherPropertyDTO[] propertyDTOs = null;
    String selectedModule = null;
    String forwardTo = null;
    boolean view = false;
    String paginationValue = "" ;

    EntitlementPolicyAdminServiceClient client = null;


          int numberOfPages = 0;
          String isPaginatedString = request.getParameter("isPaginated");
          if (isPaginatedString != null && isPaginatedString.equals("true")) {
              client = (EntitlementPolicyAdminServiceClient) session.getAttribute(EntitlementPolicyConstants.ENTITLEMENT_SUBSCRIBER_CLIENT);
          }



          String pageNumber = request.getParameter("pageNumber");
          if (pageNumber == null) {
              pageNumber = "0";
          }
          int pageNumberInt = 0;
          try {
              pageNumberInt = Integer.parseInt(pageNumber);
          } catch (NumberFormatException ignored) {
          }


    selectedModule = request.getParameter("selectedModule");
    String viewString = request.getParameter("view");
    subscriberId = request.getParameter("subscriberId");
    dataHolders = (PublisherDataHolder[]) session.
                    getAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_MODULE);

    if((viewString != null)){
        view = Boolean.parseBoolean(viewString);
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.
                    CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
	ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {

        if (client == null) {

            client = new EntitlementPolicyAdminServiceClient(cookie,
                            serverURL, configContext);
            session.setAttribute(EntitlementPolicyConstants.ENTITLEMENT_SUBSCRIBER_CLIENT, client);
        }

        if(subscriberId != null){
            subscriber = client.getSubscriber(subscriberId);
            if(subscriber != null){
                propertyDTOs = subscriber.getPropertyDTOs();
                selectedModule = subscriber.getModuleName();
                dataHolders = new PublisherDataHolder[]{subscriber};
            }
        } else {
            if(dataHolders == null){
                dataHolders = client.getPublisherModuleData();
            }
            if(dataHolders != null){
                session.setAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_MODULE, dataHolders);
                if(selectedModule != null){
                    for(PublisherDataHolder holder : dataHolders){
                        if(selectedModule.equals(holder.getModuleName())){
                            propertyDTOs = holder.getPropertyDTOs();
                            break;
                        }
                    }
                }
            }
        }
        if(propertyDTOs != null){
            session.setAttribute(EntitlementPolicyConstants.ENTITLEMENT_PUBLISHER_PROPERTY, propertyDTOs);
            java.util.Arrays.sort(propertyDTOs , new PropertyDTOComparator());
        }

        paginationValue = "isPaginated=true&view="+viewString+"&subscriberId="+subscriberId;
    } catch (Exception e) {
    	String message = resourceBundle.getString("error.while.performing.advance.search");
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
		label="add.new.subscriber"
		resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
		topPage="true"
		request="<%=request%>" />

    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <!--Yahoo includes for dom event handling-->
    <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>
    <link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    function doAdd(){
        if(validateUnsafeChars()){
			document.requestForm.action = "policy-publish.jsp";
			var update = document.createElement("input");
			update.setAttribute("type", "hidden");
			update.setAttribute("name", "update");
			update.setAttribute("value", "false");
			document.requestForm.appendChild(update);
			document.requestForm.submit();
        }
    }

    function doUpdate(){
        if(validateUnsafeChars()){
			document.requestForm.action = "policy-publish.jsp";
			var update = document.createElement("input");
			update.setAttribute("type", "hidden");
			update.setAttribute("name", "update");
			update.setAttribute("value", "true");
			document.requestForm.appendChild(update);
			document.requestForm.submit();
        }
    }

    function doCancel(){
        location.href = 'policy-publish.jsp';
    }

    function getSelectedSubjectType() {
        document.requestForm.submit();
    }

    function validateUnsafeChars(){
		var unsafeCharPattern = /[<>`\"]/;
		var elements = document.getElementsByTagName("input");
		for(i = 0; i < elements.length; i++){
			if((elements[i].type === 'text' || elements[i].type === 'password') && 
			   elements[i].value != null && elements[i].value.match(unsafeCharPattern) != null){
				CARBON.showWarningDialog("<fmt:message key="unsafe.char.validation.msg"/>");
				return false;
			}
		}
		return true;
    }

</script>

<div id="middle">
    <%
        if(view){
    %>
    <h2><fmt:message key="show.subscriber"/></h2>
    <%
        } else {
    %>
    <h2><fmt:message key="add.subscriber"/></h2>
    <%
        }
    %>
    <div id="workArea">
        <%
            if(view){
        %>
        <div class="sectionSeperator">
            <fmt:message key="subscriber.configurations"/>
        </div>
        <%
            }
        %>
        <form id="requestForm" name="requestForm" method="post" action="add-subscriber.jsp">
        <%
            if(view){
        %>
            <div class="sectionSub">
            <table  class="styledLeft"  style="width: 100%;margin-top:10px;">
            <%
                if(propertyDTOs != null){
                    for(PublisherPropertyDTO dto : propertyDTOs){
                        if(dto.getSecret()){
                            continue;
                        }
                        if(dto.getDisplayName() != null && dto.getValue() != null){
            %>
                <tr>
                    <td><%=Encode.forHtmlContent(dto.getDisplayName())%></td>
                    <td><%=Encode.forHtmlContent(dto.getValue())%></td>
                </tr>
            <%
                        }
                    }
                }
            %>
            </table>
            </div>

            <div class="buttonRow">
                <a onclick="doCancel()" class="icon-link" style="background-image:none;"><fmt:message key="back.to.subscribersList"/></a><div style="clear:both"></div>
            </div>
        <%
            } else {
        %>
            <table class="styledLeft noBorders">
                <tr>
                    <td class="leftCol-small"><fmt:message key='select.module'/><span class="required">*</span></td>
                    <td colspan="2">
                    <select  onchange="getSelectedSubjectType();" id="selectedModule" name="selectedModule">
                            <option value="Selected" selected="selected">---Select----</option>
                            <%
                                if(dataHolders != null){
                                    for (PublisherDataHolder module : dataHolders) {
                                        if(module.getModuleName().equals(selectedModule)) {
                            %>
                                <option value="<%=Encode.forHtmlAttribute(selectedModule)%>" selected="selected"><%=Encode.forHtmlContent(selectedModule)%></option>
                            <%
                                        } else {
                            %>
                                <option value="<%=Encode.forHtmlAttribute(module.getModuleName())%>"><%=Encode.forHtmlContent(module.getModuleName())%></option>
                            <%
                                        }
                                    }
                                }
                            %>
                    </select>
                    </td>
                </tr>

                <%
                    if(propertyDTOs != null){
                        for (PublisherPropertyDTO dto : propertyDTOs) {
                            if(dto.getDisplayName() == null){
                                continue;
                            }
                            String inputType = "text";
                            if (dto.getSecret()) {
                                inputType = "password";
                            }
                %>
                <tr>
                    <td class="leftCol-small"><%=Encode.forHtmlContent(dto.getDisplayName())%>
                    <%
                        if(dto.getRequired()){
                    %>
                        <span class="required">*</span>
                    <%
                        }
                    %>
                    </td>
                    <td>
                        <% if(dto.getValue() != null) {%>
                            <input type="<%=inputType%>" name="<%=Encode.forHtmlAttribute(dto.getId())%>"
                                   id="<%=Encode.forHtmlAttribute(dto.getId())%>" value="<%=Encode.forHtmlAttribute(dto.getValue())%>"
                                    <% if("subscriberId".equals(dto.getId())){ %> readonly='readonly' <% } %> />
                        <%
                            } else {
                        %>
                            <input type="<%=inputType%>" name="<%=Encode.forHtmlAttribute(dto.getId())%>"
                                   id="<%=Encode.forHtmlAttribute(dto.getId())%>"
                                   <% if("subscriberPassword".equals(dto.getId())){ %> autocomplete="off" <% } %>/>
                        <%
                            }
                        %>
                    </td>
                </tr>
            <%
                    }
                }
            %>
            <tr>
                <td colspan="2" class="buttonRow">
                    <input class="button" type="button"
                        <%if(subscriber != null){%> value="<fmt:message key='update'/>" onclick="doUpdate();" <%} else { %>
                        value="<fmt:message key='add'/>" onclick="doAdd();" <% } %> />
                    <input class="button" type="button" value="<fmt:message key='cancel'/>"  onclick="doCancel();"/>
                </td>
            </tr>
        </table>
        <%
            }
        %>
        </form>
    </div>
</div>
</fmt:bundle>
