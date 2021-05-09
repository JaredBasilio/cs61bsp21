package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private T[] items;
    private int size;
    private int front;
    private int back;

    /** Creates an empty list. */
    public ArrayDeque() {
        items = (T[]) new Object[8];
        front = items.length / 2 - 1;
        back = items.length / 2;
        size = 0;
    }

    /** This function returns the size of the array.
     * @return size int
     * */
    @Override
    public int size() {
        return size;
    }

    /** This function adds an item at the indice front and increases the size
     * @param item - item you wish to add
     * */
    @Override
    public void addFirst(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        if (front == 0) {
            items[items.length - 1] = item;
            front = items.length - 1;
        } else {
            items[front - 1] = item;
            front = front - 1;
        }
        if (items[back] == null) {
            back = front;
        }
        size += 1;
    }

    /** This function adds an item at the indice back and increases the size
     * @param item - item you wish to add
     * */
    @Override
    public void addLast(T item) {
        if (size == items.length) {
            resize(size * 2);
        }
        if (back + 1 > items.length - 1) {
            items[0] = item;
            back = 0;
        } else {
            items[back + 1] = item;
            back = back + 1;
        }
        if (items[front] == null) {
            front = back;
        }
        size += 1;
    }

    /** This function removes the last item in your array.
     * @return removed item
     * */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            double capacityPercentage = (double) size / items.length;
            if (capacityPercentage < 0.25 && items.length >= 16) {
                resize(size * 2);
            }
            T removed = items[back];
            items[back] = null;
            if (front > back) {
                if (back == 0) {
                    back = items.length - 1;
                } else {
                    back -= 1;
                }
            } else if (back > front) {
                back -= 1;
            }
            size -= 1;
            return removed;
        }
    }

    /** This function removes the first item in your array.
     * @return removed item
     * */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            double capacityPercentage = (double) size / items.length;
            if (capacityPercentage < 0.25 && items.length >= 16) {
                resize(size * 2);
            }
            T removed = items[front];
            items[front] = null;
            if (front > back) {
                if (front >= items.length - 1) {
                    front = 0;
                } else {
                    front += 1;
                }
            } else if (back > front) {
                front += 1;
            }
            size -= 1;
            return removed;
        }
    }

    /** This function prints the items in a
     * list starting from front and prints new line at the end*/
    @Override
    public void printDeque() {
        for (int i = front; i != back; i++) {
            System.out.print(items[i] + ", ");
            if (i + 1 == items.length) {
                i = 0;
            }
        }
        System.out.print(items[back]);
        System.out.println();
    }

    /** This function gets at the index.
     * @pararm i = index
     * @return item at the back
     * */
    public T get(int i) {
        if (i > size) {
            return null;
        }
        if ((front + i) >= items.length) {
            return items[(front + i) % items.length];
        } else {
            return items[front + i];
        }
    }

    /** This method creates an iterator for an Array
     * @return Iterator
     * @source lecture
     * */
    public Iterator<T> iterator() {
        return new Array();
    }

    /** This method uses the Iterator interface to make an Array Iterator
     * @source lecture {reformatted for assignment}
     * */
    private class Array implements Iterator<T> {
        private int pos;
        Array() {
            pos = 0;
        }
        public boolean hasNext() {
            return pos < size;
        }

        public T next() {
            T returnItem = get(pos);
            pos += 1;
            return returnItem;
        }
    }

    /** checks if the two objects are equal
     * @param other object
     * @return boolean
     * */
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other instanceof Deque) {
            Deque<T> o = (Deque<T>) other;
            if (o.size() != this.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i += 1) {
                if (!this.get(i).equals(o.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /** This helper function resizes the array so that it can fit to the new capacity
     * @pararm i = index
     * @return item at the back
     * */
    private void resize(int capacity) {
        T[] a = (T[]) new Object[capacity];
        if (front < back) {
            System.arraycopy(items, front, a, 0, size);
            front = 0;
            back = size - 1;
        } else {
            int frontLength = items.length - front;
            System.arraycopy(items, front, a, capacity - frontLength, frontLength);
            System.arraycopy(items, 0, a, 0, back + 1);
            front = capacity - frontLength;
        }
        items = a;
    }
}
