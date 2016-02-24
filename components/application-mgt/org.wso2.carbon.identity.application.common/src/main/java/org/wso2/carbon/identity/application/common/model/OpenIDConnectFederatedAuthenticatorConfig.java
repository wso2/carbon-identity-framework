/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.common.model;

import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationManagementUtil;

public class OpenIDConnectFederatedAuthenticatorConfig extends FederatedAuthenticatorConfig {

    /**
     *
     */
    private static final long serialVersionUID = 4146771772483904430L;

    @Override
    public boolean isValid() {
        if (IdentityApplicationManagementUtil.getProperty(properties,
                IdentityApplicationConstants.Authenticator.OIDC.CLIENT_ID) != null &&
                !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.CLIENT_ID)) &&
                IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.CLIENT_SECRET) != null &&
                !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.CLIENT_SECRET)) &&
                IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL) != null &&
                !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_AUTHZ_URL)) &&
                IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL) != null &&
                !"".equals(IdentityApplicationManagementUtil.getProperty(properties,
                        IdentityApplicationConstants.Authenticator.OIDC.OAUTH2_TOKEN_URL))) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return IdentityApplicationConstants.Authenticator.OIDC.NAME;
    }

}
