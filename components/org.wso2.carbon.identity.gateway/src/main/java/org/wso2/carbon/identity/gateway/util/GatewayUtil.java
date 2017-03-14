/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.gateway.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.identity.gateway.api.exception.GatewayServerException;
import org.wso2.msf4j.Request;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class GatewayUtil {

    private static Logger logger = LoggerFactory.getLogger(GatewayUtil.class);

    /**
     * Read provide name by using file name.
     *
     * @param fileName
     * @return provider name
     * @throws GatewayServerException
     */
    public static String getProviderName(String fileName) throws GatewayServerException {
        if (!fileName.endsWith("." + org.wso2.carbon.identity.gateway.common.util.Constants.YAML_EXTENSION)) {
            throw new GatewayServerException("Provider config file should be yaml.");
        }
        return fileName.substring(0, fileName.indexOf(".yaml"));
    }

    /**
     * Load provider config.
     *
     * @param artifact
     * @param providerClass
     * @param <T>
     * @return
     * @throws GatewayServerException
     */
    public static <T extends Object> T getProvider(Artifact artifact, Class<T> providerClass)
            throws GatewayServerException {
        Path path = Paths.get(artifact.getPath());
        T provider = null;
        if (Files.exists(path)) {
            try {
                Reader in = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8);
                Yaml yaml = new Yaml();
                yaml.setBeanAccess(BeanAccess.FIELD);
                provider = yaml.loadAs(in, providerClass);
                if (provider == null) {
                    throw new GatewayServerException("Provider is not loaded correctly.");
                }
            } catch (IOException e) {
                String errorMessage = "Error occurred while loading the " + providerClass.getSimpleName() + " yaml file, " +
                        e.getMessage();
                logger.error(errorMessage, e);
                throw new GatewayServerException(errorMessage, e);
            }
        }
        return provider;
    }

    public static String getParameter(Request request, String paramName) {
        Map<String, String> queryParams = (Map<String, String>) request.getProperty(
                org.wso2.carbon.identity.gateway.common.util.Constants.QUERY_PARAMETERS);
        Map<String, String> bodyParams = (Map<String, String>) request.getProperty(
                org.wso2.carbon.identity.gateway.common.util.Constants.BODY_PARAMETERS);
        if (queryParams.get(paramName) != null) {
            return queryParams.get(paramName);
        } else {
            return bodyParams.get(paramName);
        }
    }

    public static String buildQueryString(Map<String, String[]> parameterMap) {
        StringBuilder queryString = new StringBuilder("?");
        try {
            boolean isFirst = true;
            Iterator iterator = parameterMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String[] entryValue = (String[]) entry.getValue();
                int length = entryValue.length;

                for (int i = 0; i < length; ++i) {
                    String paramValue = entryValue[i];
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        queryString.append("&");
                    }

                    queryString.append(URLEncoder.encode((String) entry.getKey(), StandardCharsets.UTF_8.name()));
                    queryString.append("=");
                    queryString.append(URLEncoder.encode(paramValue, StandardCharsets.UTF_8.name()));
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Error occured while buildQueryString, " + e.getMessage(), e);
        }

        return queryString.toString();
    }
}
