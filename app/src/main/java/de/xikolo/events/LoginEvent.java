package de.xikolo.events;

import de.xikolo.events.base.Event;

public class LoginEvent extends Event {

    public LoginEvent() {
        super(LoginEvent.class.getSimpleName());
    }

}
