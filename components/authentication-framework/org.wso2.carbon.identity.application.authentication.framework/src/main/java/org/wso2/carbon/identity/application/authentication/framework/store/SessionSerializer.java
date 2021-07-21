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

package org.wso2.carbon.identity.application.authentication.framework.store;

import java.io.IOException;
import java.io.InputStream;

/**
 * Extension Point for the Session Serializers.
 */
public interface SessionSerializer {

    /**
     * Return the name of the Serializer.
     *
     * @return String
     */
    public String getName();

    /**
     * Serialize the session object.
     *
     * @param value Session Object
     * @return Object Input
     */
    public InputStream serializeSessionObject(Object value) throws IOException;

    /**
     * DeSerialize the session object.
     *
     * @param objectInput Serialized Session Object
     * @return Object
     */
    public Object deSerializeSessionObject(InputStream inputStream) throws IOException, ClassNotFoundException;
}
