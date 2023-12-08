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

import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.JsGraalGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.graaljs.JsGraalWrapperFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn.JsOpenJdkNashornGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn.JsOpenJdkNashornWrapperFactory;
import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

/**
 * Singleton to get the appropriate Javascript Proxy Object Wrapper Factory.
 */
public class JsWrapperFactoryProvider {

    private static JsWrapperFactoryProvider jsWrapperFactoryProvider = new JsWrapperFactoryProvider();
    private JsWrapperBaseFactory jsWrapperBaseFactory;


    private JsWrapperFactoryProvider() {

        if (FrameworkServiceDataHolder.getInstance()
                .getJsGraphBuilderFactory() instanceof JsOpenJdkNashornGraphBuilderFactory) {
            jsWrapperBaseFactory = new JsOpenJdkNashornWrapperFactory();
        } else if (FrameworkServiceDataHolder.getInstance()
                .getJsGraphBuilderFactory() instanceof JsGraalGraphBuilderFactory) {
            jsWrapperBaseFactory = new JsGraalWrapperFactory();
        } else {
            jsWrapperBaseFactory = new JsWrapperFactory();
        }
    }

    /**
     * returns Js Wrapper Factory Provider.
     * @return Js Wrapper Factory Provider
     */
    public static JsWrapperFactoryProvider getInstance() {

        return jsWrapperFactoryProvider;
    }

    /**
     * Returns Appropriate Authentication Context Member Object Factory
     * based on used Script Engine.
     * @return JavaScript Object Factory
     */
    public JsWrapperBaseFactory getWrapperFactory() {

        return jsWrapperBaseFactory;
    }
}
