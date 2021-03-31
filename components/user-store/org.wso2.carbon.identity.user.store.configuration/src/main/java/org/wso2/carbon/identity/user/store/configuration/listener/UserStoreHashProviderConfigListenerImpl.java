package org.wso2.carbon.identity.user.store.configuration.listener;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.internal.UserStoreConfigListenersHolder;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.exceptions.HashProviderException;
import org.wso2.carbon.user.core.hash.HashProviderFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Hash Provider Listener for validating the HashProvider Params.
 */
public class UserStoreHashProviderConfigListenerImpl extends AbstractUserStoreConfigListener {

    public static final String DIGEST_FUNCTION = "PasswordDigest";
    public static final String HASH_PROVIDER_PARAMS_JSON = "Hash.Algorithm.Properties";
    private JsonObject hashPropertyJSON;

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
        if (StringUtils.isNotBlank(hashProviderParamsJSON)) {
            HashProviderFactory hashProviderFactory = UserStoreConfigListenersHolder.getInstance().
                    getHashProviderFactory(digestFunction);
            if (hashProviderFactory != null) {
                Set<String> hashProviderMetaProperties = hashProviderFactory.getHashProviderConfigProperties();
                validateParams(hashProviderParamsJSON, hashProviderMetaProperties);
                Map<String, Object> hashProviderPropertiesMap =
                        getHashProviderInitConfigs(hashProviderParamsJSON);
                try {
                    hashProviderFactory.getHashProvider(hashProviderPropertiesMap);
                } catch (HashProviderException e) {
                    throw new UserStoreException("Error occurred while initializing the hashProvider.", e);
                }
            }
        }
    }

    /**
     * Validating the hashProvider params.
     *
     * @param hashProviderParamsJSON       Hash provider params in the JSON string format.
     * @param hashProviderConfigProperties Set of metaProperties of the HashProvider.
     * @throws UserStoreException The exception thrown at validating the hashProvider params.
     */
    private void validateParams(String hashProviderParamsJSON, Set<String> hashProviderConfigProperties)
            throws UserStoreException {

        try {
            Gson gson = new Gson();
            hashPropertyJSON = gson.fromJson(hashProviderParamsJSON, JsonObject.class);
            Set<String> hashPropertyJSONKey = hashPropertyJSON.keySet();
            for (String hashProperty : hashPropertyJSONKey) {
                if (!hashProviderConfigProperties.contains(hashProperty)) {
                    throw new UserStoreException(hashProperty +
                            " is not a configuration property which needs to be configured.");
                }
            }
        } catch (JsonSyntaxException e) {
            throw new UserStoreException("User store hashing configuration should be a proper JSON format", e);
        }
    }

    /**
     * Get map of user store properties related to the HashProviderFactory.
     *
     * @param hashingAlgorithmProperties The hashingAlgorithmProperties.
     * @return The map of user store properties related to the HashProviderFactory.
     */
    private Map<String, Object> getHashProviderInitConfigs(String hashingAlgorithmProperties) {

        Map<String, Object> hashProviderInitConfigsMap = new HashMap<>();
        if (StringUtils.isNotBlank(hashingAlgorithmProperties)) {
            Set<String> hashPropertyJSONKey = hashPropertyJSON.keySet();
            for (String hashProperty : hashPropertyJSONKey) {
                hashProviderInitConfigsMap.put(hashProperty, hashPropertyJSON.get(hashProperty).getAsString());
            }
        }
        return hashProviderInitConfigsMap;
    }
}
