/*
 * Copyright (c) 2014-2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.idp.mgt.dao;

import org.apache.commons.lang.ArrayUtils;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;
import org.wso2.carbon.idp.mgt.internal.IdPManagementServiceComponent;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FileBasedIdPMgtDAO {

    /**
     * @param idPName
     * @param tenantDomain
     * @return
     */
    public IdentityProvider getIdPByName(String idPName, String tenantDomain) {
        return IdPManagementServiceComponent.getFileBasedIdPs().get(idPName);
    }

    /**
     * @param property
     * @param value
     * @param tenantDomain
     * @return
     */
    public IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain) {

        return getIdPByAuthenticatorPropertyValue(property, value, tenantDomain, IdentityApplicationConstants
                .Authenticator.SAML2SSO.NAME);
    }

    public IdentityProvider getIdPByAuthenticatorPropertyValue(String property, String value, String tenantDomain,
                                                               String authenticatorName) {

        Map<String, IdentityProvider> identityProviders = IdPManagementServiceComponent.getFileBasedIdPs();
        for (Entry<String, IdentityProvider> entry : identityProviders.entrySet()) {
            FederatedAuthenticatorConfig[] federatedAuthenticatorConfigs = entry.getValue().
                    getFederatedAuthenticatorConfigs();
            // Get SAML2 Web SSO authenticator
            FederatedAuthenticatorConfig samlAuthenticatorConfig = IdentityApplicationManagementUtil.
                    getFederatedAuthenticator(federatedAuthenticatorConfigs, authenticatorName);
            if (samlAuthenticatorConfig != null) {
                Property samlProperty = IdentityApplicationManagementUtil.getProperty(samlAuthenticatorConfig.
                        getProperties(), property);
                if (samlProperty != null) {
                    if (value.equalsIgnoreCase(samlProperty.getValue())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param realmId
     * @param tenantDomain
     * @return
     */
    public IdentityProvider getIdPByRealmId(String realmId, String tenantDomain) {

        Map<String, IdentityProvider> map = IdPManagementServiceComponent.getFileBasedIdPs();

        for (Iterator<Entry<String, IdentityProvider>> iterator = map.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, IdentityProvider> entry = iterator.next();
            if (entry.getValue().getHomeRealmId() != null
                    && entry.getValue().getHomeRealmId().equals(realmId)) {
                return entry.getValue();
            }

        }

        return null;
    }

    /**
     * Retrieves the first matching IDP for the given metadata property.
     * Intended to ony be used to retrieve IDP name based on a unique metadata property.
     *
     * @param property IDP metadata property name.
     * @param value Value associated with given Property.
     * @return Identity Provider name.
     */
    public String getIdPNameByMetadataProperty(String property, String value) {

        Map<String, IdentityProvider> identityProviders = IdPManagementServiceComponent.getFileBasedIdPs();
        for (Entry<String, IdentityProvider> entry : identityProviders.entrySet()) {
            IdentityProviderProperty[] identityProviderProperties = entry.getValue().getIdpProperties();
            if (!ArrayUtils.isEmpty(identityProviderProperties)) {
                for (IdentityProviderProperty prop : identityProviderProperties) {
                    if (prop != null && property.equals(prop.getName()) && value.equals(prop.getValue())) {
                        return entry.getValue().getIdentityProviderName();
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method is used get the IDP for file based applications where the resource id is the IDP name.
     *
     * @param resourceId Resource ID which will be the IDP name for file based.
     * @param tenantDomain Tenant domain of IDP.
     * @return Identity Provider.
     */
    public IdentityProvider getIdPByResourceId(String resourceId, String tenantDomain) {

        // For the File based applications, application name is set as resource id.
        return getIdPByName(resourceId, tenantDomain);
    }
}
