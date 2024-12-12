module test.com.brinvex.investperf {
    requires com.brinvex.investperf;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    opens test.com.brinvex.investperf to org.junit.platform.commons;

}