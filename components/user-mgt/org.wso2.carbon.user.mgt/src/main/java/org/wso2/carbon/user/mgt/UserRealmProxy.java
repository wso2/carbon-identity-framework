/*
 * Copyright (c) 2007 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.user.mgt;


import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.user.api.Claim;
import org.wso2.carbon.user.api.ClaimMapping;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealmService;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.bulkimport.BulkImportConfig;
import org.wso2.carbon.user.mgt.bulkimport.CSVUserBulkImport;
import org.wso2.carbon.user.mgt.bulkimport.ExcelUserBulkImport;
import org.wso2.carbon.user.mgt.bulkimport.UserBulkImport;
import org.wso2.carbon.user.mgt.common.ClaimValue;
import org.wso2.carbon.user.mgt.common.FlaggedName;
import org.wso2.carbon.user.mgt.common.UIPermissionNode;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.user.mgt.common.UserRealmInfo;
import org.wso2.carbon.user.mgt.common.UserStoreInfo;
import org.wso2.carbon.user.mgt.internal.UserMgtDSComponent;
import org.wso2.carbon.user.mgt.permission.ManagementPermissionUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserRealmProxy {

    private static final Log log = LogFactory.getLog(UserRealmProxy.class);

    private static final String APPLICATIONS_PATH = RegistryConstants.PATH_SEPARATOR
            + CarbonConstants.UI_PERMISSION_NAME + RegistryConstants.PATH_SEPARATOR
            + "applications";
    private static final String DISPLAY_NAME_CLAIM = "http://wso2.org/claims/displayName";
    private static final String DISPLAY_NAME_ATTRIBUTE = "DisplayNameAttribute";

    public static final String FALSE = "false";
    public static final String PERMISSION = "/permission";
    public static final String PERMISSION_TREE = "/permission/";
    public static final String PERMISSION_ADMIN = "/permission/admin";
    public static final String PERMISSION_ADMIN_TREE = "/permission/admin/";
    public static final String PERMISSION_PROTECTED = "/permission/protected";
    public static final String PERMISSION_PROTECTED_TREE = "/permission/protected/";
    private UserRealm realm = null;

    public UserRealmProxy(UserRealm userRealm) {
        this.realm = userRealm;
    }

    public String[] listUsers(String filter, int maxLimit) throws UserAdminException {
        try {
            return realm.getUserStoreManager().listUsers(filter, maxLimit);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public FlaggedName[] listUsers(ClaimValue claimValue, String filter, int maxLimit) throws UserAdminException {

        try {
            String[] usersWithClaim = null;

            if (claimValue.getClaimURI() != null && claimValue.getValue() != null) {
                usersWithClaim = realm.getUserStoreManager().getUserList(claimValue.getClaimURI(),
                        claimValue.getValue(), null);
            }
            int i = 0;
            FlaggedName[] flaggedNames = new FlaggedName[0];
            if (usersWithClaim != null) {
                flaggedNames = new FlaggedName[usersWithClaim.length + 1];

                Arrays.sort(usersWithClaim);
                // Check whether to use the display name claim when filtering users.
                String showDisplayName = IdentityUtil.getProperty(IdentityConstants.SHOW_DISPLAY_NAME);
                boolean isShowDisplayNameEnabled = Boolean.parseBoolean(showDisplayName);

                // Check for display name attribute mappings and retrieve the relevant claim uri.
                String displayNameAttribute = realm.getRealmConfiguration()
                        .getUserStoreProperty(DISPLAY_NAME_ATTRIBUTE);
                String displayNameClaimURI = DISPLAY_NAME_CLAIM;
                if (StringUtils.isNotBlank(displayNameAttribute)) {
                    ClaimMapping[] claimMappings = realm.getClaimManager().getAllClaimMappings();
                    if (claimMappings != null) {
                        for (ClaimMapping claimMapping: claimMappings) {
                            if (displayNameAttribute.equals(claimMapping.getMappedAttribute())) {
                                displayNameClaimURI = claimMapping.getClaim().getClaimUri();
                                break;
                            }
                        }
                    }
                }

                for (String user : usersWithClaim) {
                    flaggedNames[i] = new FlaggedName();
                    flaggedNames[i].setItemName(user);
                    // Retrieving the displayName.
                    String displayName = null;
                    if (StringUtils.isNotBlank(displayNameAttribute) || isShowDisplayNameEnabled) {
                        displayName = realm.getUserStoreManager().getUserClaimValue(user, displayNameClaimURI,
                                null);
                    }

                    if (StringUtils.isNotBlank(displayName)) {
                        int index = user.indexOf(UserCoreConstants.DOMAIN_SEPARATOR);
                        if (index > 0) {
                            flaggedNames[i].setItemDisplayName(user.substring(0, index + 1) + displayName);
                        } else {
                            flaggedNames[i].setItemDisplayName(displayName);
                        }
                    } else {
                        flaggedNames[i].setItemDisplayName(user);
                    }

                    int index1 = flaggedNames[i].getItemName() != null ? flaggedNames[i].getItemName().
                            indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
                    boolean domainProvided = index1 > 0;
                    String domain = domainProvided ? flaggedNames[i].getItemName().substring(0, index1) : null;
                    if (domain != null && !UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) &&
                            !UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                        UserStoreManager secondaryUM = realm.getUserStoreManager().getSecondaryUserStoreManager(domain);
                        if (secondaryUM != null && secondaryUM.isReadOnly()) {
                            flaggedNames[i].setEditable(false);
                        } else {
                            flaggedNames[i].setEditable(true);
                        }
                    } else {
                        if (realm.getUserStoreManager().isReadOnly()) {
                            flaggedNames[i].setEditable(false);
                        } else {
                            flaggedNames[i].setEditable(true);
                        }
                    }
                    i++;
                }
                if (usersWithClaim.length > 0) { //tail flagged name is added to handle pagination
                    FlaggedName flaggedName = new FlaggedName();
                    flaggedName.setItemName(FALSE);
                    flaggedName.setDomainName("");
                    flaggedNames[flaggedNames.length - 1] = flaggedName;
                }
            }
            return flaggedNames;
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public FlaggedName[] listAllUsers(String filter, int maxLimit) throws UserAdminException {
        FlaggedName[] flaggedNames = null;
        Map<String, Integer> userCount = new HashMap<String, Integer>();
        try {
            UserStoreManager userStoreManager = realm.getUserStoreManager();
            String[] users = userStoreManager.listUsers(filter, maxLimit);
            RealmConfiguration realmConfig = userStoreManager.getRealmConfiguration();
            flaggedNames = new FlaggedName[users.length + 1];
            int i = 0;
            for (String user : users) {
                flaggedNames[i] = new FlaggedName();
                //check if display name present
                int index = user.indexOf(UserCoreConstants.NAME_COMBINER);
                if (index > 0) { //if display name is appended
                    flaggedNames[i].setItemName(user.substring(0, index));
                    flaggedNames[i].setItemDisplayName(user.substring(index + UserCoreConstants.NAME_COMBINER.length()));
                } else {
                    //if only user name is present
                    flaggedNames[i].setItemName(user);
                    // set Display name as the item name
                    flaggedNames[i].setItemDisplayName(user);
                }
                int index1 = flaggedNames[i].getItemName() != null
                        ? flaggedNames[i].getItemName().indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
                boolean domainProvided = index1 > 0;
                String domain = domainProvided ? flaggedNames[i].getItemName().substring(0, index1) : null;
                if (StringUtils.isBlank(domain)) {
                    domain = UserCoreUtil.getDomainName(realmConfig);
                }
                if (domain != null && !UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) &&
                        !UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                    UserStoreManager secondaryUM =
                            realm.getUserStoreManager().getSecondaryUserStoreManager(domain);
                    if (secondaryUM != null && secondaryUM.isReadOnly()) {
                        flaggedNames[i].setEditable(false);
                    } else {
                        flaggedNames[i].setEditable(true);
                    }
                } else {
                    if (realm.getUserStoreManager().isReadOnly()) {
                        flaggedNames[i].setEditable(false);
                    } else {
                        flaggedNames[i].setEditable(true);
                    }
                }
                if (domain != null) {
                    if (userCount.containsKey(domain)) {
                        userCount.put(domain, userCount.get(domain) + 1);
                    } else {
                        userCount.put(domain, 1);
                    }
                } else {
                    if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                    } else {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                    }
                }
                i++;
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
        Arrays.sort(flaggedNames, new Comparator<FlaggedName>() {
            @Override
            public int compare(FlaggedName o1, FlaggedName o2) {
                if (o1 == null || o2 == null) {
                    return 0;
                }
                return o1.getItemName().toLowerCase().compareTo(o2.getItemName().toLowerCase());
            }
        });
        String exceededDomains = "";
        boolean isPrimaryExceeding = false;
        try {
            Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.getUserStoreManager()).
                    getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST);
            String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
            for (int i = 0; i < domains.length; i++) {
                if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equalsIgnoreCase(domains[i])) {
                    if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).intValue() ==
                            maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).intValue()) {
                        isPrimaryExceeding = true;
                    }
                    continue;
                }
                if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                    exceededDomains += domains[i];
                    if (i != domains.length - 1) {
                        exceededDomains += ":";
                    }
                }
            }
            FlaggedName flaggedName = new FlaggedName();
            if (isPrimaryExceeding) {
                flaggedName.setItemName("true");
            } else {
                flaggedName.setItemName(FALSE);
            }
            flaggedName.setItemDisplayName(exceededDomains);
            flaggedNames[flaggedNames.length - 1] = flaggedName;
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
        return flaggedNames;
    }

    public FlaggedName[] getAllSharedRoleNames(String filter, int maxLimit)
            throws UserAdminException {
        try {

            UserStoreManager userStoreMan = realm.getUserStoreManager();
            // get all roles without hybrid roles
            String[] externalRoles;
            if (userStoreMan instanceof AbstractUserStoreManager) {
                externalRoles =
                        ((AbstractUserStoreManager) userStoreMan).getSharedRoleNames(filter,
                                maxLimit);
            } else {
                throw new UserAdminException(
                        "Initialized User Store Manager is not capable of getting the shared roles");
            }

            List<FlaggedName> flaggedNames = new ArrayList<FlaggedName>();
            Map<String, Integer> userCount = new HashMap<String, Integer>();

            for (String externalRole : externalRoles) {
                FlaggedName fName = new FlaggedName();
                mapEntityName(externalRole, fName, userStoreMan);
                fName.setRoleType(UserMgtConstants.EXTERNAL_ROLE);

                // setting read only or writable
                int index = externalRole != null ? externalRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
                boolean domainProvided = index > 0;
                String domain = domainProvided ? externalRole.substring(0, index) : null;
                UserStoreManager secManager =
                        realm.getUserStoreManager()
                                .getSecondaryUserStoreManager(domain);

                if (domain != null && !UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) &&
                        !UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                    if (secManager != null &&
                            (secManager.isReadOnly() || (secManager.getRealmConfiguration()
                                    .getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null &&
                                    FALSE.equals(secManager.getRealmConfiguration().
                                            getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))))) {
                        fName.setEditable(false);
                    } else {
                        fName.setEditable(true);
                    }
                }
                if (domain != null) {
                    if (userCount.containsKey(domain)) {
                        userCount.put(domain, userCount.get(domain) + 1);
                    } else {
                        userCount.put(domain, 1);
                    }
                } else {
                    if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                    } else {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                    }
                }
                flaggedNames.add(fName);
            }

            String exceededDomains = "";
            boolean isPrimaryExceeding = false;
            Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.
                    getUserStoreManager()).getMaxListCount(UserCoreConstants.
                    RealmConfig.PROPERTY_MAX_ROLE_LIST);
            String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
            for (int i = 0; i < domains.length; i++) {
                if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                    if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                            equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                        isPrimaryExceeding = true;
                    }
                    continue;
                }
                if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                    exceededDomains += domains[i];
                    if (i != domains.length - 1) {
                        exceededDomains += ":";
                    }
                }
            }
            FlaggedName[] roleNames =
                    flaggedNames.toArray(new FlaggedName[flaggedNames.size() + 1]);
            Arrays.sort(roleNames, new Comparator<FlaggedName>() {
                @Override
                public int compare(FlaggedName o1, FlaggedName o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return o1.getItemName().toLowerCase().compareTo(o2.getItemName().toLowerCase());
                }
            });
            FlaggedName flaggedName = new FlaggedName();
            if (isPrimaryExceeding) {
                flaggedName.setItemName("true");
            } else {
                flaggedName.setItemName(FALSE);
            }
            flaggedName.setItemDisplayName(exceededDomains);
            roleNames[roleNames.length - 1] = flaggedName;
            return roleNames;

        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public FlaggedName[] getAllRolesNames(String filter, int maxLimit) throws UserAdminException {
        try {

            UserStoreManager userStoreMan = realm.getUserStoreManager();
            //get all roles without hybrid roles
            String[] externalRoles;
            if (userStoreMan instanceof AbstractUserStoreManager) {
                externalRoles = ((AbstractUserStoreManager) userStoreMan).getRoleNames(filter,
                        maxLimit, true, true, true);
            } else {
                externalRoles = userStoreMan.getRoleNames();
            }

            List<FlaggedName> flaggedNames = new ArrayList<FlaggedName>();
            Map<String, Integer> userCount = new HashMap<String, Integer>();

            for (String externalRole : externalRoles) {
                FlaggedName fName = new FlaggedName();
                mapEntityName(externalRole, fName, userStoreMan);
                fName.setRoleType(UserMgtConstants.EXTERNAL_ROLE);

                // setting read only or writable
                int index = externalRole != null ? externalRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
                boolean domainProvided = index > 0;
                String domain = domainProvided ? externalRole.substring(0, index) : null;
                UserStoreManager secManager = realm.getUserStoreManager().getSecondaryUserStoreManager(domain);

                if (domain != null && !UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) &&
                        !UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                    if (secManager != null && (secManager.isReadOnly() ||
                            (FALSE.equals(secManager.getRealmConfiguration().
                                    getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))))) {
                        fName.setEditable(false);
                    } else {
                        fName.setEditable(true);
                    }
                } else {
                    if (realm.getUserStoreManager().isReadOnly() ||
                            (FALSE.equals(realm.getUserStoreManager().getRealmConfiguration().
                                    getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) {
                        fName.setEditable(false);
                    } else {
                        fName.setEditable(true);
                    }
                }
                if (domain != null) {
                    if (userCount.containsKey(domain)) {
                        userCount.put(domain, userCount.get(domain) + 1);
                    } else {
                        userCount.put(domain, 1);
                    }
                } else {
                    if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                    } else {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                    }
                }
                flaggedNames.add(fName);
            }

            String filteredDomain = null;
            // get hybrid roles
            if (filter.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                filteredDomain = filter.split(CarbonConstants.DOMAIN_SEPARATOR)[0];
            }

            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR)) {
                filter = filter.substring(filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR) + 1);
            }

            String[] hybridRoles = ((AbstractUserStoreManager) userStoreMan).getHybridRoles(filter);

            // Filter the internal system roles created to maintain the backward compatibility.
            hybridRoles = filterInternalSystemRoles(hybridRoles);

            for (String hybridRole : hybridRoles) {
                if (filteredDomain != null && !hybridRole.startsWith(filteredDomain)) {
                    continue;
                }
                FlaggedName fName = new FlaggedName();
                fName.setItemName(hybridRole);
                if (hybridRole.toLowerCase().startsWith(UserCoreConstants.INTERNAL_DOMAIN.toLowerCase())) {
                    fName.setRoleType(UserMgtConstants.INTERNAL_ROLE);
                } else {
                    fName.setRoleType(UserMgtConstants.APPLICATION_DOMAIN);
                }
                fName.setEditable(true);
                flaggedNames.add(fName);
            }
            String exceededDomains = "";
            boolean isPrimaryExceeding = false;
            Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.getUserStoreManager()).
                    getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST);
            String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
            for (int i = 0; i < domains.length; i++) {
                if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                    if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                            equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                        isPrimaryExceeding = true;
                    }
                    continue;
                }
                if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                    exceededDomains += domains[i];
                    if (i != domains.length - 1) {
                        exceededDomains += ":";
                    }
                }
            }
            FlaggedName[] roleNames = flaggedNames.toArray(new FlaggedName[flaggedNames.size() + 1]);
            Arrays.sort(roleNames, new Comparator<FlaggedName>() {
                @Override
                public int compare(FlaggedName o1, FlaggedName o2) {
                    if (o1 == null || o2 == null) {
                        return 0;
                    }
                    return o1.getItemName().toLowerCase().compareTo(o2.getItemName().toLowerCase());
                }
            });
            FlaggedName flaggedName = new FlaggedName();
            if (isPrimaryExceeding) {
                flaggedName.setItemName("true");
            } else {
                flaggedName.setItemName(FALSE);
            }
            flaggedName.setItemDisplayName(exceededDomains);
            roleNames[roleNames.length - 1] = flaggedName;
            return roleNames;

        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }

    }

    private String[] filterInternalSystemRoles(String[] roles) throws UserAdminException {

        if (isRoleAndGroupSeparationEnabled()) {
            List<String> hybridRolesList = new ArrayList<>(Arrays.asList(roles));
            hybridRolesList.removeIf(s -> s.startsWith(
                    UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR
                            + UserCoreConstants.INTERNAL_SYSTEM_ROLE_PREFIX));

            roles = hybridRolesList.toArray(new String[0]);
        }
        return roles;
    }

    public UserRealmInfo getUserRealmInfo() throws UserAdminException {

        UserRealmInfo userRealmInfo = new UserRealmInfo();

        String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();

        try {

            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            if (realm.getAuthorizationManager().isUserAuthorized(userName,
                    "/permission/admin/manage/identity", CarbonConstants.UI_PERMISSION_ACTION) ||
                    realm.getAuthorizationManager().isUserAuthorized(userName,
                            "/permission/admin/manage/identity/usermgt/users", CarbonConstants.UI_PERMISSION_ACTION)
                    || realm.getAuthorizationManager().isUserAuthorized(userName,
                    "/permission/admin/manage/identity/usermgt/passwords",
                    CarbonConstants.UI_PERMISSION_ACTION) ||
                    realm.getAuthorizationManager().isUserAuthorized(userName,
                            "/permission/admin/manage/identity/usermgt/view", CarbonConstants.UI_PERMISSION_ACTION) ||
                    realm.getAuthorizationManager().isUserAuthorized(userName,
                            "/permission/admin/manage/identity/rolemgt/view", CarbonConstants.UI_PERMISSION_ACTION)) {

                userRealmInfo.setAdminRole(realmConfig.getAdminRoleName());
                userRealmInfo.setAdminUser(realmConfig.getAdminUserName());
                userRealmInfo.setEveryOneRole(realmConfig.getEveryOneRoleName());
                ClaimMapping[] defaultClaims = realm.getClaimManager().
                        getAllClaimMappings(UserCoreConstants.DEFAULT_CARBON_DIALECT);
                if (ArrayUtils.isNotEmpty(defaultClaims)) {
                    Arrays.sort(defaultClaims, new ClaimMappingsComparator());
                }
                List<String> fullClaimList = new ArrayList<String>();
                List<String> requiredClaimsList = new ArrayList<String>();
                List<String> defaultClaimList = new ArrayList<String>();

                for (ClaimMapping claimMapping : defaultClaims) {
                    Claim claim = claimMapping.getClaim();

                    fullClaimList.add(claim.getClaimUri());
                    if (claim.isRequired()) {
                        requiredClaimsList.add(claim.getClaimUri());
                    }
                    if (claim.isSupportedByDefault()) {
                        defaultClaimList.add(claim.getClaimUri());
                    }
                }

                userRealmInfo.setUserClaims(fullClaimList.toArray(new String[fullClaimList.size()]));
                userRealmInfo.setRequiredUserClaims(requiredClaimsList.toArray(new String[requiredClaimsList.size()]));
                userRealmInfo.setDefaultUserClaims(defaultClaimList.toArray(new String[defaultClaimList.size()]));
            }

            List<UserStoreInfo> storeInfoList = new ArrayList<UserStoreInfo>();
            List<String> domainNames = new ArrayList<String>();
            RealmConfiguration secondaryConfig = realmConfig;
            UserStoreManager secondaryManager = realm.getUserStoreManager();

            while (true) {

                secondaryConfig = secondaryManager.getRealmConfiguration();
                UserStoreInfo userStoreInfo = getUserStoreInfo(secondaryConfig, secondaryManager);
                if (secondaryConfig.isPrimary()) {
                    userRealmInfo.setPrimaryUserStoreInfo(userStoreInfo);
                }
                storeInfoList.add(userStoreInfo);
                userRealmInfo.setBulkImportSupported(secondaryManager.isBulkImportSupported());
                String domainName = secondaryConfig
                        .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);
                if (domainName != null && domainName.trim().length() > 0) {
                    domainNames.add(domainName.toUpperCase());
                }
                secondaryManager = secondaryManager.getSecondaryUserStoreManager();
                if (secondaryManager == null) {
                    break;
                }
            }

            if (storeInfoList.size() > 1) {
                userRealmInfo.setMultipleUserStore(true);
            }

            userRealmInfo.setUserStoresInfo(storeInfoList.toArray(new UserStoreInfo[storeInfoList.size()]));
            userRealmInfo.setDomainNames(domainNames.toArray(new String[domainNames.size()]));

            String itemsPerPageString = realmConfig.getRealmProperty("MaxItemsPerUserMgtUIPage");
            int itemsPerPage = 15;
            try {
                itemsPerPage = Integer.parseInt(itemsPerPageString);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error parsing number of items per page, using default value", e);
                }
            }
            userRealmInfo.setMaxItemsPerUIPage(itemsPerPage);

            String maxPageInCacheString = realmConfig.getRealmProperty("MaxUserMgtUIPagesInCache");
            int maxPagesInCache = 6;
            try {
                maxPagesInCache = Integer.parseInt(maxPageInCacheString);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error parsing number of maximum pages in cache, using default value", e);
                }
            }
            userRealmInfo.setMaxUIPagesInCache(maxPagesInCache);


            String enableUIPageCacheString = realmConfig.getRealmProperty("EnableUserMgtUIPageCache");
            boolean enableUIPageCache = true;
            if (FALSE.equals(enableUIPageCacheString)) {
                enableUIPageCache = false;
            }
            userRealmInfo.setEnableUIPageCache(enableUIPageCache);

        } catch (Exception e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        }

        return userRealmInfo;
    }

    private UserStoreInfo getUserStoreInfo(RealmConfiguration realmConfig,
                                           UserStoreManager manager) throws UserAdminException {
        try {

            UserStoreInfo info = new UserStoreInfo();

            info.setReadOnly(manager.isReadOnly());

            boolean readRolesEnabled = Boolean.parseBoolean(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.READ_GROUPS_ENABLED));
            info.setReadGroupsEnabled(readRolesEnabled);
            boolean writeRolesEnabled = Boolean.parseBoolean(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED));
            info.setWriteGroupsEnabled(!manager.isReadOnly() && readRolesEnabled && writeRolesEnabled);
            info.setPasswordsExternallyManaged(realmConfig.isPasswordsExternallyManaged());
            info.setPasswordRegEx(realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_JS_REG_EX));

            info.setPasswordRegExViolationErrorMsg(realmConfig
                    .getUserStoreProperty("PasswordJavaRegExViolationErrorMsg"));
            //TODO  Need to get value from UserCoreConstants.RealmConfig.PROPERTY_PASSWORD_ERROR_MSG

            info.setUsernameRegExViolationErrorMsg(realmConfig
                    .getUserStoreProperty("UsernameJavaRegExViolationErrorMsg"));
            //TODO  Need to get value from UserCoreConstants.RealmConfig.PROPERTY_PASSWORD_ERROR_MSG

            info.setUserNameRegEx(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_NAME_JS_REG_EX));

            info.setRoleNameRegEx(
                    realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLE_NAME_JS_REG_EX));
            info.setExternalIdP(realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_EXTERNAL_IDP));

            info.setBulkImportSupported(this.isBulkImportSupported(realmConfig));
            info.setDomainName(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME));

            boolean caseSensitiveUsername = IdentityUtil.isUserStoreCaseSensitive(manager);

            info.setCaseSensitiveUsername(caseSensitiveUsername);
            return info;
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            String domainName = manager.getRealmConfiguration().getUserStoreProperty(UserCoreConstants.RealmConfig
                    .PROPERTY_DOMAIN_NAME);
            String errorMsg = "Error while getting user realm information for domain '" + domainName + "' : " + e
                    .getMessage();
            throw new UserAdminException(errorMsg, e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }


    private boolean isBulkImportSupported(RealmConfiguration realmConfig) throws UserAdminException {
            if (realmConfig != null) {
                return Boolean.valueOf(realmConfig.getUserStoreProperties().get("IsBulkImportSupported"));
            } else {
                throw new UserAdminException("Unable to retrieve user store manager from realm.");
            }
    }

    /**
     * Add the primary domain to the user name if username does not contain a domain
     *
     * @param userName Logged in username
     * @return Primary domain added username
     */
    private final String addPrimaryDomainIfNotExists(String userName) {

        if (StringUtils.isNotEmpty(userName) && (!userName.contains(UserCoreConstants.DOMAIN_SEPARATOR))) {
            StringBuilder builder = new StringBuilder();
            builder.append(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).append(CarbonConstants.DOMAIN_SEPARATOR)
                    .append(userName);
            userName = builder.toString();
        }
        return userName;
    }

    public void addUser(String userName, String password, String[] roles, ClaimValue[] claims,
                        String profileName) throws UserAdminException {
        try {
            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            if (realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_EXTERNAL_IDP) != null) {
                throw new UserAdminException(
                        "Please contact your external Identity Provider to add users");
            }

            // check whether login-in user has privilege to add user with admin privileges
            if (roles != null && roles.length > 0) {
                String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
                String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
                Arrays.sort(roles);
                boolean isRoleHasAdminPermission = false;
                for (String role : roles) {
                    isRoleHasAdminPermission = realm.getAuthorizationManager().
                            isRoleAuthorized(role, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                    if (!isRoleHasAdminPermission) {
                        isRoleHasAdminPermission = realm.getAuthorizationManager().
                                isRoleAuthorized(role, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                    }

                    if (isRoleHasAdminPermission) {
                        break;
                    }
                }

                if (isRoleHasAdminPermission &&
                        !adminUser.equalsIgnoreCase(loggedInUserName)) {
                    log.warn("An attempt to assign user " + userName + " " +
                            "to a role which has admin permission by user : " + loggedInUserName);
                    throw new UserStoreException("You do not have the required privilege to assign a user to a role " +
                            "which has admin permission.");
                }
            }
            UserStoreManager admin = realm.getUserStoreManager();
            Map<String, String> claimMap = new HashMap<String, String>();
            if (claims != null) {
                for (ClaimValue claimValue : claims) {
                    claimMap.put(claimValue.getClaimURI(), claimValue.getValue());
                }
            }
            admin.addUser(userName, password, roles, claimMap, profileName, false);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void changePassword(String userName, String newPassword) throws UserAdminException {
        try {

            String loggedInUserName = getLoggedInUser();
            if (loggedInUserName != null && loggedInUserName.equalsIgnoreCase(userName)) {
                log.warn("An attempt to change password with out providing old password : " +
                        loggedInUserName);
                throw new UserStoreException("An attempt to change password with out providing old password");
            }
            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            if (loggedInUserName != null) {
                loggedInUserName = addPrimaryDomainIfNotExists(loggedInUserName);
            }
            String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
            if (realmConfig.getAdminUserName().equalsIgnoreCase(userName) &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to change password of admin user by user : " + loggedInUserName);
                throw new UserStoreException("You do not have the required privilege to change the password of admin " +
                        "user");
            }

            if (userName != null) {
                boolean isUserHadAdminPermission;

                // check whether this user had admin permission
                isUserHadAdminPermission = realm.getAuthorizationManager().
                        isUserAuthorized(userName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                if (!isUserHadAdminPermission) {
                    isUserHadAdminPermission = realm.getAuthorizationManager().
                            isUserAuthorized(userName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                }

                if (isUserHadAdminPermission &&
                        !adminUser.equalsIgnoreCase(loggedInUserName)) {
                    log.warn("An attempt to change password of user has admin permission by user : " +
                            loggedInUserName);
                    throw new UserStoreException("You do not have the required privilege to change the password of a " +
                            "user with admin permission");
                }
            }
            realm.getUserStoreManager().updateCredentialByAdmin(userName, newPassword);
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void deleteUser(String userName, Registry registry) throws UserAdminException {
        try {
            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
            if (realmConfig.getAdminUserName().equalsIgnoreCase(userName) &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to delete the admin user by user : " + loggedInUserName);
                throw new UserStoreException("You do not have the required privilege to delete the admin user");
            }

            if (userName != null) {

                boolean isUserHadAdminPermission;

                // check whether this user had admin permission
                isUserHadAdminPermission = realm.getAuthorizationManager().
                        isUserAuthorized(userName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                if (!isUserHadAdminPermission) {
                    isUserHadAdminPermission = realm.getAuthorizationManager().
                            isUserAuthorized(userName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                }

                if (isUserHadAdminPermission &&
                        !adminUser.equalsIgnoreCase(loggedInUserName)) {
                    log.warn("An attempt to delete a user who has admin permission by user : " + loggedInUserName);
                    throw new UserStoreException("You do not have the required privilege to delete a user who has " +
                            "admin permission");
                }
            }

            realm.getUserStoreManager().deleteUser(userName);
            String path = RegistryConstants.PROFILES_PATH + userName;
            if (registry.resourceExists(path)) {
                registry.delete(path);
            }
        } catch (RegistryException e) {
            String msg = "Error deleting user from registry " + e.getMessage();
            log.error(msg, e);
            throw new UserAdminException(msg, e);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void addRole(String roleName, String[] userList, String[] permissions,
                        boolean isSharedRole) throws UserAdminException {
        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            String adminUserName = addPrimaryDomainIfNotExists(realm.getRealmConfiguration().getAdminUserName());
            if (permissions != null &&
                    !adminUserName.equalsIgnoreCase(loggedInUserName)) {
                Arrays.sort(permissions);
                if (Arrays.binarySearch(permissions, PERMISSION_ADMIN) > -1 ||
                        Arrays.binarySearch(permissions, "/permission/admin/") > -1 ||
                        Arrays.binarySearch(permissions, PERMISSION) > -1 ||
                        Arrays.binarySearch(permissions, "/permission/") > -1 ||
                        Arrays.binarySearch(permissions, "/permission/protected") > -1 ||
                        Arrays.binarySearch(permissions, "/permission/protected/") > -1) {
                    log.warn("An attempt to create a role with admin permission by user " + loggedInUserName);
                    throw new UserStoreException("You do not have the required privilege to create a role with admin " +
                            "permission");
                }
            }

            UserStoreManager usAdmin = realm.getUserStoreManager();
            UserStoreManager secManager = null;

            if (roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                secManager = usAdmin.getSecondaryUserStoreManager(roleName.substring(0, roleName.indexOf
                        (UserCoreConstants.DOMAIN_SEPARATOR)));
            } else {
                secManager = usAdmin;
            }

            if (secManager == null) {
                throw new UserAdminException("Invalid Domain");
            }

            if (!secManager.isReadOnly()
                    && !FALSE.equals(secManager.getRealmConfiguration().
                    getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))) {
                usAdmin.addRole(roleName,
                        userList,
                        ManagementPermissionUtil.getRoleUIPermissions(roleName, permissions),
                        isSharedRole);
            } else {
                throw new UserAdminException("Read only user store or Role creation is disabled");
            }
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to add the role: %s, to the user store", roleName), e);
            }
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to add the role: %s", roleName), e);
            }
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void addInternalRole(String roleName, String[] userList, String[] permissions)
            throws UserAdminException {
        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            String adminUser = addPrimaryDomainIfNotExists(realm.getRealmConfiguration().getAdminUserName());
            if (permissions != null &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                Arrays.sort(permissions);
                if (Arrays.binarySearch(permissions, PERMISSION_ADMIN) > -1 ||
                        Arrays.binarySearch(permissions, "/permission/admin/") > -1 ||
                        Arrays.binarySearch(permissions, PERMISSION) > -1 ||
                        Arrays.binarySearch(permissions, "/permission/") > -1 ||
                        Arrays.binarySearch(permissions, "/permission/protected") > -1 ||
                        Arrays.binarySearch(permissions, "/permission/protected/") > -1) {
                    log.warn("An attempt to create a role with admin permission by user " + loggedInUserName);
                    throw new UserStoreException("You do not have the required privilege to create a role with admin " +
                            "permission");
                }
            }

            UserStoreManager usAdmin = realm.getUserStoreManager();
            if (usAdmin instanceof AbstractUserStoreManager) {
                if ((roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR) && UserMgtConstants.APPLICATION_DOMAIN
                        .equals(roleName.substring(0, roleName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR))))) {
                    usAdmin.addRole(roleName, userList, null, false);
                } else {
                    if (roleName.startsWith(UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR)) {
                        usAdmin.addRole(roleName, userList, null, false);
                    } else {
                        usAdmin.addRole(UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR
                                + roleName, userList, null, false);
                    }
                }
            } else {
                throw new UserStoreException("Internal role can not be created");
            }
            // adding permission with internal domain name
            if ((roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR) && UserMgtConstants.APPLICATION_DOMAIN.equals
                    (roleName.substring(0, roleName.indexOf(UserCoreConstants.DOMAIN_SEPARATOR))))) {
                ManagementPermissionUtil.updateRoleUIPermission(roleName, permissions);
            } else {
                ManagementPermissionUtil.updateRoleUIPermission(UserCoreConstants.INTERNAL_DOMAIN
                        + UserCoreConstants.DOMAIN_SEPARATOR + roleName, permissions);
            }
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void updateRoleName(String roleName, String newRoleName)
            throws UserAdminException {
        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            String adminUser = addPrimaryDomainIfNotExists(realm.getRealmConfiguration().getAdminUserName());
            boolean isRoleHasAdminPermission;

            String roleWithoutDN = roleName.split(UserCoreConstants.TENANT_DOMAIN_COMBINER)[0];

            // check whether this role had admin permission
            isRoleHasAdminPermission = realm.getAuthorizationManager().
                    isRoleAuthorized(roleWithoutDN, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
            if (!isRoleHasAdminPermission) {
                isRoleHasAdminPermission = realm.getAuthorizationManager().
                        isRoleAuthorized(roleWithoutDN, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
            }

            if (isRoleHasAdminPermission &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to rename a role with admin permission by user " + loggedInUserName);
                throw new UserStoreException("You do not have the required privilege to rename a role with admin " +
                        "permission");
            }

            UserStoreManager usAdmin = realm.getUserStoreManager();
            usAdmin.updateRoleName(roleName, newRoleName);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void deleteRole(String roleName) throws UserAdminException {
        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            String adminUser = addPrimaryDomainIfNotExists(realm.getRealmConfiguration().getAdminUserName());
            boolean isRoleHasAdminPermission;

            // check whether this role had admin permission
            isRoleHasAdminPermission = realm.getAuthorizationManager().
                    isRoleAuthorized(roleName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
            if (!isRoleHasAdminPermission) {
                isRoleHasAdminPermission = realm.getAuthorizationManager().
                        isRoleAuthorized(roleName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
            }

            if (isRoleHasAdminPermission &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to delete a role with admin permission by user " + loggedInUserName);
                throw new UserStoreException("You do not have the required privilege to delete a role with admin " +
                        "permission");
            }

            realm.getUserStoreManager().deleteRole(roleName);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public FlaggedName[] getUsersOfRole(String roleName, String filter, int limit) throws UserAdminException {
        try {

            int index = roleName != null ? roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
            boolean domainProvided = index > 0;

            String domain = domainProvided ? roleName.substring(0, index) : null;

            if (domain != null && filter != null && !filter.toLowerCase().startsWith(domain.toLowerCase()) &&
                !(UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                  || UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain))) {
                filter = domain + CarbonConstants.DOMAIN_SEPARATOR + filter;
            }

            UserStoreManager usMan = realm.getUserStoreManager();
            String[] usersOfRole;
            boolean canLimitAndFilterUsersFromUMLevel = canLimitAndFilterUsersFromUMLevel(roleName, usMan);

            if (domain == null && limit != 0) {
                if (filter != null) {
                    filter = CarbonConstants.DOMAIN_SEPARATOR + filter;
                } else {
                    filter = "/*";
                }
            }

            /*
            With the fix delivered for https://github.com/wso2/product-is/issues/6511, limiting and filtering from
            the JDBC UserStoreManager is possible thus making the in-memory filtering and limiting logic in here
            irrelevant for JDBC UM. But still, Read Only LDAP UM does not supports DB level limiting and filtering
            (refer to https://github.com/wso2/product-is/issues/6573) thus the logic is kept as it is.
             */
            if (canLimitAndFilterUsersFromUMLevel) {
                int userCountLimit = getUserCountLimit(limit);
                String domainFreeFilter = getDomainFreeFilter(filter);

                AbstractUserStoreManager abstractUsMan = (AbstractUserStoreManager) usMan;
                usersOfRole = abstractUsMan.getUserListOfRole(roleName, domainFreeFilter, userCountLimit);
            } else {
                usersOfRole = usMan.getUserListOfRole(roleName);
            }
            Arrays.sort(usersOfRole);
            Map<String, Integer> userCount = new HashMap<String, Integer>();

            if (limit == 0) {
                filter = filter.replace("*", ".*");
                Pattern pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
                List<FlaggedName> flaggedNames = new ArrayList<FlaggedName>();
                for (String anUsersOfRole : usersOfRole) {
                    //check if display name is present in the user name
                    int combinerIndex = anUsersOfRole.indexOf(UserCoreConstants.NAME_COMBINER);
                    Matcher matcher;
                    if (combinerIndex > 0) {
                        matcher = pattern.matcher(anUsersOfRole.substring(combinerIndex +
                                UserCoreConstants.NAME_COMBINER.length()));
                    } else {
                        matcher = pattern.matcher(anUsersOfRole);
                    }
                    if (!matcher.matches()) {
                        continue;
                    }
                    FlaggedName fName = new FlaggedName();
                    fName.setSelected(true);
                    if (combinerIndex > 0) { //if display name is appended
                        fName.setItemName(anUsersOfRole.substring(0, combinerIndex));
                        fName.setItemDisplayName(anUsersOfRole.substring(combinerIndex +
                                UserCoreConstants.NAME_COMBINER.length()));
                    } else {
                        //if only user name is present
                        fName.setItemName(anUsersOfRole);
                        fName.setItemDisplayName(anUsersOfRole);
                    }
                    if (domain != null && !(UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain)
                                            || UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain))) {
                        if (usMan.getSecondaryUserStoreManager(domain) != null &&
                                (usMan.getSecondaryUserStoreManager(domain).isReadOnly() ||
                                        FALSE.equals(usMan.getSecondaryUserStoreManager(domain).getRealmConfiguration().
                                                getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) {
                            fName.setEditable(false);
                        } else {
                            fName.setEditable(true);
                        }
                    } else {
                        if (usMan.isReadOnly() || (usMan.getSecondaryUserStoreManager(domain) != null &&
                                FALSE.equals(usMan.getRealmConfiguration().
                                        getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) {
                            fName.setEditable(false);
                        } else {
                            fName.setEditable(true);
                        }
                    }
                    if (domain != null) {
                        if (userCount.containsKey(domain)) {
                            userCount.put(domain, userCount.get(domain) + 1);
                        } else {
                            userCount.put(domain, 1);
                        }
                    } else {
                        if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                    userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                        } else {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                        }
                    }
                    flaggedNames.add(fName);
                }
                String exceededDomains = "";
                boolean isPrimaryExceeding = false;
                Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.getUserStoreManager()).
                        getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST);
                String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
                for (int i = 0; i < domains.length; i++) {
                    if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                        if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                                equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                            isPrimaryExceeding = true;
                        }
                        continue;
                    }
                    if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                        exceededDomains += domains[i];
                        if (i != domains.length - 1) {
                            exceededDomains += ":";
                        }
                    }
                }
                FlaggedName flaggedName = new FlaggedName();
                if (isPrimaryExceeding) {
                    flaggedName.setItemName("true");
                } else {
                    flaggedName.setItemName(FALSE);
                }
                flaggedName.setItemDisplayName(exceededDomains);
                flaggedNames.add(flaggedName);
                return flaggedNames.toArray(new FlaggedName[flaggedNames.size()]);
            }

            String[] userNames = usMan.listUsers(filter, limit);
            FlaggedName[] flaggedNames = new FlaggedName[userNames.length + 1];
            for (int i = 0; i < userNames.length; i++) {
                FlaggedName fName = new FlaggedName();
                fName.setItemName(userNames[i]);
                if (Arrays.binarySearch(usersOfRole, userNames[i]) > -1) {
                    fName.setSelected(true);
                }
                //check if display name is present in the user name
                int combinerIndex = userNames[i].indexOf(UserCoreConstants.NAME_COMBINER);
                if (combinerIndex > 0) { //if display name is appended
                    fName.setItemName(userNames[i].substring(0, combinerIndex));
                    fName.setItemDisplayName(userNames[i].substring(combinerIndex +
                            UserCoreConstants.NAME_COMBINER.length()));
                } else {
                    //if only user name is present
                    fName.setItemName(userNames[i]);
                }
                if (domain != null && !(UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) ||
                                        UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain))) {
                    if (usMan.getSecondaryUserStoreManager(domain) != null &&
                            (usMan.getSecondaryUserStoreManager(domain).isReadOnly() ||
                                    FALSE.equals(usMan.getSecondaryUserStoreManager(domain).getRealmConfiguration().
                                            getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) {
                        fName.setEditable(false);
                    } else {
                        fName.setEditable(true);
                    }
                } else {
                    if (usMan.isReadOnly() || (usMan.getSecondaryUserStoreManager(domain) != null &&
                            FALSE.equals(usMan.getRealmConfiguration().
                                    getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) {
                        fName.setEditable(false);
                    } else {
                        fName.setEditable(true);
                    }
                }
                if (domain != null) {
                    if (userCount.containsKey(domain)) {
                        userCount.put(domain, userCount.get(domain) + 1);
                    } else {
                        userCount.put(domain, 1);
                    }
                } else {
                    if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                    } else {
                        userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                    }
                }
                flaggedNames[i] = fName;
            }
            String exceededDomains = "";
            boolean isPrimaryExceeding = false;
            Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.getUserStoreManager()).
                    getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST);
            String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
            for (int i = 0; i < domains.length; i++) {
                if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                    if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                            equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                        isPrimaryExceeding = true;
                    }
                    continue;
                }
                if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                    exceededDomains += domains[i];
                    if (i != domains.length - 1) {
                        exceededDomains += ":";
                    }
                }
            }
            FlaggedName flaggedName = new FlaggedName();
            if (isPrimaryExceeding) {
                flaggedName.setItemName("true");
            } else {
                flaggedName.setItemName(FALSE);
            }
            flaggedName.setItemDisplayName(exceededDomains);
            flaggedNames[flaggedNames.length - 1] = flaggedName;
            return flaggedNames;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public FlaggedName[] getRolesOfUser(String userName, String filter, int limit) throws UserAdminException {
        try {

            int index = userName != null ? userName.indexOf(CarbonConstants.DOMAIN_SEPARATOR) : -1;
            boolean domainProvided = index > 0;
            String domain = domainProvided ? userName.substring(0, index) : null;

            if (filter == null) {
                filter = "*";
            }

            UserStoreManager admin = realm.getUserStoreManager();
            String[] userRoles = ((AbstractUserStoreManager) admin).getRoleListOfUser(userName);
            Map<String, Integer> userCount = new HashMap<String, Integer>();

            // Filter the internal system roles created to maintain the backward compatibility.
            userRoles = filterInternalSystemRoles(userRoles);

            if (limit == 0) {

                // want to check whether role is internal of not
                // no limit?
                String modifiedFilter = filter;
                if (filter.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                    modifiedFilter = filter.
                            substring(filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR) + 1);
                }

                String[] hybridRoles = ((AbstractUserStoreManager) admin).getHybridRoles(modifiedFilter);

                // Filter the internal system roles created to maintain the backward compatibility.
                hybridRoles = filterInternalSystemRoles(hybridRoles);

                if (hybridRoles != null) {
                    Arrays.sort(hybridRoles);
                }

                // filter with regexp
                modifiedFilter = modifiedFilter.replace("*", ".*");

                Pattern pattern = Pattern.compile(modifiedFilter, Pattern.CASE_INSENSITIVE);

                List<FlaggedName> flaggedNames = new ArrayList<>();

                for (String role : userRoles) {
                    String matchingRole = role;
                    String roleDomain = null;
                    if (matchingRole.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                        matchingRole = matchingRole.
                                substring(matchingRole.indexOf(CarbonConstants.DOMAIN_SEPARATOR) + 1);
                        if (filter.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                            roleDomain = role.
                                    substring(0, role.indexOf(CarbonConstants.DOMAIN_SEPARATOR) + 1);
                        }
                    }
                    if (hybridRoles != null && Arrays.binarySearch(hybridRoles, role) > -1) {
                        Matcher matcher = pattern.matcher(matchingRole);
                        if (!(matcher.matches() && (roleDomain == null ||
                                filter.toLowerCase().startsWith(roleDomain.toLowerCase())))) {
                            continue;
                        }
                    } else {
                        Matcher matcher = pattern.matcher(matchingRole);
                        if (!(matcher.matches() && (roleDomain == null ||
                                filter.toLowerCase().startsWith(roleDomain.toLowerCase())))) {
                            continue;
                        }
                    }

                    FlaggedName fName = new FlaggedName();
                    mapEntityName(role, fName, admin);
                    fName.setSelected(true);
                    if (domain != null && !UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(domain) &&
                            !UserMgtConstants.APPLICATION_DOMAIN.equalsIgnoreCase(domain)) {
                        if ((admin.getSecondaryUserStoreManager(domain).isReadOnly() ||
                                (admin.getSecondaryUserStoreManager(domain).getRealmConfiguration().
                                        getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null &&
                                        admin.getSecondaryUserStoreManager(domain).getRealmConfiguration().
                                                getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED).equals(FALSE))) &&
                                hybridRoles != null && Arrays.binarySearch(hybridRoles, role) < 0) {

                            fName.setEditable(false);
                        } else {
                            fName.setEditable(true);
                        }
                    } else {
                        if ((admin.isReadOnly() || (admin.getRealmConfiguration().
                                getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED) != null &&
                                FALSE.equals(admin.getRealmConfiguration().
                                        getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED)))) &&
                                hybridRoles != null && Arrays.binarySearch(hybridRoles, role) < 0) {
                            fName.setEditable(false);
                        } else {
                            fName.setEditable(true);
                        }
                    }
                    if (domain != null) {
                        if (userCount.containsKey(domain)) {
                            userCount.put(domain, userCount.get(domain) + 1);
                        } else {
                            userCount.put(domain, 1);
                        }
                    } else {
                        if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                    userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                        } else {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                        }
                    }
                    flaggedNames.add(fName);
                }
                String exceededDomains = "";
                boolean isPrimaryExceeding = false;
                Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.getUserStoreManager()).
                        getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST);
                String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
                for (int i = 0; i < domains.length; i++) {
                    if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                        if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                                equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                            isPrimaryExceeding = true;
                        }
                        continue;
                    }
                    if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                        exceededDomains += domains[i];
                        if (i != domains.length - 1) {
                            exceededDomains += ":";
                        }
                    }
                }
                FlaggedName flaggedName = new FlaggedName();
                if (isPrimaryExceeding) {
                    flaggedName.setItemName("true");
                } else {
                    flaggedName.setItemName(FALSE);
                }
                flaggedName.setItemDisplayName(exceededDomains);
                flaggedNames.add(flaggedName);
                return flaggedNames.toArray(new FlaggedName[flaggedNames.size()]);
            }

            String[] internalRoles = null;
            String[] externalRoles = null;

            // only internal roles are retrieved.
            if (filter.toLowerCase().startsWith(UserCoreConstants.INTERNAL_DOMAIN.toLowerCase())) {
                if (admin instanceof AbstractUserStoreManager) {
                    filter = filter.substring(filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR) + 1);
                    internalRoles = ((AbstractUserStoreManager) admin).getHybridRoles(filter);
                } else {
                    internalRoles = admin.getHybridRoles();
                }
            } else {
                // filter has a domain value
                if (domain != null && filter.toLowerCase().startsWith(domain.toLowerCase() +
                        CarbonConstants.DOMAIN_SEPARATOR)) {
                    if (admin instanceof AbstractUserStoreManager) {
                        externalRoles = ((AbstractUserStoreManager) admin).getRoleNames(filter, limit,
                                true, true, true);
                    } else {
                        externalRoles = admin.getRoleNames();
                    }
                } else {

                    if (admin instanceof AbstractUserStoreManager) {
                        internalRoles = ((AbstractUserStoreManager) admin).getHybridRoles(filter);
                    } else {
                        internalRoles = admin.getHybridRoles();
                    }

                    if (domain == null) {
                        filter = CarbonConstants.DOMAIN_SEPARATOR + filter;
                    } else {
                        filter = domain + CarbonConstants.DOMAIN_SEPARATOR + filter;
                    }

                    if (admin instanceof AbstractUserStoreManager) {
                        externalRoles = ((AbstractUserStoreManager) admin).getRoleNames(filter, limit,
                                true, true, true);
                    } else {
                        externalRoles = admin.getRoleNames();
                    }
                }
            }

            // Filter the internal system roles created to maintain the backward compatibility.
            internalRoles = filterInternalSystemRoles(internalRoles);

            List<FlaggedName> flaggedNames = new ArrayList<FlaggedName>();

            Arrays.sort(userRoles);
            if (externalRoles != null) {
                for (String externalRole : externalRoles) {
                    FlaggedName fname = new FlaggedName();

                    mapEntityName(externalRole, fname, admin);
                    fname.setDomainName(domain);
                    if (Arrays.binarySearch(userRoles, externalRole) > -1) {
                        fname.setSelected(true);
                    }
                    if (domain != null) {
                        UserStoreManager secManager = admin.getSecondaryUserStoreManager(domain);
                        if (secManager.isReadOnly() ||
                                FALSE.equals(secManager.getRealmConfiguration().
                                        getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))) {
                            fname.setEditable(false);
                        } else {
                            fname.setEditable(true);
                        }
                    } else {
                        if (admin.isReadOnly() || FALSE.equals(admin.getRealmConfiguration().
                                getUserStoreProperty(UserCoreConstants.RealmConfig.WRITE_GROUPS_ENABLED))) {
                            fname.setEditable(false);
                        } else {
                            fname.setEditable(true);
                        }
                    }
                    if (domain != null) {
                        if (userCount.containsKey(domain)) {
                            userCount.put(domain, userCount.get(domain) + 1);
                        } else {
                            userCount.put(domain, 1);
                        }
                    } else {
                        if (userCount.containsKey(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME)) {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME,
                                    userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME) + 1);
                        } else {
                            userCount.put(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME, 1);
                        }
                    }
                    flaggedNames.add(fname);
                }
            }

            if (internalRoles != null) {
                for (String internalRole : internalRoles) {
                    FlaggedName fname = new FlaggedName();
                    fname.setItemName(internalRole);
                    fname.setDomainName(UserCoreConstants.INTERNAL_DOMAIN);
                    if (Arrays.binarySearch(userRoles, internalRole) > -1) {
                        fname.setSelected(true);
                    }
                    fname.setEditable(true);
                    flaggedNames.add(fname);
                }
            }

            // Sort the roles by role name.
            Collections.sort(flaggedNames, new Comparator<FlaggedName>() {
                @Override
                public int compare(FlaggedName role1, FlaggedName role2) {
                    return role1.getItemName().compareToIgnoreCase(role2.getItemName());
                }
            });

            String exceededDomains = "";
            boolean isPrimaryExceeding = false;
            Map<String, Integer> maxUserListCount = ((AbstractUserStoreManager) realm.
                    getUserStoreManager()).getMaxListCount(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST);
            String[] domains = userCount.keySet().toArray(new String[userCount.keySet().size()]);
            for (int i = 0; i < domains.length; i++) {
                if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(domains[i])) {
                    if (userCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME).
                            equals(maxUserListCount.get(UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME))) {
                        isPrimaryExceeding = true;
                    }
                    continue;
                }
                if (userCount.get(domains[i]).equals(maxUserListCount.get(domains[i].toUpperCase()))) {
                    exceededDomains += domains[i];
                    if (i != domains.length - 1) {
                        exceededDomains += ":";
                    }
                }
            }
            FlaggedName flaggedName = new FlaggedName();
            if (isPrimaryExceeding) {
                flaggedName.setItemName("true");
            } else {
                flaggedName.setItemName(FALSE);
            }
            flaggedName.setItemDisplayName(exceededDomains);
            flaggedNames.add(flaggedName);
            return flaggedNames.toArray(new FlaggedName[flaggedNames.size()]);
        } catch (Exception e) {
            log.error(e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void updateUsersOfRole(String roleName, FlaggedName[] userList)
            throws UserAdminException {

        try {

            if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName)) {
                log.error("Security Alert! Carbon anonymous role is being manipulated");
                throw new UserStoreException("Invalid data"); // obscure error
                // message
            }

            if (realm.getRealmConfiguration().getEveryOneRoleName().equalsIgnoreCase(roleName)) {
                log.error("Security Alert! Carbon Everyone role is being manipulated");
                throw new UserStoreException("Invalid data"); // obscure error
                // message
            }

            UserStoreManager admin = realm.getUserStoreManager();
            String[] oldUserList = admin.getUserListOfRole(roleName);
            List<String> list = new ArrayList<String>();
            if (oldUserList != null) {
                for (String value : oldUserList) {
                    int combinerIndex = value.indexOf(UserCoreConstants.NAME_COMBINER);
                    if (combinerIndex > 0) {
                        list.add(value.substring(0, combinerIndex));
                    } else {
                        list.add(value);
                    }
                }
                oldUserList = list.toArray(new String[list.size()]);
            }

            if (oldUserList != null) {
                Arrays.sort(oldUserList);
            }

            List<String> delUsers = new ArrayList<>();
            List<String> addUsers = new ArrayList<>();

            for (FlaggedName fName : userList) {
                boolean isSelected = fName.isSelected();
                String userName = fName.getItemName();
                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
                    log.error("Security Alert! Carbon anonymous user is being manipulated");
                    return;
                }
                int oldindex = Arrays.binarySearch(oldUserList, userName);
                if (oldindex > -1 && !isSelected) {
                    // deleted
                    delUsers.add(userName);
                } else if (oldindex < 0 && isSelected) {
                    // added
                    addUsers.add(userName);
                }
            }

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());

            boolean isRoleHasAdminPermission = realm.getAuthorizationManager().
                    isRoleAuthorized(roleName, "/permission/", UserMgtConstants.EXECUTE_ACTION);
            if (!isRoleHasAdminPermission) {
                isRoleHasAdminPermission = realm.getAuthorizationManager().
                        isRoleAuthorized(roleName, "/permission/admin/", UserMgtConstants.EXECUTE_ACTION);
            }

            if ((realmConfig.getAdminRoleName().equalsIgnoreCase(roleName) || isRoleHasAdminPermission) &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to add or remove users from Admin role by user : "
                        + loggedInUserName);
                throw new UserStoreException("Can not add or remove user from Admin permission role");
            }

            String[] delUsersArray = null;
            String[] addUsersArray = null;

            String[] users = realm.getUserStoreManager().getUserListOfRole(roleName);


            if (delUsers != null && users != null) {
                Arrays.sort(users);

                delUsersArray = delUsers.toArray(new String[delUsers.size()]);
                Arrays.sort(delUsersArray);
                if (Arrays.binarySearch(delUsersArray, loggedInUserName) > -1
                        && Arrays.binarySearch(users, loggedInUserName) > -1
                        && !adminUser.equalsIgnoreCase(loggedInUserName)) {
                    log.warn("An attempt to remove from role : " + roleName + " by user :" + loggedInUserName);
                    throw new UserStoreException("Can not remove yourself from role : " + roleName);
                }
            }

            if (addUsers != null) {
                addUsersArray = addUsers.toArray(new String[addUsers.size()]);
            }
            admin.updateUserListOfRole(roleName, delUsersArray, addUsersArray);
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void updateRolesOfUser(String userName, String[] roleList) throws UserAdminException {
        try {

            if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
                log.error("Security Alert! Carbon anonymous user is being manipulated");
                throw new UserAdminException("Invalid data"); // obscure error
                // message
            }
            if (roleList != null) {
                String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
                RealmConfiguration realmConfig = realm.getRealmConfiguration();
                String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
                Arrays.sort(roleList);
                String[] roles = realm.getUserStoreManager().getRoleListOfUser(userName);

                UserStoreManager admin = realm.getUserStoreManager();
                String[] oldRoleList = admin.getRoleListOfUser(userName);
                Arrays.sort(oldRoleList);
                List<String> addRoles = new ArrayList<String>();
                List<String> delRoles = new ArrayList<String>();


                boolean isUserHasAdminPermission = false;
                String adminPermissionRole = null;
                if (roles != null) {
                    Arrays.sort(roles);
                    for (String role : roles) {
                        isUserHasAdminPermission = realm.getAuthorizationManager().
                                isRoleAuthorized(role, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                        if (!isUserHasAdminPermission) {
                            isUserHasAdminPermission = realm.getAuthorizationManager().
                                    isRoleAuthorized(role, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                        }

                        if (isUserHasAdminPermission) {
                            break;
                        }
                    }
                }
                boolean isRoleHasAdminPermission;
                for (String roleName : roleList) {
                    isRoleHasAdminPermission = realm.getAuthorizationManager().
                            isRoleAuthorized(roleName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                    if (!isRoleHasAdminPermission) {
                        isRoleHasAdminPermission = realm.getAuthorizationManager().
                                isRoleAuthorized(roleName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                    }

                    if (isRoleHasAdminPermission) {
                        adminPermissionRole = roleName;
                        break;
                    }
                }

                if (roles == null || Arrays.binarySearch(roles, realmConfig.getAdminRoleName()) < 0) {
                    if ((Arrays.binarySearch(roleList, realmConfig.getAdminRoleName()) > -1 ||
                            (!isUserHasAdminPermission && adminPermissionRole != null)) &&
                            !adminUser.equalsIgnoreCase(loggedInUserName)) {
                        log.warn("An attempt to add users to Admin permission role by user : " +
                                loggedInUserName);
                        throw new UserStoreException("Can not add users to Admin permission role");
                    }
                } else {
                    if (Arrays.binarySearch(roleList, realmConfig.getAdminRoleName()) < 0 &&
                            !adminUser.equalsIgnoreCase(loggedInUserName)) {
                        log.warn("An attempt to remove users from Admin role by user : " +
                                loggedInUserName);
                        throw new UserStoreException("Can not remove users from Admin role");
                    }
                }

                for (String name : roleList) {
                    int oldindex = Arrays.binarySearch(oldRoleList, name);
                    if (oldindex < 0) {
                        addRoles.add(name);
                    }
                }

                for (String name : oldRoleList) {
                    int newindex = Arrays.binarySearch(roleList, name);
                    if (newindex < 0) {
                        if (realm.getRealmConfiguration().getEveryOneRoleName().equalsIgnoreCase(name)) {
                            log.warn("Carbon Internal/everyone role can't be manipulated");
                            continue;
                        }
                        delRoles.add(name);
                    }
                }
                admin.updateRoleListOfUser(userName, delRoles.toArray(new String[delRoles.size()]),
                        addRoles.toArray(new String[addRoles.size()]));
            }
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void updateUsersOfRole(String roleName, String[] newUsers, String[] deleteUsers)
            throws UserAdminException {

        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());

            if (CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME.equalsIgnoreCase(roleName)) {
                log.error("Security Alert! Carbon anonymous role is being manipulated by user " + loggedInUserName);
                throw new UserStoreException("Invalid data");
            }

            if (realm.getRealmConfiguration().getEveryOneRoleName().equalsIgnoreCase(roleName)) {
                log.error("Security Alert! Carbon Everyone role is being manipulated by user " + loggedInUserName);
                throw new UserStoreException("Invalid data");
            }

            boolean isRoleHasAdminPermission = realm.getAuthorizationManager().
                    isRoleAuthorized(roleName, "/permission/", UserMgtConstants.EXECUTE_ACTION);
            if (!isRoleHasAdminPermission) {
                isRoleHasAdminPermission = realm.getAuthorizationManager().
                        isRoleAuthorized(roleName, "/permission/admin/", UserMgtConstants.EXECUTE_ACTION);
            }

            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
            if ((realmConfig.getAdminRoleName().equalsIgnoreCase(roleName) ||
                    isRoleHasAdminPermission) &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                log.warn("An attempt to add or remove users from a admin role by user : " + loggedInUserName);
                throw new UserStoreException("You do not have the required privilege to add or remove user from a " +
                        "admin role");
            }

            if (deleteUsers != null) {
                Arrays.sort(deleteUsers);
                if (realmConfig.getAdminRoleName().equalsIgnoreCase(roleName) &&
                        Arrays.binarySearch(deleteUsers, realmConfig.getAdminUserName()) > -1) {
                    log.warn("An attempt to remove Admin user from Admin role by user : "
                            + loggedInUserName);
                    throw new UserStoreException("Can not remove Admin user " +
                            "from Admin role");
                }
            }

            UserStoreManager admin = realm.getUserStoreManager();
            String[] oldUserList = admin.getUserListOfRole(roleName);
            List<String> list = new ArrayList<String>();
            if (oldUserList != null) {
                for (String value : oldUserList) {
                    int combinerIndex = value.indexOf(UserCoreConstants.NAME_COMBINER);
                    if (combinerIndex > 0) {
                        list.add(value.substring(0, combinerIndex));
                    } else {
                        list.add(value);
                    }
                }
                oldUserList = list.toArray(new String[list.size()]);
                Arrays.sort(oldUserList);
            }


            List<String> delUser = new ArrayList<String>();
            List<String> addUsers = new ArrayList<String>();

            if (oldUserList != null) {
                if (newUsers != null) {
                    for (String name : newUsers) {
                        if (Arrays.binarySearch(oldUserList, name) < 0) {
                            addUsers.add(name);
                        }
                    }
                    newUsers = addUsers.toArray(new String[addUsers.size()]);
                }

                if (deleteUsers != null) {
                    for (String name : deleteUsers) {
                        if (Arrays.binarySearch(oldUserList, name) > -1) {
                            delUser.add(name);
                        }
                    }
                    deleteUsers = delUser.toArray(new String[delUser.size()]);
                }
            } else {
                deleteUsers = null;
            }


            admin.updateUserListOfRole(roleName, deleteUsers, newUsers);

        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void updateRolesOfUser(String userName, String[] newRoles, String[] deletedRoles) throws UserAdminException {
        try {

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());

            if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equalsIgnoreCase(userName)) {
                log.error("Security Alert! Carbon anonymous user is being manipulated by user "
                        + loggedInUserName);
                throw new UserAdminException("Invalid data");
            }

            if (deletedRoles != null) {
                for (String name : deletedRoles) {
                    if (realm.getRealmConfiguration().getEveryOneRoleName().equalsIgnoreCase(name)) {
                        log.error("Security Alert! Carbon everyone role is being manipulated by user "
                                + loggedInUserName);
                        throw new UserAdminException("Invalid data");
                    }
                    if (realm.getRealmConfiguration().getAdminRoleName().equalsIgnoreCase(name) &&
                            realm.getRealmConfiguration().getAdminUserName().equalsIgnoreCase(userName)) {
                        log.error("Can not remove admin user from admin role "
                                + loggedInUserName);
                        throw new UserAdminException("Can not remove admin user from admin role");
                    }
                }
            }

            RealmConfiguration realmConfig = realm.getRealmConfiguration();
            String adminUser = addPrimaryDomainIfNotExists(realmConfig.getAdminUserName());
            if (!adminUser.equalsIgnoreCase(loggedInUserName)) {

                boolean isUserHadAdminPermission;

                // check whether this user had admin permission
                isUserHadAdminPermission = realm.getAuthorizationManager().
                        isUserAuthorized(userName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                if (!isUserHadAdminPermission) {
                    isUserHadAdminPermission = realm.getAuthorizationManager().
                            isUserAuthorized(userName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                }

                if (newRoles != null) {
                    boolean isRoleHasAdminPermission = false;
                    // check whether new roles has admin permission
                    for (String roleName : newRoles) {

                        if (roleName.equalsIgnoreCase(realmConfig.getAdminRoleName())) {
                            log.warn("An attempt to add users to Admin permission role by user : " +
                                    loggedInUserName);
                            throw new UserStoreException("Can not add users to Admin permission role");
                        }

                        isRoleHasAdminPermission = realm.getAuthorizationManager().
                                isRoleAuthorized(roleName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                        if (!isRoleHasAdminPermission) {
                            isRoleHasAdminPermission = realm.getAuthorizationManager().
                                    isRoleAuthorized(roleName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                        }

                        if (isRoleHasAdminPermission) {
                            break;
                        }
                    }

                    if (!isUserHadAdminPermission && isRoleHasAdminPermission) {
                        log.warn("An attempt to add users to Admin permission role by user : " +
                                loggedInUserName);
                        throw new UserStoreException("Can not add users to Admin permission role");
                    }
                }

                if (deletedRoles != null) {

                    boolean isRemoveRoleHasAdminPermission = false;
                    // check whether delete roles has admin permission
                    for (String roleName : deletedRoles) {
                        isRemoveRoleHasAdminPermission = realm.getAuthorizationManager().
                                isRoleAuthorized(roleName, PERMISSION, UserMgtConstants.EXECUTE_ACTION);
                        if (!isRemoveRoleHasAdminPermission) {
                            isRemoveRoleHasAdminPermission = realm.getAuthorizationManager().
                                    isRoleAuthorized(roleName, PERMISSION_ADMIN, UserMgtConstants.EXECUTE_ACTION);
                        }

                        if (isRemoveRoleHasAdminPermission) {
                            break;
                        }
                    }

                    if (isUserHadAdminPermission && isRemoveRoleHasAdminPermission) {
                        log.warn("An attempt to remove users from Admin role by user : " +
                                loggedInUserName);
                        throw new UserStoreException("Can not remove users from Admin role");
                    }
                }
            }

            realm.getUserStoreManager().updateRoleListOfUser(userName, deletedRoles, newRoles);

        } catch (UserStoreException e) {
            // previously logged so loggin
            // g not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public UIPermissionNode getAllUIPermissions(int tenantId)
            throws UserAdminException {

        UIPermissionNode nodeRoot;
        Collection regRoot;
        Collection parent = null;
        Registry tenentRegistry = null;

        try {
            Registry registry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                if (CarbonContext.getThreadLocalCarbonContext().getTenantId() != MultitenantConstants.SUPER_TENANT_ID) {
                    log.error("Illegal access attempt");
                    throw new UserStoreException("Illegal access attempt");
                }
                regRoot = (Collection) registry.get(UserMgtConstants.UI_PERMISSION_ROOT);
                String displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_PERMISSION_ROOT, displayName);
            } else {

                regRoot = (Collection) registry.get(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT);

                tenentRegistry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry(tenantId);
                Collection appRoot;

                if (tenentRegistry.resourceExists(APPLICATIONS_PATH)) {
                    appRoot = (Collection) tenentRegistry.get(APPLICATIONS_PATH);
                    parent = (Collection) tenentRegistry.newCollection();
                    parent.setProperty(UserMgtConstants.DISPLAY_NAME, "All Permissions");
                    parent.setChildren(new String[]{regRoot.getPath(), appRoot.getPath()});
                }

                String displayName = null;

                if (parent != null) {
                    displayName = parent.getProperty(UserMgtConstants.DISPLAY_NAME);
                } else {
                    displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                }

                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT,
                        displayName);
            }

            if (parent != null) {
                buildUIPermissionNode(parent, nodeRoot, registry, tenentRegistry, null, null, null);
            } else {
                buildUIPermissionNode(regRoot, nodeRoot, registry, tenentRegistry, null, null, null);
            }

            return nodeRoot;
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public UIPermissionNode getRolePermissions(String roleName, int tenantId)
            throws UserAdminException {
        UIPermissionNode nodeRoot;
        Collection regRoot;
        Collection parent = null;
        Registry tenentRegistry = null;

        try {
            Registry registry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                regRoot = (Collection) registry.get(UserMgtConstants.UI_PERMISSION_ROOT);
                String displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_PERMISSION_ROOT, displayName);
            } else {

                regRoot = (Collection) registry.get(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT);

                tenentRegistry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry(tenantId);
                Collection appRoot;

                if (tenentRegistry.resourceExists(APPLICATIONS_PATH)) {
                    appRoot = (Collection) tenentRegistry.get(APPLICATIONS_PATH);
                    parent = (Collection) tenentRegistry.newCollection();
                    parent.setProperty(UserMgtConstants.DISPLAY_NAME, "All Permissions");
                    parent.setChildren(new String[]{regRoot.getPath(), appRoot.getPath()});
                }

                String displayName = null;

                if (parent != null) {
                    displayName = parent.getProperty(UserMgtConstants.DISPLAY_NAME);
                } else {
                    displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                }

                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT,
                        displayName);
            }

            if (parent != null) {
                buildUIPermissionNode(parent, nodeRoot, registry, tenentRegistry,
                        realm.getAuthorizationManager(), roleName, null);
            } else {
                buildUIPermissionNode(regRoot, nodeRoot, registry, tenentRegistry,
                        realm.getAuthorizationManager(), roleName, null);
            }

            return nodeRoot;
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    public void setRoleUIPermission(String roleName, String[] rawResources)
            throws UserAdminException {

        Permission[] permissions = null;
        UserStoreManager userStoreManager = null;
        try {
            if (((AbstractUserStoreManager) realm.getUserStoreManager()).isOthersSharedRole(roleName)) {
                throw new UserAdminException("Logged in user is not authorized to assign " +
                        "permissions to a role belong to another tenant");
            }
            if (realm.getRealmConfiguration().getAdminRoleName().equalsIgnoreCase(roleName)) {
                String msg = "UI permissions of Admin is not allowed to change";
                log.error(msg);
                throw new UserAdminException(msg);
            }

            String loggedInUserName = addPrimaryDomainIfNotExists(getLoggedInUser());
            String adminUser = addPrimaryDomainIfNotExists(realm.getRealmConfiguration().getAdminUserName());
            if (rawResources != null &&
                    !adminUser.equalsIgnoreCase(loggedInUserName)) {
                Arrays.sort(rawResources);
                if (isPermissionsListHasAdminPermissions(rawResources)) {
                    log.warn("An attempt to Assign admin permission for role by user : " +
                            loggedInUserName);
                    throw new UserStoreException("Can not assign Admin for permission role");
                }
            }

            String[] optimizedList = UserCoreUtil.optimizePermissions(rawResources);
            AuthorizationManager authMan = realm.getAuthorizationManager();
            authMan.clearRoleActionOnAllResources(roleName, UserMgtConstants.EXECUTE_ACTION);

            permissions = new Permission[optimizedList.length];
            for (int i = 0; i < optimizedList.length; i++) {
                authMan.authorizeRole(roleName, optimizedList[i], UserMgtConstants.EXECUTE_ACTION);
                permissions[i] = new Permission(optimizedList[i], UserMgtConstants.EXECUTE_ACTION);
            }

            userStoreManager = realm.getUserStoreManager();
            ManagementPermissionUtil.handlePostUpdatePermissionsOfRole(roleName, permissions, userStoreManager);
        } catch (UserStoreException e) {
            ManagementPermissionUtil
                    .handleOnUpdatePermissionsOfRoleFailure(e.getMessage(), roleName, permissions, userStoreManager);
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    /**
     * Check whether the admin permissions are available in the rawResources.
     *
     * @param rawResources Resource permissions list.
     * @return True if the permissions list contains any admin root permissions.
     */
    private boolean isPermissionsListHasAdminPermissions(String[] rawResources) {

        return (Arrays.binarySearch(rawResources, PERMISSION_ADMIN) > -1 ||
                Arrays.binarySearch(rawResources, PERMISSION_ADMIN_TREE) > -1 ||
                Arrays.binarySearch(rawResources, PERMISSION_PROTECTED) > -1 ||
                Arrays.binarySearch(rawResources, PERMISSION_PROTECTED_TREE) > -1 ||
                Arrays.binarySearch(rawResources, PERMISSION) > -1 ||
                Arrays.binarySearch(rawResources, PERMISSION_TREE) > -1);
    }

    public void bulkImportUsers(String userStoreDomain, String fileName, InputStream inStream, String defaultPassword)
            throws UserAdminException {
        try {
            BulkImportConfig config = new BulkImportConfig(inStream, fileName);
            if (defaultPassword != null && defaultPassword.trim().length() > 0) {
                config.setDefaultPassword(defaultPassword.trim());
            }
            if (StringUtils.isNotEmpty(userStoreDomain)) {
                config.setUserStoreDomain(userStoreDomain);
            }

            UserStoreManager userStore = this.realm.getUserStoreManager();
            userStore = userStore.getSecondaryUserStoreManager(userStoreDomain);

            if (fileName.endsWith("csv")) {
                UserBulkImport csvAdder = new CSVUserBulkImport(config);
                csvAdder.addUserList(userStore);
            } else if (fileName.endsWith("xls") || fileName.endsWith("xlsx")) {
                UserBulkImport excelAdder = new ExcelUserBulkImport(config);
                excelAdder.addUserList(userStore);
            } else {
                throw new UserAdminException("Unsupported format");
            }
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        }

    }


    public void changePasswordByUser(String userName, String oldPassword, String newPassword)
            throws UserAdminException {

        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(userName);
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userName);
            UserRealmService realmService = UserMgtDSComponent.getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            org.wso2.carbon.user.api.UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
            org.wso2.carbon.user.api.UserStoreManager userStore = userRealm.getUserStoreManager();
            userStore.updateCredential(tenantAwareUsername, newPassword, oldPassword);
        } catch (UserStoreException e) {
            // previously logged so logging not needed
            throw new UserAdminException(e.getMessage(), e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            log.error("Error while getting tenant user realm", e);
            throw new UserAdminException("Error while getting tenant user realm" , e);
        }
    }


    public boolean hasMultipleUserStores() throws UserAdminException {
        try {
            return realm.getUserStoreManager().getSecondaryUserStoreManager() != null;
        } catch (UserStoreException e) {
            log.error(e);
            throw new UserAdminException("Unable to check for multiple user stores");
        }
    }

    private void buildUIPermissionNode(Collection parent, UIPermissionNode parentNode,
                                       Registry registry, Registry tenantRegistry, AuthorizationManager authMan,
                                       String roleName, String userName)
            throws RegistryException, UserStoreException {

        boolean isSelected = false;
        if (roleName != null) {
            isSelected = authMan.isRoleAuthorized(roleName, parentNode.getResourcePath(),
                    UserMgtConstants.EXECUTE_ACTION);
        } else if (userName != null) {
            isSelected = authMan.isUserAuthorized(userName, parentNode.getResourcePath(),
                    UserMgtConstants.EXECUTE_ACTION);
        }
        if (isSelected) {
            buildUIPermissionNodeAllSelected(parent, parentNode, registry, tenantRegistry);
            parentNode.setSelected(true);
        } else {
            buildUIPermissionNodeNotAllSelected(parent, parentNode, registry, tenantRegistry,
                    authMan, roleName, userName);
        }
    }

    private void buildUIPermissionNodeAllSelected(Collection parent, UIPermissionNode parentNode,
                                                  Registry registry, Registry tenantRegistry) throws RegistryException,
            UserStoreException {

        String[] children = parent.getChildren();
        UIPermissionNode[] childNodes = new UIPermissionNode[children.length];
        for (int i = 0; i < children.length; i++) {
            String child = children[i];
            Resource resource = null;

            if (registry.resourceExists(child)) {
                resource = registry.get(child);
            } else if (tenantRegistry != null) {
                resource = tenantRegistry.get(child);
            } else {
                throw new RegistryException("Permission resource not found in the registry.");
            }

            childNodes[i] = getUIPermissionNode(resource, true);
            if (resource instanceof Collection) {
                buildUIPermissionNodeAllSelected((Collection) resource, childNodes[i], registry,
                        tenantRegistry);
            }
        }
        parentNode.setNodeList(childNodes);
    }

    private void buildUIPermissionNodeNotAllSelected(Collection parent, UIPermissionNode parentNode,
                                                     Registry registry, Registry tenantRegistry,
                                                     AuthorizationManager authMan, String roleName, String userName)
            throws RegistryException, UserStoreException {

        String[] children = parent.getChildren();
        UIPermissionNode[] childNodes = new UIPermissionNode[children.length];

        for (int i = 0; i < children.length; i++) {
            String child = children[i];
            Resource resource = null;

            if (tenantRegistry != null && child.startsWith("/permission/applications")) {
                resource = tenantRegistry.get(child);
            } else if (registry.resourceExists(child)) {
                resource = registry.get(child);
            } else {
                throw new RegistryException("Permission resource not found in the registry.");
            }

            boolean isSelected = false;
            if (roleName != null) {
                isSelected = authMan.isRoleAuthorized(roleName, child,
                        UserMgtConstants.EXECUTE_ACTION);
            } else if (userName != null) {
                isSelected = authMan.isUserAuthorized(userName, child,
                        UserMgtConstants.EXECUTE_ACTION);
            }
            childNodes[i] = getUIPermissionNode(resource, isSelected);
            if (resource instanceof Collection) {
                buildUIPermissionNodeNotAllSelected((Collection) resource, childNodes[i],
                        registry, tenantRegistry, authMan, roleName, userName);
            }
        }
        parentNode.setNodeList(childNodes);
    }

    private UIPermissionNode getUIPermissionNode(Resource resource,
                                                 boolean isSelected) throws RegistryException {
        String displayName = resource.getProperty(UserMgtConstants.DISPLAY_NAME);
        return new UIPermissionNode(resource.getPath(), displayName, isSelected);
    }

    /**
     * Gets logged in user of the server
     *
     * @return user name
     */
    private String getLoggedInUser() {

        return CarbonContext.getThreadLocalCarbonContext().getUsername();
    }

    private void mapEntityName(String entityName, FlaggedName fName,
                               UserStoreManager userStoreManager) {
        if (entityName.contains(UserCoreConstants.SHARED_ROLE_TENANT_SEPERATOR)) {
            String[] nameAndDn = entityName.split(UserCoreConstants.SHARED_ROLE_TENANT_SEPERATOR);
            fName.setItemName(nameAndDn[0]);
            fName.setDn(nameAndDn[1]);

            // TODO remove abstract user store
            fName.setShared(((AbstractUserStoreManager) userStoreManager).isOthersSharedRole(entityName));
            if (fName.isShared()) {
                fName.setItemDisplayName(UserCoreConstants.SHARED_ROLE_TENANT_SEPERATOR +
                        fName.getItemName());
            }

        } else {
            fName.setItemName(entityName);
        }

    }

    public boolean isSharedRolesEnabled() throws UserAdminException {
        UserStoreManager userManager;
        try {
            userManager = realm.getUserStoreManager();   // TODO remove abstract user store
            return ((AbstractUserStoreManager) userManager).isSharedGroupEnabled();
        } catch (UserStoreException e) {
            log.error(e);
            throw new UserAdminException("Unable to check shared role enabled", e);
        }
    }


    public String[] concatArrays(String[] o1, String[] o2) {
        String[] ret = new String[o1.length + o2.length];

        System.arraycopy(o1, 0, ret, 0, o1.length);
        System.arraycopy(o2, 0, ret, o1.length, o2.length);

        return ret;
    }

    private class ClaimMappingsComparator implements Comparator<ClaimMapping> {

        @Override
        public int compare(ClaimMapping o1, ClaimMapping o2) {
            return o1.getClaim().getClaimUri().compareTo(o2.getClaim().getClaimUri());
        }
    }

    /**
     *  Checks whether the user store containing the given role name supports filter and limit.
     */
    private boolean canLimitAndFilterUsersFromUMLevel(String roleName, UserStoreManager userStoreManager) {

        // Currently filter and limit for users in the role is supported only with the JDBC user store manager.
        boolean canLimitAndFilterWithUM = false;

        int domainSeparatorIndex = roleName.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (domainSeparatorIndex > 0) {
            String domainInRole = roleName.substring(0, domainSeparatorIndex);
            UserStoreManager secondaryUserStoreManager = userStoreManager.getSecondaryUserStoreManager(domainInRole);
            if (secondaryUserStoreManager != null) {
                canLimitAndFilterWithUM = secondaryUserStoreManager instanceof JDBCUserStoreManager;
            }
        } else {
            canLimitAndFilterWithUM = userStoreManager instanceof JDBCUserStoreManager;
        }
        return canLimitAndFilterWithUM;
    }

    private String getDomainFreeFilter(String filter) {

        String domainFreeFilter = filter;
        int domainSeparatorIndex = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR);
        if (!CarbonConstants.DOMAIN_SEPARATOR.equalsIgnoreCase(filter) && domainSeparatorIndex >= 0) {
            domainFreeFilter = filter.substring(domainSeparatorIndex + 1);
        }
        return domainFreeFilter;
    }

    private int getUserCountLimit(int limit) {

        // User store level filtering with "getUserListOfRole" interpret getting all users as limit < 0.
        // However, the current method "getUsersOfRole" interpret it as 0. Therefore, following conversion is
        // done to preserve backward compatibility.
        int userCountLimit = limit;
        if (limit == 0) {
            userCountLimit = -1;
        }
        return userCountLimit;
    }

    /**
     * Get permission of roles list.
     *
     * @param roleNames List of roles.
     * @param tenantId  Tenanat ID.
     * @return Permissions.
     * @throws UserAdminException UserAdminException.
     */
    public UIPermissionNode getRolePermissions(List<String> roleNames, int tenantId) throws UserAdminException {

        UIPermissionNode nodeRoot;
        Collection regRoot;
        Collection parent = null;
        Registry tenantRegistry = null;

        try {
            Registry registry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                regRoot = (Collection) registry.get(UserMgtConstants.UI_PERMISSION_ROOT);
                String displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_PERMISSION_ROOT, displayName);
            } else {
                regRoot = (Collection) registry.get(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT);
                tenantRegistry = UserMgtDSComponent.getRegistryService().getGovernanceSystemRegistry(tenantId);
                Collection appRoot;

                if (tenantRegistry.resourceExists(APPLICATIONS_PATH)) {
                    appRoot = (Collection) tenantRegistry.get(APPLICATIONS_PATH);
                    parent = (Collection) tenantRegistry.newCollection();
                    parent.setProperty(UserMgtConstants.DISPLAY_NAME, "All Permissions");
                    parent.setChildren(new String[] { regRoot.getPath(), appRoot.getPath() });
                }

                String displayName;
                if (parent != null) {
                    displayName = parent.getProperty(UserMgtConstants.DISPLAY_NAME);
                } else {
                    displayName = regRoot.getProperty(UserMgtConstants.DISPLAY_NAME);
                }
                nodeRoot = new UIPermissionNode(UserMgtConstants.UI_ADMIN_PERMISSION_ROOT, displayName);
            }

            if (parent != null) {
                buildUIPermissionNode(parent, nodeRoot, registry, tenantRegistry, realm.getAuthorizationManager(),
                        roleNames);
            } else {
                buildUIPermissionNode(regRoot, nodeRoot, registry, tenantRegistry, realm.getAuthorizationManager(),
                        roleNames);
            }
            return nodeRoot;
        } catch (UserStoreException | RegistryException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    private void buildUIPermissionNode(Collection parent, UIPermissionNode parentNode,
            Registry registry, Registry tenantRegistry, AuthorizationManager authMan,
            List<String> roleNames)
            throws RegistryException, UserStoreException {

        boolean isSelected = false;
        for (String roleName : roleNames) {
            if (StringUtils.isNotBlank(roleName)) {
                if (authMan.isRoleAuthorized(roleName, parentNode.getResourcePath(),
                        UserMgtConstants.EXECUTE_ACTION)) {
                    isSelected = true;
                    break;
                }
            }
        }

        if (isSelected) {
            buildUIPermissionNodeAllSelected(parent, parentNode, registry, tenantRegistry);
            parentNode.setSelected(true);
        } else {
            buildUIPermissionNodeNotAllSelected(parent, parentNode, registry, tenantRegistry,
                    authMan, roleNames);
        }
    }

    private void buildUIPermissionNodeNotAllSelected(Collection parent, UIPermissionNode parentNode,
            Registry registry, Registry tenantRegistry,
            AuthorizationManager authMan, List<String> roleNames)
            throws RegistryException, UserStoreException {

        String[] children = parent.getChildren();
        UIPermissionNode[] childNodes = new UIPermissionNode[children.length];

        for (int i = 0; i < children.length; i++) {
            String child = children[i];
            Resource resource;

            if (tenantRegistry != null && child.startsWith("/permission/applications")) {
                resource = tenantRegistry.get(child);
            } else if (registry.resourceExists(child)) {
                resource = registry.get(child);
            } else {
                throw new RegistryException("Permission resource not found in the registry.");
            }

            boolean isSelected = false;
            for (String roleName : roleNames) {
                if (StringUtils.isNotBlank(roleName)) {
                    if (authMan.isRoleAuthorized(roleName, child, UserMgtConstants.EXECUTE_ACTION)) {
                        isSelected = true;
                        break;
                    }
                }
            }
            childNodes[i] = getUIPermissionNode(resource, isSelected);
            if (resource instanceof Collection) {
                buildUIPermissionNodeNotAllSelected((Collection) resource, childNodes[i],
                        registry, tenantRegistry, authMan, roleNames);
            }
        }
        parentNode.setNodeList(childNodes);
    }

    /**
     * Get hybrid role list of a group.
     *
     * @param groupName Group name.
     * @return List of hybrid roles of the group.
     * @throws UserAdminException UserAdminException.
     */
    public List<String> getHybridRoleListOfGroup(String groupName, String domainName) throws UserAdminException {

        try {
            return ((AbstractUserStoreManager) realm.getUserStoreManager())
                    .getHybridRoleListOfGroup(groupName, domainName);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    /**
     * Check whether the given hybrid role is exist in the system.
     *
     * @param roleName Role name.
     * @return {@code true} if the given role is exist in the system.
     * @throws UserAdminException UserAdminException.
     */
    public boolean isExistingHybridRole(String roleName) throws UserAdminException {

        try {
            return ((AbstractUserStoreManager) realm.getUserStoreManager()).isExistingHybridRole(roleName);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    /**
     * Update group list of role.
     *
     * @param roleName      Role name.
     * @param deletedGroups Deleted groups.
     * @param newGroups     New groups.
     * @throws UserAdminException UserAdminException.
     */
    public void updateGroupListOfHybridRole(String roleName, String[] deletedGroups, String[] newGroups)
            throws UserAdminException {

        try {
            ((AbstractUserStoreManager) realm.getUserStoreManager())
                    .updateGroupListOfHybridRole(roleName, deletedGroups, newGroups);
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }

    /**
     * Checks whether groups and roles separation feature enabled.
     *
     * @return {@code true} if the groups and roles separation feature enabled.
     */
    public boolean isRoleAndGroupSeparationEnabled() throws UserAdminException {

        try {
            return ((AbstractUserStoreManager) realm.getUserStoreManager()).isRoleAndGroupSeparationEnabled();
        } catch (UserStoreException e) {
            log.error(e.getMessage(), e);
            throw new UserAdminException(e.getMessage(), e);
        }
    }
}
