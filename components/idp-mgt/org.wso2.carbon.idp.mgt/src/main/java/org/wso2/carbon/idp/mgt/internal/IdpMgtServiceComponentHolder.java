/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.idp.mgt.internal;

import org.wso2.carbon.idp.mgt.listener.IdentityProviderMgtListener;
import org.wso2.carbon.idp.mgt.util.MetadataConverter;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;
import java.util.ArrayList;
import java.util.List;

public class IdpMgtServiceComponentHolder {

    public static IdpMgtServiceComponentHolder instance = new IdpMgtServiceComponentHolder();

    public static IdpMgtServiceComponentHolder getInstance() {
        return instance;
    }


    private  RealmService realmService = null;

    private ConfigurationContextService configurationContextService = null;
    private volatile List<IdentityProviderMgtListener> idpMgtListeners = new ArrayList<>();
    private RegistryService registryService;

    private List<MetadataConverter> metadataConverters = new ArrayList<>();


    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public List<IdentityProviderMgtListener> getIdpMgtListeners() {
        return idpMgtListeners;
    }

    public void setIdpMgtListeners(List<IdentityProviderMgtListener> idpMgtListeners) {
        this.idpMgtListeners = idpMgtListeners;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public List<MetadataConverter> getMetadataConverters() {
        return metadataConverters;
    }

    public void setMetadataConverters(List<MetadataConverter> metadataConverters) {
        this.metadataConverters = metadataConverters;
    }
    public void addMetadataConverter(MetadataConverter converter){
        this.metadataConverters.add(converter);
    }
    public void removeMetadataConverter(MetadataConverter converter){
        this.metadataConverters.remove(converter);
    }
}
