package org.wso2.carbon.identity.gateway.processor.handler.authentication;


import org.wso2.carbon.identity.gateway.context.AuthenticationContext;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.AbstractSequenceBuildFactory;
import org.wso2.carbon.identity.gateway.processor.handler.authentication.impl.model.AbstractSequence;

public class DefaultSequenceBuilderFactory extends AbstractSequenceBuildFactory {
    @Override
    public DefaultAbstractSequence buildSequence(AuthenticationContext authenticationContext)
            throws AuthenticationHandlerException {
        DefaultAbstractSequence defaultAbstractSequence = new DefaultAbstractSequence(authenticationContext);
        return defaultAbstractSequence;
    }

    @Override
    public String getName() {
        return null;
    }
}
