/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.application.authentication.framework.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Common authentication response wrapper.
 */
public class CommonAuthResponseWrapper extends HttpServletResponseWrapper {

    private Map extraParameters;
    private HttpServletRequest request;
    private boolean isRedirect = false;
    private String redirectURL;
    private CommonAuthServletPrintWriter printWriter;
    private boolean wrappedByFramework = false;

    public CommonAuthResponseWrapper(HttpServletResponse response) {
        super(response);
        extraParameters = new HashMap();
        printWriter = new CommonAuthServletPrintWriter(new ByteArrayOutputStream());
    }

    public CommonAuthResponseWrapper(HttpServletResponse response, HttpServletRequest request) {

        super(response);
        this.request = request;
        extraParameters = new HashMap();
        printWriter = new CommonAuthServletPrintWriter(new ByteArrayOutputStream());
    }

    @Override
    public void sendRedirect(String location) throws IOException {

        redirectURL = location;
        isRedirect = true;
    }

    public boolean isRedirect() {
        return isRedirect;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return this.printWriter;
    }

    public byte[] getContent() throws IOException {
        return printWriter.getBufferedString().getBytes();
    }

    public void write() throws IOException {
        writeContent();
    }

    private void writeContent() throws IOException {
        byte[] content = getContent();
        ServletResponse response = getResponse();
        OutputStream os = response.getOutputStream();

        response.setContentLength(content.length);
        os.write(content);
        os.close();
    }

    public boolean isWrappedByFramework() {

        return wrappedByFramework;
    }

    public void setWrappedByFramework(boolean wrappedByFramework) {

        this.wrappedByFramework = wrappedByFramework;
    }

    private final class CommonAuthServletPrintWriter extends PrintWriter {
        StringBuffer buffer = new StringBuffer();

        public CommonAuthServletPrintWriter(OutputStream stream) {
            super(stream);
        }

        @Override
        public void print(String s) {
            buffer.append(s);
        }

        @Override
        public void println(String s) {
            buffer.append(s + "\n");
        }

        public String getBufferedString() {
            return this.buffer.toString();
        }
    }
}
