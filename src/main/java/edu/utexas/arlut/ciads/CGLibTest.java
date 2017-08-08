// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.*;

@Slf4j
public class CGLibTest {

    public static void main(String[] args) throws Exception {
        CGLibTest a = new CGLibTest();
        a.foo();
    }

    void foo() throws Exception {

        SampleClass sc = new SampleClass(16);
        log.info("SampleClass {}", sc);

        SampleClass proxy = enhanceSampleClass();
//        String rtn = proxy.test("foo");
        proxy.setS0("shazam!");
        log.info("SampleClass class {}", proxy.getClass());
        log.info("SampleClass {}", proxy);
    }
    // =================================
    public SampleClass enhanceSampleClass() throws Exception {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(SampleClass.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                    throws Throwable {
                log.info("MethodInterceptor.intercept {}", method.getName());
                if (method.getName().startsWith("set")) {
                    log.info("Calling setter...");
                }
                    return proxy.invokeSuper(obj, args);
            }
        });

        java.lang.Class[] argumentTypes = {Integer.class};
        java.lang.Object[] arguments = {new Integer(17)};
        SampleClass proxy = (SampleClass)enhancer.create(argumentTypes, arguments);
        return proxy;
    }
    // =================================

    @ToString
    public static class SampleClass {
        SampleClass(Integer id) {
            this.id = id;
        }
        public String test(String input) {
            log.info("sampleClass {}", input);
            return "Hello " + input;
        }
        public void setS0(String s0) {
            this.s0 = s0;
        }
        private final Integer id;
        private String s0;
    }

}
