package org.wso2.carbon.identity.framework.testutil.log;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.Serializable;

/**
 * Log4J Log Appender, which does nothing. Logs simply goes into a black hole.
 * This is primarily used to skip DEBUG and INFO logs being printed by testNg unit tests.
 * We create multiple unit test(s) and suite(s) for each DEBUG and INFO log levels to get the proper
 * coverage.
 * <p>
 * Printing all the logs into the console clutters the console, makes the test execution slow and causes OOM.
 * <p>
 * Null Appender helps to prevent the above problems.
 */
public class NullAppender extends AbstractAppender {

    protected NullAppender(String name, Filter filter, Layout<? extends Serializable> layout) {

        super(name, filter, layout);
    }

    @Override
    public void append(LogEvent logEvent) {

    }
}
