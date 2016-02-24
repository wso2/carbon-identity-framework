/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.entitlement.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.entitlement.internal.EntitlementServiceComponent;
import org.wso2.carbon.identity.entitlement.pdp.EntitlementEngine;
import org.wso2.carbon.identity.entitlement.pip.CarbonAttributeFinder;
import org.wso2.carbon.identity.entitlement.pip.PIPAttributeFinder;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserOperationEventListener;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This listener is registered as a user operation listener. Whenever a user operation takes place
 * this listener fires and responsible for clearing caches within entitlement engine. This will
 * clear all 3 caches including  PIP_ATTRIBUTE_CACHE, PDP_DECISION_INVALIDATION_CACHE,
 * ENTITLEMENT_POLICY_INVALIDATION_CACHE
 */
public class CacheClearingUserOperationListener extends AbstractUserOperationEventListener {

    private static final Log log = LogFactory.getLog(CacheClearingUserOperationListener.class);

    @Override
    public int getExecutionOrderId() {
        return 6;
    }

    /**
     * TThis method is overridden to clear caches on doPostDeleteUser operation
     *
     * @param userName         username
     * @param userStoreManager UserStoreManagerClass
     * @return Returns true always since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager) throws
                                                                                        UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post delete user operation for user " +
                      userName);
        }
        clearCarbonAttributeCache();
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        return true;
    }

    /**
     * This method is overridden to clear caches on doPostSetUserClaimValue operation
     *
     * @param userName         username
     * @param userStoreManager UserStoreManagerClass
     * @return Returns true always since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post set user claim value operation for user "
                      + userName);
        }
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        clearCarbonAttributeCache();
        return true;
    }

    /**
     * This method is overridden to clear caches on doPostSetUserClaimValues operation
     *
     * @param userName         Username of subjected user for claim updating
     * @param claims           Set of updated claims
     * @param profileName      Name of the profile
     * @param userStoreManager UserStoreManager instance got called
     * @return Always returns true since no major effect on further operations
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims,
                                            String profileName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post set user claim values operation for " +
                      "user " + userName);
        }
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        clearCarbonAttributeCache();
        return true;
    }

    /**
     * TThis method is overridden to clear caches on doPostDeleteUserClaimValues operation
     *
     * @param userName         username
     * @param userStoreManager UserStoreManagerClass
     * @return Returns true always since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUserClaimValues(String userName,
                                               UserStoreManager userStoreManager) throws
                                                                                  UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post delete user claim values operation for " +
                      "user " + userName);
        }
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        clearCarbonAttributeCache();
        return true;
    }

    /**
     * This method is overridden to clear caches on doPostDeleteUserClaimValue operation
     *
     * @param userName         username
     * @param userStoreManager UserStoreManagerClass
     * @return Always Returns true, since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post delete user claim value operation for " +
                      "user " + userName);
        }
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        clearCarbonAttributeCache();
        return true;
    }

    /**
     * This method is overridden to clear caches on doPostAddRole operation
     *
     * @param roleName         Name of the added role
     * @param userList         List of the users who got added the role
     * @param permissions      set of permissions
     * @param userStoreManager UserStoreManager instance got called
     * @return Always Returns true, since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostAddRole(String roleName, String[] userList, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post add role operation for role " +
                      roleName);
        }
        clearCarbonAttributeCache();
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        return true;
    }

    /**
     * This method is overridden to clear caches on doPostDeleteRole operation
     *
     * @param roleName         Deleted role name
     * @param userStoreManager UserStoreManagerClass
     * @return Always Returns true, since no major effect on further procedures
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostDeleteRole(String roleName, UserStoreManager userStoreManager) throws
                                                                                        UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post delete role operation for role " +
                      roleName);
        }
        clearCarbonAttributeCache();
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        return true;
    }

    /**
     * @param roleName         Old role name of the updating role
     * @param newRoleName      New role name of the updating role name
     * @param userStoreManager UserStoreManager instance got called
     * @return Always returns true since no major effect on further procedure.
     * @throws org.wso2.carbon.user.core.UserStoreException
     */
    @Override
    public boolean doPostUpdateRoleName(String roleName, String newRoleName,
                                        UserStoreManager userStoreManager) throws
                                                                           UserStoreException {
        if (log.isDebugEnabled()) {
            log.debug("Clearing entitlement cache on post update role operation for role " +
                      roleName);
        }
        clearCarbonAttributeCache();
        // Always returns true since cache clearing failure does not make an effect on subsequent
        // User Operation Listeners
        return true;
    }

    /**
     * this method is responsible for clearing all 3 major caches of entitlement engine
     * including  PIP_ATTRIBUTE_CACHE , PDP_DECISION_INVALIDATION_CACHE, ENTITLEMENT_POLICY_INVALIDATION_CACHE
     */
    private void clearCarbonAttributeCache() {

        CarbonAttributeFinder finder = EntitlementEngine.getInstance().getCarbonAttributeFinder();
        if (finder != null) {
            finder.clearAttributeCache();
            // we need to invalidate policy cache as well. Decision cache is cleared within
            // clearAttributeCache.
            EntitlementEngine.getInstance().getPolicyCache().invalidateCache();
        } else {
            // Return if no finders are found
            return;
        }
        // clearing pip attribute finder caches
        Map<PIPAttributeFinder, Properties> designators =
                EntitlementServiceComponent.getEntitlementConfig()
                        .getDesignators();
        if (designators != null && !designators.isEmpty()) {

            Set<PIPAttributeFinder> pipAttributeFinders = designators.keySet();
            for (PIPAttributeFinder pipAttributeFinder : pipAttributeFinders) {
                pipAttributeFinder.clearCache();
            }
        }
    }
}
