package org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl;


import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.AuthenticationHandlerException;
import org.wso2.carbon.identity.gateway.authentication.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.FrameworkHandler;
import org.wso2.carbon.identity.gateway.authentication.processor.handler.authentication.impl.model.AbstractSequence;


public abstract class AbstractSequenceBuildFactory extends FrameworkHandler {
    public abstract AbstractSequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException;
}
