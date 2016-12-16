package io.github.sat0198;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class Operators {

    interface Operator {
        int op(int i);
    }

    class Add1 implements Operator {
        public int op(int i) {
            return i + 1;
        }
    }

    class Add2 implements Operator {
        public int op(int i) {
            return i + 2;
        }
    }

    class Add4 implements Operator {
        public int op(int i) {
            return i + 4;
        }
    }

    // Simple iterative operation combiner.
    // Single call site irrespective of array element index.

    class Simple {
        Simple(Operator[] ops) {
            this.ops = ops;
        }
        int run(int num) {
            for (int i = 0; i < ops.length; ++i) {
                // Single call site
                num = ops[i].op(num);
            }
            return num;
        }
        Operator[] ops;
    }

    // Manually unrolled operation combiner.
    // Distinct call site for upto 3 elements, single call site otherwise.

    class Switch {
        Switch(Operator[] ops) {
            this.ops = ops;
        }
        int run(int num) {
            switch (ops.length) {
            case 3:
                // First of three call sites
                num = ops[2].op(num);
                // Drop through
            case 2:
                // Second of three call sites
                num = ops[1].op(num);
                // Drop through
            case 1:
                // Third of three call sites
                num = ops[0].op(num);
                break;
            default:
                for (int i = 0; i < ops.length; ++i) {
                    num = ops[i].op(num);
                }
            }
            return num;
        }
        Operator[] ops;
    }

    Operator[] ops111 ={ new Add1(), new Add1(), new Add1() };
    Operator[] ops121 ={ new Add1(), new Add2(), new Add1() };
    Operator[] ops124 ={ new Add1(), new Add2(), new Add4() };
    Operator[] ops241 ={ new Add2(), new Add4(), new Add1() };
    Operator[] ops412 ={ new Add4(), new Add1(), new Add2() };

    Simple simple111 = new Simple(ops111);
    Simple simple121 = new Simple(ops121);
    Simple simple124 = new Simple(ops124);

    Switch switch111 = new Switch(ops111);
    Switch switch121 = new Switch(ops121);
    Switch switch124 = new Switch(ops124);
    Switch switch241 = new Switch(ops241);
    Switch switch412 = new Switch(ops412);

    int num = 0;

    // Single mono-morphic call site
    @Benchmark
    public void testSimple111() {
        num = simple111.run(num);
    }

    // Single bi-morphic call site
    // Expected to be slightly slower than testSimple111
    @Benchmark
    public void testSimple121() {
        num = simple121.run(num);
    }

    // Single tri-morphic call site
    // Expected to be very much slower than testSimple111
    @Benchmark
    public void testSimple124() {
        num = simple124.run(num);
    }

    // Three mono-morphic call sites
    // Expected to be faster then testSimple111
    @Benchmark
    public void testSwitch111() {
        num = switch111.run(num);
    }

    // Three call sites, still all mono-morphic
    // Expected to be same speed as testSwitch111
    @Benchmark
    public void testSwitch121() {
        num = switch121.run(num);
    }

    // Three call sites, still all mono-morphic
    // Expected to be same speed as testSwitch111
    @Benchmark
    public void testSwitch124() {
        num = switch124.run(num);
    }

    // Three call sites, now all tri-morphic
    // Expected to be much slower than testSwitch111
    @Benchmark
    @OperationsPerInvocation(3)
    public void testMultiSwitch() {
        num = switch124.run(num);
        num = switch241.run(num);
        num = switch412.run(num);
    }
}
