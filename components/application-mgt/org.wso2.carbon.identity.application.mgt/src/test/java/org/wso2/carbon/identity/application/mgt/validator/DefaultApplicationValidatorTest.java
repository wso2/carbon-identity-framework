/*
 * Copyright (c) 2021-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.mgt.validator;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.DiscoverableGroup;
import org.wso2.carbon.identity.application.common.model.GroupBasicInfo;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.RequestPathAuthenticatorConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpTrustedAppMetadata;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.identity.application.mgt.internal.ApplicationManagementServiceComponentHolder;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreClientException;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.wso2.carbon.base.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.ErrorMessage.ERROR_CHECKING_GROUP_EXISTENCE;
import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.TRUSTED_APP_CONSENT_REQUIRED_PROPERTY;
import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID;

/**
 * Test class for DefaultApplicationValidator.
 */
public class DefaultApplicationValidatorTest {

    private static final String DEFAULT_USER_STORE_DOMAIN = "PRIMARY";
    private static final String USERNAME = "test-user";
    private static final String USER_ID = "test-user-id";

    MockedStatic<IdentityTenantUtil> mockIdentityTenantUtil;
    MockedStatic<ApplicationManagementServiceComponentHolder> mockedApplicationManagementServiceComponentHolder;
    ApplicationManagementServiceComponentHolder mockComponentHolder;
    UserRealm mockUserRealm;
    RealmService mockRealmService;
    AbstractUserStoreManager mockAbstractUserStoreManager;

    /**
     * Setup the test environment for the test class.
     */
    @BeforeClass
    public void setup() throws org.wso2.carbon.user.api.UserStoreException {

        mockIdentityTenantUtil = mockStatic(IdentityTenantUtil.class);
        mockedApplicationManagementServiceComponentHolder =
                mockStatic(ApplicationManagementServiceComponentHolder.class);
        mockComponentHolder = mock(ApplicationManagementServiceComponentHolder.class);
        mockUserRealm = mock(UserRealm.class);
        mockRealmService = mock(RealmService.class);
        mockAbstractUserStoreManager = mock(AbstractUserStoreManager.class);
        setupInitConfigurations();
    }

    @AfterClass
    public void end() {

        mockIdentityTenantUtil.close();
        mockedApplicationManagementServiceComponentHolder.close();
    }

    @DataProvider(name = "validateAdaptiveAuthScriptDataProvider")
    public Object[][] validateAdaptiveAuthScriptDataProvider() {

        String scripOne = "var onLoginRequest = function(context) {\n" +
                "    \tfor (var step = 0; step < 5; step++) {\n" +
                "  \t\tLog.info('Walking east one step');\n" +
                "\t} \n" +
                "    executeStep(1);\n" +
                "};\n";
        String scripTwo = "var onLoginRequest = function(context) {for(var step = 0; step < 5; step++) {\n" +
                "  \t\tLog.info('Walking east one step');\n" +
                "\t} \n" +
                "    executeStep(1);\n" +
                "};\n";
        String scripThree = "var onLoginRequest = function(context) {for(var step = 0; step < 5; step++) {" +
                "Log.info('Walking east one step');} executeStep(1);};";
        String scriptFour = "var onLoginRequest = function(context) {\n" +
                "    var n = 0;\n" +
                "\tvar x = 0;\n" +
                "\tvar abc = 'abc';while (n < 3) {\n" +
                "\t  n++;\n" +
                "\t  x += n;\n" +
                "\t}\n" +
                "    executeStep(1);\n" +
                "};\n";
        String scriptFive = "var onLoginRequest = function(context) {\n" +
                "var i = 0;\n" +
                "do {\n" +
                "  i += 1;\n" +
                "  console.log(i);\n" +
                "}while(i < 5);\n" +
                "    executeStep(1);\n" +
                "};\n";
        String scripSix = "var onLoginRequest = function(context) {\n" +
                "    var forceAuth = true;\n" +
                "    var therefore = true;\n" +
                "    var fivefor = true;\n" +
                "    var for_auth = true;\n" +
                "    var _for = true;\n" +
                "    var whilefor = true;\n" +
                "    var forwhile = true;\n" +
                "    var some_whiles = true;\n" +
                "    var fore = true;\n" +
                "    var foreach = true;\n" +
                "    var foreaches = true;\n" +
                "    executeStep(1);\n" +
                "};\n";

        String scriptSeven = "var onLoginRequest = function(context) {\n" +
                "    var array1 = ['a', 'b', 'c'];\n" +
                "    array1.forEach(element => console.log(element));\n" +
                "    executeStep(1);\n" +
                "};\n";

        String scriptEight = "// This script will only allow for login to application while the user's age is over " +
                "configured value\n// The user will  be redirected to an error page if the date of birth is not " +
                "present or user is below configured var onLoginRequest = function(context) {for(var step = 0;" +
                " step < 5; step++) {Log.info('Walking east one step');} executeStep(1);} value\n\nvar ageLimit" +
                " = 18;\n\n// Error page to redirect " +
                "unauthorized users,\n// can be either an absolute url or relative url to server root, or " +
                "empty/null\n// null/empty value will redirect to the default error page\nvar errorPage = '';\n\n// " +
                "Additional query params to be added to the above url.\n// Hint: Use i18n keys for error " +
                "messages\nvar errorPageParameters = {\n 'status': 'Unauthorized',\n    'statusMsg': \"You need to " +
                "be over \" + ageLimit + \" for login to this application.\"\n};\n\n/*\nHi am a multi line comment " +
                "1\n*/\n// Date of birth attribute at the client side\nvar dateOfBirthClaim = 'http://wso2" +
                ".org/claims/dob';\n\n// The validator function for DOB. Default validation check if the DOB is in " +
                "YYYY-MM-dd format\nvar validateDOB = function (dob) {\n    return dob.match(/^(\\d{4})-(\\d{2})-" +
                "(\\d{2})$/);\n};\n\nvar onLoginRequest = function(context) {\n    executeStep(1, {\n        " +
                "onSuccess: function (context) {\n            var underAge = true;\n            // Extracting user " +
                "store domain of authenticated subject from the first step\n            var dob = context" +
                ".currentKnownSubject.localClaims[dateOfBirthClaim];// Adding a single line comment with for while " +
                "foreach \n            Log.debug('DOB for user ' + context.currentKnownSubject.identifier + ' is : " +
                "' + dob);\n Log.debug('while for forEach ');\n if (dob && validateDOB(dob)) {\n var birthDate = new " +
                "Date(dob);\n   if (getAge(birthDate) >= ageLimit) {\n                    underAge = false;\n       " +
                "         }\n            }\n            if (underAge === true) {\n                Log.debug('User ' +" +
                " context.currentKnownSubject.identifier + ' is under aged. Hence denied to login.');\n              " +
                "  sendError(errorPage, errorPageParameters);\n            }\n        }\n    });\n};\n/*\n* Hi am a " +
                "multi line comment 2\n*/\nvar getAge = function(birthDate) {\n    var /*Adding a multiline comment " +
                "with for while foreach*/today = new Date();\n    var age = today.getFullYear() - birthDate" +
                ".getFullYear();\n    var m = today.getMonth()/* Adding a multiline comment with for while foreach */" +
                " - birthDate.getMonth();\n    if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {\n  " +
                "      age--;\n    }\n    return age;\n};";

        return new String[][]{
                // isValidationFailScenario, isLoopsAllowed, script
                {"true", "false", scripOne},
                {"true", "false", scripTwo},
                {"true", "false", scripThree},
                {"true", "false", scriptFour},
                {"true", "false", scriptFive},
                {"true", "false", scriptFour},
                {"true", "false", scriptSeven},
                {"false", "false", scripSix},
                {"false", "true", scripOne},
                {"false", "true", scriptFive},
                {"false", "false", scriptEight}
        };
    }

    @Test(dataProvider = "validateAdaptiveAuthScriptDataProvider")
    public void validateAdaptiveAuthScriptTest(String isValidationFailScenario, String isLoopsAllowed, String script)
            throws Exception {

        DefaultApplicationValidator defaultApplicationValidator = new DefaultApplicationValidator();

        Field configuration = IdentityUtil.class.getDeclaredField("configuration");
        configuration.setAccessible(true);
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("AdaptiveAuth.AllowLoops", isLoopsAllowed);
        configuration.set(IdentityUtil.class, configMap);

        Method validateAdaptiveAuthScript = DefaultApplicationValidator.class.getDeclaredMethod(
                "validateAdaptiveAuthScript", List.class, AuthenticationScriptConfig.class);
        validateAdaptiveAuthScript.setAccessible(true);

        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
        scriptConfig.setContent(script);

        List<String> validationErrors = new ArrayList<>();
        validateAdaptiveAuthScript.invoke(defaultApplicationValidator, validationErrors, scriptConfig);

        if (Boolean.parseBoolean(isValidationFailScenario)) {
            Assert.assertFalse(validationErrors.isEmpty(), "This is an invalid scenario. There should be " +
                    "validation messages.");

            List<String> filtered = validationErrors.stream()
                    .filter(error -> StringUtils.containsIgnoreCase(error, "loop"))
                    .collect(Collectors.toList());
            Assert.assertFalse(filtered.isEmpty(), "There should be a validation message related to loops");
        } else {
            Assert.assertTrue(validationErrors.isEmpty(), "There are validation messages. This is a valid case " +
                    "there should not be any validation messages. Validation messages: " +
                    String.join("|", validationErrors));
        }
    }

    @DataProvider(name = "validateTrustedAppMetadataDataProvider")
    public Object[][] validateTrustedAppMetadataDataProvider() {

        String[] validThumbprints = {"thumbprint1", "thumbprint2"};
        String[] inValidThumbprints = Collections.nCopies(21, "thumbprint").toArray(new String[0]);

        return new Object[][]{
                // Valid scenario with both android and iOS configurations.
                {"com.wso2.sample", validThumbprints, "sample.app.id", false, false, false},
                // Valid scenario with only android configurations.
                {"com.wso2.sample", validThumbprints, "", false, false, false},
                // Valid scenario with only iOS configurations.
                {"", new String[0], "sample.app.id", false, false, false},
                // Invalid scenario without both android and iOS configurations.
                {"", new String[0], "", false, false, true},
                // Invalid scenario with invalid thumbprints.
                {"com.wso2.sample", inValidThumbprints, "sample.app.id", false, false, true},
                // Valid scenario with consent required config enabled.
                {"com.wso2.sample", validThumbprints, "sample.app.id", true, true, false},
                // Invalid scenario with consent required config enabled and consent not granted.
                {"com.wso2.sample", validThumbprints, "sample.app.id", true, false, true},
                // Invalid scenario with empty android package name and non-empty thumbprints.
                {"", validThumbprints, "sample.app.id", true, false, true},
                // Invalid scenario with non-empty android package name and empty thumbprints.
                {"com.wso2.sample", new String[0], "sample.app.id", true, false, true}
        };
    }

    @Test(dataProvider = "validateTrustedAppMetadataDataProvider")
    public void testValidateTrustedAppMetadata(String androidPackageName, String[] thumbprints, String appleAppId,
                                               boolean consentRequired, boolean consentGranted,
                                               boolean isValidationFailScenario)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        ServiceProvider sp = new ServiceProvider();
        SpTrustedAppMetadata spTrustedAppMetadata = new SpTrustedAppMetadata();
        spTrustedAppMetadata.setIsFidoTrusted(true);
        spTrustedAppMetadata.setIsConsentGranted(consentGranted);
        spTrustedAppMetadata.setAndroidPackageName(androidPackageName);
        spTrustedAppMetadata.setAndroidThumbprints(thumbprints);
        spTrustedAppMetadata.setAppleAppId(appleAppId);
        sp.setTrustedAppMetadata(spTrustedAppMetadata);

        try (MockedStatic<IdentityUtil> identityUtil = Mockito.mockStatic(IdentityUtil.class)) {
            identityUtil.when(() -> IdentityUtil.getProperty(TRUSTED_APP_CONSENT_REQUIRED_PROPERTY)).
                    thenReturn(String.valueOf(consentRequired));

            List<String> validationErrors = new ArrayList<>();

            DefaultApplicationValidator defaultApplicationValidator = new DefaultApplicationValidator();
            Method validateTrustedAppMetadata = DefaultApplicationValidator.class.getDeclaredMethod(
                    "validateTrustedAppMetadata", List.class, ServiceProvider.class);
            validateTrustedAppMetadata.setAccessible(true);
            validateTrustedAppMetadata.invoke(defaultApplicationValidator, validationErrors, sp);

            if (isValidationFailScenario) {
                Assert.assertFalse(validationErrors.isEmpty(), "This is an invalid scenario. There should be " +
                        "validation messages.");
            } else {
                Assert.assertTrue(validationErrors.isEmpty(), "There are validation messages. This is a valid case " +
                        "there should not be any validation messages. Validation messages: " +
                        String.join("|", validationErrors));
            }
        }
    }

    @Test
    public void testValidateTrustedAppWithEmptyMetadataObj() throws NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        ServiceProvider sp = new ServiceProvider();
        List<String> validationErrors = new ArrayList<>();

        DefaultApplicationValidator defaultApplicationValidator = new DefaultApplicationValidator();
        Method validateTrustedAppMetadata = DefaultApplicationValidator.class.getDeclaredMethod(
                "validateTrustedAppMetadata", List.class, ServiceProvider.class);
        validateTrustedAppMetadata.setAccessible(true);
        validateTrustedAppMetadata.invoke(defaultApplicationValidator, validationErrors, sp);

        Assert.assertTrue(validationErrors.isEmpty(), "Validation should be skipped and there should not be any " +
                "error messages.");
    }

    @DataProvider(name = "validateApplicationVersionDataProvider")
    public Object[][] validateApplicationVersionDataProvider() {

        return new Object[][]{
                // version, valid status
                {"v2.0.0", "oauth2", true},
                {"v1.0.0", "oauth2", true},
                {"v0.0.0", "oauth2", true},
                {"v0.0.1", "oauth2", false},
                {"v0.1.1", "oauth2", false},
                {"v1.1.1", "oauth2", false},
                {"v2.0.1", "oauth2", false},
                {"dummy", "oauth2", false},
                {"v2.0.0", "samlsso", true},
                {"v1.0.0", "samlsso", true},
                {"v0.0.0", "samlsso", true},
                {"v0.0.1", "samlsso", false},
                {"v0.1.1", "samlsso", false},
                {"v1.1.1", "samlsso", false},
                {"v2.0.1", "samlsso", false},
                {"dummy", "samlsso", false},
                {"v2.0.0", null, true},
                {"v1.0.0", null, true},
                {"v0.0.0", null, true},
                {"v0.0.1", null, false},
                {"v0.1.1", null, false},
                {"v1.1.1", null, false},
                {"v2.0.1", null, false},
                {"dummy", null, false},

        };
    }

    @Test(dataProvider = "validateApplicationVersionDataProvider")
    public void testValidateApplicationVersion(String version, String authType, boolean isValid)
            throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {

        // Prepare the service provider object.
        ServiceProvider sp = new ServiceProvider();
        InboundAuthenticationConfig inboundAuthenticationConfig = new InboundAuthenticationConfig();
        if (authType != null) {
            InboundAuthenticationRequestConfig inboundAuthenticationRequestConfig
                    = new InboundAuthenticationRequestConfig();
            inboundAuthenticationRequestConfig.setInboundAuthType(authType);
            inboundAuthenticationConfig.setInboundAuthenticationRequestConfigs(
                    new InboundAuthenticationRequestConfig[]{ inboundAuthenticationRequestConfig});
            sp.setInboundAuthenticationConfig(inboundAuthenticationConfig);
        }
        sp.setApplicationVersion(version);
        List<String> validationErrors = new ArrayList<>();

        DefaultApplicationValidator defaultApplicationValidator = new DefaultApplicationValidator();
        Method validateApplicationVersion = DefaultApplicationValidator.class.getDeclaredMethod(
                "validateApplicationVersion", List.class, ServiceProvider.class);
        validateApplicationVersion.setAccessible(true);
        validateApplicationVersion.invoke(defaultApplicationValidator, validationErrors, sp);

        assertEquals(validationErrors.isEmpty(), isValid, "Valid app version has been introduced. " +
                "Please update the test case accordingly.");
    }

    @Test(description = "Validate error message for discoverable groups in a non-discoverable application")
    public void testValidateDiscoverableGroupsForNonDiscoverableApp() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            sp.setDiscoverable(false);
            sp.setDiscoverableGroups(
                    new DiscoverableGroup[] {getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 1, 0)});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 1);
                assertEquals(validationErrors.get(0),
                        "Discoverable groups are defined for a non-discoverable application.");
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate null user store in discoverable groups")
    public void testValidateNullUserStoreInDiscoverableGroups() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            sp.setDiscoverableGroups(new DiscoverableGroup[] {getNewDiscoverableGroup(null, 1, 0),
                    getNewDiscoverableGroup(null, 1, 0)});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 2);
                assertEquals(validationErrors.get(0),
                        "No user store defined for the discoverable groups indexed at 0.");
                assertEquals(validationErrors.get(1),
                        "No user store defined for the discoverable groups indexed at 1.");
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate non-existing user store in discoverable groups")
    public void testValidateNonExistingUserStoreInDiscoverableGroups() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager("non-existing")).thenReturn(null);
            sp.setDiscoverableGroups(new DiscoverableGroup[] {getNewDiscoverableGroup("non-existing", 1, 0),
                    getNewDiscoverableGroup("non-existing", 1, 0)});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 2);
                assertEquals(validationErrors.get(0),
                        "The provided user store: 'non-existing' is not found.");
                assertEquals(validationErrors.get(1),
                        "The provided user store: 'non-existing' is not found.");
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate empty or null groups list in discoverable groups")
    public void testValidateEmptyOrNullGroupsList() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            DiscoverableGroup discoverableGroup = getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 0, 0);
            sp.setDiscoverableGroups(new DiscoverableGroup[] { discoverableGroup });
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 1);
                assertEquals(validationErrors.get(0),
                        "No groups defined for the user store: 'PRIMARY' in the discoverable groups configuration.");
                discoverableGroup.setGroups(null);
                validationErrors = applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 1);
                assertEquals(validationErrors.get(0),
                        "No groups defined for the user store: 'PRIMARY' in the discoverable groups configuration.");
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate null group ids within discoverable groups")
    public void testValidateNullGroupIdsInDiscoverableGroups() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            DiscoverableGroup discoverableGroup = getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0);
            discoverableGroup.getGroups()[0].setId(null);
            discoverableGroup.getGroups()[1].setId(null);
            sp.setDiscoverableGroups(new DiscoverableGroup[] {discoverableGroup});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 2);
                assertEquals(validationErrors.get(0),
                        "Group ID is not defined for the group indexed at 0 for the user store: 'PRIMARY' in the" +
                                " discoverable groups configuration.");
                assertEquals(validationErrors.get(1),
                        "Group ID is not defined for the group indexed at 1 for the user store: 'PRIMARY' in the" +
                                " discoverable groups configuration.");
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate the error message when the group id is incorrect")
    public void testValidateIncorrectGroupId() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            DiscoverableGroup discoverableGroup = getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0);
            sp.setDiscoverableGroups(new DiscoverableGroup[] {discoverableGroup});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0"))).thenThrow(
                        new UserStoreClientException());
                when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-1"))).thenThrow(
                        new UserStoreClientException());
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 2);
                assertEquals(validationErrors.get(0), "No group found for the given group ID: 'test-group-id-0'.");
                assertEquals(validationErrors.get(1), "No group found for the given group ID: 'test-group-id-1'.");
            } catch (IdentityApplicationManagementException | UserStoreException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate the failure of group existence checking")
    public void testValidateGroupExistenceCheckFailure() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            DiscoverableGroup discoverableGroup = getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 2, 0);
            sp.setDiscoverableGroups(new DiscoverableGroup[] {discoverableGroup});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0"))).thenThrow(
                        new UserStoreException());
                applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
            } catch (IdentityApplicationManagementException | UserStoreException e) {
                assert e instanceof IdentityApplicationManagementException;
                assertEquals(((IdentityApplicationManagementException) e).getErrorCode(),
                        ERROR_CHECKING_GROUP_EXISTENCE.getCode());
            }
        });
    }

    @Test(description = "Validate the success scenario of discoverable groups validation")
    public void testValidateDiscoverableGroupsSuccess() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            DiscoverableGroup discoverableGroup = getNewDiscoverableGroup(DEFAULT_USER_STORE_DOMAIN, 1, 0);
            sp.setDiscoverableGroups(new DiscoverableGroup[] {discoverableGroup});
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                when(mockAbstractUserStoreManager.getGroupNameByGroupId(eq("test-group-id-0"))).thenReturn(
                        "test-group-name-0");
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 0);
            } catch (IdentityApplicationManagementException | UserStoreException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @Test(description = "Validate the success scenario for null or empty discoverable groups validation")
    public void testValidateEmptyDiscoverableGroupsSuccess() throws IdentityApplicationManagementException {

        validateDiscoverableGroupsCommon((sp) -> {
            when(mockAbstractUserStoreManager.getSecondaryUserStoreManager(DEFAULT_USER_STORE_DOMAIN)).thenReturn(
                    mockAbstractUserStoreManager);
            ApplicationValidator applicationValidator = new DefaultApplicationValidator();
            try {
                sp.setDiscoverableGroups(null);
                List<String> validationErrors =
                        applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 0);
                sp.setDiscoverableGroups(new DiscoverableGroup[0]);
                applicationValidator.validateApplication(sp, SUPER_TENANT_DOMAIN_NAME, USERNAME);
                assertEquals(validationErrors.size(), 0);
            } catch (IdentityApplicationManagementException e) {
                fail("Unexpected Exception occurred.", e);
            }
        });
    }

    @DataProvider(name = "validateArrayFillInAdaptiveAuthScriptDataProvider")
    public Object[][] validateArrayFillInAdaptiveAuthScriptDataProvider() {

        // Scripts containing Array().fill() that should be rejected.
        String scriptWithArrayFill = "var onLoginRequest = function(context) {\n" +
                "    var arr = Array(100).fill(0);\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillSpaces = "var onLoginRequest = function(context) {\n" +
                "    var arr = Array ( 1000 ) . fill ( null );\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillVariable = "var onLoginRequest = function(context) {\n" +
                "    const N = 10000;\n" +
                "    var arr = Array(N).fill(null);\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillInFunction = "var onLoginRequest = function(context) {\n" +
                "    function createArray() {\n" +
                "        return Array(500).fill('test');\n" +
                "    }\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillMultiLine = "var onLoginRequest = function(context) {\n" +
                "    var arr = Array(100)\n" +
                "        .fill(0);\n" +
                "    executeStep(1);\n" +
                "};";

        // Scripts that should be accepted (no Array().fill()).
        String scriptWithArrayOnly = "var onLoginRequest = function(context) {\n" +
                "    var arr = Array(10);\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithFillOnly = "var onLoginRequest = function(context) {\n" +
                "    var arr = ['a', 'b', 'c'];\n" +
                "    arr.fill('x');\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillInComment = "var onLoginRequest = function(context) {\n" +
                "    // Array(100).fill(0) - this is commented out\n" +
                "    executeStep(1);\n" +
                "};";

        String scriptWithArrayFillInMultiLineComment = "var onLoginRequest = function(context) {\n" +
                "    /* This is a comment with Array(100).fill(0)\n" +
                "       that spans multiple lines */\n" +
                "    executeStep(1);\n" +
                "};";

        String validComplexScript = "var onLoginRequest = function(context) {\n" +
                "    var users = ['alice', 'bob', 'charlie'];\n" +
                "    var roles = new Array();\n" +
                "    roles.push('admin');\n" +
                "    executeStep(1);\n" +
                "};";

        return new Object[][]{
                // isValidationFailScenario, script, expectedErrorMessage.
                {true, scriptWithArrayFill, "Script contains Array().fill() constructs which are not allowed."},
                {true, scriptWithArrayFillSpaces, "Script contains Array().fill() constructs which are not allowed."},
                {true, scriptWithArrayFillVariable, "Script contains Array().fill() constructs which are not allowed."},
                {true, scriptWithArrayFillInFunction, "Script contains Array().fill() constructs which are " +
                        "not allowed."},
                {true, scriptWithArrayFillMultiLine, "Script contains Array().fill() constructs which are" +
                        " not allowed."},
                {false, scriptWithArrayOnly, null},
                {false, scriptWithFillOnly, null},
                {false, scriptWithArrayFillInComment, null},
                {false, scriptWithArrayFillInMultiLineComment, null},
                {false, validComplexScript, null}
        };
    }

    /**
     * Test array fill validation in adaptive authentication scripts.
     *
     * @param isValidationFailScenario Whether validation should fail.
     * @param script                   The script to validate.
     * @param expectedErrorMessage     Expected error message if validation fails.
     * @throws Exception If an error occurs during testing.
     */
    @Test(dataProvider = "validateArrayFillInAdaptiveAuthScriptDataProvider")
    public void testValidateArrayFillInAdaptiveAuthScript(boolean isValidationFailScenario, String script,
                                                          String expectedErrorMessage) throws Exception {

        DefaultApplicationValidator defaultApplicationValidator = new DefaultApplicationValidator();

        // Use reflection to call the private validateAdaptiveAuthScript method.
        Method validateAdaptiveAuthScript = DefaultApplicationValidator.class.getDeclaredMethod(
                "validateAdaptiveAuthScript", List.class, AuthenticationScriptConfig.class);
        validateAdaptiveAuthScript.setAccessible(true);

        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
        scriptConfig.setContent(script);

        List<String> validationErrors = new ArrayList<>();
        validateAdaptiveAuthScript.invoke(defaultApplicationValidator, validationErrors, scriptConfig);

        if (isValidationFailScenario) {
            Assert.assertFalse(validationErrors.isEmpty(), "This is an invalid scenario. There should be " +
                    "validation messages.");

            // Check if there's a validation message related to array fill.
            List<String> arrayFillErrors = validationErrors.stream()
                    .filter(error -> StringUtils.containsIgnoreCase(error, "Array().fill()"))
                    .collect(Collectors.toList());

            Assert.assertFalse(arrayFillErrors.isEmpty(), "There should be a validation message related to " +
                    "Array().fill()");

            if (expectedErrorMessage != null) {
                Assert.assertTrue(validationErrors.contains(expectedErrorMessage),
                        "Expected error message not found. Actual errors: " + String.join("|", validationErrors));
            }
        } else {
            // Filter out any loop-related errors as we're only testing array fill validation.
            List<String> arrayFillErrors = validationErrors.stream()
                    .filter(error -> StringUtils.containsIgnoreCase(error, "Array().fill()"))
                    .collect(Collectors.toList());

            Assert.assertTrue(arrayFillErrors.isEmpty(), "There should not be any Array().fill() validation messages " +
                    "for valid scripts. Array fill errors: " + String.join("|", arrayFillErrors));
        }
    }

    /**
     * Test array fill validation through the main validateApplication method.
     *
     * @throws Exception If an error occurs during testing.
     */
    @Test
    public void testValidateApplicationWithArrayFillScript() throws Exception {

        // Create a service provider with adaptive auth script containing Array().fill()
        ServiceProvider serviceProvider = createServiceProviderWithAdaptiveAuthScript(
                "var onLoginRequest = function(context) {\n" +
                "    var largeArray = Array(100000).fill(null);\n" +
                "    executeStep(1);\n" +
                "};"
        );

        // Mock required dependencies.
        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getAllRequestPathAuthenticators(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new RequestPathAuthenticatorConfig[0]);
        when(applicationManagementService.getAllLocalAuthenticators(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig[0]);
        when(applicationManagementService.getAllLocalClaimUris(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new String[0]);

        try (MockedStatic<ApplicationManagementService> staticApplicationManagementService =
                 mockStatic(ApplicationManagementService.class)) {

            staticApplicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(applicationManagementService);

            DefaultApplicationValidator validator = new DefaultApplicationValidator();
            List<String> validationErrors = validator.validateApplication(serviceProvider,
                    SUPER_TENANT_DOMAIN_NAME, USERNAME);

            // Verify that validation fails due to Array().fill() usage.
            Assert.assertFalse(validationErrors.isEmpty(), "Validation should fail for scripts with Array().fill()");

            boolean hasArrayFillError = validationErrors.stream()
                    .anyMatch(error -> error.contains("Script contains Array().fill() constructs which are " +
                            "not allowed."));

            Assert.assertTrue(hasArrayFillError,
                    "Should have Array().fill() validation error. Actual errors: " +
                    String.join("|", validationErrors));
        }
    }

    /**
     * Test array fill validation with valid script through validateApplication method.
     *
     * @throws Exception If an error occurs during testing.
     */
    @Test
    public void testValidateApplicationWithValidScript() throws Exception {

        // Create a service provider with adaptive auth script without Array().fill().
        ServiceProvider serviceProvider = createServiceProviderWithAdaptiveAuthScript(
                "var onLoginRequest = function(context) {\n" +
                "    var normalArray = ['a', 'b', 'c'];\n" +
                "    var emptyArray = Array(10);\n" +
                "    executeStep(1);\n" +
                "};"
        );

        // Mock required dependencies.
        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getAllRequestPathAuthenticators(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new RequestPathAuthenticatorConfig[0]);
        when(applicationManagementService.getAllLocalAuthenticators(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new org.wso2.carbon.identity.application.common.model.LocalAuthenticatorConfig[0]);
        when(applicationManagementService.getAllLocalClaimUris(eq(SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(new String[0]);

        try (MockedStatic<ApplicationManagementService> staticApplicationManagementService =
                 mockStatic(ApplicationManagementService.class)) {

            staticApplicationManagementService.when(ApplicationManagementService::getInstance)
                    .thenReturn(applicationManagementService);

            DefaultApplicationValidator validator = new DefaultApplicationValidator();
            List<String> validationErrors = validator.validateApplication(serviceProvider,
                    SUPER_TENANT_DOMAIN_NAME, USERNAME);

            // Verify that there are no Array().fill() related errors.
            boolean hasArrayFillError = validationErrors.stream()
                    .anyMatch(error -> error.contains("Script contains Array().fill() constructs which are not " +
                            "allowed."));

            Assert.assertFalse(hasArrayFillError,
                    "Should not have Array().fill() validation error for valid scripts. Errors: " +
                    String.join("|", validationErrors));
        }
    }

    /**
     * Helper method to create a ServiceProvider with adaptive authentication script.
     *
     * @param scriptContent The content of the adaptive authentication script.
     * @return ServiceProvider with the given script.
     */
    private ServiceProvider createServiceProviderWithAdaptiveAuthScript(String scriptContent) {

        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setApplicationName("test-app");
        serviceProvider.setApplicationVersion("v1.0.0");
        serviceProvider.setApplicationID(1);

        // Create authentication script config.
        AuthenticationScriptConfig scriptConfig = new AuthenticationScriptConfig();
        scriptConfig.setContent(scriptContent);

        // Create local and outbound authentication config.
        org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig authConfig =
                new org.wso2.carbon.identity.application.common.model.LocalAndOutboundAuthenticationConfig();
        authConfig.setAuthenticationScriptConfig(scriptConfig);

        serviceProvider.setLocalAndOutBoundAuthenticationConfig(authConfig);

        return serviceProvider;
    }

    /**
     * Common method to validate discoverable groups by giving a custom assertion function.
     *
     * @param assertionFunc Assertion function.
     * @throws IdentityApplicationManagementException If an error occurs while validating discoverable groups.
     */
    private void validateDiscoverableGroupsCommon(Consumer<ServiceProvider> assertionFunc)
            throws IdentityApplicationManagementException {

        ApplicationManagementService applicationManagementService = mock(ApplicationManagementService.class);
        when(applicationManagementService.getAllRequestPathAuthenticators(eq(SUPER_TENANT_DOMAIN_NAME))).thenReturn(
                new RequestPathAuthenticatorConfig[0]);
        try (MockedStatic<ApplicationManagementService> staticApplicationManagementService = Mockito.mockStatic(
                ApplicationManagementService.class)) {
            staticApplicationManagementService.when(ApplicationManagementService::getInstance).thenReturn(
                    applicationManagementService);
            ServiceProvider sp = new ServiceProvider();
            sp.setApplicationName("test-app");
            sp.setApplicationVersion("v1.0.0");
            sp.setDiscoverable(true);
            sp.setAccessUrl("https://localhost:5000/test-app");
            assertionFunc.accept(sp);
        }
    }

    /**
     * Get a new DiscoverableGroup object.
     *
     * @param userStore      User store domain.
     * @param numberOfGroups Number of groups to be added.
     * @param startIndex     Suffix start index of the group.
     * @return New DiscoverableGroup object.
     */
    private DiscoverableGroup getNewDiscoverableGroup(String userStore, int numberOfGroups, int startIndex) {

        DiscoverableGroup discoverableGroup = new DiscoverableGroup();
        discoverableGroup.setUserStore(userStore);
        List<GroupBasicInfo> groupBasicInfos = new ArrayList<>();
        for (int i = startIndex; i < numberOfGroups + startIndex; i++) {
            GroupBasicInfo groupBasicInfo = new GroupBasicInfo();
            groupBasicInfo.setId("test-group-id-" + i);
            groupBasicInfo.setName("test-group-name-" + i);
            groupBasicInfos.add(groupBasicInfo);
        }
        discoverableGroup.setGroups(groupBasicInfos.toArray(new GroupBasicInfo[0]));
        return discoverableGroup;
    }

    /**
     * Setup the configurations for the test.
     */
    private void setupInitConfigurations() throws org.wso2.carbon.user.api.UserStoreException {

        String carbonHome = Paths.get(System.getProperty("user.dir"), "target", "test-classes", "repository").
                toString();
        System.setProperty(CarbonBaseConstants.CARBON_HOME, carbonHome);
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, Paths.get(carbonHome, "conf").toString());

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(
                MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(SUPER_TENANT_ID);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(USERNAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setUserId(USER_ID);

        CarbonConstants.ENABLE_LEGACY_AUTHZ_RUNTIME = false;

        mockIdentityTenantUtil.when(() -> IdentityTenantUtil.getTenantId(eq(
                        MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)))
                .thenReturn(SUPER_TENANT_ID);

        mockedApplicationManagementServiceComponentHolder.when(
                        ApplicationManagementServiceComponentHolder::getInstance)
                .thenReturn(mockComponentHolder);
        when(mockComponentHolder.getRealmService()).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(SUPER_TENANT_ID)).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockAbstractUserStoreManager);
    }
}
