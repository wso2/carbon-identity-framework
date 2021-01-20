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

public class NashornJsWrapperBuilder implements JsWrapperBuilderUtil.JsWrapperBuilder {

    @Override
    public NashornJsAuthenticatedUser buildJsAuthenticatedUser(AuthenticatedUser authenticatedUser) {

        return new NashornJsAuthenticatedUser(authenticatedUser);
    }

    @Override
    public NashornJsAuthenticationContext buildJsAuthenticationContext(AuthenticationContext authenticationContext) {

        return new NashornJsAuthenticationContext(authenticationContext);
    }

    @Override
    public NashornJsCookie buildJsCookie(Cookie cookie) {

        return new NashornJsCookie(cookie);
    }

    @Override
    public NashornJsParameters buildJsParameters(Map parameters) {

        return new NashornJsParameters(parameters);
    }

    @Override
    public NashornJsServletRequest buildJsServletRequest(TransientObjectWrapper<HttpServletRequest> wrapped) {

        return new NashornJsServletRequest(wrapped);
    }

    @Override
    public NashornJsServletResponse buildJsServletResponse(TransientObjectWrapper<HttpServletResponse> wrapped) {

        return new NashornJsServletResponse(wrapped);
    }

    @Override
    public GraalSerializableJsFunction buildSerializableFunction(String source, boolean isFunction) {

        return new GraalSerializableJsFunction(source, isFunction);
    }
}
