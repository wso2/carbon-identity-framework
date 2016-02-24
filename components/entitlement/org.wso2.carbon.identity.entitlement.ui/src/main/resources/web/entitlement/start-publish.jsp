<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ page
        import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.common.EntitlementConstants" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.client.EntitlementPolicyAdminServiceClient" %>
<%@ page import="org.wso2.carbon.identity.entitlement.ui.util.ClientUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>


<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="resources/js/main.js"></script>
<!--Yahoo includes for dom event handling-->
<script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
<script src="../entitlement/js/create-basic-policy.js" type="text/javascript"></script>

<%
    String[] subscriberIds = null;
    boolean showNoSubscriber = false;
    String publishAll = request.getParameter("publishAllPolicies");
    String policyId = request.getParameter("policyId");
    String toPDP = request.getParameter("toPDP");
    String[] selectedPolicies = request.getParameterValues("policies");
    String publishAction = request.getParameter("publishAction");
    String policyVersion = request.getParameter("policyVersion");
    String policyOrder = request.getParameter("policyOrder");
    String policyEnable = request.getParameter("policyEnable");
    String versionSelector = request.getParameter("versionSelector");
    String orderSelector = request.getParameter("orderSelector");
    String BUNDLE = "org.wso2.carbon.identity.entitlement.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    if(publishAction == null || publishAction.trim().length() == 0) {
        publishAction = (String)session.getAttribute("publishAction");        
    } else {
        session.setAttribute("publishAction", publishAction);    
    }

    // setting default action
    if(publishAction == null){
        publishAction = EntitlementConstants.PolicyPublish.ACTION_CREATE;
    }

    if(policyOrder == null){
        policyOrder = "";
    }

    if(policyEnable == null){
        policyEnable = "";
    }

    int numberOfPages = 0;
    String subscriberSearchString = request.getParameter("subscriberSearchString");
    if (subscriberSearchString == null) {
        subscriberSearchString = "*";
    } else {
        subscriberSearchString = subscriberSearchString.trim();
    }
    String paginationValue = "subscriberSearchString=" + subscriberSearchString;

    String pageNumber = request.getParameter("pageNumber");
    if (pageNumber == null) {
        pageNumber = "0";
    }
    int pageNumberInt = 0;
    try {
        pageNumberInt = Integer.parseInt(pageNumber);
    } catch (NumberFormatException ignored) {
        // ignore
    }

    if (publishAll != null && "true".equals(publishAll.trim())) {
        session.setAttribute("publishAllPolicies", true);
    } else {
        session.setAttribute("publishAllPolicies", false);
    }

    if (policyId != null && policyId.trim().length() > 0) {
        selectedPolicies = new String[]{policyId};
    }

    if(selectedPolicies != null ){
        session.setAttribute("selectedPolicies", selectedPolicies);
    } else {
        selectedPolicies = (String[]) session.getAttribute("selectedPolicies");
    }

    String tmp = "";
    if(selectedPolicies != null && selectedPolicies.length == 1){
        policyId = selectedPolicies[0];
    }


    try{
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.                                                                           CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        EntitlementPolicyAdminServiceClient client = new EntitlementPolicyAdminServiceClient(cookie,
                serverURL, configContext);       
        if (policyId != null && policyId.trim().length() > 0) {
            String[] versions = client.getPolicyVersions(policyId);
            if(versions != null && versions.length > 0){
                for(int i = 0; i < (versions.length - 1); i++){ // remove current version
                    String version = versions[i];
                    if(policyVersion != null && policyVersion.trim().equalsIgnoreCase(version)) {
                        tmp += "<option value=\"" + version + "\"    selected=\"selected\" >" + version + "</option>";
                    } else {
                        tmp += "<option value=\"" + version + "\" >" + version + "</option>";
                    }
                }
            }
        }
        // as these are just strings, get all values in to UI and the do the pagination
        subscriberIds = client.getSubscriberIds(subscriberSearchString);
        if(subscriberIds != null){
            numberOfPages = (int) Math.ceil((double) subscriberIds.length / 5);
            subscriberIds = ClientUtil.doPagingForStrings(pageNumberInt, 5,
                    client.getSubscriberIds(subscriberSearchString));
        } else {
            showNoSubscriber = true;
        }
    } catch (Exception e) {
        String message = resourceBundle.getString("error.loading.subscribers") + e.getMessage();
%>
<script type="text/javascript">
    CARBON.showErrorDialog('<%=Encode.forJavaScript(Encode.forHtml(message))%>', function () {
        location.href = "index.jsp";
    });
</script>
<%
    }
%>
<fmt:bundle basename="org.wso2.carbon.identity.entitlement.ui.i18n.Resources">
<carbon:breadcrumb
        label="publish.policy"
        resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
        topPage="true"
        request="<%=request%>"/>

<link href="../entitlement/css/entitlement.css" rel="stylesheet" type="text/css" media="all"/>

<script type="text/javascript">

    var allSubscribersSelected = false;

    function doCancel() {
        location.href = 'index.jsp';
    }

    function doNext() {
        document.publishForm.action = "policy-publish.jsp";
        document.publishForm.submit();
    }

    function doPaginate(page, pageNumberParameterName, pageNumber){

        document.publishForm.action =  page + "?" + pageNumberParameterName + "=" + pageNumber + "&";
        document.publishForm.submit();
    }

    function showVersion(){

        var selectorElement = document.getElementById('policyVersionSelect');
        selectorElement.innerHTML = '<label for="policyVersion"></label>' +
            '<select id="policyVersion" name="policyVersion" class="leftCol-small">' +'<%=tmp%>' + '</select>';
    }

    function disableVersion(){
        var selectorElement = document.getElementById('policyVersionSelect');
        selectorElement.innerHTML = '';
    }

    function showOrder(){

        var selectorElement = document.getElementById('policyOrderSelect');
        selectorElement.innerHTML = '<label for="policyOrder"></label>' +
                '<input type="text" name="policyOrder" id="policyOrder" value="<%=Encode.forJavaScript(Encode.forHtmlAttribute(policyOrder))%>"/>';
    }

    function disableOrder(){
        var selectorElement = document.getElementById('policyOrderSelect');
        selectorElement.innerHTML = '';
    }

    function searchService() {
        document.publishForm.action = "start-publish.jsp";
        document.publishForm.submit();
    }

    function resetVars() {
        allSubscribersSelected = false;

        var isSelected = false;
        if (document.publishForm.subscribersList[0] != null) { // there is more than 1 service
            for (var j = 0; j < document.publishForm.subscribersList.length; j++) {
                if (document.publishForm.subscribersList[j].checked) {
                    isSelected = true;
                }
            }
        } else if (document.publishForm.subscribersList != null) { // only 1 service
            if (document.publishForm.subscribersList.checked) {
                isSelected = true;
            }
        }
        return false;
    }

    function viewSubscriber(subscriber) {
        location.href = "add-subscriber.jsp?view=true&subscriberId=" + subscriber;
    }

    function publishToSubscriber(toPDP) {
        var selected = false;
        if(!toPDP){
            if (document.publishForm.subscribersList == null) {
                CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
                return;
            }

            if (document.publishForm.subscribersList[0] != null) { // there is more than 1 policy
                for (var j = 0; j < document.publishForm.subscribersList.length; j++) {
                    selected = document.publishForm.subscribersList[j].checked;
                    if (selected) break;
                }
            } else if (document.publishForm.subscribersList != null) { // only 1 policy
                selected = document.publishForm.subscribersList.checked;
            }

            if (!selected) {
                CARBON.showInfoDialog('<fmt:message key="select.subscriber.to.be.published"/>');
                return;
            }
            if (allSubscribersSelected) {
                CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribers.prompt"/>", function () {
                    document.publishForm.action = "publish-finish.jsp";
                    document.publishForm.submit();
                });
            } else {
                CARBON.showConfirmationDialog("<fmt:message key="publish.selected.subscriber.prompt"/>", function () {
                    document.publishForm.action = "publish-finish.jsp";
                    document.publishForm.submit();
                });
            }
        }  else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.pdp.subscriber.prompt"/>", function () {
                document.publishForm.action = "publish-finish.jsp";
                document.publishForm.submit();
            });
        }
    }

    function publishToAll() {
        if (document.publishForm.subscribersList == null) {
            CARBON.showWarningDialog('<fmt:message key="no.subscriber.to.be.published"/>');
            return;
        } else {
            CARBON.showConfirmationDialog("<fmt:message key="publish.to.all.subscribersList.prompt"/>", function () {
                document.publishForm.action = "publish-finish.jsp?publishToAllSubscribers=true";
                document.publishForm.submit();
            });
        }
    }

    function showOnChange(){

        if(jQuery('#addPolicy').is(':checked')) {
            jQuery('#showPolicyVersion').show();
            jQuery('#showPolicyOrder').show();
            jQuery('#showPolicyEnable').show();
        }
        if(jQuery('#updatePolicy').is(':checked')) {
            jQuery('#showPolicyVersion').show();
            jQuery('#showPolicyOrder').hide();
            jQuery('#showPolicyEnable').hide();
        }
        if(jQuery('#orderPolicy').is(':checked')) {
            jQuery('#showPolicyVersion').hide();
            jQuery('#showPolicyOrder').show();
            jQuery('#showPolicyEnable').hide();
        }
        if(jQuery('#enablePolicy').is(':checked')) {
            jQuery('#showPolicyVersion').hide();
            jQuery('#showPolicyOrder').hide();
            jQuery('#showPolicyEnable').hide();
        }
        if(jQuery('#disablePolicy').is(':checked')) {
            jQuery('#showPolicyVersion').hide();
            jQuery('#showPolicyOrder').hide();
            jQuery('#showPolicyEnable').hide();
        }
        if(jQuery('#deletePolicy').is(':checked')) {
            jQuery('#showPolicyVersion').hide();
            jQuery('#showPolicyOrder').hide();
            jQuery('#showPolicyEnable').hide();
        }
    }

</script>

<div id="middle">
    <h2><fmt:message key="publish.policy"/></h2>
    <div id="workArea">
        <form action="start-publish.jsp" name="publishForm" method="post">

        <%
            if(policyId != null){

        %>
        <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th  colspan="6"> <fmt:message key="select.publish.actions"/></th>
            </tr>
            </thead>
            <tr>
                <td style="width: 17%;margin-top:10px;">
                    <label>
                        <input id="addPolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_CREATE%>">
                        <fmt:message key="select.publish.actions.add"/>
                    </label>
                </td>
                <td style="width: 17%;margin-top:10px;">
                    <label>
                        <input id="updatePolicy" name="publishAction" type="radio" onchange="showOnChange()"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_UPDATE%>">
                        <fmt:message key="select.publish.actions.update"/>
                    </label>
                </td>
                <td style="width: 16%;margin-top:10px;">
                    <label>
                        <input id="orderPolicy" name="publishAction" type="radio"  onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_ORDER%>">
                        <fmt:message key="select.publish.actions.order"/>
                    </label>
                </td>
                <td style="width: 16%;margin-top:10px;">
                    <label>
                        <input id="enablePolicy" name="publishAction" type="radio" onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_ENABLE%>">
                        <fmt:message key="select.publish.actions.enable"/>
                    </label>
                </td>
                <td style="width: 16%;margin-top:10px;">
                    <label>
                        <input id="disablePolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_DISABLE%>">
                        <fmt:message key="select.publish.actions.disable"/>
                    </label>
                </td>
                <td style="width: 16%;margin-top:10px;">
                    <label>
                        <input id="deletePolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                        <% if(EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_DELETE%>">
                        <fmt:message key="select.publish.actions.delete"/>
                    </label>
                </td>
            </tr>
        </table>

        <%
            }  else {
        %>

        <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th  colspan="6"> <fmt:message key="select.publish.actions"/></th>
            </tr>
            </thead>
            <tr>
                <td style="width: 20%;margin-top:10px;">
                    <label>
                        <input id="addPolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_CREATE%>">
                        <fmt:message key="select.publish.actions.add.policies"/>
                    </label>
                </td>
                <td style="width: 20%;margin-top:10px;">
                    <label>
                        <input id="updatePolicy" name="publishAction" type="radio" onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_UPDATE%>">
                        <fmt:message key="select.publish.actions.update.policies"/>
                    </label>
                </td>
                <td style="width: 20%;margin-top:10px;">
                    <label>
                        <input id="enablePolicy" name="publishAction" type="radio" onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_ENABLE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_ENABLE%>">
                        <fmt:message key="select.publish.actions.enable.policies"/>
                    </label>
                </td>
                <td style="width: 20%;margin-top:10px;">
                    <label>
                        <input id="disablePolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_DISABLE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_DISABLE%>">
                        <fmt:message key="select.publish.actions.disable.policies"/>
                    </label>
                </td>
                <td style="width: 20%;margin-top:10px;">
                    <label>
                        <input id="deletePolicy" name="publishAction" type="radio"   onchange="showOnChange()"
                                <% if(EntitlementConstants.PolicyPublish.ACTION_DELETE.equals(publishAction)){%> checked="checked" <% }%>
                               value="<%=EntitlementConstants.PolicyPublish.ACTION_DELETE%>">
                        <fmt:message key="select.publish.actions.delete.policies"/>
                    </label>
                </td>
            </tr>
        </table>

        <%
            }
        %>

        <%
            if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction)){
        %>
            <%
                if(policyId != null){
            %>
                <table id="showPolicyEnable"  class="styledLeft" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="select.publish.enable.disable"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td style="width: 33%;margin-top:10px;">
                            <label>
                                <input name="policyEnable" type="radio" value="true"
                                        <%if(policyEnable == null || policyEnable.trim().length() == 0) { %>
                                       checked="checked"
                                        <% } %>
                                        >
                                <fmt:message key="select.publish.enable"/>
                            </label>
                        </td>
                        <td style="width: 33%;margin-top:10px;">
                            <label>
                                <input name="policyEnable" type="radio"
                                        <%if(policyEnable != null && policyEnable.trim().length() > 0) { %>
                                       checked="checked"
                                        <% } %>
                                        >
                                <fmt:message key="select.publish.disable"/>
                            </label>
                        </td>
                        <td style="width: 33%;margin-top:10px;" >
                        </td>
                    </tr>
                </table>
            <%
                }  else {
            %>
                <table id="showPolicyEnable"  class="styledLeft" style="width: 100%;margin-top:10px;">
                    <thead>
                    <tr>
                        <th colspan="3"><fmt:message key="select.publish.enable.disable"/></th>
                    </tr>
                    </thead>
                    <tr>
                        <td style="width: 33%;margin-top:10px;">
                            <label>
                                <input name="policyEnable" type="radio" value="true"
                                        <%if(policyEnable == null || policyEnable.trim().length() == 0) { %>
                                       checked="checked"
                                        <% } %>
                                        >
                                <fmt:message key="select.publish.enable.policies"/>
                            </label>
                        </td>
                        <td style="width: 33%;margin-top:10px;">
                            <label>
                                <input name="policyEnable" type="radio"
                                        <%if(policyEnable != null && policyEnable.trim().length() > 0) { %>
                                       checked="checked"
                                        <% } %>
                                        >
                                <fmt:message key="select.publish.disable.policies"/>
                            </label>
                        </td>
                        <td style="width: 33%;margin-top:10px;" >
                        </td>
                    </tr>
                </table>
            <%
                }
            %>

        <%
            }
        %>


        <%
            if(policyId != null){
        %>
            <%

                if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction) ||
                        EntitlementConstants.PolicyPublish.ACTION_UPDATE.equals(publishAction)){
            %>
        <table id="showPolicyVersion" class="styledLeft" style="width: 100%;margin-top:10px;">
            <thead>
            <tr>
                <th colspan="4"><fmt:message key="select.publish.version"/></th>
            </tr>
            </thead>
            <tr>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="versionSelector" type="radio" value="versionSelector"
                                <%if(policyVersion == null || policyVersion.trim().length() == 0) { %>
                               checked="checked"
                                <% } %>
                               onclick="disableVersion();">
                        <fmt:message key="select.publish.version.current"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;">
                    <label>
                        <input name="versionSelector" type="radio"
                                <%if(policyVersion != null && policyVersion.trim().length() > 0) { %>
                               checked="checked"
                                <% } %>
                               onclick="showVersion();">
                        <fmt:message key="select.publish.version.older"/>
                    </label>
                </td>
                <td style="width: 33%;margin-top:10px;" id="policyVersionSelect" >
                </td>
            </tr>
        </table>
        <%
            }
        %>

        <%
            if(EntitlementConstants.PolicyPublish.ACTION_CREATE.equals(publishAction) ||
                    EntitlementConstants.PolicyPublish.ACTION_ORDER.equals(publishAction)){
        %>
        <table id="showPolicyOrder"  class="styledLeft" style="width: 100%;margin-top:10px;">
        <thead>
            <tr>
                <th colspan="3"><fmt:message key="select.publish.order"/></th>
            </tr>
        </thead>
        <tr>
            <td style="width: 33%;margin-top:10px;">
                <label>
                    <input name="orderSelector" type="radio" value="orderSelector"
                    <%if(policyOrder == null || policyOrder.trim().length() == 0) { %>
                           checked="checked"
                    <% } %>
                           onclick="disableOrder();">
                    <fmt:message key="select.publish.order.default"/>
                </label>
            </td>
            <td style="width: 33%;margin-top:10px;">
                <label>
                    <input name="orderSelector" type="radio"
                    <%if(policyOrder != null && policyOrder.trim().length() > 0) { %>
                           checked="checked"
                    <% } %>
                           onclick="showOrder();">
                    <fmt:message key="select.publish.order.custom"/>
                </label>
            </td>
            <td style="width: 33%;margin-top:10px;" id="policyOrderSelect" >
            </td>
        </tr>
        </table>
        <%
            }
        %>

        <%
            }
        %>

    <%
        if(!"true".equals(toPDP)){
    %>

            <table class="styledLeft noBorders" style="width: 100%;margin-top:10px;">
                <thead>
                <tr>
                    <th><fmt:message key='select.subscriber'/></th>
                </tr>
                </thead>
            </table>
            <table class="styledLeft noBorders">
                <tbody>
                <tr style="border:0; !important">
                    <td style="width: 20%;margin-top:10px;">
                        <nobr>
                            <fmt:message key="search"/>
                            <input type="text" name="subscriberSearchString"
                                   value="<%= subscriberSearchString != null? Encode.forHtmlAttribute(subscriberSearchString) :""%>"/>&nbsp;
                        </nobr>
                    </td>
                    <td>
                        <a class="icon-link" href="#" style="background-image: url(images/search.gif);"
                           onclick="searchService(); return false;"
                           alt="<fmt:message key="search"/>"></a>
                    </td>
                </tr>
                </tbody>
            </table>

            <table class="styledLeft" style="width: 100%;margin-top:10px;">
            <%
                if (subscriberIds != null && subscriberIds.length > 0) {
                    for (String subscriber : subscriberIds) {
                        if (subscriber != null && subscriber.trim().length() > 0 ) {
            %>

            <tr>
                <td width="10px" style="text-align:center; !important">
                    <input type="checkbox" name="subscribersList"
                           value="<%=Encode.forHtmlAttribute(subscriber)%>"
                           onclick="resetVars()" class="chkBox" />
                </td>
                <td><%=Encode.forHtml(subscriber)%>
                </td>
            </tr>
            <%
                    }
                }
            %>
            <%
                } else {
            %>
            <tr class="noRuleBox">
                <td colspan="3"><fmt:message key="no.subscribersList.defined"/><br/></td>
            </tr>
            <%
                }
            %>

        </table>

        <carbon:paginator pageNumber="<%=pageNumberInt%>"
                          numberOfPages="<%=numberOfPages%>"
                          action="post"
                          page="start-publish.jsp"
                          pageNumberParameterName="pageNumber"
                          parameters="<%=Encode.forHtmlAttribute(paginationValue)%>"
                          resourceBundle="org.wso2.carbon.identity.entitlement.ui.i18n.Resources"
                          prevKey="prev" nextKey="next"/>

    <%
        } else {
    %>
            <tr>
                <td>
                    <input name="subscribersList" type="hidden" value="<%=EntitlementConstants.PDP_SUBSCRIBER_ID%>" />
                </td>
            </tr>
    <%
        }
    %>


        <tr class="buttonRow">
            <td>
                <%
                    if("true".equals(toPDP)){
                %>
                <input type="button" class="button" value="Publish" onclick="publishToSubscriber(true);">
                <%
                    }  else {
                %>
                <input type="button" class="button" value="Publish" onclick="publishToSubscriber(false);">
                <input type="button" class="button" value="PublishToAll" onclick="publishToAll();">
                <%
                    }
                %>
                <input type="button" class="button" value="Cancel" onclick="doCancel();">
            </td>
        </tr>
    </form>

<%if(policyVersion != null && policyVersion.trim().length() > 0) { %>
<script type="text/javascript">
    showVersion()
</script>
<%}%>
<%if(policyOrder != null && policyOrder.trim().length() > 0) { %>
<script type="text/javascript">
    showOrder();
</script>
<%}%>
<%if(showNoSubscriber) { %>
<script type="text/javascript">
CARBON.showWarningDialog('<%=resourceBundle.getString("no.subscribers.found")%>');
</script>
<%}%>
    </div>
</div>
</fmt:bundle>