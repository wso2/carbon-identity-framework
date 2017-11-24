package org.wso2.carbon.identity.testutil.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4J Log Appender, which does nothing. Logs simply goes into a black hole.
 * This is primarily used to skip DEBUG and INFO logs being printed by testNg unit tests.
 * We create multiple unit test(s) and suite(s) for each DEBUG and INFO log levels to get the proper
 * coverage.
 *
 * Printing all the logs into the console clutters the console, makes the test execution slow and causes OOM.
 *
 * Null Appender helps to prevent the above problems.
 */
public class NullAppender extends AppenderSkeleton {

    @Override
    protected void append(LoggingEvent loggingEvent) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
