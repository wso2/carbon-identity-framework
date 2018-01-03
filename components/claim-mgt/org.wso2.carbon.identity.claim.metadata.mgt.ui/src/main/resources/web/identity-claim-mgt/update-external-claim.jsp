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

<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="java.text.MessageFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.ui.client.ClaimMetadataAdminClient" %>
<%@ page import="org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<style>
    .sectionHelp {
        padding-left: 17px;
    }
</style>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext)
            config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    String externalClaimURI = request.getParameter("externalClaimURI");
    String externalClaimDialectURI = request.getParameter("externalClaimDialectURI");
    List<String> availableLocalClaimsURIs = new ArrayList<String>();
    String mappedLocalClaimURI = null;

    ExternalClaimDTO[] externalClaims = null;
    externalClaims = (ExternalClaimDTO[])session.getAttribute("externalClaims-"+ externalClaimDialectURI);

    try {
        ClaimMetadataAdminClient client = new ClaimMetadataAdminClient(cookie, serverURL, configContext);

        if (externalClaims == null) {
            externalClaims = client.getExternalClaims(externalClaimDialectURI);
        }

        ExternalClaimDTO externalClaim = null;
        if (externalClaims != null) {
            for (ExternalClaimDTO externalClaimDTO : externalClaims) {
                if (externalClaimDTO.getExternalClaimURI().equals(externalClaimURI)) {
                    externalClaim = externalClaimDTO;
                    break;
                }
            }
        }

        if (externalClaim == null) {
            String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
            ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

            String unformatted = resourceBundle.getString("error.while.loading.external.claim");
                String message = MessageFormat.format(unformatted, new Object[]{Encode.forHtmlContent(externalClaimURI)});

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
    } else {

        mappedLocalClaimURI = externalClaim.getMappedLocalClaimURI();

        LocalClaimDTO[] availableLocalClaims = client.getLocalClaims();
        if (availableLocalClaims != null) {
            for (LocalClaimDTO localClaim : availableLocalClaims) {
                availableLocalClaimsURIs.add(localClaim.getLocalClaimURI());
            }
        }

        if (externalClaims != null) {
            for (ExternalClaimDTO externalClaimDTO : externalClaims) {
                if (!externalClaimDTO.getExternalClaimURI().equalsIgnoreCase(externalClaim.getExternalClaimURI())) {
                    availableLocalClaimsURIs.remove(externalClaimDTO.getMappedLocalClaimURI());
                }
            }
        }
    }

} catch (Exception e) {
    String BUNDLE = "org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());
    String message = resourceBundle.getString("error.while.loading.claim.details");
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
    <carbon:breadcrumb label="update"
                       resourceBundle="org.wso2.carbon.identity.claim.metadata.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>

    <div id="middle">
        <h2><fmt:message key='update.external.title'/> <%=Encode.forHtml(externalClaimURI)%></h2>

        <div id="workArea">

            <script type="text/javascript">

                function removeItem(externalClaimDialectURI, externalClaimURI, externalClaimURIForMessage) {

                    function doDelete() {
                        $.ajax({
                            type: 'POST',
                            url: 'remove-external-claim-finish-ajaxprocessor.jsp',
                            headers: {
                                Accept: "text/html"
                            },
                            data: 'externalClaimDialectURI=' + externalClaimDialectURI + '&externalClaimURI=' +
                            externalClaimURI,
                            async: false,
                            success: function (responseText, status) {
                                if (status == "success") {
                                    location.assign("list-external-claims.jsp?externalClaimDialectURI=" +
                                            externalClaimDialectURI + "&ordinal=1");
                                }
                            }
                        });
                    }

                    CARBON.showConfirmationDialog('<fmt:message key="remove.message1"/> ' + externalClaimURIForMessage +
                            '<fmt:message key="remove.message2"/>', doDelete, null);
                }

                function validate() {

                    var value = document.getElementsByName("mappedLocalClaimURI")[0].value;
                    if (value == '') {
                        CARBON.showWarningDialog('<fmt:message key="mapped.local.claim.uri.is.required"/>');
                        return false;
                    }

                    document.updateclaim.submit();
                }
            </script>

            <div style="height:30px;">
                <a href="#" class="icon-link deleteLink"
                   style="background-image:url(../identity-claim-mgt/images/delete.gif);"
                   onclick="removeItem('<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimDialectURI))%>',
                           '<%=Encode.forJavaScriptAttribute(Encode.forUriComponent(externalClaimURI))%>',
                           '<%=Encode.forJavaScriptAttribute(externalClaimURI)%>');return
                           false;"><fmt:message key='delete.external.claim'/>
                </a>
            </div>

            <form name="updateclaim" action="update-external-claim-finish-ajaxprocessor.jsp" method="post">
                <table style="width: 100%" class="styledLeft">

                    <thead>
                    <tr>
                        <th colspan="2"><fmt:message key='update.external.claim.details'/></th>
                    </tr>
                    </thead>

                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="styledLeft" cellspacing="0" style="width: 100%">

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='dialect.uri'/></td>
                                    <td class="leftCol-small"><%=Encode.forHtmlContent(externalClaimDialectURI)%></td>
                                    <td class="leftCol-big" hidden><input type="text" name="externalClaimDialectURI"
                                                                       id="externalClaimDialectURI"
                                                                       value="<%=Encode.forHtmlAttribute(externalClaimDialectURI)%>"
                                                                       readonly class="text-box-big"/></td>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='claim.uri'/></td>
                                    <td class="leftCol-small"><%=Encode.forHtmlContent(externalClaimURI)%></td>
                                    <td class="leftCol-big" hidden><input type="text" name="externalClaimURI"
                                                                   id="externalClaimURI"
                                                                   value="<%=Encode.forHtmlAttribute(externalClaimURI)%>"
                                                                   readonly class="text-box-big"/></td>
                                    </td>
                                </tr>

                                <tr>
                                    <td class="leftCol-small"><fmt:message key='mapped.local.claim'/><font
                                            color="red">*</font></td>
                                    <td class="leftCol-big">
                                        <select id="mappedLocalClaimURI" name="mappedLocalClaimURI">
                                            <%
                                                for (String localClaimURI : availableLocalClaimsURIs) {
                                                    if (StringUtils.isNotBlank(mappedLocalClaimURI) &&
                                                            mappedLocalClaimURI.equalsIgnoreCase(localClaimURI)) {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(localClaimURI)%>" selected><%=Encode.forHtmlContent(localClaimURI)%></option>
                                            <%
                                            } else {
                                            %>
                                            <option value="<%=Encode.forHtmlAttribute(localClaimURI)%>"><%=Encode.forHtmlContent(localClaimURI)%></option>
                                            <%
                                                    }
                                                }
                                            %>
                                        </select>
                                        <div class="sectionHelp"><fmt:message key="select.mapped.claim"/></div>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="buttonRow">
                            <input type="button" value="<fmt:message key='update'/>" class="button"
                                   onclick="validate();"/>
                            <input class="button" type="reset" value="<fmt:message key='cancel'/>"
                                   onclick="javascript:document.location.href='list-external-claims.jsp?externalClaimDialectURI=<%=Encode.forUriComponent(externalClaimDialectURI)%>&ordinal=1'"/>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>
