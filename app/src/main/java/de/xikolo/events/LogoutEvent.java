package de.xikolo.events;

import de.xikolo.events.base.Event;

public class LogoutEvent extends Event {

    public LogoutEvent() {
        super(LogoutEvent.class.getSimpleName());
    }

}
