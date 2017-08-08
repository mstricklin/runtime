package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProxyTest {
    public static void main(String[] args) {
        ProxyTest a = new ProxyTest();
        a.foo();
    }

    void foo() {

        TestIF t = (TestIF) Proxy.newProxyInstance(TestIF.class.getClassLoader(),
                                                   new Class<?>[] {TestIF.class},
                                                   new TestInvocationHandler(new TestImpl()));
        t.hello("Duke");
    }


    Map<Integer, ManagedClassA> m = newHashMap();

    // =================================



    interface TestIF {
        String hello(String name);
    }
    static class TestImpl implements TestIF {
        public String hello(String name) {
            log.info("TestImpl call hello({})", name);
            return String.format("Hello %s, this is %s %s", name, this, getClass().getSimpleName());
        }
    }

    // =================================
    static class TestInvocationHandler implements InvocationHandler {
        private Object testImpl;

        public TestInvocationHandler(Object impl) {
            this.testImpl = impl;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            log.info("TestInvocationHandler.invoke {}", method);
            log.info("TestInvocationHandler.invoke {}", method.getName());
//            log.info("invoke {} {} {}", proxy, method, args);
            if(Object.class  == method.getDeclaringClass()) {
                String name = method.getName();
                if("equals".equals(name)) {
                    return proxy == args[0];
                } else if("hashCode".equals(name)) {
                    return System.identityHashCode(proxy);
                } else if("toString".equals(name)) {
                    return proxy.getClass().getName() + "@" +
                            Integer.toHexString(System.identityHashCode(proxy)) +
                            ", with InvocationHandler " + this;
                } else {
                    throw new IllegalStateException(String.valueOf(method));
                }
            }
            return method.invoke(testImpl, args);
        }
    }
}
