package org.wso2.carbon.identity.debug.framework;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class to handle debug authentication flows.
 */
public class DebugFlowService {

    /**
     * Executes the debug authentication flow for the given IdP and authenticator.
     *
     * @param idp IdentityProvider object.
     * @param authenticatorName Name of the authenticator.
     * @param username Username for authentication.
     * @param password Password for authentication.
     * @param sessionDataKey Session data key.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @return DebugResponse object containing flow results.
     */
    public Map<String, Object> executeDebugFlow(IdentityProvider idp, String authenticatorName, String username, String password, String sessionDataKey, HttpServletRequest request, HttpServletResponse response) {
        // Provide context.
        ContextProvider contextProvider = new ContextProvider();
        AuthenticationContext authContext = contextProvider.provideContext(request);
        authContext.setProperty("idpName", idp.getIdentityProviderName());
        authContext.setProperty("authenticatorName", authenticatorName);
        authContext.setProperty("username", username);
        authContext.setProperty("password", password);
        authContext.setContextIdentifier(sessionDataKey);
        request.setAttribute("sessionDataKey", sessionDataKey);
        request.setAttribute("AuthenticationContext", authContext);
        request.setAttribute("username", username);
        request.setAttribute("password", password);
        

        // Execute authentication.
        Executer executer = new Executer();
        boolean authResult = executer.execute(idp, authContext);

        // Coordinate request.
        RequestCoordinator coordinator = new RequestCoordinator();
        coordinator.coordinate(authContext, request, response);

        // Process results.
        Processor processor = new Processor();
        Object processedResult = processor.process(authContext);

        // Build debug results.
        Map<String, Object> debugResults = new HashMap<>();
        debugResults.put("debugSessionId", java.util.UUID.randomUUID().toString());
        debugResults.put("status", authResult ? "SUCCESS" : "FAILURE");
        debugResults.put("result", processedResult);
        return debugResults;
    }
}
