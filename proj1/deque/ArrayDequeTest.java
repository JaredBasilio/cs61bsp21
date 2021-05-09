package deque;

import org.junit.Test;
import static org.junit.Assert.*;
import edu.princeton.cs.introcs.StdRandom;

/** Performs some basic linked list tests. */
public class ArrayDequeTest {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {
        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {
        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);
    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();
    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());
    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < 10; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < 5; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = 9; i > 5; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }
    }

    @Test
    public void deepEquals() {
        ArrayDeque<Integer> ad1 = new ArrayDeque<Integer>();
        ArrayDeque<Integer> ad2 = new ArrayDeque<Integer>();
        ad1.addFirst(1);
        ad1.addFirst(2);
        ad1.addFirst(3);
        ad2.addFirst(1);
        ad2.addFirst(2);
        ad2.addFirst(3);
        ad2.addFirst(4);

        assertFalse(ad1.equals(ad2));
        boolean result1 = ad1.equals(ad1);
        assertTrue(result1);

        ArrayDeque<Integer> ad3 = new ArrayDeque<Integer>();
        LinkedListDeque<Integer> ad4 = new LinkedListDeque<>();
        ad3.addFirst(1);
        ad3.addFirst(2);
        ad3.addFirst(3);
        ad4.addFirst(1);
        ad4.addFirst(2);
        ad4.addFirst(3);

        boolean result2 = ad3.equals(ad4);
    }

    @Test
    public void addRemovePerformance() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        for (int i = 0;i < 8;i++) {
            ad.addFirst(i);
        }
        for (int i = 0;i < 8;i++) {
            ad.addLast(i);
        }
        for (int i = 0;i<16;i++){
            ad.removeFirst();
        }
        assertTrue(ad.isEmpty());
    }

    @Test
    public void get() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        ad.addLast(0);
        assertEquals(0, (int) ad.get(0));
        assertEquals(0, (int) ad.get(0));
        ad.addFirst(3);
        ad.addLast(4);
        assertEquals(4, (int) ad.removeLast());
        assertEquals(3, (int) ad.get(0));
        assertEquals(0, (int) ad.removeLast());
        assertEquals(3, (int) ad.get(0));
        ad.addLast(9);
        ad.addFirst(10);
        assertEquals(3,(int) ad.get(1));
        ad.addLast(12);
        ad.addLast(13);
        assertEquals(13, (int) ad.removeLast());
        ad.addLast(15);
        assertEquals(12, (int) ad.get(3));
        assertEquals(10, (int) ad.removeFirst());
        ad.addLast(18);
        assertEquals(18, (int) ad.get(4));
        ad.addFirst(20);
        assertEquals(18, (int) ad.removeLast());
        ad.addFirst(22);
    }

    @Test
    public void multipleADs() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        assertTrue(ad.isEmpty());
        for (int i = 0;i<100;i++){
            ad.addFirst(i);
        }
        assertFalse(ad.isEmpty());
        for (int i = 0;i<100;i++){
            ad.removeFirst();
        }
        assertTrue(ad.isEmpty());
        for (int i = 0;i<100;i++){
            ad.addFirst(i);
        }
        assertFalse(ad.isEmpty());
    }

    //@Test
    public void fillUpEmptyFillUpAgain() {

    }

    //int[] values = {90,20,901,469,512,862,974,418,492,541,86,453,62,378,634,623,103,581,249,653,448,158,12,465,700,621,47,120,357,310,32,600};
    @Test //uses the correct one and my version
    public void randomAddLastRemoveFirstisEmpty() {
        ArrayDeque<Integer> array = new ArrayDeque<Integer>(); //incorrect
        LinkedListDeque<Integer> array2 = new LinkedListDeque<Integer>(); //correct

        //String errorMessage = "\n";
        for (int i = 0; i < 10000; i += 1) {
            int value = StdRandom.uniform(1000);

            array.addLast(value);
            array2.addLast(value);
            //errorMessage = errorMessage + "addLast(" + value + ") \n";
            if (array.size() > 0 && array2.size() > 0 && value >= 500) {
                int actual = array2.removeFirst();
                int expected = array.removeFirst();
                //errorMessage = errorMessage + "removeFirst() ==> " + actual + ", " + expected + " \n";
                assertEquals(expected, actual);
            } else {
                assertEquals(array.isEmpty(), array2.isEmpty());
            }
        }
    }

    @Test
    public void randomAddFirstRemoveLastisEmpty() {
        ArrayDeque<Integer> array = new ArrayDeque<Integer>(); //incorrect
        LinkedListDeque<Integer> array2 = new LinkedListDeque<Integer>(); //correct

        for (int i = 0; i < 10000; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            int value = StdRandom.uniform(1000);
            array.addFirst(value);
            array2.addFirst(value);
            if (array.size() > 0 && array2.size() > 0 && value >= 500) {
                int actual = array2.removeLast();
                int expected = array.removeLast();
                assertEquals(expected, actual);
            } else {
                assertEquals(array.isEmpty(), array2.isEmpty());
            }
        }
    }

    @Test
    public void ensuringResizeDoesntCauseNulls() {
        ArrayDeque<Integer> ad = new ArrayDeque<Integer>();
        for (int i = 0;i < 18;i++) {
            ad.addFirst(i);
        }
        for (int i = 0;i < 18;i++) {
            ad.removeLast();
        }
    }

}
