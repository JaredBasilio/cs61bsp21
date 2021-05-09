package tester;
import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    /**
     * This function tests if a studentArrayDeque and a correct arrayDeque consistently
     * produce the same output with the following operations.
     * @source Lecture 3
     */
    @Test
    public void test() {
        StudentArrayDeque<Integer> array = new StudentArrayDeque<Integer>();
        ArrayDequeSolution<Integer> array2 = new ArrayDequeSolution<Integer>();

        String errorMessage = "\n";
        for (int i = 0; i < 1000; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            int value = StdRandom.uniform(1000);
            if (numberBetweenZeroAndOne < 0.5) {
                array.addLast(value);
                array2.addLast(value);
                errorMessage = errorMessage + "addLast(" + value + ") \n";
            } else {
                array.addFirst(value);
                array2.addFirst(value);
                errorMessage = errorMessage + "addFirst(" + value + ") \n";
            }
            if (array.size() > 0 && array2.size() > 0 && value >= 500) {
                if (array.size() % 3 == 0) {
                    int expected = array2.removeLast();
                    int actual = array.removeLast();
                    errorMessage = errorMessage + "removeLast() \n";
                    assertEquals(errorMessage, expected, actual);
                } else {
                    int expected = array2.removeFirst();
                    int actual = array.removeFirst();
                    errorMessage = errorMessage + "removeFirst() \n";
                    assertEquals(errorMessage, expected, actual);
                }
            }
        }
    }
}
