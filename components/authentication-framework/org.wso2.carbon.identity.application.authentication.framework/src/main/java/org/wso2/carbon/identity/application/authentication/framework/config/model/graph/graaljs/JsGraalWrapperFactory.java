package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsWrapperBaseFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graaljs.JsGraalWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for GraalJS execution.
 * Since Nashorn is deprecated in JDK 11 and onwards. We are introducing GraalJS engine.
 */
public class JsGraalWrapperFactory implements JsWrapperBaseFactory {

    @Override
    public JsGraalAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new JsGraalAuthenticatedUser(authenticatedUser);
    }

    @Override
    public JsGraalAuthenticatedUser createJsAuthenticatedUser(AuthenticationContext authenticationContext,
                                                              AuthenticatedUser authenticatedUser) {

        return new JsGraalAuthenticatedUser(authenticationContext, authenticatedUser);
    }

    @Override
    public JsGraalAuthenticationContext createJsAuthenticationContext
            (AuthenticationContext authenticationContext) {

        return new JsGraalAuthenticationContext(authenticationContext);
    }

    @Override
    public JsGraalCookie createJsCookie(Cookie cookie) {

        return new JsGraalCookie(cookie);
    }

    @Override
    public JsGraalWritableParameters createJsParameters(Map parameters) {

        return new JsGraalWritableParameters(parameters);
    }

    @Override
    public JsGraalWritableParameters createJsWritableParameters(Map data) {

        return new JsGraalWritableParameters(data);
    }

    @Override
    public JsGraalServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new JsGraalServletRequest(wrapped);
    }

    @Override
    public JsGraalServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new JsGraalServletResponse(wrapped);
    }

}
