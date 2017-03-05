package org.wso2.carbon.identity.gateway.dao;

import org.wso2.carbon.identity.gateway.api.context.GatewayMessageContext;

public abstract class IdentityContextDAO {

    public abstract GatewayMessageContext get(String key);

    public abstract void put(String key, GatewayMessageContext context);

    public abstract void remove(String key);
}
