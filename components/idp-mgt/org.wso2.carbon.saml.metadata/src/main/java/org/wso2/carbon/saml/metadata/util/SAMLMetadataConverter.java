/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.saml.metadata.util;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.SAML2SSOFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderSAMLException;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.saml.metadata.builder.DefaultIDPMetadataBuilder;
import org.wso2.carbon.saml.metadata.builder.IDPMetadataBuilder;


/**
 * Created by pasindutennage on 9/27/16.
 */
public class SAMLMetadataConverter implements MetadataConverter {

    /**
     * Retrieves whether this property contains SAML Metadata     *
     *
     * @param property
     * @return boolean     *
     */
    public boolean canHandle(Property property) {
        if (property != null) {
            String meta = property.getName();
            if (meta != null && meta.contains("saml")) {
                if (property.getValue() != null && property.getValue().length() > 0){
                    return true;
                }
                    return false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Compares original federatedAuthenticationConfig and the meta data generated FederationAuthenticatorConfig and returns the final federatedAuthenticatorConfig object
     *
     * @param original,metaPassed
     * @return FederatedAuthenticatorConfig     *
     */
    private FederatedAuthenticatorConfig validate(FederatedAuthenticatorConfig original, FederatedAuthenticatorConfig metaPassed) {
        Property propertiesOriginal[] = original.getProperties();//size 25
        Property propertiesMetadata[] = metaPassed.getProperties();//size 24
        for (int i = 0; i < propertiesMetadata.length; i++) {
            Property propertyMetadata = propertiesMetadata[i];
            if (propertyMetadata != null) {
                String propertyMetaName = propertyMetadata.getName();
                String propertyMetaValue = propertyMetadata.getValue();
                if (propertyMetaName != null && propertyMetaName.length() > 0) {
                    for (int j = 0; j < propertiesOriginal.length; j++) {
                        Property propertyOrigin = propertiesOriginal[j];
                        if (propertyOrigin != null) {
                            String propertyOriginName = propertyOrigin.getName();
                            String propertyOriginValue = propertyOrigin.getValue();
                            if (propertyOriginName != null && propertyOriginName.equals(propertyMetaName)) {
                                if (propertyMetaValue == null || propertyMetaValue.equals("")) {
                                    propertiesMetadata[i].setValue(propertyOriginValue);
                                } else if (propertyOriginValue != null && propertyOriginValue.length() > 0) {
                                    propertiesMetadata[i].setValue(propertyOriginValue);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        original.setProperties(propertiesMetadata);
        return original;
    }

    /**
     * Returns a FederatuedAuthenticatorConfigObject that is generated using metadata
     *
     * @param properties,builder
     * @return FederatedAuthenticatorConfig
     * @throws javax.xml.stream.XMLStreamException, IdentityProviderManagementException
     */
    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfig(Property properties [], StringBuilder builder) throws javax.xml.stream.XMLStreamException, IdentityProviderManagementException {

        String spName = "";
        String metadata = "";
        for (int y = 0; y < properties.length; y++) {
            if (properties[y] != null && properties[y].getName() != null && properties[y].getName().toString().equals(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)) {
                spName = properties[y].getValue();
            }
            if (properties[y] != null && properties[y].getName() != null && properties[y].getName().toString().equals("meta_data_saml")) {
                metadata = properties[y].getValue();
            }
        }
        if (spName.equals("")) {
            throw new IdentityProviderManagementException("SP name can't be empty");
        }
        if (metadata.equals("")) {
            throw new IdentityProviderManagementException("No metadata found");
        }

        OMElement element;
        try {
            element = AXIOMUtil.stringToOM(metadata);
        } catch (javax.xml.stream.XMLStreamException ex) {
            throw new javax.xml.stream.XMLStreamException("Invalid metadata content, Failed to convert to OMElement");
        }
        FederatedAuthenticatorConfig federatedAuthenticatorConfigMetadata;
        try {
            federatedAuthenticatorConfigMetadata = SAML2SSOFederatedAuthenticatorConfig.build(element, builder);
        } catch (IdentityApplicationManagementException ex) {
            throw new IdentityProviderManagementException("Invalid file content");
        }
        if(federatedAuthenticatorConfigMetadata!=null && ArrayUtils.isNotEmpty(federatedAuthenticatorConfigMetadata.getProperties())) {
            for (int y = 0; y < federatedAuthenticatorConfigMetadata.getProperties().length; y++) {
                if (federatedAuthenticatorConfigMetadata.getProperties()[y] != null && federatedAuthenticatorConfigMetadata.getProperties()[y].getName() != null && federatedAuthenticatorConfigMetadata.getProperties()[y].getName().toString().equals(IdentityApplicationConstants.Authenticator.SAML2SSO.SP_ENTITY_ID)) {
                    federatedAuthenticatorConfigMetadata.getProperties()[y].setValue(spName);
                    break;
                }
            }
        }
        return federatedAuthenticatorConfigMetadata;
    }


    public String getMetadataString(FederatedAuthenticatorConfig federatedAuthenticatorConfig) throws IdentityProviderSAMLException{

        DefaultIDPMetadataBuilder builder = new DefaultIDPMetadataBuilder();
        try {

            String metadata = builder.build(federatedAuthenticatorConfig);
            return metadata;
        }catch(MetadataException ex){
            throw  new IdentityProviderSAMLException("Error invoking build in IDPMetadataBuilder");
        }



    }


}
