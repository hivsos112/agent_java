import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.lang.instrument.Instrumentation;

/**
 * Created by yangl on 2017/6/19.
 */
public class agentTest {
    static Logger logger = Logger.getLogger(agentTest.class);

    static {
        System.out.println("agentTest static method run-------------");
        BasicConfigurator.configure();
        Thread monitor =  new Thread(new MonitorThread());
        monitor.setDaemon(true);
        monitor.start();
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain-1." + agentArgs);
        inst.addTransformer(new MonitorTransformer());
    }

    public static void log4j(String msg) {
        System.out.println(msg);
        logger.info(msg);
    }

    public static void main(String[] args) {
        logger.info("logger info ");
    }

}
