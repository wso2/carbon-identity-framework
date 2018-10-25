package org.wso2.carbon.identity.functions.library.mgt;

import java.util.regex.Pattern;

/**
 * Holds function library utility.
 */
public class FunctionLibraryMgtUtil {

    // Regex for validating function library name
    public static String FUNCTION_LIBRARY_NAME_VALIDATING_REGEX = "^[a-zA-Z0-9 ._-]*$";


    /**
     * Validate function library name according to the regex.
     *
     * @return validated or not
     */
    public static boolean isRegexValidated(String functionlibName) {

        String functionlibValidatorRegex = FUNCTION_LIBRARY_NAME_VALIDATING_REGEX;
        Pattern regexPattern = Pattern.compile(functionlibValidatorRegex);
        return regexPattern.matcher(functionlibName).matches();
    }
}
