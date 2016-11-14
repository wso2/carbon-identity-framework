package org.wso2.carbon.idp.mgt.util;/*
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

import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderSAMLException;
/**
 * This interface provides functionality to convert a string to federatedAuthenticatorConfig and vise versa
 */
public interface MetadataConverter {

    boolean canHandle(Property property);

    FederatedAuthenticatorConfig getFederatedAuthenticatorConfig(Property properties [], StringBuilder builder)
            throws IdentityProviderManagementException, javax.xml.stream.XMLStreamException;//TODO

    String getMetadataString(FederatedAuthenticatorConfig federatedAuthenticatorConfig) throws IdentityProviderSAMLException;

    boolean canHandle(FederatedAuthenticatorConfig federatedAuthenticatorConfig);

    void saveMetadataString(int tenantId, String idpName, String metadata) throws IdentityProviderManagementException;

    void deleteMetadataString(int tenantId, String idPName)throws IdentityProviderManagementException;

}