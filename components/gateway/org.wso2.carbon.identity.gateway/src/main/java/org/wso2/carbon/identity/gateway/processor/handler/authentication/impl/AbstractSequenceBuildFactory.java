package org.wso2.carbon.identity.gateway.processor.handler.authentication.impl;


import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;


public abstract class AbstractSequenceBuildFactory extends FrameworkHandler {
    public abstract AbstractSequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;
}
