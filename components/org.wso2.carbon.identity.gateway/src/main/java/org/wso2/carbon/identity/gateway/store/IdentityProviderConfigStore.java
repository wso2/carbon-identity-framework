package org.wso2.carbon.identity.gateway.store;


import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;

import java.util.HashMap;
import java.util.Map;

public class IdentityProviderConfigStore {
    private Map<String,IdentityProviderConfig> idpEntityMap = new HashMap<>();
    private static IdentityProviderConfigStore identityProviderConfigStore = new IdentityProviderConfigStore();


    private IdentityProviderConfigStore(){

    }
    public static IdentityProviderConfigStore getInstance(){
        return IdentityProviderConfigStore.identityProviderConfigStore ;
    }

    public IdentityProviderConfig getIdentityProvider(String idpName){
        IdentityProviderConfig identityProvider = idpEntityMap.get(idpName);
        return identityProvider ;
    }

    public void addIdentityProvider(IdentityProviderConfig identityProvider){
        if(identityProvider != null){
            idpEntityMap.put(identityProvider.getName(), identityProvider);
        }
    }

    public boolean validate(IdentityProviderConfig identityProvider){
        return true ;
    }
}
