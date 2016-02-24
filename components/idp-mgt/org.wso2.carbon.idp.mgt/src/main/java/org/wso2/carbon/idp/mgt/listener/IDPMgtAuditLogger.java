/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.idp.mgt.listener;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.user.core.util.UserCoreUtil;

public class IDPMgtAuditLogger extends AbstractIdentityProviderMgtListener {


    Log audit = CarbonConstants.AUDIT_LOG;
    private static String AUDIT_MESSAGE = "Initiator : %s | Action : %s | Target : %s | Data : { %s } | Result : %s ";
    private final String SUCCESS = "Success";

    @Override
    public int getDefaultOrderId() {
        return 220;
    }

    @Override
    public boolean doPostAddIdP(IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {
        String displayName = "Undefined";
        String idpName = "Undefined";
        if (identityProvider != null) {
            if(StringUtils.isNotEmpty(identityProvider.getDisplayName())){
                displayName = identityProvider.getDisplayName();
            }
            idpName = identityProvider.getIdentityProviderName();
        }
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "add", UserCoreUtil.addTenantDomainToEntry(displayName,
                tenantDomain), idpName, SUCCESS));

        return true;
    }

    @Override
    public boolean doPostUpdateIdP(String oldIdPName, IdentityProvider identityProvider, String tenantDomain) throws
            IdentityProviderManagementException {
        String displayName = "Undefined";
        if (identityProvider != null && StringUtils.isNotEmpty(identityProvider.getDisplayName())) {
            displayName = identityProvider.getDisplayName();
        }
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "update", oldIdPName, UserCoreUtil
                .addTenantDomainToEntry(displayName, tenantDomain), SUCCESS));
        return true;
    }

    @Override
    public boolean doPostDeleteIdP(String idPName, String tenantDomain) throws IdentityProviderManagementException {
        if(StringUtils.isEmpty(idPName)){
            idPName = "Undefined";
        }
        audit.info(String.format(AUDIT_MESSAGE, getUser(), "delete", UserCoreUtil.addTenantDomainToEntry
                (idPName, tenantDomain), null, SUCCESS));
        return true;
    }

    private String getUser() {
        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (user != null) {
            user = user + "@" + CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }
}
