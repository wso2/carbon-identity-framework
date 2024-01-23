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

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.governance.stub.IdentityGovernanceAdminServiceIdentityGovernanceExceptionException;
import org.wso2.carbon.identity.governance.stub.IdentityGovernanceAdminServiceStub;
import org.wso2.carbon.identity.governance.stub.bean.ConnectorConfig;
import org.wso2.carbon.identity.governance.stub.bean.Property;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdentityGovernanceAdminClient {

    protected IdentityGovernanceAdminServiceStub stub = null;
    public static final String IDENTITY_MGT_ADMIN_SERVICE_URL = "IdentityGovernanceAdminService";
    public static final String DEFAULT = "DEFAULT";

    private static final Log log = LogFactory.getLog(IdentityGovernanceAdminClient.class);

    public IdentityGovernanceAdminClient(String cookie, String backendServerURL, ConfigurationContext configContext)
            throws Exception {
        try {
            stub = new IdentityGovernanceAdminServiceStub(configContext,
                                                          backendServerURL + IDENTITY_MGT_ADMIN_SERVICE_URL);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (Exception e) {
            throw new Exception("Error occurred while creating IdentityGovernanceAdminClient Object", e);
        }
    }

    public Map<String, Map<String, List<ConnectorConfig>>> getConnectorList()
            throws RemoteException, IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {

        ConnectorConfig[] configs = stub.getConnectorList();
        Map<String, Map<String, List<ConnectorConfig>>> catMap = new HashMap<>();
        if (configs != null) {
            for (ConnectorConfig conf : configs) {
                String categoryName = StringUtils.isBlank(conf.getCategory()) ? DEFAULT : conf.getCategory();
                String subCategoryName = StringUtils.isBlank(conf.getSubCategory()) ? DEFAULT : conf.getSubCategory();

                Map<String, List<ConnectorConfig>> subCatMap = catMap.get(categoryName);
                if (subCatMap == null) {
                    subCatMap = new HashMap<>();
                    catMap.put(categoryName, subCatMap);
                }
                List confList = subCatMap.get(subCategoryName);
                if(confList == null) {
                    confList = new ArrayList();
                    subCatMap.put(subCategoryName, confList);
                }
                confList.add(conf);
            }
        }
        return catMap;
    }

    public void updateConfigurations(Property[] properties)
            throws RemoteException, IdentityGovernanceAdminServiceIdentityGovernanceExceptionException {
        stub.updateConfigurations(properties);
    }

}
