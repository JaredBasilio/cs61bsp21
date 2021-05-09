package flik;
import org.junit.Test;
import static org.junit.Assert.*;

public class Test1 {
    @Test
    public void test1() {
        assertTrue(Flik.isSameNumber(1, 1));
        assertTrue(Flik.isSameNumber(128, 128));
        assertFalse(Flik.isSameNumber(1, 2));
    }
}
