<%--
  ~ Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~
  --%>

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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="carbon" uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.IdentityProvider" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.Property" %>
<%@ page import="org.wso2.carbon.identity.application.common.model.idp.xsd.ProvisioningConnectorConfig" %>
<%@ page import="org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.UUID" %>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    boolean isSfProvDefault = false;
    String sfApiVersion = null;
    String sfDomainName = null;
    String sfClientId = null;
    String sfClientSecret = null;
    String sfUserName = null;
    String sfProvPattern = null;
    String sfProvSeparator = null;
    String sfProvDomainName = null;
    String sfPassword = null;
    String sfOauth2TokenEndpoint = null;
    String sfUniqueID = null;
    ProvisioningConnectorConfig salesforce = null;
    IdentityProvider identityProvider = null;

    String sfProvEnabledChecked = "";
    String sfProvDefaultDisabled = "";
    String sfProvDefaultChecked = "disabled=\'disabled\'";
    boolean isSfProvEnabled = false;

    Map<String, UUID> idpUniqueIdMap = (Map<String, UUID>) session.getAttribute("idpUniqueIdMap");
    ProvisioningConnectorConfig[] provisioningConnectors = null;

    String idPName = request.getParameter("idPName");
    if (idPName != null && idPName.equals("")) {
        idPName = null;
    }

    if (idpUniqueIdMap == null) {
        idpUniqueIdMap = new HashMap<String, UUID>();
    }

    if (idPName != null && idpUniqueIdMap.get(idPName) != null) {
        identityProvider = (IdentityProvider) session.getAttribute(idpUniqueIdMap.get(idPName).toString());
    }

    if (idPName != null && identityProvider != null) {
        provisioningConnectors = identityProvider.getProvisioningConnectorConfigs();
    }

    if (provisioningConnectors != null) {
        for (ProvisioningConnectorConfig provisioningConnector : provisioningConnectors) {
            if (provisioningConnector != null && "salesforce".equals(provisioningConnector.getName())) {
                salesforce = provisioningConnector;
            }
        }
    }

    if (salesforce != null) {

        if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
            isSfProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName()
                    .equals(salesforce.getName());
        }

        Property[] sfProperties = salesforce.getProvisioningProperties();
        if (sfProperties != null && sfProperties.length > 0) {
            for (Property sfProperty : sfProperties) {
                if ("sf-api-version".equals(sfProperty.getName())) {
                    sfApiVersion = sfProperty.getValue();
                } else if ("sf-domain-name".equals(sfProperty.getName())) {
                    sfDomainName = sfProperty.getValue();
                } else if ("sf-clientid".equals(sfProperty.getName())) {
                    sfClientId = sfProperty.getValue();
                } else if ("sf-client-secret".equals(sfProperty.getName())) {
                    sfClientSecret = sfProperty.getValue();
                } else if ("sf-username".equals(sfProperty.getName())) {
                    sfUserName = sfProperty.getValue();
                } else if ("sf-password".equals(sfProperty.getName())) {
                    sfPassword = sfProperty.getValue();
                } else if ("sf-token-endpoint".equals(sfProperty.getName())) {
                    sfOauth2TokenEndpoint = sfProperty.getValue();
                } else if ("sf-prov-pattern".equals(sfProperty.getName())) {
                    sfProvPattern = sfProperty.getValue();
                } else if ("sf-prov-separator".equals(sfProperty.getName())) {
                    sfProvSeparator = sfProperty.getValue();
                } else if ("sf-prov-domainName".equals(sfProperty.getName())) {
                    sfProvDomainName = sfProperty.getValue();
                } else if ("UniqueID".equals(sfProperty.getName())) {
                    sfUniqueID = sfProperty.getValue();
                }
            }
        }
        if (salesforce.getEnabled()) {
            isSfProvEnabled = true;
        }
    }

    if (identityProvider != null) {
        if (isSfProvEnabled) {
            sfProvEnabledChecked = "checked=\'checked\'";
            sfProvDefaultChecked = "";
            if (isSfProvDefault) {
                sfProvDefaultChecked = "checked=\'checked\'";
            }
        }
    }

    if (sfApiVersion == null) {
        sfApiVersion = "";
    }
    if (sfDomainName == null) {
        sfDomainName = "";
    }
    if (sfClientId == null) {
        sfClientId = "";
    }
    if (sfClientSecret == null) {
        sfClientSecret = "";
    }
    if (sfUserName == null) {
        sfUserName = "";
    }
    if (sfPassword == null) {
        sfPassword = "";
    }
    if (sfOauth2TokenEndpoint == null) {
        sfOauth2TokenEndpoint = IdentityApplicationConstants.SF_OAUTH2_TOKEN_ENDPOINT;
    }
    if (sfProvPattern == null) {
        sfProvPattern = "";
    }

    if (sfProvSeparator == null) {
        sfProvSeparator = "";
    }

    if (sfProvDomainName == null) {
        sfProvDomainName = "";
    }


%>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">

    <h2 id="sf_prov_head" class="sectionSeperator trigger active"
        style="background-color: beige;">
        <a href="#"><fmt:message key="sf.provisioning.connector"/></a>

        <div id="sf_enable_logo" class="enablelogo"
             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                src="images/ok.png" alt="enable" width="16" height="16"></div>
    </h2>
    <div class="toggle_container sectionSub"
         style="margin-bottom: 10px; display: none;" id="sfProvRow">

        <table class="carbonFormTable">
            <tr>
                <td class="leftCol-med labelField"><label
                        for="sfProvEnabled"><fmt:message
                        key='sf.provisioning.enabled'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="sfProvEnabled" name="sfProvEnabled"
                               type="checkbox" <%=sfProvEnabledChecked%>
                               onclick="checkProvEnabled(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='sf.provisioning.enabled.help'/>
                                        </span>
                    </div>
                </td>
            </tr>
            <tr style="display:none;">
                <td class="leftCol-med labelField"><label
                        for="sfProvDefault"><fmt:message
                        key='sf.provisioning.default'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="sfProvDefault" name="sfProvDefault"
                               type="checkbox" <%=sfProvDefaultChecked%>
                                <%=sfProvDefaultDisabled%>
                               onclick="checkProvDefault(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='sf.provisioning.default.help'/>
                                        </span>
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.api.version'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-api-version"
                           name="sf-api-version" type="text"
                           value=<%=Encode.forHtmlAttribute(sfApiVersion) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.domain.name'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-domain-name"
                           name="sf-domain-name" type="text"
                           value=<%=Encode.forHtmlAttribute(sfDomainName) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.client.id'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-clientid"
                           name="sf-clientid" type="text"
                           value=<%=Encode.forHtmlAttribute(sfClientId) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.client.secret'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-client-secret"
                           name="sf-client-secret" type="password" autocomplete="off"
                           value=<%=Encode.forHtmlAttribute(sfClientSecret) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.username'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-username"
                           name="sf-username" type="text"
                           value=<%=Encode.forHtmlAttribute(sfUserName) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.password'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-password"
                           name="sf-password" type="password" autocomplete="off"
                           value=<%=Encode.forHtmlAttribute(sfPassword) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.oauth.endpoint'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="sf-token-endpoint"
                           name="sf-token-endpoint" type="text"
                           value=<%=Encode.forHtmlAttribute(sfOauth2TokenEndpoint)%>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.pattern'/>:
                </td>
                <td>
                    <div>
                        <input class="text-box-big" id="sf-prov-pattern"
                               name="sf-prov-pattern" type="text"
                               value="<%=Encode.forHtmlAttribute(sfProvPattern)%>">
                    </div>
                    <div class="sectionHelp">
                        <fmt:message key='sf_prov_pattern.help'/>
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.separator'/>:
                </td>
                <td>
                    <div>
                        <input class="text-box-big" id="sf-prov-separator"
                               name="sf-prov-separator" type="text"
                               value=<%=Encode.forHtmlAttribute(sfProvSeparator)%>>
                    </div>
                    <div class="sectionHelp">
                        <fmt:message key='sf.provisioning.separator.help'/>
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='sf.provisioning.domain'/>:
                </td>
                <td><input class="text-box-big" id="sf-prov-domainName"
                           name="sf-prov-domainName" type="text"
                           value=<%=Encode.forHtmlAttribute(sfProvDomainName)%>>
                    <%if (sfUniqueID != null) {%>
                    <input type="hidden" id="sf-unique-id" name="sf-unique-id"
                           value=<%=Encode.forHtmlAttribute(sfUniqueID)%>>
                    <%}%>
                </td>
            </tr>

        </table>

    </div>
</fmt:bundle>