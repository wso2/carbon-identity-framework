package org.wso2.carbon.identity.event;

import org.wso2.carbon.identity.common.base.message.MessageContext;
import org.wso2.carbon.identity.event.model.Event;

public class EventMessageContext extends MessageContext {

    private Event event;

    public EventMessageContext(Event event) {
        super();
        this.event = event;

    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
