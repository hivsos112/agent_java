package monitor;

import javassist.*;

import java.io.IOException;

/**
 * Created by yangl on 2017/7/25.
 */
public class ThreadMonitor {
    // 线程总数
    // 运行中的线程信息
    // 运行时间过长预警

    public static byte[] ThreadPoolExecutorEx(ClassLoader loader, String className) {
        try {
            //用于取得字节码类，必须在当前的classpath中，使用全称 ,这部分是关于javassist的知识
            ClassPool pool = ClassPool.getDefault();
            //pool.insertClassPath(new ClassClassPath(this.getClass()));
            pool.appendClassPath(new LoaderClassPath(loader));
            pool.importPackage("utils");
            pool.importPackage("monitor.bean");
            CtClass ctclass = pool.get(className);
//            String methodName = "beforeExecute";
//            CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);
//            StringBuilder body = new StringBuilder();
//            body.append("{");
//            body.append(" ThreadMXBean bean = ManagementFactory.getThreadMXBean(); ");
//            body.append(" ThreadInfo tInfo = bean.getThreadInfo($1.getId());");
//            body.append(" System.out.println($1.getName() + \":\" + $1.getId() + \":\" + tInfo.getThreadId()); ");
//            body.append(" if(tInfo != null) { tInfo.setLastExecTime(System.currentTimeMillis());System.out.println(\"beforeExecute execute :\" + $1.getId() + tInfo.getLastExecTime());}");
//            body.append("}");
//            ctmethod.setBody(body.toString());
            CtMethod runWorker = ctclass.getDeclaredMethod("runWorker");
            //runWorker.setName("runWorker$old");
            //CtMethod newMethod = CtNewMethod.copy(runWorker, "runWorker", ctclass, null);
            String runWorkerBody =
                    " {Thread wt = Thread.currentThread();" +
                            "        Runnable task = $1.firstTask;" +
                            "        $1.firstTask = null;" +
                            "        $1.unlock();\n" +
                            "        boolean completedAbruptly = true;\n" +
                            "        try {\n" +
                            "            while (task != null || (task = getTask()) != null) {\n" +
                            "                $1.lock();" +
                            "                if ((runStateAtLeast(ctl.get(), STOP) ||" +
                            "                     (Thread.interrupted() &&" +
                            "                      runStateAtLeast(ctl.get(), STOP))) &&" +
                            "                    !wt.isInterrupted())\n" +
                            "                    wt.interrupt();" +
                            "                try {\n" +
                            "                    beforeExecute(wt, task);" +
                            "                    ThreadBean tb = U.getThreadBean(wt.getId());"+
                            "                    Throwable thrown = null;" +
                            "                    try {" +
                            "                        tb.setThread(wt);tb.setStartRunTime(System.currentTimeMillis());tb.setRunnable(true);"+
                            "                        task.run();" +
                            "                    } catch (RuntimeException x) {" +
                            "                        thrown = x; throw x;" +
                            "                    } catch (Error x) {" +
                            "                        thrown = x; throw x;" +
                            "                    } catch (Throwable x) {" +
                            "                        thrown = x; throw new Error(x);" +
                            "                    } finally {" +
                            "                        tb.setThread(null);tb.setStartRunTime(-1l);tb.setRunnable(false);"+
                            "                        afterExecute(task, thrown);" +
                            "                    }\n" +
                            "                } finally {" +
                            "                    task = null;" +
                            "                    $1.completedTasks++;" +
                            "                    $1.unlock();" +
                            "                }" +
                            "            }" +
                            "            completedAbruptly = false;\n" +
                            "        } finally {" +
                            "            processWorkerExit($1, completedAbruptly);}}";
            runWorker.setBody(runWorkerBody);
            // ctclass.addMethod(runWorker);
            // ctclass.writeFile("D://javaclass");
            return ctclass.toBytecode();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] ThreadInfoEx(ClassLoader loader, String className) {
        try {
            //用于取得字节码类，必须在当前的classpath中，使用全称 ,这部分是关于javassist的知识
            ClassPool pool = ClassPool.getDefault();
            //pool.insertClassPath(new ClassClassPath(this.getClass()));
            pool.appendClassPath(new LoaderClassPath(loader));
            pool.importPackage("java.lang.management.*");
            // System.out.println(className);
            CtClass ctclass = pool.get(className);
            CtField lastExecuteTime = CtField.make("private long lastExecTime;", ctclass);
            ctclass.addField(lastExecuteTime);
            CtMethod getter = CtNewMethod.make(
                    "public long getLastExecTime() { System.out.println(this.getThreadName() + \"(get):\" + this.hashCode());return this.lastExecTime; }",
                    ctclass);
            ctclass.addMethod(getter);
            CtMethod setter = CtNewMethod.make(
                    "public void setLastExecTime(long time) {System.out.println(this.getThreadName() + \"(set):\" + this.hashCode()); this.lastExecTime = time; }",
                    ctclass);
            ctclass.addMethod(setter);

            return ctclass.toBytecode();
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
