/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.trusted.app.mgt.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.identity.trusted.app.mgt.exceptions.TrustedAppMgtException;
import org.wso2.carbon.identity.trusted.app.mgt.internal.TrustedAppMgtDataHolder;
import org.wso2.carbon.identity.trusted.app.mgt.model.TrustedAndroidApp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.ANDROID_APP_NAMESPACE_VALUE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.CERT_FINGERPRINT_ATTRIBUTE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.CP_ANDROID_TRUSTED_APPS;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.CT_APPLICATION_JSON;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_CACHE_CONTROL;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_PRAGMA;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.NAMESPACE_ATTRIBUTE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.PACKAGE_NAME_ATTRIBUTE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.RELATION_ATTRIBUTE;
import static org.wso2.carbon.identity.trusted.app.mgt.utils.Constants.TARGET_ATTRIBUTE;

/**
 * Servlet to discover android based trusted apps.
 */
@Component(
        service = Servlet.class,
        immediate = true,
        property = {
                "osgi.http.whiteboard.servlet.pattern=" + CP_ANDROID_TRUSTED_APPS,
                "osgi.http.whiteboard.servlet.name=AndroidTrustedAppDiscoveryServlet",
                "osgi.http.whiteboard.servlet.asyncSupported=true"
        }
)
public class AndroidTrustedAppDiscoveryServlet extends HttpServlet {

    private static final Log LOG = LogFactory.getLog(AndroidTrustedAppDiscoveryServlet.class);
    private static final long serialVersionUID = 5056003394569773278L;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) {
        try {
            List<TrustedAndroidApp> trustedApps =
                    TrustedAppMgtDataHolder.getInstance().getTrustedAppMgtService().getTrustedAndroidApps();
            String response = generateJsonResponse(trustedApps);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            httpServletResponse.setContentType(CT_APPLICATION_JSON);
            httpServletResponse.setHeader(HTTP_RESP_HEADER_CACHE_CONTROL, HTTP_RESP_HEADER_VAL_CACHE_CONTROL_NO_STORE);
            httpServletResponse.setHeader(HTTP_RESP_HEADER_PRAGMA, HTTP_RESP_HEADER_VAL_PRAGMA_NO_CACHE);
            PrintWriter out = httpServletResponse.getWriter();
            out.print(response);
        } catch (TrustedAppMgtException | IOException e) {
            LOG.error("Server error when loading trusted apps for android platform.", e);
            httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String generateJsonResponse(List<TrustedAndroidApp> trustedApps) {

        JsonArray responseObject = new JsonArray();
        for (TrustedAndroidApp app : trustedApps) {

            // Populate relation attribute.
            JsonArray relation = new JsonArray();
            Set<String> permissions = app.getPermissions();
            for (String permission : permissions) {
                relation.add(permission.trim());
            }

            // Populate target attribute.
            JsonObject target = new JsonObject();
            target.addProperty(NAMESPACE_ATTRIBUTE, ANDROID_APP_NAMESPACE_VALUE);
            target.addProperty(PACKAGE_NAME_ATTRIBUTE, app.getPackageName());

            JsonArray fingerprintArray = new JsonArray();
            String[] thumbprints = app.getThumbprints();
            for (String fingerprint : thumbprints) {
                fingerprintArray.add(fingerprint.trim());
            }
            target.add(CERT_FINGERPRINT_ATTRIBUTE, fingerprintArray);

            // Create trusted app object.
            JsonObject appObject = new JsonObject();
            appObject.add(RELATION_ATTRIBUTE, relation);
            appObject.add(TARGET_ATTRIBUTE, target);
            responseObject.add(appObject);
        }
        return responseObject.toString();
    }
}
