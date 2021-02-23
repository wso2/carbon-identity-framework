package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalJsWritableParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Factory to create a Javascript Object Wrappers for GraalJs execution.
 */
public class GraalJsWrapperFactory implements JsWrapperFactory {

    @Override
    public GraalJsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new GraalJsAuthenticatedUser(authenticatedUser);
    }

    @Override
    public GraalJsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext) {

        return new GraalJsAuthenticationContext(authenticationContext);
    }

    @Override
    public GraalJsCookie createJsCookie(Cookie cookie) {

        return new GraalJsCookie(cookie);
    }

    @Override
    public GraalJsParameters createJsParameters(Map parameters) {

        return new GraalJsParameters(parameters);
    }

    @Override
    public GraalJsWritableParameters createJsWritableParameters(Map data) {

        return new GraalJsWritableParameters(data);
    }

    @Override
    public GraalJsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new GraalJsServletRequest(wrapped);
    }

    @Override
    public GraalJsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new GraalJsServletResponse(wrapped);
    }

    @Override
    public GraalSerializableJsFunction createSerializableFunction(String source, boolean isFunction) {

        return new GraalSerializableJsFunction(source, isFunction);
    }


}
