/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt;

import java.util.regex.Pattern;

/**
 * This class holds function library utility.
 */
public class FunctionLibraryMgtUtil {

    // Regex for validating function library name.
    public static String FUNCTION_LIBRARY_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9 ._-]*$";

    /**
     * Validate function library name according to the regex.
     *
     * @return Validated or not
     */
    public static boolean isRegexValidated(String functionlibName) {

        String functionlibValidatorRegex = FUNCTION_LIBRARY_NAME_VALIDATING_REGEX;
        Pattern regexPattern = Pattern.compile(functionlibValidatorRegex);
        return regexPattern.matcher(functionlibName).matches();
    }
}
