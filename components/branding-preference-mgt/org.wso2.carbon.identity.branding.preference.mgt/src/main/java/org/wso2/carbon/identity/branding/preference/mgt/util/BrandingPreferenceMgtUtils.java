/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.com).
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

package org.wso2.carbon.identity.branding.preference.mgt.util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.identity.branding.preference.mgt.exception.BrandingPreferenceMgtClientException;
import org.wso2.carbon.identity.branding.preference.mgt.exception.BrandingPreferenceMgtServerException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Util class for branding preference management.
 */
public class BrandingPreferenceMgtUtils {

    /**
     * Generate Branding Preference input stream.
     *
     * @param preferencesJSON JSON string of preferences.
     * @return Input stream of the preferences JSON string.
     * @throws UnsupportedEncodingException Unsupported Encoding Exception.
     */
    public static InputStream generatePreferenceInputStream(String preferencesJSON) throws
            UnsupportedEncodingException {

        return new ByteArrayInputStream(preferencesJSON.getBytes(StandardCharsets.UTF_8.name()));
    }

    /**
     * Check whether the given string is a valid JSON or not.
     *
     * @param stringJSON Input String.
     * @return True if the input string is a valid JSON.
     */
    public static boolean isValidJSONString(String stringJSON) {

        if (StringUtils.isBlank(stringJSON)) {
            return false;
        }
        try {
            JSONObject objectJSON = new JSONObject(stringJSON);
            if (objectJSON.length() == 0) {
                return false;
            }
        } catch (JSONException exception) {
            return false;
        }
        return true;
    }

    /**
     * This method can be used to generate a BrandingPreferenceMgtClientException from
     * BrandingPreferenceMgtConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error BrandingPreferenceMgtConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return BrandingPreferenceMgtClientException.
     */
    public static BrandingPreferenceMgtClientException handleClientException(
            BrandingPreferenceMgtConstants.ErrorMessages error, String... data) {

        String message = populateMessageWithData(error, data);
        return new BrandingPreferenceMgtClientException(message, error.getCode());
    }

    public static BrandingPreferenceMgtClientException handleClientException(
            BrandingPreferenceMgtConstants.ErrorMessages error, String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new BrandingPreferenceMgtClientException(message, error.getCode(), e);
    }

    /**
     * This method can be used to generate a BrandingPreferenceMgtServerException from
     * BrandingPreferenceMgtConstants.ErrorMessages object when no exception is thrown.
     *
     * @param error SecretConstants.ErrorMessages.
     * @param data  data to replace if message needs to be replaced.
     * @return BrandingPreferenceMgtClientException.
     */
    public static BrandingPreferenceMgtServerException handleServerException(
            BrandingPreferenceMgtConstants.ErrorMessages error, String... data) {

        String message = populateMessageWithData(error, data);
        return new BrandingPreferenceMgtServerException(message, error.getCode());
    }

    public static BrandingPreferenceMgtServerException handleServerException(
            BrandingPreferenceMgtConstants.ErrorMessages error, String data, Throwable e) {

        String message = populateMessageWithData(error, data);
        return new BrandingPreferenceMgtServerException(message, error.getCode(), e);
    }

    public static BrandingPreferenceMgtServerException handleServerException(
            BrandingPreferenceMgtConstants.ErrorMessages error, Throwable e) {

        String message = populateMessageWithData(error);
        return new BrandingPreferenceMgtServerException(message, error.getCode(), e);
    }

    private static String populateMessageWithData(BrandingPreferenceMgtConstants.ErrorMessages error, String... data) {

        String message;
        if (data != null && data.length != 0) {
            message = String.format(error.getMessage(), data);
        } else {
            message = error.getMessage();
        }
        return message;
    }

    private static String populateMessageWithData(BrandingPreferenceMgtConstants.ErrorMessages error) {

        return error.getMessage();
    }
}
