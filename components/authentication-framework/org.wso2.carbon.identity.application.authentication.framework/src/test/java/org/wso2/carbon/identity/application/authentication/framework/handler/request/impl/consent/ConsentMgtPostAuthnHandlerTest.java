/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.ApplicationConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.consent.exception.SSOConsentServiceException;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.central.log.mgt.utils.LoggerUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Covers the claim filtering this handler applies before and after consent, and the configuration that turns
 * it off. The handler returns early for OIDC and OAuth2, so these paths are the ones the remaining protocols
 * take, SAML among them.
 */
public class ConsentMgtPostAuthnHandlerTest {

    private static final String REQUESTED_CLAIM_URI = "http://wso2.org/claims/givenname";
    private static final String RESOLVED_CLAIM_URI = "http://wso2.org/claims/country";

    @Mock
    private AuthenticationContext context;
    @Mock
    private SequenceConfig sequenceConfig;
    @Mock
    private ApplicationConfig applicationConfig;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private SSOConsentService ssoConsentService;
    @Mock
    private FrameworkServiceDataHolder dataHolder;

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @DataProvider(name = "legacyHandlingDataProvider")
    public Object[][] legacyHandlingData() {

        return new Object[][]{
                // flag on the context, whether the unresolved claim should survive the filter
                {null, false},
                {Boolean.FALSE, false},
                {Boolean.TRUE, true},
        };
    }

    /**
     * A claim the service provider requested but which resolved to no value is dropped from the consent page,
     * unless the federated claims were handled the way releases before IS 6.0.0 handled them.
     */
    @Test(dataProvider = "legacyHandlingDataProvider")
    public void testPreConsentKeepsUnresolvedClaimOnlyWhenFilteringIsDisabled(Object flag, boolean shouldSurvive)
            throws Exception {

        ConsentClaimsData consentClaimsData = consentDataWithOneRequestedClaim();
        invokePreConsent(flag, consentClaimsData);

        Assert.assertEquals(consentClaimsData.getRequestedClaims().size(), shouldSurvive ? 1 : 0,
                "Requested claim survival did not match the configuration on the context.");
    }

    /**
     * The same filter runs again when the approved consent is recorded. Dropping the claim there leaves the
     * receipt without the claim the user was asked about, so the user is asked again on the next login.
     */
    @Test(dataProvider = "legacyHandlingDataProvider")
    public void testPostConsentKeepsUnresolvedClaimOnlyWhenFilteringIsDisabled(Object flag, boolean shouldSurvive)
            throws Exception {

        ConsentClaimsData consentClaimsData = consentDataWithOneRequestedClaim();
        invokeRemovalGuard("handlePostConsent", flag, consentClaimsData);

        Assert.assertEquals(consentClaimsData.getRequestedClaims().size(), shouldSurvive ? 1 : 0,
                "Requested claim survival on the recording path did not match the configuration.");
    }

    /**
     * The flag is only honoured as a Boolean true. Anything else leaves the filter in place, so an unrelated
     * value on the context cannot switch the behaviour on by accident.
     */
    @Test
    public void testFlagIsHonouredOnlyAsBooleanTrue() throws Exception {

        Method helper = ConsentMgtPostAuthnHandler.class
                .getDeclaredMethod("isLegacyFederatedClaimHandlingEnabled", AuthenticationContext.class);
        helper.setAccessible(true);
        ConsentMgtPostAuthnHandler handler = new ConsentMgtPostAuthnHandler();

        for (Object value : new Object[]{null, Boolean.FALSE, "true", "TRUE", 1}) {
            when(context.getProperty(FrameworkConstants.LEGACY_FEDERATED_CLAIM_HANDLING))
                    .thenReturn(value);
            Assert.assertFalse((Boolean) helper.invoke(handler, context),
                    "Filtering was turned off by a context value that is not Boolean true: " + value);
        }
        when(context.getProperty(FrameworkConstants.LEGACY_FEDERATED_CLAIM_HANDLING))
                .thenReturn(Boolean.TRUE);
        Assert.assertTrue((Boolean) helper.invoke(handler, context));
        Assert.assertFalse((Boolean) helper.invoke(handler, (AuthenticationContext) null),
                "A missing context must leave the filter in place.");
    }

    private ConsentClaimsData consentDataWithOneRequestedClaim() {

        ClaimMetaData unresolved = new ClaimMetaData();
        unresolved.setClaimUri(REQUESTED_CLAIM_URI);
        List<ClaimMetaData> requested = new ArrayList<>();
        requested.add(unresolved);

        ConsentClaimsData data = new ConsentClaimsData();
        data.setRequestedClaims(requested);
        data.setMandatoryClaims(new ArrayList<>());
        return data;
    }

    private void invokePreConsent(Object flag, ConsentClaimsData consentClaimsData) throws Exception {

        invokeRemovalGuard("handlePreConsent", flag, consentClaimsData);
    }

    /**
     * Drives the handler far enough to reach the claim removal and asserts on the consent data it mutated.
     */
    private void invokeRemovalGuard(String methodName, Object flag, ConsentClaimsData consentClaimsData)
            throws Exception {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("samlsp");

        AuthenticatedUser user = new AuthenticatedUser();
        user.setUserName("federateduser");
        user.setTenantDomain("carbon.super");
        user.setUserStoreDomain("PRIMARY");
        user.setAuthenticatedSubjectIdentifier("federateduser");
        // The requested claim resolved to nothing; a different claim did, so the attribute map is not empty.
        Map<ClaimMapping, String> attributes = new HashMap<>();
        attributes.put(ClaimMapping.build(RESOLVED_CLAIM_URI, RESOLVED_CLAIM_URI, null, false), "sri lanka");
        user.setUserAttributes(attributes);

        when(context.getSequenceConfig()).thenReturn(sequenceConfig);
        when(sequenceConfig.getApplicationConfig()).thenReturn(applicationConfig);
        when(applicationConfig.getApplicationName()).thenReturn("samlsp");
        when(applicationConfig.getClaimMappings()).thenReturn(Collections.emptyMap());
        when(applicationConfig.getServiceProvider()).thenReturn(serviceProvider);
        when(sequenceConfig.getAuthenticatedUser()).thenReturn(user);
        when(context.getProperty(FrameworkConstants.LEGACY_FEDERATED_CLAIM_HANDLING)).thenReturn(flag);
        // The recording path reads the approval off the request and takes the consent data from the context.
        when(request.getParameter("consent")).thenReturn("approve");
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(context.getParameter("consentClaimMetaData")).thenReturn(consentClaimsData);

        try (MockedStatic<FrameworkServiceDataHolder> holder = mockStatic(FrameworkServiceDataHolder.class);
             MockedStatic<LoggerUtils> loggerUtils = mockStatic(LoggerUtils.class)) {
            loggerUtils.when(LoggerUtils::isDiagnosticLogsEnabled).thenReturn(false);
            holder.when(FrameworkServiceDataHolder::getInstance).thenReturn(dataHolder);
            when(dataHolder.getSSOConsentService()).thenReturn(ssoConsentService);
            when(ssoConsentService.getConsentRequiredClaimsWithExistingConsents(any(), any()))
                    .thenReturn(consentClaimsData);
            when(ssoConsentService.getConsentRequiredClaimsWithoutExistingConsents(any(), any()))
                    .thenReturn(consentClaimsData);

            Method method = ConsentMgtPostAuthnHandler.class.getDeclaredMethod(
                    methodName, HttpServletRequest.class, HttpServletResponse.class, AuthenticationContext.class);
            method.setAccessible(true);
            try {
                method.invoke(new ConsentMgtPostAuthnHandler(), request, response, context);
            } catch (Exception e) {
                // Not swallowed: the claim removal sits early in both methods, so anything thrown means the
                // case never reached what it is asserting on.
                throw new RuntimeException(methodName + " failed before reaching the claim removal", e);
            }
        } catch (SSOConsentServiceException e) {
            // Declared by the stubbed service methods; reaching it means the case asserted nothing.
            throw new IllegalStateException("Consent service stubbing failed", e);
        }
    }
}
