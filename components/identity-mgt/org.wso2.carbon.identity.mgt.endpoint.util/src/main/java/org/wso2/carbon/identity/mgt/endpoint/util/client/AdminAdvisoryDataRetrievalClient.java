/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint.util.client;

import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.admin.advisory.mgt.stub.AdminAdvisoryManagementServiceStub;
import org.wso2.carbon.admin.advisory.mgt.stub.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointConstants;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementEndpointUtil;
import org.wso2.carbon.identity.mgt.endpoint.util.IdentityManagementServiceUtil;

/**
 * Client to interact with the Admin Advisory Management API.
 */
public class AdminAdvisoryDataRetrievalClient {

    private static final Log LOG = LogFactory.getLog(AdminAdvisoryDataRetrievalClient.class);
    private static final String ENABLE_BANNER = "enableBanner";
    private static final String BANNER_CONTENT= "bannerContent";

    /**
     * Check for admin advisory banner configs in super tenant using AdminAdvisoryManagementService admin service stub.
     *
     * @return JSON Object containing admin advisory banner configs.
     * @throws AdminAdvisoryDataRetrievalClientException Error while retrieving the admin advisory banner configs.
     */
    public JSONObject getAdminAdvisoryBannerDataFromServiceStub() throws AdminAdvisoryDataRetrievalClientException {

        StringBuilder builder = new StringBuilder();
        String serviceURL = null;
        try {
            serviceURL = builder.append(IdentityManagementServiceUtil.getInstance().getServiceContextURL())
                    .append(IdentityManagementEndpointConstants.ServiceEndpoints.ADMIN_ADVISORY_MANAGEMENT_SERVICE)
                    .toString().replaceAll("(?<!(http:|https:))//", "/");
            AdminAdvisoryManagementServiceStub serviceStub = new AdminAdvisoryManagementServiceStub(serviceURL);
            ServiceClient client = serviceStub._getServiceClient();
            IdentityManagementEndpointUtil.authenticate(client);
            AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = serviceStub.getAdminAdvisoryConfig();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ENABLE_BANNER, adminAdvisoryBannerDTO.getEnableBanner());
            jsonObject.put(BANNER_CONTENT, adminAdvisoryBannerDTO.getBannerContent());
            return jsonObject;
        } catch (Exception e) {
            String msg = "Error while getting admin advisory banner preference for uri: " + serviceURL;
            LOG.error(msg, e);
            throw new AdminAdvisoryDataRetrievalClientException(msg, e);
        }
    }
}
