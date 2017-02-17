package org.wso2.carbon.identity.gateway.dao;

import org.wso2.carbon.identity.gateway.api.context.IdentityMessageContext;

public class CacheBackedIdentitySessionDAO extends IdentityContextDAO {

    private static IdentityContextDAO instance = new CacheBackedIdentitySessionDAO();
    private IdentityContextDAO asyncSessionDAO = AsyncIdentityContextDAO.getInstance();

    private CacheBackedIdentitySessionDAO() {

    }

    public static IdentityContextDAO getInstance() {
        return instance;
    }

    @Override
    public void put(String key, IdentityMessageContext context) {
        asyncSessionDAO.put(key, context);
    }

    @Override
    public IdentityMessageContext get(String key) {
        return asyncSessionDAO.get(key);
    }

    @Override
    public void remove(String key) {
        asyncSessionDAO.remove(key);
    }
}
