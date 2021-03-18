/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph;

import org.wso2.carbon.identity.application.authentication.framework.internal.FrameworkServiceDataHolder;

/**
 * Singleton to get the appropriate Javascript Proxy Object Wrapper Factory.
 */
public class JsWrapperFactoryProvider {

    private static JsWrapperFactoryProvider jsWrapperFactoryProvider = new JsWrapperFactoryProvider();
    private JsWrapperFactory jsWrapperFactory;


    private JsWrapperFactoryProvider() {
        if (FrameworkServiceDataHolder.getInstance().
                getJsGraphBuilderFactory() instanceof JsPolyglotGraphBuilderFactory) {
            jsWrapperFactory = new GraalJsWrapperFactory();
        } else {
            jsWrapperFactory = new NashornJsWrapperFactory();
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
    public JsWrapperFactory getWrapperFactory() {

        return jsWrapperFactory;
    }
}
