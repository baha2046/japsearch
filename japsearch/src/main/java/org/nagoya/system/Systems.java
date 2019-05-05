package org.nagoya.system;

import org.nagoya.preferences.GeneralSettings;
import org.nagoya.system.event.EventDispatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Systems {
    private static final JFXDialogPool dialogPool = new JFXDialogPool();
    private static final EventDispatcher eventDispatcher = new EventDispatcher();
    private static final DirectorySystem directorySystem = new DirectorySystem();
    private static final ActorDB actorDB = new ActorDB();

    private static final GeneralSettings preferences = GeneralSettings.getInstance();

    public static GeneralSettings getPreferences() { return preferences; }

    public static JFXDialogPool getDialogPool() {
        return Systems.dialogPool;
    }

    public static EventDispatcher getEventDispatcher() {
        return Systems.eventDispatcher;
    }

    public static ExecuteSystem getExecuteSystem() {
        return ExecuteSystem.getInstance();
    }

    public static Future<?> useExecutors(Runnable run) {
        return useExecutors(ExecuteSystem.role.NORMAL, run);
    }

    public static Future<?> usePriorityExecutors(Runnable run) {
        return useExecutors(ExecuteSystem.role.IO, run);
    }

    public static Future<?> useExecutors(ExecuteSystem.role role, Runnable run) {
        return ExecuteSystem.getInstance().useExecutor(run, role);
    }

    public static ExecutorService getExecutorServices() {
        return getExecutorServices(ExecuteSystem.role.NORMAL);
    }

    public static ExecutorService getExecutorServices(ExecuteSystem.role role) {
        return ExecuteSystem.getInstance().getService(role);
    }


    public static DirectorySystem getDirectorySystem() {
        return Systems.directorySystem;
    }

    public static ActorDB getActorDB() {
        return Systems.actorDB;
    }

    public static void shutdown()
    {
        getExecuteSystem().shutdown();
    }
}
