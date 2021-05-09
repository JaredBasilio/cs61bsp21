package deque;
import java.util.Comparator;
import org.junit.Test;
import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    @Test
    public void testInteger() {
        Comparator<Integer> nc = new IntComparator();
        MaxArrayDeque<Integer> list = new MaxArrayDeque<>(nc);
        list.addLast(2);
        list.addLast(3);
        list.addLast(5);
        assertEquals(5, (int) list.max());
    }

    @Test
    public void testString() {
        Comparator<String> nc = new StringComparator();
        MaxArrayDeque<String> list = new MaxArrayDeque<String>(nc);
        list.addLast("smo");
        list.addLast("long");
        list.addLast("longest");
        assertEquals("longest", list.max());
    }

    public static class IntComparator implements Comparator<Integer> {
        public int compare(Integer a, Integer b) {
            if (a > b) {
                return 1;
            } else if (a.equals(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public static class StringComparator implements Comparator<String> {
        public int compare(String a, String b) {
            int length1 = a.length();
            int length2 = b.length();
            if (length1 > length2) {
                return 1;
            } else if (length1 == length2) {
                return 0;
            } else {
                return -1;
            }
        }
    }
}
