/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.common.model;

import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.*;
import org.opensaml.saml2.metadata.provider.DOMMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.parse.BasicParserPool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class SAML2SSOFederatedAuthenticatorConfig extends FederatedAuthenticatorConfig {
    private static Log log = LogFactory.getLog(SAML2SSOFederatedAuthenticatorConfig.class);

    /**
     * Convert metadata String to entityDescriptor
     *
     * @param metadataString
     * @return EntityDescriptor
     */
    private static EntityDescriptor generateMetadataObjectFromString(String metadataString) throws IdentityApplicationManagementException {
        EntityDescriptor entityDescriptor = null;
        try {
            DocumentBuilderFactory factory = IdentityUtil.getSecuredDocumentBuilderFactory();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(metadataString.getBytes()));
            Element node = document.getDocumentElement();
            DOMMetadataProvider idpMetaDataProvider = new DOMMetadataProvider(node);
            idpMetaDataProvider.setRequireValidMetadata(true);
            idpMetaDataProvider.setParserPool(new BasicParserPool());
            idpMetaDataProvider.initialize();
            XMLObject xmlObject = idpMetaDataProvider.getMetadata();
            entityDescriptor = (EntityDescriptor) xmlObject;
        } catch (MetadataProviderException | SAXException | ParserConfigurationException | IOException e) {
            throw new IdentityApplicationManagementException("Error while converting file content to entity descriptor");
        }
        return entityDescriptor;
    }

    /**
     * Set the values of SamlSSOFederatedAuthenticationConfig from entitydescriptor
     *
     * @param entityDescriptor ,federatedAuthenticatorConfig,builder
     * @return FederatedAuthenticatorConfig
     */
    private static FederatedAuthenticatorConfig parse(EntityDescriptor entityDescriptor, FederatedAuthenticatorConfig federatedAuthenticatorConfig, StringBuilder builder) throws IdentityApplicationManagementException {

        if (entityDescriptor != null) {
            List<RoleDescriptor> roleDescriptors = entityDescriptor.getRoleDescriptors();
            //assuming only one IDPSSO is inside the entitydescripter
            if (CollectionUtils.isNotEmpty(roleDescriptors)) {
                RoleDescriptor roleDescriptor = roleDescriptors.get(0);
                if (roleDescriptor != null) {
                    IDPSSODescriptor idpssoDescriptor;
                    try {
                        idpssoDescriptor = (IDPSSODescriptor) roleDescriptor;
                    } catch (ClassCastException ex) {
                        throw new IdentityApplicationManagementException("No IDP Descriptors found, invalid file content");
                    }
                    Property properties[] = new Property[24];

                    Property property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID);
                    if (entityDescriptor.getEntityID() != null) {
                        property.setValue(entityDescriptor.getEntityID());
                    } else {
                        property.setValue("");
                        throw new IdentityApplicationManagementException("No Entity ID found, invalid file content");
                    }
                    properties[0] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID);
                    property.setValue("");//not available in the metadata specification
                    properties[1] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL);
                    List<SingleSignOnService> singleSignOnServices = idpssoDescriptor.getSingleSignOnServices();
                    if (CollectionUtils.isNotEmpty(singleSignOnServices)) {
                        boolean found = false;
                        for (int j = 0; j < singleSignOnServices.size(); j++) {
                            SingleSignOnService singleSignOnService = singleSignOnServices.get(j);
                            if (singleSignOnService != null) {
                                if (singleSignOnService.getLocation() != null) {
                                    property.setValue(singleSignOnService.getLocation());
                                    found = true;
                                    break;
                                }
                            }
                        }
                        if (!found) {
                            property.setValue("");
                            throw new IdentityApplicationManagementException("No SSO URL, invalid file content");
                        }
                    } else {
                        property.setValue("");
                        throw new IdentityApplicationManagementException("No SSO URL, invalid file content");
                    }
                    properties[2] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED);
                    if (idpssoDescriptor.getWantAuthnRequestsSigned() != null && idpssoDescriptor.getWantAuthnRequestsSigned() == true) {
                        property.setValue("true");
                    } else {
                        property.setValue("false");
                    }
                    properties[3] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED);
                    List<SingleLogoutService> singleLogoutServices = idpssoDescriptor.getSingleLogoutServices();
                    if (CollectionUtils.isNotEmpty(singleLogoutServices)) {
                        property.setValue("true");
                    } else {
                        property.setValue("false");
                    }
                    properties[4] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL);
                    if (CollectionUtils.isNotEmpty(singleLogoutServices)) {
                        boolean foundSingleLogoutServicePostBinding = false;
                        for (SingleLogoutService singleLogoutService : singleLogoutServices) {
                            if (singleLogoutService != null) {
                                if (singleLogoutService.getBinding() != null && singleLogoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI) && singleLogoutService.getLocation() != null) {
                                    property.setValue(singleLogoutService.getLocation());
                                    foundSingleLogoutServicePostBinding = true;
                                    break;
                                }
                            }
                        }
                        if (!foundSingleLogoutServicePostBinding) {
                            for (SingleLogoutService singleLogoutService : singleLogoutServices) {
                                if (singleLogoutService != null) {
                                    if (singleLogoutService.getBinding() != null && singleLogoutService.getLocation() != null) {
                                        property.setValue(singleLogoutService.getLocation());
                                        foundSingleLogoutServicePostBinding = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!foundSingleLogoutServicePostBinding) {
                            property.setValue("");
                        }
                    } else {
                        property.setValue("");
                    }
                    properties[5] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED);
                    property.setValue("");//not found in the metadata spec
                    //Not found in the Metadata Spec
                    properties[6] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED);
                    property.setValue("");//not found in the metadata spec
                    properties[7] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_USER_ID_IN_CLAIMS);
                    property.setValue("");//not found in the metadata spec
                    properties[8] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION);
                    property.setValue("");//not found in the metadata spec
                    properties[9] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_SIGNING);
                    property.setValue("");///not found in the metadata spec
                    properties[10] = property;

                    List<KeyDescriptor> descriptors = idpssoDescriptor.getKeyDescriptors();
                    if (CollectionUtils.isNotEmpty(descriptors)) {
                        for (int i = 0; i < descriptors.size(); i++) {
                            KeyDescriptor descriptor = descriptors.get(i);
                            if (descriptor != null) {
                                String use = "";
                                try {
                                    use = descriptor.getUse().name().toString();
                                } catch (Exception ex) {
                                    log.error("Error !!!!", ex);
                                }
                                if (use != null && use.equals("SIGNING")) {
                                    properties[10].setValue("true");
                                } else if (use != null && use.equals("ENCRYPTION")) {
                                    properties[9].setValue("true");
                                }
                            }
                        }
                    }

                    property = new Property();
                    property.setName("commonAuthQueryParams");//SAML querry param in the gui
                    property.setValue("");//not found in the metadata spec
                    properties[11] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.REQUEST_METHOD);
                    property.setValue("");//not found in the metadata spec
                    properties[12] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM);
                    property.setValue("");//not found in the metadata spec
                    properties[13] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.DIGEST_ALGORITHM);
                    property.setValue("");//not found in the metadata spec
                    properties[14] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_COMPARISON_LEVEL);
                    property.setValue("");//not found in the metadata spec
                    properties[15] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_NAME_ID_POLICY);
                    property.setValue("");//not found in the metadata spec
                    properties[16] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.FORCE_AUTHENTICATION);
                    property.setValue("");//not found in the metadata spec
                    properties[17] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.SIGNATURE_ALGORITHM_POST);
                    property.setValue("");//not found in the metadata spec
                    properties[18] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.AUTHENTICATION_CONTEXT_CLASS);
                    property.setValue("");//not found in the metadata spec
                    properties[19] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.ATTRIBUTE_CONSUMING_SERVICE_INDEX);
                    property.setValue("");//not found in the metadata spec
                    properties[20] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_CERT);
                    property.setValue("");//not found in the metadata spec
                    properties[21] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_AUTHN_CONTEXT);
                    property.setValue("");//not found in the metadata spec
                    properties[22] = property;

                    property = new Property();
                    property.setName(IdentityApplicationConstants.Authenticator.SAML2SSO.INCLUDE_PROTOCOL_BINDING);
                    property.setValue("");//not found in the metadata spec
                    properties[23] = property;

                    federatedAuthenticatorConfig.setProperties(properties);

                    //set certificates
                    if (CollectionUtils.isNotEmpty(descriptors)) {
                        for (int i = 0; i < descriptors.size(); i++) {
                            KeyDescriptor descriptor = descriptors.get(i);
                            if (descriptor != null) {
                                if (descriptor.getUse() != null && descriptor.getUse().toString().equals("SIGNING")) {
                                    try {
                                        String cert = null;
                                        if (descriptor.getKeyInfo() != null) {
                                            if (descriptor.getKeyInfo().getX509Datas() != null && descriptor.getKeyInfo().getX509Datas().size() > 0) {
                                                for (int k = 0; k < descriptor.getKeyInfo().getX509Datas().size(); k++) {
                                                    if (descriptor.getKeyInfo().getX509Datas().get(k) != null) {
                                                        if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates() != null &&
                                                                descriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().size() > 0) {
                                                            for (int y = 0; y < descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().size(); y++) {
                                                                if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y) != null) {
                                                                    if (descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y).
                                                                            getValue() != null && descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().
                                                                            get(y).getValue().length() > 0) {
                                                                        cert = descriptor.getKeyInfo().getX509Datas().get(k).getX509Certificates().get(y).
                                                                                getValue().toString();
                                                                        builder.append(org.apache.axiom.om.util.Base64.encode(cert.getBytes()));
                                                                        return federatedAuthenticatorConfig;
                                                                    }
                                                                }
                                                            }
                                                        }

                                                    }
                                                }
                                            }
                                        }
                                    } catch (java.lang.Exception ex) {
                                        log.error("Error While setting Certificate", ex);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                throw new IdentityApplicationManagementException("No Role Descriptors found, invalid file content");
            }
        }
        return federatedAuthenticatorConfig;
    }

    /**
     * Convert metadata OMElement to FederatedAuthenticatorConfigobject
     *
     * @param saml2FederatedAuthenticatorConfigOM ,builder
     * @return FederatedAuthenticatorConfig
     */

    public static FederatedAuthenticatorConfig build(OMElement saml2FederatedAuthenticatorConfigOM, StringBuilder builder) throws IdentityApplicationManagementException {

        FederatedAuthenticatorConfig federatedAuthenticatorConfig = new FederatedAuthenticatorConfig();
        EntityDescriptor entityDescriptor = generateMetadataObjectFromString(saml2FederatedAuthenticatorConfigOM.toString());
        if (entityDescriptor != null) {
            federatedAuthenticatorConfig = parse(entityDescriptor, federatedAuthenticatorConfig, builder);
        } else {
            throw new IdentityApplicationManagementException("Error while trying to convert to metadata, Invalid file content");
        }
        return federatedAuthenticatorConfig;
    }

    /**
     *
     */
    private static final long serialVersionUID = -171672098979315832L;

    /**
     * The IdP's Entity Issuer value
     */
    private String idpEntityId;

    /**
     * If Single Logout is enabled
     */
    private boolean isLogoutEnabled;

    /**
     * The SAML2 Web SSO URL of the IdP
     */
    private String saml2SSOUrl;

    /**
     * If LogoutRequest should be signed
     */
    private boolean isLogoutRequestSigned;

    /**
     * If the LogoutRequestUrl is different from ACS URL
     */
    private String logoutRequestUrl;

    /*
     * The service provider's Entity Id
     */
    private String spEntityId;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnRequestSigned;

    /**
     * If the AuthnRequest has to be signed
     */
    private boolean isAuthnResponseSigned;

    /**
     * If the AuthnResponse has to be encrypted
     */
    private boolean isAuthnResponseEncrypted;

    /**
     * If User ID is found among claims
     */
    private boolean isUserIdInClaims;

    public SAML2SSOFederatedAuthenticatorConfig(FederatedAuthenticatorConfig federatedAuthenticatorConfig) {
        for (Property property : federatedAuthenticatorConfig.getProperties()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID.equals(property.getName())) {
                idpEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID.equals(property.getName())) {
                spEntityId = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL.equals(property.getName())) {
                saml2SSOUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_REQ_SIGNED.equals(
                    property.getName())) {
                isAuthnRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_ENABLED.equals(
                    property.getName())) {
                isLogoutEnabled = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_LOGOUT_REQ_SIGNED.equals(
                    property.getName())) {
                isLogoutRequestSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.LOGOUT_REQ_URL.equals(property.getName())) {
                logoutRequestUrl = property.getValue();
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_AUTHN_RESP_SIGNED.equals(
                    property.getName())) {
                isAuthnResponseSigned = Boolean.parseBoolean(property.getValue());
            } else if (IdentityApplicationConstants.Authenticator.SAML2SSO.IS_ENABLE_ASSERTION_ENCRYPTION.equals(
                    property.getName())) {
                isAuthnResponseEncrypted = Boolean.parseBoolean(property.getValue());
            }
        }
    }

    @Override
    public boolean isValid() {

        if (IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.IDP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID))
                && IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL) != null
                && !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.SAML2SSO.SSO_URL))) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return IdentityApplicationConstants.Authenticator.SAML2SSO.NAME;
    }

    ////////////////////////////// Getters ///////////////////////////

    public String getIdpEntityId() {
        return idpEntityId;
    }

    public boolean isLogoutEnabled() {
        return isLogoutEnabled;
    }

    public boolean isLogoutRequestSigned() {
        return isLogoutRequestSigned;
    }

    public String getLogoutRequestUrl() {
        return logoutRequestUrl;
    }

    public String getSpEntityId() {
        return spEntityId;
    }

    public boolean isAuthnRequestSigned() {
        return isAuthnRequestSigned;
    }

    public boolean isAuthnResponseSigned() {
        return isAuthnResponseSigned;
    }

    public boolean isUserIdInClaims() {
        return isUserIdInClaims;
    }

    public String getSaml2SSOUrl() {
        return saml2SSOUrl;
    }

    public boolean isAuthnResponseEncrypted() {
        return isAuthnResponseEncrypted;
    }
}
