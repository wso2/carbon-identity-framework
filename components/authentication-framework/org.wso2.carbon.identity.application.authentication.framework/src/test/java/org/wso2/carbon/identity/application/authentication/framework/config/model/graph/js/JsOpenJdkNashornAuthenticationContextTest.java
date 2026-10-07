/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.testng.annotations.Test;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.openjdk.nashorn.JsOpenJdkNashornAuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for the device data member resolution in {@link JsAuthenticationContext}, exercised
 * through {@link JsOpenJdkNashornAuthenticationContext} which inherits the base member lookup.
 * The GraalJS wrapper overrides {@code hasMember} to always return {@code true}, so the base
 * implementation is only observable via the Nashorn wrapper.
 */
public class JsOpenJdkNashornAuthenticationContextTest {

    @Test
    public void testHasDeviceDataMemberWhenResolved() {

        AuthenticationContext authenticationContext = new AuthenticationContext();
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("platform", "ANDROID");
        authenticationContext.setProperty(FrameworkConstants.DEVICE_DATA, deviceData);

        JsOpenJdkNashornAuthenticationContext jsAuthenticationContext =
                new JsOpenJdkNashornAuthenticationContext(authenticationContext);

        assertTrue(jsAuthenticationContext.hasMember(FrameworkConstants.JSAttributes.JS_DEVICE_DATA));
    }

    @Test
    public void testHasDeviceDataMemberWhenNotResolved() {

        AuthenticationContext authenticationContext = new AuthenticationContext();

        JsOpenJdkNashornAuthenticationContext jsAuthenticationContext =
                new JsOpenJdkNashornAuthenticationContext(authenticationContext);

        assertFalse(jsAuthenticationContext.hasMember(FrameworkConstants.JSAttributes.JS_DEVICE_DATA));
    }

    @Test
    public void testHasDeviceDataMemberWhenValueIsNotAMap() {

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setProperty(FrameworkConstants.DEVICE_DATA, "not-a-map");

        JsOpenJdkNashornAuthenticationContext jsAuthenticationContext =
                new JsOpenJdkNashornAuthenticationContext(authenticationContext);

        assertFalse(jsAuthenticationContext.hasMember(FrameworkConstants.JSAttributes.JS_DEVICE_DATA));
    }
}
