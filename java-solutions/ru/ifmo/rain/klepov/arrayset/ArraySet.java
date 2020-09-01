package ru.ifmo.rain.klepov.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements NavigableSet<T> {
    private final List<T> data;
    private final Comparator<? super T> comparator;
    private final Comparator<? super T> given_cmp;

    public ArraySet() {
        this(List.of(), null);
    }

    public ArraySet(Collection<? extends T> collection) {
        this(collection, null);
    }

    @SuppressWarnings("unchecked")
    private ArraySet(List<T> list, Comparator<? super T> cmp) {
        data = list;
        given_cmp = cmp == Comparator.naturalOrder() ? null : cmp;
        comparator = (cmp == null) ? (Comparator<? super T>) Comparator.naturalOrder() : cmp;
    }

    @SuppressWarnings("unchecked")
    public ArraySet(Collection<? extends T> collection, Comparator<? super  T> cmp) {
        given_cmp = cmp == Comparator.naturalOrder() ? null : cmp;
        comparator = (cmp == null) ? (Comparator<? super T>) Comparator.naturalOrder() : cmp;
        Set<T> set = new TreeSet<>(cmp);
        set.addAll(collection);
        data = List.copyOf(set);

    }

    private boolean isNormInd(int ind) {
        return 0 <= ind && ind < data.size();
    }

    private int findInd(T t, int df, int dnf) {
        int ind = Collections.binarySearch(data, Objects.requireNonNull(t), comparator);
        if (ind < 0) {
            ind = -ind - 1;
            return isNormInd(ind + dnf) ? ind + dnf : -1;
        }
        return isNormInd(ind) ? ind + df : -1;
    }

    private T findElement(T t, int df, int dnf) {
        int ind = findInd(t, df, dnf);
        return isNormInd(ind) ? data.get(ind) : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        return Collections.binarySearch(data, (T) Objects.requireNonNull(o), comparator) >= 0;
    }

    @Override
    public T lower(T t) {
        return findElement(t, -1,-1);
    }

    @Override
    public T floor(T t) {
        return findElement(t, 0,-1);
    }

    @Override
    public T ceiling(T t) {
        return findElement(t, 0,0);
    }

    @Override
    public T higher(T t) {
        return findElement(t, 1,0);
    }

    @Override
    public T pollFirst() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T pollLast() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }

    @Override
    public NavigableSet<T> descendingSet() {
        return new ArraySet<>(new ReversingList<>(data), Collections.reverseOrder(comparator));
    }

    @Override
    public Iterator<T> descendingIterator() {
        return new ReversingList<>(data).iterator();
    }

    public ArraySet(Comparator<? super T> cmp) {
        this(Collections.emptyList(), cmp);
    }

    private int compare(T x, T y) {
        return comparator.compare(x, y);
    }

    @Override
    public NavigableSet<T> subSet(T fromElement, boolean fromInclusive, T toElement, boolean toInclusive) {
        if (compare(fromElement, toElement) > 0) {
            throw new IllegalArgumentException();
        }
        int from = findInd(fromElement,  fromInclusive ? 0 : 1 , 0);
        int to = toInclusive ? findInd(toElement, 0, -1) : findInd(toElement, -1, -1);
        if (from == -1 || to == -1 || from > to) {
            return new ArraySet<>(comparator);
        }
        return new ArraySet<>(data.subList(from, to + 1), comparator);
    }

    @Override
    public NavigableSet<T> headSet(T toElement, boolean inclusive) {
        if (size() == 0)
            return new ArraySet<>(comparator);
        int ind = findFromTail(toElement, inclusive) + 1;
        return new ArraySet<>(data.subList(0, ind), comparator);
    }

    private int findFromHead(T el, boolean inclusive) {
        int ind = Collections.binarySearch(data, el, comparator);
        return ind < 0 ? ~ind : (inclusive ? ind : ind + 1);
    }

    private int findFromTail(T el, boolean inclusive) {
        int ind = Collections.binarySearch(data, el, comparator);
        return ind < 0 ? ~ind - 1 : (inclusive ? ind : ind - 1);
    }

    @Override
    public NavigableSet<T> tailSet(T fromElement, boolean inclusive) {
        if (size() == 0)
            return new ArraySet<>(comparator);
        int ind = findFromHead(fromElement, inclusive);
        return new ArraySet<>(data.subList(ind, data.size()), comparator);
    }

    @Override
    public Comparator<? super T> comparator() {
        return given_cmp;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return tailSet(fromElement, true);
    }

    @Override
    public T first() {
        if (size() == 0)
            throw new NoSuchElementException();
        return data.get(0);
    }

    @Override
    public T last() {
        if (size() == 0)
            throw new NoSuchElementException();
        return data.get(size() - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
