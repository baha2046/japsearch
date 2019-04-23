package org.nagoya.system;

import org.nagoya.system.event.EventDispatcher;

import java.util.concurrent.ExecutorService;

public class Systems {
    private static final JFXDialogPool dialogPool = new JFXDialogPool();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private static final DirectorySystem directorySystem = new DirectorySystem();
    private static final ActorDB actorDB = new ActorDB();

    public static JFXDialogPool getDialogPool() {
        return Systems.dialogPool;
    }

    public static EventDispatcher getEventDispatcher() {
        return Systems.eventDispatcher;
    }

    public static ExecuteSystem getExecuteSystem() {
        return ExecuteSystem.getInstance();
    }

    public static void useExecutors(Runnable run) {
        ExecuteSystem.getInstance().useExecutor(run, ExecuteSystem.Priority.NORMAL);
    }

    public static void usePriorityExecutors(Runnable run) {
        ExecuteSystem.getInstance().useExecutor(run, ExecuteSystem.Priority.HIGH);
    }

    public static ExecutorService getExecutorServices() {
        return ExecuteSystem.getInstance().getService(ExecuteSystem.Priority.NORMAL);
    }


    public static DirectorySystem getDirectorySystem() {
        return Systems.directorySystem;
    }

    public static ActorDB getActorDB() {
        return Systems.actorDB;
    }
}
