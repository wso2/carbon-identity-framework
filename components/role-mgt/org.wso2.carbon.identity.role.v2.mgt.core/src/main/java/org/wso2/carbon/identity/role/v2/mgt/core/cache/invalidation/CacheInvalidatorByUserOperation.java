package org.wso2.carbon.identity.role.v2.mgt.core.cache.invalidation;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleCacheById;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RoleIdCacheKey;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.RolesCacheByUserId;
import org.wso2.carbon.identity.role.v2.mgt.core.cache.UserIdCacheKey;
import org.wso2.carbon.user.api.Permission;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.Claim;
import org.wso2.carbon.user.core.common.Group;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.listener.UniqueIDUserOperationEventListener;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.UniqueIDUserClaimSearchEntry;

import java.util.List;
import java.util.Map;

public class CacheInvalidatorByUserOperation implements UniqueIDUserOperationEventListener {

    @Override
    public boolean doPreAddInternalRoleWithID(String s, String[] strings, Permission[] permissions,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddInternalRoleWithID(String s, String[] strings, Permission[] permissions,
                                               UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserClaimValueWithID(String s, String s1, String s2, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserClaimValuesWithID(String s, String[] strings, String s1, Map<String, String> map,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserClaimValueWithID(String s, String s1, List<String> list, String s2,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserClaimValuesWithID(String s, String[] strings, String s1, Map<String, String> map,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserListWithID(String s, String s1, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserListWithID(Condition condition, String s, String s1, int i, int i1, String s2, String s3,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserListWithID(String s, String s1, int i, int i1, List<User> list,
                                          UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserListWithID(String s, String s1, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserListWithID(String s, String s1, List<User> list, int i, int i1,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserListWithID(Condition condition, String s, String s1, int i, int i1, String s2,
                                           String s3, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserWithID(String s, String[] strings, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserWithID(String s, String[] strings, String s1, User user,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetPaginatedUserListWithID(String s, String s1, List<User> list,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostListUsersWithID(String s, int i, int i1, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetRoleListOfUserWithID(String s, String s1, String[] strings,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserListOfRoleWithID(String s, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUsersClaimValuesWithID(List<String> list, List<String> list1, String s,
                                                   List<UniqueIDUserClaimSearchEntry> list2,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAuthenticateWithID(String s, String s1, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAuthenticateWithID(String s, String s1, AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAuthenticateWithID(List<LoginIdentifier> list, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAuthenticateWithID(List<LoginIdentifier> list, AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAuthenticateWithID(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAuthenticateWithID(String s, AuthenticationResult authenticationResult,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAddUserWithID(String s, Object o, String[] strings, Map<String, String> map, String s1,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddUserWithID(User user, Object o, String[] strings, Map<String, String> map, String s,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        RoleCacheById.getInstance().clear(userStoreManager.getTenantId());
        RolesCacheByUserId.getInstance().clear(userStoreManager.getTenantId());
        return false;
    }

    @Override
    public boolean doPreUpdateUserName(String s, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateUserName(User user, UserStoreManager userStoreManager) throws UserStoreException {

        RoleCacheById.getInstance().clear(userStoreManager.getTenantId());
        return false;
    }

    @Override
    public boolean doPreUpdateCredentialWithID(String s, Object o, Object o1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateCredentialWithID(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateCredentialByAdminWithID(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateCredentialByAdminWithID(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUserWithID(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUserWithID(String s, UserStoreManager userStoreManager) throws UserStoreException {

        RoleCacheById.getInstance().clear(userStoreManager.getTenantId());
        UserIdCacheKey userIdCacheKey = new UserIdCacheKey(s);
        RolesCacheByUserId.getInstance().clearCacheEntry(userIdCacheKey, userStoreManager.getTenantId());
        return false;
    }

    @Override
    public boolean doPreSetUserClaimValueWithID(String s, String s1, String s2, String s3,
                                                UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostSetUserClaimValueWithID(String s, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreSetUserClaimValuesWithID(String s, Map<String, String> map, String s1,
                                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostSetUserClaimValuesWithID(String s, Map<String, String> map, String s1,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValuesWithID(String s, String[] strings, String s1,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValuesWithID(String s, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValueWithID(String s, String s1, String s2, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValueWithID(String s, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAddRoleWithID(String s, String[] strings, Permission[] permissions,
                                      UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddRoleWithID(String s, String[] strings, Permission[] permissions,
                                       UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateUserListOfRoleWithID(String s, String[] strings, String[] strings1,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateUserListOfRoleWithID(String s, String[] strings, String[] strings1,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateRoleListOfUserWithID(String s, String[] strings, String[] strings1,
                                                   UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateRoleListOfUserWithID(String s, String[] strings, String[] strings1,
                                                    UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetRoleListOfUsersWithID(List<String> list, Map<String, List<String>> map,
                                                  UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAddGroup(String s, List<String> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddGroup(String s, List<String> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetGroup(String s, List<String> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetGroup(String s, List<String> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateGroup(String s, List<Claim> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateGroup(String s, List<Claim> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreRenameGroup(String s, String s1, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostRenameGroup(String s, String s1, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreListGroup(Condition condition, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostListGroups(Condition condition, String s, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetGroupListOfUsers(List<String> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetGroupListOfUsers(List<String> list, Map<String, List<Group>> map,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateGroupListOfUser(String s, List<String> list, List<String> list1,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateGroupListOfUser(String s, List<String> list, List<String> list1,
                                               UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteGroup(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteGroup(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreGetUserListOfGroup(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostGetUserListOfGroup(String s, List<User> list, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public int getExecutionOrderId() {

        return 0;
    }

    @Override
    public boolean doPreAuthenticate(String s, Object o, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAuthenticate(String s, boolean b, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAddUser(String s, Object o, String[] strings, Map<String, String> map, String s1,
                                UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddUser(String s, Object o, String[] strings, Map<String, String> map, String s1,
                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateCredential(String s, Object o, Object o1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateCredential(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateCredentialByAdmin(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String s, Object o, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUser(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUser(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreSetUserClaimValue(String s, String s1, String s2, String s3, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostSetUserClaimValue(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreSetUserClaimValues(String s, Map<String, String> map, String s1,
                                           UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostSetUserClaimValues(String s, Map<String, String> map, String s1,
                                            UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValues(String s, String[] strings, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValues(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteUserClaimValue(String s, String s1, String s2, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteUserClaimValue(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreAddRole(String s, String[] strings, Permission[] permissions, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostAddRole(String s, String[] strings, Permission[] permissions,
                                 UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreDeleteRole(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostDeleteRole(String s, UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateRoleName(String s, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateRoleName(String s, String s1, UserStoreManager userStoreManager)
            throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String s, String[] strings, String[] strings1,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String s, String[] strings, String[] strings1,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String s, String[] strings, String[] strings1,
                                             UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String s, String[] strings, String[] strings1,
                                              UserStoreManager userStoreManager) throws UserStoreException {

        return false;
    }
}
