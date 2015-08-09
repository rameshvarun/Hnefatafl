package net.varunramesh.hnefatafl.game;

import com.badlogic.gdx.utils.Timer;

/**
 * Created by Varun on 8/8/2015.
 */
public class Utils {

    private static class RunnableTask extends Timer.Task {
        public final Runnable runnable;

        public RunnableTask(Runnable runnable) { this.runnable = runnable; }

        @Override
        public void run() { runnable.run(); }
    }

    public static void schedule(Runnable runnable, float delay) {
        Timer.schedule(new RunnableTask(runnable), delay);
    }
}
