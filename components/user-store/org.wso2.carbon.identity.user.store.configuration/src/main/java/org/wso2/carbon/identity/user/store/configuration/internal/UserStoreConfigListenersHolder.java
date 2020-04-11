/*
 * Copyright (c) 2015 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.user.store.configuration.internal;

import org.wso2.carbon.identity.user.store.configuration.UserStoreConfigService;
import org.wso2.carbon.identity.user.store.configuration.dao.AbstractUserStoreDAOFactory;
import org.wso2.carbon.identity.user.store.configuration.listener.UserStoreConfigListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service holder for required OSGi services.
 */
public class UserStoreConfigListenersHolder {

    private static UserStoreConfigListenersHolder userStoreConfigListenersHolder = new UserStoreConfigListenersHolder();
    private List<UserStoreConfigListener> listeners = new ArrayList<>();
    private Map<String, AbstractUserStoreDAOFactory> userStoreDAOFactory = new HashMap<>();
    private UserStoreConfigService userStoreConfigService;
    private Set<String> allowedUserstores = null;


    private UserStoreConfigListenersHolder() {

    }

    public static UserStoreConfigListenersHolder getInstance() {
        return userStoreConfigListenersHolder;
    }

    public void setUserStoreConfigListenerService(UserStoreConfigListener userStoreConfigListener) {
        listeners.add(userStoreConfigListener);
    }

    public void unsetUserStoreConfigListenerService(UserStoreConfigListener userStoreConfigListener) {
        listeners.remove(userStoreConfigListener);
    }

    public List<UserStoreConfigListener> getUserStoreConfigListeners() {
        return listeners;
    }

    public Map<String, AbstractUserStoreDAOFactory> getUserStoreDAOFactories() {
        return userStoreDAOFactory;
    }

    public void setUserStoreConfigService(UserStoreConfigService userStoreConfigService) {
        this.userStoreConfigService = userStoreConfigService;
    }

    public UserStoreConfigService getUserStoreConfigService() {
        return userStoreConfigService;
    }

    public Set<String> getAllowedUserstores() {

        return allowedUserstores;
    }

    public void setAllowedUserstores(Set<String> allowedUserstores) {

        this.allowedUserstores = allowedUserstores;
    }

}
