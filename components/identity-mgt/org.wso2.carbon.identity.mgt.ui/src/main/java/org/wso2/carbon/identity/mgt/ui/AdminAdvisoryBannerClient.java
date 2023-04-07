/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.mgt.stub.AdminAdvisoryManagementServiceStub;
import org.wso2.carbon.identity.mgt.stub.dto.AdminAdvisoryBannerDTO;

/**
 * AdminAdvisoryBannerClient class.
 */
public class AdminAdvisoryBannerClient {
    protected static final Log LOG = LogFactory.getLog(AdminAdvisoryBannerClient.class);
    protected AdminAdvisoryManagementServiceStub stub;

    public AdminAdvisoryBannerClient(String url, ConfigurationContext configContext) throws AxisFault {

        try {
            stub = new AdminAdvisoryManagementServiceStub(configContext, url
                    + "AdminAdvisoryManagementService");
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    public AdminAdvisoryBannerClient(String cookie, String url, ConfigurationContext configContext) throws AxisFault {

        try {
            stub = new AdminAdvisoryManagementServiceStub(configContext, url
                    + "AdminAdvisoryManagementService");
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    private String[] handleException(String msg, Exception e) throws AxisFault {

        LOG.error(msg, e);
        throw new AxisFault(msg, e);
    }

    public void saveBannerConfig(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO) throws AxisFault {

        try {
            stub.saveAdminAdvisoryConfig(adminAdvisoryBannerDTO);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    public AdminAdvisoryBannerDTO loadBannerConfig() throws AxisFault {

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = null;
        try {
            adminAdvisoryBannerDTO = stub.getAdminAdvisoryConfig();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return adminAdvisoryBannerDTO;
    }
}
