/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.graal;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.AbstractJSObjectWrapper;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.JsAuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;

/**
 * Javascript wrapper for Java level AuthenticatedUser for GraalJs Execution.
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
public class GraalJsAuthenticatedUser extends AbstractJSObjectWrapper<AuthenticatedUser>
        implements ProxyObject, JsAuthenticatedUser {

    private static final Log LOG = LogFactory.getLog(GraalJsAuthenticatedUser.class);
    private int step;
    private String idp;

    /**
     * Constructor to be used when required to access step specific user details.
     *
     * @param context Authentication context
     * @param wrappedUser Authenticated user
     * @param step        Authentication step
     * @param idp         Authenticated Idp
     */
    public GraalJsAuthenticatedUser(AuthenticationContext context,
                                    AuthenticatedUser wrappedUser, int step, String idp) {

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
    public GraalJsAuthenticatedUser(AuthenticatedUser wrappedUser, int step, String idp) {

        super(wrappedUser);
        this.step = step;
        this.idp = idp;
    }

    /**
     * Constructor to be used when required to access step independent user.
     *
     * @param wrappedUser Authenticated user
     */
    public GraalJsAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public GraalJsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

        this(wrappedUser);
        initializeContext(context);
    }

    @Override
    public Object getMember(String name) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_AUTHENTICATED_SUBJECT_IDENTIFIER:
                return getWrapped().getAuthenticatedSubjectIdentifier();
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                return getWrapped().getUserName();
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                return getWrapped().getUserStoreDomain();
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                return getWrapped().getTenantDomain();
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new GraalJsClaims(getContext(), step, idp, false);
                } else {
                    // Represent step independent user
                    return new GraalJsClaims(getContext(), getWrapped(), false);
                }
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new GraalJsClaims(getContext(), step, idp, true);
                } else {
                    // Represent step independent user
                    return new GraalJsClaims(getContext(), getWrapped(), true);
                }
            case FrameworkConstants.JSAttributes.JS_LOCAL_ROLES:
                return getLocalRoles();
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new GraalJsRuntimeClaims(getContext(), step, idp);
                } else {
                    // Represent step independent user
                    return new GraalJsRuntimeClaims(getContext(), getWrapped());
                }
            default:
                return super.getMember(name);
        }
    }

    @Override
    public Object getMemberKeys() {

        return  ProxyArray.fromArray(new String[]{ FrameworkConstants.JSAttributes.JS_USERNAME});
    }

    public void putMember(String name, Value value) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                getWrapped().setUserName(value.asString());
                break;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                getWrapped().setUserStoreDomain(value.asString());
                break;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                getWrapped().setTenantDomain(value.asString());
                break;
        }
    }

    @Override
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
            // idp check is performed in getMember() method for following cases
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                return true;
            default:
                return super.hasMember(name);
        }
    }

    private String[] getLocalRoles() {

        if (idp == null || FrameworkConstants.LOCAL.equals(idp)) {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            int usersTenantId = IdentityTenantUtil.getTenantId(getWrapped().getTenantDomain());

            try {
                String usernameWithDomain = UserCoreUtil.addDomainToName(getWrapped().getUserName(), getWrapped()
                    .getUserStoreDomain());
                UserRealm userRealm = realmService.getTenantUserRealm(usersTenantId);
                return userRealm.getUserStoreManager().getRoleListOfUser(usernameWithDomain);
            } catch (UserStoreException e) {
                LOG.error("Error when getting role list of user: " + getWrapped(), e);
            }
        }
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }
}
