/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.internal.util;

import static org.apache.ignite.internal.util.ArrayUtils.nullOrEmpty;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

/**
 * Minimal list API to work with primitive ints. This list exists to avoid boxing/unboxing when using standard list from Java.
 */
public class IgniteIntList implements Externalizable {
    /** Serial version uid. */
    private static final long serialVersionUID = 0L;

    /** Array. */
    private int[] arr;

    /** Index. */
    private int idx;

    /**
     * Default constructor.
     */
    public IgniteIntList() {
        // No-op.
    }

    /**
     * Constructor.
     *
     * @param size Size.
     */
    public IgniteIntList(int size) {
        arr = new int[size];
        // idx = 0
    }

    /**
     * Constructor.
     *
     * @param arr Array.
     */
    public IgniteIntList(int[] arr) {
        this.arr = arr;

        idx = arr.length;
    }

    /**
     * Returns list from values.
     *
     * @param vals Values.
     * @return List from values.
     */
    public static IgniteIntList asList(int... vals) {
        if (nullOrEmpty(vals)) {
            return new IgniteIntList();
        }

        return new IgniteIntList(vals);
    }

    /**
     * Constructor.
     *
     * @param arr  Array.
     * @param size Size.
     */
    private IgniteIntList(int[] arr, int size) {
        this.arr = arr;
        idx = size;
    }

    /**
     * Returns copy of this list.
     *
     * @return Copy of this list.
     */
    public IgniteIntList copy() {
        if (idx == 0) {
            return new IgniteIntList();
        }

        return new IgniteIntList(Arrays.copyOf(arr, idx));
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IgniteIntList)) {
            return false;
        }

        IgniteIntList that = (IgniteIntList) o;

        if (idx != that.idx) {
            return false;
        }

        if (idx == 0 || arr == that.arr) {
            return true;
        }

        for (int i = 0; i < idx; i++) {
            if (arr[i] != that.arr[i]) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int res = 1;

        for (int i = 0; i < idx; i++) {
            int element = arr[i];
            res = 31 * res + element;
        }

        return res;
    }

    /**
     * Adds list of elements.
     *
     * @param l List to add all elements of.
     */
    public void addAll(IgniteIntList l) {
        assert l != null;

        if (l.isEmpty()) {
            return;
        }

        if (arr == null) {
            arr = new int[4];
        }

        int len = arr.length;

        while (len < idx + l.size()) {
            len <<= 1;
        }

        arr = Arrays.copyOf(arr, len);

        System.arraycopy(l.arr, 0, arr, idx, l.size());

        idx += l.size();
    }

    /**
     * Adds all elements from {@code from} to {@code to}.
     *
     * @param to   To list.
     * @param from From list.
     * @return To list (passed in or created).
     */
    public static IgniteIntList addAll(@Nullable IgniteIntList to, IgniteIntList from) {
        if (to == null) {
            IgniteIntList res = new IgniteIntList(from.size());

            res.addAll(from);

            return res;
        } else {
            to.addAll(from);

            return to;
        }
    }

    /**
     * Add element to this array.
     *
     * @param x Value.
     */
    public void add(int x) {
        if (arr == null) {
            arr = new int[4];
        } else if (arr.length == idx) {
            arr = Arrays.copyOf(arr, arr.length << 1);
        }

        arr[idx++] = x;
    }

    /**
     * Clears the list.
     */
    public void clear() {
        idx = 0;
    }

    /**
     * Gets the last element.
     *
     * @return The last element.
     */
    public int last() {
        return arr[idx - 1];
    }

    /**
     * Removes and returns the last element of the list. Complementary method to {@link #add(int)} for stack like usage.
     *
     * @return Removed element.
     * @throws NoSuchElementException If the list is empty.
     */
    public int remove() throws NoSuchElementException {
        if (idx == 0) {
            throw new NoSuchElementException();
        }

        return arr[--idx];
    }

    /**
     * Returns (possibly reordered) copy of this list, excluding all elements of given list.
     *
     * @param l List of elements to remove.
     * @return New list without all elements from {@code l}.
     */
    public IgniteIntList copyWithout(IgniteIntList l) {
        assert l != null;

        if (idx == 0) {
            return new IgniteIntList();
        }

        if (l.idx == 0) {
            return new IgniteIntList(Arrays.copyOf(arr, idx));
        }

        int[] newArr = Arrays.copyOf(arr, idx);
        int newIdx = idx;

        for (int i = 0; i < l.size(); i++) {
            int rmVal = l.get(i);

            for (int j = 0; j < newIdx; j++) {
                if (newArr[j] == rmVal) {

                    while (newIdx > 0 && newArr[newIdx - 1] == rmVal) {
                        newIdx--;
                    }

                    if (newIdx > 0) {
                        newArr[j] = newArr[newIdx - 1];
                        newIdx--;
                    }
                }
            }
        }

        return new IgniteIntList(newArr, newIdx);
    }

    /**
     * Returns value by index.
     *
     * @param i Index.
     * @return Value.
     */
    public int get(int i) {
        assert i < idx;

        return arr[i];
    }

    /**
     * Returns size.
     *
     * @return Size.
     */
    public int size() {
        return idx;
    }

    /**
     * Returns {@code true} if this list has no elements.
     *
     * @return {@code True} if this list has no elements.
     */
    public boolean isEmpty() {
        return idx == 0;
    }

    /**
     * Checks if there is an element.
     *
     * @param l Element to find.
     * @return {@code True} if found.
     */
    public boolean contains(int l) {
        for (int i = 0; i < idx; i++) {
            if (arr[i] == l) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if all the elements are present.
     *
     * @param l List to check.
     * @return {@code True} if this list contains all the elements of passed in list.
     */
    public boolean containsAll(IgniteIntList l) {
        for (int i = 0; i < l.size(); i++) {
            if (!contains(l.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that there are no duplicates.
     *
     * @return {@code True} if there are no duplicates.
     */
    public boolean distinct() {
        for (int i = 0; i < idx; i++) {
            for (int j = i + 1; j < idx; j++) {
                if (arr[i] == arr[j]) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Truncate the list.
     *
     * @param size New size.
     * @param last If {@code true} the last elements will be removed, otherwise the first.
     */
    public void truncate(int size, boolean last) {
        assert size >= 0 && size <= idx;

        if (size == idx) {
            return;
        }

        if (!last && idx != 0 && size != 0) {
            System.arraycopy(arr, idx - size, arr, 0, size);
        }

        idx = size;
    }

    /**
     * Removes element by given index.
     *
     * @param i Index.
     * @return Removed value.
     */
    public int removeIndex(int i) {
        assert i < idx : i;

        int res = arr[i];

        if (i == idx - 1) { // Last element.
            idx = i;
        } else {
            System.arraycopy(arr, i + 1, arr, i, idx - i - 1);
            idx--;
        }

        return res;
    }

    /**
     * Removes value from this list.
     *
     * @param startIdx Index to begin search with.
     * @param val      Value.
     * @return Index of removed value if the value was found and removed or {@code -1} otherwise.
     */
    public int removeValue(int startIdx, int val) {
        assert startIdx >= 0;

        for (int i = startIdx; i < idx; i++) {
            if (arr[i] == val) {
                removeIndex(i);

                return i;
            }
        }

        return -1;
    }

    /**
     * Removes value from this list.
     *
     * @param startIdx Index to begin search with.
     * @param oldVal   Old value.
     * @param newVal   New value.
     * @return Index of replaced value if the value was found and replaced or {@code -1} otherwise.
     */
    public int replaceValue(int startIdx, int oldVal, int newVal) {
        for (int i = startIdx; i < idx; i++) {
            if (arr[i] == oldVal) {
                arr[i] = newVal;

                return i;
            }
        }

        return -1;
    }

    /**
     * Returns array copy.
     *
     * @return Array copy.
     */
    public int[] array() {
        int[] res = new int[idx];

        System.arraycopy(arr, 0, res, 0, idx);

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(idx);

        for (int i = 0; i < idx; i++) {
            out.writeInt(arr[i]);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        idx = in.readInt();

        arr = new int[idx];

        for (int i = 0; i < idx; i++) {
            arr[i] = in.readInt();
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("[");

        for (int i = 0; i < idx; i++) {
            if (i != 0) {
                b.append(',');
            }

            b.append(arr[i]);
        }

        b.append(']');

        return b.toString();
    }

    /**
     * Reads list form {@code in}.
     *
     * @param in Input to read list from.
     * @return Grid int list.
     * @throws IOException If failed.
     */
    @Nullable
    public static IgniteIntList readFrom(DataInput in) throws IOException {
        int idx = in.readInt();

        if (idx == -1) {
            return null;
        }

        int[] arr = new int[idx];

        for (int i = 0; i < idx; i++) {
            arr[i] = in.readInt();
        }

        return new IgniteIntList(arr);
    }

    /**
     * Writes list to {@code out}.
     *
     * @param out  Output to write to.
     * @param list List.
     * @throws IOException If failed.
     */
    public static void writeTo(DataOutput out, @Nullable IgniteIntList list) throws IOException {
        out.writeInt(list != null ? list.idx : -1);

        if (list != null) {
            for (int i = 0; i < list.idx; i++) {
                out.writeInt(list.arr[i]);
            }
        }
    }

    /**
     * Sorts this list. Use {@code copy().sort()} if you need a defensive copy.
     *
     * @return {@code this} For chaining.
     */
    public IgniteIntList sort() {
        if (idx > 1) {
            Arrays.sort(arr, 0, idx);
        }

        return this;
    }

    /**
     * Removes given number of elements from the end. If the given number of elements is higher than list size, then list will be cleared.
     *
     * @param cnt Count to pop from the end.
     */
    public void pop(int cnt) {
        assert cnt >= 0 : cnt;

        if (idx < cnt) {
            idx = 0;
        } else {
            idx -= cnt;
        }
    }

    /**
     * Returns iterator.
     *
     * @return Iterator.
     */
    public IgniteIntIterator iterator() {
        return new IgniteIntIterator() {
            int cur = 0;

            @Override
            public boolean hasNext() {
                return cur < idx;
            }

            @Override
            public int next() {
                return arr[cur++];
            }
        };
    }
}
