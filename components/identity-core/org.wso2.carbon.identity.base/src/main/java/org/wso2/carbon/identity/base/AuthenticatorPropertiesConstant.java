package org.wso2.carbon.identity.base;

public class AuthenticatorPropertiesConstant {

    /**
     * The Defined by Types - SYSTEM: system define authenticator, USER: user defined authentication extension.
     */
    public static enum DefinedByType {

        SYSTEM,
        USER
    }

    /**
     * The Authentication Types -
     *      External User Account Authentication: This authenticator can authenticate federated users
     *                                            and provision them.
     *      Internal User Account Authentication: This authenticator collects the identifiers and authenticates user
     *                                            accounts managed within the organization.
     *      2FA Authentication: This authenticator can only verify users in the second or
     *                          subsequent steps of the login process.
     */
    public static enum AuthenticationType {

        IDENTIFICATION,
        VERIFICATION_ONLY,
        REQUEST_PATH
    }
 }
