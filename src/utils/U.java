package utils;

import monitor.bean.ThreadBean;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangl on 2017/8/2.
 */
public class U {
    final static ConcurrentHashMap<Long, ThreadBean> threadInfos = new ConcurrentHashMap<>();

    public static ThreadBean getThreadBean(long threadId) {

        if (!threadInfos.containsKey(threadId)) {
            threadInfos.put(threadId, new ThreadBean());
        }
        return threadInfos.get(threadId);

    }

    public static ConcurrentHashMap<Long, ThreadBean> getThreadInfos() {
        return threadInfos;
    }

    public static String getTreadTrack(Thread t) {
        if (t == null) {
            t = Thread.currentThread();
        }
        StackTraceElement[] st = t.getStackTrace();
        if (st == null) {
            return null;
        }
        StringBuffer sbf = new StringBuffer("The following thread are running slowly, please pay attention!");
        for (StackTraceElement e : st) {
            if (sbf.length() > 0) {
                sbf.append(System.getProperty("line.separator"));
            }
            sbf.append(java.text.MessageFormat.format("  at {0}.{1}({2}) "
                    , e.getClassName()
                    , e.getMethodName()
                    , e.getLineNumber()));
        }
        return sbf.toString();
    }
}
