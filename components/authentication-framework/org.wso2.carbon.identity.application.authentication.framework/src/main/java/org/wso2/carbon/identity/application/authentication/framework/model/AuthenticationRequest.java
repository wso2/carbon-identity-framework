/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This cache keeps all parameters and headers which are directed towards authentication
 * framework. Whenever a request to authentication framework comes, The relevant component which
 * sends the request saves all required information to this cache, which are retrieved later from
 * authentication framework
 */
@SuppressWarnings("unused")
public class AuthenticationRequest implements Serializable {

    private static final long serialVersionUID = 8131978212432223682L;

    /**
     * Type of the request coming to framework eg:saml
     */
    private String type;
    /**
     * Calling entity to framework
     */
    private String commonAuthCallerPath;
    /**
     * Whether the request is force authentication request
     */
    private boolean forceAuth;
    /**
     * Whether the request is passive authentication request
     */
    private boolean passiveAuth;
    /**
     * Tenant domain of the caller application
     */
    private String tenantDomain;
    /**
     * Whether the request is a post or redirect
     */
    private boolean isPost;
    /**
     * Relying party of the request
     */
    private String relyingParty;
    /**
     * used to store query params which should be sent to Authentication Framework
     */
    private Map<String, String[]> requestQueryParams = new HashMap<String, String[]>();
    /**
     * used to store request headers which should be sent to Authentication Framework.
     */
    private Map<String, String> requestHeaders = new HashMap<String, String>();

    /**
     * To retrieve request query params which are stored.
     *
     * @return A map of query parameters
     */
    public Map<String, String[]> getRequestQueryParams() {
        return requestQueryParams;
    }

    /**
     * Set request query params which are comming from the calling servelets
     *
     * @param requestQueryParams Map of query params
     */
    public void setRequestQueryParams(Map<String, String[]> requestQueryParams) {
        this.requestQueryParams.putAll(requestQueryParams);
    }

    /**
     * Add headers which are in the authentication request.
     *
     * @param key    Key of the header
     * @param values value of the header
     */
    public void addHeader(String key, String values) {
        requestHeaders.put(key, values);
    }

    /**
     * Get relying party of the authentication request
     *
     * @return relying party of the authentication request
     */
    public String getRelyingParty() {
        return relyingParty;
    }

    /**
     * Set the relying party of the authentication request
     *
     * @param relyingParty Relying party. Party that sends the request
     */
    public void setRelyingParty(String relyingParty) {
        this.relyingParty = relyingParty;
    }

    /**
     * Get request headers in authentication requests
     *
     * @return A map of headers in authentication request
     */
    public Map<String, String> getRequestHeaders() {
        return requestHeaders;
    }

    /**
     * Whether the request is a post or not
     *
     * @return true if the request is post, else false
     */
    public boolean isPost() {
        return isPost;
    }

    /**
     * Set the type of request. If it is POST sets this to true. If GET sets to false
     *
     * @param post True if the request is a POST. false if it is GET
     */
    public void setPost(boolean post) {
        isPost = post;
    }

    /**
     * Add a parameter to the set of query params
     *
     * @param key   Key of the Query param
     * @param value Value of the query param
     */
    public void addRequestQueryParam(String key, String[] value) {
        requestQueryParams.put(key, value);
    }

    /**
     * Get the query param with specified key
     *
     * @param key Key of the query param
     * @return Value of the query param with the requested key.
     */
    public String[] getRequestQueryParam(String key) {
        return requestQueryParams.get(key);
    }

    /**
     * Append params to already existing set of request query params
     *
     * @param map Map of new params
     */
    public void appendRequestQueryParams(Map<String, String[]> map) {
        requestQueryParams.putAll(map);
    }

    /**
     * Get the tenant domain
     *
     * @return Tenant domain
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Set the tenant domain which the authentication request is comming from
     *
     * @param tenantDomain Tenant Domain
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Whether the request is a force auth request or not
     *
     * @return True if the request is force authenticate request. else false
     */
    public boolean getForceAuth() {
        return forceAuth;
    }

    /**
     * Set the force auth status.
     *
     * @param forceAuth True if the incoming request is force authenticate request.
     */
    public void setForceAuth(boolean forceAuth) {
        this.forceAuth = forceAuth;
    }

    /**
     * Get common auth caller path. Path of the calling party to common auth
     *
     * @return Common auth caller path
     */
    public String getCommonAuthCallerPath() {
        return commonAuthCallerPath;
    }

    /**
     * set common auth caller path. Path of the calling party to common auth
     *
     * @param commonAuthCallerPath Path which the common auth endpoint is called from
     */
    public void setCommonAuthCallerPath(String commonAuthCallerPath) {
        this.commonAuthCallerPath = commonAuthCallerPath;
    }

    /**
     * Type of the request. ex - saml
     *
     * @return Type of the request
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type of the request
     *
     * @param type Type of the request
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get whether the request is a passive authentication request
     *
     * @return True if the request is passive authentication request. else false
     */
    public boolean getPassiveAuth() {
        return passiveAuth;
    }

    /**
     * Set passive auth. Whether the authentication request is a passive one
     *
     * @param passiveAuth True if the authentication is passive. Else false
     */
    public void setPassiveAuth(boolean passiveAuth) {
        this.passiveAuth = passiveAuth;
    }

}
