package org.wso2.carbon.identity.vc.config.management.internal;

import org.wso2.carbon.identity.api.resource.mgt.APIResourceManager;

/**
 * Data holder for VC Config Management Service.
 */
public class VCConfigManagementServiceDataHolder {

    private APIResourceManager apiResourceManager;

    public static final VCConfigManagementServiceDataHolder INSTANCE = new VCConfigManagementServiceDataHolder();

    private VCConfigManagementServiceDataHolder() {

    }

    /**
     * Get the instance of VCConfigManagementServiceDataHolder.
     *
     * @return VCConfigManagementServiceDataHolder instance.
     */
    public static VCConfigManagementServiceDataHolder getInstance() {

        return INSTANCE;
    }

    public APIResourceManager getAPIResourceManager() {

        return apiResourceManager;
    }

    public void setAPIResourceManager(APIResourceManager apiResourceManager) {

        this.apiResourceManager = apiResourceManager;
    }
}
