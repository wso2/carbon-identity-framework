/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.claim.mgt.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.claim.mgt.stub.ClaimManagementServiceStub;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimDTO;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimDialectDTO;
import org.wso2.carbon.claim.mgt.stub.dto.ClaimMappingDTO;

public class ClaimAdminClient {

    private static final Log log = LogFactory.getLog(ClaimAdminClient.class);
    private ClaimManagementServiceStub stub;

    /**
     * Instantiates ClaimAdminClient
     *
     * @param cookie           For session management
     * @param backendServerURL URL of the back end server where ClaimManagementServiceStub is
     *                         running.
     * @param configCtx        ConfigurationContext
     * @throws org.apache.axis2.AxisFault if error occurs when instantiating the stub
     */
    public ClaimAdminClient(String cookie, String backendServerURL,
                            ConfigurationContext configCtx) throws AxisFault {
        String serviceURL = backendServerURL + "ClaimManagementService";
        stub = new ClaimManagementServiceStub(configCtx, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);

        option.setProperty(
                org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                cookie);
    }

    /**
     * @param
     * @return
     * @throws AxisFault
     */
    public ClaimDialectDTO[] getAllClaimMappings() throws AxisFault {
        try {
            return stub.getClaimMappings();
        } catch (Exception e) {
            handleException("Error while reading claim mappings", e);
        }

        return new ClaimDialectDTO[0];
    }

    /**
     * @param
     * @param dialect
     * @return
     * @throws AxisFault
     */
    public ClaimDialectDTO getAllClaimMappingsByDialect(String dialect)
            throws AxisFault {
        try {
            return stub.getClaimMappingByDialect(dialect);
        } catch (Exception e) {
            handleException("Error while reading claim mappings by dialect", e);
        }

        return null;
    }

    /**
     * We need to treat http://wso2.org/claims/role claim differently.
     * Should not be set to supporrtedByDefaut to true.
     *
     * @param dialect
     * @return
     * @throws AxisFault
     */
    public ClaimDialectDTO getAllClaimMappingsByDialectWithRole(String dialect)
            throws AxisFault {
        try {
            ClaimDialectDTO claims = stub.getClaimMappingByDialect(dialect);
            ClaimMappingDTO[] oldClaims = claims.getClaimMappings();
            ClaimMappingDTO[] newClaims = new ClaimMappingDTO[claims
                    .getClaimMappings().length + 1];
            int i = 0;
            for (; i < oldClaims.length; i++) {
                newClaims[i] = oldClaims[i];
            }
            ClaimMappingDTO mapping = new ClaimMappingDTO();
            ClaimDTO role = new ClaimDTO();
            role.setClaimUri("http://wso2.org/claims/role");
            mapping.setClaim(role);
            newClaims[i] = mapping;
            claims.setClaimMappings(newClaims);
            return claims;
        } catch (Exception e) {
            handleException("Error while reading claim mappings by dialect", e);
        }

        return null;
    }

    /**
     * @param dialect
     * @throws AxisFault
     */
    public void addNewClaimDialect(ClaimDialectDTO dialect) throws AxisFault {
        try {
            stub.addNewClaimDialect(dialect);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * @param
     * @param claimMapping
     * @throws AxisFault
     */
    public void addNewClaimMappping(ClaimMappingDTO claimMapping)
            throws AxisFault {
        try {
            stub.addNewClaimMapping(claimMapping);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * @param
     * @param claimMapping
     * @throws AxisFault
     */
    public void updateClaimMapping(ClaimMappingDTO claimMapping)
            throws AxisFault {
        try {
            stub.upateClaimMapping(claimMapping);
        } catch (Exception e) {
            handleException("Error while updating claim mapping", e);
        }
    }

    /**
     * @param
     * @param dialectUri
     * @param claimUri
     * @throws AxisFault
     */
    public void removeClaimMapping(String dialectUri, String claimUri)
            throws AxisFault {
        try {
            stub.removeClaimMapping(dialectUri, claimUri);
        } catch (Exception e) {
            handleException("Error while removing claim mapping", e);
        }
    }

    /**
     * @param
     * @param dialectUri
     * @throws AxisFault
     */
    public void removeClaimDialect(String dialectUri) throws AxisFault {
        try {
            stub.removeClaimDialect(dialectUri);
        } catch (Exception e) {
            handleException("Error while removing claim dialect", e);
        }
    }

    /**
     * Logs and wraps the given exception.
     *
     * @param msg Error message
     * @param e   Exception
     * @throws AxisFault which wraps the error
     */
    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(msg, e);
        throw new AxisFault(msg, e);
    }

}
