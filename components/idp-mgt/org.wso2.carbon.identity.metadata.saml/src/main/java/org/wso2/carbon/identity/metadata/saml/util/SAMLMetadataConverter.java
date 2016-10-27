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
package org.wso2.carbon.identity.metadata.saml.util;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.SAML2SSOFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.metadata.saml.builder.DefaultIDPMetadataBuilder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderSAMLException;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;


/**
 * This class implements the SAML metadata functionality to convert string to FederatedAuthenticator config nad vise versa
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
     * Returns a FederatuedAuthenticatorConfigObject that is generated using metadata
     *
     * @param properties,builder
     * @return FederatedAuthenticatorConfig
     * @throws javax.xml.stream.XMLStreamException, IdentityProviderManagementException
     */
    public FederatedAuthenticatorConfig getFederatedAuthenticatorConfig(Property properties [], StringBuilder builder) throws javax.xml.stream.XMLStreamException, IdentityProviderManagementException {


        String metadata = "";
        for (int y = 0; y < properties.length; y++) {

            if (properties[y] != null && properties[y].getName() != null && properties[y].getName().toString().equals("meta_data_saml")) {
                metadata = properties[y].getValue();
            }
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
            federatedAuthenticatorConfigMetadata = SAML2SSOFederatedAuthenticatorConfigBuilder.build(element, builder);
        } catch (IdentityApplicationManagementException ex) {//TODO check this
            throw new IdentityProviderManagementException("Invalid file content");
        }

        return federatedAuthenticatorConfigMetadata;
    }


    public String getMetadataString(FederatedAuthenticatorConfig federatedAuthenticatorConfig) throws IdentityProviderSAMLException{

        DefaultIDPMetadataBuilder builder = new DefaultIDPMetadataBuilder();
        try {

            String metadata = builder.build(federatedAuthenticatorConfig);
            return metadata;
        }catch(MetadataException ex){
            throw  new IdentityProviderSAMLException("Error invoking build in IDPMetadataBuilder", ex);
        }

    }
    public boolean canHandle(FederatedAuthenticatorConfig federatedAuthenticatorConfig){
        if(federatedAuthenticatorConfig!=null && federatedAuthenticatorConfig.getName()
                .equals(IdentityApplicationConstants.Authenticator.SAML2SSO.NAME)){
            return true;
        }
        return false;
    }

}
