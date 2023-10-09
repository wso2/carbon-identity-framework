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

import java.util.ArrayList;
import java.util.List;

/**
 * Application Tag Patch Object.
 */
public class ApplicationTagsPatch {

    private String operation;
    private List<ListValue> tags = null;


    public ApplicationTagsPatch() {
    }

    public ApplicationTagsPatch(ApplicationTagsPatchBuilder applicationTagsPatchBuilder) {

        this.operation = applicationTagsPatchBuilder.operation.toString();
        this.tags = applicationTagsPatchBuilder.tags;
    }

    public String getOperation() {

        return operation;
    }

    public List<ListValue> getTags() {

        return tags;
    }

    /**
     * ApplicationTagsPatch builder.
     */
    public static class ApplicationTagsPatchBuilder {

        /**
         * OperationEnum for the patch operations.
         */
        public enum OperationEnum {

            ADD("ADD"),
            REMOVE("REMOVE");

            private final String value;

            OperationEnum(String v) {
                value = v;
            }
        }

        private OperationEnum operation;
        private List<ListValue> tags = null;

        public ApplicationTagsPatchBuilder() {
        }

        public ApplicationTagsPatchBuilder operation(OperationEnum operation) {

            this.operation = operation;
            return this;
        }

        public ApplicationTagsPatchBuilder tags(List<ListValue> tags) {

            this.tags = tags;
            return this;
        }

        public ApplicationTagsPatchBuilder addTagsItem(ListValue tagsItem) {
            if (this.tags == null) {
                this.tags = new ArrayList<>();
            }
            this.tags.add(tagsItem);
            return this;
        }

        public ApplicationTagsPatch build() {

            return new ApplicationTagsPatch(this);
        }
    }
}
