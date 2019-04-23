package org.nagoya.system;

import org.nagoya.system.event.CustomEventListener;
import org.nagoya.system.event.CustomEventSource;

public interface FXContext extends CustomEventListener, CustomEventSource {

    void registerListener();
}
