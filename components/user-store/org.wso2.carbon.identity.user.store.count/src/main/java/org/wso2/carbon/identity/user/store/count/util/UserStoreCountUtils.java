/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.user.store.count.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.user.store.count.UserStoreCountRetriever;
import org.wso2.carbon.identity.user.store.count.dto.PairDTO;
import org.wso2.carbon.identity.user.store.count.exception.UserStoreCounterException;
import org.wso2.carbon.identity.user.store.count.internal.UserStoreCountDataHolder;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalCountRetriever;
import org.wso2.carbon.identity.user.store.count.jdbc.internal.InternalStoreCountConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Util class for user store counting functionality for users, roles and by claims
 */
public class UserStoreCountUtils {
    public static final String countRetrieverClass = "CountRetrieverClass";
    private static final Log log = LogFactory.getLog(UserStoreCountUtils.class);

    /**
     * Get the available list of user store domains
     *
     * @return
     * @throws UserStoreCounterException
     */
    public static Map<String, RealmConfiguration> getUserStoreList() throws UserStoreCounterException {
        String domain;
        RealmConfiguration realmConfiguration;
        Map<String, RealmConfiguration> userStoreList = new HashMap<>();

        try {
            realmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();
            domain = IdentityUtil.getPrimaryDomainName();
            userStoreList.put(domain, realmConfiguration);

            while (realmConfiguration != null) {
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
                if (realmConfiguration != null) {
                    domain = realmConfiguration
                            .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                    userStoreList.put(domain, realmConfiguration);
                } else {
                    break;
                }
            }

        } catch (UserStoreException e) {
            throw new UserStoreCounterException("Error while listing user stores for count functionality", e);
        }

        return userStoreList;
    }

    /**
     * Get the domain names of user stores which has count functionality enabled
     *
     * @return
     */
    public static Set<String> getCountEnabledUserStores() throws UserStoreCounterException {
        RealmConfiguration realmConfiguration;
        Set<String> userStoreList = new HashSet<>();

        try {
            realmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();

            while (realmConfiguration != null) {
                if (!Boolean.valueOf(realmConfiguration.getUserStoreProperty(
                        UserCoreConstants.RealmConfig.USER_STORE_DISABLED))) {
                    if (StringUtils.isNotEmpty(realmConfiguration.getUserStoreProperty(countRetrieverClass))) {
                        userStoreList.add(realmConfiguration
                                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));
                    }
                }
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
            }
        } catch (UserStoreException e) {
            throw new UserStoreCounterException("Error while getting the count enabled user stores", e);
        }

        return userStoreList;
    }

    /**
     * Get the available user store domains
     *
     * @return
     * @throws UserStoreCounterException
     */
    public static Set<String> getUserStoreDomains() throws UserStoreCounterException {
        return getUserStoreList().keySet();
    }

    /**
     * Create an instance of the given count retriever class
     *
     * @param domain
     * @return
     * @throws UserStoreCounterException
     */
    public static UserStoreCountRetriever getCounterInstanceForDomain(String domain) throws UserStoreCounterException {
        if (StringUtils.isEmpty(domain)) {
            domain = IdentityUtil.getPrimaryDomainName();
        }

        RealmConfiguration realmConfiguration = getUserStoreList().get(domain);
        if (realmConfiguration != null && realmConfiguration.getUserStoreProperty(countRetrieverClass) != null) {
            String retrieverType = realmConfiguration.getUserStoreProperty(countRetrieverClass);
            UserStoreCountRetriever userStoreCountRetriever = UserStoreCountDataHolder.getInstance()
                    .getCountRetrieverFactories().get(retrieverType).buildCountRetriever(realmConfiguration);
            if (userStoreCountRetriever == null) {
                throw new UserStoreCounterException(
                        "Could not create an instance of class: " + retrieverType + " for " +
                                "the domain: " + domain);
            }
            return userStoreCountRetriever;
        } else {
            return null;
        }
    }

    /**
     * Create an instance of the given count retriever for internal domain
     *
     * @return
     * @throws UserStoreCounterException
     */
    public static UserStoreCountRetriever getInternalCounterInstance() throws UserStoreCounterException {
        UserStoreCountRetriever countRetreiver;

        try {
            countRetreiver = UserStoreCountDataHolder.getInstance().getCountRetrieverFactories()
                    .get(InternalCountRetriever.class.getName()).buildCountRetriever(CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration());
            if (countRetreiver == null) {
                throw new UserStoreCounterException("Could not create a count retriever for Internal domain");
            }
        } catch (UserStoreException e) {
            throw new UserStoreCounterException("Could not create a count retriever for Internal domain");
        }

        return countRetreiver;
    }

    /**
     * Converts a given array of PairDTOs to a Map
     *
     * @param pairDTOs
     * @return
     */
    public static Map<String, String> convertArrayToMap(PairDTO[] pairDTOs) {
        Map<String, String> map = new HashMap<>();
        for (PairDTO pairDTO : pairDTOs) {
            map.put(pairDTO.getKey(), pairDTO.getValue());
        }
        return map;
    }

    /**
     * Converts a given Map to an array of PairDTOs
     *
     * @param claims
     * @return
     */
    public static PairDTO[] convertMapToArray(Map<String, String> claims) {
        PairDTO[] pairs = new PairDTO[claims.size()];
        Iterator iterator = claims.entrySet().iterator();
        int i = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            pairs[i] = new PairDTO(entry.getKey().toString(), entry.getValue().toString());
            i++;
        }

        return pairs;
    }

    public static Long getInternalRoleCount(String filter) throws UserStoreCounterException {
        return getInternalCounterInstance().countRoles(UserCoreConstants.INTERNAL_DOMAIN + "%" + filter);
    }

    public static Long getApplicationRoleCount(String filter) throws UserStoreCounterException {
        return getInternalCounterInstance()
                .countRoles(InternalStoreCountConstants.APPLICATION_DOMAIN + "%" + filter);
    }

    public static boolean isUserStoreEnabled(String domain) throws UserStoreCounterException {

        RealmConfiguration realmConfiguration;
        boolean isEnabled = false;
        try {
            realmConfiguration = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration();

            do {
                String userStoreDomain = realmConfiguration.
                        getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

                if (domain.equals(userStoreDomain)) {
                    isEnabled = !Boolean.valueOf(realmConfiguration.getUserStoreProperty(UserCoreConstants.RealmConfig.
                            USER_STORE_DISABLED));
                    break;
                }
                realmConfiguration = realmConfiguration.getSecondaryRealmConfig();
            } while (realmConfiguration != null);

        } catch (UserStoreException e) {
            throw new UserStoreCounterException("Error occurred while getting Secondary Realm Configuration", e);
        }
        return isEnabled;
    }
}
