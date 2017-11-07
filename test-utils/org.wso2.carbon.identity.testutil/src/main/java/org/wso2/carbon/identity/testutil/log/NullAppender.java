package org.wso2.carbon.identity.testutil.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4J Log Appender, which does nothing. Logs simply goes into a black hole.
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
