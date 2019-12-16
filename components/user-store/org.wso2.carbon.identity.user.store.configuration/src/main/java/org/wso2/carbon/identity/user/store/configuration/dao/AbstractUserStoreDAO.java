/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.user.store.configuration.dao;

import org.wso2.carbon.identity.user.store.configuration.dto.PropertyDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStoreDTO;
import org.wso2.carbon.identity.user.store.configuration.dto.UserStorePersistanceDTO;
import org.wso2.carbon.identity.user.store.configuration.utils.IdentityUserStoreMgtException;
import org.wso2.carbon.identity.user.store.configuration.utils.SecondaryUserStoreConfigurationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the abstract implementation of {@link UserStoreDAO}.
 */
public abstract class AbstractUserStoreDAO implements UserStoreDAO {

    public static final String DISABLED = "Disabled";

    @Override
    public void addUserStore(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        UserStorePersistanceDTO userStorePersistanceDTO = getUserStorePersistanceDTO(userStoreDTO,
                getUserStoreProperties(userStoreDTO, userStoreDTO.getDomainId()));
        doAddUserStore(userStorePersistanceDTO);
    }

    @Override
    public void updateUserStore(UserStoreDTO userStoreDTO, boolean isStateChange) throws IdentityUserStoreMgtException {

        if (isStateChange) {
            userStoreDTO = getUserStoreProperty(userStoreDTO);
        }
        UserStorePersistanceDTO userStorePersistanceDTO = getUserStorePersistanceDTO(userStoreDTO,
                getUserStoreProperties(userStoreDTO, userStoreDTO.getDomainId()));
        userStorePersistanceDTO.setUserStoreDTO(userStoreDTO);
        doUpdateUserStore(userStorePersistanceDTO, isStateChange);
    }

    private UserStoreDTO getUserStoreProperty(UserStoreDTO userStoreDTO) throws IdentityUserStoreMgtException {

        boolean newState = userStoreDTO.getDisabled();
        UserStoreDTO userStoreDTOTemp = getUserStore(userStoreDTO.getDomainId());
        if (userStoreDTOTemp != null) {
            userStoreDTO = userStoreDTOTemp;
            userStoreDTO.setDisabled(newState);
            PropertyDTO[] propertyDTO = userStoreDTO.getProperties();
            for (PropertyDTO propertyDTOValue : propertyDTO) {
                if (propertyDTOValue.getName().equals(DISABLED)) {
                    propertyDTOValue.setValue(String.valueOf(newState));
                }
            }
        }
        return userStoreDTO;
    }

    @Override
    public void updateUserStoreDomainName(String previousDomainName, UserStoreDTO userStoreDTO)
            throws IdentityUserStoreMgtException {

        UserStorePersistanceDTO userStorePersistanceDTO = getUserStorePersistanceDTO(userStoreDTO,
                getUserStoreProperties(userStoreDTO, previousDomainName));
        doUpdateUserStoreDomainName(previousDomainName, userStorePersistanceDTO);
    }

    @Override
    public UserStoreDTO getUserStore(String domain) throws IdentityUserStoreMgtException {

        UserStorePersistanceDTO userStorePersistanceDTO = doGetUserStore(domain);
        if (userStorePersistanceDTO != null) {
            return userStorePersistanceDTO.getUserStoreDTO();
        }
        return null;
    }

    @Override
    public UserStoreDTO[] getUserStores() throws IdentityUserStoreMgtException {

        UserStorePersistanceDTO[] userStorePersistanceDTOS = doGetAllUserStores();
        List<UserStoreDTO> userStoreDTOs = new ArrayList<>();
        for (UserStorePersistanceDTO userStorePersistanceDTO : userStorePersistanceDTOS) {
            userStoreDTOs.add(userStorePersistanceDTO.getUserStoreDTO());
        }
        return userStoreDTOs.toArray(new UserStoreDTO[userStoreDTOs.size()]);
    }

    protected abstract void doAddUserStore(UserStorePersistanceDTO userStorePersistanceDTO)
            throws IdentityUserStoreMgtException;

    protected abstract void doUpdateUserStore(UserStorePersistanceDTO userStorePersistanceDTO, boolean isStateChange)
            throws IdentityUserStoreMgtException;

    protected abstract void doUpdateUserStoreDomainName(String previousDomainName, UserStorePersistanceDTO
            userStorePersistanceDTO) throws IdentityUserStoreMgtException;

    protected abstract UserStorePersistanceDTO doGetUserStore(String domain) throws IdentityUserStoreMgtException;

    protected abstract UserStorePersistanceDTO[] doGetAllUserStores() throws IdentityUserStoreMgtException;

    private String getUserStoreProperties(UserStoreDTO userStoreDTO, String existingDomainName)
            throws IdentityUserStoreMgtException {

        return SecondaryUserStoreConfigurationUtil.getUserStoreProperties(userStoreDTO, existingDomainName);
    }

    private UserStorePersistanceDTO getUserStorePersistanceDTO(UserStoreDTO userStoreDTO, String userStoreProperties) {

        UserStorePersistanceDTO userStorePersistanceDTO = new UserStorePersistanceDTO();
        userStorePersistanceDTO.setUserStoreDTO(userStoreDTO);
        userStorePersistanceDTO.setUserStoreProperties(userStoreProperties);
        return userStorePersistanceDTO;
    }
}
