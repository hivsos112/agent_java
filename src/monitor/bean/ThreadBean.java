package monitor.bean;

/**
 * Created by yangl on 2017/8/2.
 */
public class ThreadBean {
    private Thread thread;
    private boolean runnable;
    private long startRunTime;
    private boolean warn = false;

    public Thread getThread() {
        return thread;
    }

    public boolean isWarn() {
        return warn;
    }

    public void setWarn(boolean warn) {
        this.warn = warn;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }

    public long getStartRunTime() {
        return startRunTime;
    }

    public void setStartRunTime(long startRunTime) {
        this.startRunTime = startRunTime;
    }
}
