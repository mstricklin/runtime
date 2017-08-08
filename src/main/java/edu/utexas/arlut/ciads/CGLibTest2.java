// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@Slf4j
public class CGLibTest2 {
    public static void main(String[] args) throws Exception {
        CGLibTest2 a = new CGLibTest2();
        a.foo();
    }

    void foo() throws Exception {
        Original original = new Original();
        log.info("Original class {}", original.getClass());
        MethodInterceptor handler = new Handler(original);
        Original f = (Original) Enhancer.create(Original.class, handler);
        f.originalMethod("Hallo");
        log.info("Handler class {}", f.getClass());
    }

    static class Original {
        public void originalMethod(String s) {
            System.out.println(s);
        }
    }

    static class Handler implements MethodInterceptor {
        private final Original original;

        public Handler(Original original) {
            this.original = original;
        }
        @Override
        public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            System.out.println("BEFORE");
            method.invoke(original, args);
            System.out.println("AFTER");
            return null;
        }
    }
}
