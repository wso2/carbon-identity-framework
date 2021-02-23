package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface specific Js Wrapper Factory.
 */
public interface JsWrapperFactory {

    JsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser);

    JsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext);

    JsCookie createJsCookie(Cookie cookie);

    JsParameters createJsParameters(Map parameters);

    JsParameters createJsWritableParameters(Map data);

    JsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped);

    JsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped);

    SerializableJsFunction createSerializableFunction(String source, boolean isFunction);

}
