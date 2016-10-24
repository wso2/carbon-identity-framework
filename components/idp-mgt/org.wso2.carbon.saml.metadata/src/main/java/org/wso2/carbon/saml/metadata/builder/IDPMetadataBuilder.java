/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.saml.metadata.builder;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.SAML2SSOFederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.core.handler.AbstractIdentityHandler;
import org.wso2.carbon.idp.mgt.MetadataException;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

public abstract class IDPMetadataBuilder extends AbstractIdentityHandler {


    public String build(FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig) throws MetadataException {


        EntityDescriptor entityDescriptor = buildEntityDescriptor(samlFederatedAuthenticatorConfig);
        IDPSSODescriptor idpSsoDesc = buildIDPSSODescriptor();
        buildValidityPeriod(idpSsoDesc);
        buildSupportedProtocol(idpSsoDesc);

        buildSingleSignOnService(idpSsoDesc, samlFederatedAuthenticatorConfig);
        buildNameIdFormat(idpSsoDesc);
        buildSingleLogOutService(idpSsoDesc, samlFederatedAuthenticatorConfig);
        entityDescriptor.getRoleDescriptors().add(idpSsoDesc);
        buildKeyDescriptor(entityDescriptor);
        buildExtensions(idpSsoDesc);
        buildContact(idpSsoDesc);

        return marshallDescriptor(entityDescriptor);
    }

    private FederatedAuthenticatorConfig getSAMLFederatedAuthenticatorConfig(IdentityProvider identityProvider) {
        for (FederatedAuthenticatorConfig config : identityProvider.getFederatedAuthenticatorConfigs()) {
            if (IdentityApplicationConstants.Authenticator.SAML2SSO.NAME.equals(config.getName())) {
                return config;
            }
        }
        return null;
    }

    protected abstract EntityDescriptor buildEntityDescriptor(FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig ) throws MetadataException;

    protected abstract IDPSSODescriptor buildIDPSSODescriptor() throws MetadataException;

    protected abstract void buildValidityPeriod(IDPSSODescriptor idpSsoDesc) throws MetadataException;

    protected abstract void buildSupportedProtocol(IDPSSODescriptor idpSsoDesc) throws MetadataException;

    protected abstract void buildKeyDescriptor(EntityDescriptor entityDescriptor) throws MetadataException;

    protected abstract void buildNameIdFormat(IDPSSODescriptor idpSsoDesc) throws MetadataException;

    protected abstract void buildSingleSignOnService(IDPSSODescriptor idpSsoDesc, FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig) throws MetadataException;

    protected abstract void buildSingleLogOutService(IDPSSODescriptor idpSsoDesc, FederatedAuthenticatorConfig samlFederatedAuthenticatorConfig) throws MetadataException;

    protected abstract void buildExtensions(IDPSSODescriptor idpSsoDesc) throws MetadataException;

    protected abstract void buildContact(IDPSSODescriptor idpSsoDesc) throws MetadataException;

    protected abstract String marshallDescriptor(EntityDescriptor entityDescriptor) throws MetadataException;

}
