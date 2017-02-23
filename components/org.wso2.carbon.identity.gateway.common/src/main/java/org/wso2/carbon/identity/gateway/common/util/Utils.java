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
package org.wso2.carbon.identity.gateway.common.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;


public class Utils {
    public static String buildQueryString(Map<String, String[]> parameterMap)   {
        StringBuilder queryString = new StringBuilder("?");
        try {
            boolean isFirst = true;
            Iterator i$ = parameterMap.entrySet().iterator();

            while (i$.hasNext()) {
                Map.Entry entry = (Map.Entry) i$.next();
                String[] arr$ = (String[]) entry.getValue();
                int len$ = arr$.length;

                for (int i$1 = 0; i$1 < len$; ++i$1) {
                    String paramValue = arr$[i$1];
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
            e.printStackTrace();
        }

        return queryString.toString();
    }
}
