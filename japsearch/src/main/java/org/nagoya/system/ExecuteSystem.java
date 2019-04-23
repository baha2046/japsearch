package org.nagoya.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class ExecuteSystem {
    private final static ExecuteSystem instance = new ExecuteSystem();
    private final ExecutorService normalExecutor;
    private final ExecutorService priorityExecutor;

    private ExecuteSystem() {
        this.priorityExecutor = Executors.newSingleThreadExecutor(this.makeThreadFactory(Thread.MAX_PRIORITY));
        this.normalExecutor = Executors.newWorkStealingPool();
    }

    public static ExecuteSystem getInstance() {
        return instance;
    }

    private ThreadFactory makeThreadFactory(final int priority) {
        return r -> {
            Thread thread = new Thread(r);
            thread.setPriority(priority);
            return thread;
        };
    }

    public void useExecutor(Runnable runnable, Priority priority) {
        if (priority == Priority.NORMAL) {
            this.normalExecutor.execute(runnable);
        } else {
            this.priorityExecutor.execute(runnable);
        }
    }

    public ExecutorService getService(Priority priority) {
        if (priority == Priority.NORMAL) {
            return this.normalExecutor;
        } else {
            return this.priorityExecutor;
        }
    }

    public enum Priority {
        NORMAL, HIGH
    }
}
