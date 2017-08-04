import monitor.bean.ThreadBean;
import org.apache.log4j.Logger;
import utils.U;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yangl on 2017/7/6.
 */
public class MonitorThread implements Runnable {
    static Logger logger = Logger.getLogger(MonitorThread.class);

    @Override
    public void run() {
        while (true) {
            //ClientStatus cs = StatusUtil.getClientStatus();
            //logger.info(cs.getFreeMemory() + "/" + cs.getTotalMemory() + "," + cs.getRuntime());
            // System.out.println(cs.getFreeMemory() + "/" + cs.getTotalMemory() + "," + cs.getRuntime());
            //longTimeThreadWarn();
            longTimeRunningThread();
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行时间过长的线程预警
     */
    private void longTimeThreadWarn() {
        ConcurrentHashMap<Long, ThreadBean> threadInfos = U.getThreadInfos();
        Iterator iter = threadInfos.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            ThreadBean tb = (ThreadBean) entry.getValue();
            if (tb.isRunnable() && !tb.isWarn()) {
                long startTime = tb.getStartRunTime();
                if (System.currentTimeMillis() - startTime > 5 * 1000) {
                    // System.out.println("threadInfo :" + tb.isRunnable() +":"+ tb.isWarn() +":"+ tb.getStartRunTime());
                    tb.setWarn(true);
                    logger.warn(U.getTreadTrack(tb.getThread()));
                }
            }
        }
    }


    private void longTimeRunningThread() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] ids = bean.getAllThreadIds();
        // System.out.println("cpu enabled------------:" + bean.isThreadCpuTimeEnabled());
        if (bean.isThreadCpuTimeEnabled()) {
            for (long id : ids) {
                ThreadInfo tInfo = bean.getThreadInfo(id);
                // logger.info(tInfo.getThreadName() + (tInfo.getThreadName().matches("http\\-apr\\-\\d+\\-exec\\-\\w*")));
                //if (tInfo.getThreadName().matches("http\\-apr\\-\\d+\\-exec\\-\\w*")) {
                    long cpuTime = bean.getThreadCpuTime(id);

                    logger.info(tInfo.getThreadName() + " run time:" + cpuTime + ",now status:" + tInfo.getThreadState());
                    if (tInfo.getThreadState().equals(Thread.State.RUNNABLE)) {
                        try {
                            //Method m = ThreadInfo.class.getMethod("getLastExecTime");
                            //long lastExecTime = (long) m.invoke(tInfo);
                            //System.out.println(tInfo.getThreadName() + " lastExecTime :" + lastExecTime);

                            //logger.warn(tInfo.getThreadName() + " execution time is too long:total CPU time:" + cpuTime
                            //        + ",is running :" + (System.currentTimeMillis() - lastExecTime) / 1000 + " sec");
                            StringBuffer sbf = new StringBuffer();
                            for (StackTraceElement e : tInfo.getStackTrace()) {
                                if (sbf.length() > 0) {
                                    sbf.append(" <- ");
                                    sbf.append(System.getProperty("line.separator"));
                                }
                                sbf.append(java.text.MessageFormat.format("{0}.{1}() {2}"
                                        , e.getClassName()
                                        , e.getMethodName()
                                        , e.getLineNumber()));
                            }
                            logger.info("thread info:" + sbf);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
               // }
            }
        }

    }
}
