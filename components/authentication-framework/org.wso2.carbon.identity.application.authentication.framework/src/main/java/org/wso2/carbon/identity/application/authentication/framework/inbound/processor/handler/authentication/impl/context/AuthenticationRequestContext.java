package org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl.context;


import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityMessageContext;
import org.wso2.carbon.identity.application.authentication.framework.inbound.IdentityRequest;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .Constants;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.Sequence;
import org.wso2.carbon.identity.application.authentication.framework.inbound.processor.handler.authentication.impl
        .model.ServiceProviderConfig;
import org.wso2.carbon.identity.core.bean.context.MessageContext;

import java.io.Serializable;
import java.util.Map;

public class AuthenticationRequestContext implements Serializable {

    private transient Sequence sequence = null ;
    private transient ServiceProviderConfig serviceProviderConfig = null ;

    public Sequence getSequence() {
        return sequence;
    }

    public void setSequence(
            Sequence sequence) {
        this.sequence = sequence;
    }

    public ServiceProviderConfig getServiceProviderConfig() {
        return serviceProviderConfig;
    }

    public void setServiceProviderConfig(
            ServiceProviderConfig serviceProviderConfig) {
        this.serviceProviderConfig = serviceProviderConfig;
    }
}
