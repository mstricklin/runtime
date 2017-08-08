// CLASSIFICATION NOTICE: This file is UNCLASSIFIED
package edu.utexas.arlut.ciads;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, NoSuchFieldException {
        Manage m = new Manage(17);

        A a0 = m.managedItem(A.class);
//        a0.setS0("shazam");
        log.info("A {} {}", a0, a0.getClass());


//        cache = new Manage(23);
        a0 = m.managedItem(A.class);
        a0.setS0("shazam0");
        m.dump();
        a0.setS1("shazam1");
        log.info("A {} {}", a0, a0.getClass());

        m.dump();
    }

    public interface IDed {
        Integer getId();
        IDed copy();
    }


    @Getter
    @Setter
    @AllArgsConstructor
    static class A implements IDed {

        final Integer id;
        String s0;
        String s1;
        @Override
        public int hashCode() {
            return id;
        }
        public String toString() {
            return "edu.utexas.arlut.ciads.App.A(id=" + this.getId() + ", s0=" + this.getS0() + ", s1=" + this.getS1() + ")";
        }
        public A() {
            id = -1;
        }
        private A(final A a0) {
            // assert a0.getClass() == A.class
            this.id = a0.id;
            this.s0 = a0.s0;
            this.s1 = a0.s1;
        }
        public A copy() {
            return new A(this);
        }
    }
}
