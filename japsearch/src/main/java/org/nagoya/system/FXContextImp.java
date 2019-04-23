package org.nagoya.system;


import org.nagoya.GUICommon;
import org.nagoya.system.event.CustomEvent;
import org.nagoya.system.event.CustomEventType;

public abstract class FXContextImp extends FXMLController implements FXContext {

    @Override
    public void registerListener() {
    }

    protected void registerListener(CustomEventType eventType) {
        Systems.getEventDispatcher().register(eventType, this);
    }

    @Override
    public void fireEvent(CustomEventType eventType, Object object) {
        GUICommon.debugMessage(() -> "fireEvent " + eventType.getName());
        Systems.getEventDispatcher().submit(new CustomEvent(object, eventType));
    }

    @Override
    public void executeEvent(CustomEvent e) {
    }
}
