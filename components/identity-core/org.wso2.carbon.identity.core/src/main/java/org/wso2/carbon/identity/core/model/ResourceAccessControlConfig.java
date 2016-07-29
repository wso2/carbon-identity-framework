/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

/**
 * Model class to hold resource access configs.
 */
public class ResourceAccessControlConfig implements Serializable{

    private String context;
    private String httpMethod;
    private boolean isSecured;
    private String permissions;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public boolean isSecured() {
        return isSecured;
    }

    public void setIsSecured(boolean isSecured) {
        this.isSecured = isSecured;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public static class ResourceKey {

        private String context;
        private String httpMethod;

        public String getContext() {
            return context;
        }

        public void setContext(String context) {
            this.context = context;
        }

        public String getHttpMethod() {
            return httpMethod;
        }

        public void setHttpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ResourceKey that = (ResourceKey) o;

            if (!context.equals(that.context)) return false;
            return httpMethod.equals(that.httpMethod);

        }

        @Override
        public int hashCode() {
            int result = context.hashCode();
            result = 31 * result + httpMethod.hashCode();
            return result;
        }
    }


}
