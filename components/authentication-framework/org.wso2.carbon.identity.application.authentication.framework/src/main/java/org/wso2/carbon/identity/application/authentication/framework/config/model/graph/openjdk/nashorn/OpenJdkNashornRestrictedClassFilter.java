/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.application.authentication.framework.config.model.graph.openjdk.nashorn;

import org.openjdk.nashorn.api.scripting.ClassFilter;

/**
 * This class filter disallows all classes which are not explicitly
 * bound to the Javascript context.
 *
 * Since Nashorn is deprecated in JDK 11 and onwards. We replaced it with OpenJDK Nashorn ClassFilter.
 */
public class OpenJdkNashornRestrictedClassFilter implements ClassFilter {

    @Override
    public boolean exposeToScripts(String s) {

        return false;
    }
}
