/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base.JsBaseAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserIdNotFoundException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.List;

/**
 * Abstract Javascript wrapper for Java level AuthenticatedUser.
 * This provides controlled access to AuthenticatedUser object via provided javascript native syntax.
 * e.g
 * var userName = context.lastAuthenticatedUser.username
 * <p>
 * instead of
 * var userName = context.getLastAuthenticatedUser().getUserName()
 * <p>
 * Also it prevents writing an arbitrary values to the respective fields, keeping consistency on runtime
 * AuthenticatedUser.
 *
 * @see AuthenticatedUser
 */
public abstract class JsAuthenticatedUser extends AbstractJSObjectWrapper<AuthenticatedUser>
        implements JsBaseAuthenticatedUser {

    private static final Log LOG = LogFactory.getLog(JsAuthenticatedUser.class);
    protected int step;
    protected String idp;

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param context Authentication context
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser,
                               int step, String idp) {

        this(wrappedUser, step, idp);
        initializeContext(context);
    }

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public JsAuthenticatedUser(AuthenticatedUser wrappedUser, int step, String idp) {

        super(wrappedUser);
        this.step = step;
        this.idp = idp;
    }

    /**
     * Constructor to be used when required to access step independent user.
     *
     * @param wrappedUser Authenticated user
     */
    public JsAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public JsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

        this(wrappedUser);
        initializeContext(context);
    }

    public void setMember(String name, Object value) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                getWrapped().setUserName((String) value);
                break;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                getWrapped().setUserStoreDomain((String) value);
                break;
            default:
                super.setMember(name, value);
        }
    }

    public boolean hasMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT_IDENTIFIER:
                return getWrapped().getAuthenticatedSubjectIdentifier() != null;
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName() != null;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain() != null;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain() != null;
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                return idp != null;
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                return idp != null && !FrameworkConstants.LOCAL.equals(idp);
            default:
                return super.hasMember(name);
        }
    }

    protected String[] getLocalRoles() {

        if (idp == null || FrameworkConstants.LOCAL.equals(idp)) {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            int usersTenantId = IdentityTenantUtil.getTenantId(getWrapped().getTenantDomain());

            try {
                UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
                List<String> roleListOfUser =
                        ((AbstractUserStoreManager) userRealm.getUserStoreManager())
                                .getRoleListOfUserWithID(getWrapped().getUserId());
                return roleListOfUser.toArray(new String[0]);
            } catch (UserStoreException e) {
                LOG.error("Error when getting role list of user: " + getWrapped(), e);
            } catch (UserIdNotFoundException e) {
                LOG.error("User id is not available for user: " + getWrapped().getLoggableUserId(), e);
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
