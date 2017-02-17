package org.wso2.carbon.identity.gateway.dao;

import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;

public abstract class IdentityContextDAO {

    public abstract void put(String key, IdentityMessageContext context);

    public abstract IdentityMessageContext get(String key);

    public abstract void remove(String key);
}
