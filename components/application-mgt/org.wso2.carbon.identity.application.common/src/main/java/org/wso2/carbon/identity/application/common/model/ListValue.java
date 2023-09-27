/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.identity.application.common.model;

/**
 * Application Tag ID Object.
 */
public class ListValue {

    private String value;

    public ListValue() {
    }

    public ListValue(ListValueBuilder listValueBuilder) {

        this.value = listValueBuilder.value;
    }

    public String getValue() {

        return value;
    }

    /**
     * ListValue builder.
     */
    public static class ListValueBuilder {

        private String value;

        public ListValueBuilder() {
        }

        public ListValueBuilder value(String value) {

            this.value = value;
            return this;
        }

        public ListValue build() {

            return new ListValue(this);
        }
    }
}
