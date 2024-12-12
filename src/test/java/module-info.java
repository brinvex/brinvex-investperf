module test.brinvex.ipa {
    requires com.brinvex.ipa;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    opens test.com.brinvex.ipa to org.junit.platform.commons;

}