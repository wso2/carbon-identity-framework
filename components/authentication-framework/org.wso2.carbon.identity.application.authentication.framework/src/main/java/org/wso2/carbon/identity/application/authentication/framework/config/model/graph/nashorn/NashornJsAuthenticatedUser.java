package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.nashorn;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * Javascript wrapper for Java level AuthenticatedUser for Nashorn.
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
public class NashornJsAuthenticatedUser extends AbstractJSObjectWrapper<AuthenticatedUser>
        implements JsAuthenticatedUser {

    private static final Log LOG = LogFactory.getLog(JsAuthenticatedUser.class);
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
    public NashornJsAuthenticatedUser(AuthenticationContext context,
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
    public NashornJsAuthenticatedUser(AuthenticatedUser wrappedUser, int step, String idp) {

        super(wrappedUser);
        this.step = step;
        this.idp = idp;
    }

    /**
     * Constructor to be used when required to access step independent user.
     *
     * @param wrappedUser Authenticated user
     */
    public NashornJsAuthenticatedUser(AuthenticatedUser wrappedUser) {

        super(wrappedUser);
    }

    public NashornJsAuthenticatedUser(AuthenticationContext context, AuthenticatedUser wrappedUser) {

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
                    return new NashornJsClaims(getContext(), step, idp, false);
                } else {
                    // Represent step independent user
                    return new NashornJsClaims(getContext(), getWrapped(), false);
                }
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new NashornJsClaims(getContext(), step, idp, true);
                } else {
                    // Represent step independent user
                    return new NashornJsClaims(getContext(), getWrapped(), true);
                }
            case FrameworkConstants.JSAttributes.JS_LOCAL_ROLES:
                return getLocalRoles();
            case FrameworkConstants.JSAttributes.JS_CLAIMS:
                if (StringUtils.isNotBlank(idp)) {
                    return new NashornJsRuntimeClaims(getContext(), step, idp);
                } else {
                    // Represent step independent user
                    return new NashornJsRuntimeClaims(getContext(), getWrapped());
                }
            default:
                return super.getMember(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        switch (name) {
            case FrameworkConstants.JSAttributes.JS_USERNAME:
                getWrapped().setUserName((String) value);
                break;
            case FrameworkConstants.JSAttributes.JS_USER_STORE_DOMAIN:
                getWrapped().setUserStoreDomain((String) value);
                break;
            case FrameworkConstants.JSAttributes.JS_TENANT_DOMAIN:
                getWrapped().setTenantDomain((String) value);
                break;
            default:
                super.setMember(name, value);
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
            case FrameworkConstants.JSAttributes.JS_LOCAL_CLAIMS:
                return idp != null;
            case FrameworkConstants.JSAttributes.JS_REMOTE_CLAIMS:
                return idp != null && !FrameworkConstants.LOCAL.equals(idp);
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

