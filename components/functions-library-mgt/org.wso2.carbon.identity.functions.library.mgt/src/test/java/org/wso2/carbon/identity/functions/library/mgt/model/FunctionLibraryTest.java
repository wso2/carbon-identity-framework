package org.wso2.carbon.identity.functions.library.mgt.model;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class FunctionLibraryTest {
    private final FunctionLibrary functionLibrary = new FunctionLibrary();

    @Test
    public void getFunctionLibraryname(){
        functionLibrary.setFunctionLibraryName("sampleLib1.js");
        assertEquals("sampleLib1.js",functionLibrary.getFunctionLibraryName());
    }

    @Test
    public void getDescription(){
        functionLibrary.setDescription("sampleLib1Description");
        assertEquals("sampleLib1Description",functionLibrary.getDescription());
    }

    @Test
    public void getFunctionLibraryScript(){
        functionLibrary.setFunctionLibraryScript("function sample1 ()");
        assertEquals("function sample1 ()",functionLibrary.getFunctionLibraryScript());
    }
}