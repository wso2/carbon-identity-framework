package org.wso2.carbon.identity.debug.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Provides context for debug authentication flows.
 */
public class ContextProvider {
    /**
     * Creates and configures an AuthenticationContext for debug flows.
     *
     * @param request HttpServletRequest.
     * @return Configured AuthenticationContext.
     */
    public AuthenticationContext provideContext(HttpServletRequest request) {
        AuthenticationContext context = new AuthenticationContext();
        // Set debug-specific properties here.
        context.setRequestType("DFDP_DEBUG");
        context.setCallerSessionKey(java.util.UUID.randomUUID().toString());
        context.setTenantDomain("carbon.super");
        context.setRelyingParty("DFDP_DEBUG_SP");
        context.setProperty("IS_DEBUG_FLOW", Boolean.TRUE);
        return context;
    }
}
