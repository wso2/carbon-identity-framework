/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.core.model;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.core.URLBuilderException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServiceURLTest {

    String protocol = "protocol";
    String hostname = "hostname";
    String urlPath = "urlPath";
    int port = 9443;
    Map<String, String> parameters = new HashMap<>();
    String fragment = "fragment";
    Map<String, String> fragmentParams = new HashMap<>();

    @BeforeMethod
    public void setUp() {

    }

    @Test
    public void testGetProtocol() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getProtocol(), protocol);
    }

    @Test
    public void testGetHostName() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getHostName(), hostname);
    }

    @Test
    public void testGetPort() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getPort(), port);
    }

    @Test
    public void testGetUrlPath() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getUrlPath(), urlPath);
    }

    @Test
    public void testGetParameter() {

        parameters.put("key", "value");
        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getParameter("key"), "value");
    }

    @Test
    public void testGetParameterKeys() {

        parameters.clear();
        String[] keys = {"key1", "key2", "key3", "key4"};
        for (String key : keys) {
            parameters.put(key, "v");
        }
        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getParameterKeys(), keys);
    }

    @Test
    public void testGetFragment() {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostname, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getFragment(), "fragment");
    }

    @DataProvider
    public Object[][] getAbsoluteURLData() {

        parameters.clear();
        fragmentParams.clear();
        ArrayList<String> keys = new ArrayList<String>(Arrays.asList("key1", "key2", "key3", "key4"));
        for (String key : keys) {
            parameters.put(key, "v");
            fragmentParams.put(key, "fragment");
        }

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "", null, "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "https://www.wso2.com:9443#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, "/samlsso", null, "fragment",
                        "https://www.wso2.com:9443/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "/samlsso/", null,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "https://www.wso2.com:9443/samlsso#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, "samlsso", null, "fragment",
                        "https://www.wso2.com:9443/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "/samlsso", parameters, "fragment",
                        "https://www.wso2.com:9443/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#fragment"},
                {"https", "www.wso2.com", 9443, "/samlsso/", parameters, "",
                        "https://www.wso2.com:9443/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv"},
                {"https", "www.wso2.com", 9443, "/samlsso", null, "fragment",
                        "https://www.wso2.com:9443/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "/samlsso/", null, "",
                        "https://www.wso2.com:9443/samlsso"},
                {"https", "www.wso2.com", 9443, "samlsso/", null, "fragment",
                        "https://www.wso2.com:9443/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, null, parameters,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "https://www.wso2.com:9443?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, null, null, "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "https://www.wso2.com:9443#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"}
        };
    }

    @Test(dataProvider = "getAbsoluteURLData")
    public void testGetAbsoluteURL(String protocol, String hostName, int port, String urlPath,
                                   Map<String, String> parameters, String fragment, String expected) {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostName, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getAbsoluteURL(), expected);
    }

    @DataProvider
    public Object[][] getRelativeURLData() {

        parameters.clear();
        fragmentParams.clear();
        ArrayList<String> keys = new ArrayList<String>(Arrays.asList("key1", "key2", "key3", "key4"));
        for (String key : keys) {
            parameters.put(key, "v");
            fragmentParams.put(key, "fragment");
        }

        return new Object[][]{
                {"https", "www.wso2.com", 9443, "/samlsso", null, "", "/samlsso"},
                {"https", "www.wso2.com", 9443, "/samlsso", null,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "/samlsso#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, "/samlsso", null, "fragment", "/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "/samlsso/", null, "fragment", "/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "samlsso", null, "fragment", "/samlsso#fragment"},
                {"https", "www.wso2.com", 9443, "samlsso/", null,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "/samlsso#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, "/samlsso", parameters,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
                {"https", "www.wso2.com", 9443, "/samlsso/", parameters, "fragment",
                        "/samlsso?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#fragment"},
                {"https", "www.wso2.com", 9443, "", parameters,
                        "key1=fragment&key2=fragment&key3=fragment&key4=fragment",
                        "?key1%3Dv%26key2%3Dv%26key3%3Dv%26key4%3Dv#key1%3Dfragment%26key2%3Dfragment%26key3%3Dfragment%26key4%3Dfragment"},
        };
    }

    @Test(dataProvider = "getRelativeURLData")
    public void testGetRelativeURL(String protocol, String hostName, int port, String urlPath,
                                   Map<String, String> parameters, String fragment, String expected) {

        ServiceURL serviceURL = null;
        try {
            serviceURL = new ServiceURL(protocol, hostName, port, urlPath, parameters, fragment);
        } catch (URLBuilderException e) {
            //Mock behaviour, hence ignored
        }

        Assert.assertEquals(serviceURL.getRelativeURL(), expected);
    }
}
