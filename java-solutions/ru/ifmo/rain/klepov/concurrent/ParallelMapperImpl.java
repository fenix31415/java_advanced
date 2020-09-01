package ru.ifmo.rain.klepov.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.Collections;

/**
 * Implementation of ParallelMapper interface.
 */
public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> workers;
    private final Queue<Runnable> tasks;

	/**
	 * Constructs an instance containing specific number of threads.
	 * @param threads number of threads.
	 */
    public ParallelMapperImpl(final int threads) {
        Utils.checkThreads(threads);
        tasks = new ArrayDeque<>();
        workers = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Utils.addAndStart(workers, new Thread(() -> {
                try {
                    while (!Thread.interrupted()) {
                        Runnable task;
						synchronized (tasks) {
							while (tasks.isEmpty()) {
								tasks.wait();
							}
							task = tasks.remove();
							tasks.notifyAll();
						}
						task.run();
                    }
                } catch (InterruptedException ignored) { }
            }));
        }
    }
	
	/** Stops all threads. All unfinished mappings leave in undefined state. */
	@Override
    public void close() {
        workers.forEach(Thread::interrupt);
		for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException ignored) { }
        }
    }
	
	/**
     * Maps function {@code f} over specified {@code args}.
     * Mapping for each element performs in parallel.
     *
     * @throws InterruptedException if calling thread was interrupted
     */
    @Override
    public <T, R> List<R> map(Function<? super T, ? extends R> f, List<? extends T> args) throws InterruptedException {
        AnswersContainer<R> answers = new AnswersContainer<>(args.size());
        for (int i = 0; i < args.size(); i++) {
            final int ind = i;
			//addTask(() -> answers.set(ind, f.apply(args.get(ind))));
            addTask(() -> {
				R value = null;
				RuntimeException curException = null;
				try {
					value = f.apply(args.get(ind));
				} catch (final RuntimeException e) {
					curException = e;
				}
				
				synchronized (answers) {
					if (curException != null) {
						if (answers.savedException != null) {
							answers.savedException.addSuppressed(curException);
						} else {
							answers.savedException = curException;
						}
					}
				}
				answers.set(ind, value);
			});
			
        }
        return answers.waitRes();
    }
	
    private void addTask(final Runnable task) throws InterruptedException {
        synchronized (tasks) {
            tasks.add(task);
            tasks.notifyAll();
        }
    }

    private class AnswersContainer<R> {
		private RuntimeException savedException;
        private final List<R> ans;
        private int filled;

        AnswersContainer(final int size) {
			savedException = null;
            ans = new ArrayList<>(Collections.nCopies(size, null));
            filled = 0;
        }

        void set(final int pos, R data) {
            ans.set(pos, data);
            synchronized (this) {
                if (++filled == ans.size()) {
                    notify();
                }
            }
        }

        synchronized List<R> waitRes() throws InterruptedException {
            while (filled < ans.size()) {
                wait();
            }
			if (savedException != null) {
				throw savedException;
			}
            return ans;
        }
    }
}