package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graal.GraalSerializableJsFunction;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsCookie;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsParameters;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsServletRequest;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn.NashornJsServletResponse;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.TransientObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NashornJsWrapperFactory implements JsWrapperFactorySingleton.JsWrapperFactory {

    @Override
    public NashornJsAuthenticatedUser createJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new NashornJsAuthenticatedUser(authenticatedUser);
    }

    @Override
    public NashornJsAuthenticationContext createJsAuthenticationContext(AuthenticationContext authenticationContext) {

        return new NashornJsAuthenticationContext(authenticationContext);
    }

    @Override
    public NashornJsCookie createJsCookie(Cookie cookie) {

        return new NashornJsCookie(cookie);
    }

    @Override
    public NashornJsParameters createJsParameters(Map parameters) {

        return new NashornJsParameters(parameters);
    }

    @Override
    public NashornJsServletRequest createJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new NashornJsServletRequest(wrapped);
    }

    @Override
    public NashornJsServletResponse createJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new NashornJsServletResponse(wrapped);
    }

    @Override
    public GraalSerializableJsFunction createSerializableFunction(String source, boolean isFunction) {

        return new GraalSerializableJsFunction(source, isFunction);
    }
}
