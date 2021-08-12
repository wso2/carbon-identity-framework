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
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.common.model.script.AuthenticationScriptConfig;
import org.wso2.carbon.identity.application.mgt.validator.DefaultApplicationValidator;
import org.wso2.carbon.identity.core.util.IdentityUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                "\twhile (n < 3) {\n" +
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
                "messages\nvar errorPageParameters = {\n    'status': 'Unauthorized',\n    'statusMsg': 'You need to " +
                "be over ' + ageLimit + ' years to login to this application.'\n};\n\n/*\nHi am a multi line comment " +
                "1\n*/\n// Date of birth attribute at the client side\nvar dateOfBirthClaim = 'http://wso2" +
                ".org/claims/dob';\n\n// The validator function for DOB. Default validation check if the DOB is in " +
                "YYYY-MM-dd format\nvar validateDOB = function (dob) {\n    return dob.match(/^(\\d{4})-(\\d{2})-" +
                "(\\d{2})$/);\n};\n\nvar onLoginRequest = function(context) {\n    executeStep(1, {\n        " +
                "onSuccess: function (context) {\n            var underAge = true;\n            // Extracting user " +
                "store domain of authenticated subject from the first step\n            var dob = context" +
                ".currentKnownSubject.localClaims[dateOfBirthClaim];// Adding a single line comment with for while " +
                "foreach \n            Log.debug('DOB of user ' + context.currentKnownSubject.identifier + ' is : ' +" +
                " dob);\n            if (dob && validateDOB(dob)) {\n                var birthDate = new Date(dob);\n" +
                "                if (getAge(birthDate) >= ageLimit) {\n                    underAge = false;\n       " +
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
}
