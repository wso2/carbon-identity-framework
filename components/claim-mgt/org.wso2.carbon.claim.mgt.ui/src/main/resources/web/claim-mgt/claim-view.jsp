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

<%@page import="org.apache.axis2.context.ConfigurationContext" %>
<%@page import="org.wso2.carbon.CarbonConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimAttributeDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO" %>
<%@ page import="org.wso2.carbon.claim.mgt.ui.client.ClaimAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>


<%@page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@page import="org.wso2.carbon.user.core.UserCoreConstants" %>
<%@page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.ResourceBundle" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.owasp.encoder.Encode" %>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String forwardTo = null;
    ClaimDialectDTO[] claimMappping = null;
    String dialectUri = request.getParameter("dialect");
    String BUNDLE = "org.wso2.carbon.claim.mgt.ui.i18n.Resources";
    ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE, request.getLocale());

    try {
        ClaimAdminClient client = new ClaimAdminClient(cookie, serverURL, configContext);
        claimMappping = client.getAllClaimMappings();
        session.setAttribute("claimMappping", claimMappping);
    } catch (Exception e) {
        String message = resourceBundle.getString("error.while.loading.claim.mappings");
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


<fmt:bundle basename="org.wso2.carbon.claim.mgt.ui.i18n.Resources">
    <carbon:breadcrumb label="claim.mgt"
                       resourceBundle="org.wso2.carbon.claim.mgt.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
    <script type="text/javascript" src="../carbon/admin/js/main.js"></script>
    <script type="text/javascript">
        jQuery(document).ready(function () {

            jQuery(".toggle_container").hide();
            /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the above code if you want to keep the content section expanded. */
            var triggerHandler = function () {
                if (jQuery(this.parentNode).next().is(":visible")) {
                    $('i', this).get(0).className = "claim-arrow-down";
                    jQuery(this).parent().removeClass('trigger-title-container-light');
                } else {
                    $('i', this).get(0).className = "claim-arrow-up";
                    jQuery(this).parent().addClass('trigger-title-container-light');
                }

                jQuery(this.parentNode).next().slideToggle("fast");
                return false; //Prevent the browser jump to the link anchor
            };
            jQuery("a.trigger-title").click(triggerHandler);
        });
        function removeItem(dialect, claim, length) {
            var defaultDialect = "<%=UserCoreConstants.DEFAULT_CARBON_DIALECT%>";
            if ((dialect == defaultDialect) && (length < 2 )) {
                CARBON.showWarningDialog('<fmt:message key="cannot.remove.default.carbon.dialect.all.claims"/>');
                return false;
            } else {
                function doDelete() {
                    $.ajax({
                        type: 'POST',
                        url: 'remove-claim-finish-ajaxprocessor.jsp',
                        headers: {
                            Accept: "text/html"
                        },
                        data: 'dialect=' + dialect + '&claimUri=' + claim,
                        async: false,
                        success: function (responseText, status) {
                            if (status == "success") {
                                location.assign("claim-view.jsp?dialect=" + dialect + "&ordinal=1");
                            }
                        }
                    });
                }

                CARBON.showConfirmationDialog('<fmt:message key="remove.message1"/>' + claim + '<fmt:message key="remove.message2"/>',
                       doDelete, null);
            }
        }
    </script>
    <style type="text/css">
        .editLink {
            background: transparent url(../admin/images/edit.gif) no-repeat 0px 0px !important;
            float: left !important;
            padding: 0px 0px 0px 20px !important;
            color: inherit !important;
            font-size: 12px !important;
            line-height: 20px !important;
            margin-right: 10px !important;
            margin-bottom: 5px !important;
            margin-left: 5px !important;
            height: 30px;
        }

        .deleteLink {
            background: transparent url(../admin/images/delete.gif) no-repeat 0px 0px !important;
            float: left !important;
            padding: 0px 0px 0px 20px !important;
            color: inherit !important;
            font-size: 12px !important;
            line-height: 20px !important;
            margin-right: 10px !important;
            margin-bottom: 5px !important;
            margin-left: 5px !important;
            height: 30px;
        }

        .trigger-title {
            float: left;
            display: block;
            min-width: 300px;
            vertical-align: bottom;
        }

        .claim-arrow-up {
            background: url('../admin/images/up-arrow.png') left bottom;
            width: 16px;
            height: 16px;
            display: inline-block;
            margin-top: 3px;
        }

        .claim-arrow-down {
            background: url('../admin/images/down-arrow.png') left bottom;
            width: 16px;
            height: 16px;
            display: inline-block;
            margin-top: 3px;
        }

        div.trigger-title-container {
            border: solid 1px #c2c4c6;

            -moz-box-shadow: 3px 3px 3px #888;
            -webkit-box-shadow: 3px 3px 3px #888;
            box-shadow: 3px 3px 3px #888;

            padding: 0;
            background-color: #e9e9e9;
            background-repeat: no-repeat;
            background-position: 5px center;
            padding-left: 5px;
            padding-bottom: 0px !important;
            margin-bottom: 0px !important;
            margin: 0;
            z-index: 1;
            cursor: pointer;
            min-height: 30px;

            font-weight: normal;
            outline-color: -moz-use-text-color;
            outline-style: none;
            outline-width: 0;
            font-size: 13px;

        }

        div.trigger-title-container-light {
            background-color: #fefefe;
            -moz-box-shadow: 0 0 0 transparent;
            -webkit-box-shadow: 0 0 0 transparent;
            box-shadow: 0 0 0 transparent;
            font-weight: bold;
        }

        div.trigger-title-container span {
            line-height: 16px;
        }

    </style>
    <div id="middle">
        <h2><fmt:message key='available.claims.for'/><%=Encode.forHtml(dialectUri)%>
        </h2>

        <div id="workArea">

            <%--<div style="height:30px;">--%>
                <%--<a href="javascript:document.location.href='add-claim.jsp?dialect=<%=dialectUri%>'" class="icon-link"--%>
                   <%--style="background-image:url(../admin/images/add.gif);"><fmt:message key='add.new.claim.mapping'/></a>--%>
            <%--</div>--%>


            <%
                for (int i = 0; i < claimMappping.length; i++) {
                    if (claimMappping[i].getDialectURI().equals(dialectUri)) {
                        ClaimMappingDTO[] claims = claimMappping[i].getClaimMappings();
            %>


            <% for (int j = 0; j < claims.length; j++) {
                if (claims[j].getClaim().getDisplayTag() != null) {%>
            <div class="trigger-title-container">
                <a href="#" class="trigger-title"><i class="claim-arrow-down"></i>
                    <span><%=Encode.forHtmlContent(claims[j].getClaim().getDisplayTag())%></span></a>
                <a href="update-claim.jsp?dialect=<%=Encode.forUriComponent(dialectUri)%>&claimUri=<%=Encode.forUriComponent(claims[j].getClaim().getClaimUri())%>"
                   class="editLink icon-link">Edit</a>
                <a href="#" class="icon-link deleteLink" style="background-image:url(../claim-mgt/images/delete.gif);"
                   onclick="removeItem('<%=Encode.forJavaScriptAttribute(dialectUri)%>','<%=Encode.forJavaScriptAttribute(claims[j].getClaim().getClaimUri())%>','<%=Encode.forJavaScriptAttribute(String.valueOf(claims.length))%>'  );return false;"><fmt:message
                        key='remove.claim.mapping'/></a>

                <div style="clear:both"></div>
            </div>
            <div class="toggle_container">
                <table style="width: 100%" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="leftCol-small"><fmt:message key='description'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(claims[j].getClaim().getDescription())%>
                        </td>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='claim.uri'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(claims[j].getClaim().getClaimUri())%>
                        </td>
                    </tr>
                    <%

                        if (claims[j].getMappedAttribute() != null && claims[j].getMappedAttribute().indexOf(";") <= 0) {

                            ClaimAttributeDTO[] attrMap = claims[j].getMappedAttributes();

                            if (attrMap != null) {
                                for (int x = 0; x < attrMap.length; x++) {
                                    String val = attrMap[x].getDomainName() + "/" + attrMap[x].getAttributeName();
                                    claims[j].setMappedAttribute(claims[j].getMappedAttribute() + ";"
                                                                 + val);
                                }
                            }
                        } else {
                            ClaimAttributeDTO[] attrMap = claims[j].getMappedAttributes();
                            if (attrMap != null) {
                                StringBuilder mappedAttributeWithDomain = new StringBuilder();
                                for (int x = 0; x < attrMap.length; x++) {
                                    mappedAttributeWithDomain.append(";");
                                    mappedAttributeWithDomain.append(attrMap[x].getDomainName());
                                    mappedAttributeWithDomain.append("/");
                                    mappedAttributeWithDomain.append(attrMap[x].getAttributeName());
                                }
                                String mappedAttribute = mappedAttributeWithDomain.toString();
                                if (StringUtils.isNotEmpty(mappedAttribute)) {
                                    mappedAttribute = mappedAttribute.substring(1);
                                    claims[j].setMappedAttribute(mappedAttribute);
                                }
                            }
                        }

                    %>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='mapped.attribute'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(claims[j].getMappedAttribute())%>
                        </td>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='regular.expression'/></td>
                        <% if ((claims[j].getClaim().getRegEx()) == null) { %>
                        <td class="leftCol-big"><%="" %>
                        </td>
                        <% } else {%>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(claims[j].getClaim().getRegEx()) %>
                        </td>
                        <% } %>
                    </tr>
                    <tr>
                        <td class="leftCol-small"><fmt:message key='display.order'/></td>
                        <td class="leftCol-big"><%=Encode.forHtmlContent(String.valueOf(claims[j].getClaim().getDisplayOrder()))%>
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-small"><fmt:message key='supported.by.default'/></td>
                        <%if (claims[j].getClaim().getSupportedByDefault()) { %>
                        <td>true</td>
                        <% } else { %>
                        <td>false</td>
                        <%} %>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='required'/></td>
                        <%if (claims[j].getClaim().getRequired()) { %>
                        <td>true</td>
                        <% } else { %>
                        <td>false</td>
                        <%} %>
                    </tr>

                    <tr>
                        <td class="leftCol-small"><fmt:message key='readonly'/></td>
                        <%if (claims[j].getClaim().getReadOnly()) { %>
                        <td>true</td>
                        <% } else { %>
                        <td>false</td>
                        <%} %>
                    </tr>

                    </tbody>
                </table>
            </div>
            <%
                    }
                }
            %>

            <%
                    }
                }
            %>
        </div>
    </div>
</fmt:bundle>
