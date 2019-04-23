package org.nagoya.system.event;

public interface CustomEventSource {
    void fireEvent(CustomEventType eventType, Object object);
}
