/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.application.mgt;

import org.apache.commons.lang.StringUtils;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationConfig;
import org.wso2.carbon.identity.application.common.model.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.identity.application.common.model.SpTrustedAppMetadata;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.mgt.validator.DefaultApplicationValidator;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.wso2.carbon.identity.application.mgt.ApplicationConstants.TRUSTED_APP_CONSENT_REQUIRED_PROPERTY;

/**
 * Test class for DefaultApplicationValidator.
 */
public class DefaultApplicationValidatorTest {

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
                {"v1.0.0", "oauth2", true},
                // if auth type is samlsso, the downgrading should not be allowed.
                {"v0.0.0", "oauth2", true},
                {"v0.0.1", "oauth2", false},
                {"v0.1.1", "oauth2", false},
                {"v1.1.1", "oauth2", false},
                {"dummy", "oauth2", false},
                {"v1.0.0", "samlsso", true},
                // if auth type is samlsso, the downgrading should not be allowed.
                {"v0.0.0", "samlsso", false},
                {"v0.0.1", "samlsso", false},
                {"v0.1.1", "samlsso", false},
                {"v1.1.1", "samlsso", false},
                {"dummy", "samlsso", false},
                {"v1.0.0", null, true},
                // if no auth type is marked, the downgrading should not be allowed.
                {"v0.0.0", null, false},
                {"v0.0.1", null, false},
                {"v0.1.1", null, false},
                {"v1.1.1", null, false},
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

        Assert.assertEquals(validationErrors.isEmpty(), isValid, "Valid app version has been introduced. " +
                "Please update the test case accordingly.");
    }
}
