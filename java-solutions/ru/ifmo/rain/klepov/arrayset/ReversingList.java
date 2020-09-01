package ru.ifmo.rain.klepov.arrayset;

import java.util.AbstractList;
import java.util.List;

public class ReversingList<T> extends AbstractList<T> {
    private List<T> data;
    private boolean notReversed;

    public ReversingList(List<T> other) {
        if (other instanceof ReversingList) {
            notReversed = !((ReversingList<T>) other).notReversed;
            data = ((ReversingList<T>) other).data;
        } else {
            notReversed = false;
            data = other;
        }
    }

    @Override
    public T get(int ind) {
        return data.get(notReversed ? ind : size() - ind - 1);
    }

    @Override
    public int size() {
        return data.size();
    }
}
