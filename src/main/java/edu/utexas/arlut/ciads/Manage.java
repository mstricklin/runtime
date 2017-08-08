// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import static com.google.common.collect.Maps.newHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.*;

// Need proxy for easiest rolling back...

// Ultimate goal:
// 1. make a T t into cache
// 2. return proxy which points at t
// 3. on calling set* update* insert* make a doCopy of t->t' in mutated
// 4. re-point proxy at t'
// 5. on commit, merge t', keep proxy pointing at t1

@Slf4j
public class Manage {
    Manage(Integer val) {
        this.val = val;
    }
    void dump() {
        log.info(" == original ==");
        for (Map.Entry<Integer, App.IDed> e : cache.entrySet())
            log.info("{} => {}", e.getKey(), e.getValue());
        log.info(" == mutated ==");
        for (Map.Entry<Integer, App.IDed> e : mutated.entrySet())
            log.info("{} => {}", e.getKey(), e.getValue());
    }

    <T extends App.IDed> T managedItem(Class<T> clazz) throws InstantiationException, IllegalAccessException {
        log.info("Class {}", clazz);

        try {
            Integer id = ai.getAndIncrement();
            T t = clazz.newInstance();
            cache.put(id, t);
            T t0 = makeEnhancedClass(clazz, id);
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(t, id);
            idField.set(t0, id);

            log.info("T is {}", t);
            cache.put(t.getId(), t);
            return t0;
        } catch (NoSuchFieldException e) {
            log.error("Class {} lacks a field 'id'", clazz, e);
            return null;
        } catch (IllegalAccessException e) {
            log.error("Unable to set id in {}", clazz, e);
            return null;
        }
    }

    Enhancer enhancer = new Enhancer();
    public <T> T makeEnhancedClass(Class<T> clazz, Integer id) throws IllegalAccessException, InstantiationException {
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(makeDispatcher(id));
        T t = clazz.cast(enhancer.create());
        return t;
    }

    Callback makeDispatcher(final Integer id) {
        return new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                    throws Throwable {
                final String mName = method.getName();
                if (mName.startsWith("set") || mName.startsWith("update") || mName.startsWith("insert")) {
                    log.info("Calling setter...{}", Manage.this.val);
                    markMutated(id);
                }
                log.info("intercept {}", method.getName());
                App.IDed t = get(id);
                return proxy.invoke(t, args);
            }
        };
    }
    void markMutated(Integer id) throws Exception {
        if ( ! mutated.containsKey(id)) {
            App.IDed i = cache.get(id);
            // doCopy here...
            App.IDed i0 = doCopy(App.IDed.class, i);
            log.info("move {} to mutated", i);
            mutated.put(id, i0);
        }
    }

    // serialization-clone here? kostaskougios cloning?
    public App.IDed doCopy(Class<App.IDed> clazz, App.IDed instance) {
        return instance.copy();
//        BeanCopier copier = BeanCopier.create(clazz, clazz, false);
//        T otherInstance = clazz.newInstance();
//        copier.copy(instance, otherInstance, null);
//        return otherInstance;
    }

    App.IDed get(Integer id) {
        if (mutated.containsKey(id))
            return mutated.get(id);
        if (cache.containsKey(id))
            return cache.get(id);
        return null; // throw!!!
    }
    private Integer val;

    final Map<Integer, App.IDed> cache = newHashMap();
    final Map<Integer, App.IDed> mutated = newHashMap();

    AtomicInteger ai = new AtomicInteger(0);
}
