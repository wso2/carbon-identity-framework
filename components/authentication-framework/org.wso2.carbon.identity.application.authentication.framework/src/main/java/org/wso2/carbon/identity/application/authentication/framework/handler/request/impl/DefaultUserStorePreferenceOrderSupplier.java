/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl;

import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;

import java.util.ArrayList;
import java.util.List;

public class DefaultUserStorePreferenceOrderSupplier implements UserStorePreferenceOrderSupplier<List<String>> {

    private static volatile DefaultUserStorePreferenceOrderSupplier instance =
            new DefaultUserStorePreferenceOrderSupplier();
    private AuthenticationContext context;
    private ServiceProvider serviceProvider;

    public static DefaultUserStorePreferenceOrderSupplier getInstance() {

        return instance;
    }

    public DefaultUserStorePreferenceOrderSupplier(AuthenticationContext authenticationContext, ServiceProvider
            serviceProvider) {

        this.context = authenticationContext;
        this.serviceProvider = serviceProvider;
    }

    private DefaultUserStorePreferenceOrderSupplier() {

    }

    public List<String> get() throws UserStoreException {

        return new ArrayList<>();
    }

    public AuthenticationContext getContext() {

        return context;
    }

    public ServiceProvider getServiceProvider() {

        return serviceProvider;
    }
}
