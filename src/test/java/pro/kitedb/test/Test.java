package pro.kitedb.test;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import pro.kitedb.graph.reader.PlainObjectGraph;

import java.util.List;

@RunWith(JUnit4.class)
public class Test {

    @org.junit.Test
    public void test() {
        List<String> graph = new PlainObjectGraph(A.class, "**").getGraph();
        Assert.assertEquals("Incorrect size result", 10, graph.size());

        graph = new PlainObjectGraph(A.class, "b.*").getGraph();
        Assert.assertEquals("Incorrect size result", 4, graph.size());
        Assert.assertEquals("Incorrect value", "b.*", graph.get(0));
        Assert.assertEquals("Incorrect value", "b.b1", graph.get(1));
        Assert.assertEquals("Incorrect value", "b.b2", graph.get(2));
        Assert.assertEquals("Incorrect value", "b.b3", graph.get(3));

        graph = new PlainObjectGraph(A.class, "b.**").getGraph();
        Assert.assertEquals("Incorrect size result", 7, graph.size());
        Assert.assertEquals("Incorrect value", "b.**", graph.get(0));
        Assert.assertEquals("Incorrect value", "b.b1", graph.get(1));
        Assert.assertEquals("Incorrect value", "b.b2", graph.get(2));
        Assert.assertEquals("Incorrect value", "b.b3", graph.get(3));

        Assert.assertEquals("Incorrect value", "b.c.c1", graph.get(4));
        Assert.assertEquals("Incorrect value", "b.c.c2", graph.get(5));
        Assert.assertEquals("Incorrect value", "b.c.c3", graph.get(6));
    }

    public static class A {
        private int a1;
        private String a2;
        private Long a3;
        private B b;
    }

    public static class B {
        private int b1;
        private String b2;
        private Long b3;
        private C c;
    }

    public static class C {
        private int c1;
        private String c2;
        private Long c3;
    }
}
