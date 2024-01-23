/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.role.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.ClaimMetadataManagementServiceStub;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.LocalClaimDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.stub.dto.ClaimPropertyDTO;
import org.wso2.carbon.identity.claim.metadata.mgt.util.ClaimConstants;
import org.wso2.carbon.role.mgt.ui.ClaimDataAdminException;

import java.rmi.RemoteException;

/**
 * This class invokes the operations of ClaimMetadataManagementService.
 */
public class ClaimDataAdminClient {

    private ClaimMetadataManagementServiceStub stub;
    private static final String SERVICE = "ClaimMetadataManagementService";

    /**
     * Instantiates ClaimMetadataAdminClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where ClaimManagementServiceStub is running.
     * @param configCtx        ConfigurationContext
     * @throws AxisFault if error occurs when instantiating the stub
     */
    public ClaimDataAdminClient(String cookie, String backendServerURL, ConfigurationContext configCtx) throws
            AxisFault {

        String serviceURL = backendServerURL + SERVICE;
        stub = new ClaimMetadataManagementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
    }

    public LocalClaimDTO[] getLocalClaims()
            throws RemoteException, ClaimMetadataManagementServiceClaimMetadataException {

        LocalClaimDTO[] localClaimDTOs = stub.getLocalClaims();
        if (localClaimDTOs == null) {
            localClaimDTOs = new LocalClaimDTO[0];
        }
        return localClaimDTOs;
    }

    public String getRegex(LocalClaimDTO[] localClaimDTO, String claimURI) {

        String pattern = "";
        for (LocalClaimDTO localClaim : localClaimDTO) {
            if (claimURI.equals(localClaim.getLocalClaimURI())) {
                ClaimPropertyDTO[] claimPropertyDTOs = localClaim.getClaimProperties();
                if (claimPropertyDTOs != null) {
                    for (ClaimPropertyDTO claimPropertyDTO : claimPropertyDTOs) {
                        if (ClaimConstants.REGULAR_EXPRESSION_PROPERTY.equals(claimPropertyDTO.getPropertyName())) {
                            pattern = claimPropertyDTO.getPropertyValue();
                            pattern = "/" + pattern + "/";
                            break;
                        }
                    }
                }
            }
        }
        return pattern;
    }

    public String getRegex(String claimURI) throws ClaimDataAdminException {

        LocalClaimDTO[] localClaims = new LocalClaimDTO[0];
        try {
            localClaims = getLocalClaims();
        } catch (ClaimMetadataManagementServiceClaimMetadataException e) {
            throw new ClaimDataAdminException("Error while accessing claims.", e);
        } catch (RemoteException e) {
            throw new ClaimDataAdminException("Error while getting new claims.", e);
        }
        return getRegex(localClaims, claimURI);
    }
}
