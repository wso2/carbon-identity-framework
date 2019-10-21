<%--
  ~ Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ResourceBundle" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    ClaimDialectDTO[] claimDialects = null;
    try {
        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);
        claimDialects = client.getClaimDialects();
    
        // Add the local claim dialect to the top.
        if (claimDialects != null && claimDialects.length > 0) {
            List<ClaimDialectDTO> dialectList = new ArrayList<>(Arrays.asList(claimDialects));
            int localDialectIndex = -1;
            for (int i = 0; i < dialectList.size(); i++) {
                if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equalsIgnoreCase(
                        dialectList.get(i).getClaimDialectURI())) {
                    localDialectIndex = i;
                    break;
                }
            }
            if (localDialectIndex != -1) {
                dialectList.add(0, dialectList.remove(localDialectIndex));
            }
            claimDialects = dialectList.toArray(new ClaimDialectDTO[0]);
        }
        
    } catch (Exception e) {
        String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
        ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
        String message = resourceBundle.getString("error.while.loading.claim.dialects");
        CarbonUIMessage.sendCarbonUIMessage(message, CarbonUIMessage.ERROR, request);
        String forwardTo = "../admin/error.jsp";
%>
<script type="text/javascript">
    function forward() {
        location.href = "<%=forwardTo%>";
    }

    forward();
</script>
<%
    }
%>

<fmt:bundle basename="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources">
    <carbon:breadcrumb
            label="claim.dialects"
            resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='list.title'/></h2>

        <div id="workArea">

            <script type="text/javascript">
                function removeItem(externalClaimDialectURI, externalClaimDialectURIForMessage) {

                    function doDelete() {
                        $.ajax({
                            type: 'POST',
                            url: 'remove-dialect-finish-ajaxprocessor.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'externalClaimDialectURI=' + externalClaimDialectURI,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list.jsp?region=region1&item=claim_mgt_menu&ordinal=0");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('<fmt:message key="remove.message0"/> ' +
                            externalClaimDialectURIForMessage + '<fmt:message key="remove.message2"/>', doDelete, null);
                }
            </script>


            <table style="width: 100%" class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key='available.claim.dialectes'/></th>
                </tr>
                </thead>
                <tbody>

                <%
                    if (claimDialects != null && claimDialects.length > 0) {
                        for (int i = 0; i < claimDialects.length; i++) {
                            String claimDialectURI = claimDialects[i].getClaimDialectURI();
                %>

                <tr>
                    <%
                            if (UserCoreConstants.DEFAULT_CARBON_DIALECT.equalsIgnoreCase(claimDialectURI)) {
                    %>

                    <td width="50%">
                        <a href="list-local-claims.jsp"><%=UserCoreConstants.DEFAULT_CARBON_DIALECT%></a>
                    </td>
                    <td width="50%">
                    </td>

                    <%
                            } else {
                    %>

                    <td width="50%">
                        <a href="list-external-claims.jsp?externalClaimDialectURI=<%=Encode.forUriComponent(claimDialectURI)%>"><%=Encode.forHtmlContent(claimDialectURI)%></a>
                    </td>
                    <td width="50%">
                        <a title="<fmt:message key='remove.claim.dialect'/>"
                           onclick="removeItem('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(claimDialectURI))%>', '<%=Encode.forJavaScriptAttribute(claimDialectURI)%>');return false;"
                           href="#" style="background-image: url(images/delete.gif);"
                           class="icon-link"><fmt:message key='delete'/></a>
                    </td>

                    <%
                            }
                    %>
                </tr>

                <%
                        }
                    } else {
                %>

                <tr>
                    <td width="100%" colspan="2"><i><fmt:message key='no.claim.dialects.available'/></i></td>
                </tr>

                <%
                    }
                %>

                </tbody>
            </table>
            <br/>
        </div>
    </div>
</fmt:bundle>
