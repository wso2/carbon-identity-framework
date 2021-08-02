package org.wso2.carbon.identity.secret.mgt.core;

import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;

/**
 * Secret resolve manager service interface.
 */
public interface SecretResolveManager {

    /**
     * This API is used to retrieve the given secret with resolved value.
     *
     * @param secretName Name of the {@link Secret}.
     * @return 200 ok. Returns {@link Secret} requested.
     * @throws SecretManagementException Secret management exception.
     */
    Secret getResolvedSecret(String secretName) throws SecretManagementException;
}
