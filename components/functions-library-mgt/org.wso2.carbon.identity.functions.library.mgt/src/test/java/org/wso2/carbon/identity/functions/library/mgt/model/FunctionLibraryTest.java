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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.functions.library.mgt.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit test for function library model.
 */
public class FunctionLibraryTest {

    private final FunctionLibrary functionLibrary = new FunctionLibrary();

    @Test
    public void getFunctionLibraryname() {

        functionLibrary.setFunctionLibraryName("sampleLib1.js");
        assertEquals("sampleLib1.js", functionLibrary.getFunctionLibraryName());
    }

    @Test
    public void getDescription() {

        functionLibrary.setDescription("sampleLib1Description");
        assertEquals("sampleLib1Description", functionLibrary.getDescription());
    }

    @Test
    public void getFunctionLibraryScript() {

        functionLibrary.setFunctionLibraryScript("function sample1 ()");
        assertEquals("function sample1 ()", functionLibrary.getFunctionLibraryScript());
    }
}
