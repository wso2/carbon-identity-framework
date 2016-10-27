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
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.IdentityRegistryResources;
import org.wso2.carbon.identity.metadata.saml.builder.DefaultIDPMetadataBuilder;
import org.wso2.carbon.identity.metadata.saml.internal.IDPMetadataSAMLServiceComponentHolder;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderSAMLException;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;

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

    /**
     * Deletes an IDP metadata registry component if exists
     *
     * @param idPName , tennantId
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                             information from registry
     */
    public void deleteMetadataString(int tenantId, String idPName) throws IdentityProviderManagementException {
        try {

            UserRegistry registry = IDPMetadataSAMLServiceComponentHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
            String samlIdpPath = IdentityRegistryResources.SAMLIDP;
            String path = samlIdpPath + idPName;

            try {

                if (registry.resourceExists(path)) {
                    boolean isTransactionStarted = Transaction.isStarted();
                    try {

                        if (!isTransactionStarted) {
                            registry.beginTransaction();
                        }

                        registry.delete(path);

                        if (!isTransactionStarted) {
                            registry.commitTransaction();
                        }

                    } catch (RegistryException e) {
                        if (!isTransactionStarted) {
                            registry.rollbackTransaction();
                        }
                        throw new IdentityProviderManagementException("Error while deleting metadata String in registry for " + idPName);
                    }


                }
            } catch (RegistryException e) {
                throw new IdentityProviderManagementException("Error while deleting Identity Provider", e);
            }




        } catch (RegistryException e) {
            throw new IdentityProviderManagementException("Error while setting a registry object in IdentityProviderManager");
        }

    }

    /**
     * Updates an IDP metadata registry component
     *
     * @param idpName , tennantId, metadata
     * @throws IdentityProviderManagementException Error when deleting Identity Provider
     *                                             information from registry
     */
    public  void saveMetadataString(int tenantId, String idpName, String metadata) throws IdentityProviderManagementException {

        try {

            UserRegistry registry = IDPMetadataSAMLServiceComponentHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
            String identityProvidersPath = IdentityRegistryResources.IDENTITYPROVIDER;
            String samlIdpPath = IdentityRegistryResources.SAMLIDP;
            String path = samlIdpPath + idpName;
            Resource resource;
            resource = registry.newResource();
            resource.setContent(metadata);


            boolean isTransactionStarted = Transaction.isStarted();
            if (!isTransactionStarted) {
                registry.beginTransaction();
            }

            try {
                if (!registry.resourceExists(identityProvidersPath)) {

                    org.wso2.carbon.registry.core.Collection idpCollection = registry.newCollection();
                    registry.put(identityProvidersPath, idpCollection);

                }
                if (!registry.resourceExists(samlIdpPath)) {

                    org.wso2.carbon.registry.core.Collection samlIdpCollection = registry.newCollection();
                    registry.put(samlIdpPath, samlIdpCollection);

                }
                if (!registry.resourceExists(path)) {
                    registry.put(path, resource);
                } else {
                    registry.delete(path);
                    registry.put(path, resource);
                }

                if (!isTransactionStarted) {
                    registry.commitTransaction();
                }
            } catch (RegistryException e) {

                if (!isTransactionStarted) {
                    registry.rollbackTransaction();
                }

                throw new IdentityProviderManagementException("Error while creating resource in registry");
            }

        } catch (RegistryException e) {
            throw new IdentityProviderManagementException("Error while setting a registry object in IdentityProviderManager");
        }
    }


}
