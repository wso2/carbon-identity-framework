/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.mgt.endpoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * This class overrides the control of the resource bundle reading. code below referenced to Sun's/Oracle's code and
 * changed accordingly to read resource bundle to a given character format.
 */
public class EncodedControl extends ResourceBundle.Control {

    public static final String JAVA_PROPERTIES = "java.properties";
    public static final String PROPERTIES = "properties";
    private String encoding;

    public EncodedControl(String encoding) {

        this.encoding = encoding;
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, IOException, InstantiationException {

        if (!format.equals(JAVA_PROPERTIES)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }
        String bundleName = toBundleName(baseName, locale);
        ResourceBundle bundle = null;
        final String resourceName = toResourceName(bundleName, PROPERTIES);
        final ClassLoader classLoader = loader;
        final boolean reloadFlag = reload;
        InputStream stream;
        try {
            stream = getStream(resourceName, classLoader, reloadFlag);
        } catch (PrivilegedActionException e) {
            throw (IOException) e.getException();
        }
        if (stream != null) {
            try {
                Reader reader = new InputStreamReader(stream, encoding);
                bundle = new PropertyResourceBundle(reader);
            } finally {
                stream.close();
            }
        }
        return bundle;
    }

    private InputStream getStream(final String resourceName, final ClassLoader classLoader, final boolean reloadFlag)
            throws PrivilegedActionException {

        return AccessController.doPrivileged(
                new PrivilegedExceptionAction<InputStream>() {
                    public InputStream run() throws IOException {

                        InputStream inputStream = null;
                        if (reloadFlag) {
                            URL url = classLoader.getResource(resourceName);
                            if (url != null) {
                                URLConnection connection = url.openConnection();
                                if (connection != null) {
                                    // Disable caches to get fresh data for reloading.
                                    connection.setUseCaches(false);
                                    inputStream = connection.getInputStream();
                                }
                            }
                        } else {
                            inputStream = classLoader.getResourceAsStream(resourceName);
                        }
                        return inputStream;
                    }
                });
    }
}
