package org.nagoya.system;

import java.util.concurrent.*;

public class ExecuteSystem {
    private final static ExecuteSystem instance = new ExecuteSystem();

    private final ExecutorService normalExecutor;
    private final ExecutorService fileIOExecutor;
    private final ExecutorService movieExecutor;
    private final ExecutorService eventExecutor;
    private final ExecutorService imageExecutor;

    private ExecuteSystem() {
        this.fileIOExecutor =
                Executors.newSingleThreadExecutor(this.makeThreadFactory(Thread.MAX_PRIORITY));
        this.movieExecutor =
                new ThreadPoolExecutor(1, 1, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.eventExecutor =
                new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        //this.imageExecutor =
        //        new ThreadPoolExecutor(1, 2, 1L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.normalExecutor =
                Executors.newWorkStealingPool();

        //this.movieExecutor = this.normalExecutor;
        this.imageExecutor = this.normalExecutor;
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

    public Future<?> useExecutor(Runnable runnable, role role) {
        switch (role) {
            case EVENT:
                return this.eventExecutor.submit(runnable);
            case IO:
                return this.fileIOExecutor.submit(runnable);
            case MOVIE:
                return this.movieExecutor.submit(runnable);
            case IMAGE:
                return this.imageExecutor.submit(runnable);

            default:
                return this.normalExecutor.submit(runnable);
        }
    }

    public <T> Future<T> useExecutor(Callable<T> callable, role role) {
        switch (role) {
            case EVENT:
                return this.eventExecutor.submit(callable);
            case IO:
                return this.fileIOExecutor.submit(callable);
            case MOVIE:
                return this.movieExecutor.submit(callable);
            case IMAGE:
                return this.imageExecutor.submit(callable);

            default:
                return this.normalExecutor.submit(callable);
        }
    }

    ExecutorService getService(role role) {
        switch (role) {
            case EVENT:
                return this.eventExecutor;
            case IO:
                return this.fileIOExecutor;
            case MOVIE:
                return this.movieExecutor;
            case IMAGE:
                return this.imageExecutor;

            default:
                return this.normalExecutor;
        }
    }

    public enum role {
        NORMAL, IO, EVENT, MOVIE, IMAGE
    }

    void shutdown() {
        this.normalExecutor.shutdown();
        this.fileIOExecutor.shutdown();
        this.movieExecutor.shutdown();
        this.imageExecutor.shutdown();
        this.eventExecutor.shutdown();

        try {
            this.normalExecutor.awaitTermination(10, TimeUnit.SECONDS);
            this.fileIOExecutor.awaitTermination(10, TimeUnit.SECONDS);
            this.movieExecutor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.normalExecutor.shutdownNow();
        this.fileIOExecutor.shutdownNow();
        this.movieExecutor.shutdownNow();
    }
}
