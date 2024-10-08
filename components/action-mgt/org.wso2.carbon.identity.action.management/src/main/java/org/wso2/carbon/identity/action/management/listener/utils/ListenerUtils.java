package org.wso2.carbon.identity.action.management.listener.utils;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * Utility class that handles the relevant utility tasks of listeners.
 */
public class ListenerUtils {

    /**
     * To get the current user, who is doing the current task.
     *
     * @return current logged-in user.
     */
    public static String getUser() {

        String user = CarbonContext.getThreadLocalCarbonContext().getUsername();
        if (StringUtils.isNotEmpty(user)) {
            user = UserCoreUtil
                    .addTenantDomainToEntry(user, CarbonContext.getThreadLocalCarbonContext().getTenantDomain());
        } else {
            user = CarbonConstants.REGISTRY_SYSTEM_USERNAME;
        }
        return user;
    }

    /**
     * Returns initiator based on the masking config.
     *
     * @return Initiator. If log masking is enabled returns the userId, if userId can not be resolved then returns the
     * masked username.
     */
    public static String getInitiator() {

        String initiator = null;
        if (LoggerUtils.isLogMaskingEnable) {
            String username = MultitenantUtils.getTenantAwareUsername(ListenerUtils.getUser());
            String tenantDomain = MultitenantUtils.getTenantDomain(ListenerUtils.getUser());
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
                initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
            }
            if (StringUtils.isBlank(initiator)) {
                initiator = LoggerUtils.getMaskedContent(ListenerUtils.getUser());
            }
        } else {
            initiator = ListenerUtils.getUser();
        }
        return initiator;
    }

    /**
     * Get the initiator for audit logs.
     *
     * @return Initiator id despite masking.
     */
    public static String getInitiatorId() {

        String initiator = null;
        String username = MultitenantUtils.getTenantAwareUsername(ListenerUtils.getUser());
        String tenantDomain = MultitenantUtils.getTenantDomain(ListenerUtils.getUser());
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(tenantDomain)) {
            initiator = IdentityUtil.getInitiatorId(username, tenantDomain);
        }
        if (StringUtils.isBlank(initiator)) {
            if (username.equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                // If the initiator is wso2.system, we need not to mask the username.
                return LoggerUtils.Initiator.System.name();
            }
            initiator = LoggerUtils.getMaskedContent(ListenerUtils.getUser());
        }
        return initiator;
    }
}

