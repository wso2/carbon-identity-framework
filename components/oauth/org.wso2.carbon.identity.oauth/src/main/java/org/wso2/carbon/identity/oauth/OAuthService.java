/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth;

import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.authn.oauth.OAuthUtil;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDO;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.dao.OAuthAppDAO;
import org.wso2.carbon.identity.oauth.dao.OAuthConsumerDAO;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerDTO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OAuthService {

    private static final String OAUTH_LATEST_TIMESTAMP = "OAUTH_LATEST_TIMESTAMP";
    private static final String OAUTH_NONCE_STORE = "OAUTH_NONCE_STORE";
    private static Log log = LogFactory.getLog(OAuthService.class);

    /**
     * Checks whether the given consumer is valid or not. This is done by validating the signature,
     * signed by this particular consumer.
     *
     * @param oauthConsumer Parameter related to the OAuth authorization header.
     * @return
     * @throws Exception
     */
    public boolean isOAuthConsumerValid(OAuthConsumerDTO oauthConsumer) throws IdentityException {

        String oAuthSecretKey = getOAuthSecretKey(oauthConsumer.getOauthConsumerKey());

        if (oAuthSecretKey == null) {
            log.debug("Invalid Consumer Key.");
            throw IdentityException.error("Invalid Consumer Key");
        }
        try {
            return validateOauthSignature(oauthConsumer,
                    oAuthSecretKey);
        } catch (AuthenticationException e) {
            throw IdentityException.error(e.getMessage(), e);
        }
    }

    /**
     * Returns the OAuth request token after verifying the consumer. The Service Provider verifies
     * the signature and Consumer Key. If successful, it generates a Request Token and Token Secret
     * and returns them to the Consumer in the HTTP response body as defined in Service Provider
     * Response Parameters. The Service Provider MUST ensure the Request Token cannot be exchanged
     * for an Access Token until the User successfully grants access in Obtaining User
     * Authorization.
     *
     * @param params                        A container for the following attributes.
     * @param params:oauth_consumer_key     (required) : Domain identifying the third-party web
     *                                      application. This is the domain used when registering the application with WSO2
     *                                      Identity Server.
     * @param params:oauth_nonce            (required) : Random 64-bit, unsigned number encoded as an ASCII
     *                                      string in decimal format. The nonce/time-stamp pair should always be unique to
     *                                      prevent replay attacks.
     * @param params:oauth_signature_method (required) : Signature algorithm. The legal values for
     *                                      this parameter "RSA-SHA1" or "HMAC-SHA1". WSO2 does not support "PLAINTEXT" and
     *                                      "RSA-SHA1".
     * @param params:oauth_signature        (required) String generated using the referenced signature
     *                                      method.
     * @param params:oauth_timestamp        (required) : Integer representing the time the request is sent.
     *                                      The time-stamp should be expressed in number of seconds after January 1, 1970
     *                                      00:00:00 GMT. scope (required) URL identifying the service(s) to be accessed. The
     *                                      resulting token enables access to the specified service(s) only.
     * @param params:scope                  : The resource the third party web application should be authorized to
     *                                      access. To specify more than one scope, list each one separated with a space. This
     *                                      parameter is not defined in the OAuth standards, it is a WSO2-specific parameter.
     * @param params:oauth_callback         (required) : URL the user should be redirected to after access
     *                                      to a service is granted (in response to a call to OAuthAuthorizeToken). The
     *                                      response to this getOauthRequestToken call verifies that WSO2 handles a call-back
     *                                      URL.
     * @param params:oauth_version          (optional) : The OAuth version used by the requesting web
     *                                      application. This value must be "1.0"; if not provided, WSO2 assumes version 1.0
     *                                      is in use.
     * @return oauth_token, oauth_token_secret, oauth_callback_confirmed
     * @throws Exception
     */
    public Parameters getOauthRequestToken(Parameters params) throws AuthenticationException, IdentityOAuthAdminException {

        boolean isValidSignature = false;
        String secretkey = null;

        validateTimestampAndNonce(params.getOauthTimeStamp(), params.getOauthNonce());

        OAuthConsumerDAO dao = new OAuthConsumerDAO();
        secretkey = dao.getOAuthConsumerSecret(params.getOauthConsumerKey());

        if (secretkey == null) {
            log.debug("Invalid Credentials.");
            throw new AuthenticationException("Invalid Credentials.");
        }

        isValidSignature = validateOauthSignature(params, secretkey, null);

        if (!isValidSignature) {
            throw new AuthenticationException("Invalid Signature");
        }

        return generateOauthToken(params);
    }

    /**
     * Authorizes the OAuth request token for the given scope. In order for the Consumer to be able
     * to exchange the Request Token for an Access Token, the Consumer MUST obtain approval from the
     * User by directing the User to the Service Provider. The Consumer constructs an HTTP GET
     * request to the Service Provider's User Authorization URL with the following parameters.
     *
     * @param params             A container for the following attributes.
     * @param params:oauth_token (required) : Request token obtained from WSO2.
     * @param params:userName    : User who authorizes the token.
     * @param params:password    : Password of the user who authorizes the token.
     * @return oauth_token, oauth_verifier
     * @throws Exception
     */
    public Parameters authorizeOauthRequestToken(Parameters params) throws IdentityException, AuthenticationException {
        String tenantUser = MultitenantUtils.getTenantAwareUsername(params.getAuthorizedbyUserName());
        String domainName = MultitenantUtils.getTenantDomain(params.getAuthorizedbyUserName());
        boolean isAuthenticated = false;
        try {
            isAuthenticated = IdentityTenantUtil
                    .getRealm(domainName, params.getAuthorizedbyUserName()).getUserStoreManager()
                    .authenticate(tenantUser, params.getAuthorizedbyUserPassword());
        } catch (UserStoreException e) {
            log.error("Error while authenticating the user", e);
            throw IdentityException.error("Error while authenticating the user");
        }
        if (isAuthenticated) {
            OAuthConsumerDAO dao = new OAuthConsumerDAO();
            String oauthVerifier = org.wso2.carbon.identity.oauth.OAuthUtil.getRandomNumber();
            Parameters token = dao.authorizeOAuthToken(params.getOauthToken(), tenantUser,
                    oauthVerifier);
            token.setOauthToken(params.getOauthToken());
            token.setOauthTokenVerifier(oauthVerifier);
            return token;
        } else {
            throw new AuthenticationException("User Authentication Failed");
        }
    }

    /**
     * Exchanges the authorized OAuth token to an access token. To request an Access Token, the
     * Consumer makes an HTTP request to the Service Provider's Access Token URL. The Service
     * Provider documentation specifies the HTTP method for this request, and HTTP POST is
     * RECOMMENDED. The request MUST be signed per Signing Requests. The Service Provider MUST
     * ensure that: The request signature has been successfully verified. The Request Token has
     * never been exchanged for an Access Token. The Request Token matches the Consumer Key. The
     * verification code received from the Consumer has been successfully verified.
     *
     * @param params                        A container for the following attributes.
     * @param params:oauth_consumer_key     (required) : Domain identifying the third-party web
     *                                      application. This is the domain used when registering the application with WSO2
     *                                      Identity Server.
     * @param params:oauth_nonce            (required) : Random 64-bit, unsigned number encoded as an ASCII
     *                                      string in decimal format. The nonce/time-stamp pair should always be unique to
     *                                      prevent replay attacks.
     * @param params:oauth_signature_method (required) : Signature algorithm. The legal values for
     *                                      this parameter "RSA-SHA1" or "HMAC-SHA1". WSO2 does not support "PLAINTEXT" and
     *                                      "RSA-SHA1".
     * @param params:oauth_signature        (required) String generated using the referenced signature
     *                                      method.
     * @param params:oauth_timestamp        (required) : Integer representing the time the request is sent.
     *                                      The time-stamp should be expressed in number of seconds after January 1, 1970
     *                                      00:00:00 GMT. scope (required) URL identifying the service(s) to be accessed. The
     *                                      resulting token enables access to the specified service(s) only.
     * @param params:oauth_version          (optional) : The OAuth version used by the requesting web
     *                                      application. This value must be "1.0"; if not provided, WSO2 assumes version 1.0
     *                                      is in use.
     * @return oauth_token, oauth_token_secret
     * @throws Exception
     */
    public Parameters getAccessToken(Parameters params) throws IdentityOAuthAdminException, AuthenticationException,
            IdentityException {

        boolean isValidSignature = false;
        String secretKey = null;

        OAuthConsumerDAO dao = new OAuthConsumerDAO();
        secretKey = dao.getOAuthConsumerSecret(params.getOauthConsumerKey());

        if (secretKey == null) {
            log.debug("Invalid Credentials.");
            throw new AuthenticationException("Invalid Credentials.");
        }

        String tokenSecret = dao.getOAuthTokenSecret(params.getOauthToken(), false);

        isValidSignature = validateOauthSignature(params, secretKey, tokenSecret);

        if (!isValidSignature) {
            throw new AuthenticationException("Invalid Signature");
        }

        // The request signature has been successfully verified

        Parameters resp = dao.getRequestToken(params.getOauthToken());

        if (resp.getOauthTokenVerifier() == null
                || !resp.getOauthTokenVerifier().equals(params.getOauthTokenVerifier())
                || resp.getAuthorizedbyUserName() == null) {
            throw new AuthenticationException("Invalid request for OAuth access token");
        }

        // The Request Token has never been exchanged for an Access Token resp.isAccessTokenIssued()
        // = false

        // The verification code received from the Consumer has been successfully verified -
        // resp.getOauthTokenVerifier()

        String oauthToken = org.wso2.carbon.identity.oauth.OAuthUtil.getRandomNumber();
        String oauthSecret = org.wso2.carbon.identity.oauth.OAuthUtil.getRandomNumber();

        dao.issueAccessToken(params.getOauthConsumerKey(), oauthToken, oauthSecret,
                params.getOauthToken(), resp.getAuthorizedbyUserName(), resp.getScope());
        resp.setOauthToken(oauthToken);
        resp.setOauthTokenSecret(oauthSecret);
        return resp;
    }

    /**
     * Returns the scope and the web application this particular token been issued to.
     *
     * @param oauthToken OAuth request token.
     * @return
     * @throws Exception
     */
    public Parameters getScopeAndAppName(String oauthToken) throws Exception {
        OAuthConsumerDAO consumerDAO = new OAuthConsumerDAO();
        Parameters params = consumerDAO.getRequestToken(oauthToken);

        OAuthAppDAO appDAO = new OAuthAppDAO();
        OAuthAppDO oauthAppDO = appDAO.getAppInformation(params.getOauthConsumerKey());

        Parameters resp = new Parameters();
        resp.setScope(params.getScope());
        resp.setAppName(oauthAppDO.getApplicationName());

        return resp;
    }

    /**
     * Validates the request to a resource protected with OAuth. After successfully receiving the
     * Access Token and Token Secret, the Consumer is able to access the Protected Resources on
     * behalf of the User.
     *
     * @param params                        A container for the following attributes.
     * @param params:oauth_consumer_key     (required) : Domain identifying the third-party web
     *                                      application. This is the domain used when registering the application with WSO2
     *                                      Identity Server.
     * @param params:oauth_token            (required) : OAuth access token.
     * @param params:oauth_nonce            (required) : Random 64-bit, unsigned number encoded as an ASCII
     *                                      string in decimal format. The nonce/time-stamp pair should always be unique to
     *                                      prevent replay attacks.
     * @param params:oauth_signature_method (required) : Signature algorithm. The legal values for
     *                                      this parameter "RSA-SHA1" or "HMAC-SHA1". WSO2 does not support "PLAINTEXT" and
     *                                      "RSA-SHA1".
     * @param params:oauth_signature        (required) String generated using the referenced signature
     *                                      method.
     * @param params:oauth_timestamp        (required) : Integer representing the time the request is sent.
     *                                      The time-stamp should be expressed in number of seconds after January 1, 1970
     *                                      00:00:00 GMT. scope (required) URL identifying the service(s) to be accessed. The
     *                                      resulting token enables access to the specified service(s) only.
     * @param params:oauth_version          (optional) : The OAuth version used by the requesting web
     *                                      application. This value must be "1.0"; if not provided, WSO2 assumes version 1.0
     *                                      is in use.
     * @return Parameters : scope : the authorized scope
     * @throws Exception Error when validating the access token request.
     */
    public Parameters validateAuthenticationRequest(Parameters params) throws AuthenticationException, IdentityException {

        boolean isAuthenticated = false;
        String secretKey = null;

        validateTimestampAndNonce(params.getOauthTimeStamp(), params.getOauthNonce());

        OAuthConsumerDAO dao = new OAuthConsumerDAO();
        secretKey = dao.getOAuthConsumerSecret(params.getOauthConsumerKey());
        if (secretKey == null) {
            log.debug("Invalid Credentials.");
            throw new AuthenticationException("Invalid Credentials.");
        }

        String tokenSecret = dao.getOAuthTokenSecret(params.getOauthToken(), true);

        isAuthenticated = validateOauthSignature(params, secretKey, tokenSecret);

        if (isAuthenticated) {
            // Signature is verified - so this is a valid OAuth consumer.
            String subject = dao.validateAccessToken(params.getOauthConsumerKey(),
                    params.getOauthToken(), params.getScope());
            Parameters returnParams = new Parameters();
            returnParams.setAuthorizedbyUserName(subject);
            returnParams.setScope(params.getScope());
            return returnParams;
        } else {
            throw new AuthenticationException("Invalid Signature.");
        }
    }

    /**
     * @param oauthParams
     * @return
     * @throws RegistryException
     * @throws IdentityException
     */
    private Parameters generateOauthToken(Parameters oauthParams) throws IdentityOAuthAdminException {

        OAuthConsumerDAO dao = new OAuthConsumerDAO();

        String oauthToken = org.wso2.carbon.identity.oauth.OAuthUtil.getRandomNumber();
        String oauthSecret = org.wso2.carbon.identity.oauth.OAuthUtil.getRandomNumber();

        dao.createOAuthRequestToken(oauthParams.getOauthConsumerKey(), oauthToken, oauthSecret,
                oauthParams.getOauthCallback(), oauthParams.getScope());

        Parameters params = new Parameters();
        params.setOauthConsumerKey(oauthParams.getOauthConsumerKey());
        params.setOauthToken(oauthToken);
        params.setOauthTokenSecret(oauthSecret);

        return params;
    }

    /**
     * @param oauthParams
     * @param secretKey
     * @return
     * @throws Exception
     */
    private boolean validateOauthSignature(OAuthConsumerDTO oauthParams, String secretKey)
            throws AuthenticationException {

        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(oauthParams.getOauthConsumerKey());
        oauthParameters.setOAuthConsumerSecret(secretKey);
        oauthParameters.setOAuthNonce(oauthParams.getOauthNonce());
        oauthParameters.setOAuthTimestamp(oauthParams.getOauthTimeStamp());
        oauthParameters.setOAuthSignatureMethod(oauthParams.getOauthSignatureMethod());

        validateTimestampAndNonce(oauthParams.getOauthTimeStamp(), oauthParams.getOauthNonce());

        OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
        String signature;
        try {
            String baseString = OAuthUtil.getSignatureBaseString(oauthParams.getBaseString(),
                    oauthParams.getHttpMethod(), oauthParameters.getBaseParameters());
            signature = signer.getSignature(baseString, oauthParameters);
        } catch (OAuthException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }

        if (signature != null
                && URLEncoder.encode(signature).equals(oauthParams.getOauthSignature())) {
            return true;
        } else if (signature != null && signature.equals(oauthParams.getOauthSignature())) {
            return true;
        }
        return false;
    }

    /**
     * @param oauthParams
     * @param secretKey
     * @return
     * @throws Exception
     */
    private boolean validateOauthSignature(Parameters oauthParams, String secretKey, String tokenSecret)
            throws AuthenticationException {

        GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
        oauthParameters.setOAuthConsumerKey(oauthParams.getOauthConsumerKey());
        oauthParameters.setOAuthConsumerSecret(secretKey);
        oauthParameters.setOAuthNonce(oauthParams.getOauthNonce());
        oauthParameters.setOAuthTimestamp(oauthParams.getOauthTimeStamp());
        oauthParameters.setOAuthSignatureMethod(oauthParams.getOauthSignatureMethod());

        if (oauthParams.getOauthToken() != null) {
            oauthParameters.setOAuthToken(oauthParams.getOauthToken());
        }

        if (oauthParams.getOauthTokenVerifier() != null) {
            oauthParameters.setOAuthVerifier((oauthParams.getOauthTokenVerifier()));
        }

        if (tokenSecret != null) {
            oauthParameters.setOAuthTokenSecret(tokenSecret);
        }

        OAuthHmacSha1Signer signer = new OAuthHmacSha1Signer();
        String signature;
        try {
            String baseString = OAuthUtil.getSignatureBaseString(oauthParams.getBaseString(),
                    oauthParams.getHttpMethod(), oauthParameters.getBaseParameters());
            signature = signer.getSignature(baseString, oauthParameters);
        } catch (OAuthException e) {
            throw new AuthenticationException("Error while validating signature");
        }

        if (signature != null
                && URLEncoder.encode(signature).equals(oauthParams.getOauthSignature())) {
            return true;
        } else if (signature != null && signature.equals(oauthParams.getOauthSignature())) {
            return true;
        }
        return false;
    }

    /**
     * Unless otherwise specified by the Service Provider, the time-stamp is expressed in the number
     * of seconds since January 1, 1970 00:00:00 GMT. The time-stamp value MUST be a positive
     * integer and MUST be equal or greater than the time-stamp used in previous requests. The
     * Consumer SHALL then generate a Nonce value that is unique for all requests with that
     * timestamp. A nonce is a random string, uniquely generated for each request. The nonce allows
     * the Service Provider to verify that a request has never been made before and helps prevent
     * replay attacks when requests are made over a non-secure channel (such as HTTP).
     *
     * @param timestamp
     * @param nonce
     * @throws Exception
     */
    private void validateTimestampAndNonce(String timestamp, String nonce) throws AuthenticationException {
        if (timestamp == null || nonce == null || nonce.trim().length() == 0) {
            // We are not going to give out the exact error why the request failed.
            throw new AuthenticationException("Invalid request for OAuth access token");
        }

        long time = Long.parseLong(timestamp);

        synchronized (this) {
            long latestTimeStamp = 0;
            String strTimestamp;
            ServiceContext context = MessageContext.getCurrentMessageContext().getServiceContext();

            if ((strTimestamp = (String) context.getProperty(OAUTH_LATEST_TIMESTAMP)) != null) {
                latestTimeStamp = Long.parseLong(strTimestamp);
            }

            if (time < 0 || time < latestTimeStamp) {
                // The time-stamp value MUST be a positive integer and MUST be equal or greater than
                // the time-stamp used in previous requests
                throw new AuthenticationException("Invalid timestamp");
            }
            context.setProperty(OAUTH_LATEST_TIMESTAMP, String.valueOf(time));

            List<String> nonceStore = null;

            if ((nonceStore = (List<String>) context.getProperty(OAUTH_NONCE_STORE)) != null) {
                if (nonceStore.contains(nonce)) {
                    // We are not going to give out the exact error why the request failed.
                    throw new AuthenticationException("Invalid request for OAuth access token");
                } else {
                    nonceStore.add(nonce);
                }
            } else {
                nonceStore = new ArrayList<String>();
                nonceStore.add(nonce);
                context.setProperty(OAUTH_NONCE_STORE, nonceStore);
            }
        }

    }

    /**
     * @param consumerKey Consumer Key provided by the user
     * @return consumer secret
     * @throws Exception Error when reading the consumer secret from the persistence store.
     */
    private String getOAuthSecretKey(String consumerKey) throws IdentityOAuthAdminException {
        OAuthConsumerDAO dao = new OAuthConsumerDAO();
        return dao.getOAuthConsumerSecret(consumerKey);
    }

}
