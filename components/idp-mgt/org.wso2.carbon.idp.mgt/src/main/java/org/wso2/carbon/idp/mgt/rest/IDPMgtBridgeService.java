/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.util.ArrayList;
import java.util.List;

public class IDPMgtBridgeService {

    private static final Log log = LogFactory.getLog(IdentityProviderManager.class);

    private static IDPMgtBridgeService instance = new IDPMgtBridgeService();

    private IdentityProviderManager identityProviderManager = IdentityProviderManager.getInstance();

    private IDPMgtBridgeService() {

    }

    /**
     * @return
     */
    public static IDPMgtBridgeService getInstance() {

        return instance;
    }

    public ExtendedIdentityProvider getIDPById(String id, String tenantDomain) throws
            IdentityProviderManagementException, IDPMgtBridgeServiceException {

        IdentityProvider idp = identityProviderManager.getIdPById(id, tenantDomain);
        if (idp == null) {
            throw new IDPMgtBridgeServiceException("404", "IDP not found", "IDP not found");
        }

        return new ExtendedIdentityProvider(idp);
    }

    public List<ExtendedIdentityProvider> getIDPs(String tenantDomain) throws
            IdentityProviderManagementException, IDPMgtBridgeServiceException {

        List<IdentityProvider> idPs = identityProviderManager.getIdPs(tenantDomain);

        return getExtendedIDPList(idPs);
    }

    private List<ExtendedIdentityProvider> getExtendedIDPList(List<IdentityProvider> identityProviderList) {

        List<ExtendedIdentityProvider> extendedIdentityProviders = new ArrayList<>();

        for (IdentityProvider idp : identityProviderList) {
            extendedIdentityProviders.add(new ExtendedIdentityProvider(idp));
        }
        return extendedIdentityProviders;
    }
}
