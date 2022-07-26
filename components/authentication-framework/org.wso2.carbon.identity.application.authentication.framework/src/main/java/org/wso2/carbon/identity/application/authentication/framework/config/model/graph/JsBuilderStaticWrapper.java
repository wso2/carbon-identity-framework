/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.AsyncProcess;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn.JsOpenJdkNashornGraphBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn.JsOpenJdkNashornGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

import java.util.Map;

/**
 * Singleton to get the appropriate JsGraphBuilder static Methods.
 */
public class JsBuilderStaticWrapper {

    private static JsBuilderStaticWrapper jsWrapperFactoryProvider = new JsBuilderStaticWrapper();

    private JsBuilderStaticWrapper() {
    }

    public static void addPrompt(String templateId, Map<String, Object> parameters, Map<String, Object> handlers,
                                      Map<String, Object> callbacks) {

        if (FrameworkServiceDataHolder.getInstance().
                getJsGraphBuilderFactory() instanceof JsOpenJdkNashornGraphBuilderFactory) {
            JsOpenJdkNashornGraphBuilder.addPrompt(templateId, parameters, handlers, callbacks);
        } else {
            JsGraphBuilder.addPrompt(templateId, parameters, handlers, callbacks);
        }
    }

    public static void addLongWaitProcess(AsyncProcess asyncProcess,
                                          Map<String, Object> parameterMap) {

        if (FrameworkServiceDataHolder.getInstance().
                getJsGraphBuilderFactory() instanceof JsOpenJdkNashornGraphBuilderFactory) {
            JsOpenJdkNashornGraphBuilder.addLongWaitProcess(asyncProcess, parameterMap);
        } else {
            JsGraphBuilder.addLongWaitProcess(asyncProcess, parameterMap);

        }
    }
}
