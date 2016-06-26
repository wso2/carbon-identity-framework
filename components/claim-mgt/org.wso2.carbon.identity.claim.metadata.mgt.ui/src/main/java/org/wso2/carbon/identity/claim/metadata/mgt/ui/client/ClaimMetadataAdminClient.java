/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.identity.claim.metadata.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceStub;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ExternalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;

import java.rmi.RemoteException;

/**
 * This class invokes the operations of ClaimMetadataManagementService.
 */
public class ClaimMetadataAdminClient {

    private static final Log log = LogFactory.getLog(ClaimMetadataAdminClient.class);
    private ClaimMetadataManagementServiceStub stub;

    /**
     * Instantiates ClaimMetadataAdminClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where ClaimManagementServiceStub is running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault if error occurs when instantiating the stub
     */
    public ClaimMetadataAdminClient(String cookie, String backendServerURL, ConfigurationContext configCtx) throws
            AxisFault {

        String serviceURL = backendServerURL + "ClaimMetadataManagementService";
        stub = new ClaimMetadataManagementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }


    public ClaimDialectDTO[] getClaimDialects() throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            return stub.getClaimDialects();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void addClaimDialect(ClaimDialectDTO externalClaimDialect) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.addClaimDialect(externalClaimDialect);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void removeClaimDialect(String externalClaimDialect) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            ClaimDialectDTO claimDialect = new ClaimDialectDTO();
            claimDialect.setClaimDialectURI(externalClaimDialect);
            stub.removeClaimDialect(claimDialect);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    public LocalClaimDTO[] getLocalClaims() throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            return stub.getLocalClaims();
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void addLocalClaim(LocalClaimDTO localCLaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.addLocalClaim(localCLaim);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void updateLocalClaim(LocalClaimDTO localClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.updateLocalClaim(localClaim);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void removeLocalClaim(String localCLaimURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.removeLocalClaim(localCLaimURI);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    public ExternalClaimDTO[] getExternalClaims(String externalClaimDialectURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            return stub.getExternalClaims(externalClaimDialectURI);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void addExternalClaim(ExternalClaimDTO externalClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.addExternalClaim(externalClaim);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void updateExternalClaim(ExternalClaimDTO externalClaim) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.updateExternalClaim(externalClaim);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void removeExternalClaim(String externalClaimDialectURI, String externalClaimURI) throws RemoteException,
            ClaimMetadataManagementServiceClaimMetadataException {
        try {
            stub.removeExternalClaim(externalClaimDialectURI, externalClaimURI);
        } catch (RemoteException e) {
            log.error(e.getMessage(), e);
            throw e;
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
