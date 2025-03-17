/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.registration.mgt.utils;

import static org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils.ENABLE_V2_AUDIT_LOGS;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.registration.mgt.Constants.ErrorMessages;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationClientException;
import org.wso2.carbon.identity.user.registration.mgt.exception.RegistrationServerException;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class RegistrationMgtUtils {

    private RegistrationMgtUtils() {

    }

    /**
     * Handle the registration flow management server exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow management server exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationServerException.
     */
    public static RegistrationServerException handleServerException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationServerException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Handle the registration flow management client exceptions.
     *
     * @param error Error message.
     * @param e     Throwable.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Throwable e, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description, e);
    }

    /**
     * Handle the registration flow management client exceptions.
     *
     * @param error Error message.
     * @param data  The error message data.
     * @return RegistrationClientException.
     */
    public static RegistrationClientException handleClientException(ErrorMessages error, Object... data) {

        String description = error.getDescription();
        if (ArrayUtils.isNotEmpty(data)) {
            description = String.format(description, data);
        }
        return new RegistrationClientException(error.getCode(), error.getMessage(), description);
    }

    /**
     * Check whether the v2 audit logs are enabled.
     *
     * @return true if v2 audit logs are enabled.
     */
    public static boolean isEnableV2AuditLogs() {

        return Boolean.parseBoolean(System.getProperty(ENABLE_V2_AUDIT_LOGS));
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    public static String getInitiatorId() {

        String initiator = null;
        String username = MultitenantUtils.getTenantAwareUsername(getUser());
        String tenantDomain = MultitenantUtils.getTenantDomain(getUser());
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        if (StringUtils.isBlank(initiator)) {
            if (username.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                // If the initiator is wso2.system, we need not to mask the username.
                return LoggerUtils.Initiator.System.name();
            }
            initiator = LoggerUtils.getMaskedContent(getUser());
        }
        return initiator;
    }

    /**
     * To get the current user, who is doing the current task.
     *
     * @return current logged-in user.
     */
    private static String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(user)) {
            user = UserCoreUtil
                    .addTenantDomainToEntry(user, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }
}
