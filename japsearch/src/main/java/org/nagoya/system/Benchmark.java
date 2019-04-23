package org.nagoya.system;


import org.nagoya.GUICommon;

public class Benchmark {
    private final String name;
    private final long startTime;
    private final boolean use;

    public Benchmark(boolean u, String n) {
        name = n + " ";
        use = u;
        startTime = System.nanoTime();
    }

    public void B(String s) {
        if(use)
            GUICommon.debugMessage(()->name + s + (System.nanoTime() - startTime));
    }
}
