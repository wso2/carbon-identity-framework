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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.*;
import java.util.Hashtable;

/**
 * Default implementation of the Session Serializer.
 */
@Component(
        name = "identity.application.authentication.framework.store",
        immediate = true
)
public class DefaultSessionSerializer implements SessionSerializer {

    private static final Log log = LogFactory.getLog(DefaultSessionSerializer.class);

    protected void activate(ComponentContext ctxt) {

        try {
            DefaultSessionSerializer serializer = new DefaultSessionSerializer();
            Hashtable<String, String> props = new Hashtable<>();
            ctxt.getBundleContext().registerService(DefaultSessionSerializer.class.getName(),
                    serializer, props);
            if (log.isDebugEnabled()) {
                log.debug("Default Session Serializer is activated");
            }
        } catch (Throwable e) {
            log.fatal("Error while activating the Default Session Serializer ", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.debug("Default Session Serializer is deactivated.");
        }
    }

    @Override
    public String getName() {
        return "DefaultSessionSerializer";
    }

    @Override
    public InputStream serializeSessionObject(Object value) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(value);
        oos.flush();
        oos.close();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    @Override
    public Object deSerializeSessionObject(InputStream inputStream) throws IOException, ClassNotFoundException {

        ObjectInputStream ois = new ObjectInputStream(inputStream);
        return ois.readObject();
    }
}
