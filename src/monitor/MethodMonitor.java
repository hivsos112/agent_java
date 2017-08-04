package monitor;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangl on 2017/8/2.
 */
public class MethodMonitor {
    final static List<String> methodList = new ArrayList<String>();
    final static List<String> interfaceList = new ArrayList<String>();
    final static String prefix = "\nlong startTime = System.currentTimeMillis();\n";
    final static String postfix = "\nlong endTime = System.currentTimeMillis();\n";

    static {
        methodList.add("com.bsoft.servlet.JsonRequest.doPost");
        methodList.add("com.bsoft.servlet.JsonRequest.doGet");
        methodList.add("MyTest.testOracle");

    }

    static {
        interfaceList.add("java.sql.PreparedStatement");
    }

    public static byte[] executeInfo(ClassLoader loader, String className, Class<?> classBeingRedefined) {
        List<String> methods = new ArrayList<>();
        for (String method : methodList) {
            if (className.startsWith(method.substring(0, method.lastIndexOf(".") - 1))) {
                methods.add(method.substring(method.lastIndexOf('.') + 1, method.length()));
            }
        }

        try {
            if (methods.size() > 0 || className.endsWith("Statement")) {
                //用于取得字节码类，必须在当前的classpath中，使用全称 ,这部分是关于javassist的知识
                //循环一下，看看哪些方法需要加时间监测
                ClassPool pool = ClassPool.getDefault();
                //pool.insertClassPath(new ClassClassPath(this.getClass()));
                pool.appendClassPath(new LoaderClassPath(loader));
                CtClass ctclass = pool.get(className);
                if (!ctclass.isInterface() && className.endsWith("Statement")) {
                    try {
                        CtMethod m = ctclass.getDeclaredMethod("executeQuery");
                        if (m.getParameterTypes().length > 0) {
                            System.out.println(m.getParameterTypes()[0]);
                            m.instrument(
                                    new ExprEditor() {
                                        public void edit(MethodCall mc)
                                                throws CannotCompileException
                                        {
                                            System.out.println(mc.getMethodName());
                                            if (mc.getMethodName().equals("executeQuery")) {
                                                String out = "System.out.println(\"execute sql [\" + getOriginalSql() + \"] cost \" + (startTime-endTime) + \"ms\"";
                                                System.out.println("executeQuery execute");
                                                mc.replace("{ " + prefix + "; $_ = $proceed($$); " + postfix + out +" }");
                                            }
                                        }
                                    });
                           // m.insertBefore(prefix + "System.out.println(\"execute sql is :\" + $1);");
                           // m.insertAfter("System.out.println(\"execute sql [\" + getOriginalSql() + \"] cost \" + (System.currentTimeMillis() + var2) + \"ms\");");
                        } else {
                            m.insertBefore(prefix + "System.out.println(\"execute sql is :\" + getOriginalSql());");
                            m.insertAfter("System.out.println(\"execute sql [\" + getOriginalSql() + \"] cost \" + (System.currentTimeMillis()) + \"ms\");");
                        }
                        System.out.println("className: " + className);
                        ctclass.writeFile("D://javaclass");
                    } catch (NotFoundException e1) {

                    }
                    return ctclass.toBytecode();
                }
                // System.out.println(PreparedStatement.class.isAssignableFrom(loader.loadClass(className)));
                for (int i = 0; i < methods.size(); i++) {
                    //获取方法名
                    String methodName = methods.get(i);
                    // String outputStr = "\nSystem.out.println(\"this method " + methodName + " cost:\" +(endTime - startTime) +\"ms.\");";
                    //得到这方法实例
                    CtMethod ctmethod = ctclass.getDeclaredMethod(methodName);
                    //新定义一个方法叫做比如sayHello$impl
                    String newMethodName = methodName + "$impl";
                    //原来的方法改个名字
                    ctmethod.setName(newMethodName);
                    //创建新的方法，复制原来的方法 ，名字为原来的名字
                    CtMethod newMethod = CtNewMethod.copy(ctmethod, methodName, ctclass, null);
                    //构建新的方法体
                    StringBuilder bodyStr = new StringBuilder();
                    bodyStr.append("{");
                    bodyStr.append(prefix);
                    bodyStr.append("try {\n");
                    //调用原有代码，类似于method();($$)表示所有的参数
                    bodyStr.append(newMethodName + "($$);\n");
                    bodyStr.append("}finally {");
                    bodyStr.append(postfix);
                    bodyStr.append("agentTest.log4j(\"[" + className + "." + methodName + "] cost:\" +(endTime - startTime) +\"ms.\");");
                    //bodyStr.append(outputStr);
                    bodyStr.append(" }}");
                    //替换新方法
                    newMethod.setBody(bodyStr.toString());
                    //增加新方法
                    ctclass.addMethod(newMethod);
                }
                ctclass.detach();
                return ctclass.toBytecode();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
