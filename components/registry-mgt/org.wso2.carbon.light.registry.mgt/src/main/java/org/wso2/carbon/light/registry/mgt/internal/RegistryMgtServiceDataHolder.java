package org.wso2.carbon.light.registry.mgt.internal;

public class RegistryMgtServiceDataHolder {

    private RegistryMgtServiceDataHolder() {

    }

    private static final RegistryMgtServiceDataHolder instance = new RegistryMgtServiceDataHolder();

    /**
     * Get the RegistryMgtServiceDataHolder instance.
     *
     * @return RegistryMgtServiceDataHolder instance.
     */
    public static RegistryMgtServiceDataHolder getInstance() {

        return instance;
    }
}
