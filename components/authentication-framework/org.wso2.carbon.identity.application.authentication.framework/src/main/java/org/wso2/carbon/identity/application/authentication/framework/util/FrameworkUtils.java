/*
 * Copyright (c) 2013-2023, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.claim.mgt.ClaimManagementException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.SameSiteCookie;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.identity.application.authentication.framework.ApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationDataPublisher;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticationFlowHandler;
import org.wso2.carbon.identity.application.authentication.framework.AuthenticatorFlowStatus;
import org.wso2.carbon.identity.application.authentication.framework.FederatedApplicationAuthenticator;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationErrorCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationErrorCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationErrorCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationRequestCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.AuthenticationResultCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCache;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheEntry;
import org.wso2.carbon.identity.application.authentication.framework.cache.SessionContextCacheKey;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.builder.FileBasedConfigurationBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.AuthenticatorConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ExternalIdPConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.OptimizedApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsBaseGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGenericGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.JsGraalGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn.JsOpenJdkNashornGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.SessionContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.exception.InvalidCredentialsException;
import org.wso2.carbon.identity.application.authentication.framework.exception.PostAuthenticationFailedException;
import org.wso2.carbon.identity.application.authentication.framework.exception.UserSessionException;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.ApplicationRolesResolver;
import org.wso2.carbon.identity.application.authentication.framework.handler.approles.exception.ApplicationRolesException;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.ClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.claims.impl.DefaultClaimHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.HomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.hrd.impl.DefaultHomeRealmDiscoverer;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.ProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.provisioning.impl.DefaultProvisioningHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.AuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.CallBackHandlerFactory;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.LogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.RequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultAuthenticationRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultLogoutRequestHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultRequestCoordinator;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.RequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.StepBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.DefaultRequestPathBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl.GraphBasedSequenceHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.StepHandler;
import org.wso2.carbon.identity.application.authentication.framework.handler.step.impl.GraphBasedStepHandler;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceComponent;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedIdPData;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationError;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationFrameworkWrapper;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationRequest;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticationResult;
import org.wso2.carbon.identity.application.authentication.framework.store.UserSessionStore;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.FederatedAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.IdPGroup;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.model.Property;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.ApplicationConstants;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;
import org.wso2.carbon.identity.claim.metadata.mgt.ClaimMetadataHandler;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.configuration.mgt.core.exception.ConfigurationManagementException;
import org.wso2.carbon.identity.configuration.mgt.core.model.Attribute;
import org.wso2.carbon.identity.core.ServiceURLBuilder;
import org.wso2.carbon.identity.core.URLBuilderException;
import org.wso2.carbon.identity.core.model.CookieBuilder;
import org.wso2.carbon.identity.core.model.IdentityCookieConfig;
import org.wso2.carbon.identity.core.util.IdentityCoreConstants;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.event.IdentityEventConstants;
import org.wso2.carbon.identity.event.IdentityEventException;
import org.wso2.carbon.identity.event.event.Event;
import org.wso2.carbon.identity.event.services.IdentityEventService;
import org.wso2.carbon.identity.multi.attribute.login.mgt.ResolvedUserResult;
import org.wso2.carbon.identity.role.v2.mgt.core.exception.IdentityRoleManagementException;
import org.wso2.carbon.identity.user.profile.mgt.association.federation.FederatedAssociationManager;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;
import org.wso2.carbon.idp.mgt.IdpManager;
import org.wso2.carbon.idp.mgt.util.IdPManagementUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.UserStorePreferenceOrderSupplier;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.DiagnosticLog;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.AdaptiveAuthentication.AUTHENTICATOR_NAME_IN_AUTH_CONFIG;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.CONSOLE_APP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.CONSOLE_APP_PATH;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.MY_ACCOUNT_APP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Application.MY_ACCOUNT_APP_PATH;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.CONTEXT_PROP_INVALID_EMAIL_USERNAME;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.AUTHENTICATION_CONTEXT_EXPIRY_VALIDATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.SKIP_LOCAL_USER_SEARCH_FOR_AUTHENTICATION_FLOW_HANDLERS;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.Config.USER_SESSION_MAPPING_ENABLED;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.ENABLE_CONFIGURED_IDP_SUB_FOR_FEDERATED_USER_ASSOCIATION;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.APPLICATION_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.InternalRoleDomains.WORKFLOW_DOMAIN;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.REQUEST_PARAM_SP;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.CORRELATION_ID;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.IS_IDF_INITIATED_FROM_AUTHENTICATOR;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.RequestParams.USER_TENANT_DOMAIN_HINT;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USERNAME_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants.USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM;
import static org.wso2.carbon.identity.application.authentication.framework.util.FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_IDP_BY_NAME;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.configuration.mgt.core.constant.ConfigurationConstants.ErrorMessages.ERROR_CODE_RESOURCE_DOES_NOT_EXISTS;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ATTRIBUTE_NAME;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ENABLED;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_NAME;
import static org.wso2.carbon.identity.core.util.IdentityCoreConstants.ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_TYPE;
import static org.wso2.carbon.identity.core.util.IdentityTenantUtil.isLegacySaaSAuthenticationEnabled;
import static org.wso2.carbon.identity.core.util.IdentityUtil.getLocalGroupsClaimURI;

/**
 * Utility methods for authentication framework.
 */
public class FrameworkUtils {

    public static final String SESSION_DATA_KEY = "sessionDataKey";
    public static final String TENANT_DOMAIN = "tenantDomain";
    public static final String UTF_8 = "UTF-8";
    private static final Log log = LogFactory.getLog(FrameworkUtils.class);
    private static int maxInactiveInterval;
    private static final String EMAIL = "email";
    private static List<String> cacheDisabledAuthenticators = Arrays
            .asList(FrameworkConstants.RequestType.CLAIM_TYPE_SAML_SSO, FrameworkConstants.OAUTH2);

    public static final String QUERY_SEPARATOR = "&";
    public static final String EQUAL = "=";
    public static final String REQUEST_PARAM_APPLICATION = "application";
    private static final String ALREADY_WRITTEN_PROPERTY = "AlreadyWritten";

    private static final String CONTINUE_ON_CLAIM_HANDLING_ERROR = "ContinueOnClaimHandlingError";
    public static final String CORRELATION_ID_MDC = "Correlation-ID";

    private static boolean isTenantIdColumnAvailableInFedAuthTable = false;
    private static boolean isIdpIdColumnAvailableInFedAuthTable = false;
    public static final String ROOT_DOMAIN = "/";

    private static final String HASH_CHAR = "#";
    private static final String HASH_CHAR_ENCODED = "%23";
    private static final String QUESTION_MARK = "?";

    private static Boolean authenticatorNameInAuthConfigPreference;
    private static final String OPENJDK_SCRIPTER_CLASS_NAME = "org.openjdk.nashorn.api.scripting.ScriptObjectMirror";
    private static final String JDK_SCRIPTER_CLASS_NAME = "jdk.nashorn.api.scripting.ScriptObjectMirror";
    private static final String GRAALJS_SCRIPTER_CLASS_NAME = "org.graalvm.polyglot.Context";

    private FrameworkUtils() {
    }

    public static List<String> getCacheDisabledAuthenticators() {
        return cacheDisabledAuthenticators;
    }

    /**
     * To add authentication request cache entry to cache.
     *
     * @param key          cache entry key
     * @param authReqEntry AuthenticationReqCache Entry.
     */
    public static void addAuthenticationRequestToCache(String key, AuthenticationRequestCacheEntry authReqEntry) {
        AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
        AuthenticationRequestCache.getInstance().addToCache(cacheKey, authReqEntry);
    }

    /**
     * To get authentication cache request from cache.
     *
     * @param key Key of the cache entry
     * @return
     */
    public static AuthenticationRequestCacheEntry getAuthenticationRequestFromCache(String key) {

        AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
        return AuthenticationRequestCache.getInstance().getValueFromCache(cacheKey);
    }

    /**
     * removes authentication request from cache.
     *
     * @param key SessionDataKey
     */
    public static void removeAuthenticationRequestFromCache(String key) {

        if (key != null) {
            AuthenticationRequestCacheKey cacheKey = new AuthenticationRequestCacheKey(key);
            AuthenticationRequestCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * Builds the wrapper, wrapping incoming request and information take from cache entry.
     *
     * @param request    Original request coming to authentication framework
     * @param cacheEntry Cache entry from the cache, which is added from calling servlets
     * @return
     */
    public static HttpServletRequest getCommonAuthReqWithParams(HttpServletRequest request,
                                                                AuthenticationRequestCacheEntry cacheEntry) {

        // add this functionality as a constructor
        Map<String, String[]> modifiableParameters = new TreeMap<String, String[]>();
        if (cacheEntry != null) {
            AuthenticationRequest authenticationRequest = cacheEntry.getAuthenticationRequest();

            if (!authenticationRequest.getRequestQueryParams().isEmpty()) {
                modifiableParameters.putAll(authenticationRequest.getRequestQueryParams());
            }

            // Adding field variables to wrapper
            if (authenticationRequest.getType() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.TYPE,
                                         new String[]{authenticationRequest.getType()});
            }
            if (authenticationRequest.getCommonAuthCallerPath() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.CALLER_PATH,
                                         new String[]{authenticationRequest.getCommonAuthCallerPath()});
            }
            if (authenticationRequest.getRelyingParty() != null) {
                modifiableParameters.put(FrameworkConstants.RequestParams.ISSUER,
                                         new String[]{authenticationRequest.getRelyingParty()});
            }
            if (authenticationRequest.getTenantDomain() != null && !IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                modifiableParameters.put(FrameworkConstants.RequestParams.TENANT_DOMAIN,
                                         new String[]{authenticationRequest.getTenantDomain()});
            }
            modifiableParameters.put(FrameworkConstants.RequestParams.FORCE_AUTHENTICATE,
                                     new String[]{String.valueOf(authenticationRequest.getForceAuth())});
            modifiableParameters.put(FrameworkConstants.RequestParams.PASSIVE_AUTHENTICATION,
                                     new String[]{String.valueOf(authenticationRequest.getPassiveAuth())});

            if (log.isDebugEnabled()) {
                StringBuilder queryStringBuilder = new StringBuilder("");

                for (Map.Entry<String, String[]> entry : modifiableParameters.entrySet()) {
                    StringBuilder paramValueBuilder = new StringBuilder("");
                    String[] paramValueArr = entry.getValue();

                    if (paramValueArr != null) {
                        for (String paramValue : paramValueArr) {
                            paramValueBuilder.append("{").append(paramValue).append("}");
                        }
                    }

                    queryStringBuilder.append("\n").append(
                            entry.getKey() + "=" + paramValueBuilder.toString());
                }

                log.debug("\nInbound Request parameters: " + queryStringBuilder.toString());
            }

            return new AuthenticationFrameworkWrapper(request, modifiableParameters,
                                                      authenticationRequest.getRequestHeaders());
        }
        return request;
    }

    /**
     * @param name
     * @return
     */
    public static ApplicationAuthenticator getAppAuthenticatorByName(String name) {

        for (ApplicationAuthenticator authenticator : FrameworkServiceComponent.getAuthenticators()) {

            if (name.equals(authenticator.getName())) {
                return authenticator;
            }
        }

        return null;
    }

    /**
     * @param request
     * @return
     */
    public static AuthenticationContext getContextData(HttpServletRequest request) {

        AuthenticationContext context = null;
        if (request.getParameter("promptResp") != null && request.getParameter("promptId") != null) {
            String promptId = request.getParameter("promptId");
            context = FrameworkUtils.getAuthenticationContextFromCache(promptId);
            if (context != null) {
                FrameworkUtils.removeAuthenticationContextFromCache(promptId);
                return context;
            }
        }
        for (ApplicationAuthenticator authenticator : FrameworkServiceComponent.getAuthenticators()) {
            try {
                String contextIdentifier = authenticator.getContextIdentifier(request);

                if (contextIdentifier != null && !contextIdentifier.isEmpty()) {
                    context = FrameworkUtils.getAuthenticationContextFromCache(contextIdentifier);
                    if (context != null) {
                        break;
                    }
                }
            } catch (UnsupportedOperationException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Ignore UnsupportedOperationException.", e);
                }
                continue;
            }
        }

        return context;
    }

    public static RequestCoordinator getRequestCoordinator() {

        RequestCoordinator requestCoordinator = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_REQ_COORDINATOR);

        if (obj instanceof RequestCoordinator) {
            requestCoordinator = (RequestCoordinator) obj;
        } else {
            requestCoordinator = DefaultRequestCoordinator.getInstance();
        }

        return requestCoordinator;
    }

    /**
     * @return
     */
    public static AuthenticationRequestHandler getAuthenticationRequestHandler() {

        AuthenticationRequestHandler authenticationRequestHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_AUTH_REQ_HANDLER);

        if (obj instanceof AuthenticationRequestHandler) {
            authenticationRequestHandler = (AuthenticationRequestHandler) obj;
        } else {
            authenticationRequestHandler = DefaultAuthenticationRequestHandler.getInstance();
        }

        return authenticationRequestHandler;
    }

    /**
     * @return
     */
    public static LogoutRequestHandler getLogoutRequestHandler() {

        LogoutRequestHandler logoutRequestHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_LOGOUT_REQ_HANDLER);

        if (obj instanceof LogoutRequestHandler) {
            logoutRequestHandler = (LogoutRequestHandler) obj;
        } else {
            logoutRequestHandler = DefaultLogoutRequestHandler.getInstance();
        }

        return logoutRequestHandler;
    }

    /**
     * Returns the step based sequence handler.
     * @return
     */
    public static StepBasedSequenceHandler getStepBasedSequenceHandler() {

        StepBasedSequenceHandler stepBasedSequenceHandler;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_STEP_BASED_SEQ_HANDLER);
        if (obj instanceof StepBasedSequenceHandler) {
            stepBasedSequenceHandler = (StepBasedSequenceHandler) obj;
        } else {
            stepBasedSequenceHandler = new GraphBasedSequenceHandler();
        }
        return stepBasedSequenceHandler;
    }

    /**
     * @return
     */
    public static RequestPathBasedSequenceHandler getRequestPathBasedSequenceHandler() {

        RequestPathBasedSequenceHandler reqPathBasedSeqHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_REQ_PATH_BASED_SEQ_HANDLER);

        if (obj instanceof RequestPathBasedSequenceHandler) {
            reqPathBasedSeqHandler = (RequestPathBasedSequenceHandler) obj;
        } else {
            reqPathBasedSeqHandler = DefaultRequestPathBasedSequenceHandler.getInstance();
        }

        return reqPathBasedSeqHandler;
    }

    /**
     * @return
     */
    public static StepHandler getStepHandler() {

        StepHandler stepHandler;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_STEP_HANDLER);
        if (obj instanceof StepHandler) {
            stepHandler = (StepHandler) obj;
        } else {
            stepHandler = new GraphBasedStepHandler();
        }
        return stepHandler;
    }

    /**
     * @return
     */
    public static HomeRealmDiscoverer getHomeRealmDiscoverer() {

        HomeRealmDiscoverer homeRealmDiscoverer = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_HRD);

        if (obj instanceof HomeRealmDiscoverer) {
            homeRealmDiscoverer = (HomeRealmDiscoverer) obj;
        } else {
            homeRealmDiscoverer = DefaultHomeRealmDiscoverer.getInstance();
        }

        return homeRealmDiscoverer;
    }

    /**
     * @return
     */
    public static ClaimHandler getClaimHandler() {

        ClaimHandler claimHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_CLAIM_HANDLER);

        if (obj instanceof ClaimHandler) {
            claimHandler = (ClaimHandler) obj;
        } else {
            claimHandler = DefaultClaimHandler.getInstance();
        }

        return claimHandler;
    }

    /**
     * @return
     */
    public static ProvisioningHandler getProvisioningHandler() {

        ProvisioningHandler provisioningHandler = null;
        Object obj = ConfigurationFacade.getInstance().getExtensions()
                .get(FrameworkConstants.Config.QNAME_EXT_PROVISIONING_HANDLER);

        if (obj instanceof ProvisioningHandler) {
            provisioningHandler = (ProvisioningHandler) obj;
        } else {
            provisioningHandler = DefaultProvisioningHandler.getInstance();
        }

        return provisioningHandler;
    }

    /**
     * Create the user store preference order supplier from the configured call back handler factory.
     */
    public static UserStorePreferenceOrderSupplier<List<String>> getUserStorePreferenceOrderSupplier
    (AuthenticationContext context, ServiceProvider serviceProvider) {

        return getCallBackHandlerFactory().createUserStorePreferenceOrderSupplier(context, serviceProvider);
    }

    private static CallBackHandlerFactory getCallBackHandlerFactory() {

        // Retrieve tha call back handler configured at application-authentication.xml file.
        CallBackHandlerFactory userStorePreferenceCallbackHandlerFactory;
        Object obj = ConfigurationFacade.getInstance().getExtensions().get(FrameworkConstants.Config
                .QNAME_EXT_USER_STORE_ORDER_CALLBACK_HANDLER);
        if (obj != null) {
            userStorePreferenceCallbackHandlerFactory = (CallBackHandlerFactory) obj;
        } else {
            userStorePreferenceCallbackHandlerFactory = new CallBackHandlerFactory();
        }
        return userStorePreferenceCallbackHandlerFactory;
    }

    /**
     * @param request
     * @param response
     * @throws IOException
     * @deprecated use {@link #sendToRetryPage(HttpServletRequest, HttpServletResponse, AuthenticationContext)}.
     */
    @Deprecated
    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // TODO read the URL from framework config file rather than carbon.xml
        sendToRetryPage(request, response, null, null);
    }

    /**
     * Send user to retry page during an authentication flow failure.
     *
     * @param request       Http servlet request.
     * @param response      Http servlet response.
     * @param status        Failure status.
     * @param statusMsg     Failure status message.
     * @throws IOException
     * @deprecated use
     * {@link #sendToRetryPage(HttpServletRequest, HttpServletResponse, AuthenticationContext, String, String)}.
     */
    @Deprecated
    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response, String status,
                                       String statusMsg) throws IOException {

        sendToRetryPage(request, response, null, status, statusMsg);
    }

    /**
     * Send user to retry page during an authentication flow failure.
     *
     * @param request       Http servlet request.
     * @param response      Http servlet response.
     * @param context       Authentication Context.
     * @throws IOException
     */
    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationContext context) throws IOException {

        sendToRetryPage(request, response, context, null, null);
    }

    /**
     * Send user to retry page during an authentication flow failure.
     *
     * @param request       Http servlet request.
     * @param response      Http servlet response.
     * @param context       Authentication Context.
     * @param status        Failure status.
     * @param statusMsg     Failure status message.
     * @throws IOException
     */
    public static void sendToRetryPage(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationContext context, String status,
                                       String statusMsg) throws IOException {

        try {
            URIBuilder uriBuilder = new URIBuilder(
                    ConfigurationFacade.getInstance().getAuthenticationEndpointRetryURL());
            if (status != null && statusMsg != null) {
                if (context != null) {
                    Map<String, String> failureData = new HashMap<>();
                    failureData.put(FrameworkConstants.STATUS_PARAM, status);
                    failureData.put(FrameworkConstants.STATUS_MSG_PARAM, statusMsg);
                    failureData.put(FrameworkConstants.REQUEST_PARAM_SP, context.getServiceProviderName());
                    AuthenticationError authenticationError = new AuthenticationError(failureData);
                    String errorKey = UUID.randomUUID().toString();
                    FrameworkUtils.addAuthenticationErrorToCache(errorKey, authenticationError,
                            context.getTenantDomain());
                    uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_ERROR_KEY, errorKey);
                    uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_AUTH_FLOW_ID,
                            context.getContextIdentifier());
                } else {
                    uriBuilder.addParameter(FrameworkConstants.STATUS_PARAM, status);
                    uriBuilder.addParameter(FrameworkConstants.STATUS_MSG_PARAM, statusMsg);
                }
            }
            request.setAttribute(FrameworkConstants.RequestParams.FLOW_STATUS, AuthenticatorFlowStatus.INCOMPLETE);
            request.setAttribute(FrameworkConstants.IS_SENT_TO_RETRY, true);
            if (status != null) {
                request.setAttribute(FrameworkConstants.REQ_ATTR_RETRY_STATUS, status);
            }
            if (context != null) {
                if (IdentityTenantUtil.isTenantedSessionsEnabled()) {
                    uriBuilder.addParameter(USER_TENANT_DOMAIN_HINT, context.getUserTenantDomain());
                }
                uriBuilder.addParameter(REQUEST_PARAM_SP, context.getServiceProviderName());
                if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled()) {
                    uriBuilder.addParameter(TENANT_DOMAIN, context.getTenantDomain());
                }
                String authFlowId = context.getContextIdentifier();
                uriBuilder.addParameter(FrameworkConstants.REQUEST_PARAM_AUTH_FLOW_ID, authFlowId);
                response.sendRedirect(uriBuilder.build().toString());
            } else {
                response.sendRedirect(getRedirectURL(uriBuilder.build().toString(), request));
            }
        } catch (URISyntaxException e) {
            log.error("Error building redirect url for failure", e);
            FrameworkUtils.sendToRetryPage(request, response);
        } finally {
            List<String> cookiesToInvalidateConfig = IdentityUtil.getCookiesToInvalidateConfigurationHolder();
            if (ArrayUtils.isNotEmpty(request.getCookies())) {
                Arrays.stream(request.getCookies())
                        .filter(cookie -> cookiesToInvalidateConfig.stream()
                                .anyMatch(cookieToInvalidate -> cookie.getName().contains(cookieToInvalidate)))
                        .forEach(cookie -> removeCookie(request, response, cookie.getName()));
            }
        }
    }

    /**
     * This method is used to append sp name and sp tenant domain as parameter to a given url. Those information will
     * be fetched from request parameters or referer.
     *
     * @param redirectURL Redirect URL.
     * @param request     HttpServlet Request.
     * @return sp information appended redirect URL.
     */
    public static String getRedirectURL(String redirectURL, HttpServletRequest request) {

        String spName = (String) request.getAttribute(REQUEST_PARAM_SP);
        String tenantDomain = (String) request.getAttribute(TENANT_DOMAIN);
        if (StringUtils.isBlank(spName)) {
            spName = getServiceProviderNameByReferer(request);
        }

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = getTenantDomainByReferer(request);
        }

        try {
            if (StringUtils.isNotBlank(spName)) {
                redirectURL = appendUri(redirectURL, REQUEST_PARAM_SP, spName);
            }

            if (StringUtils.isNotBlank(MDC.get(CORRELATION_ID_MDC))) {
                redirectURL = appendUri(redirectURL, CORRELATION_ID, MDC.get(CORRELATION_ID_MDC));
            }

            if (!IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && StringUtils.isNotBlank(tenantDomain)) {
                redirectURL = appendUri(redirectURL, TENANT_DOMAIN, tenantDomain);
            }
        } catch (UnsupportedEncodingException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while encoding parameters: " + tenantDomain + " and/or " + spName, e);
            }
            return redirectURL;
        }

        return redirectURL;
    }

    /**
     * This method is used to get the application name from the authentication context.
     * @param context Authentication context.
     * @return Application name.
     */
    public static Optional<String> getApplicationName(AuthenticationContext context) {

        // Get the application name from the context directly if it's not null.
        Optional<String> serviceProviderName = Optional.ofNullable(context)
                .map(AuthenticationContext::getServiceProviderName);
        if (serviceProviderName.isPresent()) {
            return serviceProviderName;
        }

        // Get the application name from the sequence config if it's not available in the
        // context.getServiceProviderName().
        return Optional.ofNullable(context)
                .map(AuthenticationContext::getSequenceConfig)
                .map(SequenceConfig::getApplicationConfig)
                .map(ApplicationConfig::getApplicationName);
    }

    /**
     * This method is used to get the application resource id from the authentication context.
     * @param context Authentication context.
     * @return Application resource id.
     */
    public static Optional<String> getApplicationResourceId(AuthenticationContext context) {

        // Get the application resource id from the optimized application config if it's available.
        Optional<String> optimizedResourceId = Optional.ofNullable(context)
                .map(AuthenticationContext::getSequenceConfig)
                .map(SequenceConfig::getOptimizedApplicationConfig)
                .map(OptimizedApplicationConfig::getServiceProviderResourceId);
        if (optimizedResourceId.isPresent()) {
            return optimizedResourceId;
        }
        // Get the application resource id from the sequence config if it's not available in the optimized
        // application config
        return Optional.ofNullable(context)
                .map(AuthenticationContext::getSequenceConfig)
                .map(SequenceConfig::getApplicationConfig)
                .map(ApplicationConfig::getServiceProvider)
                .map(ServiceProvider::getApplicationResourceId);
    }

    /**
     * Retrieve the user id claim configured for the federated IDP.
     *
     * @param federatedIdpName  Federated IDP name.
     * @param tenantDomain      Tenant domain.
     * @return  User ID claim configured for the IDP.
     * @throws PostAuthenticationFailedException PostAuthenticationFailedException.
     */
    public static String getUserIdClaimURI(String federatedIdpName, String tenantDomain)
            throws PostAuthenticationFailedException {

        String userIdClaimURI;
        IdentityProvider idp;
        try {
            idp = FrameworkServiceDataHolder.getInstance().getIdentityProviderManager()
                    .getIdPByName(federatedIdpName, tenantDomain);
        } catch (IdentityProviderManagementException e) {
            throw new PostAuthenticationFailedException(
                    ERROR_WHILE_GETTING_IDP_BY_NAME.getCode(),
                    String.format(FrameworkErrorConstants.ErrorMessages.ERROR_WHILE_GETTING_IDP_BY_NAME.getMessage(),
                            tenantDomain), e);
        }
        if (idp == null) {
            return null;
        }
        ClaimConfig claimConfigs = idp.getClaimConfig();
        if (claimConfigs == null) {
            return null;
        }
        ClaimMapping[] claimMappings = claimConfigs.getClaimMappings();
        if (claimMappings == null || claimMappings.length < 1) {
            return null;
        }
        userIdClaimURI = claimConfigs.getUserClaimURI();
        if (userIdClaimURI != null) {
            return userIdClaimURI;
        }
        ClaimMapping userNameClaimMapping = Arrays.stream(claimMappings).filter(claimMapping ->
                        StringUtils.equals(USERNAME_CLAIM, claimMapping.getLocalClaim().getClaimUri()))
                .findFirst()
                .orElse(null);
        if (userNameClaimMapping != null) {
            userIdClaimURI = userNameClaimMapping.getRemoteClaim().getClaimUri();
        }
        return userIdClaimURI;
    }

    /**
     * Get the external subject from the step config.
     *
     * @param stepConfig    Step config.
     * @param tenantDomain  Tenant domain.
     * @return External subject.
     * @throws PostAuthenticationFailedException PostAuthenticationFailedException.
     */
    public static String getExternalSubject(StepConfig stepConfig, String tenantDomain)
            throws PostAuthenticationFailedException {

        String externalSubject = null;
        String userIdClaimURI = getUserIdClaimURI(stepConfig.getAuthenticatedIdP(), tenantDomain);
        if (StringUtils.isNotEmpty(userIdClaimURI)) {
            externalSubject = stepConfig.getAuthenticatedUser().getUserAttributes().entrySet().stream()
                    .filter(userAttribute -> userAttribute.getKey().getRemoteClaim().getClaimUri()
                            .equals(userIdClaimURI))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return externalSubject;
    }

    /**
     * Get the configuration whether the external subject attribute based on IdP configurations..
     *
     * @return true if the IdP configurations has to be honoured.
     */
    public static boolean isConfiguredIdpSubForFederatedUserAssociationEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(ENABLE_CONFIGURED_IDP_SUB_FOR_FEDERATED_USER_ASSOCIATION));
    }

    private static String getServiceProviderNameByReferer(HttpServletRequest request) {

        String serviceProviderName = null;
        String refererHeader = request.getHeader("referer");
        if (StringUtils.isNotBlank(refererHeader)) {
            String[] queryParams = refererHeader.split(QUERY_SEPARATOR);
            for (String queryParam : queryParams) {
                if (queryParam.contains(REQUEST_PARAM_SP + EQUAL) || queryParam.contains(REQUEST_PARAM_APPLICATION +
                        EQUAL)) {
                    serviceProviderName = queryParam.substring(queryParam.lastIndexOf(EQUAL) + 1);
                    break;
                }
            }
        }

        return serviceProviderName;
    }

    private static String getTenantDomainByReferer(HttpServletRequest request) {

        String tenantDomain = null;
        String refererHeader = request.getHeader("referer");
        if (StringUtils.isNotBlank(refererHeader)) {
            String[] queryParams = refererHeader.split(QUERY_SEPARATOR);
            for (String queryParam : queryParams) {
                if (queryParam.contains(TENANT_DOMAIN + EQUAL)) {
                    tenantDomain = queryParam.substring(queryParam.lastIndexOf(EQUAL) + 1);
                    break;
                }
            }
        }
        return tenantDomain;
    }

    private static String appendUri(String uri, String key, String value) throws UnsupportedEncodingException {

        if (StringUtils.isNotBlank(uri) && StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {

            if (uri.contains("?") || uri.contains("#")) {
                uri += "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
            } else {
                uri += "?" + key + "=" + URLEncoder.encode(value, "UTF-8");
            }
        }
        return uri;
    }

    /**
     * Removes commonAuthCookie.
     *
     * @param req  Incoming HttpServletRequest.
     * @param resp HttpServlet response which the cookie must be written.
     */
    public static void removeAuthCookie(HttpServletRequest req, HttpServletResponse resp) {

        removeCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE, SameSiteCookie.NONE);
    }

    public static boolean isOrganizationQualifiedRequest() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getOrganizationId() != null;
    }
    /**
     * Remove the auth cookie in the tenanted path.
     *
     * @param req    HTTP request
     * @param resp   HTTP response
     * @param tenantDomain Tenant domain
     */
    public static void removeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String tenantDomain) {

        String path;
        if (isOrganizationQualifiedRequest()) {
            path = FrameworkConstants.ORGANIZATION_CONTEXT_PREFIX + tenantDomain + "/";
        } else {
            if (!IdentityTenantUtil.isSuperTenantRequiredInUrl() &&
                    MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                path = "/";
            } else {
                path = FrameworkConstants.TENANT_CONTEXT_PREFIX + tenantDomain + "/";
            }
        }
        removeCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE, SameSiteCookie.NONE, path);
    }

    /**
     * Removes a cookie which is already stored.
     *
     * @param req        Incoming HttpServletRequest.
     * @param resp       HttpServletResponse which should be stored.
     * @param cookieName Name of the cookie which should be removed.
     */
    public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName) {

        Cookie[] cookies = req.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {

                    CookieBuilder cookieBuilder = new CookieBuilder(cookieName,
                            cookie.getValue());
                    IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig
                            (cookieName);

                    if (cookieConfig != null) {
                        updateCookieConfig(cookieBuilder, cookieConfig, 0, ROOT_DOMAIN);
                    } else {
                        cookieBuilder.setHttpOnly(true);
                        cookieBuilder.setSecure(true);
                        cookieBuilder.setPath(ROOT_DOMAIN);
                    }

                    cookieBuilder.setMaxAge(0);
                    resp.addCookie(cookieBuilder.build());
                    break;
                }
            }
        }
    }

    /**
     * Removes a cookie which is already stored.
     *
     * @param req            Incoming HttpServletRequest.
     * @param resp           HttpServletResponse which should be stored.
     * @param cookieName     Name of the cookie which should be removed.
     * @param sameSiteCookie SameSite attribute value for the cookie.
     */
    public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName,
                                    SameSiteCookie sameSiteCookie) {

        removeCookie(req, resp, cookieName, sameSiteCookie, ROOT_DOMAIN);
    }


    public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName,
                                    SameSiteCookie sameSiteCookie, String path) {

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    CookieBuilder cookieBuilder = new CookieBuilder(cookieName,
                            cookie.getValue());
                    IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig(cookieName);
                    if (cookieConfig != null) {
                        updateCookieConfig(cookieBuilder, cookieConfig, 0, path);
                    } else {
                        cookieBuilder.setHttpOnly(true);
                        cookieBuilder.setSecure(true);
                        cookieBuilder.setPath(StringUtils.isNotBlank(path) ? path : ROOT_DOMAIN);
                        cookieBuilder.setSameSite(sameSiteCookie);
                    }
                    cookieBuilder.setMaxAge(0);
                    resp.addCookie(cookieBuilder.build());
                    break;
                }
            }
        }
    }

    /**
     * @param req
     * @param resp
     * @param id
     */
    @Deprecated
    public static void storeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String id) {
        storeAuthCookie(req, resp, id, null, ROOT_DOMAIN);
    }

    /**
     * @param req
     * @param resp
     * @param id
     * @param age
     */
    public static void storeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String id, Integer age) {

        setCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE, id, age, SameSiteCookie.NONE);
    }

    public static void storeAuthCookie(HttpServletRequest req, HttpServletResponse resp, String id, Integer age,
                                       String path) {

        setCookie(req, resp, FrameworkConstants.COMMONAUTH_COOKIE, id, age, SameSiteCookie.NONE, path);
    }

    /**
     * Stores a cookie to the response taking configurations from identity.xml file.
     *
     * @param req        Incoming HttpSerletRequest.
     * @param resp       Outgoing HttpServletResponse.
     * @param cookieName Name of the cookie to be stored.
     * @param id         Cookie id.
     * @param age        Max age of the cookie.
     */
    public static void setCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName, String id,
                                 Integer age) {

        CookieBuilder cookieBuilder = new CookieBuilder(cookieName, id);

        IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig(cookieName);

        if (cookieConfig != null) {

            updateCookieConfig(cookieBuilder, cookieConfig, age, null);
        } else {

            cookieBuilder.setSecure(true);
            cookieBuilder.setHttpOnly(true);
            cookieBuilder.setPath(ROOT_DOMAIN);

            if (age != null) {
                cookieBuilder.setMaxAge(age);
            }
        }

        resp.addCookie(cookieBuilder.build());
    }

    /**
     * Stores a cookie to the response taking configurations from identity.xml file.
     *
     * @param req         Incoming HttpServletRequest.
     * @param resp        Outgoing HttpServletResponse.
     * @param cookieName  Name of the cookie to be stored.
     * @param id          Cookie id.
     * @param age         Max age of the cookie.
     * @param setSameSite SameSite attribute value for the cookie.
     */
    public static void setCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName, String id,
                                 Integer age, SameSiteCookie setSameSite) {

        setCookie(req, resp, cookieName, id, age, setSameSite, null);
    }

    public static void setCookie(HttpServletRequest req, HttpServletResponse resp, String cookieName, String id,
                                 Integer age, SameSiteCookie setSameSite, String path) {

        CookieBuilder cookieBuilder = new CookieBuilder(cookieName, id);
        IdentityCookieConfig cookieConfig = IdentityUtil.getIdentityCookieConfig(cookieName);
        if (cookieConfig != null) {
            updateCookieConfig(cookieBuilder, cookieConfig, age, path);
        } else {
            cookieBuilder.setSecure(true);
            cookieBuilder.setHttpOnly(true);
            cookieBuilder.setPath(StringUtils.isNotBlank(path) ? path : ROOT_DOMAIN);
            cookieBuilder.setSameSite(setSameSite);
            if (age != null) {
                cookieBuilder.setMaxAge(age);
            }
        }
        resp.addCookie(cookieBuilder.build());
    }

    /**
     * @param req Incoming HttpServletRequest.
     * @return CommonAuthID cookie.
     */
    public static Cookie getAuthCookie(HttpServletRequest req) {

        return getCookie(req, FrameworkConstants.COMMONAUTH_COOKIE);
    }

    /**
     * Returns the cookie with the given name.
     * @param req Incoming HttpServletRequest.
     * @param cookieName Name of the cookie.
     * @return Cookie with the given name. If it's not present null will be returned.
     */
    public static Cookie getCookie(HttpServletRequest req, String cookieName) {

        Cookie[] cookies = req.getCookies();

        if (cookies != null) {

            for (Cookie cookie : cookies) {

                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }

        return null;
    }

    /**
     * @param contextId
     * @param context
     */
    public static void addAuthenticationContextToCache(String contextId, AuthenticationContext context) {

        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
        AuthenticationContextCacheEntry cacheEntry = new AuthenticationContextCacheEntry(context);
        cacheEntry.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getTempDataCleanUpTimeout()));
        AuthenticationContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * @param key
     * @param authenticationResult
     */
    public static void addAuthenticationResultToCache(String key, AuthenticationResult authenticationResult) {

        AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(key);
        AuthenticationResultCacheEntry cacheEntry = new AuthenticationResultCacheEntry();
        cacheEntry.setResult(authenticationResult);
        cacheEntry.setValidityPeriod(TimeUnit.MINUTES.toNanos(IdentityUtil.getOperationCleanUpTimeout()));
        AuthenticationResultCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * To get authentication cache result from cache.
     * @param key
     * @return
     */
    public static AuthenticationResultCacheEntry getAuthenticationResultFromCache(String key) {
        AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(key);
        AuthenticationResultCacheEntry authResult = AuthenticationResultCache.getInstance().getValueFromCache(cacheKey);
        return authResult;
    }

    /**
     *  Removes authentication result from cache.
     * @param autheticationResultId
     */
    public static void removeAuthenticationResultFromCache(String autheticationResultId) {
        if (autheticationResultId != null) {
            AuthenticationResultCacheKey cacheKey = new AuthenticationResultCacheKey(autheticationResultId);
            AuthenticationResultCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * @deprecated Use the {@link #addSessionContextToCache(String, SessionContext, String)}
     *
     * @param key
     * @param sessionContext
     */
    @Deprecated
    public static void addSessionContextToCache(String key, SessionContext sessionContext) {

        SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
        SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();

        Map<String, SequenceConfig> seqData = sessionContext.getAuthenticatedSequences();
        if (seqData != null) {
            for (Entry<String, SequenceConfig> entry : seqData.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getAuthenticatedUser().setUserAttributes(null);
                    entry.getValue().setAuthenticationGraph(null);
                }
            }
        }
        Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (authenticatedUserObj instanceof AuthenticatedUser) {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            cacheEntry.setLoggedInUser(authenticatedUser.getAuthenticatedSubjectIdentifier());
        }
        cacheEntry.setContext(sessionContext);
        SessionContextCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    @Deprecated
    public static void addSessionContextToCache(String key, SessionContext sessionContext, String tenantDomain) {

        addSessionContextToCache(key, sessionContext, tenantDomain, tenantDomain);
    }

    public static void addSessionContextToCache(String key, SessionContext sessionContext, String tenantDomain,
                                                String loginTenantDomain) {

        SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
        SessionContextCacheEntry cacheEntry = new SessionContextCacheEntry();
        cacheEntry.setContextIdentifier(key);

        Map<String, SequenceConfig> seqData = sessionContext.getAuthenticatedSequences();
        if (seqData != null) {
            for (Entry<String, SequenceConfig> entry : seqData.entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().getAuthenticatedUser().setUserAttributes(null);

                    // AuthenticationGraph in the SequenceConfig is used during the authentication flow and is not
                    // needed after the whole authentication flow is completed. Hense removed from the SessionContext.
                    entry.getValue().setAuthenticationGraph(null);
                }
            }
        }
        Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
        if (authenticatedUserObj instanceof AuthenticatedUser) {
            AuthenticatedUser authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            cacheEntry.setLoggedInUser(authenticatedUser.getAuthenticatedSubjectIdentifier());
        }

        long timeoutPeriod;
        if (sessionContext.isRememberMe()) {
            timeoutPeriod = TimeUnit.SECONDS.toNanos(
                    IdPManagementUtil.getRememberMeTimeout(tenantDomain));
        } else {
            timeoutPeriod = TimeUnit.SECONDS.toNanos(
                    IdPManagementUtil.getIdleSessionTimeOut(tenantDomain));
        }

        cacheEntry.setContext(sessionContext);
        cacheEntry.setValidityPeriod(timeoutPeriod);
        SessionContextCache.getInstance().addToCache(cacheKey, cacheEntry, loginTenantDomain);
    }

    /**
     * @param key
     * @return
     *
     * @deprecated to use {{@link #getSessionContextFromCache(String, String)}} to support maintaining cache in
     * tenant space.
     */
    @Deprecated
    public static SessionContext getSessionContextFromCache(String key) {

        return getSessionContextFromCache(key, getLoginTenantDomainFromContext());
    }

    /**
     * @param key
     * @param loginTenantDomain
     * @return
     */
    public static SessionContext getSessionContextFromCache(String key, String loginTenantDomain) {

        SessionContext sessionContext = null;
        if (StringUtils.isNotBlank(key)) {
            SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
            Object cacheEntryObj = SessionContextCache.getInstance().getValueFromCache(cacheKey, loginTenantDomain);

            if (cacheEntryObj != null) {
                sessionContext = ((SessionContextCacheEntry) cacheEntryObj).getContext();
            }
        }
        return sessionContext;
    }

    /**
     * Retrieve session context from the session cache.
     *
     * @param request           HttpServletRequest.
     * @param context           Authentication context.
     * @param sessionContextKey Session context key.
     * @return Session context key.
     * @throws FrameworkException Error in triggering session expire event.
     */
    public static SessionContext getSessionContextFromCache(HttpServletRequest request, AuthenticationContext context
            , String sessionContextKey) throws FrameworkException {

        SessionContext sessionContext = null;
        if (StringUtils.isNotBlank(sessionContextKey)) {
            SessionContextCacheKey cacheKey = new SessionContextCacheKey(sessionContextKey);
            SessionContextCache sessionContextCache = SessionContextCache.getInstance();
            SessionContextCacheEntry cacheEntry = sessionContextCache.getSessionContextCacheEntry(cacheKey,
                    context.getLoginTenantDomain());

            if (cacheEntry != null) {
                sessionContext = cacheEntry.getContext();
                boolean isSessionExpired = sessionContextCache.isSessionExpired(cacheKey, cacheEntry);
                if (isSessionExpired) {
                    triggerSessionExpireEvent(request, context, sessionContext);
                    if (log.isDebugEnabled()) {
                        log.debug("A SESSION_EXPIRE event was fired for the expired session found corresponding " +
                                "to the key: " + cacheKey.getContextId());
                    }
                    return null;
                }
            }
        }
        return sessionContext;
    }

    /**
     * To add authentication error to cache.
     *
     * @param key   Error key.
     */
    public static void addAuthenticationErrorToCache(String key, AuthenticationError authenticationError,
                                                     String tenantDomain) {

        AuthenticationErrorCacheKey cacheKey = new AuthenticationErrorCacheKey(key);
        AuthenticationErrorCacheEntry cacheEntry = new AuthenticationErrorCacheEntry(authenticationError, tenantDomain);
        AuthenticationErrorCache.getInstance().addToCache(cacheKey, cacheEntry);
    }

    /**
     * To get authentication error from cache.
     *
     * @param key   Error key.
     * @return      AuthenticationError.
     */
    public static AuthenticationError getAuthenticationErrorFromCache(String key) {

        AuthenticationErrorCacheKey cacheKey = new AuthenticationErrorCacheKey(key);
        AuthenticationErrorCacheEntry authResult = AuthenticationErrorCache.getInstance().getValueFromCache(cacheKey);
        if (authResult != null) {
            return authResult.getAuthenticationError();
        }
        return null;
    }

    /**
     * To remove authentication error from cache.
     *
     * @param key   Error key.
     */
    public static void removeAuthenticationErrorFromCache(String key) {

        if (StringUtils.isNotEmpty(key)) {
            AuthenticationErrorCacheKey cacheKey = new AuthenticationErrorCacheKey(key);
            AuthenticationErrorCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * Trigger SESSION_EXPIRE event on session expiry due to a session idle timeout or a remember me session time out.
     *
     * @param request        HttpServletRequest.
     * @param context        Authentication context.
     * @param sessionContext Session context.
     * @throws FrameworkException Error in triggering the session expiry event.
     */
    private static void triggerSessionExpireEvent(HttpServletRequest request, AuthenticationContext context,
                                                  SessionContext sessionContext) throws FrameworkException {

        AuthenticatedUser authenticatedUser = new AuthenticatedUser();
        if (sessionContext != null) {
            Object authenticatedUserObj = sessionContext.getProperty(FrameworkConstants.AUTHENTICATED_USER);
            if (authenticatedUserObj instanceof AuthenticatedUser) {
                authenticatedUser = (AuthenticatedUser) authenticatedUserObj;
            }
            context.setSubject(authenticatedUser);

            IdentityEventService eventService = FrameworkServiceDataHolder.getInstance().getIdentityEventService();
            try {
                Map<String, Object> eventProperties = new HashMap<>();
                eventProperties.put(IdentityEventConstants.EventProperty.REQUEST, request);
                eventProperties.put(IdentityEventConstants.EventProperty.CONTEXT, context);
                eventProperties.put(IdentityEventConstants.EventProperty.SESSION_CONTEXT, sessionContext);
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, authenticatedUser);
                paramMap.put(FrameworkConstants.AnalyticsAttributes.SESSION_ID, context.getSessionIdentifier());
                Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
                eventProperties.put(IdentityEventConstants.EventProperty.PARAMS, unmodifiableParamMap);

                Event event = new Event(IdentityEventConstants.EventName.SESSION_EXPIRE.name(), eventProperties);
                eventService.handleEvent(event);
            } catch (IdentityEventException e) {
                throw new FrameworkException("Error in triggering session expire event for the session: " +
                        context.getSessionIdentifier() + " of user: " + authenticatedUser.toFullQualifiedUsername(), e);
            }
        }
    }

    /**
     * @param key
     * @deprecated to use {{@link #removeSessionContextFromCache(String, String)}} to support maintaining cache in
     * tenant space.
     */
    @Deprecated
    public static void removeSessionContextFromCache(String key) {

        removeSessionContextFromCache(key, getLoginTenantDomainFromContext());
    }

    /**
     * @param key
     * @param loginTenantDomain
     */
    public static void removeSessionContextFromCache(String key, String loginTenantDomain) {

        if (key != null) {
            SessionContextCacheKey cacheKey = new SessionContextCacheKey(key);
            SessionContextCache.getInstance().clearCacheEntry(cacheKey, loginTenantDomain);
        }
    }

    /**
     * Get the tenant domain from the context if tenanted session is enabled, else return carbon.super.
     *
     * @return tenant domain
     */
    public static String getLoginTenantDomainFromContext() {

        // We use the tenant domain set in context only in tenanted session is enabled.
        if (IdentityTenantUtil.isTenantedSessionsEnabled()) {
            String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();
            if (StringUtils.isNotBlank(tenantDomain)) {
                return tenantDomain;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("TenantedSessionsEnabled is enabled, but the tenant domain is not set to the" +
                            " context. Hence using the tenant domain from the carbon context.");
                }
                return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            }
        }
        return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    }

    /**
     * @param contextId
     */
    public static void removeAuthenticationContextFromCache(String contextId) {

        if (contextId != null) {
            AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
            AuthenticationContextCache.getInstance().clearCacheEntry(cacheKey);
        }
    }

    /**
     * @param contextId
     * @return
     */
    public static AuthenticationContext getAuthenticationContextFromCache(String contextId) {

        AuthenticationContext authenticationContext = null;
        AuthenticationContextCacheKey cacheKey = new AuthenticationContextCacheKey(contextId);
        AuthenticationContextCacheEntry authenticationContextCacheEntry = AuthenticationContextCache.getInstance().
                getValueFromCache(cacheKey);

        if (authenticationContextCacheEntry != null) {
            authenticationContext = authenticationContextCacheEntry.getContext();
        }

        if (log.isDebugEnabled() && authenticationContext == null) {
            log.debug("Authentication Context is null");
        }

        return authenticationContext;
    }

    /**
     * @param externalIdPConfig
     * @param name
     * @return
     */
    public static Map<String, String> getAuthenticatorPropertyMapFromIdP(
            ExternalIdPConfig externalIdPConfig, String name) {

        Map<String, String> propertyMap = new HashMap<String, String>();

        if (externalIdPConfig != null) {
            FederatedAuthenticatorConfig[] authenticatorConfigs = externalIdPConfig
                    .getIdentityProvider().getFederatedAuthenticatorConfigs();

            for (FederatedAuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (authenticatorConfig.getName().equals(name)) {

                    for (Property property : authenticatorConfig.getProperties()) {
                        propertyMap.put(property.getName(), property.getValue());
                    }
                    break;
                }
            }
        }

        return propertyMap;
    }

    /**
     * @param attributeValue
     * @return
     */
    public static Map<ClaimMapping, String> buildClaimMappings(Map<String, String> attributeValue) {

        Map<ClaimMapping, String> claimMap = new HashMap<ClaimMapping, String>();

        for (Iterator<Entry<String, String>> iterator = attributeValue.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<String, String> entry = iterator.next();
            if (entry.getValue() == null) {
                continue;
            }
            claimMap.put(ClaimMapping.build(entry.getKey(), entry.getKey(), null, false),
                         entry.getValue());
        }

        return claimMap;

    }

    /**
     * @param attributeValues
     * @return
     */
    public static Set<String> getKeySet(Map<ClaimMapping, String> attributeValues) {

        Set<String> claimList = new HashSet<String>();

        for (Iterator<Entry<ClaimMapping, String>> iterator = attributeValues.entrySet().iterator(); iterator
                .hasNext(); ) {
            Entry<ClaimMapping, String> entry = iterator.next();
            claimList.add(entry.getKey().getLocalClaim().getClaimUri());

        }

        return claimList;

    }

    /**
     * @param claimMappings
     * @return
     */
    public static Map<String, String> getClaimMappings(ClaimMapping[] claimMappings,
                                                       boolean useLocalDialectAsKey) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        for (ClaimMapping claimMapping : claimMappings) {
            if (useLocalDialectAsKey) {
                remoteToLocalClaimMap.put(claimMapping.getLocalClaim().getClaimUri(), claimMapping
                        .getRemoteClaim().getClaimUri());
            } else {
                remoteToLocalClaimMap.put(claimMapping.getRemoteClaim().getClaimUri(), claimMapping
                        .getLocalClaim().getClaimUri());
            }
        }
        return remoteToLocalClaimMap;
    }

    /**
     * @param claimMappings
     * @param useLocalDialectAsKey
     * @return
     */
    public static Map<String, String> getClaimMappings(Map<ClaimMapping, String> claimMappings,
                                                       boolean useLocalDialectAsKey) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        if (claimMappings != null) {
            for (Entry<ClaimMapping, String> entry : claimMappings.entrySet()) {
                ClaimMapping claimMapping = entry.getKey();
                if (useLocalDialectAsKey) {
                    remoteToLocalClaimMap.put(claimMapping.getLocalClaim().getClaimUri(), entry.getValue());
                } else {
                    remoteToLocalClaimMap.put(claimMapping.getRemoteClaim().getClaimUri(), entry.getValue());
                }
            }
        }
        return remoteToLocalClaimMap;
    }

    /**
     * @param claimMappings
     * @return
     */
    public static Map<String, String> getLocalToSPClaimMappings(Map<String, String> claimMappings) {

        Map<String, String> remoteToLocalClaimMap = new HashMap<String, String>();

        for (Entry<String, String> entry : claimMappings.entrySet()) {
            remoteToLocalClaimMap.put(entry.getValue(), entry.getKey());
        }
        return remoteToLocalClaimMap;
    }

    public static String getQueryStringWithFrameworkContextId(String originalQueryStr,
                                                              String callerContextId, String frameworkContextId) {

        String queryParams = originalQueryStr;

        /*
         * Upto now, query-string contained a 'sessionDataKey' of the calling servlet. At here we
         * replace it with the framework context id.
         */
        queryParams = queryParams.replace(callerContextId, frameworkContextId);

        return queryParams;
    }

    public static List<String> getStepIdPs(StepConfig stepConfig) {

        List<String> stepIdps = new ArrayList<String>();
        List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();

        for (AuthenticatorConfig authenticatorConfig : authenticatorConfigs) {
            List<String> authenticatorIdps = authenticatorConfig.getIdpNames();

            for (String authenticatorIdp : authenticatorIdps) {
                stepIdps.add(authenticatorIdp);
            }
        }

        return stepIdps;
    }

    public static List<String> getAuthenticatedStepIdPs(List<String> stepIdPs,
                                                        List<String> authenticatedIdPs) {

        List<String> idps = new ArrayList<String>();

        if (stepIdPs != null && authenticatedIdPs != null) {
            for (String stepIdP : stepIdPs) {
                if (authenticatedIdPs.contains(stepIdP)) {
                    idps.add(stepIdP);
                    break;
                }
            }
        }

        return idps;
    }

    public static Map<String, AuthenticatorConfig> getAuthenticatedStepIdPs(StepConfig stepConfig,
                                                                            Map<String, AuthenticatedIdPData>
                                                                                    authenticatedIdPs) {

        if (log.isDebugEnabled()) {
            log.debug(String.format("Finding already authenticated IdPs of the step {order:%d}",
                    stepConfig.getOrder()));
        }

        Map<String, AuthenticatorConfig> idpAuthenticatorMap = new HashMap<>();
        List<AuthenticatorConfig> authenticatorConfigs = stepConfig.getAuthenticatorList();

        if (authenticatedIdPs != null && !authenticatedIdPs.isEmpty()) {

            for (AuthenticatorConfig authenticatorConfig : authenticatorConfigs) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("Considering the authenticator '%s'", authenticatorConfig.getName()));
                }

                String authenticatorName = authenticatorConfig.getName();
                List<String> authenticatorIdps = authenticatorConfig.getIdpNames();
                if (log.isDebugEnabled()) {
                    log.debug(String.format("%d IdP(s) found in the step.", authenticatedIdPs.size()));
                }

                for (String authenticatorIdp : authenticatorIdps) {

                    if (log.isDebugEnabled()) {
                        log.debug(String.format("Considering the IDP : '%s'", authenticatorIdp));
                    }

                    AuthenticatedIdPData authenticatedIdPData = authenticatedIdPs.get(authenticatorIdp);

                    if (authenticatedIdPData != null && authenticatedIdPData.getIdpName() !=  null &&
                            authenticatedIdPData.getIdpName().equals(authenticatorIdp)) {

                        if (FrameworkConstants.LOCAL.equals(authenticatedIdPData.getIdpName())) {
                            if (authenticatorConfig.getApplicationAuthenticator() != null &&
                                    authenticatedIdPData.isAlreadyAuthenticatedUsing(authenticatorName,
                                            authenticatorConfig.getApplicationAuthenticator().getAuthMechanism())) {
                                idpAuthenticatorMap.put(authenticatorIdp, authenticatorConfig);

                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("('%s', '%s') is an already authenticated " +
                                            "IDP - authenticator combination.",
                                            authenticatorConfig.getName(), authenticatorIdp));
                                }

                                break;
                            } else {
                                if (log.isDebugEnabled()) {
                                    log.debug(String.format("('%s', '%s') is not an already authenticated " +
                                            "IDP - authenticator combination.",
                                            authenticatorConfig.getName(), authenticatorIdp));
                                }
                            }
                        } else {

                            if (log.isDebugEnabled()) {
                                log.debug(String.format("'%s' is an already authenticated IDP.", authenticatorIdp));
                            }

                            idpAuthenticatorMap.put(authenticatorIdp, authenticatorConfig);
                            break;
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("'%s' is NOT an already authenticated IDP.", authenticatorIdp));
                        }
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("No authenticators found.");
            }
        }

        return idpAuthenticatorMap;
    }

    public static String getAuthenticatorIdPMappingString(List<AuthenticatorConfig> authConfigList) {

        StringBuilder authenticatorIdPStr = new StringBuilder("");

        for (AuthenticatorConfig authConfig : authConfigList) {
            StringBuilder idpsOfAuthenticatorStr = new StringBuilder("");

            for (String idpName : authConfig.getIdpNames()) {

                if (idpName != null) {

                    if (idpsOfAuthenticatorStr.length() != 0) {
                        idpsOfAuthenticatorStr.append(":");
                    }

                    IdentityProvider idp = authConfig.getIdps().get(idpName);

                    if (idp != null && idp.isFederationHub()) {
                        idpName += ".hub";
                    }

                    idpsOfAuthenticatorStr.append(idpName);
                }
            }

            if (authenticatorIdPStr.length() != 0) {
                authenticatorIdPStr.append(";");
            }

            authenticatorIdPStr.append(authConfig.getName()).append(":")
                    .append(idpsOfAuthenticatorStr);
        }

        return authenticatorIdPStr.toString();
    }

    /**
     * when getting query params through this, only configured params will be appended as query params.
     * The required params can be configured from application-authenticators.xml
     *
     * @param request
     * @return
     */
    public static String getQueryStringWithConfiguredParams(HttpServletRequest request) {

        boolean configAvailable = FileBasedConfigurationBuilder.getInstance()
                .isAuthEndpointQueryParamsConfigAvailable();
        List<String> queryParams = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointQueryParams();
        String action = FileBasedConfigurationBuilder.getInstance()
                .getAuthEndpointQueryParamsAction();

        StringBuilder queryStrBuilder = new StringBuilder("");
        Map<String, String[]> reqParamMap = request.getParameterMap();

        if (configAvailable) {
            if (action != null
                && action.equals(FrameworkConstants.AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE)) {
                if (reqParamMap != null) {
                    for (Map.Entry<String, String[]> entry : reqParamMap.entrySet()) {
                        String paramName = entry.getKey();
                        String paramValue = entry.getValue()[0];

                        //skip issuer and type and sessionDataKey parameters
                        if (SESSION_DATA_KEY.equals(paramName) || FrameworkConstants.RequestParams.ISSUER.equals
                                (paramName) || FrameworkConstants.RequestParams.TYPE.equals(paramName)) {
                            continue;
                        }

                        // Skip tenant domain if, 'isTenantQualifiedUrlsEnabled' is enabled in identity.xml
                        if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && TENANT_DOMAIN.equals(paramName)) {
                            continue;
                        }

                        if (!queryParams.contains(paramName)) {
                            if (queryStrBuilder.length() > 0) {
                                queryStrBuilder.append('&');
                            }

                            try {
                                queryStrBuilder.append(URLEncoder.encode(paramName, UTF_8)).append('=')
                                        .append(URLEncoder.encode(paramValue, UTF_8));
                            } catch (UnsupportedEncodingException e) {
                                log.error(
                                        "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                        e);
                            }
                        }
                    }
                }
            } else {
                for (String param : queryParams) {
                    String paramValue = request.getParameter(param);

                    if (paramValue != null) {
                        if (queryStrBuilder.length() > 0) {
                            queryStrBuilder.append('&');
                        }
                        try {
                            queryStrBuilder.append(URLEncoder.encode(param, UTF_8)).append('=')
                                    .append(URLEncoder.encode(paramValue, UTF_8));
                        } catch (UnsupportedEncodingException e) {
                            log.error(
                                    "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                    e);
                        }
                    }
                }
            }
        } else {
            if (reqParamMap != null) {
                for (Map.Entry<String, String[]> entry : reqParamMap.entrySet()) {
                    String paramName = entry.getKey();
                    String paramValue = entry.getValue()[0];

                    //skip issuer and type and sessionDataKey parameters
                    if (SESSION_DATA_KEY.equals(paramName) || FrameworkConstants.RequestParams.ISSUER.equals
                            (paramName) || FrameworkConstants.RequestParams.TYPE.equals(paramName)) {
                        continue;
                    }

                    // Skip tenant domain if, 'isTenantQualifiedUrlsEnabled' is enabled in identity.xml
                    if (IdentityTenantUtil.isTenantQualifiedUrlsEnabled() && TENANT_DOMAIN.equals(paramName)) {
                        continue;
                    }

                    if (queryStrBuilder.length() > 0) {
                        queryStrBuilder.append('&');
                    }

                    try {
                        queryStrBuilder.append(URLEncoder.encode(paramName, UTF_8)).append('=')
                                .append(URLEncoder.encode(paramValue, UTF_8));
                    } catch (UnsupportedEncodingException e) {
                        log.error(
                                "Error while URL Encoding query param to be sent to the AuthenticationEndpoint",
                                e);
                    }
                }
            }
        }

        return queryStrBuilder.toString();
    }

    public static String getRedirectURLWithFilteredParams(String redirectUrl, AuthenticationContext context) {

        return getRedirectURLWithFilteredParams(redirectUrl, context.getEndpointParams());
    }

    public static String getRedirectURLWithFilteredParams(String redirectUrl, Map<String, Serializable> dataStoreMap) {

        boolean configAvailable = FileBasedConfigurationBuilder.getInstance()
                .isAuthEndpointRedirectParamsConfigAvailable();

        List<String> queryParams;
        String action;
        if (!configAvailable) {
            queryParams = Arrays.asList("loggedInUser", "ske");
            action = "exclude";
        } else {
            queryParams = FileBasedConfigurationBuilder.getInstance()
                    .getAuthEndpointRedirectParams();
            action = FileBasedConfigurationBuilder.getInstance()
                    .getAuthEndpointRedirectParamsAction();
        }

        URIBuilder uriBuilder;

        // Check if the URL is a fragment URL. Only the path of the URL is considered here.
        boolean isAFragmentURL =
                redirectUrl != null && redirectUrl.contains(HASH_CHAR) && redirectUrl.contains(QUESTION_MARK)
                        && redirectUrl.indexOf(HASH_CHAR) < redirectUrl.indexOf(QUESTION_MARK);
        try {
            // Encode the hash character if the redirect URL is a fragmented URL.
            if (isAFragmentURL) {
                int splitIndex = redirectUrl.indexOf(QUESTION_MARK);
                uriBuilder = new URIBuilder(redirectUrl.substring(0, splitIndex).replace(HASH_CHAR, HASH_CHAR_ENCODED)
                        + redirectUrl.substring(splitIndex));
            } else {
                uriBuilder = new URIBuilder(redirectUrl);
            }
        } catch (URISyntaxException e) {
            log.warn("Unable to filter redirect params for url." + redirectUrl, e);
            return redirectUrl;
        }

        // If the host name is not white listed then the query params will not be removed from the redirect url.
        List<String> filteringEnabledHosts = FileBasedConfigurationBuilder.getInstance().getFilteringEnabledHostNames();
        if (CollectionUtils.isNotEmpty(filteringEnabledHosts)
                && !filteringEnabledHosts.contains(uriBuilder.getHost())) {
            return redirectUrl;
        }

        List<NameValuePair> queryParamsList = uriBuilder.getQueryParams();

        if (action != null
                && action.equals(FrameworkConstants.AUTH_ENDPOINT_QUERY_PARAMS_ACTION_EXCLUDE)) {
            if (queryParamsList != null) {
                Iterator<NameValuePair> iterator = queryParamsList.iterator();
                while (iterator.hasNext()) {
                    NameValuePair nameValuePair = iterator.next();
                    String paramName = nameValuePair.getName();
                    String paramValue = nameValuePair.getValue();

                    //skip sessionDataKey which is mandatory
                    if (SESSION_DATA_KEY.equals(paramName)) {
                        continue;
                    }

                    if (queryParams.contains(paramName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(paramName + " is in exclude list, hence removing from url and making " +
                                    "available via API");
                        }
                        dataStoreMap.put(paramName, paramValue);
                        iterator.remove();
                    }
                }
            }
        } else {
            if (queryParamsList != null) {
                Iterator<NameValuePair> iterator = queryParamsList.iterator();
                while (iterator.hasNext()) {
                    NameValuePair nameValuePair = iterator.next();
                    String paramName = nameValuePair.getName();
                    String paramValue = nameValuePair.getValue();

                    //skip sessionDataKey which is mandatory
                    if (SESSION_DATA_KEY.equals(paramName)) {
                        continue;
                    }

                    if (!queryParams.contains(paramName)) {
                        if (log.isDebugEnabled()) {
                            log.debug(paramName + " is not in include list, hence removing from url and making " +
                                    "available via API");
                        }
                        dataStoreMap.put(paramName, paramValue);
                        iterator.remove();
                    }
                }
            }
        }
        uriBuilder.clearParameters();
        uriBuilder.setParameters(queryParamsList);
        String redirectURLWithFilteredParams = uriBuilder.toString();

        // Decode the hash character if the redirect URL is a fragmented URL.
        if (isAFragmentURL) {
            int splitIndex = redirectUrl.indexOf(QUESTION_MARK);
            redirectURLWithFilteredParams =
                    redirectURLWithFilteredParams.substring(0, splitIndex).replace(HASH_CHAR_ENCODED, HASH_CHAR)
                            + redirectURLWithFilteredParams.substring(splitIndex);
        }
        return redirectURLWithFilteredParams;
    }

    public static boolean isRemoveAPIParamsOnConsume() {

        return FileBasedConfigurationBuilder.getInstance().isRemoveAPIParametersOnConsume();
    }

    public static int getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public static void setMaxInactiveInterval(int maxInactiveInterval) {
        FrameworkUtils.maxInactiveInterval = maxInactiveInterval;
    }

    public static String prependUserStoreDomainToName(String authenticatedSubject) {

        if (authenticatedSubject == null || authenticatedSubject.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid argument. authenticatedSubject : "
                                               + authenticatedSubject);
        }
        if (!authenticatedSubject.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
            if (UserCoreUtil.getDomainFromThreadLocal() != null
                && !UserCoreUtil.getDomainFromThreadLocal().isEmpty()) {
                authenticatedSubject = UserCoreUtil.getDomainFromThreadLocal()
                                       + CarbonConstants.DOMAIN_SEPARATOR + authenticatedSubject;
            }
        } else if (authenticatedSubject.indexOf(CarbonConstants.DOMAIN_SEPARATOR) == 0) {
            throw new IllegalArgumentException("Invalid argument. authenticatedSubject : "
                                               + authenticatedSubject + " begins with '"
                                               + CarbonConstants.DOMAIN_SEPARATOR + "'");
        }
        return authenticatedSubject;
    }

    /*
     * Find the Subject identifier among federated claims
     */
    public static String getFederatedSubjectFromClaims(IdentityProvider identityProvider,
                                                       Map<ClaimMapping, String> claimMappings) {

        String userIdClaimURI = identityProvider.getClaimConfig().getUserClaimURI();
        ClaimMapping claimMapping = new ClaimMapping();
        Claim claim = new Claim();
        claim.setClaimUri(userIdClaimURI);
        claimMapping.setRemoteClaim(claim);
        claimMapping.setLocalClaim(claim);
        return claimMappings.get(claimMapping);
    }

    /*
     * Find the Subject identifier among federated claims
     */
    public static String getFederatedSubjectFromClaims(AuthenticationContext context, String otherDialect)
            throws FrameworkException {
        String value;
        boolean useLocalClaimDialect = context.getExternalIdP().useDefaultLocalIdpDialect();
        String userIdClaimURI = context.getExternalIdP().getUserIdClaimUri();
        Map<ClaimMapping, String> claimMappings = context.getSubject().getUserAttributes();

        if (useLocalClaimDialect) {
            Map<String, String> extAttributesValueMap = FrameworkUtils.getClaimMappings(claimMappings, false);
            Map<String, String> mappedAttrs = null;
            try {
                mappedAttrs = ClaimMetadataHandler.getInstance().getMappingsMapFromOtherDialectToCarbon(otherDialect,
                        extAttributesValueMap.keySet(), context.getTenantDomain(), true);
            } catch (ClaimMetadataException e) {
                throw new FrameworkException("Error while loading claim mappings.", e);
            }

            String spUserIdClaimURI = mappedAttrs.get(userIdClaimURI);
            value = extAttributesValueMap.get(spUserIdClaimURI);
        } else {
            ClaimMapping claimMapping = new ClaimMapping();
            Claim claim = new Claim();
            claim.setClaimUri(userIdClaimURI);
            claimMapping.setRemoteClaim(claim);
            claimMapping.setLocalClaim(claim);
            value = claimMappings.get(claimMapping);
        }
        return value;
    }

    /**
     * Starts the tenant flow for the given tenant domain.
     *
     * @param tenantDomain tenant domain
     */
    public static void startTenantFlow(String tenantDomain) {
        String tenantDomainParam = tenantDomain;
        int tenantId = MultitenantConstants.SUPER_TENANT_ID;

        if (tenantDomainParam != null && !tenantDomainParam.trim().isEmpty()) {
            try {
                tenantId = FrameworkServiceComponent.getRealmService().getTenantManager()
                        .getTenantId(tenantDomain);
            } catch (UserStoreException e) {
                log.error("Error while getting tenantId from tenantDomain query param", e);
            }
        } else {
            tenantDomainParam = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext
                .getThreadLocalCarbonContext();
        carbonContext.setTenantId(tenantId);
        carbonContext.setTenantDomain(tenantDomainParam);
    }

    /**
     * Ends the tenant flow.
     */
    public static void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    /**
     * create a nano time stamp relative to Unix Epoch.
     */
    public static long getCurrentStandardNano() {

        long epochTimeReference = TimeUnit.MILLISECONDS.toNanos(
                FrameworkServiceDataHolder.getInstance().getUnixTimeReference());
        long currentSystemNano = System.nanoTime();
        long currentStandardNano = epochTimeReference + (currentSystemNano - FrameworkServiceDataHolder.getInstance()
                .getNanoTimeReference());
        return currentStandardNano;
    }

    /**
     * Append a query param to the URL (URL may already contain query params).
     */
    public static String appendQueryParamsStringToUrl(String url, String queryParamString) {
        String queryAppendedUrl = url;
        // check whether param string to append is blank
        if (StringUtils.isNotEmpty(queryParamString)) {
            // check whether the URL already contains query params
            String appender;
            if (url.contains("?")) {
                appender = "&";
            } else {
                appender = "?";
            }

            // remove leading anchor or question mark in query params
            if (queryParamString.startsWith("?") || queryParamString.startsWith("&")) {
                queryParamString = queryParamString.substring(1);
            }

            queryAppendedUrl += appender + queryParamString;
        }

        return queryAppendedUrl;
    }

    /**
     * Append a query param map to the URL (URL may already contain query params).
     *
     * @param url         URL string to append the params.
     * @param queryParams Map of query params to be append.
     * @return Built URL with query params.
     * @throws UnsupportedEncodingException Throws when trying to encode the query params.
     *
     * @deprecated Use {@link #buildURLWithQueryParams(String, Map)} instead.
     */
    @Deprecated
    public static String appendQueryParamsToUrl(String url, Map<String, String> queryParams)
            throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Passed URL is empty.");
        }
        if (queryParams == null) {
            throw new IllegalArgumentException("Passed query param map is empty.");
        }

        List<String> queryParam1 = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
            queryParam1.add(entry.getKey() + "=" + encodedValue);
        }

        String queryString = StringUtils.join(queryParam1, "&");

        return appendQueryParamsStringToUrl(url, queryString);
    }

    /**
     * Append a query param map to the URL (URL may already contain query params).
     *
     * @param url         URL string to append the params.
     * @param queryParams Map of query params to be append.
     * @return Built URL with query params.
     * @throws UnsupportedEncodingException Can be thrown when trying to encode the query params.
     */
    public static String buildURLWithQueryParams(String url, Map<String, String> queryParams)
            throws UnsupportedEncodingException {

        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Passed URL is empty.");
        }
        if (MapUtils.isEmpty(queryParams)) {
            return url;
        }

        List<String> queryParam1 = new ArrayList<>();
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            String encodedValue = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
            queryParam1.add(entry.getKey() + "=" + encodedValue);
        }

        String queryString = StringUtils.join(queryParam1, "&");
        return appendQueryParamsStringToUrl(url, queryString);
    }

    public static void publishSessionEvent(String sessionId, HttpServletRequest request, AuthenticationContext
            context, SessionContext sessionContext, AuthenticatedUser user, String status) {

        AuthenticationDataPublisher authnDataPublisherProxy = FrameworkServiceDataHolder.getInstance()
                .getAuthnDataPublisherProxy();

        if (authnDataPublisherProxy != null && authnDataPublisherProxy.isEnabled(context)) {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put(FrameworkConstants.AnalyticsAttributes.USER, user);
            paramMap.put(FrameworkConstants.AnalyticsAttributes.SESSION_ID, sessionId);

            String isPublishingSessionCountEnabledValue = IdentityUtil.getProperty(FrameworkConstants.Config
                    .PUBLISH_ACTIVE_SESSION_COUNT);
            boolean isPublishingSessionCountEnabled = Boolean.parseBoolean(isPublishingSessionCountEnabledValue);

            if (isPublishingSessionCountEnabled) {
                paramMap.put(FrameworkConstants.AnalyticsAttributes.ACTIVE_SESSION_COUNT, getActiveSessionCount(user
                        .getTenantDomain()));
            }
            Map<String, Object> unmodifiableParamMap = Collections.unmodifiableMap(paramMap);
            if (FrameworkConstants.AnalyticsAttributes.SESSION_CREATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionCreation(request, context, sessionContext,
                        unmodifiableParamMap);
            } else if (FrameworkConstants.AnalyticsAttributes.SESSION_UPDATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionUpdate(request, context, sessionContext,
                        unmodifiableParamMap);
            } else if (FrameworkConstants.AnalyticsAttributes.SESSION_TERMINATE.equalsIgnoreCase(status)) {
                authnDataPublisherProxy.publishSessionTermination(request, context, sessionContext,
                        unmodifiableParamMap);
            }
        }
    }

    private static int getActiveSessionCount(String tenantDomain) {

        int activeSessionCount = 0;
        try {
            if (FrameworkServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
                activeSessionCount = UserSessionStore.getInstance().getActiveSessionCount(tenantDomain);
            }
        } catch (UserSessionException e) {
            activeSessionCount = -1;
            log.error("An error occurred while retrieving the active session count. Therefore the active session " +
                    "count is set to -1 in the analytics event.");
        }
        return activeSessionCount;
    }

    private static void updateCookieConfig(CookieBuilder cookieBuilder, IdentityCookieConfig
            cookieConfig, Integer age, String path) {

        if (cookieConfig.getDomain() != null) {
            cookieBuilder.setDomain(cookieConfig.getDomain());
        }

        if (StringUtils.isNotBlank(path)) {
            cookieBuilder.setPath(path);
        } else if (cookieConfig.getPath() != null) {
            cookieBuilder.setPath(cookieConfig.getPath());
        }

        if (cookieConfig.getComment() != null) {
            cookieBuilder.setComment(cookieConfig.getComment());
        }

        if (age != null) {
            cookieBuilder.setMaxAge(age);
        } else if (cookieConfig.getMaxAge() > 0) {
            cookieBuilder.setMaxAge(cookieConfig.getMaxAge());
        }

        if (cookieConfig.getVersion() > 0) {
            cookieBuilder.setVersion(cookieConfig.getVersion());
        }

        if (cookieConfig.getSameSite() != null) {
            cookieBuilder.setSameSite(cookieConfig.getSameSite());
        }

        cookieBuilder.setHttpOnly(cookieConfig.isHttpOnly());

        cookieBuilder.setSecure(cookieConfig.isSecure());
    }

    public static String getMultiAttributeSeparator() {

        String multiAttributeSeparator = null;
        if (Boolean.parseBoolean(IdentityUtil.getProperty(ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ENABLED))) {
            try {
                Attribute configAttribute = FrameworkServiceDataHolder.getInstance().getConfigurationManager()
                        .getAttribute(ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_TYPE,
                                ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_RESOURCE_NAME,
                                ORG_WISE_MULTI_ATTRIBUTE_SEPARATOR_ATTRIBUTE_NAME);

                if (configAttribute != null && StringUtils.isNotBlank(configAttribute.getValue())) {
                    multiAttributeSeparator = configAttribute.getValue();
                }
            } catch (ConfigurationManagementException e) {
                if (!ERROR_CODE_RESOURCE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode()) &&
                        !ERROR_CODE_ATTRIBUTE_DOES_NOT_EXISTS.getCode().equals(e.getErrorCode())) {
                    log.error(String.format("Error while retrieving the custom MultiAttributeSeparator " +
                                    "for the tenant: %s. Error code: %s, Error message: %s",
                            CarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e.getErrorCode(),
                            e.getMessage()));
                }
            }
        }

        if (StringUtils.isBlank(multiAttributeSeparator)) {
            try {
                multiAttributeSeparator = CarbonContext.getThreadLocalCarbonContext().getUserRealm().
                        getRealmConfiguration().getUserStoreProperty(IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR);
            } catch (UserStoreException e) {
                log.error("Error while retrieving MultiAttributeSeparator from UserRealm.");
                if (log.isDebugEnabled()) {
                    log.debug("Error while retrieving MultiAttributeSeparator from UserRealm.", e);
                }
            }
        }

        if (StringUtils.isBlank(multiAttributeSeparator)) {
            multiAttributeSeparator = IdentityCoreConstants.MULTI_ATTRIBUTE_SEPARATOR_DEFAULT;
            if (log.isDebugEnabled()) {
                log.debug("Multi Attribute Separator is defaulting to " + multiAttributeSeparator);
            }
        }

        return multiAttributeSeparator;
    }

    public static String getPASTRCookieName (String sessionDataKey) {
        return FrameworkConstants.PASTR_COOKIE + "-" + sessionDataKey;
    }

    /**
     * Map the external IDP roles to local roles.
     * If excludeUnmapped is true exclude unmapped roles.
     * Otherwise include unmapped roles as well.
     *
     * @param externalIdPConfig     Relevant external IDP Config.
     * @param extAttributesValueMap Attributes map.
     * @param idpRoleClaimUri       IDP role claim URI.
     * @param excludeUnmapped       to indicate whether to exclude unmapped.
     * @return ArrayList<string> list of roles.
     */
    public static List<String> getIdentityProvideMappedUserRoles(ExternalIdPConfig externalIdPConfig,
            Map<String, String> extAttributesValueMap, String idpRoleClaimUri, Boolean excludeUnmapped) {

        if (idpRoleClaimUri == null) {
            // Since idpRoleCalimUri is not defined cannot do role mapping.
            if (log.isDebugEnabled()) {
                log.debug("Role claim uri is not configured for the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        String idpRoleAttrValue = null;
        if (extAttributesValueMap != null) {
            idpRoleAttrValue = extAttributesValueMap.get(idpRoleClaimUri);
        }
        String[] idpRoles;
        String federatedIDPRoleClaimAttributeSeparator;
        if (idpRoleAttrValue != null) {
            if (IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR) != null) {
                federatedIDPRoleClaimAttributeSeparator = IdentityUtil.getProperty(FrameworkConstants
                        .FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR);
                if (log.isDebugEnabled()) {
                    log.debug("The IDP side role claim value separator is configured as : "
                            + federatedIDPRoleClaimAttributeSeparator);
                }
            } else {
                federatedIDPRoleClaimAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();
            }

            idpRoles = idpRoleAttrValue.split(federatedIDPRoleClaimAttributeSeparator);
        } else {
            // No identity provider role values found.
            if (log.isDebugEnabled()) {
                log.debug(
                        "No role attribute value has received from the external IDP: " + externalIdPConfig.getIdPName()
                                + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        Map<String, String> idpToLocalRoleMapping = externalIdPConfig.getRoleMappings();
        Set<String> idpMappedUserRoles = new HashSet<>();
        // If no role mapping is configured in the identity provider.
        if (MapUtils.isEmpty(idpToLocalRoleMapping)) {
            if (log.isDebugEnabled()) {
                log.debug("No role mapping is configured in the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            if (excludeUnmapped) {
                return new ArrayList<>();
            }
            idpMappedUserRoles.addAll(Arrays.asList(idpRoles));
            return new ArrayList<>(idpMappedUserRoles);
        }
        for (String idpRole : idpRoles) {
            if (idpToLocalRoleMapping.containsKey(idpRole)) {
                idpMappedUserRoles.add(idpToLocalRoleMapping.get(idpRole));
            } else if (!excludeUnmapped) {
                idpMappedUserRoles.add(idpRole);
            }
        }
        return new ArrayList<>(idpMappedUserRoles);
    }

    /**
     * Get the roles assigned to the federated user.
     *
     * @param externalIdPConfig     External IDP Config.
     * @param extAttributesValueMap Attributes map.
     * @param idpGroupClaimUri      IDP group claim URI.
     * @param tenantDomain          Tenant domain.
     * @return List of roles assigned to the federated user.
     * @throws FrameworkException If an error occurred while getting the roles assigned to the federated user.
     */
    public static List<String> getAssignedRolesFromIdPGroups(ExternalIdPConfig externalIdPConfig,
                                                             Map<String, String> extAttributesValueMap,
                                                             String idpGroupClaimUri, String tenantDomain)
            throws FrameworkException {

        if (idpGroupClaimUri == null) {
            // Since idpGroupClaimUri is not defined cannot do role assignment.
            if (log.isDebugEnabled()) {
                log.debug("Group claim uri is not configured for the external IDP: " + externalIdPConfig.getIdPName()
                        + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        String idpGroupAttrValue = null;
        if (extAttributesValueMap != null) {
            idpGroupAttrValue = extAttributesValueMap.get(idpGroupClaimUri);
        }
        List<String> idpGroupValues;
        String federatedIDPGroupClaimAttributeSeparator;
        if (idpGroupAttrValue != null) {
            if (IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_GROUP_CLAIM_VALUE_SEPARATOR) != null) {
                federatedIDPGroupClaimAttributeSeparator = IdentityUtil.getProperty(FrameworkConstants
                        .FEDERATED_IDP_GROUP_CLAIM_VALUE_SEPARATOR);
                if (log.isDebugEnabled()) {
                    log.debug("The IDP side group claim value separator is configured as : "
                            + federatedIDPGroupClaimAttributeSeparator);
                }
            } else {
                federatedIDPGroupClaimAttributeSeparator = FrameworkUtils.getMultiAttributeSeparator();
            }

            idpGroupValues = Arrays.asList(idpGroupAttrValue.split(federatedIDPGroupClaimAttributeSeparator));
        } else {
            // No identity provider group values found.
            if (log.isDebugEnabled()) {
                log.debug("No group attribute value has received from the external IDP: "
                        + externalIdPConfig.getIdPName() + ", in Domain: " + externalIdPConfig.getDomain() + ".");
            }
            return new ArrayList<>();
        }
        IdPGroup[] possibleIDPGroups = externalIdPConfig.getIdentityProvider().getIdPGroupConfig();
        List<String> idpGroupIds =  new ArrayList<>();
        for (IdPGroup idpGroup : possibleIDPGroups) {
            if (idpGroup.getIdpGroupId() != null && idpGroupValues.contains(idpGroup.getIdpGroupName())) {
                idpGroupIds.add(idpGroup.getIdpGroupId());
            }
        }
        try {
            return FrameworkServiceDataHolder.getInstance().getRoleManagementServiceV2()
                    .getRoleIdListOfIdpGroups(idpGroupIds, tenantDomain);
        } catch (IdentityRoleManagementException e) {
            throw new FrameworkException("Error while getting role ids of idp groups.", e);
        }
    }

    /**
     * Get the unmapped IDP groups from the IDP group attribute value.
     *
     * @param externalIdPConfig External IDP Config.
     * @param remoteClaims      Remote claims.
     * @param idpGroupClaimUri  IDP group claim URI.
     * @return Unmapped IDP groups.
     */
    public static List<String> getUnmappedIDPGroups(ExternalIdPConfig externalIdPConfig,
                                                    Map<String, String> remoteClaims, String idpGroupClaimUri) {

        if (StringUtils.isBlank(idpGroupClaimUri) || MapUtils.isEmpty(remoteClaims) || externalIdPConfig == null) {
            return Collections.emptyList();
        }
        IdentityProvider identityProvider = externalIdPConfig.getIdentityProvider();
        if (identityProvider == null) {
            return Collections.emptyList();
        }
        IdPGroup[] possibleIdPGroups = identityProvider.getIdPGroupConfig();
        if (ArrayUtils.isEmpty(possibleIdPGroups)) {
            return Collections.emptyList();
        }
        String idpGroupValueAttr = remoteClaims.get(idpGroupClaimUri);
        if (StringUtils.isBlank(idpGroupValueAttr)) {
            return Collections.emptyList();
        }

        String idpGroupValueSeparator = getIdpGroupClaimValueSeparator();
        String[] idpGroupValues = idpGroupValueAttr.split(Pattern.quote(idpGroupValueSeparator));
        List<String> possibleIdPGroupNames = Arrays.stream(possibleIdPGroups)
                .map(IdPGroup::getIdpGroupName)
                .collect(Collectors.toList());
        return Arrays.stream(idpGroupValues)
                .filter(idpGroup -> !possibleIdPGroupNames.contains(idpGroup))
                .collect(Collectors.toList());
    }

    /**
     * Get app associated roles of local user.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID.
     * @return List of app associated roles of local user.
     * @throws FrameworkException If an error occurred while getting app associated roles of local user.
     */
    public static List<String> getAppAssociatedRolesOfLocalUser(AuthenticatedUser authenticatedUser,
                                                                String applicationId) throws FrameworkException {

        ApplicationRolesResolver appRolesResolver = FrameworkServiceDataHolder.getInstance()
                .getHighestPriorityApplicationRolesResolver();
        if (appRolesResolver == null) {
            log.debug("No app associated roles resolver found.");
            // Return empty list if no app associated roles resolver is available.
            return new ArrayList<>();
        }
        String[] appAssociatedRolesOfUser;
        try {
            appAssociatedRolesOfUser = appRolesResolver.getAppAssociatedRolesOfLocalUser(authenticatedUser,
                    applicationId);
            if (appAssociatedRolesOfUser == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(appAssociatedRolesOfUser);
        } catch (ApplicationRolesException e) {
            throw new FrameworkException("Error while retrieving app associated roles for user: " +
                    authenticatedUser.getLoggableUserId() + " and application: " + applicationId, e);
        }
    }

    /**
     * Get app associated roles of federated user.
     *
     * @param authenticatedUser Authenticated user.
     * @param applicationId     Application ID.
     * @param idpGroupClaimUri  IDP group claim URI.
     * @return List of app associated roles of federated user.
     * @throws FrameworkException If an error occurred while getting app associated roles of federated user.
     */
    public static List<String> getAppAssociatedRolesOfFederatedUser(AuthenticatedUser authenticatedUser,
                                                                    String applicationId, String idpGroupClaimUri)
            throws FrameworkException {

        ApplicationRolesResolver appRolesResolver = FrameworkServiceDataHolder.getInstance()
                .getHighestPriorityApplicationRolesResolver();
        if (appRolesResolver == null) {
            log.debug("No app associated roles resolver found.");
            // Return empty list if no app associated roles resolver is available.
            return new ArrayList<>();
        }
        String[] appAssociatedRolesOfFedUser;
        try {
            appAssociatedRolesOfFedUser = appRolesResolver.getAppAssociatedRolesOfFederatedUser(authenticatedUser,
                    applicationId, idpGroupClaimUri);
            if (appAssociatedRolesOfFedUser == null) {
                return new ArrayList<>();
            }
            return Arrays.asList(appAssociatedRolesOfFedUser);
        } catch (ApplicationRolesException e) {
            throw new FrameworkException("Error while retrieving app associated roles for user: " +
                    authenticatedUser.getLoggableUserId() + " and application: " + applicationId, e);
        }
    }

    /**
     * To get the role claim uri of an IDP.
     *
     * @param externalIdPConfig Relevant external IDP Config.
     * @return idp role claim URI.
     */
    public static String getIdpRoleClaimUri(ExternalIdPConfig externalIdPConfig) {

        // get external identity provider role claim uri.
        String idpRoleClaimUri = externalIdPConfig.getRoleClaimUri();
        if (idpRoleClaimUri == null || idpRoleClaimUri.isEmpty()) {
            // no role claim uri defined
            // we can still try to find it out - lets have a look at the claim
            // mapping.
            ClaimMapping[] idpToLocalClaimMapping = externalIdPConfig.getClaimMappings();
            if (idpToLocalClaimMapping != null && idpToLocalClaimMapping.length > 0) {
                for (ClaimMapping mapping : idpToLocalClaimMapping) {
                    if (getLocalGroupsClaimURI().equals(mapping.getLocalClaim().getClaimUri())
                            && mapping.getRemoteClaim() != null) {
                        return mapping.getRemoteClaim().getClaimUri();
                    }
                }
            } else {
                // Setting the default role claim uri.
                idpRoleClaimUri = getLocalGroupsClaimURI();
            }
        }
        return idpRoleClaimUri;
    }

    /**
     * Get the Role Claim Uri in IDPs dialect.
     *
     * @param stepConfig Relevant stepConfig.
     * @param context Relevant AuthenticationContext.
     * @return Role Claim Uri as String.
     * @throws FrameworkException
     */
    public static String getIdpRoleClaimUri(StepConfig stepConfig, AuthenticationContext context)
            throws FrameworkException {

        String idpRoleClaimUri = getIdpRoleClaimUri(context.getExternalIdP());
        return FrameworkUtils.getMappedIdpRoleClaimUri(idpRoleClaimUri, stepConfig, context);
    }

    /**
     * Returns the group claim uri of an IDP.
     *
     * @param externalIdPConfig Relevant external IDP Config.
     * @return IDP group claim URI.
     */
    public static String getIdpGroupClaimUri(ExternalIdPConfig externalIdPConfig) {

        return getIdpGroupClaimUri(externalIdPConfig.getClaimMappings());
    }

    /**
     * Returns the group claim uri of an IDP.
     *
     * @param claimMappings Claim mappings.
     * @return IDP group claim URI.
     */
    public static String getIdpGroupClaimUri(ClaimMapping[] claimMappings) {

        return Arrays.stream(claimMappings)
                .filter(claimMap ->
                        FrameworkConstants.GROUPS_CLAIM.equals(claimMap.getLocalClaim().getClaimUri()))
                .map(claimMap -> claimMap.getRemoteClaim().getClaimUri())
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns IDP group claim uri.
     *
     * @param stepConfig Step config.
     * @param context    Authentication context.
     * @return IDP group claim uri.
     */
    public static String getIdpGroupClaimUri(StepConfig stepConfig, AuthenticationContext context) {

        String idpGroupMappingURI = FrameworkConstants.GROUPS_CLAIM;

        ExternalIdPConfig externalIdPConfig = context.getExternalIdP();
        ClaimMapping[] claimMappings = externalIdPConfig.getClaimMappings();

        // Return the IDP group claim uri if claim mapping is available for groups claim.
        if (claimMappings != null && claimMappings.length > 0) {
            for (ClaimMapping mapping : claimMappings) {
                if (FrameworkConstants.GROUPS_CLAIM.equals(mapping.getLocalClaim().getClaimUri())
                        && mapping.getRemoteClaim() != null) {
                    return mapping.getRemoteClaim().getClaimUri();
                }
            }
        }

        ApplicationAuthenticator authenticator = stepConfig.
                getAuthenticatedAutenticator().getApplicationAuthenticator();

        boolean useDefaultIdpDialect = externalIdPConfig.useDefaultLocalIdpDialect();
        boolean useLocalClaimDialectForClaimMappings =
                FileBasedConfigurationBuilder.getInstance().isCustomClaimMappingsForAuthenticatorsAllowed();
        boolean mergingCustomClaimMappingsWithDefaultClaimMappingsAllowed = useLocalClaimDialectForClaimMappings &&
                FileBasedConfigurationBuilder.getInstance()
                        .isMergingCustomClaimMappingsWithDefaultClaimMappingsAllowed();

        Map<String, String> carbonToStandardClaimMapping = new HashMap<>();

        // Check whether to use the default dialect.
        if (useDefaultIdpDialect || !useLocalClaimDialectForClaimMappings ||
                mergingCustomClaimMappingsWithDefaultClaimMappingsAllowed) {
            String idPStandardDialect = authenticator.getClaimDialectURI();
            try {
                if (StringUtils.isNotBlank(idPStandardDialect)) {
                    carbonToStandardClaimMapping = ClaimMetadataHandler.getInstance()
                            .getMappingsMapFromOtherDialectToCarbon(idPStandardDialect, null,
                                    context.getTenantDomain(), false);
                }
                for (Entry<String, String> entry : carbonToStandardClaimMapping.entrySet()) {
                    if (StringUtils.isNotEmpty(idpGroupMappingURI) &&
                            idpGroupMappingURI.equalsIgnoreCase(entry.getValue())) {
                        idpGroupMappingURI = entry.getKey();
                    }
                }
            } catch (ClaimMetadataException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error in getting the mapping between idps and standard dialect.Thus returning the " +
                            "unmapped GroupClaimUri: " + idpGroupMappingURI, e);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Custom claim mappings are enabled and no custom mapping configured for groups claim. " +
                        "Thus setting GroupClaimUri to empty string.");
            }
            idpGroupMappingURI = StringUtils.EMPTY;
        }
        return idpGroupMappingURI;
    }

    /**
     * Returns effective IDP group claim uri.
     * If USE_LOCAL_ROLE_CLAIM_FOR_IDP_GROUP_CLAIM_MAPPING is true, returns the IDP role claim uri.
     * Otherwise, returns the IDP group claim uri.
     *
     * @param stepConfig Step config.
     * @param context    Authentication context.
     * @return Effective IDP group claim uri.
     * @throws FrameworkException If an error occurred while getting the effective IDP group claim uri.
     */
    public static String getEffectiveIdpGroupClaimUri(StepConfig stepConfig, AuthenticationContext context)
            throws FrameworkException {

        boolean useLocalRoleClaimForIDPGroupMapping = Boolean.parseBoolean(
                IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM));
        if (useLocalRoleClaimForIDPGroupMapping) {
            return getIdpRoleClaimUri(stepConfig, context);
        }
        return getIdpGroupClaimUri(stepConfig, context);
    }

    /**
     * Get the mapped URI for the IDP role mapping.
     * @param idpRoleClaimUri pass the IdpClaimUri created in getIdpRoleClaimUri method
     * @param stepConfig Relevant stepConfig
     * @param context Relevant authentication context
     * @return idpRole claim uri in IDPs dialect or Custom dialect
     */
    public static String getMappedIdpRoleClaimUri(String idpRoleClaimUri, StepConfig stepConfig,
                                                  AuthenticationContext context) {

        // Finally return the incoming idpClaimUri if it is in expected dialect.
        String idpRoleMappingURI = idpRoleClaimUri;

        ApplicationAuthenticator authenticator = stepConfig.
                getAuthenticatedAutenticator().getApplicationAuthenticator();

        // Read the value from management console.
        boolean useDefaultIdpDialect = context.getExternalIdP().useDefaultLocalIdpDialect();

        // Read value from file based configuration.
        boolean useLocalClaimDialectForClaimMappings =
                FileBasedConfigurationBuilder.getInstance().isCustomClaimMappingsForAuthenticatorsAllowed();
        boolean mergingCustomClaimMappingsWithDefaultClaimMappingsAllowed = useLocalClaimDialectForClaimMappings &&
                FileBasedConfigurationBuilder.getInstance()
                        .isMergingCustomClaimMappingsWithDefaultClaimMappingsAllowed();

        Map<String, String> carbonToStandardClaimMapping = new HashMap<>();

        // Check whether to use the default dialect.
        if (useDefaultIdpDialect || !useLocalClaimDialectForClaimMappings ||
                mergingCustomClaimMappingsWithDefaultClaimMappingsAllowed) {
            String idPStandardDialect = authenticator.getClaimDialectURI();
            try {
                if (StringUtils.isNotBlank(idPStandardDialect)) {
                    // Maps the idps dialect to standard dialect.
                    carbonToStandardClaimMapping = ClaimMetadataHandler.getInstance()
                            .getMappingsMapFromOtherDialectToCarbon(idPStandardDialect, null,
                                    context.getTenantDomain(), false);
                }
                // check for role claim uri in the idaps dialect.
                for (Entry<String, String> entry : carbonToStandardClaimMapping.entrySet()) {
                    if (StringUtils.isNotEmpty(idpRoleMappingURI) &&
                        idpRoleMappingURI.equalsIgnoreCase(entry.getValue())) {
                        idpRoleMappingURI = entry.getKey();
                    }
                }
            } catch (ClaimMetadataException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error in getting the mapping between idps and standard dialect.Thus returning the " +
                            "unmapped RoleClaimUri: " + idpRoleMappingURI, e);
                }
            }
        }
        return idpRoleMappingURI;
    }

    /**
     * Returns the local claim uri that is mapped for the IdP role claim uri configured.
     * If no role claim uri is configured for the IdP returns the local claim by calling 'IdentityUtils
     * .#getLocalGroupsClaimURI()'.
     * @param externalIdPConfig IdP configurations
     * @return local claim uri mapped for the IdP role claim uri.
     */
    public static String getLocalClaimUriMappedForIdPRoleClaim(ExternalIdPConfig externalIdPConfig) {
        // get external identity provider role claim uri.
        String idpRoleClaimUri = externalIdPConfig.getRoleClaimUri();
        if (StringUtils.isNotBlank(idpRoleClaimUri)) {
            // Iterate over IdP claim mappings and check for the local claim that is mapped for the remote IdP role
            // claim uri configured.
            ClaimMapping[] idpToLocalClaimMapping = externalIdPConfig.getClaimMappings();
            if (!ArrayUtils.isEmpty(idpToLocalClaimMapping)) {
                for (ClaimMapping mapping : idpToLocalClaimMapping) {
                    if (mapping.getRemoteClaim() != null && idpRoleClaimUri
                            .equals(mapping.getRemoteClaim().getClaimUri())) {
                        return mapping.getLocalClaim().getClaimUri();
                    }
                }
            }
        }
        return getLocalGroupsClaimURI();
    }

    /**
     * Returns the separator used to separate the values of the IDP group claim.
     *
     * @return IDP group claim value separator.
     */
    public static String getIdpGroupClaimValueSeparator() {

        boolean useLocalRoleIdpRoleMapping = Boolean.parseBoolean(
                IdentityUtil.getProperty(USE_IDP_ROLE_CLAIM_AS_IDP_GROUP_CLAIM));
        if (IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_GROUP_CLAIM_VALUE_SEPARATOR) != null
                && !useLocalRoleIdpRoleMapping) {
            return IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_GROUP_CLAIM_VALUE_SEPARATOR);
        } else if (IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR) != null
                && useLocalRoleIdpRoleMapping) {
            return IdentityUtil.getProperty(FrameworkConstants.FEDERATED_IDP_ROLE_CLAIM_VALUE_SEPARATOR);
        }
        return FrameworkUtils.getMultiAttributeSeparator();
    }

    /**
     * @deprecated This method is a temporary solution and might get changed in the future.
     * It is recommended not use this method.
     *
     * @param context AuthenticationContext.
     * @return true if the handlers need to be executed, otherwise false.
     */
    @Deprecated
    public static boolean isStepBasedSequenceHandlerExecuted(AuthenticationContext context) {

        boolean isNeeded = true;
        SequenceConfig sequenceConfig = context.getSequenceConfig();
        AuthenticatedUser authenticatedUser = sequenceConfig.getAuthenticatedUser();
        Object isDefaultStepBasedSequenceHandlerTriggered = context
                .getProperty(FrameworkConstants.STEP_BASED_SEQUENCE_HANDLER_TRIGGERED);
        // If authenticated user is null or if step based sequence handler is not trigged, exit the flow.
        if (authenticatedUser == null || isDefaultStepBasedSequenceHandlerTriggered == null
                || !(boolean) isDefaultStepBasedSequenceHandlerTriggered) {
            isNeeded = false;
        }
        return isNeeded;
    }

    /**
     * To get the missing mandatory claims from SP side.
     *
     * @param context Authentication Context.
     * @return set of missing claims
     */
    public static String[] getMissingClaims(AuthenticationContext context) {

        StringJoiner missingClaimsString = new StringJoiner(",");
        StringJoiner missingClaimValuesString = new StringJoiner(",");

        Map<String, String> missingClaims = getMissingClaimsMap(context);

        for (Map.Entry<String, String> entry : missingClaims.entrySet()) {
            missingClaimsString.add(entry.getKey());
            missingClaimValuesString.add(entry.getValue());
        }

        return new String[]{missingClaimsString.toString(), missingClaimValuesString.toString()};
    }

    /**
     * Get the missing mandatory claims as a hash map.
     *
     * @param context Authentication Context.
     * @return Map of missing claims.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getMissingClaimsMap(AuthenticationContext context) {

        Map<String, String> mappedAttrs = new HashMap<>();
        AuthenticatedUser user = context.getSequenceConfig().getAuthenticatedUser();
        Map<ClaimMapping, String> userAttributes = user.getUserAttributes();
        if (userAttributes != null) {
            Map<String, String> spToCarbonClaimMapping = new HashMap<>();
            Object object = context.getProperty(FrameworkConstants.SP_TO_CARBON_CLAIM_MAPPING);

            if (object instanceof Map) {
                spToCarbonClaimMapping = (Map<String, String>) object;
            }
            for (Map.Entry<ClaimMapping, String> entry : userAttributes.entrySet()) {
                String localClaimUri = entry.getKey().getLocalClaim().getClaimUri();

                //getting the carbon claim uri mapping for other claim dialects
                if (MapUtils.isNotEmpty(spToCarbonClaimMapping) && spToCarbonClaimMapping.get(localClaimUri) != null) {
                    localClaimUri = spToCarbonClaimMapping.get(localClaimUri);
                }
                mappedAttrs.put(localClaimUri, entry.getValue());
            }
        }

        Map<String, String> mandatoryClaims = context.getSequenceConfig().getApplicationConfig()
                .getMandatoryClaimMappings();
        Map<String, String> missingClaims = new HashMap<>();
        for (Map.Entry<String, String> entry : mandatoryClaims.entrySet()) {
            if (mappedAttrs.get(entry.getValue()) == null && mappedAttrs.get(entry.getKey()) == null &&
                    getUserClaimValue(context, entry.getValue()) == null) {
                missingClaims.put(entry.getKey(), entry.getValue());
            }
        }

        return missingClaims;
    }

    @SuppressWarnings("unchecked")
    private static String getUserClaimValue(AuthenticationContext context, String localClaim) {

        Object unFilteredLocalClaims = context.getProperty(FrameworkConstants.UNFILTERED_LOCAL_CLAIM_VALUES);
        if (unFilteredLocalClaims instanceof Map) {
            return ((Map<String, String>) unFilteredLocalClaims).get(localClaim);
        }
        return null;
    }

    /**
     * To get the password provisioning url from the configuration file.
     *
     * @return relevant password provisioning url.
     */
    public static String getPasswordProvisioningUIUrl() {

        String passwordProvisioningUrl = IdentityUtil.getProperty("JITProvisioning.PasswordProvisioningUI");
        if (StringUtils.isEmpty(passwordProvisioningUrl)) {
            passwordProvisioningUrl = FrameworkConstants.SIGN_UP_ENDPOINT;
        }
        return passwordProvisioningUrl;
    }

    /**
     * To get the username provisioning url from the configuration file.
     *
     * @return relevant username provisioning url.
     */
    public static String getUserNameProvisioningUIUrl() {

        String userNamePrvisioningUrl = IdentityUtil.getProperty("JITProvisioning.UserNameProvisioningUI");
        if (StringUtils.isEmpty(userNamePrvisioningUrl)) {
            userNamePrvisioningUrl = FrameworkConstants.REGISTRATION_ENDPOINT;
        }
        return userNamePrvisioningUrl;
    }

    /**
     * This method determines whether username pattern validation should be skipped for JIT provisioning users based
     * on the configuration file.
     *
     * @return boolean Whether to skip username validation or not.
     */
    public static boolean isSkipUsernamePatternValidation() {

        return Boolean.parseBoolean(
                IdentityUtil.getProperty("JITProvisioning.SkipUsernamePatternValidation"));
    }

    /**
     * This method determines whether authentication flow should be break if JIT provisioning has failed.
     *
     * @return boolean Whether to fail the authentication flow.
     */
    public static boolean isAuthenticationFailOnJitFail() {

        return Boolean.parseBoolean(IdentityUtil.getProperty("JITProvisioning.FailAuthnOnProvisionFailure"));
    }


    /**
     * This method is to provide flag about Adaptive authentication is availability.
     *
     * @return AdaptiveAuthentication Available or not.
     */
    public static boolean isAdaptiveAuthenticationAvailable() {

        return FrameworkServiceDataHolder.getInstance().isAdaptiveAuthenticationAvailable();
    }

    /**
     * This method is to check whether organization management is enabled.
     *
     * @return Organization management feature is enabled or not.
     */
    public static boolean isOrganizationManagementEnabled() {

        return FrameworkServiceDataHolder.getInstance().isOrganizationManagementEnabled();
    }

    public static boolean promptOnLongWait() {

        boolean promptOnLongWait = false;
        String promptOnLongWaitString = IdentityUtil.getProperty("AdaptiveAuth.PromptOnLongWait");
        if (promptOnLongWaitString != null) {
            promptOnLongWait = Boolean.parseBoolean(promptOnLongWaitString);
        }
        return promptOnLongWait;
    }

    /**
     * This util is used to get the standard claim dialect of an Application based on the SP configuration.
     *
     * @param clientType Client Type.
     * @param appConfig  Application config.
     * @return standard dialect if exists.
     */
    public static String getStandardDialect(String clientType, ApplicationConfig appConfig) {

        Map<String, String> claimMappings = appConfig.getClaimMappings();
        if (FrameworkConstants.RequestType.CLAIM_TYPE_OIDC.equals(clientType)) {
            return "http://wso2.org/oidc/claim";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_STS.equals(clientType)) {
            return "http://schemas.xmlsoap.org/ws/2005/05/identity";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_OPENID.equals(clientType)) {
            return "http://axschema.org";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_SCIM.equals(clientType)) {
            return "urn:scim:schemas:core:1.0";
        } else if (FrameworkConstants.RequestType.CLAIM_TYPE_WSO2.equals(clientType)) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else if (claimMappings == null || claimMappings.isEmpty()) {
            return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
        } else {
            boolean isAtLeastOneNotEqual = false;
            for (Map.Entry<String, String> entry : claimMappings.entrySet()) {
                if (!entry.getKey().equals(entry.getValue())) {
                    isAtLeastOneNotEqual = true;
                    break;
                }
            }
            if (!isAtLeastOneNotEqual) {
                return ApplicationConstants.LOCAL_IDP_DEFAULT_CLAIM_DIALECT;
            }
        }
        return null;
    }

    /**
     * Serialize the object using selected serializable function.
     * @param value Object to evaluate.
     * @return Serialized Object.
     */
    public static Object toJsSerializable(Object value) {

        return FrameworkServiceDataHolder.getInstance().getJsGenericGraphBuilderFactory()
                .getJsUtil().toJsSerializable(value);
    }

    /**
     * De-Serialize the object using selected serializable function.
     * @param value Serialized Object.
     * @param engine Js Engine.
     * @return De-Serialize object.
     * @throws FrameworkException FrameworkException.
     */
    public static Object fromJsSerializable(Object value, ScriptEngine engine) throws FrameworkException {

        return FrameworkServiceDataHolder.getInstance().getJsGraphBuilderFactory().getJsUtil().
                fromJsSerializable(value, engine);
    }

    /**
     * Get the configurations of a tenant from cache or database.
     *
     * @param tenantDomain Domain name of the tenant
     * @return Configurations belong to the tenant
     */
    private static Property[] getResidentIdpConfiguration(String tenantDomain) throws FrameworkException {

        IdpManager identityProviderManager = IdentityProviderManager.getInstance();
        IdentityProvider residentIdp = null;
        try {
            residentIdp = identityProviderManager.getResidentIdP(tenantDomain);
        } catch (IdentityProviderManagementException e) {
            String errorMsg = String.format("Error while retrieving resident Idp for %s tenant.", tenantDomain);
            throw new FrameworkException(errorMsg, e);
        }
        IdentityProviderProperty[] identityMgtProperties = residentIdp.getIdpProperties();
        Property[] configMap = new Property[identityMgtProperties.length];
        int index = 0;
        for (IdentityProviderProperty identityMgtProperty : identityMgtProperties) {
            if (ALREADY_WRITTEN_PROPERTY.equals(identityMgtProperty.getName())) {
                continue;
            }
            Property property = new Property();
            property.setName(identityMgtProperty.getName());
            property.setValue(identityMgtProperty.getValue());
            configMap[index] = property;
            index++;
        }
        return configMap;
    }

    /**
     * This method is used to get the requested resident Idp configuration details.
     *
     * @param propertyName
     * @param tenantDomain
     * @return Property
     * @throws FrameworkException
     */
    public static Property getResidentIdpConfiguration(String propertyName, String tenantDomain) throws
            FrameworkException {

        Property requestedProperty = null;
        Property[] allProperties = getResidentIdpConfiguration(tenantDomain);
        for (int i = 0; i < allProperties.length; i++) {
            if (propertyName.equals(allProperties[i].getName())) {
                requestedProperty = allProperties[i];
                break;
            }
        }

        return requestedProperty;

    }

    /**
     * Check user session mapping enabled.
     *
     * @return Return true if UserSessionMapping configuration enabled and both IDN_AUTH_USER and
     * IDN_AUTH_USER_SESSION_MAPPING tables are available.
     */
    public static boolean isUserSessionMappingEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(USER_SESSION_MAPPING_ENABLED)) && isTableExistsInSessionDB(
                "IDN_AUTH_USER") && isTableExistsInSessionDB("IDN_AUTH_USER_SESSION_MAPPING");
    }

    /**
     * Get the server config for skip user local search during federated authentication flow
     *
     * @return isSkipLocalUserSearchForAuthenticationFlowHandlersEnabled value
     */
    public static boolean isSkipLocalUserSearchForAuthenticationFlowHandlersEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(SKIP_LOCAL_USER_SEARCH_FOR_AUTHENTICATION_FLOW_HANDLERS));
    }

    /**
     * Check whether the specified table exists in the Identity database.
     *
     * @param tableName name of the table.
     * @return true if table exists.
     *
     * @deprecated Please use IdentityDatabaseUtil.isTableExists(String tableName) instead.
     */
    @Deprecated
    public static boolean isTableExists(String tableName) {

        return IdentityDatabaseUtil.isTableExists(tableName);
    }

    /**
     * Check whether the specified table exists in the Session database.
     *
     * @param tableName name of the table.
     * @return true if table exists.
     */
    public static boolean isTableExistsInSessionDB(String tableName) {

        try (Connection connection = IdentityDatabaseUtil.getSessionDBConnection(true)) {

            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.storesLowerCaseIdentifiers()) {
                tableName = tableName.toLowerCase();
            }

            try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[]{ "TABLE" })) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table - " + tableName + " available in the Session database.");
                    }
                    IdentityDatabaseUtil.commitTransaction(connection);
                    return true;
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Table - " + tableName + " not available in the Session database.");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Table - " + tableName + " not available in the Session database.");
        }
        return false;
    }

    public static boolean isConsentPageSkippedForSP(ServiceProvider serviceProvider) {

        if (serviceProvider == null) {
            throw new IllegalArgumentException("A null reference received for service provider.");
        }

        boolean isSkipConsent = false;
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            isSkipConsent = serviceProvider.getLocalAndOutBoundAuthenticationConfig().isSkipConsent();
        }

        if (log.isDebugEnabled()) {
            log.debug("SkipConsent: " + isSkipConsent + " for application: " + serviceProvider.getApplicationName()
                    + " with id: " + serviceProvider.getApplicationID());
        }
        return isSkipConsent;
    }

    /**
     * Check whether skip logout consent page or not.
     *
     * @param serviceProvider Service provider.
     * @return true/false Skip the logout consent page or not.
     */
    public static boolean isLogoutConsentPageSkippedForSP(ServiceProvider serviceProvider) {

        if (serviceProvider == null) {
            throw new IllegalArgumentException("A null reference received for service provider.");
        }
        boolean isSkipLogoutConsent = false;
        if (serviceProvider.getLocalAndOutBoundAuthenticationConfig() != null) {
            isSkipLogoutConsent = serviceProvider.getLocalAndOutBoundAuthenticationConfig().isSkipLogoutConsent();
        }

        if (log.isDebugEnabled()) {
            log.debug("SkipLogoutConsent: " + isSkipLogoutConsent + " for application: " +
                    serviceProvider.getApplicationName() + " with id: " + serviceProvider.getApplicationID());
        }

        return isSkipLogoutConsent;
    }

    /**
     * Check whether the specified column of the specified table exists in the Identity database.
     *
     * @param tableName name of the table.
     * @param columnName name of the column.
     * @return true if column exists.
     */
    public static boolean isTableColumnExists(String tableName, String columnName) {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection()) {

            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.storesLowerCaseIdentifiers()) {
                tableName = tableName.toLowerCase();
                columnName = columnName.toLowerCase();
            }

            String schemaPattern = null;
            if (metaData.getDriverName().contains("Oracle")) {
                if (log.isDebugEnabled()) {
                    log.debug("DB type detected as Oracle. Setting schemaPattern to " + metaData.getUserName());
                }
                // Oracle checks the availability of the table column
                // in all users in the DB unless specified.
                schemaPattern = metaData.getUserName();
            }
            try (ResultSet resultSet = metaData.getColumns(null, schemaPattern, tableName, columnName)) {
                if (resultSet.next()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Column - " + columnName + " in table - " + tableName + " is available in the " +
                                "Identity database.");
                    }
                    IdentityDatabaseUtil.commitTransaction(connection);
                    return true;
                }
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException ex) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                if (log.isDebugEnabled()) {
                    log.debug("Column - " + columnName + " in table - " + tableName + " is not available in the " +
                            "Identity database.");
                }
                return false;
            }

        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("Column - " + columnName + " in table - " + tableName + " is not available in the " +
                        "Identity database.");
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("Column - " + columnName + " in table - " + tableName + " is not available in the " +
                    "Identity database.");
        }
        return false;
    }

    /**
     * Checking whether the tenant id column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
     */
    public static void checkIfTenantIdColumnIsAvailableInFedAuthTable() {

        isTenantIdColumnAvailableInFedAuthTable = isTableColumnExists("IDN_FED_AUTH_SESSION_MAPPING", "TENANT_ID");
    }

    /**
     * Return whether the tenant id column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
     *
     * @return True if tenant id is available in IDN_FED_AUTH_SESSION_MAPPING table. Else return false.
     */
    public static boolean isTenantIdColumnAvailableInFedAuthTable() {

        return isTenantIdColumnAvailableInFedAuthTable;
    }

    /**
     * Checking whether the idp id column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
     */
    public static void checkIfIdpIdColumnIsAvailableInFedAuthTable() {

        isIdpIdColumnAvailableInFedAuthTable = isTableColumnExists("IDN_FED_AUTH_SESSION_MAPPING", "IDP_ID");
    }

    /**
     * Return whether the idp id column is available in the IDN_FED_AUTH_SESSION_MAPPING table.
     *
     * @return True if idp id is available in IDN_FED_AUTH_SESSION_MAPPING table. Else return false.
     */
    public static boolean isIdpIdColumnAvailableInFedAuthTable() {

        return isIdpIdColumnAvailableInFedAuthTable;
    }

    /**
     * Remove domain name from roles except the hybrid roles (Internal,Application & Workflow).
     *
     * @param domainAwareRolesList list of roles assigned to a user.
     * @return String of multi attribute separated list of roles assigned to a user with domain name removed from roles.
     */
    public static String removeDomainFromNamesExcludeHybrid(List<String> domainAwareRolesList) {

        List<String> roleList = new ArrayList<String>();
        for (String role : domainAwareRolesList) {
            String userStoreDomain = IdentityUtil.extractDomainFromName(role);
            if (UserCoreConstants.INTERNAL_DOMAIN.equalsIgnoreCase(userStoreDomain) || APPLICATION_DOMAIN
                    .equalsIgnoreCase(userStoreDomain) || WORKFLOW_DOMAIN.equalsIgnoreCase(userStoreDomain)) {
                roleList.add(role);
            } else {
                roleList.add(UserCoreUtil.removeDomainFromName(role));
            }
        }
        return String.join(FrameworkUtils.getMultiAttributeSeparator(), roleList);
    }

    /**
     * This method returns the Federated Association Manager service registered during OSGi deployement.
     *
     * @return FederatedAssociationManger service.
     * @throws FrameworkException
     */
    public static FederatedAssociationManager getFederatedAssociationManager() throws FrameworkException {

        FederatedAssociationManager federatedAssociationManager = FrameworkServiceDataHolder.getInstance()
                .getFederatedAssociationManager();

        if (federatedAssociationManager == null) {
            throw new FrameworkException("Federated Association Manager service is not available.");
        }
        return federatedAssociationManager;
    }

    /**
     * Retrieves the unique user id of the given username. If the unique user id is not available, generate an id and
     * update the userid claim in read/write userstores.
     *
     * @param tenantId    id of the tenant domain of the user
     * @param userStoreDomain userstore of the user
     * @param username        username
     * @return unique user id of the user
     * @throws UserSessionException
     */
    public static String resolveUserIdFromUsername(int tenantId, String userStoreDomain, String username) throws
            UserSessionException {

        try {
            return IdentityUtil.resolveUserIdFromUsername(tenantId, userStoreDomain, username);
        } catch (IdentityException e) {
            throw new UserSessionException("Error occurred while resolving Id for the user: " + username, e);
        }
    }

    /**
     * Check if all the authenticators inside the IdP are flow handlers.
     *
     * @param authenticatorConfigList
     * @return boolean
     */
    public static boolean isAllFlowHandlers(List<AuthenticatorConfig> authenticatorConfigList) {

        for (AuthenticatorConfig config : authenticatorConfigList) {
            if (!(config.getApplicationAuthenticator() instanceof AuthenticationFlowHandler)) {
                return false;
            }
        }
        return true;
    }

    /**
     * By looping over all the IdPs, check if at lease one IdP has enabled the JIT provisioning.
     *
     * @param context
     * @return
     * @throws PostAuthenticationFailedException Post Authentication failed exception.
     */
    public static boolean isJITProvisioningEnabled(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        SequenceConfig sequenceConfig = context.getSequenceConfig();
        for (Map.Entry<Integer, StepConfig> entry : sequenceConfig.getStepMap().entrySet()) {
            StepConfig stepConfig = entry.getValue();
            AuthenticatorConfig authenticatorConfig = stepConfig.getAuthenticatedAutenticator();
            if (authenticatorConfig == null) {
                //May have skipped from the script
                //ex: Different authentication sequences evaluated by the script
                continue;
            }
            ApplicationAuthenticator authenticator = authenticatorConfig.getApplicationAuthenticator();

            if (authenticator instanceof FederatedApplicationAuthenticator) {
                ExternalIdPConfig externalIdPConfig;
                String externalIdPConfigName = stepConfig.getAuthenticatedIdP();
                externalIdPConfig = getExternalIdpConfig(externalIdPConfigName, context);
                context.setExternalIdP(externalIdPConfig);

                if (externalIdPConfig != null && externalIdPConfig.isProvisioningEnabled()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * To get the external IDP Config.
     *
     * @param externalIdPConfigName Name of the external IDP Config.
     * @param context               Authentication Context.
     * @return relevant external IDP config.
     * @throws PostAuthenticationFailedException Post AuthenticationFailedException.
     */
    private static ExternalIdPConfig getExternalIdpConfig(String externalIdPConfigName, AuthenticationContext context)
            throws PostAuthenticationFailedException {

        ExternalIdPConfig externalIdPConfig = null;
        try {
            externalIdPConfig = ConfigurationFacade.getInstance()
                    .getIdPConfigByName(externalIdPConfigName, context.getTenantDomain());
        } catch (IdentityProviderManagementException e) {
            handleExceptions(String.format(ERROR_WHILE_GETTING_IDP_BY_NAME.getMessage(), externalIdPConfigName,
                    context.getTenantDomain()), ERROR_WHILE_GETTING_IDP_BY_NAME.getCode(), e);
        }
        return externalIdPConfig;
    }

    /**
     * To handle exceptions.
     *
     * @param errorMessage Error Message
     * @param errorCode    Error Code.
     * @param e            Exception that is thrown during a failure.
     * @throws PostAuthenticationFailedException Post Authentication Failed Exception.
     */
    private static void handleExceptions(String errorMessage, String errorCode, Exception e)
            throws PostAuthenticationFailedException {

        throw new PostAuthenticationFailedException(errorCode, errorMessage, e);
    }

    /**
     * Retrieves the unique user id of the given username. If the unique user id is not available, generate an id and
     * update the userid claim in read/write userstores.
     *
     * @param userStoreManager userStoreManager related to user.
     * @param username         username.
     * @return user id of the user.
     * @throws UserSessionException
     */
    public static String resolveUserIdFromUsername(UserStoreManager userStoreManager, String username) throws
            UserSessionException {

        try {
            if (userStoreManager instanceof AbstractUserStoreManager) {
                String userId = ((AbstractUserStoreManager) userStoreManager).getUserIDFromUserName(username);

                // If the user id is could not be resolved, probably user does not exist in the user store.
                if (StringUtils.isBlank(userId)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User id could not be resolved for username: " + username + ". Probably" +
                                " user does not exist in the user store.");
                    }
                }
                return userId;
            }
            if (log.isDebugEnabled()) {
                log.debug("Provided user store manager for the user: " + username + ", is not an instance of the " +
                        "AbstractUserStore manager");
            }
            throw new UserSessionException("Unable to get the unique id of the user: " + username + ".");
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while resolving Id for the user: " + username, e);
            }
            throw new UserSessionException("Error occurred while resolving Id for the user: " + username, e);
        }
    }

    /**
     * Retrieves the username of the given userid.
     *
     * @param userStoreManager userStoreManager related to user.
     * @param userId           userid.
     * @return username of the user.
     * @throws UserSessionException
     */
    public static String resolveUserNameFromUserId(UserStoreManager userStoreManager, String userId) throws
            UserSessionException {

        try {
            if (userStoreManager instanceof AbstractUserStoreManager) {
                String userName = ((AbstractUserStoreManager) userStoreManager).getUserNameFromUserID(userId);

                if (StringUtils.isBlank(userName)) {
                    if (log.isDebugEnabled()) {
                        log.debug("User Name could not be resolved for userid: " + userId + ".");
                    }
                }
                return userName;
            }
            if (log.isDebugEnabled()) {
                log.debug("Provided user store manager for the user: " + userId + ", is not an instance of the " +
                        "AbstractUserStore manager.");
            }
            throw new UserSessionException("Unable to get the user name of the user Id: " + userId + ".");
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while resolving user name for the user Id: " + userId, e);
            }
            throw new UserSessionException("Error occurred while resolving user name for the user Id: " + userId, e);
        }
    }

    /**
     * Pre-process user's username considering authentication context.
     *
     * @param username Username of the user.
     * @param context  Authentication context.
     * @return preprocessed username
     */
    public static String preprocessUsername(String username, AuthenticationContext context) {

        boolean isSaaSApp = context.getSequenceConfig().getApplicationConfig().isSaaSApp();

        if (isLegacySaaSAuthenticationEnabled() && isSaaSApp) {
            return username;
        }

        if (IdentityUtil.isEmailUsernameEnabled()) {
            if (StringUtils.countMatches(username, "@") == 1) {
                return username + "@" + context.getUserTenantDomain();
            }
        } else if (!username.endsWith(context.getUserTenantDomain())) {

            // If the username is email-type (without enabling email username option) or belongs to a tenant which is
            // not the app owner.
            if (isSaaSApp && StringUtils.countMatches(username, "@") >= 1) {
                return username;
            }
            return username + "@" + context.getUserTenantDomain();
        }
        return username;
    }

    /**
     * Pre-process user's username considering the service provider.
     *
     * @param username Username of the user.
     * @param serviceProvider The service provider.
     * @return preprocessed username
     */
    public static String preprocessUsername(String username, ServiceProvider serviceProvider) {

        boolean isSaaSApp = serviceProvider.isSaasApp();
        String appTenantDomain = serviceProvider.getOwner().getTenantDomain();

        if (isLegacySaaSAuthenticationEnabled() && isSaaSApp) {
            return username;
        }

        if (IdentityUtil.isEmailUsernameEnabled()) {
            if (StringUtils.countMatches(username, "@") == 1) {
                return username + "@" + appTenantDomain;
            }
        } else if (!username.endsWith(appTenantDomain)) {

            // If the username is email-type (without enabling email username option) or belongs to a tenant which is
            // not the app owner.
            if (isSaaSApp && StringUtils.countMatches(username, "@") >= 1) {
                return username;
            }
            return username + "@" + appTenantDomain;
        }
        return username;
    }

    /**
     * Gets resolvedUserResult from multi attribute login identifier if enable multi attribute login.
     *
     * @param loginIdentifier login identifier for multi attribute login
     * @param tenantDomain    user tenant domain
     * @return resolvedUserResult with SUCCESS status if enable multi attribute login. Otherwise returns
     * resolvedUserResult with FAIL status.
     */
    public static ResolvedUserResult processMultiAttributeLoginIdentification(String loginIdentifier,
                                                                              String tenantDomain) {

        ResolvedUserResult resolvedUserResult = new ResolvedUserResult(ResolvedUserResult.UserResolvedStatus.FAIL);
        if (FrameworkServiceDataHolder.getInstance().getMultiAttributeLoginService().isEnabled(tenantDomain)) {
            resolvedUserResult = FrameworkServiceDataHolder.getInstance().getMultiAttributeLoginService().
                    resolveUser(loginIdentifier, tenantDomain);
        }
        return resolvedUserResult;
    }

    /**
     * Validate the username when email username is enabled.
     *
     * @param username Username.
     * @param context Authentication context.
     * @throws InvalidCredentialsException when username is not an email when email username is enabled.
     */
    public static void validateUsername(String username, AuthenticationContext context)
            throws InvalidCredentialsException {

        if (IdentityUtil.isEmailUsernameEnabled()) {
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            if (StringUtils.countMatches(tenantAwareUsername, "@") < 1) {
                context.setProperty(CONTEXT_PROP_INVALID_EMAIL_USERNAME, true);
                throw new InvalidCredentialsException("Invalid username. Username has to be an email.");
            }
        }
    }

    /**
     * Validate the username.
     *
     * @param username Username of the user.
     * @throws InvalidCredentialsException when username is not valid.
     */
    public static void validateUsername(String username) throws InvalidCredentialsException {

        // Validate username as an email when email username is enabled.
        if (IdentityUtil.isEmailUsernameEnabled()) {
            String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(username);
            if (StringUtils.countMatches(tenantAwareUsername, "@") < 1) {
                throw new InvalidCredentialsException("Invalid username. Username has to be an email.");
            }
        }
    }

    private static String addUserId(String username, UserStoreManager userStoreManager) {

        String userId;
        userId = UUID.randomUUID().toString();
        Map<String, String> claims = new HashMap<>();
        claims.put(UserCoreClaimConstants.USER_ID_CLAIM_URI, userId);
        try {
            userStoreManager.setUserClaimValues(username, claims, null);
        } catch (UserStoreException e) {
            if (log.isDebugEnabled()) {
                log.debug("Error while updating " + UserCoreClaimConstants.USER_ID_CLAIM_URI + " claim of the user: "
                        + username + " with the unique user id.");
            }
        }
        return userId;
    }

    /**
     * Check whether the authentication flow should continue upon facing a claim handling error.
     *
     * @return true/false Continue or break flow when facing claim handling errors.
     */
    public static boolean isContinueOnClaimHandlingErrorAllowed() {

        String continueOnClaimHandlingErrorValue = IdentityUtil.getProperty(CONTINUE_ON_CLAIM_HANDLING_ERROR);

        // If config is empty or not a boolean value, the property must be set to the default value which is true.
        return !Boolean.FALSE.toString().equalsIgnoreCase(continueOnClaimHandlingErrorValue);
    }

    /**
     * Returns the end user portal url.
     *
     * @param myAccountUrl end user portal url
     * @return configured url or the default url if configured url is empty
     */
    public static final String getMyAccountURL(String myAccountUrl) {

        if (StringUtils.isNotBlank(myAccountUrl)) {
            return myAccountUrl;
        }
        try {
            return ServiceURLBuilder.create().addPath(MY_ACCOUNT_APP_PATH).build()
                    .getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw new IdentityRuntimeException(
                    "Error while building url for context: " + MY_ACCOUNT_APP_PATH);
        }
    }

    /**
     * Returns the console url.
     *
     * @param consoleUrl  console url
     * @return configured url or the default url if configured url is empty
     */
    public static final String getConsoleURL(String consoleUrl) {

        if (StringUtils.isNotBlank(consoleUrl)) {
            return consoleUrl;
        }
        try {
            return ServiceURLBuilder.create().addPath(CONSOLE_APP_PATH).build()
                    .getAbsolutePublicURL();
        } catch (URLBuilderException e) {
            throw new IdentityRuntimeException(
                    "Error while building url for context: " + CONSOLE_APP_PATH);
        }
    }

    /**
     * Updates the last accessed time of the session in the UserSessionStore.
     *
     * @param sessionContextKey     Session context cache entry identifier.
     * @param lastAccessedTime    New value of the last accessed time of the session.
     */
    public static void updateSessionLastAccessTimeMetadata(String sessionContextKey, Long lastAccessedTime) {

        if (FrameworkServiceDataHolder.getInstance().isUserSessionMappingEnabled()) {
            try {
                UserSessionStore.getInstance().updateSessionMetaData(sessionContextKey, SessionMgtConstants
                        .LAST_ACCESS_TIME, Long.toString(lastAccessedTime));
            } catch (UserSessionException e) {
                log.error("Updating session meta data failed.", e);
            }
        }
    }

    /**
     * Returns the hash value of the cookie.
     *
     * @param cookie    Cookie to be hashed.
     * @return          Hash value of cookie.
     */
    public static String getHashOfCookie(Cookie cookie) {

        if (cookie != null) {
            String cookieValue = cookie.getValue();
            if (cookieValue != null) {
                return DigestUtils.sha256Hex(cookieValue);
            }
        }
        return null;
    }

    /**
     * Get correlation id of current thread.
     *
     * @return correlation-id.
     */
    public static String getCorrelation() {

        String ref;
        if (isCorrelationIDPresent()) {
            ref = MDC.get(CORRELATION_ID_MDC);
        } else {
            ref = UUID.randomUUID().toString();
        }
        return ref;
    }

    /**
     * Check whether correlation id present in the log MDC.
     *
     * @return True if correlation id present in the log MDC.
     */
    public static boolean isCorrelationIDPresent() {

        return MDC.get(CORRELATION_ID_MDC) != null;
    }

    /**
     * Remove the ALOR cookie used by the auto login flow in the first time authentication framework receives
     * the request. Regardless of the authentication state, this cookie will be cleared to make sure it won't be reused.
     *
     * @param request HttpServletRequest
     * @param response  HttpServletRequest.
     */
    public static void removeALORCookie(HttpServletRequest request, HttpServletResponse response) {

        if (request == null || request.getCookies() == null || request.getCookies().length == 0) {
            return;
        }

        Arrays.stream(request.getCookies())
                .filter(cookie -> FrameworkConstants.AutoLoginConstant.COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .ifPresent((cookie -> {
                    try {
                        String decodedValue = new String(Base64.getDecoder().decode(cookie.getValue()));
                        JSONObject cookieValueJSON = new JSONObject(decodedValue);
                        String content = (String) cookieValueJSON.get(FrameworkConstants.AutoLoginConstant.CONTENT);
                        JSONObject contentJSON = new JSONObject(content);
                        String domainInCookie = (String) contentJSON.get(FrameworkConstants.AutoLoginConstant.DOMAIN);
                        if (StringUtils.isNotEmpty(domainInCookie)) {
                            cookie.setDomain(domainInCookie);
                        }
                    } catch (Exception e) {
                        // Resolving the domain from the cookie failed. But we will try to to delete the cookie.
                        if (log.isDebugEnabled()) {
                            log.debug("Resolving the domain from the ALOR cookie failed.");
                        }
                    }
                    cookie.setMaxAge(0);
                    cookie.setValue("");
                    cookie.setPath("/");
                    response.addCookie(cookie);
                }));
    }

    /*
    TODO: This needs to be refactored so that there is a separate context object for each authentication step,
     rather than resetting.
    */
    /**
     * Reset authentication context.
     *
     * @param context Authentication Context.
     * @throws FrameworkException
     */
    public static void resetAuthenticationContext(AuthenticationContext context) {

        context.setSubject(null);
        context.setStateInfo(null);
        context.setExternalIdP(null);
        context.setAuthenticatorProperties(new HashMap<String, String>());
        context.setRetryCount(0);
        context.setRetrying(false);
        context.setCurrentAuthenticator(null);
    }

    /**
     * Check whether the JIT provisioning enhanced feature is enabled.
     *
     * @return true if the JIT provisioning enhanced features is enabled else return false.
     */
    public static boolean isJITProvisionEnhancedFeatureEnabled() {

        return Boolean.parseBoolean(IdentityUtil.
                    getProperty(FrameworkConstants.ENABLE_JIT_PROVISION_ENHANCE_FEATURE));
    }

    /**
     * Return a filtered list of requested scope claims.
     *
     * @param claimListOfScopes Claims list of requested scopes.
     * @param claimMappings     Claim mappings list of service provider.
     * @throws ClaimManagementException
     */
    public static List<ClaimMapping> getFilteredScopeClaims(List<String> claimListOfScopes,
                                                            List<ClaimMapping> claimMappings, String tenantDomain)
            throws ClaimManagementException {

        List<String> claimMappingListOfScopes = new ArrayList<>();
        try {
            RealmService realmService = FrameworkServiceDataHolder.getInstance().getRealmService();
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm realm = (UserRealm) realmService.getTenantUserRealm(tenantId);
            ClaimManager claimManager = realm.getClaimManager();

            if (claimManager != null) {
                for (String claim : claimListOfScopes) {
                    org.wso2.carbon.user.api.ClaimMapping currentMapping = claimManager.getClaimMapping(claim);
                    if (currentMapping != null && currentMapping.getClaim() != null &&
                            currentMapping.getClaim().getClaimUri() != null) {
                        claimMappingListOfScopes.add(currentMapping.getClaim().getClaimUri());
                    } else {
                        throw new ClaimManagementException("No claim mapping are found for claim :" +
                                claim + " in :" + tenantDomain);
                    }
                }
            }
        } catch (UserStoreException e) {
            throw new ClaimManagementException("Error while trying retrieve user claims for tenant domain: " +
                    tenantDomain, e);
        }

        List<ClaimMapping> requestedScopeClaims = new ArrayList<>();
        for (ClaimMapping claim : claimMappings) {
            if (claimMappingListOfScopes.contains(claim.getLocalClaim().getClaimUri())) {
                requestedScopeClaims.add(claim);
            }
        }
        if (LoggerUtils.isDiagnosticLogsEnabled()) {
            DiagnosticLog.DiagnosticLogBuilder diagnosticLogBuilder = new DiagnosticLog.DiagnosticLogBuilder(
                    FrameworkConstants.LogConstants.AUTHENTICATION_FRAMEWORK,
                    FrameworkConstants.LogConstants.ActionIDs.PROCESS_CLAIM_CONSENT);
            diagnosticLogBuilder.resultMessage("Filter Claims by OIDC Scopes.")
                    .inputParam("available user attributes", claimListOfScopes)
                    .inputParam("available claims for scopes", claimMappingListOfScopes)
                    .resultStatus(DiagnosticLog.ResultStatus.SUCCESS)
                    .logDetailLevel(DiagnosticLog.LogDetailLevel.APPLICATION);
            LoggerUtils.triggerDiagnosticLogEvent(diagnosticLogBuilder);
        }
        return requestedScopeClaims;
    }

    /**
     * Util function to build caller patch redirect URLs using ServiceURLBuilder.
     *
     * @param callerPath        Application caller path.
     * @param context           Authentication context.
     * @return  redirect URL.
     * @throws URLBuilderException  throw if an error occurred during URL generation.
     */
    public static String buildCallerPathRedirectURL(String callerPath, AuthenticationContext context)
            throws URLBuilderException {

        String serviceProvider = null;
        if (context.getSequenceConfig() != null && context.getSequenceConfig().getApplicationConfig() != null) {
            serviceProvider = context.getSequenceConfig().getApplicationConfig().getApplicationName();
        }
        /*
         Skip My Account and Console application redirections to use ServiceURLBuilder for URL generation
         since custom domain branding capabilities are not provided for them.
         */
        if (!(MY_ACCOUNT_APP.equals(serviceProvider) || CONSOLE_APP.equals(serviceProvider))) {
            if (callerPath != null && callerPath.startsWith(FrameworkConstants.TENANT_CONTEXT_PREFIX)) {
                String callerTenant = callerPath.split("/")[2];
                String callerPathWithoutTenant = callerPath.replaceFirst("/t/[^/]+/", "/");
                String redirectURL = ServiceURLBuilder.create().addPath(callerPathWithoutTenant)
                        .setTenant(callerTenant, true).build().getAbsolutePublicURL();
                return redirectURL;
            } else if (callerPath != null && callerPath.startsWith(FrameworkConstants.ORGANIZATION_CONTEXT_PREFIX)) {
                String callerOrgId = callerPath.split("/")[2];
                String callerPathWithoutOrgId = callerPath.replaceFirst("/o/[^/]+/", "/");
                String redirectURL = ServiceURLBuilder.create().addPath(callerPathWithoutOrgId)
                        .setTenant(context.getLoginTenantDomain()).setOrganization(callerOrgId)
                        .build().getAbsolutePublicURL();
                return redirectURL;
            }
        }
        return callerPath;
    }

    /**
     * Util function to check whether using authenticator name to resolve authenticatorConfig in adaptive scripts
     * is enabled. If not authenticator display name is used.
     *
     * @return boolean indicating server config preference.
     */
    public static boolean isAuthenticatorNameInAuthConfigEnabled() {

        if (authenticatorNameInAuthConfigPreference == null) {
            authenticatorNameInAuthConfigPreference = Boolean.parseBoolean(IdentityUtil.getProperty(
                    AUTHENTICATOR_NAME_IN_AUTH_CONFIG));
        }

        return authenticatorNameInAuthConfigPreference;
    }

    /**
     * Util method to check whether authentication context expiry validation is enabled.
     *
     * @return boolean indicating whether the validation is enabled.
     */
    public static boolean isAuthenticationContextExpiryEnabled() {

        return Boolean.parseBoolean(IdentityUtil.getProperty(AUTHENTICATION_CONTEXT_EXPIRY_VALIDATION));
    }

    /**
     * Util method to check whether Identifier First is initiated from an authenticator.
     *
     * @param context Authentication context.
     * @return boolean indicating whether the IDF is initiated from an authenticator.
     */
    public static boolean isIdfInitiatedFromAuthenticator(AuthenticationContext context) {

        return Boolean.TRUE.equals(context.getProperty(IS_IDF_INITIATED_FROM_AUTHENTICATOR));
    }

    /**
     * Util method to check whether the user is resolved.
     *
     * @param context Authentication context.
     * @return boolean indicating whether the user is resolved.
     */
    public static boolean getIsUserResolved(AuthenticationContext context) {

        boolean isUserResolved = false;
        if (!context.getProperties().isEmpty() &&
                context.getProperty(FrameworkConstants.IS_USER_RESOLVED) != null) {
            isUserResolved = (boolean) context.getProperty(FrameworkConstants.IS_USER_RESOLVED);
        }
        return isUserResolved;
    }

    /**
     * This method checks if all the authentication steps up to now have been performed by authenticators that
     * implements AuthenticationFlowHandler interface. If so, it returns true.
     * AuthenticationFlowHandlers may not perform actual authentication though the authenticated user is set in the
     * context. Hence, this method can be used to determine if the user has been authenticated by a previous step.
     *
     * @param context   AuthenticationContext.
     * @return True if all the authentication steps up to now have been performed by AuthenticationFlowHandlers.
     */
    public static boolean isPreviousIdPAuthenticationFlowHandler(AuthenticationContext context) {

        Map<String, AuthenticatedIdPData> currentAuthenticatedIdPs = context.getCurrentAuthenticatedIdPs();
        return currentAuthenticatedIdPs != null && !currentAuthenticatedIdPs.isEmpty() &&
                currentAuthenticatedIdPs.values().stream().filter(Objects::nonNull)
                        .map(AuthenticatedIdPData::getAuthenticators).filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .allMatch(authenticator ->
                                authenticator.getApplicationAuthenticator() instanceof AuthenticationFlowHandler);
    }

    public static JsBaseGraphBuilderFactory createJsGraphBuilderFactoryFromConfig() {

        JsGenericGraphBuilderFactory jsGenericGraphBuilderFactory = createJsGenericGraphBuilderFactoryFromConfig();
        if (jsGenericGraphBuilderFactory instanceof JsBaseGraphBuilderFactory) {
            return (JsBaseGraphBuilderFactory) jsGenericGraphBuilderFactory;
        }
        return null;
    }

    public static JsGenericGraphBuilderFactory createJsGenericGraphBuilderFactoryFromConfig() {

        String scriptEngineName = IdentityUtil.getProperty(FrameworkConstants.SCRIPT_ENGINE_CONFIG);
        if (scriptEngineName != null) {
            if (StringUtils.equalsIgnoreCase(FrameworkConstants.GRAAL_JS, scriptEngineName)) {
                return new JsGraalGraphBuilderFactory();
            } else if (StringUtils.equalsIgnoreCase(FrameworkConstants.OPENJDK_NASHORN, scriptEngineName)) {
                return new JsOpenJdkNashornGraphBuilderFactory();
            } else if (StringUtils.equalsIgnoreCase(FrameworkConstants.NASHORN, scriptEngineName)) {
                return new JsGraphBuilderFactory();
            }
        }
        // Config is not set. Hence going with class for name approach.
        try {
            Class.forName(GRAALJS_SCRIPTER_CLASS_NAME);
            return new JsGraalGraphBuilderFactory();
        } catch (ClassNotFoundException e) {
            try {
                Class.forName(OPENJDK_SCRIPTER_CLASS_NAME);
                return new JsOpenJdkNashornGraphBuilderFactory();
            } catch (ClassNotFoundException classNotFoundException) {
                try {
                    Class.forName(JDK_SCRIPTER_CLASS_NAME);
                    return new JsGraphBuilderFactory();
                } catch (ClassNotFoundException ex) {
                    return null;
                }
            }
        }
    }

    /**
     * This method will check whether the authentication flow is API based or not.
     *
     * @param request Http servlet request.
     * @return True if the authentication flow is API based.
     */
    public static boolean isAPIBasedAuthenticationFlow(HttpServletRequest request) {

        return Boolean.TRUE.equals(request.getAttribute(FrameworkConstants.IS_API_BASED_AUTH_FLOW));
    }

    /**
     * Create a shallow copy of the input Identity Provider.
     *
     * @param idP Identity Provider.
     * @return Clone of IDP.
     */
    public static IdentityProvider createIdPClone(IdentityProvider idP) throws FrameworkException {

        ObjectOutputStream objOutPutStream;
        ObjectInputStream objInputStream;
        IdentityProvider newObject;
        try {
            ByteArrayOutputStream byteArrayOutPutStream = new ByteArrayOutputStream();
            objOutPutStream = new ObjectOutputStream(byteArrayOutPutStream);
            objOutPutStream.writeObject(idP);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutPutStream.toByteArray());
            objInputStream = new ObjectInputStream(byteArrayInputStream);
            newObject = (IdentityProvider) objInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new FrameworkException("Error deep cloning IDP object.", e);
        }
        return newObject;
    }

    /**
     * Create a shallow copy of the input Service Provider.
     *
     * @param serviceProvider Service Provider.
     * @return Clone of Application.
     */
    public static ServiceProvider createSPClone(ServiceProvider serviceProvider) throws FrameworkException {

        ObjectOutputStream objOutPutStream;
        ObjectInputStream objInputStream;
        ServiceProvider newObject;
        try {
            ByteArrayOutputStream byteArrayOutPutStream = new ByteArrayOutputStream();
            objOutPutStream = new ObjectOutputStream(byteArrayOutPutStream);
            objOutPutStream.writeObject(serviceProvider);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutPutStream.toByteArray());
            objInputStream = new ObjectInputStream(byteArrayInputStream);
            newObject = (ServiceProvider) objInputStream.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new FrameworkException("Error deep cloning application object.", e);
        }
        return newObject;
    }

    /**
     * Get claim properties of a claim in a given tenant.
     *
     * @param tenantDomain The tenant domain.
     * @param claimURI     Claim URI.
     * @return Properties of the claim.
     */
    private static Map<String, String> getClaimProperties(String tenantDomain, String claimURI) {

        try {
            List<LocalClaim> localClaims = FrameworkServiceDataHolder.getInstance()
                    .getClaimMetadataManagementService().getLocalClaims(tenantDomain);
            if (localClaims == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Returned claim list from ClaimManagementService is null");
                }
                return Collections.emptyMap();
            }
            for (LocalClaim localClaim : localClaims) {
                if (StringUtils.equalsIgnoreCase(claimURI, localClaim.getClaimURI())) {
                    return localClaim.getClaimProperties();
                }
            }
        } catch (ClaimMetadataException e) {
            log.error("Error while retrieving local claim meta data.", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Encrypt the given plain text.
     *
     * @param plainText The plaintext value to be encrypted and base64 encoded
     * @return Base64 encoded string
     * @throws CryptoException On error during encryption
     */
    public static String encrypt(String plainText) throws CryptoException {

        String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        // Get custom key from server configuration.
        String customKey = null;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            customKey = ServerConfiguration.getInstance().getFirstProperty(FrameworkConstants.TOTP_KEY);
        }
        return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(
                plainText.getBytes(StandardCharsets.UTF_8), customKey);
    }

    /**
     * Process the claimValue of the given claimURI and encrypt if required.
     *
     * @param claimURI     Claim URI.
     * @param claimValue   Claim value.
     * @param tenantDomain The tenant domain.
     * @return Processed claim value.
     * @throws FrameworkException On error during encryption process.
     */
    public static String getProcessedClaimValue(String claimURI, String claimValue, String tenantDomain)
            throws FrameworkException {

        Map<String, String> claimProperties = getClaimProperties(tenantDomain, claimURI);
        try {
            if (claimProperties.containsKey(FrameworkConstants.ENABLE_ENCRYPTION)) {
                return claimValue;
            }
            if (StringUtils.isBlank(claimValue)) {
                return claimValue;
            }
            return encrypt(claimValue);
        } catch (CryptoException e) {
            throw new FrameworkException("Error occurred while encrypting claim value of: " + claimURI, e);
        }
    }

    /**
     * This method return true if the given URL is relative URL.
     *
     * @param uriString
     * @return true if the given URL is relative URL.
     * @throws URISyntaxException
     */
    public static boolean isURLRelative(String uriString) throws URISyntaxException {

        return !new URI(uriString).isAbsolute();
    }
}
