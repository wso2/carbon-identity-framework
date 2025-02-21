package org.wso2.carbon.identity.application.common.internal;

import org.wso2.carbon.identity.action.management.api.service.ActionManagementService;
import org.wso2.carbon.identity.application.common.ApplicationAuthenticatorService;

/**
 * The data holder for the Application Common Service Component.
 */
public class ApplicationCommonServiceDataHolder {

    private static final ApplicationCommonServiceDataHolder INSTANCE = new ApplicationCommonServiceDataHolder();

    private ActionManagementService actionManagementService;
    private ApplicationAuthenticatorService applicationAuthenticatorService;

    /**
     * Get the instance of the ApplicationCommonServiceDataHolder.
     *
     * @return ApplicationCommonServiceDataHolder instance.
     */
    public static ApplicationCommonServiceDataHolder getInstance() {

        return INSTANCE;
    }

    /**
     * Get the ActionManagementService.
     *
     * @return ActionManagementService instance.
     */
    public ActionManagementService getActionManagementService() {

        return actionManagementService;
    }

    /**
     * Set the ActionManagementService.
     *
     * @param actionManagementService ActionManagementService instance.
     */
    public void setActionManagementService(ActionManagementService actionManagementService) {

        this.actionManagementService = actionManagementService;
    }

    /**
     * Get the ApplicationAuthenticatorService.
     *
     * @return ApplicationAuthenticatorService instance.
     */
    public ApplicationAuthenticatorService getApplicationAuthenticatorService() {

        return applicationAuthenticatorService;
    }

    /**
     * Set the ApplicationAuthenticatorService.
     *
     * @param applicationAuthenticatorService ApplicationAuthenticatorService instance.
     */
    public void setApplicationAuthenticatorService(ApplicationAuthenticatorService applicationAuthenticatorService) {

        this.applicationAuthenticatorService = applicationAuthenticatorService;
    }
}
