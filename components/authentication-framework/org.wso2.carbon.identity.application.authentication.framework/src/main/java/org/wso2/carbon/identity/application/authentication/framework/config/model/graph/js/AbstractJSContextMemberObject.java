/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.js;

import org.openjdk.nashorn.api.scripting.AbstractJSObject;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;

import java.io.Serializable;

/**
 * Represents the abstract class for all context objects
 */
public class AbstractJSContextMemberObject extends AbstractJSObject implements Serializable {

    private transient AuthenticationContext context;

    /**
     * Initializes context. Used when deserializing the object.
     *
     * @param context authentication context
     */
    public void initializeContext(AuthenticationContext context) {

        this.context = context;
    }

    /**
     * Get the authentication context this object is initialized with.
     *
     * @return authentication context
     */
    public AuthenticationContext getContext() {

        return context;
    }
}
