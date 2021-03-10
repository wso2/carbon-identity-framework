package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.base;

import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;

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
public abstract class JsBaseAuthenticatedUser {


}
