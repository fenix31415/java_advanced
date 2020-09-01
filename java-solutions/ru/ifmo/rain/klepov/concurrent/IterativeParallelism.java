package ru.ifmo.rain.klepov.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * Implementation of AdvancedIP interface.
 */
public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper mapper;

	/**
	 * Constructs an instance without mapper
	 */
    public IterativeParallelism() {
        mapper = null;
    }

	/**
	 * Constructs an instance with mapper
	 */
    public IterativeParallelism(ParallelMapper mapper) {
        this.mapper = mapper;
    }

	/**
     * Returns maximum value.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @throws java.util.NoSuchElementException if input list is empty.
     * @return maximum of all input values.
     */
    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        if (values.isEmpty()) {
            throw new NoSuchElementException();
        }
        return process(threads, values, stream -> stream.max(comparator).get(), BinaryOperator.maxBy(comparator));
    }

	/**
     * Returns minimum value.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @throws java.util.NoSuchElementException if values is empty.
     * @return minimum of all input values.
     */
    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, Collections.reverseOrder(comparator));
    }

	/**
     * Returns whether any of values satisfies predicate.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @return bool equal to true, if predicate is true for all the values, false otherwise.
     */
    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return !any(threads, values, predicate.negate());
    }

    /**
     * Returns whether any of values satisfies predicate.
     * @param values list with input elements.
     * @throws IllegalArgumentException if threads is not positive.
     * @return bool equal to true, if predicate is true for any of values, false otherwise.
     */
    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return process(threads, values,
			s -> s.anyMatch(predicate), BinaryOperator.maxBy(Boolean::compare));
    }
	
	/**
     * Reduces values using monoid.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @return input values reduced by monoid.
     */
    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    /**
     * Maps and reduces values using monoid.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     */
    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> f, Monoid<R> monoid)
            throws InterruptedException {
        return process(threads, values,
                s -> s.map(f).reduce(monoid.getIdentity(), monoid.getOperator()),
                monoid.getOperator());
    }

	/**
     * Join values to string.
     * @param values list with input elements.
     * @return list of joined result of {@link #toString()} call on each value.
     * @throws IllegalArgumentException if amount of threads is not positive.
     */
    @Override
    public String join(int threads, List<?> values) throws InterruptedException {
        return process(threads, values,
                stream -> {
					StringBuilder sb = new StringBuilder();
					stream.forEach(sb::append);
					return sb;
				},
                (left, right) -> {
                    left.append(right.toString());
                    return left;
                }
        ).toString();
    }
	
    /**
     * Filters values by predicate.
     * @param values list with input elements.
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @return list of input values filtered by predicate.
     */
    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values,
			final Predicate<? super T> predicate) throws InterruptedException {
		return mapOrFilter(threads, values, stream -> stream.filter(predicate).collect(Collectors.toList()));
    }

	/**
     * @param values list with input elements.
     * @param f function to be applied to each element
     * @throws IllegalArgumentException if amount of threads is not positive.
     * @return list with elements under mapping with f.
     */
    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values,
			final Function<? super T, ? extends U> f) throws InterruptedException {
		return mapOrFilter(threads, values, stream -> stream.map(f).collect(Collectors.toList()));
    }
	
	private <T, U> List<U> mapOrFilter(int threads, final List<? extends T> values,
			final Function<? super Stream<? extends T>, ? extends List<U>> processor) 
					throws InterruptedException {
		return process(threads, values, processor,
				(left, right) -> {
					left.addAll(right);
					return left;
				});
	}

    private <T> List<Stream<? extends T>> cutList(int threads, final List<? extends T> elems) {
		final int n = elems.size();
        List<Stream<? extends T>> ans = new ArrayList<>(Collections.nCopies(threads, null));
        final int q = n / threads;
        final int r = n % threads;
		final int tq = threads - r;
        for (int i = 0; i < threads; i++) {
            final int left = i * q + ((i >= tq) ? i - tq : 0);
            final int right = left + q + ((i >= tq) ? 1 : 0);

            ans.set(i, elems.subList(left, right).stream());
        }
		return ans;
    }

    private void waitJoins(final List<Thread> threadsContainer) throws InterruptedException {
        InterruptedException finalEx = null;
        for (int i = 0; i < threadsContainer.size(); i++) {
            try {
                threadsContainer.get(i).join();
            } catch (InterruptedException ex) {
                if (finalEx == null) {
                    finalEx = new InterruptedException("Thread was interrupted");
                    for (int j = i; j < threadsContainer.size(); j++) {
                        threadsContainer.get(j).interrupt();
                    }
                }
                finalEx.addSuppressed(ex);
            }
        }
        if (finalEx != null) {
            throw finalEx;
        }
    }

    private <T, U> U process(int threads, final List<? extends T> values,
			final Function<? super Stream<? extends T>, ? extends U> processor,
			final BinaryOperator<U> combiner)
            throws InterruptedException {
        final int n = values.size();
        threads = Math.min(threads, n);
        Utils.checkThreads(threads);
        final List<Stream<? extends T>> subTasks = cutList(threads, values);
        List<U> res;

        if (mapper != null) {
            res = mapper.map(processor, subTasks);
        } else {
            final List<Thread> workers = new ArrayList<>();
            res = new ArrayList<>(Collections.nCopies(threads, null));
            for (int i = 0; i < threads; i++) {
                final int pos = i;
                Utils.addAndStart(workers, new Thread(() -> res.set(pos, processor.apply(subTasks.get(pos)))));
            }
            waitJoins(workers);
        }
        return res.stream().reduce(combiner).get();
    }
}