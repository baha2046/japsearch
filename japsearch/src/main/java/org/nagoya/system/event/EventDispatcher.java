package org.nagoya.system.event;

import javafx.application.Platform;
import org.nagoya.system.ExecuteSystem;
import org.nagoya.system.Systems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class EventDispatcher {

    private final EventListenerManager listenerManager = new EventListenerManager();
    //private final BlockingQueue<CustomEvent> eventsQueue = new LinkedBlockingQueue<>();
    //private final Thread dispatchingThread;

    public EventDispatcher() {
        //this.dispatchingThread = new Thread(this::dispatchingLoop, "CustomEventDispatcher Thread");
        //set this thread as daemon. It will not prevent the application from exiting
        //this.dispatchingThread.setDaemon(true);
        //this.dispatchingThread.start();
    }

    /*public void interrupt() {
        this.dispatchingThread.interrupt();
    }*/

    public void register(CustomEventType type, CustomEventListener listener) {
        this.listenerManager.register(type, listener);
    }

    public void unregister(CustomEventType type, CustomEventListener listener) {
        this.listenerManager.unregister(type, listener);
    }

    public void submit(CustomEvent event) {
        assert event != null : "Event cannot be null";

        Systems.useExecutors(ExecuteSystem.role.EVENT, ()->{
            listenerManager.getEventListeners(event.getType()).forEach(l->this.dispatch(l,event));
        });
    }

    private void dispatch(CustomEventListener listener, CustomEvent event) {
        try {
            Platform.runLater(() -> listener.executeEvent(event));
        } catch (Exception e) {
            //listener should handle its exception
            //this is probably a bug.
            //Log the error for the moment
            //LOGGER.error("Exception occurred while dispatching event'"+event+"' to listener '"+listener+"'",e);
        }
    }

  /*  private void dispatchingLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final CustomEvent event = this.eventsQueue.take();
                this.listenerManager.getEventListeners(event.getType()).forEach(l -> this.dispatch(l, event));
            } catch (InterruptedException e) {
                //someone asked to stop this thread
                //set back the interrupt flag and
                //quit the loop
                Thread.currentThread().interrupt();
                break;
            }
        }
    }*/

    class EventListenerManager {

        private final ConcurrentHashMap<CustomEventType, List<CustomEventListener>> listenerByEventType = new ConcurrentHashMap<>();

        /**
         * Register a listener for a given event type
         *
         * @param eventType the type of the event the listener must be register to
         * @param listener  the listener to register
         */
        void register(CustomEventType eventType, CustomEventListener listener) {
            this.listenerByEventType.computeIfAbsent(eventType, t -> new ArrayList<>()).add(listener);
        }

        /**
         * Remove a listener of a given event type
         *
         * @param eventType the type of the event the listener is register to
         * @param listener  the listener to unregister
         */
        void unregister(CustomEventType eventType, CustomEventListener listener) {
            if (this.isListenerNotInCurrentListenerMap(eventType, listener)) {
                throw new IllegalArgumentException("Trying to unregister a non-registered listener");
            }

            this.listenerByEventType.computeIfPresent(eventType, (t, l) -> {
                l.remove(listener);
                return l.isEmpty() ? null : l;
            });
        }

        /**
         * @param eventType an event type
         * @return a stream of all listeners registered to the given event type
         */
        Stream<CustomEventListener> getEventListeners(CustomEventType eventType) {
            return this.listenerByEventType.getOrDefault(eventType, Collections.emptyList()).stream();
        }


        /**
         * Check if a listener is not register for the provided event type in the current listener map
         *
         * @return true if not the listener is not register for the given event type
         */
        private boolean isListenerNotInCurrentListenerMap(CustomEventType type, CustomEventListener listener) {
            return !this.listenerByEventType.getOrDefault(type, Collections.emptyList()).contains(listener);
        }
    }
}
