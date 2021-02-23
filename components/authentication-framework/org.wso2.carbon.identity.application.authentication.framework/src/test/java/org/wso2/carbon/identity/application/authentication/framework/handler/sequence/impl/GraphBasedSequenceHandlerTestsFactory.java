package org.wso2.carbon.identity.application.authentication.framework.handler.sequence.impl;

import org.testng.annotations.Factory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsNashornGraphBuilderFactory;
import org.wso2.carbon.identity.application.authentication.framework.config.model.graph.JsPolyglotGraphBuilderFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Stream;

public class GraphBasedSequenceHandlerTestsFactory {

    Class<GraphBasedSequenceHandlerAbstractTest>[] testsArray = new Class[]{GraphBasedSequenceHandlerAcrTest.class,
            GraphBasedSequenceHandlerClaimMappingsTest.class, GraphBasedSequenceHandlerClaimsTest.class,
            GraphBasedSequenceHandlerCustomFunctionsTest.class, GraphBasedSequenceHandlerExceptionRetryTest.class,
//            GraphBasedSequenceHandlerFailTest.class,
            GraphBasedSequenceHandlerLongWaitTest.class,
            GraphBasedSequenceHandlerNoJsTest.class
    };

    @Factory
    public Object[] createInstances() {
        Object[] nashornTestInstances = Arrays.stream(testsArray).map(className -> {
            try {
                return className.getConstructor(JsGraphBuilderFactory.class).newInstance(new JsNashornGraphBuilderFactory());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).toArray();

        Object[] polyglotTestInstances = Arrays.stream(testsArray).map(className -> {
            try {
                return className.getConstructor(JsGraphBuilderFactory.class).newInstance(new JsPolyglotGraphBuilderFactory());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }).toArray();

        return Stream.concat(Arrays.stream(nashornTestInstances),Arrays.stream(polyglotTestInstances)).toArray();
    }

}