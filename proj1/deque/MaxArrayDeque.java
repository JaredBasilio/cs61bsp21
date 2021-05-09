package deque;
import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparInit;

    public MaxArrayDeque(Comparator<T> c) {
        super();
        comparInit = c;
    }

    /** finds the max of the array with the initial comparator
     *
     * @return max value on the basis of comparator.
     */
    public T max() {
        if (isEmpty()) {
            return null;
        } else {
            int max = 0;
            for (int i = 0; i < size(); i += 1) {
                int cmp = comparInit.compare(get(i), get(max));
                if (cmp > 0) {
                    max = i;
                }
            }
            return get(max);
        }
    }

    /** finds the max of the array with the given comparator
     *
     * @return max value on the basis of comparator.
     */
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        } else {
            int max = 0;
            for (int i = 0; i < size(); i += 1) {
                int cmp = c.compare(get(i), get(max));
                if (cmp > 0) {
                    max = i;
                }
            }
            return get(max);
        }
    }
}
