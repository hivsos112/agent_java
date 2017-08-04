import monitor.MethodMonitor;
import monitor.ThreadMonitor;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * Created by yangl on 2017/7/5.
 */
public class MonitorTransformer implements ClassFileTransformer {


    /* (non-Javadoc)
     * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        // 先判断下现在加载的class的包路径是不是需要监控的类，通过instrumentation进来的class路径用‘/’分割
        // System.out.println(className);
        className = className.replace("/", ".");
        boolean threadMonitor = true;
        if (className.equals("java.lang.management.ThreadInfo")) {
            return ThreadMonitor.ThreadInfoEx(loader, className);
        }
        if (threadMonitor && className.equals("java.util.concurrent.ThreadPoolExecutor")) {
            return ThreadMonitor.ThreadPoolExecutorEx(loader, className);
        }
        //将‘/’替换为‘.’m比如monitor/agent/Mytest替换为monitor.agent.Mytest

        return MethodMonitor.executeInfo(loader, className,classBeingRedefined);
    }
}
