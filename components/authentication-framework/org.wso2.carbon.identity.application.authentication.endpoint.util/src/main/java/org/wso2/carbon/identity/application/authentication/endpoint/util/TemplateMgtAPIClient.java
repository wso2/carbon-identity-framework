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

package org.wso2.carbon.identity.application.authentication.endpoint.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/**
 * Client for calling /api/identity/template/mgt/v1.0.0/templates/ with mutual ssl authentication
 */
public class TemplateMgtAPIClient {

    private static final Log log = LogFactory.getLog(TemplateMgtAPIClient.class);

    private static final String HTTP_METHOD_GET = "GET";

    /**
     * Send mutual ssl https post request and return data
     *
     * @param backendURL URL of the service
     * @return Received data
     * @throws IOException
     */
    public static String getTemplateData(String backendURL) {

        InputStream inputStream = null;
        BufferedReader reader = null;
        String response = null;
        URL url = null;

        try {
            url = new URL(backendURL);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.setSSLSocketFactory(MutualSSLManager.getSslSocketFactory());
            httpsURLConnection.setDoOutput(true);
            httpsURLConnection.setDoInput(true);
            httpsURLConnection.setRequestMethod(HTTP_METHOD_GET);

            httpsURLConnection.setRequestProperty(MutualSSLManager.getUsernameHeaderName(),
                    MutualSSLManager.getCarbonLogin());

            inputStream = httpsURLConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            String line;

            while (StringUtils.isNotEmpty(line = reader.readLine())) {
                builder.append(line);
            }
            response = builder.toString();
        } catch (IOException e) {
            log.error("Sending " + HTTP_METHOD_GET + " request to URL : " + url + "failed.", e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }

            } catch (IOException e) {
                log.error("Closing stream for " + url + " failed", e);
            }
        }
        return response;
    }
}
