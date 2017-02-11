package org.wso2.carbon.identity.gateway.store;


import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProvider;
import org.wso2.carbon.identity.gateway.common.model.idp.IdentityProviderConfig;

import java.util.HashMap;
import java.util.Map;

public class IdentityProviderConfigStore {
    private Map<String,IdentityProvider> idpEntityMap = new HashMap<>();
    private static IdentityProviderConfigStore identityProviderConfigStore = new IdentityProviderConfigStore();


    private IdentityProviderConfigStore(){

    }
    public static IdentityProviderConfigStore getInstance(){
        return IdentityProviderConfigStore.identityProviderConfigStore ;
    }

    public IdentityProvider getIdentityProvider(String idpName){
        IdentityProvider identityProvider = idpEntityMap.get(idpName);
        return identityProvider ;
    }

    public void addIdentityProvider(IdentityProvider identityProvider){
        if(identityProvider != null){
            idpEntityMap.put(identityProvider.getIdentityProviderConfig().getName(), identityProvider);
        }
    }

    public boolean validate(IdentityProvider identityProvider){
        return true ;
    }
}
