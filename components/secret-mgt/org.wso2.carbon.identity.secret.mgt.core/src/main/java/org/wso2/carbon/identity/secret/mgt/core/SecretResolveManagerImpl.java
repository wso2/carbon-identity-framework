package org.wso2.carbon.identity.secret.mgt.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.secret.mgt.core.dao.SecretDAO;
import org.wso2.carbon.identity.secret.mgt.core.exception.SecretManagementException;
import org.wso2.carbon.identity.secret.mgt.core.model.Secret;
import org.wso2.carbon.identity.secret.mgt.core.model.SecretManagerConfigurationHolder;

import java.util.List;

import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_GET_DAO;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.secret.mgt.core.constant.SecretConstants.ErrorMessages.ERROR_CODE_SECRET_GET_REQUEST_INVALID;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleClientException;
import static org.wso2.carbon.identity.secret.mgt.core.util.SecretUtils.handleServerException;

public class SecretResolveManagerImpl implements SecretResolveManager {

    private static final Log log = LogFactory.getLog(SecretManagerImpl.class);
    private List<SecretDAO> secretDAOS;

    public SecretResolveManagerImpl(SecretManagerConfigurationHolder secretManagerConfigurationHolder) {

        this.secretDAOS = secretManagerConfigurationHolder.getSecretDAOS();
    }
    @Override
    public Secret getResolvedSecret(String secretName) throws SecretManagementException {

        validateSecretRetrieveRequest(secretName);
        Secret secret = this.getSecretDAO().getSecretByName(getTenantId(), secretName);
        if (secret == null) {
            if (log.isDebugEnabled()) {
                log.debug("No secret found for the secretName: " + secretName);
            }
            throw handleClientException(ERROR_CODE_SECRET_DOES_NOT_EXISTS, secretName, null);
        }
        return secret;
    }

    /**
     * Validate that secret name is non-empty.
     *
     * @param secretName The secret name.
     * @throws SecretManagementException If secret validation fails.
     */
    private void validateSecretRetrieveRequest(String secretName) throws SecretManagementException {

        if (StringUtils.isEmpty(secretName)) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid secret identifier with secretName: " + secretName + ".");
            }
            throw handleClientException(ERROR_CODE_SECRET_GET_REQUEST_INVALID, null);
        }
    }

    /**
     * Select highest priority Secret DAO from an already sorted list of Secret DAOs.
     *
     * @return Highest priority Secret DAO.
     */
    private SecretDAO getSecretDAO() throws SecretManagementException {

        if (!this.secretDAOS.isEmpty()) {
            return secretDAOS.get(secretDAOS.size() - 1);
        } else {
            throw handleServerException(ERROR_CODE_GET_DAO, "secretDAOs");
        }
    }

    private int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
}
