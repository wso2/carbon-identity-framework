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
<%@ page import="org.wso2.carbon.idp.mgt.ui.util.IdPManagementUIUtil" %>
<link href="css/idpmgt.css" rel="stylesheet" type="text/css" media="all"/>
<carbon:breadcrumb label="identity.providers" resourceBundle="org.wso2.carbon.idp.mgt.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<jsp:include page="../dialog/display_messages.jsp"/>

<%
    boolean isSfProvEnabled = false;
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

    boolean isScimProvEnabled = false;
    boolean isScimProvDefault = false;
    String scimUserName = null;
    String scimPassword = null;
    String scimGroupEp = null;
    String scimUserEp = null;
    String scimUserStoreDomain = null;
    boolean isSCIMPwdProvEnabled = false;
    String scimDefaultPwd = null;
    String disableDefaultPwd = "";
    String scimUniqueID = null;

    boolean isScim2ProvEnabled = false;
    boolean isScim2ProvDefault = false;
    String scim2UserName = null;
    String scim2Password = null;
    String scim2GroupEp = null;
    String scim2UserEp = null;
    String scim2UserStoreDomain = null;
    boolean isSCIM2PwdProvEnabled = false;
    String scim2DefaultPwd = null;
    String scim2disableDefaultPwd = "";
    String scim2UniqueID = null;

    IdentityProvider identityProvider = null;
    ProvisioningConnectorConfig[] provisioningConnectors = null;

    ProvisioningConnectorConfig salesforce = null;
    ProvisioningConnectorConfig scim = null;
    ProvisioningConnectorConfig scim2 = null;

    Map<String, UUID> idpUniqueIdMap = (Map<String, UUID>) session.getAttribute(IdPManagementUIUtil.IDP_LIST_UNIQUE_ID);

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
            } else if (provisioningConnector != null && "scim".equals(provisioningConnector.getName())) {
                scim = provisioningConnector;
            } else if (provisioningConnector != null && "SCIM2".equals(provisioningConnector.getName())) {
                scim2 = provisioningConnector;
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
                if (sfProperty != null) {
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
        }
        if (salesforce.getEnabled()) {
            isSfProvEnabled = true;
        }
    }

    if (scim != null) {
        if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
            isScimProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName().equals(scim.getName());
        }

        Property[] scimProperties = scim.getProvisioningProperties();
        if (scimProperties != null && scimProperties.length > 0) {
            for (Property scimProperty : scimProperties) {
                //This is a safety to check to avoid NPE
                if (scimProperty != null) {
                    if ("scim-username".equals(scimProperty.getName())) {
                        scimUserName = scimProperty.getValue();
                    } else if ("scim-password".equals(scimProperty.getName())) {
                        scimPassword = scimProperty.getValue();
                    } else if ("scim-user-ep".equals(scimProperty.getName())) {
                        scimUserEp = scimProperty.getValue();
                    } else if ("scim-group-ep".equals(scimProperty.getName())) {
                        scimGroupEp = scimProperty.getValue();
                    } else if ("scim-user-store-domain".equals(scimProperty.getName())) {
                        scimUserStoreDomain = scimProperty.getValue();
                    } else if ("scim-enable-pwd-provisioning".equals(scimProperty.getName())) {
                        isSCIMPwdProvEnabled = Boolean.parseBoolean(scimProperty.getValue());
                    } else if ("scim-default-pwd".equals(scimProperty.getName())) {
                        scimDefaultPwd = scimProperty.getValue();
                    } else if ("UniqueID".equals(scimProperty.getName())) {
                        scimUniqueID = scimProperty.getValue();
                    }
                }
            }
        }

        if (scim.getEnabled()) {
            isScimProvEnabled = true;
        }
    }

    if (scim2 != null) {
        if (identityProvider.getDefaultProvisioningConnectorConfig() != null
                && identityProvider.getDefaultProvisioningConnectorConfig().getName() != null) {
            isScim2ProvDefault = identityProvider.getDefaultProvisioningConnectorConfig().getName()
                    .equals(scim2.getName());
        }

        Property[] scim2Properties = scim2.getProvisioningProperties();
        if (scim2Properties != null && scim2Properties.length > 0) {
            for (Property scim2Property : scim2Properties) {
                //This is a safety to check to avoid NPE
                if (scim2Property != null) {
                    if ("scim2-username".equals(scim2Property.getName())) {
                        scim2UserName = scim2Property.getValue();
                    } else if ("scim2-password".equals(scim2Property.getName())) {
                        scim2Password = scim2Property.getValue();
                    } else if ("scim2-user-ep".equals(scim2Property.getName())) {
                        scim2UserEp = scim2Property.getValue();
                    } else if ("scim2-group-ep".equals(scim2Property.getName())) {
                        scim2GroupEp = scim2Property.getValue();
                    } else if ("scim2-user-store-domain".equals(scim2Property.getName())) {
                        scim2UserStoreDomain = scim2Property.getValue();
                    } else if ("scim2-enable-pwd-provisioning".equals(scim2Property.getName())) {
                        isSCIM2PwdProvEnabled = Boolean.parseBoolean(scim2Property.getValue());
                    } else if ("scim2-default-pwd".equals(scim2Property.getName())) {
                        scim2DefaultPwd = scim2Property.getValue();
                    } else if ("UniqueID".equals(scim2Property.getName())) {
                        scim2UniqueID = scim2Property.getValue();
                    }
                }
            }
        }

        if (scim2.getEnabled()) {
            isScim2ProvEnabled = true;
        }
    }

    String sfProvEnabledChecked = "";
    String sfProvDefaultDisabled = "";
    String sfProvDefaultChecked = "disabled=\'disabled\'";

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

    String scimProvEnabledChecked = "";
    String scimProvDefaultDisabled = "";
    String scimPwdProvEnabledChecked = "";
    String scimProvDefaultChecked = "disabled=\'disabled\'";
    if (identityProvider != null) {
        if (isScimProvEnabled) {
            scimProvEnabledChecked = "checked=\'checked\'";
            scimProvDefaultChecked = "";
            if (isScimProvDefault) {
                scimProvDefaultChecked = "checked=\'checked\'";
            }
        }
        if (isSCIMPwdProvEnabled) {
            scimPwdProvEnabledChecked = "checked=\'checked\'";
            disableDefaultPwd = "disabled=\'disabled\'";
        }
    }

    // If SCIM Provisioning has not been Configured at all,
    // make password provisioning enable by default.
    // Since scimUserName is a required field,
    // it being blank means that SCIM Provisioning has not been configured at all.
    if (scimUserName == null) {
        scimUserName = "";
        scimPwdProvEnabledChecked = "checked=\'checked\'";
        disableDefaultPwd = "disabled=\'disabled\'";
    }
    if (scimPassword == null) {
        scimPassword = "";
    }
    if (scimGroupEp == null) {
        scimGroupEp = "";
    }
    if (scimUserEp == null) {
        scimUserEp = "";
    }
    if (scimUserStoreDomain == null) {
        scimUserStoreDomain = "";
    }
    if (scimDefaultPwd == null) {
        scimDefaultPwd = "";
    }

    String scim2ProvEnabledChecked = "";
    String scim2ProvDefaultDisabled = "";
    String scim2PwdProvEnabledChecked = "";
    String scim2ProvDefaultChecked = "disabled=\'disabled\'";
    if (identityProvider != null) {
        if (isScim2ProvEnabled) {
            scim2ProvEnabledChecked = "checked=\'checked\'";
            scim2ProvDefaultChecked = "";
            if (isScim2ProvDefault) {
                scim2ProvDefaultChecked = "checked=\'checked\'";
            }
        }
        if (isSCIM2PwdProvEnabled) {
            scim2PwdProvEnabledChecked = "checked=\'checked\'";
            scim2disableDefaultPwd = "disabled=\'disabled\'";
        }
    }

    // If SCIM2 Provisioning has not been Configured at all,
    // make password provisioning enable by default.
    // Since scim2UserName is a required field,
    // it being blank means that SCIM2 Provisioning has not been configured at all.
    if (scim2UserName == null) {
        scim2UserName = "";
        scim2PwdProvEnabledChecked = "checked=\'checked\'";
        scim2disableDefaultPwd = "disabled=\'disabled\'";
    }
    if (scim2Password == null) {
        scim2Password = "";
    }
    if (scim2GroupEp == null) {
        scim2GroupEp = "";
    }
    if (scim2UserEp == null) {
        scim2UserEp = "";
    }
    if (scim2UserStoreDomain == null) {
        scim2UserStoreDomain = "";
    }
    if (scim2DefaultPwd == null) {
        scim2DefaultPwd = "";
    }
%>

<script>
    jQuery(document).ready(function () {
        if (<%=isSfProvEnabled%>) {
            jQuery('#sf_enable_logo').show();
        } else {
            jQuery('#sf_enable_logo').hide();
        }

        if (<%=isScimProvEnabled%>) {
            jQuery('#scim_enable_logo').show();
        } else {
            jQuery('#scim_enable_logo').hide();
        }

        if (<%=isScim2ProvEnabled%>) {
            jQuery('#scim2_enable_logo').show();
        } else {
            jQuery('#scim2_enable_logo').hide();
        }
    })
</script>

<fmt:bundle basename="org.wso2.carbon.idp.mgt.ui.i18n.Resources">

    <!-- Salesforce Connector -->
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

    <!-- SCIM Connector -->
    <h2 id="scim_prov_head" class="sectionSeperator trigger active"
        style="background-color: beige;">
        <a href="#"><fmt:message key="scim.provisioning.connector"/></a>

        <div id="scim_enable_logo" class="enablelogo"
             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                src="images/ok.png" alt="enable" width="16" height="16"></div>

    </h2>
    <div class="toggle_container sectionSub"
         style="margin-bottom: 10px; display: none;" id="scimProvRow">

        <table class="carbonFormTable">
            <tr>
                <td class="leftCol-med labelField"><label
                        for="scimProvEnabled"><fmt:message
                        key='scim.provisioning.enabled'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scimProvEnabled" name="scimProvEnabled"
                               type="checkbox" <%=scimProvEnabledChecked%>
                               onclick="checkProvEnabled(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='scim.provisioning.enabled.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr style="display:none;">
                <td class="leftCol-med labelField"><label
                        for="scimProvDefault"><fmt:message
                        key='scim.provisioning.default'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scimProvDefault" name="scimProvDefault"
                               type="checkbox" <%=scimProvDefaultChecked%>
                                <%=scimProvDefaultDisabled%>
                               onclick="checkProvDefault(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='scim.provisioning.default.help'/>
                        </span>
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim.provisioning.user.name'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim-username"
                           name="scim-username" type="text"
                           value=<%=Encode.forHtmlAttribute(scimUserName) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim.provisioning.user.password'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim-password"
                           name="scim-password" type="password" autocomplete="off"
                           value=<%=Encode.forHtmlAttribute(scimPassword) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim.provisioning.user.endpoint'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim-user-ep"
                           name="scim-user-ep" type="text"
                           value=<%=Encode.forHtmlAttribute(scimUserEp) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim.provisioning.group.endpoint'/>:
                </td>
                <td><input class="text-box-big" id="scim-group-ep"
                           name="scim-group-ep" type="text"
                           value=<%=Encode.forHtmlAttribute(scimGroupEp) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim.provisioning.userStore.domain'/>:
                </td>
                <td><input class="text-box-big" id="scim-user-store-domain"
                           name="scim-user-store-domain" type="text"
                           value=<%=Encode.forHtmlAttribute(scimUserStoreDomain)%>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><label><fmt:message
                        key='scim.password.provisioning.enabled'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scimPwdProvEnabled" name="scimPwdProvEnabled"
                               type="checkbox" <%=scimPwdProvEnabledChecked%>
                               onclick="disableDefaultPwd(this);"/>
                        <span style="display: inline-block" class="sectionHelp"> <fmt:message
                                key='scim.password.provisioning.enabled.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField">
                    <fmt:message key='scim.default.password'/>:
                </td>
                <td><input class="text-box-big" id="scim-default-pwd" <%=disableDefaultPwd%>
                           name="scim-default-pwd" type="text" value=<%=Encode.forHtmlAttribute(scimDefaultPwd)%>></td>
                <%if (scimUniqueID != null) {%>
                <input type="hidden" id="scim-unique-id" name="scim-unique-id"
                       value=<%=Encode.forHtmlAttribute(scimUniqueID)%>>
                <%}%>
            </tr>
        </table>
    </div>

    <!-- SCIM2 Connector -->
    <h2 id="scim2_prov_head" class="sectionSeperator trigger active"
        style="background-color: beige;">
        <a href="#"><fmt:message key="scim2.provisioning.connector"/></a>

        <div id="scim2_enable_logo" class="enablelogo"
             style="float:right;padding-right: 5px;padding-top: 5px;"><img
                src="images/ok.png" alt="enable" width="16" height="16"></div>

    </h2>
    <div class="toggle_container sectionSub"
         style="margin-bottom: 10px; display: none;" id="scim2ProvRow">

        <table class="carbonFormTable">
            <tr>
                <td class="leftCol-med labelField"><label
                        for="scim2ProvEnabled"><fmt:message
                        key='scim2.provisioning.enabled'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scim2ProvEnabled" name="scim2ProvEnabled"
                               type="checkbox" <%=scim2ProvEnabledChecked%>
                               onclick="checkProvEnabled(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='scim2.provisioning.enabled.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr style="display:none;">
                <td class="leftCol-med labelField"><label
                        for="scim2ProvDefault"><fmt:message
                        key='scim2.provisioning.default'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scim2ProvDefault" name="scim2ProvDefault"
                               type="checkbox" <%=scim2ProvDefaultChecked%>
                                <%=scim2ProvDefaultDisabled%>
                               onclick="checkProvDefault(this);"/> <span
                            style="display: inline-block" class="sectionHelp"> <fmt:message
                            key='scim2.provisioning.default.help'/>
                        </span>
                    </div>
                </td>
            </tr>

            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim2.provisioning.user.name'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim2-username"
                           name="scim2-username" type="text"
                           value=<%=Encode.forHtmlAttribute(scim2UserName) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim2.provisioning.user.password'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim2-password"
                           name="scim2-password" type="password" autocomplete="off"
                           value=<%=Encode.forHtmlAttribute(scim2Password) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim2.provisioning.user.endpoint'/>:<span
                        class="required">*</span></td>
                <td><input class="text-box-big" id="scim2-user-ep"
                           name="scim2-user-ep" type="text"
                           value=<%=Encode.forHtmlAttribute(scim2UserEp) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim2.provisioning.group.endpoint'/>:
                </td>
                <td><input class="text-box-big" id="scim2-group-ep"
                           name="scim2-group-ep" type="text"
                           value=<%=Encode.forHtmlAttribute(scim2GroupEp) %>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><fmt:message
                        key='scim2.provisioning.userStore.domain'/>:
                </td>
                <td><input class="text-box-big" id="scim2-user-store-domain"
                           name="scim2-user-store-domain" type="text"
                           value=<%=Encode.forHtmlAttribute(scim2UserStoreDomain)%>></td>
            </tr>
            <tr>
                <td class="leftCol-med labelField"><label><fmt:message
                        key='scim2.password.provisioning.enabled'/>:</label></td>
                <td>
                    <div class="sectionCheckbox">
                        <!-- -->
                        <input id="scim2PwdProvEnabled" name="scim2PwdProvEnabled"
                               type="checkbox" <%=scim2PwdProvEnabledChecked%>
                               onclick="scim2disableDefaultPwd(this);"/>
                        <span style="display: inline-block" class="sectionHelp"> <fmt:message
                                key='scim2.password.provisioning.enabled.help'/>
                        </span>
                    </div>
                </td>
            </tr>
            <tr>
                <td class="leftCol-med labelField">
                    <fmt:message key='scim2.default.password'/>:
                </td>
                <td><input class="text-box-big" id="scim2-default-pwd" <%=scim2disableDefaultPwd%>
                           name="scim2-default-pwd" type="text" value=<%=Encode.forHtmlAttribute(scim2DefaultPwd)%>></td>
                <%if (scim2UniqueID != null) {%>
                <input type="hidden" id="scim2-unique-id" name="scim2-unique-id"
                       value=<%=Encode.forHtmlAttribute(scim2UniqueID)%>>
                <%}%>
            </tr>
        </table>
    </div>
</fmt:bundle>