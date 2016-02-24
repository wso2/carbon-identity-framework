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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.workflow.mgt.bean.metadata.type;


public enum InputType {

    SELECT("Select"),
    MULTIPLE_SELECT("Multiple_Select"),
    OPTION("Option"),
    SINGLE_CHECK_BOX("Single_CheckBox"),
    MULTIPLE_CHECK_BOX("Multiple_CheckBox"),
    TEXT_AREA("TextArea"),
    PASSWORD("Password"),
    TEXT("Text"),
    MULTIPLE_STEPS_USER_ROLE("Multiple_Steps_User_Role");
    private final String value;

    InputType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static InputType fromValue(String v) {
        for (InputType c: InputType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
