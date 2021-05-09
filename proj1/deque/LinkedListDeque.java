package deque;
import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class TypeNode {
        private TypeNode prev;
        private T item;
        private TypeNode next;

        /** This function initializes a TypeNode
         * @param i - item of node, j - pointer to previous node, n - point to next node
         * */
        TypeNode(T i, TypeNode j, TypeNode n) {
            item = i;
            next = n;
            prev = j;
        }
    }

    private TypeNode sentinel;
    private TypeNode last;
    private TypeNode first;
    private int size;

    /** This function initializes a LinkedListDeque with a sentinel pointing at itself */
    public LinkedListDeque() {
        sentinel = new TypeNode(null, null, null);
        sentinel.prev = sentinel;
        sentinel.next = sentinel;
        first = null;
        last = null;
        size = 0;
    }

    /** This function adds an item x to the front of the linked list
     * @param x - value you wish to add
     * */
    @Override
    public void addFirst(T x) {
        if (first == null) {
            first = new TypeNode(x, sentinel, sentinel);
            last = first;
            sentinel.prev = last;
        } else {
            first = new TypeNode(x, sentinel, first);
            first.next.prev = first;
        }
        sentinel.next = first;
        size += 1;
    }

    /** This function adds an item x to the end of the linked list
     * @param x - value you wish to add
     * */
    @Override
    public void addLast(T x) {
        if (last == null) {
            last = new TypeNode(x, sentinel, sentinel);
            first = last;
            sentinel.next = first;
        } else {
            last.next = new TypeNode(x, last, sentinel);
            last = last.next;
        }
        sentinel.prev = last;
        size += 1;
    }

    @Override
    public int size() {
        return size;
    }

    /** This function prints all Node items in a linked list*/
    @Override
    public void printDeque() {
        TypeNode temp = first;
        while (temp.next.next != first) {
            System.out.print(temp.item + ", ");
            temp = temp.next;
        }
        System.out.print(temp.item);
        System.out.println();
    }

    /** This function removes the first item in the linked list
     * and reassigns the 2nd item and sentinel */
    @Override
    public T removeFirst() {
        if (size == 0) {
            return null;
        } else {
            T removed = first.item;
            if (first == last) {
                last = null;
                first = null;
                sentinel.next = sentinel;
                sentinel.prev = sentinel;
            } else {
                first = first.next;
                first.prev = sentinel;
                sentinel.next = first;
            }
            size -= 1;
            return removed;
        }
    }

    /** This function removes the last item in the linked list and reassigns the
     * 2nd to last item and sentinel
     * */
    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        } else {
            T removed = last.item;
            if (first == last) {
                last = null;
                first = null;
                sentinel.next = sentinel;
                sentinel.prev = sentinel;
            } else {
                last = last.prev;
                last.next = sentinel;
                sentinel.prev = last;
            }
            size -= 1;
            return removed;
        }
    }

    /** This function gets the item at the index using an iterative solution
     * @param index
     * @return item @ index of Type
     * */
    @Override
    public T get(int index) {
        TypeNode copy = first;
        while (index > 0) {
            copy = copy.next;
            index--;
        }
        return copy.item;
    }

    /** This method gets the item at index using a recursive solution
     * @param index
     * @return item @ index
     * */
    public T getRecursive(int index) {
        return recursiveHelp(first, index).item;
    }

    /** This method creates an iterator for a Linked LIst
     * @return Iterator
     * @source lecture
     * */
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /** This method uses the Iterator interface to make a LinkedListIterator
     * @source lecture {reformatted for assignment}
     * */
    private class LinkedListIterator implements Iterator<T> {
        private int pos;
        LinkedListIterator() {
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
    @Override
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

    /** This helper function assists the getRecursive method
     * @param index, node - node you will analyze
     * @return TypeNode @ index
     * */
    private TypeNode recursiveHelp(TypeNode node, int index) {
        if (index == 0) {
            return node;
        } else {
            return recursiveHelp(node.next, index - 1);
        }
    }
}
