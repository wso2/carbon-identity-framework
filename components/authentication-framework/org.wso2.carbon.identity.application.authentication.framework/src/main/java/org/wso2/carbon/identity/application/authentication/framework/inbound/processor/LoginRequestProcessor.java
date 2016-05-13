package org.wso2.carbon.identity.application.authentication.framework.inbound.processor;


import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkHandlerResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.FrameworkRuntimeException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityContextCache;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityProcessor;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityResponse;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler
        .FrameworkHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.HandlerManager;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication
        .AuthenticationHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.extension
        .ExtensionHandlerPoints;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request
        .AbstractRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.request
        .RequestHandlerException;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response
        .AbstractResponseHandler;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.response.ResponseException;
import org.wso2.carbon.identity.base.IdentityException;

public class LoginRequestProcessor extends IdentityProcessor{

    private static final String PROCESS_CONTEXT_LOGIN = "login" ;
    private static final String PROCESS_CONTEXT_AUTHENTICATION = "authentication" ;

    @Override
    public IdentityResponse.IdentityResponseBuilder process(IdentityRequest identityRequest) throws FrameworkException {
        String processContext = "" ;
        IdentityResponse.IdentityResponseBuilder identityResponseBuilder= null ;

        //Can be an issue if the factory pick non of AuthenticationRequest object by calling can handle
        AuthenticationRequest authenticationRequest = (AuthenticationRequest)identityRequest ;

        if(PROCESS_CONTEXT_LOGIN.equals(processContext)){
            AuthenticationContext authenticationContext = initAuthenticationContext(authenticationRequest);
            identityResponseBuilder = processLoginRequest(authenticationContext);
        }else if(PROCESS_CONTEXT_AUTHENTICATION.equals(processContext)){
            AuthenticationContext authenticationContext = getAuthenticationContextFromCache(authenticationRequest);
            if(authenticationContext == null){
                throw FrameworkRuntimeException.error("Invalid Request");
            }
            identityResponseBuilder = processAuthenticationRequest(authenticationContext);
        }
        return identityResponseBuilder ;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean canHandle(IdentityRequest identityRequest) {
        return false;
    }

    protected AuthenticationContext getAuthenticationContextFromCache(AuthenticationRequest authenticationRequest){

        AuthenticationContext authenticationContext = null ;
        String requestDataKey = authenticationRequest.getRequestDataKey();
        IdentityMessageContext identityMessageContext = IdentityContextCache.getInstance().getValueFromCache(requestDataKey);
        if(identityMessageContext != null){
            authenticationContext = (AuthenticationContext)identityMessageContext ;
        }
        return authenticationContext ;
    }

    protected AuthenticationContext initAuthenticationContext(AuthenticationRequest authenticationRequest){
        AuthenticationContext authenticationContext  = new AuthenticationContext(authenticationRequest);
        return authenticationContext ;
    }


    protected IdentityResponse.IdentityResponseBuilder processLoginRequest(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {

        FrameworkHandlerResponse identityFrameworkHandlerResponse = null;
        try {
            identityFrameworkHandlerResponse = validate(authenticationContext);
            if (identityFrameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {
                identityFrameworkHandlerResponse = authenticate(authenticationContext);
                if (identityFrameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

                }
            }
        } catch (AuthenticationHandlerException e) {
            identityFrameworkHandlerResponse = doBuildErrorResponse(e, authenticationContext);
        }
        return identityFrameworkHandlerResponse.getIdentityResponseBuilder();

    }

    protected IdentityResponse.IdentityResponseBuilder processAuthenticationRequest(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {
        FrameworkHandlerResponse frameworkHandlerResponse = null;
        try {
            frameworkHandlerResponse = authenticate(authenticationContext);
            if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {
                frameworkHandlerResponse = doBuildResponse(authenticationContext);
            }

        } catch (AuthenticationHandlerException e) {
            frameworkHandlerResponse = doBuildErrorResponse(e, authenticationContext);
        }
        return frameworkHandlerResponse.getIdentityResponseBuilder();

    }

    protected FrameworkHandlerResponse doBuildResponse(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {
        FrameworkHandlerResponse frameworkHandlerResponse = null;

        frameworkHandlerResponse = doPreHandle(ExtensionHandlerPoints.RESPONSE_HANDLER, authenticationContext);
        if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

            frameworkHandlerResponse = buildResponse(authenticationContext);
        }
        return frameworkHandlerResponse;


    }

    protected FrameworkHandlerResponse buildResponse(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(authenticationContext);
        return responseBuilderHandler.buildResponse(authenticationContext);


    }


    protected FrameworkHandlerResponse doValidate(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException, RequestHandlerException {
        AbstractRequestHandler protocolRequestHandler =
                HandlerManager.getInstance().getProtocolRequestHandler(authenticationContext);
        return protocolRequestHandler.validate(authenticationContext);
    }

    protected FrameworkHandlerResponse validate(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {

        FrameworkHandlerResponse frameworkHandlerResponse = null;

        frameworkHandlerResponse = doPostHandle(ExtensionHandlerPoints.REQUEST_HANDLER, authenticationContext);
        if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

            frameworkHandlerResponse = doValidate(authenticationContext);
            if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

                frameworkHandlerResponse = doPostHandle(ExtensionHandlerPoints
                                                              .REQUEST_HANDLER, authenticationContext);

            }
        }
        return frameworkHandlerResponse;

    }


    protected FrameworkHandlerResponse authenticate(AuthenticationContext authenticationContext)
            throws FrameworkHandlerException {

        FrameworkHandlerResponse frameworkHandlerResponse = null;

        frameworkHandlerResponse = doPreHandle(ExtensionHandlerPoints.AUTHENTICATION_HANDLER, authenticationContext);
        if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

            frameworkHandlerResponse = doAuthenticate(authenticationContext);
            if (frameworkHandlerResponse.equals(FrameworkHandlerResponse.CONTINUE)) {

                frameworkHandlerResponse = doPostHandle(ExtensionHandlerPoints
                                                                .AUTHENTICATION_HANDLER, authenticationContext);

            }
        }
        return frameworkHandlerResponse;

    }


    protected FrameworkHandlerResponse doAuthenticate(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        AuthenticationHandler authenticationHandler =
                HandlerManager.getInstance().getAuthenticationHandler(authenticationContext);
        return authenticationHandler.doAuthenticate(authenticationContext);
    }


    protected FrameworkHandlerResponse doBuildErrorResponse(IdentityException e,
                                                            AuthenticationContext
                                                                    authenticationContext) throws ResponseException {
        AbstractResponseHandler responseBuilderHandler =
                HandlerManager.getInstance().getResponseHandler(authenticationContext);
        return responseBuilderHandler.buildErrorResponse(authenticationContext);
    }


    protected FrameworkHandlerResponse doPreHandle(ExtensionHandlerPoints extensionHandlerPoints,
                                                   AuthenticationContext authenticationContext)

            throws FrameworkHandlerException {
        return HandlerManager.getInstance().doPreHandle(extensionHandlerPoints, authenticationContext);
    }

    protected FrameworkHandlerResponse doPostHandle(ExtensionHandlerPoints extensionHandlerPoints,
                                                    AuthenticationContext authenticationContext)

            throws FrameworkHandlerException {
        return HandlerManager.getInstance().doPostHandle(extensionHandlerPoints, authenticationContext);
    }

}
