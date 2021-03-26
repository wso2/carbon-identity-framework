package org.wso2.carbon.identity.user.store.configuration.listener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.hash.HashProviderFactory;

import java.util.Set;

/**
 * Hash Provider Listener for validatiusereeeeng the HashProvider Params.
 */
public class UserStoreHashProviderListenerImpl extends AbstractUserStoreConfigListener {

    public static final String DIGEST_FUNCTION = "PasswordDigest";
    public static final String HASH_PROVIDER_PARAMS_JSON = "Hash.Algorithm.Properties";

    @Override
    public void onUserStorePreUpdate(int tenantId, UserStoreDTO userStoreDTO, boolean isStateChange) throws
            UserStoreException {

        validateHashProviderParams(userStoreDTO);
    }

    @Override
    public void onUserStorePreAdd(int tenantId, UserStoreDTO userStoreDTO) throws UserStoreException {

        validateHashProviderParams(userStoreDTO);
    }

    /**
     * Derive the userStoreProperties from UserStoreDTO and Validating HashProvider params.
     *
     * @param userStoreDTO Data transfer object of userStore properties.
     * @throws UserStoreException The exception thrown at validating the hashProvider params.
     */
    private void validateHashProviderParams(UserStoreDTO userStoreDTO) throws UserStoreException {

        PropertyDTO[] userStoreProperty = userStoreDTO.getProperties();
        String digestFunction = null;
        String hashProviderParamsJSON = null;
        for (PropertyDTO propertyDTO : userStoreProperty) {
            if (propertyDTO.getName().equals(DIGEST_FUNCTION)) {
                digestFunction = propertyDTO.getValue();
            }
            if (propertyDTO.getName().equals(HASH_PROVIDER_PARAMS_JSON)) {
                hashProviderParamsJSON = propertyDTO.getValue();
            }
        }
        HashProviderFactory hashProviderFactory = UserStoreConfigListenersHolder.getInstance().
                getHashProviderFactory(digestFunction);
        if (hashProviderFactory != null) {
            Set<String> hashProviderMetaProperties = hashProviderFactory.getHashProviderMetaProperties();
            validateParams(hashProviderParamsJSON, hashProviderMetaProperties);
        }
    }

    /**
     * Validating the hashProvider params.
     *
     * @param hashProviderParamsJSON     Hash provider params in the JSON string format.
     * @param hashProviderMetaProperties Set of metaProperties of the HashProvider.
     * @throws UserStoreException The exception thrown at validating the hashProvider params.
     */
    private void validateParams(String hashProviderParamsJSON, Set<String> hashProviderMetaProperties)
            throws UserStoreException {

        try {
            Gson gson = new Gson();
            JsonObject hashPropertyJSON = gson.fromJson(hashProviderParamsJSON, JsonObject.class);
            if (hashPropertyJSON != null) {
                Set<String> hashPropertyJSONKey = hashPropertyJSON.keySet();
                for (String hashProperty : hashPropertyJSONKey) {
                    if (!hashProviderMetaProperties.contains(hashProperty)) {
                        throw new UserStoreException(hashProperty + " should be configured.");
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            throw new UserStoreException("UserStore Hashing Configuration should be a proper JSON format", e);
        }
    }
}
