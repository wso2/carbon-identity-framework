package org.wso2.carbon.identity.application.authentication.framework.listener;

import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;

/**
 * Authentication App Config listener to call the specific inbound components. The inbound components can use this
 * listener to modify app config.
 */
public interface AuthenticationAppConfigListener {

    /**
     * Framework will select one listener based on the inbound type returned by this.
     *
     * @return Inbound Type.
     */
    String getInboundType();


    /**
     * Handle SP Claim Mapping.
     *
     * @param applicationConfig Application Config.
     * @param tenantDomain   Tenant Domain.
     */
    void onPostLoadAppConfig(ApplicationConfig applicationConfig, String tenantDomain) throws FrameworkException;
}
