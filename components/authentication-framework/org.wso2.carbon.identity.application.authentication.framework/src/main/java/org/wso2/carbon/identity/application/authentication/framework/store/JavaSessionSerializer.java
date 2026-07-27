/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org).
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

import org.osgi.annotation.bundle.Capability;
import org.wso2.carbon.identity.application.authentication.framework.exception.SessionSerializerException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Default implementation of the Session Serializer.
 */
@Capability(
        namespace = "osgi.service",
        attribute = {
                "objectClass=org.wso2.carbon.identity.application.authentication.framework.store.SessionSerializer",
                "service.scope=singleton"
        }
)
public class JavaSessionSerializer implements SessionSerializer {

    @Override
    public InputStream serializeSessionObject(Object value) throws SessionSerializerException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(value);
            oos.flush();
            oos.close();
        } catch (IOException e) {
            throw new SessionSerializerException("Error while serializing the session object", e);
        }
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public Object deSerializeSessionObject(InputStream inputStream) throws SessionSerializerException {

        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new SessionSerializerException("Error while de serializing the session object", e);
        }
    }
}
