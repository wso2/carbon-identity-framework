/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.mgt.endpoint;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.mgt.stub.beans.VerificationBean;

public class IdentityManagementEndpointUtil {

    private IdentityManagementEndpointUtil() {
    }

    public static final String getFullQualifiedUsername(String username, String tenantDomain, String userStoreDomain) {
        String fullQualifiedUsername = username;
        if (StringUtils.isNotBlank(userStoreDomain) && !IdentityManagementEndpointConstants.PRIMARY_USER_STORE_DOMAIN
                .equals(userStoreDomain)) {
            fullQualifiedUsername = userStoreDomain + IdentityManagementEndpointConstants.USER_STORE_DOMAIN_SEPARATOR
                                    + fullQualifiedUsername;
        }

        if (StringUtils.isNotBlank(tenantDomain) && !IdentityManagementEndpointConstants.SUPER_TENANT.equals
                (tenantDomain)) {
            fullQualifiedUsername = fullQualifiedUsername + IdentityManagementEndpointConstants
                    .TENANT_DOMAIN_SEPARATOR + tenantDomain;
        }

        return fullQualifiedUsername;
    }

    public static String getPrintableError(String errorMsgSummary, String optionalErrorMsg, VerificationBean
            verificationBean) {

        StringBuilder errorMsg = new StringBuilder(errorMsgSummary);

        if (verificationBean != null && StringUtils.isNotBlank(verificationBean.getError())) {
            String[] error = verificationBean.getError().split(" ", 2);
            errorMsg.append(" ").append(error[1]);
        } else if (StringUtils.isNotBlank(optionalErrorMsg)) {
            errorMsg.append(" ").append(optionalErrorMsg);
        }

        return errorMsg.toString();
    }

    public static final String getAbsoluteServiceUrlPath(String serviceUrlPath) {
        if (StringUtils.isNotBlank(serviceUrlPath)) {
            return serviceUrlPath;
        }
        return IdentityUtil.getServerURL(IdentityUtil.getServicePath(), true, true);
    }

    public static boolean getBooleanValue(Object value) {
        if (value != null && value instanceof Boolean) {
            return (Boolean) value;
        }

        return false;
    }

    public static String getStringValue(Object value) {
        if (value != null && value instanceof String) {
            return (String) value;
        }

        return "";
    }

    public static int getIntValue(Object value) {
        if (value != null && value instanceof Integer) {
            return (Integer) value;
        }

        return 0;
    }

    public static String[] getStringArray(Object value) {
        if (value != null && value instanceof String[]) {
            return (String[]) value;
        }

        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
