/*
 *  Copyright (c) 2018, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js.nashorn;


import java.util.Map;

/**
 * Parameters that can be modified from the authentication script.
 * This wrapper uses jdk.nashorn engine.
 */
public class JsNashornWritableParameters extends JsNashornParameters {

    public JsNashornWritableParameters(Map wrapped) {

        super(wrapped);
    }

    @Override
    public void removeMember(String name) {

        if (getWrapped().containsKey(name)) {
            getWrapped().remove(name);
        }
    }

    @Override
    public void setMember(String name, Object value) {

        getWrapped().put(name, value);
    }
}
