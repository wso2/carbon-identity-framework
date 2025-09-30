package org.wso2.carbon.identity.debug.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Coordinates debug authentication requests.
 */
public class RequestCoordinator {
    /**
     * Handles the debug authentication request.
     *
     * @param context AuthenticationContext.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     */
    public void coordinate(AuthenticationContext context, HttpServletRequest request, HttpServletResponse response) {
        // TODO: Implement coordination logic (invoke framework, etc.).
    }
}
