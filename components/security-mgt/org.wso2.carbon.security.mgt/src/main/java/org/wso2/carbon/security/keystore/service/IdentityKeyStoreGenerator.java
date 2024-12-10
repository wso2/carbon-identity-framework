package org.wso2.carbon.security.keystore.service;

import org.wso2.carbon.security.keystore.KeyStoreManagementException;

public interface IdentityKeyStoreGenerator {

    void generateContextKeyStore(String tenantDomain, String suffix) throws KeyStoreManagementException;

}
